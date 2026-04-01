package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiend.common.enums.AppealStatusEnum;
import com.example.aiend.common.enums.TaskStatusEnum;
import com.example.aiend.common.enums.VerdictTypeEnum;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.ArbitrationQueryDTO;
import com.example.aiend.dto.request.ArbitrationVerdictDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.entity.Appeal;
import com.example.aiend.entity.CoinLog;
import com.example.aiend.entity.Task;
import com.example.aiend.entity.User;
import com.example.aiend.mapper.AppealMapper;
import com.example.aiend.mapper.CoinLogMapper;
import com.example.aiend.mapper.TaskMapper;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.service.ArbitrationService;
import com.example.aiend.vo.ArbitrationDetailVO;
import com.example.aiend.vo.ArbitrationListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 仲裁服务实现类
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ArbitrationServiceImpl implements ArbitrationService {
    
    private final AppealMapper appealMapper;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final CoinLogMapper coinLogMapper;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    /**
     * 获取仲裁列表
     *
     * @param queryDTO 查询条件
     * @return 分页仲裁列表
     */
    @Override
    public PageResponseDTO<ArbitrationListVO> getArbitrationList(ArbitrationQueryDTO queryDTO) {
        log.info("获取仲裁列表，查询条件：{}", queryDTO);
        
        // 构建查询条件
        LambdaQueryWrapper<Appeal> queryWrapper = new LambdaQueryWrapper<>();
        
        // 状态筛选
        if (StringUtils.hasText(queryDTO.getStatus()) && !"ALL".equalsIgnoreCase(queryDTO.getStatus())) {
            if ("PENDING".equalsIgnoreCase(queryDTO.getStatus())) {
                queryWrapper.eq(Appeal::getStatus, AppealStatusEnum.PENDING.getCode());
            } else if ("RESOLVED".equalsIgnoreCase(queryDTO.getStatus())) {
                // 已处理包括已通过和已驳回
                queryWrapper.in(Appeal::getStatus, AppealStatusEnum.APPROVED.getCode(), AppealStatusEnum.REJECTED.getCode());
            }
        }
        
        // 关键词搜索（ID或申诉类型）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword().trim();
            try {
                Long appealId = Long.parseLong(keyword);
                queryWrapper.and(w -> w.eq(Appeal::getId, appealId).or().like(Appeal::getType, keyword));
            } catch (NumberFormatException e) {
                queryWrapper.like(Appeal::getType, keyword);
            }
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(Appeal::getCreateTime);
        
        // 分页查询
        Page<Appeal> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        Page<Appeal> appealPage = appealMapper.selectPage(page, queryWrapper);
        
        // 转换为VO
        List<ArbitrationListVO> listVOs = appealPage.getRecords().stream()
                .map(this::convertToArbitrationListVO)
                .collect(Collectors.toList());
        
        return PageResponseDTO.<ArbitrationListVO>builder()
                .list(listVOs)
                .total(appealPage.getTotal())
                .page(queryDTO.getPage())
                .pageSize(queryDTO.getPageSize())
                .build();
    }
    
    /**
     * 获取仲裁详情
     *
     * @param id 仲裁单ID
     * @return 仲裁详情
     */
    @Override
    public ArbitrationDetailVO getArbitrationDetail(String id) {
        log.info("获取仲裁详情，仲裁单ID：{}", id);
        
        Long appealId = Long.parseLong(id);
        Appeal appeal = appealMapper.selectById(appealId);
        
        if (appeal == null) {
            throw new BusinessException("仲裁单不存在");
        }
        
        return convertToArbitrationDetailVO(appeal);
    }
    
    /**
     * 提交裁决
     *
     * @param verdictDTO 裁决请求参数
     * @param handlerId 处理人ID（可为null）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitVerdict(ArbitrationVerdictDTO verdictDTO, Long handlerId) {
        log.info("提交裁决，参数：{}，处理人ID：{}", verdictDTO, handlerId);
        
        Long appealId = Long.parseLong(verdictDTO.getId());
        Appeal appeal = appealMapper.selectById(appealId);
        
        if (appeal == null) {
            throw new BusinessException("仲裁单不存在");
        }
        
        // 检查状态是否为待处理
        if (!Objects.equals(appeal.getStatus(), AppealStatusEnum.PENDING.getCode())) {
            throw new BusinessException("该仲裁单已处理，不能重复裁决");
        }
        
        // 校验裁决类型
        VerdictTypeEnum verdictType = VerdictTypeEnum.fromCode(verdictDTO.getVerdictType());
        if (verdictType == null) {
            throw new BusinessException("无效的裁决类型");
        }
        
        // 获取关联任务
        Task task = taskMapper.selectById(appeal.getTaskId());
        if (task == null) {
            throw new BusinessException("关联任务不存在");
        }
        
        // 根据裁决类型处理
        switch (verdictType) {
            case REJECT:
                // 驳回申诉，维持原判
                appeal.setStatus(AppealStatusEnum.REJECTED.getCode());
                log.info("仲裁单{}驳回，维持原判", appealId);
                break;
                
            case TO_VOLUNTEER:
                // 判给志愿者，强制结算
                appeal.setStatus(AppealStatusEnum.APPROVED.getCode());
                handleVerdictToVolunteer(task);
                log.info("仲裁单{}判给志愿者，任务{}强制结算", appealId, task.getId());
                break;
                
            case TO_PUBLISHER:
                // 判给发布者，取消订单退款
                appeal.setStatus(AppealStatusEnum.APPROVED.getCode());
                handleVerdictToPublisher(task);
                log.info("仲裁单{}判给发布者，任务{}取消并退款", appealId, task.getId());
                break;
                
            default:
                throw new BusinessException("未知的裁决类型");
        }
        
        // 更新申诉记录：写入裁决结果和理由
        appeal.setHandlingResult(verdictDTO.getVerdictType());
        appeal.setHandlingReason(verdictDTO.getReason());
        appeal.setHandlerId(handlerId);  // 设置处理人ID（可为null）
        appeal.setHandleTime(LocalDateTime.now());
        appeal.setUpdateTime(LocalDateTime.now());
        appealMapper.updateById(appeal);
        
        log.info("裁决完成，仲裁单ID：{}，裁决类型：{}，理由：{}", appealId, verdictType.getDesc(), verdictDTO.getReason());
    }
    
    /**
     * 处理判给志愿者的裁决（强制结算）
     * 1. 扣除发布者冻结时间币
     * 2. 增加志愿者时间币
     * 3. 记录双方流水
     * 4. 更新任务状态为已完成
     *
     * @param task 任务实体
     */
    private void handleVerdictToVolunteer(Task task) {
        Integer coins = task.getPrice();
        
        // 扣除发布者冻结时间币
        if (task.getPublisherId() != null && coins != null && coins > 0) {
            User publisher = userMapper.selectById(String.valueOf(task.getPublisherId()));
            if (publisher != null) {
                Integer frozenBalance = publisher.getFrozenBalance() != null ? publisher.getFrozenBalance() : 0;
                if (frozenBalance >= coins) {
                    publisher.setFrozenBalance(frozenBalance - coins);
                    publisher.setUpdateTime(LocalDateTime.now());
                    userMapper.updateById(publisher);
                    log.info("仲裁裁决：扣除发布者{}冻结余额{}时间币", publisher.getId(), coins);
                    
                    // 记录发布者支出流水
                    CoinLog publisherLog = new CoinLog();
                    publisherLog.setUserId(task.getPublisherId());
                    publisherLog.setAmount(-coins);
                    publisherLog.setType(2);  // 2:任务支出
                    publisherLog.setTaskId(task.getId());
                    publisherLog.setCreateTime(LocalDateTime.now());
                    publisherLog.setUpdateTime(LocalDateTime.now());
                    coinLogMapper.insert(publisherLog);
                    log.info("仲裁裁决：记录发布者{}支出流水，金额：-{}", task.getPublisherId(), coins);
                } else {
                    log.warn("仲裁裁决：发布者{}冻结余额{}不足以支付任务报酬{}", publisher.getId(), frozenBalance, coins);
                }
            }
        }
        
        // 将任务报酬转给志愿者
        if (task.getVolunteerId() != null && coins != null && coins > 0) {
            User volunteer = userMapper.selectById(String.valueOf(task.getVolunteerId()));
            if (volunteer != null) {
                Integer newBalance = (volunteer.getBalance() == null ? 0 : volunteer.getBalance()) + coins;
                volunteer.setBalance(newBalance);
                volunteer.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(volunteer);
                log.info("仲裁裁决：任务{}报酬{}时间币已转给志愿者{}", task.getId(), coins, volunteer.getId());
                
                // 记录志愿者收入流水
                CoinLog volunteerLog = new CoinLog();
                volunteerLog.setUserId(task.getVolunteerId());
                volunteerLog.setAmount(coins);
                volunteerLog.setType(1);  // 1:任务收入
                volunteerLog.setTaskId(task.getId());
                volunteerLog.setCreateTime(LocalDateTime.now());
                volunteerLog.setUpdateTime(LocalDateTime.now());
                coinLogMapper.insert(volunteerLog);
                log.info("仲裁裁决：记录志愿者{}收入流水，金额：+{}", task.getVolunteerId(), coins);
            }
        }
        
        // 更新任务状态为已完成
        task.setStatus(TaskStatusEnum.COMPLETED.getCode());
        task.setUpdateTime(LocalDateTime.now());
        int updatedRows = taskMapper.updateById(task);
        log.info("仲裁裁决：任务{}状态已更新为已完成（status=3），影响行数：{}", task.getId(), updatedRows);
    }
    
    /**
     * 处理判给发布者的裁决（取消订单退款）
     * 1. 扣除发布者冻结时间币
     * 2. 退回到发布者余额
     * 3. 记录系统调整流水（退款）
     * 4. 更新任务状态为已取消
     *
     * @param task 任务实体
     */
    private void handleVerdictToPublisher(Task task) {
        Integer coins = task.getPrice();
        
        // 将冻结的报酬退回给发布者
        if (task.getPublisherId() != null && coins != null && coins > 0) {
            User publisher = userMapper.selectById(String.valueOf(task.getPublisherId()));
            if (publisher != null) {
                // 扣除冻结余额
                Integer frozenBalance = publisher.getFrozenBalance() != null ? publisher.getFrozenBalance() : 0;
                if (frozenBalance >= coins) {
                    publisher.setFrozenBalance(frozenBalance - coins);
                } else {
                    log.warn("仲裁裁决：发布者{}冻结余额{}不足，将冻结余额清零", publisher.getId(), frozenBalance);
                    publisher.setFrozenBalance(0);
                }
                
                // 退回到可用余额
                Integer newBalance = (publisher.getBalance() == null ? 0 : publisher.getBalance()) + coins;
                publisher.setBalance(newBalance);
                publisher.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(publisher);
                log.info("仲裁裁决：任务{}报酬{}时间币已退回给发布者{}，当前余额：{}", 
                        task.getId(), coins, publisher.getId(), newBalance);
                
                // 记录系统调整流水（退款）
                CoinLog refundLog = new CoinLog();
                refundLog.setUserId(task.getPublisherId());
                refundLog.setAmount(coins);  // 正数表示收入（退款）
                refundLog.setType(4);  // 4:系统调整（仲裁退款）
                refundLog.setTaskId(task.getId());
                refundLog.setCreateTime(LocalDateTime.now());
                refundLog.setUpdateTime(LocalDateTime.now());
                coinLogMapper.insert(refundLog);
                log.info("仲裁裁决：记录发布者{}退款流水，金额：+{}", task.getPublisherId(), coins);
            }
        }
        
        // 更新任务状态为已取消
        task.setStatus(TaskStatusEnum.CANCELLED.getCode());
        task.setUpdateTime(LocalDateTime.now());
        int updatedRows = taskMapper.updateById(task);
        log.info("仲裁裁决：任务{}状态已更新为已取消（status=4），影响行数：{}", task.getId(), updatedRows);
    }
    
    /**
     * 将Appeal实体转换为ArbitrationDetailVO
     *
     * @param appeal 申诉实体
     * @return ArbitrationDetailVO
     */
    private ArbitrationDetailVO convertToArbitrationDetailVO(Appeal appeal) {
        ArbitrationDetailVO vo = new ArbitrationDetailVO();
        vo.setId(String.valueOf(appeal.getId()));
        vo.setTaskId(String.valueOf(appeal.getTaskId()));
        vo.setInitiatorId(String.valueOf(appeal.getProposerId()));
        vo.setType(appeal.getType());
        vo.setDescription(appeal.getReason());
        vo.setDefendantResponse(appeal.getDefendantResponse());
        vo.setDefendantEvidenceImg(appeal.getDefendantEvidenceImg());
        vo.setDefendantEvidenceImages(parseEvidenceImages(appeal.getDefendantEvidenceImg()));
        
        // 转换状态
        AppealStatusEnum statusEnum = AppealStatusEnum.fromCode(appeal.getStatus());
        vo.setStatus(statusEnum != null ? statusEnum.name() : null);
        
        // 格式化时间
        if (appeal.getCreateTime() != null) {
            vo.setCreateTime(appeal.getCreateTime().format(DATE_TIME_FORMATTER));
        }
        
        // 解析证据图片
        vo.setEvidenceImages(parseEvidenceImages(appeal.getEvidenceImg()));
        
        // 获取任务信息
        Task task = taskMapper.selectById(appeal.getTaskId());
        if (task != null) {
            vo.setTaskTitle(task.getTitle());
            vo.setTaskDescription(task.getDescription());
            // 任务表暂无address字段，后续可扩展
            vo.setTaskAddress(null);
            if (task.getDeadline() != null) {
                vo.setTaskDeadline(task.getDeadline().format(DATE_TIME_FORMATTER));
            }
            
            // 判断发起人角色
            String initiatorRole = determineInitiatorRole(appeal.getProposerId(), task);
            vo.setInitiatorRole(initiatorRole);
        }
        
        // 获取发起人信息
        User initiator = userMapper.selectById(appeal.getProposerId());
        if (initiator != null) {
            vo.setInitiatorName(initiator.getNickname() != null ? initiator.getNickname() : initiator.getRealName());
        }
        
        return vo;
    }
    
    /**
     * 判断申诉发起人在任务中的角色
     *
     * @param proposerId 发起人ID
     * @param task 任务实体
     * @return 角色标识（VOLUNTEER/PUBLISHER）
     */
    private String determineInitiatorRole(Long proposerId, Task task) {
        if (Objects.equals(proposerId, task.getVolunteerId())) {
            return "VOLUNTEER";
        } else if (Objects.equals(proposerId, task.getPublisherId())) {
            return "PUBLISHER";
        }
        return null;
    }
    
    /**
     * 将Appeal实体转换为ArbitrationListVO
     *
     * @param appeal 申诉实体
     * @return ArbitrationListVO
     */
    private ArbitrationListVO convertToArbitrationListVO(Appeal appeal) {
        ArbitrationListVO vo = new ArbitrationListVO();
        vo.setId(String.valueOf(appeal.getId()));
        vo.setTaskId(String.valueOf(appeal.getTaskId()));
        vo.setInitiatorId(String.valueOf(appeal.getProposerId()));
        vo.setType(appeal.getType());
        
        // 转换状态
        AppealStatusEnum statusEnum = AppealStatusEnum.fromCode(appeal.getStatus());
        vo.setStatus(statusEnum != null ? statusEnum.name() : null);
        
        // 格式化时间
        if (appeal.getCreateTime() != null) {
            vo.setCreateTime(appeal.getCreateTime().format(DATE_TIME_FORMATTER));
        }
        
        // 获取任务信息
        Task task = taskMapper.selectById(appeal.getTaskId());
        if (task != null) {
            vo.setTaskTitle(task.getTitle());
            // 判断发起人角色
            vo.setInitiatorRole(determineInitiatorRole(appeal.getProposerId(), task));
        }
        
        // 获取发起人信息
        User initiator = userMapper.selectById(appeal.getProposerId());
        if (initiator != null) {
            vo.setInitiatorName(initiator.getNickname() != null ? initiator.getNickname() : initiator.getRealName());
        }
        
        return vo;
    }
    
    /**
     * 解析证据图片字符串为列表
     *
     * @param evidenceImg 证据图片字符串（JSON数组或逗号分隔）
     * @return 图片URL列表
     */
    private List<String> parseEvidenceImages(String evidenceImg) {
        if (!StringUtils.hasText(evidenceImg)) {
            return new ArrayList<>();
        }
        
        String trimmed = evidenceImg.trim();
        
        // 尝试解析JSON数组格式
        if (trimmed.startsWith("[") && trimmed.endsWith("]")) {
            // 简单处理JSON数组，去除方括号和引号
            String content = trimmed.substring(1, trimmed.length() - 1);
            if (!StringUtils.hasText(content)) {
                return new ArrayList<>();
            }
            return Arrays.stream(content.split(","))
                    .map(s -> s.trim().replaceAll("^\"|\"$", ""))
                    .filter(StringUtils::hasText)
                    .toList();
        }
        
        // 按逗号分隔
        return Arrays.stream(trimmed.split(","))
                .map(String::trim)
                .filter(StringUtils::hasText)
                .toList();
    }
}
