package com.example.aiend.service.client.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.common.util.UserContextUtil;
import com.example.aiend.entity.CoinLog;
import com.example.aiend.entity.User;
import com.example.aiend.mapper.CoinLogMapper;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.service.SettingsService;
import com.example.aiend.service.client.ClientUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

/**
 * 客户端用户服务实现类
 *
 * @author AI-End
 * @since 2026-03-22
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClientUserServiceImpl implements ClientUserService {

    private final UserMapper userMapper;
    private final CoinLogMapper coinLogMapper;
    private final SettingsService settingsService;
    private final RedisTemplate<String, Object> redisTemplate;

    private static final String DAILY_LOGIN_KEY_PREFIX = "daily_login:";

    @Override
    @Transactional(rollbackFor = Exception.class)
    public String dailyLoginReward() {
        Long userId = UserContextUtil.getCurrentUserId();
        
        // 1. 检查用户身份，只允许角色为“老人”的用户参与签到
        User user = userMapper.selectById(String.valueOf(userId));
        if (user == null || !"老人".equals(user.getRole())) {
            throw new BusinessException(403, "仅限老人身份专享每日签到奖励");
        }

        // 2. 检查 Redis 防重（每日一次）
        String today = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE);
        String redisKey = DAILY_LOGIN_KEY_PREFIX + today + ":" + userId;
        
        // 计算到今天午夜的失效时间，避免 Redis 数据冗余
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime endOfDay = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
        Duration duration = Duration.between(now, endOfDay);
        
        Boolean isSet = redisTemplate.opsForValue().setIfAbsent(redisKey, "1", duration);
        if (Boolean.FALSE.equals(isSet)) {
            throw new BusinessException(400, "今日已经完成签到，无需重复签到");
        }

        // 3. 读取系统后台奖励配置
        Integer rewardAmount = null;
        try {
            rewardAmount = settingsService.getSettings().getDailySignInReward();
        } catch (Exception e) {
            log.warn("无法获取系统每日签到奖励配置，跳过签到发奖", e);
        }

        // 4. 发放奖励与流水记录
        if (rewardAmount != null && rewardAmount > 0) {
            // 4.1 更新资产
            userMapper.update(null, new LambdaUpdateWrapper<User>()
                    .setSql("balance = balance + " + rewardAmount)
                    .eq(User::getId, String.valueOf(userId)));

            // 4.2 存入资金流水
            CoinLog coinLog = new CoinLog();
            coinLog.setUserId(userId);
            coinLog.setAmount(rewardAmount);
            // type 4 代表系统调整/额外发币等
            coinLog.setType(4);
            coinLogMapper.insert(coinLog);

            log.info("老人每日签到成功，userId: {}, 奖励数量: {}", userId, rewardAmount);
            return "签到成功，获得 " + rewardAmount + " 个时间币";
        }

        return "签到成功，今日暂无签到大厅活动";
    }
}
