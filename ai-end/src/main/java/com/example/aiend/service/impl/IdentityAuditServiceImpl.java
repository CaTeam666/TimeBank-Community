package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiend.common.enums.AuditStatusEnum;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.IdentityAuditQueryDTO;
import com.example.aiend.dto.request.IdentityAuditResultDTO;
import com.example.aiend.dto.request.SystemSettingsDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.entity.CoinLog;
import com.example.aiend.entity.IdentityAudit;
import com.example.aiend.entity.User;
import com.example.aiend.mapper.CoinLogMapper;
import com.example.aiend.mapper.IdentityAuditMapper;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.service.IdentityAuditService;
import com.example.aiend.service.SettingsService;
import com.example.aiend.vo.IdentityAuditVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.aliyun.oss.OSS;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实名认证审核服务实现类
 *
 * @author AI-End
 * @since 2025-12-24
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IdentityAuditServiceImpl implements IdentityAuditService {
    
    private final IdentityAuditMapper identityAuditMapper;
    private final UserMapper userMapper;
    private final CoinLogMapper coinLogMapper;
    private final SettingsService settingsService;
    private final OSS ossClient;
    
    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;
    
    @Value("${aliyun.oss.url-prefix}")
    private String urlPrefix;
    
    /**
     * 老人角色常量（数据库存储为中文）
     */
    private static final String ROLE_ELDER = "老人";
    
    /**
     * 时间币流水类型 - 系统调整（赠送初始时间币）
     */
    private static final int COIN_TYPE_SYSTEM = 4;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public PageResponseDTO<IdentityAuditVO> getAuditList(IdentityAuditQueryDTO queryDTO) {
        log.info("查询实名审核列表，查询条件：{}", queryDTO);
        
        // 构建查询条件
        LambdaQueryWrapper<IdentityAudit> queryWrapper = new LambdaQueryWrapper<>();
        
        // 状态筛选
        if (StringUtils.hasText(queryDTO.getStatus())) {
            AuditStatusEnum statusEnum = AuditStatusEnum.fromStatus(queryDTO.getStatus());
            queryWrapper.eq(IdentityAudit::getStatus, statusEnum.getCode());
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(IdentityAudit::getCreateTime);
        
        // 分页查询
        Page<IdentityAudit> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<IdentityAudit> auditPage = identityAuditMapper.selectPage(page, queryWrapper);
        
        // 转换为 VO
        List<IdentityAuditVO> auditVOList = auditPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 构建分页响应
        return PageResponseDTO.<IdentityAuditVO>builder()
                .list(auditVOList)
                .total(auditPage.getTotal())
                .page(queryDTO.getPage())
                .pageSize(queryDTO.getPageSize())
                .build();
    }
    
    @Override
    public IdentityAuditVO getAuditDetail(Long id) {
        log.info("获取实名审核详情，审核ID：{}", id);
        
        IdentityAudit audit = identityAuditMapper.selectById(id);
        if (audit == null) {
            throw new BusinessException(404, "审核任务不存在");
        }
        
        return convertToVO(audit);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAuditResult(Long id, IdentityAuditResultDTO resultDTO) {
        log.info("提交审核结果，审核ID：{}，审核状态：{}", id, resultDTO.getStatus());
        
        // 查询审核任务是否存在
        IdentityAudit audit = identityAuditMapper.selectById(id);
        if (audit == null) {
            throw new BusinessException(404, "审核任务不存在");
        }
        
        // 检查当前状态是否为待审核
        if (!AuditStatusEnum.PENDING.getCode().equals(audit.getStatus())) {
            throw new BusinessException(400, "该审核任务已处理，无法重复操作");
        }
        
        // 如果是驳回，检查驳回原因
        if ("REJECTED".equals(resultDTO.getStatus()) && !StringUtils.hasText(resultDTO.getRejectReason())) {
            throw new BusinessException(400, "驳回时必须填写驳回原因");
        }
        
        // 更新审核状态
        AuditStatusEnum statusEnum = AuditStatusEnum.fromStatus(resultDTO.getStatus());
        audit.setStatus(statusEnum.getCode());
        audit.setAuditTime(LocalDateTime.now());
        
        // 如果是驳回，设置驳回原因
        if ("REJECTED".equals(resultDTO.getStatus())) {
            audit.setRejectReason(resultDTO.getRejectReason());
        }
        
        int updateCount = identityAuditMapper.updateById(audit);
        
        if (updateCount == 0) {
            throw new BusinessException(500, "审核结果提交失败");
        }
        
        // 审核通过时，判断用户角色并赠送初始时间币
        if (AuditStatusEnum.APPROVED.getCode().equals(statusEnum.getCode())) {
            grantElderInitialCoinsIfElder(audit.getUserId());
        }
        
        log.info("审核结果提交成功，审核ID：{}，审核状态：{}", id, resultDTO.getStatus());
    }
    
    /**
     * 审核通过后，如果用户角色为老人，贠送初始时间币
     *
     * @param userId 用户ID（审核表中的用户ID）
     */
    private void grantElderInitialCoinsIfElder(Long userId) {
        // 查询用户信息（sys_user.id 为 varchar，需要转换）
        User user = userMapper.selectById(String.valueOf(userId));
        if (user == null) {
            log.warn("审核通过后查询用户失败，userId：{}", userId);
            return;
        }
        
        // 判断是否为老人角色
        if (!ROLE_ELDER.equals(user.getRole())) {
            log.info("用户角色非老人，不贠送初始时间币，userId：{}，role：{}", userId, user.getRole());
            return;
        }
        
        // 获取系统配置中的老人初始时间币数量
        SystemSettingsDTO settings = settingsService.getSettings();
        Integer initialCoins = settings.getElderInitialCoins();
        
        if (initialCoins == null || initialCoins <= 0) {
            log.info("老人初始时间币配置为0，不贠送，userId：{}", userId);
            return;
        }
        
        // 更新用户余额：增加初始时间币
        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, user.getId())
                .setSql("balance = COALESCE(balance, 0) + " + initialCoins);
        userMapper.update(null, updateWrapper);
        
        // 记录时间币流水
        CoinLog coinLog = new CoinLog();
        coinLog.setUserId(userId);
        coinLog.setAmount(initialCoins);
        coinLog.setType(COIN_TYPE_SYSTEM);
        coinLog.setCreateTime(LocalDateTime.now());
        coinLogMapper.insert(coinLog);
        
        log.info("老人实名审核通过，赠送初始时间币：{}，userId：{}", initialCoins, userId);
    }
    
    /**
     * 将 IdentityAudit 实体转换为 IdentityAuditVO
     *
     * @param audit 审核实体
     * @return 审核VO
     */
    private IdentityAuditVO convertToVO(IdentityAudit audit) {
        String submitTime = audit.getCreateTime() != null 
                ? audit.getCreateTime().format(DATE_TIME_FORMATTER) 
                : null;
        
        // 转换状态码为状态字符串
        String status = AuditStatusEnum.fromCode(audit.getStatus()).getStatus();
        
        // 查询用户角色以填充角色字段
        User user = userMapper.selectById(String.valueOf(audit.getUserId()));
        String role = (user != null) ? user.getRole() : null;
        
        // 为身份证照片生成带签名的 URL（30分钟有效）
        String idCardFront = generateSignedUrl(audit.getIdCardFront());
        String idCardBack = generateSignedUrl(audit.getIdCardBack());
        
        return IdentityAuditVO.builder()
                .id(String.valueOf(audit.getId()))
                .userId(String.valueOf(audit.getUserId()))
                .userName(audit.getRealName())
                .submitTime(submitTime)
                .ocrAge(audit.getAge())
                .idCardFront(idCardFront)
                .idCardBack(idCardBack)
                .ocrName(audit.getRealName())
                .ocrIdNumber(audit.getIdCard())
                .status(status)
                .role(role)
                .build();
    }
    
    /**
     * 为私有 OSS 资源生成带签名的临时访问 URL
     *
     * @param originalUrl 原始 URL
     * @return 带签名的 URL（非 OSS 资源则返回原 URL）
     */
    private String generateSignedUrl(String originalUrl) {
        if (!StringUtils.hasText(originalUrl) || !originalUrl.startsWith(urlPrefix)) {
            return originalUrl;
        }
        
        try {
            // 提取 OSS 中的文件路径 (Key)
            String ossKey = originalUrl.substring(urlPrefix.length());
            if (ossKey.startsWith("/")) {
                ossKey = ossKey.substring(1);
            }
            
            // 设置过期时间为 30 分钟后 (审核员查看需要较长时间)
            java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + 30 * 60 * 1000);
            
            // 生成签名 URL
            java.net.URL signedUrl = ossClient.generatePresignedUrl(bucketName, ossKey, expiration);
            String finalUrl = signedUrl.toString();
            
            log.debug("已为证件照生成签名 URL: {}", finalUrl);
            return finalUrl;
        } catch (Exception e) {
            log.warn("生成 OSS 签名 URL 失败，返回原始 URL: {}", originalUrl, e);
            return originalUrl;
        }
    }
}
