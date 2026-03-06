package com.example.aiend.controller;

import com.example.aiend.common.Result;
import com.example.aiend.dto.request.OrderCancelDTO;
import com.example.aiend.dto.request.OrderQueryDTO;
import com.example.aiend.dto.request.OrderVerifyDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.service.OrderService;
import com.example.aiend.vo.ExchangeOrderVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 订单管理控制器
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/order")
public class OrderController {

    private final OrderService orderService;

    /**
     * 获取订单列表
     *
     * @param queryDTO 查询参数
     * @return 分页订单列表
     */
    @GetMapping("/list")
    public Result<PageResponseDTO<ExchangeOrderVO>> getOrderList(OrderQueryDTO queryDTO) {
        log.info("收到获取订单列表请求，查询条件：{}", queryDTO);
        PageResponseDTO<ExchangeOrderVO> result = orderService.getOrderList(queryDTO);
        return Result.success(result);
    }

    /**
     * 订单核销
     *
     * @param verifyDTO 核销请求
     * @return 操作结果
     */
    @PostMapping("/verify")
    public Result<String> verifyOrder(@Valid @RequestBody OrderVerifyDTO verifyDTO) {
        log.info("收到订单核销请求，参数：{}", verifyDTO);
        orderService.verifyOrder(verifyDTO);
        return Result.success("核销成功");
    }

    /**
     * 取消订单
     *
     * @param cancelDTO 取消请求
     * @return 操作结果
     */
    @PostMapping("/cancel")
    public Result<String> cancelOrder(@Valid @RequestBody OrderCancelDTO cancelDTO) {
        log.info("收到取消订单请求，ID：{}", cancelDTO.getId());
        orderService.cancelOrder(cancelDTO);
        return Result.success("订单已取消");
    }
}
