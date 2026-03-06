package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiend.common.enums.TaskStatusEnum;
import com.example.aiend.common.enums.TaskTypeEnum;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.MissionForceCloseDTO;
import com.example.aiend.dto.request.MissionQueryDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.entity.CoinLog;
import com.example.aiend.entity.Task;
import com.example.aiend.entity.User;
import com.example.aiend.mapper.CoinLogMapper;
import com.example.aiend.mapper.TaskMapper;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.service.MissionService;
import com.example.aiend.vo.MissionDetailVO;
import com.example.aiend.vo.MissionLogVO;
import com.example.aiend.vo.MissionVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 任务监控服务实现类
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MissionServiceImpl implements MissionService {
    
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final CoinLogMapper coinLogMapper;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 获取任务列表
     *
     * @param queryDTO 查询条件
     * @return 分页任务列表
     */
    @Override
    public PageResponseDTO<MissionVO> getMissionList(MissionQueryDTO queryDTO) {
        log.info("获取任务列表，查询条件：{}", queryDTO);
        
        // 构建查询条件
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索（任务ID、标题）
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword().trim();
            // 尝试将关键词解析为ID
            try {
                Long taskId = Long.parseLong(keyword);
                queryWrapper.and(wrapper -> wrapper
                        .eq(Task::getId, taskId)
                        .or()
                        .like(Task::getTitle, keyword));
            } catch (NumberFormatException e) {
                // 非数字，按标题搜索
                queryWrapper.like(Task::getTitle, keyword);
            }
        }
        
        // 状态筛选
        if (StringUtils.hasText(queryDTO.getStatus()) && !"ALL".equalsIgnoreCase(queryDTO.getStatus())) {
            TaskStatusEnum statusEnum = TaskStatusEnum.fromName(queryDTO.getStatus());
            if (statusEnum != null) {
                queryWrapper.eq(Task::getStatus, statusEnum.getCode());
            }
        }
        
        // 类型筛选
        if (StringUtils.hasText(queryDTO.getType()) && !"ALL".equalsIgnoreCase(queryDTO.getType())) {
            TaskTypeEnum typeEnum = TaskTypeEnum.fromCode(queryDTO.getType());
            if (typeEnum != null) {
                queryWrapper.eq(Task::getType, typeEnum.getCode());
            }
        }
        
        // 日期筛选
        if (StringUtils.hasText(queryDTO.getDate())) {
            LocalDate date = LocalDate.parse(queryDTO.getDate(), DATE_FORMATTER);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            queryWrapper.between(Task::getCreateTime, startOfDay, endOfDay);
        }
        
        // 按创建时间倒序排序
        queryWrapper.orderByDesc(Task::getCreateTime);
        
        // 分页查询
        Page<Task> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        Page<Task> taskPage = taskMapper.selectPage(page, queryWrapper);
        
        // 转换为VO
        List<MissionVO> missionVOList = taskPage.getRecords().stream()
                .map(this::convertToMissionVO)
                .collect(Collectors.toList());
        
        return PageResponseDTO.<MissionVO>builder()
                .list(missionVOList)
                .total(taskPage.getTotal())
                .page(queryDTO.getPage())
                .pageSize(queryDTO.getPageSize())
                .build();
    }
    
    /**
     * 强制关闭任务
     *
     * @param forceCloseDTO 关闭参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void forceClose(MissionForceCloseDTO forceCloseDTO) {
        log.info("强制关闭任务，参数：{}", forceCloseDTO);
        
        Long taskId = Long.parseLong(forceCloseDTO.getTaskId());
        Task task = taskMapper.selectById(taskId);
        
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        // 检查任务状态是否可以关闭（只有待接取和进行中的任务可以强制关闭）
        Integer status = task.getStatus();
        if (!Objects.equals(status, TaskStatusEnum.PENDING.getCode()) 
                && !Objects.equals(status, TaskStatusEnum.IN_PROGRESS.getCode())) {
            throw new BusinessException("当前任务状态不允许强制关闭");
        }
        
        // 更新任务状态为已取消，并保存关闭原因
        LambdaUpdateWrapper<Task> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Task::getId, taskId)
                .set(Task::getStatus, TaskStatusEnum.CANCELLED.getCode())
                .set(Task::getCancelReason, forceCloseDTO.getReason())
                .set(Task::getUpdateTime, LocalDateTime.now());
        
        int result = taskMapper.update(null, updateWrapper);
        if (result <= 0) {
            throw new BusinessException("关闭任务失败");
        }
        
        // 退款逻辑：解冻并退还时间币给发布者
        if (task.getPrice() != null && task.getPrice() > 0 && task.getPublisherId() != null) {
            User publisher = userMapper.selectById(task.getPublisherId());
            if (publisher != null) {
                // 解冻金额：从 frozen_balance 减少
                Integer frozenBalance = publisher.getFrozenBalance() == null ? 0 : publisher.getFrozenBalance();
                Integer newFrozenBalance = Math.max(0, frozenBalance - task.getPrice());
                
                // 退还到可用余额：到 balance 增加
                Integer balance = publisher.getBalance() == null ? 0 : publisher.getBalance();
                Integer newBalance = balance + task.getPrice();
                
                publisher.setFrozenBalance(newFrozenBalance);
                publisher.setBalance(newBalance);
                publisher.setUpdateTime(LocalDateTime.now());
                userMapper.updateById(publisher);
                
                // 记录退款流水
                CoinLog coinLog = new CoinLog();
                coinLog.setUserId(task.getPublisherId());
                coinLog.setAmount(task.getPrice());  // 正数表示退款
                coinLog.setType(4);  // 类型4：系统调整（退款）
                coinLog.setTaskId(taskId);
                coinLog.setCreateTime(LocalDateTime.now());
                coinLogMapper.insert(coinLog);
                
                log.info("任务{}已关闭，从冻结余额退回{}时间币给用户{}", taskId, task.getPrice(), publisher.getId());
            }
        }
        
        log.info("任务{}已强制关闭，原因：{}", taskId, forceCloseDTO.getReason());
    }
    
    /**
     * 获取任务详情
     *
     * @param id 任务ID
     * @return 任务详情
     */
    @Override
    public MissionDetailVO getMissionDetail(String id) {
        log.info("获取任务详情，任务ID：{}", id);
        
        Long taskId = Long.parseLong(id);
        Task task = taskMapper.selectById(taskId);
        
        if (task == null) {
            throw new BusinessException("任务不存在");
        }
        
        return convertToMissionDetailVO(task);
    }
    
    /**
     * 将Task实体转换为MissionVO
     *
     * @param task 任务实体
     * @return MissionVO
     */
    private MissionVO convertToMissionVO(Task task) {
        MissionVO vo = new MissionVO();
        vo.setId(String.valueOf(task.getId()));
        vo.setTitle(task.getTitle());
        vo.setType(task.getType());
        vo.setCoins(task.getPrice());
        
        // 转换状态
        TaskStatusEnum statusEnum = TaskStatusEnum.fromCode(task.getStatus());
        vo.setStatus(statusEnum != null ? statusEnum.name() : null);
        
        // 格式化时间
        if (task.getCreateTime() != null) {
            vo.setPublishTime(task.getCreateTime().format(DATE_TIME_FORMATTER));
        }
        if (task.getDeadline() != null) {
            vo.setDeadline(task.getDeadline().format(DATE_TIME_FORMATTER));
        }
        
        // 查询发布者信息
        if (task.getPublisherId() != null) {
            User publisher = userMapper.selectById(task.getPublisherId());
            if (publisher != null) {
                vo.setCreatorName(publisher.getNickname());
                vo.setCreatorRealName(publisher.getRealName());
                vo.setCreatorPhone(publisher.getPhone());
            }
        }
        
        // 查询志愿者信息
        if (task.getVolunteerId() != null) {
            User volunteer = userMapper.selectById(task.getVolunteerId());
            if (volunteer != null) {
                vo.setVolunteerName(volunteer.getNickname());
                vo.setVolunteerPhone(volunteer.getPhone());
            }
        }
        
        return vo;
    }
    
    /**
     * 将Task实体转换为MissionDetailVO
     *
     * @param task 任务实体
     * @return MissionDetailVO
     */
    private MissionDetailVO convertToMissionDetailVO(Task task) {
        MissionDetailVO vo = new MissionDetailVO();
        vo.setId(String.valueOf(task.getId()));
        vo.setTitle(task.getTitle());
        vo.setDescription(task.getDescription());
        vo.setType(task.getType());
        vo.setCoins(task.getPrice());
        
        // 转换状态
        TaskStatusEnum statusEnum = TaskStatusEnum.fromCode(task.getStatus());
        vo.setStatus(statusEnum != null ? statusEnum.name() : null);
        
        // 格式化时间
        if (task.getCreateTime() != null) {
            vo.setPublishTime(task.getCreateTime().format(DATE_TIME_FORMATTER));
        }
        if (task.getDeadline() != null) {
            vo.setDeadline(task.getDeadline().format(DATE_TIME_FORMATTER));
        }
        
        // 查询发布者信息
        if (task.getPublisherId() != null) {
            User publisher = userMapper.selectById(task.getPublisherId());
            if (publisher != null) {
                vo.setCreatorId(String.valueOf(publisher.getId()));
                vo.setCreatorName(publisher.getNickname());
                vo.setCreatorRealName(publisher.getRealName());
                vo.setCreatorPhone(publisher.getPhone());
                vo.setCreatorAvatar(publisher.getAvatar());
                // 信用分暂时使用余额替代，实际项目中需要有专门的信用分字段
                vo.setCreatorCredit(publisher.getBalance());
            }
        }
        
        // 查询志愿者信息
        if (task.getVolunteerId() != null) {
            User volunteer = userMapper.selectById(task.getVolunteerId());
            if (volunteer != null) {
                vo.setVolunteerId(String.valueOf(volunteer.getId()));
                vo.setVolunteerName(volunteer.getNickname());
                vo.setVolunteerPhone(volunteer.getPhone());
                vo.setVolunteerAvatar(volunteer.getAvatar());
                vo.setVolunteerCredit(volunteer.getBalance());
            }
        }
        
        // 构建任务日志（模拟数据，实际项目中需要从日志表查询）
        vo.setLogs(buildTaskLogs(task));
        
        return vo;
    }
    
    /**
     * 构建任务日志
     *
     * @param task 任务实体
     * @return 日志列表
     */
    private List<MissionLogVO> buildTaskLogs(Task task) {
        List<MissionLogVO> logs = new ArrayList<>();
        
        // 发布任务日志
        if (task.getCreateTime() != null) {
            logs.add(MissionLogVO.builder()
                    .id("L" + task.getId() + "_1")
                    .time(task.getCreateTime().format(DATE_TIME_FORMATTER))
                    .content("发布了任务")
                    .build());
        }
        
        // 根据状态添加相应日志
        Integer status = task.getStatus();
        if (status != null && status >= TaskStatusEnum.IN_PROGRESS.getCode()) {
            logs.add(MissionLogVO.builder()
                    .id("L" + task.getId() + "_2")
                    .time(task.getUpdateTime() != null ? task.getUpdateTime().format(DATE_TIME_FORMATTER) : "")
                    .content("志愿者接取了任务")
                    .build());
        }
        
        if (status != null && status >= TaskStatusEnum.WAITING_CONFIRM.getCode()) {
            logs.add(MissionLogVO.builder()
                    .id("L" + task.getId() + "_3")
                    .time(task.getUpdateTime() != null ? task.getUpdateTime().format(DATE_TIME_FORMATTER) : "")
                    .content("志愿者提交了完成证明")
                    .build());
        }
        
        if (status != null && Objects.equals(status, TaskStatusEnum.COMPLETED.getCode())) {
            logs.add(MissionLogVO.builder()
                    .id("L" + task.getId() + "_4")
                    .time(task.getUpdateTime() != null ? task.getUpdateTime().format(DATE_TIME_FORMATTER) : "")
                    .content("任务已完成")
                    .build());
        }
        
        if (status != null && Objects.equals(status, TaskStatusEnum.CANCELLED.getCode())) {
            logs.add(MissionLogVO.builder()
                    .id("L" + task.getId() + "_5")
                    .time(task.getUpdateTime() != null ? task.getUpdateTime().format(DATE_TIME_FORMATTER) : "")
                    .content("任务已取消")
                    .build());
        }
        
        return logs;
    }
}
