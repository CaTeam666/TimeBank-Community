package com.example.aiend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiend.entity.AdminUser;
import org.apache.ibatis.annotations.Mapper;

/**
 * 系统端用户 Mapper
 * 用于操作 t_user 表
 *
 * @author AI-End
 * @since 2026-02-27
 */
@Mapper
public interface AdminUserMapper extends BaseMapper<AdminUser> {
}
