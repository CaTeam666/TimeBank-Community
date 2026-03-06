package com.example.aiend.common.constant;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 代理模式接口白名单常量类
 * 定义代理模式下允许访问的接口路径
 * 
 * 只有白名单中的接口才能在代理模式下被访问
 * 非白名单接口收到 proxyToken 时应返回 403 Forbidden
 *
 * @author AI-End
 * @since 2026-02-07
 */
public final class ProxyWhitelistConstant {
    
    /**
     * 私有构造方法，防止实例化
     */
    private ProxyWhitelistConstant() {
        throw new IllegalStateException("常量类不允许实例化");
    }
    
    // ==================== 任务相关接口 ====================
    
    /**
     * 发布任务
     */
    public static final String TASK_PUBLISH = "POST:/task/publish";
    
    /**
     * 查看我发布的任务
     */
    public static final String TASK_MY_PUBLISHED = "GET:/task/my/published";
    
    /**
     * 查看我接的任务（虽然老人不太会接任务，但需要支持查看）
     */
    public static final String TASK_MY_ACCEPTED = "GET:/task/my/accepted";
    
    // ==================== 用户相关接口 ====================
    
    /**
     * 查询积分余额
     */
    public static final String USER_BALANCE = "GET:/user/balance";
    
    // ==================== 商城相关接口 ====================
    
    /**
     * 查看商品列表
     */
    public static final String MALL_PRODUCTS = "GET:/mall/products";
    
    /**
     * 商品详情
     */
    public static final String MALL_PRODUCT_DETAIL = "GET:/mall/product/detail";
    
    /**
     * 兑换商品
     */
    public static final String MALL_EXCHANGE = "POST:/mall/exchange";
    
    /**
     * 查看兑换订单
     */
    public static final String MALL_ORDERS = "GET:/mall/orders";
    
    /**
     * 代理模式接口白名单集合
     * 使用 Set 提高查询效率
     */
    private static final Set<String> WHITELIST = new HashSet<>(Arrays.asList(
            // 任务相关
            TASK_PUBLISH,
            TASK_MY_PUBLISHED,
            TASK_MY_ACCEPTED,
            // 用户相关
            USER_BALANCE,
            // 商城相关
            MALL_PRODUCTS,
            MALL_PRODUCT_DETAIL,
            MALL_EXCHANGE,
            MALL_ORDERS
    ));
    
    /**
     * 判断指定接口是否在代理模式白名单中
     * 
     * @param method HTTP 方法（如 GET、POST）
     * @param path 请求路径（如 /task/publish）
     * @return 如果在白名单中返回 true，否则返回 false
     */
    public static boolean isInWhitelist(String method, String path) {
        if (method == null || path == null) {
            return false;
        }
        
        // 移除路径中可能的 /api 前缀
        String normalizedPath = path;
        if (normalizedPath.startsWith("/api")) {
            normalizedPath = normalizedPath.substring(4);
        }
        
        // 构建完整的接口标识
        String endpoint = method.toUpperCase() + ":" + normalizedPath;
        
        return WHITELIST.contains(endpoint);
    }
    
    /**
     * 获取白名单集合的副本
     * 
     * @return 白名单集合的不可变副本
     */
    public static Set<String> getWhitelist() {
        return Set.copyOf(WHITELIST);
    }
}
