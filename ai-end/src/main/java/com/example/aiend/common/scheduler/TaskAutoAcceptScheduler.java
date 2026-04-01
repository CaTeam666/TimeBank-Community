package com.example.aiend.common.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.enums.TaskStatusEnum;
import com.example.aiend.dto.request.SystemSettingsDTO;
import com.example.aiend.entity.CoinLog;
import com.example.aiend.entity.Task;
import com.example.aiend.entity.User;
import com.example.aiend.mapper.CoinLogMapper;
import com.example.aiend.mapper.TaskMapper;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 任务自动验收调度器
 * 志愿者完成任务提交凭证后，若老人在配置的期限内未确认验收，
 * 系统将自动验收并发放时间币给志愿者。
 *
 * @author AI-End
 * @since 2026-03-20
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class TaskAutoAcceptScheduler {

    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final CoinLogMapper coinLogMapper;
    private final SettingsService settingsService;

    /**
     * 每小时执行一次，检查超时未验收的任务
     */
    @Scheduled(fixedRate = 60 * 60 * 1000)
    @Transactional(rollbackFor = Exception.class)
    public void checkAndAutoAcceptTasks() {
        log.info("开始执行任务自动验收检测...");

        try {
            // 从系统配置动态读取自动验收期限（天）
            SystemSettingsDTO settings = settingsService.getSettings();
            int autoAcceptDays = settings.getTaskAutoAcceptDays() != null ? settings.getTaskAutoAcceptDays() : 3;

            LocalDateTime now = LocalDateTime.now();
            // 超时阈值 = 当前时间 - 配置的天数
            LocalDateTime timeoutThreshold = now.minusDays(autoAcceptDays);

            log.info("自动验收期限：{}天，截止时间：{}", autoAcceptDays, timeoutThreshold);

            // 查询"待验收"状态且更新时间早于阈值的任务
            LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Task::getStatus, TaskStatusEnum.WAITING_CONFIRM.getCode())
                    .isNotNull(Task::getVolunteerId)
                    .le(Task::getUpdateTime, timeoutThreshold)
                    .orderByAsc(Task::getUpdateTime);

            List<Task> timeoutTasks = taskMapper.selectList(queryWrapper);

            if (timeoutTasks.isEmpty()) {
                log.info("没有超时待验收的任务");
                return;
            }

            log.info("检测到 {} 个超时待验收的任务，开始自动验收", timeoutTasks.size());

            int successCount = 0;
            for (Task task : timeoutTasks) {
                try {
                    boolean success = autoAcceptTask(task, now);
                    if (success) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("自动验收任务失败，任务ID：{}", task.getId(), e);
                }
            }

            log.info("任务自动验收完成，处理任务数：{}，成功数：{}", timeoutTasks.size(), successCount);

        } catch (Exception e) {
            log.error("任务自动验收检测失败", e);
        }
    }

    /**
     * 自动验收单个任务
     * 逻辑与 ClientTaskServiceImpl.confirmReview() 一致：
     * 扣发布者冻结币 → 加志愿者余额 → 记流水 → 改状态为已完成
     *
     * @param task 待验收的任务
     * @param now  当前时间
     * @return 是否成功
     */
    private boolean autoAcceptTask(Task task, LocalDateTime now) {
        Long publisherId = task.getPublisherId();
        Long volunteerId = task.getVolunteerId();
        Integer coins = task.getPrice();

        // 1. 查询发布者
        User publisher = userMapper.selectById(String.valueOf(publisherId));
        if (publisher == null) {
            log.warn("自动验收失败，发布者不存在，用户ID：{}", publisherId);
            return false;
        }

        // 2. 查询志愿者
        User volunteer = userMapper.selectById(String.valueOf(volunteerId));
        if (volunteer == null) {
            log.warn("自动验收失败，志愿者不存在，用户ID：{}", volunteerId);
            return false;
        }

        // 3. 扣除发布者冻结时间币
        Integer frozenBalance = publisher.getFrozenBalance() != null ? publisher.getFrozenBalance() : 0;
        if (frozenBalance >= coins) {
            publisher.setFrozenBalance(frozenBalance - coins);
        } else {
            log.warn("发布者冻结余额不足，清零处理。用户ID：{}，冻结：{}，需扣：{}", publisherId, frozenBalance, coins);
            publisher.setFrozenBalance(0);
        }
        publisher.setUpdateTime(now);
        userMapper.updateById(publisher);

        // 4. 增加志愿者可用余额
        Integer volunteerBalance = volunteer.getBalance() != null ? volunteer.getBalance() : 0;
        volunteer.setBalance(volunteerBalance + coins);
        volunteer.setUpdateTime(now);
        userMapper.updateById(volunteer);

        // 5. 记录发布者支出流水
        CoinLog publisherLog = new CoinLog();
        publisherLog.setUserId(publisherId);
        publisherLog.setAmount(-coins);
        publisherLog.setType(2); // 2:任务支出
        publisherLog.setTaskId(task.getId());
        publisherLog.setCreateTime(now);
        publisherLog.setUpdateTime(now);
        publisherLog.setIsDeleted(0);
        coinLogMapper.insert(publisherLog);

        // 6. 记录志愿者收入流水
        CoinLog volunteerLog = new CoinLog();
        volunteerLog.setUserId(volunteerId);
        volunteerLog.setAmount(coins);
        volunteerLog.setType(1); // 1:任务收入
        volunteerLog.setTaskId(task.getId());
        volunteerLog.setCreateTime(now);
        volunteerLog.setUpdateTime(now);
        volunteerLog.setIsDeleted(0);
        coinLogMapper.insert(volunteerLog);

        // 7. 更新任务状态为已完成
        task.setStatus(TaskStatusEnum.COMPLETED.getCode());
        task.setUpdateTime(now);
        taskMapper.updateById(task);

        log.info("自动验收成功，任务ID：{}，时间币 {} 已从发布者({})转给志愿者({})",
                task.getId(), coins, publisherId, volunteerId);
        return true;
    }
}
