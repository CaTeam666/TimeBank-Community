package com.example.aiend.controller;

import com.example.aiend.common.Result;
import com.example.aiend.dto.request.ProductCreateDTO;
import com.example.aiend.dto.request.ProductDeleteDTO;
import com.example.aiend.dto.request.ProductStatusDTO;
import com.example.aiend.dto.request.ProductUpdateDTO;
import com.example.aiend.service.ProductService;
import com.example.aiend.vo.ProductVO;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 商品管理控制器
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    /**
     * 获取商品列表
     *
     * @return 商品列表
     */
    @GetMapping("/list")
    public Result<List<ProductVO>> getProductList() {
        log.info("收到获取商品列表请求");
        List<ProductVO> list = productService.getProductList();
        return Result.success(list);
    }

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @GetMapping("/{id}")
    public Result<ProductVO> getProductDetail(@PathVariable String id) {
        log.info("收到获取商品详情请求，ID：{}", id);
        ProductVO product = productService.getProductDetail(id);
        return Result.success(product);
    }

    /**
     * 新增商品
     *
     * @param createDTO 新增商品请求
     * @return 新增的商品
     */
    @PostMapping("/create")
    public Result<ProductVO> createProduct(@Valid @RequestBody ProductCreateDTO createDTO) {
        log.info("收到新增商品请求，参数：{}", createDTO);
        ProductVO product = productService.createProduct(createDTO);
        return Result.success(product);
    }

    /**
     * 更新商品
     *
     * @param updateDTO 更新商品请求
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result<Void> updateProduct(@Valid @RequestBody ProductUpdateDTO updateDTO) {
        log.info("收到更新商品请求，参数：{}", updateDTO);
        productService.updateProduct(updateDTO);
        return Result.success();
    }

    /**
     * 更新商品状态（上下架）
     *
     * @param statusDTO 状态更新请求
     * @return 操作结果
     */
    @PostMapping("/status")
    public Result<Void> updateProductStatus(@Valid @RequestBody ProductStatusDTO statusDTO) {
        log.info("收到更新商品状态请求，参数：{}", statusDTO);
        productService.updateProductStatus(statusDTO);
        return Result.success();
    }

    /**
     * 删除商品
     *
     * @param deleteDTO 删除商品请求
     * @return 操作结果
     */
    @PostMapping("/delete")
    public Result<Void> deleteProduct(@Valid @RequestBody ProductDeleteDTO deleteDTO) {
        log.info("收到删除商品请求，ID：{}", deleteDTO.getId());
        productService.deleteProduct(deleteDTO);
        return Result.success();
    }
}
