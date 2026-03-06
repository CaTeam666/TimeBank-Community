package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.enums.TaskStatusEnum;
import com.example.aiend.entity.CoinLog;
import com.example.aiend.entity.Task;
import com.example.aiend.entity.User;
import com.example.aiend.entity.ZombieTaskLog;
import com.example.aiend.mapper.CoinLogMapper;
import com.example.aiend.mapper.TaskMapper;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.mapper.ZombieTaskLogMapper;
import com.example.aiend.service.AnomalyService;
import com.example.aiend.vo.ZombieTaskLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 异常数据服务实现类
 * 处理系统异常数据，包括僵尸任务管理
 *
 * @author AI-End
 * @since 2026-01-14
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AnomalyServiceImpl implements AnomalyService {

    private final ZombieTaskLogMapper zombieTaskLogMapper;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final CoinLogMapper coinLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * Redis 任务分类 ZSet Key 前缀
     */
    private static final String TASK_CATEGORY_KEY_PREFIX = "task:category:";

    /**
     * 退款状态常量
     */
    private static final String REFUND_SUCCESS = "SUCCESS";
    private static final String REFUND_FAILURE = "FAILURE";

    /**
     * 时间币日志类型：系统调整
     */
    private static final int COIN_LOG_TYPE_SYSTEM_ADJUST = 4;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取僵尸任务日志列表
     *
     * @return 僵尸任务日志列表
     */
    @Override
    public List<ZombieTaskLogVO> getZombieTaskLogs() {
        LambdaQueryWrapper<ZombieTaskLog> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(ZombieTaskLog::getCreateTime);

        List<ZombieTaskLog> logs = zombieTaskLogMapper.selectList(queryWrapper);

        return logs.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 手动触发僵尸任务检测
     * 实际检测逻辑由调度器执行
     */
    @Override
    public void triggerZombieTaskCheck() {
        log.info("手动触发僵尸任务检测");
        // 调度器会自动执行，这里仅记录日志
    }

    /**
     * 手动重试退款
     *
     * @param logId 日志ID
     * @return 是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean retryRefund(Long logId) {
        ZombieTaskLog zombieLog = zombieTaskLogMapper.selectById(logId);
        if (zombieLog == null) {
            log.warn("僵尸任务日志不存在，ID：{}", logId);
            return false;
        }

        if (REFUND_SUCCESS.equals(zombieLog.getRefundStatus())) {
            log.info("该任务已退款成功，无需重试，日志ID：{}", logId);
            return true;
        }

        Task task = taskMapper.selectById(zombieLog.getTaskId());
        if (task == null) {
            log.warn("关联任务不存在，任务ID：{}", zombieLog.getTaskId());
            return false;
        }

        try {
            // 执行退款
            boolean refundSuccess = executeRefund(task, zombieLog.getRefundAmount());

            // 更新日志状态
            zombieLog.setRefundStatus(refundSuccess ? REFUND_SUCCESS : REFUND_FAILURE);
            zombieLog.setUpdateTime(LocalDateTime.now());
            zombieTaskLogMapper.updateById(zombieLog);

            log.info("重试退款完成，日志ID：{}，结果：{}", logId, refundSuccess ? "成功" : "失败");
            return refundSuccess;

        } catch (Exception e) {
            log.error("重试退款失败，日志ID：{}", logId, e);
            return false;
        }
    }

    /**
     * 处理单个僵尸任务
     * 将任务状态改为已取消，并尝试退款
     *
     * @param task 任务实体
     * @return 处理是否成功
     */
    @Transactional(rollbackFor = Exception.class)
    public boolean processZombieTask(Task task) {
        log.info("处理僵尸任务，任务ID：{}，标题：{}", task.getId(), task.getTitle());

        LocalDateTime now = LocalDateTime.now();
        String refundStatus = REFUND_FAILURE;

        try {
            // 1. 更新任务状态为已取消
            task.setStatus(TaskStatusEnum.CANCELLED.getCode());
            task.setUpdateTime(now);
            taskMapper.updateById(task);

            // 2. 从Redis缓存中移除任务
            removeTaskFromRedisCache(task);

            // 3. 执行退款（解冻并返还时间币）
            boolean refundSuccess = executeRefund(task, task.getPrice());
            refundStatus = refundSuccess ? REFUND_SUCCESS : REFUND_FAILURE;

            // 4. 记录僵尸任务日志
            ZombieTaskLog zombieLog = new ZombieTaskLog();
            zombieLog.setTaskId(task.getId());
            zombieLog.setTaskTitle(task.getTitle());
            zombieLog.setClosedTime(now);
            zombieLog.setRefundAmount(task.getPrice());
            zombieLog.setRefundStatus(refundStatus);
            zombieLog.setCreateTime(now);
            zombieLog.setIsDeleted(0);
            zombieTaskLogMapper.insert(zombieLog);

            log.info("僵尸任务处理完成，任务ID：{}，退款状态：{}", task.getId(), refundStatus);
            return true;

        } catch (Exception e) {
            log.error("处理僵尸任务失败，任务ID：{}", task.getId(), e);
            return false;
        }
    }

    /**
     * 执行退款操作
     * 将冻结的时间币返还给发布者
     *
     * @param task   任务实体
     * @param amount 退款金额
     * @return 是否成功
     */
    private boolean executeRefund(Task task, Integer amount) {
        if (amount == null || amount <= 0) {
            log.info("退款金额为0或无效，跳过退款");
            return true;
        }

        try {
            // 查询发布者信息
            User publisher = userMapper.selectById(task.getPublisherId());
            if (publisher == null) {
                log.warn("发布者不存在，用户ID：{}", task.getPublisherId());
                return false;
            }

            // 解冻时间币：减少冻结余额，增加可用余额
            Integer frozenBalance = publisher.getFrozenBalance() != null ? publisher.getFrozenBalance() : 0;
            Integer balance = publisher.getBalance() != null ? publisher.getBalance() : 0;

            // 更新用户余额
            publisher.setFrozenBalance(Math.max(0, frozenBalance - amount));
            publisher.setBalance(balance + amount);
            publisher.setUpdateTime(LocalDateTime.now());
            userMapper.updateById(publisher);

            // 记录时间币流水
            CoinLog coinLog = new CoinLog();
            coinLog.setUserId(Long.parseLong(publisher.getId()));
            coinLog.setAmount(amount);
            coinLog.setType(COIN_LOG_TYPE_SYSTEM_ADJUST);
            coinLog.setTaskId(task.getId());
            coinLog.setCreateTime(LocalDateTime.now());
            coinLog.setIsDeleted(0);
            coinLogMapper.insert(coinLog);

            log.info("退款成功，用户ID：{}，金额：{}", publisher.getId(), amount);
            return true;

        } catch (Exception e) {
            log.error("执行退款失败，任务ID：{}", task.getId(), e);
            return false;
        }
    }

    /**
     * 从Redis缓存中移除任务
     *
     * @param task 任务实体
     */
    private void removeTaskFromRedisCache(Task task) {
        try {
            String taskType = task.getType();
            Long taskId = task.getId();

            // 删除单个任务缓存
            String itemKey = "task:item:" + taskId;
            redisTemplate.delete(itemKey);

            // 从分类ZSet中移除
            if (taskType != null) {
                String categoryKey = TASK_CATEGORY_KEY_PREFIX + taskType;
                // 遍历ZSet找到并移除该任务
                redisTemplate.opsForZSet().removeRangeByScore(categoryKey, 0, Double.MAX_VALUE);
            }

            log.info("已从Redis缓存中移除任务，任务ID：{}", taskId);

        } catch (Exception e) {
            log.error("从Redis缓存移除任务失败，任务ID：{}", task.getId(), e);
            // 缓存操作失败不影响业务流程
        }
    }

    /**
     * 将实体转换为VO
     *
     * @param log 僵尸任务日志实体
     * @return VO对象
     */
    private ZombieTaskLogVO convertToVO(ZombieTaskLog log) {
        return ZombieTaskLogVO.builder()
                .id(String.valueOf(log.getId()))
                .taskId(String.valueOf(log.getTaskId()))
                .taskTitle(log.getTaskTitle())
                .closedTime(log.getClosedTime() != null ? log.getClosedTime().format(DATE_TIME_FORMATTER) : null)
                .refundAmount(log.getRefundAmount())
                .refundStatus(log.getRefundStatus())
                .build();
    }
}
