package com.example.aiend.service.client.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.enums.RelationStatusEnum;
import com.example.aiend.common.enums.UserRoleEnum;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.SystemSettingsDTO;
import com.example.aiend.dto.request.client.FamilyBindDTO;
import com.example.aiend.dto.request.client.FamilyReviewDTO;
import com.example.aiend.dto.request.client.FamilyUnbindDTO;
import com.example.aiend.dto.request.client.ProxyToggleDTO;
import com.example.aiend.dto.response.client.FamilyBindResponseVO;
import com.example.aiend.dto.response.client.FamilyMemberVO;
import com.example.aiend.dto.response.client.PendingConfirmCountVO;
import com.example.aiend.dto.response.client.PendingRequestListVO;
import com.example.aiend.dto.response.client.PendingRequestVO;
import com.example.aiend.dto.response.client.ProxyToggleResponseVO;
import com.example.aiend.entity.User;
import com.example.aiend.entity.UserRelation;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.mapper.UserRelationMapper;
import com.example.aiend.service.MessageService;
import com.example.aiend.common.enums.MessageTypeEnum;
import com.example.aiend.service.SettingsService;
import com.example.aiend.service.client.FamilyService;
import com.example.aiend.common.util.ProxyTokenUtil;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 亲情代理服务实现类
 * 实现亲情账号绑定、解绑、代理切换等业务逻辑
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {
    
    private final UserRelationMapper userRelationMapper;
    private final UserMapper userMapper;
    private final MessageService messageService;
    private final SettingsService settingsService;
    private final OSS ossClient;

    @Value("${aliyun.oss.bucket-name}")
    private String bucketName;

    @Value("${aliyun.oss.url-prefix}")
    private String urlPrefix;
    
    /**
     * 日期时间格式化器
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 审核动作：通过
     */
    private static final String ACTION_APPROVE = "approve";
    
    /**
     * 审核动作：拒绝
     */
    private static final String ACTION_REJECT = "reject";
    

    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyBindResponseVO bind(Long childId, FamilyBindDTO bindDTO) {
        log.info("申请绑定亲情账号，childId：{}，phone：{}", childId, bindDTO.getPhone());
        
        // 通过手机号查找被绑定的长者（@TableLogic 自动过滤已删除用户）
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getPhone, bindDTO.getPhone());
        User parent = userMapper.selectOne(userQuery);
        
        if (parent == null) {
            throw new BusinessException(404, "手机号对应的用户不存在");
        }
        
        Long parentId = Long.parseLong(parent.getId());
        
        // 校验目标用户必须是长者角色（数据库存储中文描述）
        if (!UserRoleEnum.ELDER.getDesc().equals(parent.getRole())) {
            throw new BusinessException(400, "只能绑定长者角色的用户");
        }
        
        // 校验当前用户不能是长者角色（长者不能发起绑定）
        User currentUser = userMapper.selectById(String.valueOf(childId));
        if (currentUser != null && UserRoleEnum.ELDER.getDesc().equals(currentUser.getRole())) {
            throw new BusinessException(400, "长者身份不能添加亲情账号，请使用子女账号操作");
        }
        
        // 检查是否已存在绑定关系（@TableLogic 自动过滤已删除记录）
        LambdaQueryWrapper<UserRelation> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(UserRelation::getChildId, childId)
                .eq(UserRelation::getParentId, parentId);
        UserRelation existRelation = userRelationMapper.selectOne(existQuery);
        
        if (existRelation != null) {
            RelationStatusEnum existStatus = RelationStatusEnum.fromCode(existRelation.getStatus());
            // 如果是待审核状态（待管理员审核或待老人确认）
            if (existStatus != null && existStatus.isPending()) {
                throw new BusinessException(409, "已存在待审核的绑定申请");
            }
            // 如果已是绑定状态
            if (existStatus != null && existStatus.isBound()) {
                throw new BusinessException(409, "已存在绑定关系");
            }
        }
        
        // 校验老人已有的有效绑定数是否已达上限
        checkFamilyBindingLimit(parentId);
        
        // 校验证明材料：要求至少2张照片（根据接口文档 v1.2）
        String proofImg = bindDTO.getProofImg();
        if (StringUtils.hasText(proofImg)) {
            String[] urls = proofImg.split(",");
            if (urls.length < 2) {
                log.warn("证明材料不足，childId：{}，urls.length：{}", childId, urls.length);
                throw new BusinessException(400, "请至少上传2张证明材料照片（如户口本首页及相关页）");
            }
        }
        
        // 创建绑定申请
        UserRelation relation = new UserRelation();
        relation.setChildId(childId);
        relation.setParentId(parentId);
        relation.setRelation(bindDTO.getRelation());
        relation.setProofImg(bindDTO.getProofImg());
        relation.setStatus(RelationStatusEnum.PENDING_ADMIN_AUDIT.getCode());
        relation.setCreateTime(LocalDateTime.now());
        relation.setUpdateTime(LocalDateTime.now());
        relation.setIsDeleted(0);
        
        userRelationMapper.insert(relation);
        
        log.info("绑定申请创建成功，relationId：{}", relation.getId());
        
        return FamilyBindResponseVO.builder()
                .relationId(relation.getId())
                .build();
    }
    
    @Override
    public List<FamilyMemberVO> getList(Long childId) {
        log.info("获取亲情账号列表，childId：{}", childId);
        
        // 查询已绑定的关系（status=2）
        LambdaQueryWrapper<UserRelation> query = new LambdaQueryWrapper<>();
        query.eq(UserRelation::getChildId, childId)
                .eq(UserRelation::getStatus, RelationStatusEnum.BOUND.getCode())
                .eq(UserRelation::getIsDeleted, 0);
        List<UserRelation> relations = userRelationMapper.selectList(query);
        
        if (relations.isEmpty()) {
            return List.of();
        }
        
        // 批量查询长者用户信息
        Set<Long> parentIds = relations.stream()
                .map(UserRelation::getParentId)
                .collect(Collectors.toSet());
        
        Map<Long, User> userMap = getUsers(parentIds);
        
        // 转换为 VO
        return relations.stream()
                .map(relation -> {
                    User parent = userMap.get(relation.getParentId());
                    return FamilyMemberVO.builder()
                            .id(parent != null ? parent.getId() : null)
                            .relationId(relation.getId())
                            .nickname(parent != null ? parent.getNickname() : null)
                            .avatar(parent != null ? parent.getAvatar() : null)
                            .balance(parent != null ? parent.getBalance() : null)
                            .relation(relation.getRelation())
                            .phone(parent != null ? maskPhone(parent.getPhone()) : null)
                            .status(relation.getStatus())
                            .build();
                })
                .collect(Collectors.toList());
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbind(Long childId, FamilyUnbindDTO unbindDTO) {
        log.info("解绑亲情账号，childId：{}，relationId：{}", childId, unbindDTO.getRelationId());
        
        // 查询绑定关系
        UserRelation relation = userRelationMapper.selectById(unbindDTO.getRelationId());
        
        if (relation == null) {
            throw new BusinessException(404, "绑定关系不存在");
        }
        
        // 验证权限（只能解绑自己的亲情账号）
        if (!childId.equals(relation.getChildId())) {
            throw new BusinessException(403, "无权限操作");
        }
        
        // 使用 deleteById 触发 @TableLogic 逻辑删除（updateById 无法更新 @TableLogic 字段）
        userRelationMapper.deleteById(unbindDTO.getRelationId());
        
        log.info("解绑成功，relationId：{}", unbindDTO.getRelationId());
    }
    
    @Override
    public ProxyToggleResponseVO toggleProxy(Long childId, ProxyToggleDTO toggleDTO) {
        log.info("切换代理模式，childId：{}，enable：{}", childId, toggleDTO.getEnable());
        
        // 关闭代理模式
        if (!Boolean.TRUE.equals(toggleDTO.getEnable())) {
            return ProxyToggleResponseVO.builder()
                    .proxyToken(null)
                    .build();
        }
        
        String parentIdStr = toggleDTO.getParentId();
        if (parentIdStr == null || parentIdStr.isEmpty()) {
            throw new BusinessException(400, "开启代理模式时必须指定长者用户ID");
        }
        Long parentId = Long.parseLong(parentIdStr);
        
        // 验证绑定关系（@TableLogic 自动过滤已删除记录）
        LambdaQueryWrapper<UserRelation> query = new LambdaQueryWrapper<>();
        query.eq(UserRelation::getChildId, childId)
                .eq(UserRelation::getParentId, parentId)
                .eq(UserRelation::getStatus, RelationStatusEnum.BOUND.getCode());
        UserRelation relation = userRelationMapper.selectOne(query);
        
        if (relation == null) {
            throw new BusinessException(403, "无绑定关系，无权代理");
        }
        
        // 验证长者用户存在
        User parent = userMapper.selectById(String.valueOf(parentId));
        if (parent == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 生成代理令牌
        String proxyToken = generateProxyToken(childId, parentId);
        
        log.info("已切换为代理模式，childId：{}，parentId：{}", childId, parentId);
        
        return ProxyToggleResponseVO.builder()
                .proxyToken(proxyToken)
                .build();
    }
    
    @Override
    public PendingRequestListVO getPendingRequests(Long parentId) {
        log.info("获取待审核的绑定申请，parentId：{}", parentId);
        
        // 查询待老人确认的绑定申请（status=1）
        LambdaQueryWrapper<UserRelation> query = new LambdaQueryWrapper<>();
        query.eq(UserRelation::getParentId, parentId)
                .eq(UserRelation::getStatus, RelationStatusEnum.PENDING_ELDER_CONFIRM.getCode())
                .eq(UserRelation::getIsDeleted, 0)
                .orderByDesc(UserRelation::getCreateTime);
        List<UserRelation> relations = userRelationMapper.selectList(query);
        
        if (relations.isEmpty()) {
            return PendingRequestListVO.builder()
                    .total(0)
                    .requests(List.of())
                    .build();
        }
        
        // 批量查询申请人用户信息
        Set<Long> childIds = relations.stream()
                .map(UserRelation::getChildId)
                .collect(Collectors.toSet());
        
        Map<Long, User> userMap = getUsers(childIds);
        
        // 转换为 VO
        List<PendingRequestVO> requests = relations.stream()
                .map(relation -> {
                    User child = userMap.get(relation.getChildId());
                    return PendingRequestVO.builder()
                            .relationId(relation.getId())
                            .childId(relation.getChildId())
                            .childName(child != null ? child.getRealName() : null)
                            .childAvatar(child != null ? child.getAvatar() : null)
                            .childPhone(child != null ? maskPhone(child.getPhone()) : null)
                            .relation(relation.getRelation())
                            .proofImg(generateSignedUrls(relation.getProofImg()))
                            .createTime(relation.getCreateTime() != null 
                                    ? relation.getCreateTime().format(DATE_TIME_FORMATTER) : null)
                            .build();
                })
                .collect(Collectors.toList());
        
        return PendingRequestListVO.builder()
                .total(requests.size())
                .requests(requests)
                .build();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long parentId, FamilyReviewDTO reviewDTO) {
        log.info("审核绑定申请，parentId：{}，relationId：{}，action：{}", 
                parentId, reviewDTO.getRelationId(), reviewDTO.getAction());
        
        // 查询申请记录
        UserRelation relation = userRelationMapper.selectById(reviewDTO.getRelationId());
        
        if (relation == null || relation.getIsDeleted() == 1) {
            throw new BusinessException(404, "申请记录不存在");
        }
        
        // 验证权限（只能审核向自己发起的申请）
        if (!parentId.equals(relation.getParentId())) {
            throw new BusinessException(403, "无权限操作");
        }
        
        // 检查当前状态是否为待老人确认
        if (!RelationStatusEnum.PENDING_ELDER_CONFIRM.getCode().equals(relation.getStatus())) {
            throw new BusinessException(400, "该申请当前不是待确认状态，无法操作");
        }
        
        // 执行审核操作
        if (ACTION_APPROVE.equals(reviewDTO.getAction())) {
            // 老人同意，校验当前已绑定数量是否已达上限（只统计BOUND状态，不含待审核）
            SystemSettingsDTO settings = settingsService.getSettings();
            int maxLimit = settings.getFamilyBindingMaxLimit() != null ? settings.getFamilyBindingMaxLimit() : 3;
            LambdaQueryWrapper<UserRelation> boundQuery = new LambdaQueryWrapper<>();
            boundQuery.eq(UserRelation::getParentId, parentId)
                    .eq(UserRelation::getStatus, RelationStatusEnum.BOUND.getCode());
            long boundCount = userRelationMapper.selectCount(boundQuery);
            if (boundCount >= maxLimit) {
                throw new BusinessException(400, "该老人已有" + boundCount + "个亲属绑定，已达上限（" + maxLimit + "人）");
            }
            // 通过校验，状态改为已绑定
            relation.setStatus(RelationStatusEnum.BOUND.getCode());
        } else if (ACTION_REJECT.equals(reviewDTO.getAction())) {
            // 拒绝时必须填写拒绝原因
            if (!StringUtils.hasText(reviewDTO.getRejectReason())) {
                throw new BusinessException(400, "拒绝时必须填写拒绝原因");
            }
            relation.setStatus(RelationStatusEnum.REJECTED.getCode());
            relation.setRejectReason(reviewDTO.getRejectReason());
        }
        
        relation.setUpdateTime(LocalDateTime.now());
        userRelationMapper.updateById(relation);
        
        // 删除对应的亲情绑定消息
        messageService.deleteMessageByBizId(relation.getId(), MessageTypeEnum.FAMILY_BIND.getCode());
        log.info("已删除亲情绑定消息，业务ID：{}", relation.getId());
        
        log.info("审核完成，relationId：{}，status：{}", relation.getId(), relation.getStatus());
    }
    
    @Override
    public PendingConfirmCountVO getPendingConfirmCount(Long parentId) {
        log.info("获取待确认的绑定申请数量，parentId：{}", parentId);
        
        // 查询待老人确认的绑定申请数量（status=1）
        LambdaQueryWrapper<UserRelation> query = new LambdaQueryWrapper<>();
        query.eq(UserRelation::getParentId, parentId)
                .eq(UserRelation::getStatus, RelationStatusEnum.PENDING_ELDER_CONFIRM.getCode())
                .eq(UserRelation::getIsDeleted, 0);
        
        Long count = userRelationMapper.selectCount(query);
        
        log.info("待确认申请数量：{}", count);
        
        return PendingConfirmCountVO.builder()
                .count(count.intValue())
                .build();
    }
    
    /**
     * 批量获取用户信息
     *
     * @param userIds 用户ID集合
     * @return 用户ID -> 用户对象映射
     */
    private Map<Long, User> getUsers(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        
        List<User> users = userMapper.selectBatchIds(
                userIds.stream().map(String::valueOf).collect(Collectors.toList()));
        
        return users.stream()
                .collect(Collectors.toMap(
                        u -> Long.parseLong(u.getId()),
                        u -> u,
                        (a, b) -> a
                ));
    }
    
    /**
     * 校验老人已有的有效绑定数是否已达系统配置上限
     * 有效绑定包括：已绑定(2)、待管理员审核(0)、待老人确认(1)
     *
     * @param parentId 老人用户ID
     * @throws BusinessException 超出上限时抛出异常
     */
    private void checkFamilyBindingLimit(Long parentId) {
        SystemSettingsDTO settings = settingsService.getSettings();
        int maxLimit = settings.getFamilyBindingMaxLimit() != null ? settings.getFamilyBindingMaxLimit() : 3;
        
        // 统计有效绑定数（已绑定 + 待审核状态）
        LambdaQueryWrapper<UserRelation> countQuery = new LambdaQueryWrapper<>();
        countQuery.eq(UserRelation::getParentId, parentId)
                .eq(UserRelation::getIsDeleted, 0)
                .in(UserRelation::getStatus,
                        RelationStatusEnum.BOUND.getCode(),
                        RelationStatusEnum.PENDING_ADMIN_AUDIT.getCode(),
                        RelationStatusEnum.PENDING_ELDER_CONFIRM.getCode());
        long activeCount = userRelationMapper.selectCount(countQuery);
        
        if (activeCount >= maxLimit) {
            throw new BusinessException(400, "该老人已有" + activeCount + "个亲属绑定，已达上限（" + maxLimit + "人）");
        }
    }
    
    /**
     * 手机号脱敏处理
     * 如：13812345678 -> 138****5678
     *
     * @param phone 原始手机号
     * @return 脱敏后的手机号
     */
    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
    
    /**
     * 生成代理令牌
     * 使用 ProxyTokenUtil 生成包含完整代理信息的令牌
     * 
     * ProxyToken 包含：
     * - userId: 被代理人ID（老人ID）- 执行操作时使用此ID
     * - realUserId: 实际操作人ID（子女ID）- 用于审计日志
     * - isProxy: true - 代理标识
     * - expireTime: 过期时间（2小时）
     *
     * @param childId 子女用户ID（实际操作人）
     * @param parentId 长者用户ID（被代理人）
     * @return 代理令牌
     */
    private String generateProxyToken(Long childId, Long parentId) {
        // userId 是被代理人（老人），realUserId 是实际操作人（子女）
        return ProxyTokenUtil.generateProxyToken(parentId, childId);
    }

    /**
     * 为多张图片（逗号分隔）生成带签名的临时访问 URL
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
     * 为单张图片生成带签名的临时访问 URL
     */
    private String generateSignedUrl(String originalUrl) {
        if (!StringUtils.hasText(originalUrl) || !originalUrl.trim().startsWith(urlPrefix)) {
            return originalUrl;
        }

        try {
            String trimmedUrl = originalUrl.trim();
            String ossKey = trimmedUrl.substring(urlPrefix.length());
            if (ossKey.startsWith("/")) {
                ossKey = ossKey.substring(1);
            }

            java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + 30 * 60 * 1000);
            java.net.URL signedUrl = ossClient.generatePresignedUrl(bucketName, ossKey, expiration);
            return signedUrl.toString();
        } catch (Exception e) {
            log.warn("生成 OSS 签名 URL 失败: {}", originalUrl, e);
            return originalUrl;
        }
    }
}
