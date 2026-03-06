package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiend.common.enums.OrderStatusEnum;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.OrderCancelDTO;
import com.example.aiend.dto.request.OrderQueryDTO;
import com.example.aiend.dto.request.OrderVerifyDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.entity.CoinLog;
import com.example.aiend.entity.ExchangeOrder;
import com.example.aiend.entity.Product;
import com.example.aiend.entity.User;
import com.example.aiend.mapper.CoinLogMapper;
import com.example.aiend.mapper.ExchangeOrderMapper;
import com.example.aiend.mapper.ProductMapper;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.service.OrderService;
import com.example.aiend.vo.ExchangeOrderVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 订单服务实现类
 *
 * @author AI-End Team
 * @since 2024-12-27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final ExchangeOrderMapper orderMapper;
    private final UserMapper userMapper;
    private final ProductMapper productMapper;
    private final CoinLogMapper coinLogMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取订单列表
     *
     * @param queryDTO 查询参数
     * @return 分页订单列表
     */
    @Override
    public PageResponseDTO<ExchangeOrderVO> getOrderList(OrderQueryDTO queryDTO) {
        log.info("获取订单列表，查询参数：{}", queryDTO);

        LambdaQueryWrapper<ExchangeOrder> queryWrapper = new LambdaQueryWrapper<>();

        // 状态筛选
        if (StringUtils.hasText(queryDTO.getStatus()) && !"ALL".equalsIgnoreCase(queryDTO.getStatus())) {
            OrderStatusEnum statusEnum = OrderStatusEnum.fromStatus(queryDTO.getStatus());
            if (statusEnum != null) {
                queryWrapper.eq(ExchangeOrder::getStatus, statusEnum.getCode());
            }
        }

        // 关键词搜索 - 订单号或志愿者名
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword().trim();
            
            // 按订单号搜索
            queryWrapper.and(w -> w.like(ExchangeOrder::getOrderNo, keyword)
                    .or().apply("user_id IN (SELECT id FROM sys_user WHERE nickname LIKE {0} OR realName LIKE {0})", 
                            "%" + keyword + "%"));
        }

        // 按创建时间倒序
        queryWrapper.orderByDesc(ExchangeOrder::getCreateTime);

        // 分页查询
        Page<ExchangeOrder> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        Page<ExchangeOrder> orderPage = orderMapper.selectPage(page, queryWrapper);

        // 转换为VO
        List<ExchangeOrderVO> listVOs = orderPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        return PageResponseDTO.<ExchangeOrderVO>builder()
                .list(listVOs)
                .total(orderPage.getTotal())
                .page(queryDTO.getPage())
                .pageSize(queryDTO.getPageSize())
                .build();
    }

    /**
     * 订单核销
     * 核销后扣除用户冻结时间币，记录流水
     *
     * @param verifyDTO 核销请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void verifyOrder(OrderVerifyDTO verifyDTO) {
        log.info("订单核销，请求参数：{}", verifyDTO);

        ExchangeOrder order = null;

        // 根据ID或核销码查询订单
        if (StringUtils.hasText(verifyDTO.getId())) {
            order = orderMapper.selectById(Long.parseLong(verifyDTO.getId()));
        } else if (StringUtils.hasText(verifyDTO.getVerifyCode())) {
            LambdaQueryWrapper<ExchangeOrder> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(ExchangeOrder::getVerifyCode, verifyDTO.getVerifyCode());
            order = orderMapper.selectOne(wrapper);
        }

        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 检查订单状态
        if (!OrderStatusEnum.PENDING.getCode().equals(order.getStatus())) {
            OrderStatusEnum statusEnum = OrderStatusEnum.fromCode(order.getStatus());
            throw new BusinessException(400, "订单状态异常，当前状态：" + 
                    (statusEnum != null ? statusEnum.getDesc() : "未知"));
        }
        
        // 查询用户
        User user = userMapper.selectById(String.valueOf(order.getUserId()));
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        Integer amount = order.getAmount();
        
        // 1. 扣除用户冻结时间币
        Integer frozenBalance = user.getFrozenBalance() != null ? user.getFrozenBalance() : 0;
        if (frozenBalance >= amount) {
            user.setFrozenBalance(frozenBalance - amount);
        } else {
            log.warn("用户{}冻结余额{}不足以扣除{}，将冻结余额清零", user.getId(), frozenBalance, amount);
            user.setFrozenBalance(0);
        }
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("用户{}冻结时间币扣除成功，金额：{}，剩余冻结：{}", 
                user.getId(), amount, user.getFrozenBalance());
        
        // 2. 记录时间币流水
        CoinLog coinLog = new CoinLog();
        coinLog.setUserId(order.getUserId());
        coinLog.setAmount(-amount);  // 负数表示支出
        coinLog.setType(3);  // 3: 兑换支出
        coinLog.setCreateTime(LocalDateTime.now());
        coinLog.setUpdateTime(LocalDateTime.now());
        coinLogMapper.insert(coinLog);
        log.info("兑换流水记录成功，userId：{}，amount：-{}", order.getUserId(), amount);

        // 3. 更新订单状态为已核销
        order.setStatus(OrderStatusEnum.COMPLETED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);
        
        log.info("订单核销成功，订单ID：{}，订单号：{}", order.getId(), order.getOrderNo());
    }

    /**
     * 取消订单
     * 取消后将冻结时间币退还到用户可用余额
     *
     * @param cancelDTO 取消请求
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(OrderCancelDTO cancelDTO) {
        log.info("取消订单，ID：{}", cancelDTO.getId());

        Long orderId = Long.parseLong(cancelDTO.getId());
        ExchangeOrder order = orderMapper.selectById(orderId);
        
        if (order == null) {
            throw new BusinessException(404, "订单不存在");
        }

        // 检查订单状态 - 只有待核销的订单可以取消
        if (!OrderStatusEnum.PENDING.getCode().equals(order.getStatus())) {
            throw new BusinessException(400, "只有待核销的订单可以取消");
        }
        
        // 查询用户
        User user = userMapper.selectById(String.valueOf(order.getUserId()));
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        Integer amount = order.getAmount();
        
        // 1. 将冻结时间币退还到可用余额
        Integer currentBalance = user.getBalance() != null ? user.getBalance() : 0;
        Integer frozenBalance = user.getFrozenBalance() != null ? user.getFrozenBalance() : 0;
        
        // 从冻结余额扣除
        if (frozenBalance >= amount) {
            user.setFrozenBalance(frozenBalance - amount);
        } else {
            log.warn("用户{}冻结余额{}不足以退还{}，将冻结余额清零", user.getId(), frozenBalance, amount);
            user.setFrozenBalance(0);
        }
        // 退还到可用余额
        user.setBalance(currentBalance + amount);
        user.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(user);
        log.info("用户{}时间币退还成功，退还金额：{}，当前余额：{}，剩余冻结：{}", 
                user.getId(), amount, user.getBalance(), user.getFrozenBalance());

        // 2. 更新订单状态为已取消
        order.setStatus(OrderStatusEnum.CANCELLED.getCode());
        order.setUpdateTime(LocalDateTime.now());
        orderMapper.updateById(order);

        log.info("订单取消成功，订单ID：{}，订单号：{}", orderId, order.getOrderNo());
    }

    /**
     * 实体转VO
     *
     * @param order 订单实体
     * @return 订单VO
     */
    private ExchangeOrderVO convertToVO(ExchangeOrder order) {
        ExchangeOrderVO vo = new ExchangeOrderVO();
        vo.setId(String.valueOf(order.getId()));
        vo.setOrderNo(order.getOrderNo());
        vo.setVolunteerId(String.valueOf(order.getUserId()));
        vo.setProductId(String.valueOf(order.getProductId()));
        vo.setCost(order.getAmount());
        vo.setVerifyCode(order.getVerifyCode());

        // 格式化时间
        if (order.getCreateTime() != null) {
            vo.setCreateTime(order.getCreateTime().format(DATE_TIME_FORMATTER));
        }

        // 转换状态
        OrderStatusEnum statusEnum = OrderStatusEnum.fromCode(order.getStatus());
        vo.setStatus(statusEnum != null ? statusEnum.getStatus() : OrderStatusEnum.PENDING.getStatus());

        // 关联查询用户信息
        User user = userMapper.selectById(order.getUserId());
        if (user != null) {
            vo.setVolunteerName(user.getNickname() != null ? user.getNickname() : user.getRealName());
        }

        // 关联查询商品信息
        Product product = productMapper.selectById(order.getProductId());
        if (product != null) {
            vo.setProductName(product.getName());
            vo.setProductImage(product.getImage());
        }

        return vo;
    }
}
