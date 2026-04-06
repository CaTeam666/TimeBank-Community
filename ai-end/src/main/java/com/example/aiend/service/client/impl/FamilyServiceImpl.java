package com.example.aiend.service.client.impl;

import com.aliyun.oss.OSS;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.enums.MessageTypeEnum;
import com.example.aiend.common.enums.RelationStatusEnum;
import com.example.aiend.common.enums.UserRoleEnum;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.common.util.ProxyTokenUtil;
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
import com.example.aiend.service.SettingsService;
import com.example.aiend.service.client.FamilyService;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FamilyServiceImpl implements FamilyService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final String ACTION_APPROVE = "approve";
    private static final String ACTION_REJECT = "reject";

    private final UserRelationMapper userRelationMapper;
    private final UserMapper userMapper;
    private final MessageService messageService;
    private final SettingsService settingsService;
    private final ObjectProvider<OSS> ossClientProvider;

    @Value("${aliyun.oss.bucket-name:}")
    private String bucketName;

    @Value("${aliyun.oss.url-prefix:}")
    private String urlPrefix;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public FamilyBindResponseVO bind(Long childId, FamilyBindDTO bindDTO) {
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getPhone, bindDTO.getPhone());
        User parent = userMapper.selectOne(userQuery);
        if (parent == null) {
            throw new BusinessException(404, "手机号对应的用户不存在");
        }

        Long parentId = Long.parseLong(parent.getId());
        if (!UserRoleEnum.ELDER.getDesc().equals(parent.getRole())) {
            throw new BusinessException(400, "只能绑定老人角色的用户");
        }

        User currentUser = userMapper.selectById(String.valueOf(childId));
        if (currentUser != null && UserRoleEnum.ELDER.getDesc().equals(currentUser.getRole())) {
            throw new BusinessException(400, "老人身份不能添加亲情账号，请使用子女账号操作");
        }

        LambdaQueryWrapper<UserRelation> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(UserRelation::getChildId, childId).eq(UserRelation::getParentId, parentId);
        UserRelation existRelation = userRelationMapper.selectOne(existQuery);
        if (existRelation != null) {
            RelationStatusEnum existStatus = RelationStatusEnum.fromCode(existRelation.getStatus());
            if (existStatus != null && existStatus.isPending()) {
                throw new BusinessException(409, "已存在待审核的绑定申请");
            }
            if (existStatus != null && existStatus.isBound()) {
                throw new BusinessException(409, "已存在绑定关系");
            }
        }

        checkFamilyBindingLimit(parentId);

        String proofImg = bindDTO.getProofImg();
        if (StringUtils.hasText(proofImg) && proofImg.split(",").length < 2) {
            throw new BusinessException(400, "请至少上传 2 张证明材料图片");
        }

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

        return FamilyBindResponseVO.builder().relationId(relation.getId()).build();
    }

    @Override
    public List<FamilyMemberVO> getList(Long childId) {
        LambdaQueryWrapper<UserRelation> query = new LambdaQueryWrapper<>();
        query.eq(UserRelation::getChildId, childId)
                .eq(UserRelation::getStatus, RelationStatusEnum.BOUND.getCode())
                .eq(UserRelation::getIsDeleted, 0);
        List<UserRelation> relations = userRelationMapper.selectList(query);
        if (relations.isEmpty()) {
            return List.of();
        }

        Set<Long> parentIds = relations.stream().map(UserRelation::getParentId).collect(Collectors.toSet());
        Map<Long, User> userMap = getUsers(parentIds);
        return relations.stream().map(relation -> {
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
        }).collect(Collectors.toList());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unbind(Long childId, FamilyUnbindDTO unbindDTO) {
        UserRelation relation = userRelationMapper.selectById(unbindDTO.getRelationId());
        if (relation == null) {
            throw new BusinessException(404, "绑定关系不存在");
        }
        if (!childId.equals(relation.getChildId())) {
            throw new BusinessException(403, "无权限操作");
        }
        userRelationMapper.deleteById(unbindDTO.getRelationId());
    }

    @Override
    public ProxyToggleResponseVO toggleProxy(Long childId, ProxyToggleDTO toggleDTO) {
        if (!Boolean.TRUE.equals(toggleDTO.getEnable())) {
            return ProxyToggleResponseVO.builder().proxyToken(null).build();
        }

        String parentIdStr = toggleDTO.getParentId();
        if (!StringUtils.hasText(parentIdStr)) {
            throw new BusinessException(400, "开启代理模式时必须指定老人用户ID");
        }
        Long parentId = Long.parseLong(parentIdStr);

        LambdaQueryWrapper<UserRelation> query = new LambdaQueryWrapper<>();
        query.eq(UserRelation::getChildId, childId)
                .eq(UserRelation::getParentId, parentId)
                .eq(UserRelation::getStatus, RelationStatusEnum.BOUND.getCode());
        UserRelation relation = userRelationMapper.selectOne(query);
        if (relation == null) {
            throw new BusinessException(403, "无绑定关系，无权代理");
        }

        User parent = userMapper.selectById(String.valueOf(parentId));
        if (parent == null) {
            throw new BusinessException(404, "用户不存在");
        }

        return ProxyToggleResponseVO.builder()
                .proxyToken(generateProxyToken(childId, parentId))
                .build();
    }

    @Override
    public PendingRequestListVO getPendingRequests(Long parentId) {
        LambdaQueryWrapper<UserRelation> query = new LambdaQueryWrapper<>();
        query.eq(UserRelation::getParentId, parentId)
                .eq(UserRelation::getStatus, RelationStatusEnum.PENDING_ELDER_CONFIRM.getCode())
                .eq(UserRelation::getIsDeleted, 0)
                .orderByDesc(UserRelation::getCreateTime);
        List<UserRelation> relations = userRelationMapper.selectList(query);
        if (relations.isEmpty()) {
            return PendingRequestListVO.builder().total(0).requests(List.of()).build();
        }

        Set<Long> childIds = relations.stream().map(UserRelation::getChildId).collect(Collectors.toSet());
        Map<Long, User> userMap = getUsers(childIds);
        List<PendingRequestVO> requests = relations.stream().map(relation -> {
            User child = userMap.get(relation.getChildId());
            return PendingRequestVO.builder()
                    .relationId(relation.getId())
                    .childId(relation.getChildId())
                    .childName(child != null ? child.getRealName() : null)
                    .childAvatar(child != null ? child.getAvatar() : null)
                    .childPhone(child != null ? maskPhone(child.getPhone()) : null)
                    .relation(relation.getRelation())
                    .proofImg(generateSignedUrls(relation.getProofImg()))
                    .createTime(relation.getCreateTime() != null ? relation.getCreateTime().format(DATE_TIME_FORMATTER) : null)
                    .build();
        }).collect(Collectors.toList());

        return PendingRequestListVO.builder().total(requests.size()).requests(requests).build();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void review(Long parentId, FamilyReviewDTO reviewDTO) {
        UserRelation relation = userRelationMapper.selectById(reviewDTO.getRelationId());
        if (relation == null || relation.getIsDeleted() == 1) {
            throw new BusinessException(404, "申请记录不存在");
        }
        if (!parentId.equals(relation.getParentId())) {
            throw new BusinessException(403, "无权限操作");
        }
        if (!RelationStatusEnum.PENDING_ELDER_CONFIRM.getCode().equals(relation.getStatus())) {
            throw new BusinessException(400, "该申请当前不是待确认状态");
        }

        if (ACTION_APPROVE.equals(reviewDTO.getAction())) {
            SystemSettingsDTO settings = settingsService.getSettings();
            int maxLimit = settings.getFamilyBindingMaxLimit() != null ? settings.getFamilyBindingMaxLimit() : 3;
            LambdaQueryWrapper<UserRelation> boundQuery = new LambdaQueryWrapper<>();
            boundQuery.eq(UserRelation::getParentId, parentId)
                    .eq(UserRelation::getStatus, RelationStatusEnum.BOUND.getCode());
            long boundCount = userRelationMapper.selectCount(boundQuery);
            if (boundCount >= maxLimit) {
                throw new BusinessException(400, "该老人已达到亲属绑定上限");
            }
            relation.setStatus(RelationStatusEnum.BOUND.getCode());
        } else if (ACTION_REJECT.equals(reviewDTO.getAction())) {
            if (!StringUtils.hasText(reviewDTO.getRejectReason())) {
                throw new BusinessException(400, "拒绝时必须填写拒绝原因");
            }
            relation.setStatus(RelationStatusEnum.REJECTED.getCode());
            relation.setRejectReason(reviewDTO.getRejectReason());
        }

        relation.setUpdateTime(LocalDateTime.now());
        userRelationMapper.updateById(relation);
        messageService.deleteMessageByBizId(relation.getId(), MessageTypeEnum.FAMILY_BIND.getCode());
    }

    @Override
    public PendingConfirmCountVO getPendingConfirmCount(Long parentId) {
        LambdaQueryWrapper<UserRelation> query = new LambdaQueryWrapper<>();
        query.eq(UserRelation::getParentId, parentId)
                .eq(UserRelation::getStatus, RelationStatusEnum.PENDING_ELDER_CONFIRM.getCode())
                .eq(UserRelation::getIsDeleted, 0);
        Long count = userRelationMapper.selectCount(query);
        return PendingConfirmCountVO.builder().count(count.intValue()).build();
    }

    private Map<Long, User> getUsers(Set<Long> userIds) {
        if (userIds.isEmpty()) {
            return Map.of();
        }
        List<User> users = userMapper.selectBatchIds(userIds.stream().map(String::valueOf).collect(Collectors.toList()));
        return users.stream().collect(Collectors.toMap(u -> Long.parseLong(u.getId()), u -> u, (a, b) -> a));
    }

    private void checkFamilyBindingLimit(Long parentId) {
        SystemSettingsDTO settings = settingsService.getSettings();
        int maxLimit = settings.getFamilyBindingMaxLimit() != null ? settings.getFamilyBindingMaxLimit() : 3;
        LambdaQueryWrapper<UserRelation> countQuery = new LambdaQueryWrapper<>();
        countQuery.eq(UserRelation::getParentId, parentId)
                .eq(UserRelation::getIsDeleted, 0)
                .in(UserRelation::getStatus,
                        RelationStatusEnum.BOUND.getCode(),
                        RelationStatusEnum.PENDING_ADMIN_AUDIT.getCode(),
                        RelationStatusEnum.PENDING_ELDER_CONFIRM.getCode());
        long activeCount = userRelationMapper.selectCount(countQuery);
        if (activeCount >= maxLimit) {
            throw new BusinessException(400, "该老人已达到亲属绑定上限");
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }

    private String generateProxyToken(Long childId, Long parentId) {
        return ProxyTokenUtil.generateProxyToken(parentId, childId);
    }

    private String generateSignedUrls(String proofImg) {
        if (!StringUtils.hasText(proofImg)) {
            return proofImg;
        }
        return java.util.Arrays.stream(proofImg.split(",")).map(this::generateSignedUrl).collect(Collectors.joining(","));
    }

    private String generateSignedUrl(String originalUrl) {
        if (!StringUtils.hasText(originalUrl) || !StringUtils.hasText(urlPrefix) || !originalUrl.trim().startsWith(urlPrefix)) {
            return originalUrl;
        }

        OSS ossClient = ossClientProvider.getIfAvailable();
        if (ossClient == null || !StringUtils.hasText(bucketName)) {
            return originalUrl;
        }

        try {
            String ossKey = originalUrl.trim().substring(urlPrefix.length());
            if (ossKey.startsWith("/")) {
                ossKey = ossKey.substring(1);
            }
            java.util.Date expiration = new java.util.Date(System.currentTimeMillis() + 30 * 60 * 1000);
            return ossClient.generatePresignedUrl(bucketName, ossKey, expiration).toString();
        } catch (Exception e) {
            log.warn("生成 OSS 签名 URL 失败: {}", originalUrl, e);
            return originalUrl;
        }
    }
}
