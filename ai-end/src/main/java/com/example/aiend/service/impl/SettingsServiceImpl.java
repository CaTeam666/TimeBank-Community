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
    private static final String KEY_TRANSACTION_FEE_PERCENT = "transaction_fee_percent";
    
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
        dto.setElderInitialCoins(parseIntOrDefault(configMap.get(KEY_ELDER_INITIAL_COINS), 0));
        dto.setDailySignInReward(parseIntOrDefault(configMap.get(KEY_DAILY_SIGN_IN_REWARD), 0));
        dto.setMonthlyRank1Reward(parseIntOrDefault(configMap.get(KEY_MONTHLY_RANK_1_REWARD), 0));
        dto.setTransactionFeePercent(parseIntOrDefault(configMap.get(KEY_TRANSACTION_FEE_PERCENT), 0));
        
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
        if (settingsDTO.getTransactionFeePercent() != null) {
            updateConfigValue(KEY_TRANSACTION_FEE_PERCENT, String.valueOf(settingsDTO.getTransactionFeePercent()));
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
