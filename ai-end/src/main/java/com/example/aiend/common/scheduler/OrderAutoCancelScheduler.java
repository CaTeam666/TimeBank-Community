package com.example.aiend.common.scheduler;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.enums.OrderStatusEnum;
import com.example.aiend.dto.request.SystemSettingsDTO;
import com.example.aiend.entity.ExchangeOrder;
import com.example.aiend.entity.User;
import com.example.aiend.mapper.ExchangeOrderMapper;
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
 * 订单自动取消调度器
 * 商品兑换订单生成后，若在配置的时长内未完成核销，
 * 系统将自动取消订单并将冻结的时间币退回用户可用余额。
 *
 * @author AI-End
 * @since 2026-03-20
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderAutoCancelScheduler {

    private final ExchangeOrderMapper orderMapper;
    private final UserMapper userMapper;
    private final SettingsService settingsService;

    /**
     * 每小时执行一次，检查超时未核销的订单
     */
    @Scheduled(fixedRate = 60 * 60 * 1000)
    @Transactional(rollbackFor = Exception.class)
    public void checkAndAutoCancelOrders() {
        log.info("开始执行订单自动取消检测...");

        try {
            // 从系统配置动态读取订单自动取消时长（小时）
            SystemSettingsDTO settings = settingsService.getSettings();
            int cancelTimeoutHours = settings.getOrderCancelTimeoutHours() != null ? settings.getOrderCancelTimeoutHours() : 24;

            LocalDateTime now = LocalDateTime.now();
            // 超时阈值 = 当前时间 - 配置的小时数
            LocalDateTime timeoutThreshold = now.minusHours(cancelTimeoutHours);

            log.info("订单自动取消时长：{}小时，截止时间：{}", cancelTimeoutHours, timeoutThreshold);

            // 查询"待核销"状态且创建时间早于阈值的订单
            LambdaQueryWrapper<ExchangeOrder> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(ExchangeOrder::getStatus, OrderStatusEnum.PENDING.getCode())
                    .le(ExchangeOrder::getCreateTime, timeoutThreshold)
                    .orderByAsc(ExchangeOrder::getCreateTime);

            List<ExchangeOrder> timeoutOrders = orderMapper.selectList(queryWrapper);

            if (timeoutOrders.isEmpty()) {
                log.info("没有超时未核销的订单");
                return;
            }

            log.info("检测到 {} 个超时未核销的订单，开始自动取消", timeoutOrders.size());

            int successCount = 0;
            for (ExchangeOrder order : timeoutOrders) {
                try {
                    boolean success = autoCancelOrder(order, now);
                    if (success) {
                        successCount++;
                    }
                } catch (Exception e) {
                    log.error("自动取消订单失败，订单ID：{}", order.getId(), e);
                }
            }

            log.info("订单自动取消完成，处理订单数：{}，成功数：{}", timeoutOrders.size(), successCount);

        } catch (Exception e) {
            log.error("订单自动取消检测失败", e);
        }
    }

    /**
     * 自动取消单个订单
     * 逻辑与 OrderServiceImpl.cancelOrder() 一致：
     * 解冻时间币 → 退还可用余额 → 改订单状态为已取消
     *
     * @param order 待取消的订单
     * @param now   当前时间
     * @return 是否成功
     */
    private boolean autoCancelOrder(ExchangeOrder order, LocalDateTime now) {
        // 1. 查询用户
        User user = userMapper.selectById(String.valueOf(order.getUserId()));
        if (user == null) {
            log.warn("自动取消订单失败，用户不存在，用户ID：{}", order.getUserId());
            return false;
        }

        Integer amount = order.getAmount();

        // 2. 将冻结时间币退还到可用余额
        Integer currentBalance = user.getBalance() != null ? user.getBalance() : 0;
        Integer frozenBalance = user.getFrozenBalance() != null ? user.getFrozenBalance() : 0;

        // 从冻结余额扣除
        if (frozenBalance >= amount) {
            user.setFrozenBalance(frozenBalance - amount);
        } else {
            log.warn("用户{}冻结余额{}不足以退还{}，将冻结余额清零", user.getId(), frozenBalance, amount);
            user.setFrozenBalance(0);
        }
        // 退还到可用余额
        user.setBalance(currentBalance + amount);
        user.setUpdateTime(now);
        userMapper.updateById(user);

        // 3. 更新订单状态为已取消
        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setUpdateTime(now);
        orderMapper.updateById(order);

        log.info("订单自动取消成功，订单ID：{}，订单号：{}，退还时间币：{}",
                order.getId(), order.getOrderNo(), amount);
        return true;
    }
}
