package com.example.aiend.service.client;

import com.example.aiend.dto.request.client.ExchangeRequestDTO;
import com.example.aiend.dto.response.client.ExchangeResponseDTO;
import com.example.aiend.dto.response.client.ProductDetailDTO;
import com.example.aiend.dto.response.client.ProductListDTO;
import com.example.aiend.vo.MyExchangeOrderVO;

import java.util.List;

/**
 * 爱心超市服务接口
 * 处理商品查询和积分兑换业务
 *
 * @author AI-End
 * @since 2026-01-13
 */
public interface MallService {
    
    /**
     * 获取商品列表
     * 支持按名称搜索和按分类筛选
     *
     * @param keyword 搜索关键词（可选）
     * @param category 商品分类（可选）
     * @return 商品列表
     */
    List<ProductListDTO> getProductList(String keyword, String category);
    
    /**
     * 获取商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    ProductDetailDTO getProductDetail(Long productId);
    
    /**
     * 积分兑换商品
     * 包含库存扣减、积分扣减、订单创建、流水记录
     *
     * @param requestDTO 兑换请求
     * @return 兑换结果（订单编号和核销码）
     */
    ExchangeResponseDTO exchangeProduct(ExchangeRequestDTO requestDTO);
    
    /**
     * 获取我的兑换订单列表
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    List<MyExchangeOrderVO> getMyOrders(String userId);
}
