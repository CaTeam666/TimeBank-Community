package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.enums.ProductStatusEnum;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.ProductCreateDTO;
import com.example.aiend.dto.request.ProductDeleteDTO;
import com.example.aiend.dto.request.ProductStatusDTO;
import com.example.aiend.dto.request.ProductUpdateDTO;
import com.example.aiend.entity.Product;
import com.example.aiend.mapper.ProductMapper;
import com.example.aiend.service.ProductService;
import com.example.aiend.vo.ProductVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 商品服务实现类
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductMapper productMapper;

    /**
     * 获取商品列表
     *
     * @return 商品列表
     */
    @Override
    public List<ProductVO> getProductList() {
        log.info("获取商品列表");
        
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.orderByDesc(Product::getCreateTime);
        
        List<Product> products = productMapper.selectList(queryWrapper);
        
        return products.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    /**
     * 获取商品详情
     *
     * @param id 商品ID
     * @return 商品详情
     */
    @Override
    public ProductVO getProductDetail(String id) {
        log.info("获取商品详情，ID：{}", id);
        
        Product product = productMapper.selectById(Long.parseLong(id));
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        
        return convertToVO(product);
    }

    /**
     * 新增商品
     *
     * @param createDTO 新增商品请求
     * @return 新增的商品
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductVO createProduct(ProductCreateDTO createDTO) {
        log.info("新增商品，请求参数：{}", createDTO);
        
        Product product = new Product();
        product.setName(createDTO.getName());
        product.setDescription(createDTO.getDescription());
        product.setImage(createDTO.getImage());
        product.setPrice(createDTO.getPrice());
        product.setStock(createDTO.getStock());
        product.setCategory(createDTO.getCategory());
        product.setSalesCount(0);
        
        // 解析状态
        ProductStatusEnum statusEnum = ProductStatusEnum.fromStatus(createDTO.getStatus());
        product.setStatus(statusEnum != null ? statusEnum.getCode() : ProductStatusEnum.ON_SHELF.getCode());
        
        productMapper.insert(product);
        log.info("商品新增成功，ID：{}", product.getId());
        
        return convertToVO(product);
    }

    /**
     * 更新商品
     *
     * @param updateDTO 更新商品请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProduct(ProductUpdateDTO updateDTO) {
        log.info("更新商品，请求参数：{}", updateDTO);
        
        Long productId = Long.parseLong(updateDTO.getId());
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        
        // 更新非空字段
        if (StringUtils.hasText(updateDTO.getName())) {
            product.setName(updateDTO.getName());
        }
        if (updateDTO.getDescription() != null) {
            product.setDescription(updateDTO.getDescription());
        }
        if (updateDTO.getImage() != null) {
            product.setImage(updateDTO.getImage());
        }
        if (updateDTO.getPrice() != null) {
            product.setPrice(updateDTO.getPrice());
        }
        if (updateDTO.getStock() != null) {
            product.setStock(updateDTO.getStock());
        }
        if (StringUtils.hasText(updateDTO.getCategory())) {
            product.setCategory(updateDTO.getCategory());
        }
        
        productMapper.updateById(product);
        log.info("商品更新成功，ID：{}", productId);
    }

    /**
     * 更新商品状态
     *
     * @param statusDTO 状态更新请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateProductStatus(ProductStatusDTO statusDTO) {
        log.info("更新商品状态，请求参数：{}", statusDTO);
        
        Long productId = Long.parseLong(statusDTO.getId());
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        
        ProductStatusEnum statusEnum = ProductStatusEnum.fromStatus(statusDTO.getStatus());
        if (statusEnum == null) {
            throw new BusinessException(400, "无效的状态值");
        }
        
        product.setStatus(statusEnum.getCode());
        productMapper.updateById(product);
        log.info("商品状态更新成功，ID：{}，新状态：{}", productId, statusDTO.getStatus());
    }

    /**
     * 删除商品
     *
     * @param deleteDTO 删除商品请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteProduct(ProductDeleteDTO deleteDTO) {
        log.info("删除商品，ID：{}", deleteDTO.getId());
        
        Long productId = Long.parseLong(deleteDTO.getId());
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        
        productMapper.deleteById(productId);
        log.info("商品删除成功，ID：{}", productId);
    }

    /**
     * 实体转VO
     *
     * @param product 商品实体
     * @return 商品VO
     */
    private ProductVO convertToVO(Product product) {
        ProductVO vo = new ProductVO();
        vo.setId(String.valueOf(product.getId()));
        vo.setName(product.getName());
        vo.setDescription(product.getDescription());
        vo.setImage(product.getImage());
        vo.setPrice(product.getPrice());
        vo.setStock(product.getStock());
        vo.setSalesCount(product.getSalesCount() != null ? product.getSalesCount() : 0);
        vo.setCategory(product.getCategory());
        
        // 转换状态
        ProductStatusEnum statusEnum = ProductStatusEnum.fromCode(product.getStatus());
        vo.setStatus(statusEnum != null ? statusEnum.getStatus() : ProductStatusEnum.ON_SHELF.getStatus());
        
        return vo;
    }
}
