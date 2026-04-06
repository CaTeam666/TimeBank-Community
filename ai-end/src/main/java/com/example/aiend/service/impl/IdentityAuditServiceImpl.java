package com.example.aiend.service.impl;

import com.aliyun.oss.OSS;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class IdentityAuditServiceImpl implements IdentityAuditService {

    private static final String ROLE_ELDER = "老人";
    private static final int COIN_TYPE_SYSTEM = 4;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final IdentityAuditMapper identityAuditMapper;
    private final UserMapper userMapper;
    private final CoinLogMapper coinLogMapper;
    private final SettingsService settingsService;
    private final ObjectProvider<OSS> ossClientProvider;

    @Value("${aliyun.oss.bucket-name:}")
    private String bucketName;

    @Value("${aliyun.oss.url-prefix:}")
    private String urlPrefix;

    @Override
    public PageResponseDTO<IdentityAuditVO> getAuditList(IdentityAuditQueryDTO queryDTO) {
        LambdaQueryWrapper<IdentityAudit> queryWrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(queryDTO.getStatus())) {
            AuditStatusEnum statusEnum = AuditStatusEnum.fromStatus(queryDTO.getStatus());
            queryWrapper.eq(IdentityAudit::getStatus, statusEnum.getCode());
        }
        queryWrapper.orderByDesc(IdentityAudit::getCreateTime);

        Page<IdentityAudit> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<IdentityAudit> auditPage = identityAuditMapper.selectPage(page, queryWrapper);
        List<IdentityAuditVO> auditVOList = auditPage.getRecords().stream().map(this::convertToVO).collect(Collectors.toList());
        return PageResponseDTO.<IdentityAuditVO>builder()
                .list(auditVOList)
                .total(auditPage.getTotal())
                .page(queryDTO.getPage())
                .pageSize(queryDTO.getPageSize())
                .build();
    }

    @Override
    public IdentityAuditVO getAuditDetail(Long id) {
        IdentityAudit audit = identityAuditMapper.selectById(id);
        if (audit == null) {
            throw new BusinessException(404, "审核任务不存在");
        }
        return convertToVO(audit);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAuditResult(Long id, IdentityAuditResultDTO resultDTO) {
        IdentityAudit audit = identityAuditMapper.selectById(id);
        if (audit == null) {
            throw new BusinessException(404, "审核任务不存在");
        }
        if (!AuditStatusEnum.PENDING.getCode().equals(audit.getStatus())) {
            throw new BusinessException(400, "该审核任务已处理");
        }
        if ("REJECTED".equals(resultDTO.getStatus()) && !StringUtils.hasText(resultDTO.getRejectReason())) {
            throw new BusinessException(400, "驳回时必须填写驳回原因");
        }

        AuditStatusEnum statusEnum = AuditStatusEnum.fromStatus(resultDTO.getStatus());
        audit.setStatus(statusEnum.getCode());
        audit.setAuditTime(LocalDateTime.now());
        if ("REJECTED".equals(resultDTO.getStatus())) {
            audit.setRejectReason(resultDTO.getRejectReason());
        }
        int updateCount = identityAuditMapper.updateById(audit);
        if (updateCount == 0) {
            throw new BusinessException(500, "审核结果提交失败");
        }
        if (AuditStatusEnum.APPROVED.getCode().equals(statusEnum.getCode())) {
            grantElderInitialCoinsIfElder(audit.getUserId());
        }
    }

    private void grantElderInitialCoinsIfElder(Long userId) {
        User user = userMapper.selectById(String.valueOf(userId));
        if (user == null || !ROLE_ELDER.equals(user.getRole())) {
            return;
        }

        SystemSettingsDTO settings = settingsService.getSettings();
        Integer initialCoins = settings.getElderInitialCoins();
        if (initialCoins == null || initialCoins <= 0) {
            return;
        }

        LambdaUpdateWrapper<User> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(User::getId, user.getId())
                .setSql("balance = COALESCE(balance, 0) + " + initialCoins);
        userMapper.update(null, updateWrapper);

        CoinLog coinLog = new CoinLog();
        coinLog.setUserId(userId);
        coinLog.setAmount(initialCoins);
        coinLog.setType(COIN_TYPE_SYSTEM);
        coinLog.setCreateTime(LocalDateTime.now());
        coinLogMapper.insert(coinLog);
    }

    private IdentityAuditVO convertToVO(IdentityAudit audit) {
        String submitTime = audit.getCreateTime() != null ? audit.getCreateTime().format(DATE_TIME_FORMATTER) : null;
        String status = AuditStatusEnum.fromCode(audit.getStatus()).getStatus();
        User user = userMapper.selectById(String.valueOf(audit.getUserId()));
        String role = user != null ? user.getRole() : null;

        return IdentityAuditVO.builder()
                .id(String.valueOf(audit.getId()))
                .userId(String.valueOf(audit.getUserId()))
                .userName(audit.getRealName())
                .submitTime(submitTime)
                .ocrAge(audit.getAge())
                .idCardFront(generateSignedUrl(audit.getIdCardFront()))
                .idCardBack(generateSignedUrl(audit.getIdCardBack()))
                .ocrName(audit.getRealName())
                .ocrIdNumber(audit.getIdCard())
                .status(status)
                .role(role)
                .build();
    }

    private String generateSignedUrl(String originalUrl) {
        if (!StringUtils.hasText(originalUrl) || !StringUtils.hasText(urlPrefix) || !originalUrl.startsWith(urlPrefix)) {
            return originalUrl;
        }

        OSS ossClient = ossClientProvider.getIfAvailable();
        if (ossClient == null || !StringUtils.hasText(bucketName)) {
            return originalUrl;
        }

        try {
            String ossKey = originalUrl.substring(urlPrefix.length());
            if (ossKey.startsWith("/")) {
                ossKey = ossKey.substring(1);
            }
            java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + 30 * 60 * 1000);
            return ossClient.generatePresignedUrl(bucketName, ossKey, expiration).toString();
        } catch (Exception e) {
            log.warn("生成 OSS 签名 URL 失败，回退原始 URL: {}", originalUrl, e);
            return originalUrl;
        }
    }
}
