package com.example.aiend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiend.entity.IdentityAudit;
import org.apache.ibatis.annotations.Mapper;

/**
 * 实名认证审核 Mapper
 *
 * @author AI-End
 * @since 2025-12-24
 */
@Mapper
public interface IdentityAuditMapper extends BaseMapper<IdentityAudit> {
}
