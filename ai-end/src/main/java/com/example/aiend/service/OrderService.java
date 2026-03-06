package com.example.aiend.service;

import com.example.aiend.dto.request.OrderCancelDTO;
import com.example.aiend.dto.request.OrderQueryDTO;
import com.example.aiend.dto.request.OrderVerifyDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.vo.ExchangeOrderVO;

/**
 * 订单服务接口
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
public interface OrderService {

    /**
     * 获取订单列表
     *
     * @param queryDTO 查询参数
     * @return 分页订单列表
     */
    PageResponseDTO<ExchangeOrderVO> getOrderList(OrderQueryDTO queryDTO);

    /**
     * 订单核销
     *
     * @param verifyDTO 核销请求
     */
    void verifyOrder(OrderVerifyDTO verifyDTO);

    /**
     * 取消订单
     *
     * @param cancelDTO 取消请求
     */
    void cancelOrder(OrderCancelDTO cancelDTO);
}
