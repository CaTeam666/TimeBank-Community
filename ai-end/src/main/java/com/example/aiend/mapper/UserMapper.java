package com.example.aiend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiend.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户 Mapper
 *
 * @author AI-End
 * @since 2025-12-19
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
}
