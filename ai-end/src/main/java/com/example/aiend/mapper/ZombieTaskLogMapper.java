package com.example.aiend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiend.entity.ZombieTaskLog;
import org.apache.ibatis.annotations.Mapper;

/**
 * 僵尸任务日志Mapper接口
 *
 * @author AI-End
 * @since 2026-01-14
 */
@Mapper
public interface ZombieTaskLogMapper extends BaseMapper<ZombieTaskLog> {
}
