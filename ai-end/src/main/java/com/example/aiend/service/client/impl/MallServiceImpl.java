package com.example.aiend.service.client.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.client.ExchangeRequestDTO;
import com.example.aiend.dto.response.client.ExchangeResponseDTO;
import com.example.aiend.dto.response.client.ProductDetailDTO;
import com.example.aiend.dto.response.client.ProductListDTO;
import com.example.aiend.entity.ExchangeOrder;
import com.example.aiend.entity.Product;
import com.example.aiend.entity.User;
import com.example.aiend.mapper.ExchangeOrderMapper;
import com.example.aiend.mapper.ProductMapper;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.service.client.MallService;
import com.example.aiend.vo.MyExchangeOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * 爱心超市服务实现类
 * 处理商品查询和积分兑换业务
 *
 * @author AI-End
 * @since 2026-01-13
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MallServiceImpl implements MallService {
    
    private final ProductMapper productMapper;
    private final UserMapper userMapper;
    private final ExchangeOrderMapper exchangeOrderMapper;
    
    /** 时间格式化器 */
    private static final DateTimeFormatter ORDER_NO_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
    
    /**
     * 获取商品列表
     * 查询上架状态的商品，支持按名称搜索和分类筛选
     *
     * @param keyword 搜索关键词（可选）
     * @param category 商品分类（可选）
     * @return 商品列表
     */
    @Override
    public List<ProductListDTO> getProductList(String keyword, String category) {
        log.info("获取商品列表，keyword：{}，category：{}", keyword, category);
        
        // 构建查询条件
        LambdaQueryWrapper<Product> queryWrapper = new LambdaQueryWrapper<>();
        // 只查询上架状态的商品
        queryWrapper.eq(Product::getStatus, 1);
        
        // 按关键词模糊搜索商品名称
        if (StringUtils.hasText(keyword)) {
            queryWrapper.like(Product::getName, keyword);
        }
        
        // 按分类筛选
        if (StringUtils.hasText(category)) {
            queryWrapper.eq(Product::getCategory, category);
        }
        
        // 按创建时间倒序
        queryWrapper.orderByDesc(Product::getCreateTime);
        
        List<Product> products = productMapper.selectList(queryWrapper);
        
        // 转换为DTO
        return products.stream()
                .map(this::convertToListDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取商品详情
     *
     * @param productId 商品ID
     * @return 商品详情
     */
    @Override
    public ProductDetailDTO getProductDetail(Long productId) {
        log.info("获取商品详情，productId：{}", productId);
        
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        
        return convertToDetailDTO(product);
    }
    
    /**
     * 积分兑换商品
     * 事务处理：扣减库存、冻结积分、创建订单
     * 注意：兑换时只冻结时间币，核销后才真正扣除
     *
     * @param requestDTO 兑换请求
     * @return 兑换结果
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ExchangeResponseDTO exchangeProduct(ExchangeRequestDTO requestDTO) {
        log.info("积分兑换请求，userId：{}，productId：{}，quantity：{}", 
                requestDTO.getUserId(), requestDTO.getProductId(), requestDTO.getQuantity());
        
        Long userId = Long.parseLong(requestDTO.getUserId());
        Long productId = requestDTO.getProductId();
        Integer quantity = requestDTO.getQuantity();
        
        // 1. 查询商品信息
        Product product = productMapper.selectById(productId);
        if (product == null) {
            throw new BusinessException(404, "商品不存在");
        }
        if (product.getStatus() != 1) {
            throw new BusinessException(400, "商品已下架");
        }
        if (product.getStock() < quantity) {
            throw new BusinessException(400, "库存不足");
        }
        
        // 计算总价
        int totalPrice = product.getPrice() * quantity;
        
        // 2. 查询用户信息并校验余额
        User user = userMapper.selectById(String.valueOf(userId));
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (user.getBalance() == null || user.getBalance() < totalPrice) {
            throw new BusinessException(400, "积分不足");
        }
        
        // 3. 扣减库存并增加销量
        LambdaUpdateWrapper<Product> productUpdate = new LambdaUpdateWrapper<>();
        productUpdate.eq(Product::getId, productId)
                .ge(Product::getStock, quantity)  // 乐观锁：库存大于等于兑换数量
                .setSql("stock = stock - " + quantity)
                .setSql("sales_count = sales_count + " + quantity)
                .set(Product::getUpdateTime, LocalDateTime.now());
        int updated = productMapper.update(null, productUpdate);
        if (updated == 0) {
            throw new BusinessException(400, "库存不足，兑换失败");
        }
        log.info("商品{}库存扣减成功，数量：{}", productId, quantity);
        
        // 检查库存是否为0，如果为0则自动下架
        Product updatedProduct = productMapper.selectById(productId);
        if (updatedProduct.getStock() == 0) {
            LambdaUpdateWrapper<Product> offShelfUpdate = new LambdaUpdateWrapper<>();
            offShelfUpdate.eq(Product::getId, productId)
                    .set(Product::getStatus, 0)  // 0: 下架
                    .set(Product::getUpdateTime, LocalDateTime.now());
            productMapper.update(null, offShelfUpdate);
            log.info("商品{}库存为0，已自动下架", productId);
        }
        
        // 4. 冻结用户时间币（从可用余额转入冻结余额）
        Integer currentBalance = user.getBalance();
        Integer currentFrozen = user.getFrozenBalance() != null ? user.getFrozenBalance() : 0;
        user.setBalance(currentBalance - totalPrice);
        user.setFrozenBalance(currentFrozen + totalPrice);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("用户{}时间币冻结成功，冻结金额：{}，当前冻结总额：{}", 
                userId, totalPrice, currentFrozen + totalPrice);
        
        // 5. 生成订单编号和核销码
        String orderNo = generateOrderNo();
        String verifyCode = generateVerifyCode();
        
        // 6. 创建兑换订单（状态为待核销）
        ExchangeOrder order = new ExchangeOrder();
        order.setOrderNo(orderNo);
        order.setUserId(userId);
        order.setProductId(productId);
        order.setAmount(totalPrice);  // 消耗时间币
        order.setVerifyCode(verifyCode);
        order.setStatus(0);  // 0: 待核销
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        exchangeOrderMapper.insert(order);
        log.info("兑换订单创建成功，orderNo：{}，verifyCode：{}", orderNo, verifyCode);
        
        // 注意：兑换时不记录流水，核销时才记录
        
        return ExchangeResponseDTO.builder()
                .orderNo(orderNo)
                .verifyCode(verifyCode)
                .build();
    }
    
    /**
     * 转换为列表DTO
     */
    private ProductListDTO convertToListDTO(Product product) {
        return ProductListDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .image(product.getImage())
                .price(product.getPrice())
                .category(product.getCategory())
                .stock(product.getStock())
                .sales(product.getSalesCount())
                .build();
    }
    
    /**
     * 转换为详情DTO
     */
    private ProductDetailDTO convertToDetailDTO(Product product) {
        return ProductDetailDTO.builder()
                .id(product.getId())
                .name(product.getName())
                .image(product.getImage())
                .price(product.getPrice())
                .category(product.getCategory())
                .stock(product.getStock())
                .sales(product.getSalesCount())
                .description(product.getDescription())
                .build();
    }
    
    /**
     * 生成订单编号
     * 格式：ORD + 年月日时分秒 + 3位随机数
     */
    private String generateOrderNo() {
        String timestamp = LocalDateTime.now().format(ORDER_NO_FORMATTER);
        int random = new Random().nextInt(900) + 100;  // 100-999
        return "ORD" + timestamp + random;
    }
    
    /**
     * 生成6位核销码
     */
    private String generateVerifyCode() {
        int code = new Random().nextInt(900000) + 100000;  // 100000-999999
        return String.valueOf(code);
    }
    
    /**
     * 获取我的兑换订单列表
     * 查询指定用户的兑换订单记录，联表查询商品信息
     *
     * @param userId 用户ID
     * @return 订单列表
     */
    @Override
    public List<MyExchangeOrderVO> getMyOrders(String userId) {
        log.info("获取我的兑换订单列表，userId：{}", userId);
        
        Long userIdLong = Long.parseLong(userId);
        List<MyExchangeOrderVO> orders = exchangeOrderMapper.selectMyOrders(userIdLong);
        
        log.info("查询到订单数量：{}", orders.size());
        return orders;
    }
}
