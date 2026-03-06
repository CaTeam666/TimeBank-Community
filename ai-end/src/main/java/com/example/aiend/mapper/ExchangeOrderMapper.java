package com.example.aiend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiend.entity.ExchangeOrder;
import com.example.aiend.vo.MyExchangeOrderVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 兑换订单Mapper接口
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Mapper
public interface ExchangeOrderMapper extends BaseMapper<ExchangeOrder> {

    /**
     * 查询用户的兑换订单列表（联查商品信息）
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    @Select("SELECT " +
            "o.order_no AS id, " +
            "o.user_id AS userId, " +
            "o.product_id AS productId, " +
            "p.name AS productName, " +
            "p.image AS productImage, " +
            "p.price AS price, " +
            "1 AS quantity, " +
            "o.amount AS totalPrice, " +
            "o.status AS status, " +
            "o.verify_code AS verifyCode, " +
            "DATE_FORMAT(o.create_time, '%Y-%m-%d %H:%i:%s') AS createTime " +
            "FROM tb_exchange_order o " +
            "LEFT JOIN tb_product p ON o.product_id = p.id " +
            "WHERE o.user_id = #{userId} AND o.is_deleted = 0 " +
            "ORDER BY o.create_time DESC")
    List<MyExchangeOrderVO> selectMyOrders(@Param("userId") Long userId);
}
