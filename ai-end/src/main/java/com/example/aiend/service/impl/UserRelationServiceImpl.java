package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiend.common.enums.MessageTypeEnum;
import com.example.aiend.common.enums.RelationStatusEnum;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.UserRelationAuditDTO;
import com.example.aiend.dto.request.UserRelationQueryDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.entity.User;
import com.example.aiend.entity.UserRelation;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.mapper.UserRelationMapper;
import com.example.aiend.service.MessageService;
import com.example.aiend.service.UserRelationService;
import com.example.aiend.vo.UserRelationVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import com.aliyun.oss.OSS;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户关系（亲情绑定）服务实现类
 *
 * @author AI-End
 * @since 2025-12-25
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserRelationServiceImpl implements UserRelationService {
    
    private final UserRelationMapper userRelationMapper;
    private final UserMapper userMapper;
    private final MessageService messageService;
    private final OSS ossClient;
    
    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;
    
    @Value("${aliyun.oss.url-prefix}")
    private String urlPrefix;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public PageResponseDTO<UserRelationVO> getRelationList(UserRelationQueryDTO queryDTO) {
        log.info("查询亲情绑定列表，查询条件：{}", queryDTO);
        
        // 构建查询条件
        LambdaQueryWrapper<UserRelation> queryWrapper = new LambdaQueryWrapper<>();
        
        // 状态筛选
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(UserRelation::getStatus, queryDTO.getStatus());
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(UserRelation::getCreateTime);
        
        // 分页查询
        Page<UserRelation> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<UserRelation> relationPage = userRelationMapper.selectPage(page, queryWrapper);
        
        // 获取所有关联的用户ID
        Set<Long> userIds = relationPage.getRecords().stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getChildId(), r.getParentId()))
                .collect(Collectors.toSet());
        
        // 批量查询用户信息
        Map<Long, User> userMap = Map.of();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds.stream()
                    .map(String::valueOf)
                    .collect(Collectors.toList()));
            userMap = users.stream()
                    .collect(Collectors.toMap(u -> Long.parseLong(u.getId()), u -> u, (a, b) -> a));
        }
        
        // 关键词筛选（需要在获取用户信息后过滤）
        final Map<Long, User> finalUserMap = userMap;
        List<UserRelation> filteredRecords = relationPage.getRecords();
        
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword().trim();
            filteredRecords = relationPage.getRecords().stream()
                    .filter(r -> {
                        User child = finalUserMap.get(r.getChildId());
                        User parent = finalUserMap.get(r.getParentId());
                        // 匹配子女姓名、手机号或父母姓名、手机号
                        return (child != null && (
                                (child.getRealName() != null && child.getRealName().contains(keyword)) ||
                                (child.getPhone() != null && child.getPhone().contains(keyword))
                        )) || (parent != null && (
                                (parent.getRealName() != null && parent.getRealName().contains(keyword)) ||
                                (parent.getPhone() != null && parent.getPhone().contains(keyword))
                        ));
                    })
                    .collect(Collectors.toList());
        }
        
        // 转换为 VO
        List<UserRelationVO> voList = filteredRecords.stream()
                .map(r -> convertToVO(r, finalUserMap))
                .collect(Collectors.toList());
        
        // 构建分页响应
        return PageResponseDTO.<UserRelationVO>builder()
                .list(voList)
                .total(relationPage.getTotal())
                .page(queryDTO.getPage())
                .pageSize(queryDTO.getPageSize())
                .build();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void auditRelation(Long id, UserRelationAuditDTO auditDTO) {
        log.info("审核亲情绑定申请，ID：{}，审核状态：{}", id, auditDTO.getStatus());
        
        // 查询申请记录
        UserRelation relation = userRelationMapper.selectById(id);
        if (relation == null) {
            throw new BusinessException(404, "申请记录不存在");
        }
        
        // 检查当前状态是否为待管理员审核
        if (!RelationStatusEnum.PENDING_ADMIN_AUDIT.getCode().equals(relation.getStatus())) {
            throw new BusinessException(400, "该申请已处理，无法重复操作");
        }
        
        // 根据 DTO 动作映射到业务状态码
        // DTO: 1-通过, 2-拒绝
        // Enum: 1-PENDING_ELDER_CONFIRM, 3-REJECTED
        Integer targetStatus;
        if (Integer.valueOf(1).equals(auditDTO.getStatus())) {
            targetStatus = RelationStatusEnum.PENDING_ELDER_CONFIRM.getCode();
        } else if (Integer.valueOf(2).equals(auditDTO.getStatus())) {
            targetStatus = RelationStatusEnum.REJECTED.getCode();
        } else {
            throw new BusinessException(400, "非法审核状态");
        }

        // 更新状态
        relation.setStatus(targetStatus);
        
        // 如果是拒绝，设置拒绝原因
        if (RelationStatusEnum.REJECTED.getCode().equals(targetStatus)) {
            relation.setRejectReason(auditDTO.getRejectReason());
        }
        
        int updateCount = userRelationMapper.updateById(relation);
        
        if (updateCount == 0) {
            throw new BusinessException(500, "审核操作失败");
        }
        
        // 如果管理员审核通过（状态变为1），创建消息通知老人
        if (RelationStatusEnum.PENDING_ELDER_CONFIRM.getCode().equals(auditDTO.getStatus())) {
            // 查询子女信息用于构造消息内容
            User child = userMapper.selectById(String.valueOf(relation.getChildId()));
            String childName = child != null ? child.getRealName() : "用户";
            String relationDesc = relation.getRelation() != null ? relation.getRelation() : "亲属";
            
            // 创建消息
            String title = "收到亲情账号绑定申请";
            String content = "用户" + childName + "申请绑定您为" + relationDesc + "，请确认";
            
            messageService.createMessage(
                    relation.getParentId(),
                    MessageTypeEnum.FAMILY_BIND.getCode(),
                    relation.getId(),
                    title,
                    content
            );
            
            log.info("已创建亲情绑定消息，接收人：{}，业务ID：{}", relation.getParentId(), relation.getId());
        }
        
        log.info("亲情绑定审核完成，ID：{}，审核状态：{}", id, auditDTO.getStatus());
    }
    
    /**
     * 将 UserRelation 实体转换为 UserRelationVO
     *
     * @param relation 关系实体
     * @param userMap 用户信息Map
     * @return 关系VO
     */
    private UserRelationVO convertToVO(UserRelation relation, Map<Long, User> userMap) {
        User child = userMap.get(relation.getChildId());
        User parent = userMap.get(relation.getParentId());
        
        String createTime = relation.getCreateTime() != null
                ? relation.getCreateTime().format(DATE_TIME_FORMATTER)
                : null;
        
        return UserRelationVO.builder()
                .id(relation.getId())
                .childId(relation.getChildId())
                .childName(child != null ? child.getRealName() : null)
                .childPhone(child != null ? child.getPhone() : null)
                .parentId(relation.getParentId())
                .parentName(parent != null ? parent.getRealName() : null)
                .parentPhone(parent != null ? parent.getPhone() : null)
                .proofImg(generateSignedUrls(relation.getProofImg()))
                .status(relation.getStatus())
                .rejectReason(relation.getRejectReason())
                .createTime(createTime)
                .build();
    }
    
    /**
     * 为多张证件照片（逗号分隔）生成签名 URL
     *
     * @param proofImg 原始 URL 字符串
     * @return 带签名的 URL 字符串
     */
    private String generateSignedUrls(String proofImg) {
        if (!StringUtils.hasText(proofImg)) {
            return proofImg;
        }
        
        String[] urls = proofImg.split(",");
        return java.util.Arrays.stream(urls)
                .map(this::generateSignedUrl)
                .collect(Collectors.joining(","));
    }
    
    /**
     * 为单张照片生成带签名的临时访问 URL
     *
     * @param originalUrl 原始 URL
     * @return 带签名的 URL（非 OSS 资源则返回原 URL）
     */
    private String generateSignedUrl(String originalUrl) {
        if (!StringUtils.hasText(originalUrl) || !originalUrl.trim().startsWith(urlPrefix)) {
            return originalUrl;
        }
        
        try {
            String trimmedUrl = originalUrl.trim();
            // 提取 OSS 中的文件路径 (Key)
            String ossKey = trimmedUrl.substring(urlPrefix.length());
            if (ossKey.startsWith("/")) {
                ossKey = ossKey.substring(1);
            }
            
            // 设置过期时间为 30 分钟后 (审核员查看需要较长时间)
            java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + 30 * 60 * 1000);
            
            // 生成签名 URL
            java.net.URL signedUrl = ossClient.generatePresignedUrl(bucketName, ossKey, expiration);
            return signedUrl.toString();
        } catch (Exception e) {
            log.warn("生成 OSS 签名 URL 失败，返回原始 URL: {}", originalUrl, e);
            return originalUrl;
        }
    }
}
