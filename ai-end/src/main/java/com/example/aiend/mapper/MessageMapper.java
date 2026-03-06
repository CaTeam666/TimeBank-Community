package com.example.aiend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiend.entity.Message;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统消息Mapper接口
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Mapper
public interface MessageMapper extends BaseMapper<Message> {
}
