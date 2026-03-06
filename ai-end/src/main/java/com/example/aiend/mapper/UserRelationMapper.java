package com.example.aiend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiend.entity.UserRelation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户关系（亲情绑定）Mapper
 *
 * @author AI-End
 * @since 2025-12-25
 */
@Mapper
public interface UserRelationMapper extends BaseMapper<UserRelation> {
}
