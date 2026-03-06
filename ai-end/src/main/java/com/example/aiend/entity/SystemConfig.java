package com.example.aiend.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统配置实体类
 * 存储系统核心参数配置，以键值对形式存储
 *
 * @author AI-End
 * @since 2025-12-27
 */
@Data
@TableName("sys_config")
public class SystemConfig {
    
    /**
     * 配置ID（主键）
     */
    @TableId(type = IdType.AUTO)
    private Integer id;
    
    /**
     * 配置键
     */
    @TableField("config_key")
    private String configKey;
    
    /**
     * 配置值
     */
    @TableField("config_value")
    private String configValue;
    
    /**
     * 配置描述
     */
    @TableField("description")
    private String description;
    
    /**
     * 更新时间
     */
    @TableField("update_time")
    private LocalDateTime updateTime;
}
