package com.example.aiend.controller.client;

import com.example.aiend.common.Result;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.config.interceptor.ProxyAuthInterceptor;
import com.example.aiend.dto.request.client.ExchangeRequestDTO;
import com.example.aiend.dto.response.client.ExchangeResponseDTO;
import com.example.aiend.dto.response.client.ProductDetailDTO;
import com.example.aiend.dto.response.client.ProductListDTO;
import com.example.aiend.service.client.MallService;
import com.example.aiend.vo.MyExchangeOrderVO;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 爱心超市控制器
 * 处理商品查询、商品详情、积分兑换等请求
 *
 * @author AI-End
 * @since 2026-01-13
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
public class MallController {
    
    private final MallService mallService;
    
    /**
     * 获取商品列表
     * 支持按名称搜索和按分类筛选
     *
     * @param keyword 搜索关键词（可选）
     * @param category 商品分类（可选）
     * @return 商品列表
     */
    @GetMapping("/mall/products")
    public Result<List<ProductListDTO>> getProductList(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category) {
        log.info("获取商品列表请求，keyword：{}，category：{}", keyword, category);
        
        List<ProductListDTO> products = mallService.getProductList(keyword, category);
        return Result.success(products, "查询成功");
    }
    
    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/mall/product/detail")
    public Result<ProductDetailDTO> getProductDetail(@RequestParam Long id) {
        log.info("获取商品详情请求，id：{}", id);
        
        ProductDetailDTO detail = mallService.getProductDetail(id);
        return Result.success(detail, "查询成功");
    }
    
    /**
     * 积分兑换商品
     * 用户使用积分兑换商品，生成兑换订单
     * 支持代理模式：代理模式下自动使用被代理人的积分进行兑换
     *
     * @param requestDTO 兑换请求
     * @param request HTTP请求对象（用于获取代理模式信息）
     * @return 兑换结果（订单编号和核销码）
     */
    @PostMapping("/mall/exchange")
    public Result<ExchangeResponseDTO> exchangeProduct(
            @Valid @RequestBody ExchangeRequestDTO requestDTO,
            HttpServletRequest request) {
        
        // 优先检查代理模式
        Boolean isProxyMode = (Boolean) request.getAttribute(ProxyAuthInterceptor.ATTR_IS_PROXY_MODE);
        if (Boolean.TRUE.equals(isProxyMode)) {
            // 代理模式：使用被代理人ID（老人ID）
            Long proxyUserId = (Long) request.getAttribute(ProxyAuthInterceptor.ATTR_PROXY_USER_ID);
            Long realUserId = (Long) request.getAttribute(ProxyAuthInterceptor.ATTR_PROXY_REAL_USER_ID);
            log.info("代理模式兑换商品，被代理人ID：{}，实际操作人ID：{}", proxyUserId, realUserId);
            requestDTO.setUserId(String.valueOf(proxyUserId));
        }
        
        log.info("积分兑换请求，userId：{}，productId：{}，quantity：{}", 
                requestDTO.getUserId(), requestDTO.getProductId(), requestDTO.getQuantity());
        
        ExchangeResponseDTO response = mallService.exchangeProduct(requestDTO);
        return Result.success(response, "兑换成功");
    }
    
    /**
     * 获取我的兑换订单列表
     * 查询指定用户的兑换订单记录
     * 支持代理模式：代理模式下自动查询被代理人的订单
     *
     * @param userId 用户ID
     * @param request HTTP请求对象（用于获取代理模式信息）
     * @return 订单列表
     */
    @GetMapping("/mall/orders")
    public Result<List<MyExchangeOrderVO>> getMyOrders(
            @RequestParam(required = false) String userId,
            HttpServletRequest request) {
        
        String queryUserId;
        
        // 优先检查代理模式
        Boolean isProxyMode = (Boolean) request.getAttribute(ProxyAuthInterceptor.ATTR_IS_PROXY_MODE);
        if (Boolean.TRUE.equals(isProxyMode)) {
            // 代理模式：使用被代理人ID（老人ID）
            Long proxyUserId = (Long) request.getAttribute(ProxyAuthInterceptor.ATTR_PROXY_USER_ID);
            log.info("代理模式查询订单列表，被代理人ID：{}", proxyUserId);
            queryUserId = String.valueOf(proxyUserId);
        } else {
            // 非代理模式：使用请求参数中的用户ID
            if (userId == null || userId.isEmpty()) {
                throw new BusinessException("用户ID不能为空");
            }
            queryUserId = userId;
        }
        
        log.info("获取我的兑换订单列表请求，userId：{}", queryUserId);
        
        List<MyExchangeOrderVO> orders = mallService.getMyOrders(queryUserId);
        return Result.success(orders, "查询成功");
    }
}
