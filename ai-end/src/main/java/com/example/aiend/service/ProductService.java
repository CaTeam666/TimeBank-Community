package com.example.aiend.service;

import com.example.aiend.dto.request.ProductCreateDTO;
import com.example.aiend.dto.request.ProductDeleteDTO;
import com.example.aiend.dto.request.ProductStatusDTO;
import com.example.aiend.dto.request.ProductUpdateDTO;
import com.example.aiend.vo.ProductVO;

import java.util.List;

/**
 * 商品服务接口
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
public interface ProductService {

    /**
     * 获取商品列表
     *
     * @return 商品列表
     */
    List<ProductVO> getProductList();

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    ProductVO getProductDetail(String id);

    /**
     * 新增商品
     *
     * @param createDTO 新增商品请求
     * @return 新增的商品
     */
    ProductVO createProduct(ProductCreateDTO createDTO);

    /**
     * 更新商品
     *
     * @param updateDTO 更新商品请求
     */
    void updateProduct(ProductUpdateDTO updateDTO);

    /**
     * 更新商品状态
     *
     * @param statusDTO 状态更新请求
     */
    void updateProductStatus(ProductStatusDTO statusDTO);

    /**
     * 删除商品
     *
     * @param deleteDTO 删除商品请求
     */
    void deleteProduct(ProductDeleteDTO deleteDTO);
}
