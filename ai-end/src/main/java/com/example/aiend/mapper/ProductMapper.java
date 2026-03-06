package com.example.aiend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiend.entity.Product;
import org.apache.ibatis.annotations.Mapper;

/**
 * 商品Mapper接口
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Mapper
public interface ProductMapper extends BaseMapper<Product> {
}
