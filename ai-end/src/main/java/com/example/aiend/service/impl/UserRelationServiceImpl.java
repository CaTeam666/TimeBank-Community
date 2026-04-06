package com.example.aiend.service.impl;

import com.aliyun.oss.OSS;
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
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserRelationServiceImpl implements UserRelationService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final UserRelationMapper userRelationMapper;
    private final UserMapper userMapper;
    private final MessageService messageService;
    private final ObjectProvider<OSS> ossClientProvider;

    @Value("${aliyun.oss.bucket-name:}")
    private String bucketName;

    @Value("${aliyun.oss.url-prefix:}")
    private String urlPrefix;

    @Override
    public PageResponseDTO<UserRelationVO> getRelationList(UserRelationQueryDTO queryDTO) {
        LambdaQueryWrapper<UserRelation> queryWrapper = new LambdaQueryWrapper<>();
        if (queryDTO.getStatus() != null) {
            queryWrapper.eq(UserRelation::getStatus, queryDTO.getStatus());
        }
        queryWrapper.orderByDesc(UserRelation::getCreateTime);

        Page<UserRelation> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<UserRelation> relationPage = userRelationMapper.selectPage(page, queryWrapper);

        Set<Long> userIds = relationPage.getRecords().stream()
                .flatMap(r -> java.util.stream.Stream.of(r.getChildId(), r.getParentId()))
                .collect(Collectors.toSet());

        Map<Long, User> userMap = Map.of();
        if (!userIds.isEmpty()) {
            List<User> users = userMapper.selectBatchIds(userIds.stream().map(String::valueOf).collect(Collectors.toList()));
            userMap = users.stream().collect(Collectors.toMap(u -> Long.parseLong(u.getId()), u -> u, (a, b) -> a));
        }

        final Map<Long, User> finalUserMap = userMap;
        List<UserRelation> filteredRecords = relationPage.getRecords();
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword().trim();
            filteredRecords = relationPage.getRecords().stream()
                    .filter(r -> {
                        User child = finalUserMap.get(r.getChildId());
                        User parent = finalUserMap.get(r.getParentId());
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

        List<UserRelationVO> voList = filteredRecords.stream()
                .map(r -> convertToVO(r, finalUserMap))
                .collect(Collectors.toList());

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
        UserRelation relation = userRelationMapper.selectById(id);
        if (relation == null) {
            throw new BusinessException(404, "申请记录不存在");
        }
        if (!RelationStatusEnum.PENDING_ADMIN_AUDIT.getCode().equals(relation.getStatus())) {
            throw new BusinessException(400, "该申请已处理");
        }

        Integer targetStatus;
        if (Integer.valueOf(1).equals(auditDTO.getStatus())) {
            targetStatus = RelationStatusEnum.PENDING_ELDER_CONFIRM.getCode();
        } else if (Integer.valueOf(2).equals(auditDTO.getStatus())) {
            targetStatus = RelationStatusEnum.REJECTED.getCode();
        } else {
            throw new BusinessException(400, "非法审核状态");
        }

        relation.setStatus(targetStatus);
        if (RelationStatusEnum.REJECTED.getCode().equals(targetStatus)) {
            relation.setRejectReason(auditDTO.getRejectReason());
        }

        int updateCount = userRelationMapper.updateById(relation);
        if (updateCount == 0) {
            throw new BusinessException(500, "审核操作失败");
        }

        if (RelationStatusEnum.PENDING_ELDER_CONFIRM.getCode().equals(targetStatus)) {
            User child = userMapper.selectById(String.valueOf(relation.getChildId()));
            String childName = child != null ? child.getRealName() : "用户";
            String relationDesc = relation.getRelation() != null ? relation.getRelation() : "亲属";
            messageService.createMessage(
                    relation.getParentId(),
                    MessageTypeEnum.FAMILY_BIND.getCode(),
                    relation.getId(),
                    "收到亲情账号绑定申请",
                    "用户" + childName + "申请绑定您为" + relationDesc + "，请确认"
            );
        }
    }

    private UserRelationVO convertToVO(UserRelation relation, Map<Long, User> userMap) {
        User child = userMap.get(relation.getChildId());
        User parent = userMap.get(relation.getParentId());
        String createTime = relation.getCreateTime() != null ? relation.getCreateTime().format(DATE_TIME_FORMATTER) : null;

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
            log.warn("生成 OSS 签名 URL 失败，回退原始 URL: {}", originalUrl, e);
            return originalUrl;
        }
    }
}
