package com.example.aiend.service.client.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.enums.RelationStatusEnum;
import com.example.aiend.common.exception.BusinessException;
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
import com.example.aiend.service.client.FamilyService;
import com.example.aiend.common.util.ProxyTokenUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

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
        
        // 通过手机号查找被绑定的长者
        LambdaQueryWrapper<User> userQuery = new LambdaQueryWrapper<>();
        userQuery.eq(User::getPhone, bindDTO.getPhone())
                .eq(User::getDeleted, 0);
        User parent = userMapper.selectOne(userQuery);
        
        if (parent == null) {
            throw new BusinessException(404, "手机号对应的用户不存在");
        }
        
        Long parentId = Long.parseLong(parent.getId());
        
        // 检查是否已存在绑定关系（未删除的）
        LambdaQueryWrapper<UserRelation> existQuery = new LambdaQueryWrapper<>();
        existQuery.eq(UserRelation::getChildId, childId)
                .eq(UserRelation::getParentId, parentId)
                .eq(UserRelation::getIsDeleted, 0);
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
        
        if (relation == null || relation.getIsDeleted() == 1) {
            throw new BusinessException(404, "绑定关系不存在");
        }
        
        // 验证权限（只能解绑自己的亲情账号）
        if (!childId.equals(relation.getChildId())) {
            throw new BusinessException(403, "无权限操作");
        }
        
        // 逻辑删除
        relation.setIsDeleted(1);
        relation.setUpdateTime(LocalDateTime.now());
        userRelationMapper.updateById(relation);
        
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
        
        Long parentId = toggleDTO.getParentId();
        if (parentId == null) {
            throw new BusinessException(400, "开启代理模式时必须指定长者用户ID");
        }
        
        // 验证绑定关系
        LambdaQueryWrapper<UserRelation> query = new LambdaQueryWrapper<>();
        query.eq(UserRelation::getChildId, childId)
                .eq(UserRelation::getParentId, parentId)
                .eq(UserRelation::getStatus, RelationStatusEnum.BOUND.getCode())
                .eq(UserRelation::getIsDeleted, 0);
        UserRelation relation = userRelationMapper.selectOne(query);
        
        if (relation == null) {
            throw new BusinessException(403, "无绑定关系，无权代理");
        }
        
        // 验证长者用户存在
        User parent = userMapper.selectById(String.valueOf(parentId));
        if (parent == null || parent.getDeleted() == 1) {
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
                            .proofImg(relation.getProofImg())
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
            // 老人同意，状态改为已绑定
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
}
