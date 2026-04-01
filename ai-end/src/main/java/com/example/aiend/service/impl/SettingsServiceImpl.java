package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.aiend.dto.request.SystemSettingsDTO;
import com.example.aiend.entity.SystemConfig;
import com.example.aiend.mapper.SystemConfigMapper;
import com.example.aiend.service.SettingsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 系统设置服务实现类
 *
 * @author AI-End
 * @since 2025-12-27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SettingsServiceImpl implements SettingsService {
    
    private final SystemConfigMapper systemConfigMapper;
    
    /**
     * 配置键常量定义
     */
    private static final String KEY_ELDER_INITIAL_COINS = "elder_initial_coins";
    private static final String KEY_DAILY_SIGN_IN_REWARD = "daily_sign_in_reward";
    private static final String KEY_MONTHLY_RANK_1_REWARD = "monthly_rank_1_reward";
    private static final String KEY_MONTHLY_RANK_2_REWARD = "monthly_rank_2_reward";
    private static final String KEY_MONTHLY_RANK_3_REWARD = "monthly_rank_3_reward";
    private static final String KEY_MONTHLY_RANK_4_REWARD = "monthly_rank_4_reward";
    private static final String KEY_MONTHLY_RANK_5_REWARD = "monthly_rank_5_reward";
    private static final String KEY_TRANSACTION_FEE_PERCENT = "transaction_fee_percent";
    private static final String KEY_ZOMBIE_TASK_TIMEOUT_HOURS = "zombie_task_timeout_hours";
    private static final String KEY_TASK_AUTO_ACCEPT_DAYS = "task_auto_accept_days";
    private static final String KEY_FAMILY_BINDING_MAX_LIMIT = "family_binding_max_limit";
    private static final String KEY_PROXY_DAILY_ACTION_LIMIT = "proxy_daily_action_limit";
    private static final String KEY_ORDER_CANCEL_TIMEOUT_HOURS = "order_cancel_timeout_hours";
    
    /**
     * 获取所有系统配置
     *
     * @return 系统配置对象
     */
    @Override
    public SystemSettingsDTO getSettings() {
        log.info("获取系统配置");
        
        // 查询所有配置
        List<SystemConfig> configs = systemConfigMapper.selectList(new LambdaQueryWrapper<>());
        
        // 转换为Map便于查找
        Map<String, String> configMap = new HashMap<>();
        for (SystemConfig config : configs) {
            configMap.put(config.getConfigKey(), config.getConfigValue());
        }
        
        // 构建返回对象
        SystemSettingsDTO dto = new SystemSettingsDTO();
        dto.setElderInitialCoins(parseIntOrDefault(configMap.get(KEY_ELDER_INITIAL_COINS), 100));
        dto.setDailySignInReward(parseIntOrDefault(configMap.get(KEY_DAILY_SIGN_IN_REWARD), 5));
        dto.setMonthlyRank1Reward(parseIntOrDefault(configMap.get(KEY_MONTHLY_RANK_1_REWARD), 500));
        dto.setMonthlyRank2Reward(parseIntOrDefault(configMap.get(KEY_MONTHLY_RANK_2_REWARD), 300));
        dto.setMonthlyRank3Reward(parseIntOrDefault(configMap.get(KEY_MONTHLY_RANK_3_REWARD), 100));
        dto.setMonthlyRank4Reward(parseIntOrDefault(configMap.get(KEY_MONTHLY_RANK_4_REWARD), 50));
        dto.setMonthlyRank5Reward(parseIntOrDefault(configMap.get(KEY_MONTHLY_RANK_5_REWARD), 30));
        dto.setTransactionFeePercent(parseIntOrDefault(configMap.get(KEY_TRANSACTION_FEE_PERCENT), 0));
        dto.setZombieTaskTimeoutHours(parseIntOrDefault(configMap.get(KEY_ZOMBIE_TASK_TIMEOUT_HOURS), 24));
        dto.setTaskAutoAcceptDays(parseIntOrDefault(configMap.get(KEY_TASK_AUTO_ACCEPT_DAYS), 3));
        dto.setFamilyBindingMaxLimit(parseIntOrDefault(configMap.get(KEY_FAMILY_BINDING_MAX_LIMIT), 3));
        dto.setProxyDailyActionLimit(parseIntOrDefault(configMap.get(KEY_PROXY_DAILY_ACTION_LIMIT), 5));
        dto.setOrderCancelTimeoutHours(parseIntOrDefault(configMap.get(KEY_ORDER_CANCEL_TIMEOUT_HOURS), 24));
        
        log.info("获取系统配置成功：{}", dto);
        return dto;
    }
    
    /**
     * 更新系统配置
     *
     * @param settingsDTO 配置参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateSettings(SystemSettingsDTO settingsDTO) {
        Objects.requireNonNull(settingsDTO, "配置参数不能为空");
        
        log.info("更新系统配置，参数：{}", settingsDTO);
        
        // 更新各配置项
        if (settingsDTO.getElderInitialCoins() != null) {
            updateConfigValue(KEY_ELDER_INITIAL_COINS, String.valueOf(settingsDTO.getElderInitialCoins()));
        }
        if (settingsDTO.getDailySignInReward() != null) {
            updateConfigValue(KEY_DAILY_SIGN_IN_REWARD, String.valueOf(settingsDTO.getDailySignInReward()));
        }
        if (settingsDTO.getMonthlyRank1Reward() != null) {
            updateConfigValue(KEY_MONTHLY_RANK_1_REWARD, String.valueOf(settingsDTO.getMonthlyRank1Reward()));
        }
        if (settingsDTO.getMonthlyRank2Reward() != null) {
            updateConfigValue(KEY_MONTHLY_RANK_2_REWARD, String.valueOf(settingsDTO.getMonthlyRank2Reward()));
        }
        if (settingsDTO.getMonthlyRank3Reward() != null) {
            updateConfigValue(KEY_MONTHLY_RANK_3_REWARD, String.valueOf(settingsDTO.getMonthlyRank3Reward()));
        }
        if (settingsDTO.getMonthlyRank4Reward() != null) {
            updateConfigValue(KEY_MONTHLY_RANK_4_REWARD, String.valueOf(settingsDTO.getMonthlyRank4Reward()));
        }
        if (settingsDTO.getMonthlyRank5Reward() != null) {
            updateConfigValue(KEY_MONTHLY_RANK_5_REWARD, String.valueOf(settingsDTO.getMonthlyRank5Reward()));
        }
        if (settingsDTO.getTransactionFeePercent() != null) {
            updateConfigValue(KEY_TRANSACTION_FEE_PERCENT, String.valueOf(settingsDTO.getTransactionFeePercent()));
        }
        if (settingsDTO.getZombieTaskTimeoutHours() != null) {
            updateConfigValue(KEY_ZOMBIE_TASK_TIMEOUT_HOURS, String.valueOf(settingsDTO.getZombieTaskTimeoutHours()));
        }
        if (settingsDTO.getTaskAutoAcceptDays() != null) {
            updateConfigValue(KEY_TASK_AUTO_ACCEPT_DAYS, String.valueOf(settingsDTO.getTaskAutoAcceptDays()));
        }
        if (settingsDTO.getFamilyBindingMaxLimit() != null) {
            updateConfigValue(KEY_FAMILY_BINDING_MAX_LIMIT, String.valueOf(settingsDTO.getFamilyBindingMaxLimit()));
        }
        if (settingsDTO.getProxyDailyActionLimit() != null) {
            updateConfigValue(KEY_PROXY_DAILY_ACTION_LIMIT, String.valueOf(settingsDTO.getProxyDailyActionLimit()));
        }
        if (settingsDTO.getOrderCancelTimeoutHours() != null) {
            updateConfigValue(KEY_ORDER_CANCEL_TIMEOUT_HOURS, String.valueOf(settingsDTO.getOrderCancelTimeoutHours()));
        }
        
        log.info("系统配置更新成功");
    }
    
    /**
     * 更新单个配置项的值
     *
     * @param configKey   配置键
     * @param configValue 配置值
     */
    private void updateConfigValue(String configKey, String configValue) {
        LambdaUpdateWrapper<SystemConfig> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(SystemConfig::getConfigKey, configKey)
               .set(SystemConfig::getConfigValue, configValue);
        
        int updated = systemConfigMapper.update(null, wrapper);
        if (updated == 0) {
            // 如果配置不存在，则插入新配置
            log.info("配置项不存在，新增配置：{} = {}", configKey, configValue);
            SystemConfig config = new SystemConfig();
            config.setConfigKey(configKey);
            config.setConfigValue(configValue);
            systemConfigMapper.insert(config);
        }
    }
    
    /**
     * 解析整数，如果解析失败返回默认值
     *
     * @param value        字符串值
     * @param defaultValue 默认值
     * @return 解析后的整数
     */
    private Integer parseIntOrDefault(String value, Integer defaultValue) {
        if (value == null || value.isEmpty()) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            log.warn("解析整数失败，值：{}，使用默认值：{}", value, defaultValue);
            return defaultValue;
        }
    }
}
