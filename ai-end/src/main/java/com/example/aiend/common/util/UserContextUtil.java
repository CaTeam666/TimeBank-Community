package com.example.aiend.common.util;

import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.config.interceptor.ProxyAuthInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 用户上下文工具类
 * 用于获取当前请求的用户身份信息
 * 
 * 支持两种模式：
 * 1. 普通模式：从 Authorization header 中的普通 token 解析用户ID
 * 2. 代理模式：从拦截器设置的请求属性中获取被代理人ID
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Slf4j
public final class UserContextUtil {
    
    /**
     * 私有构造方法，防止实例化
     */
    private UserContextUtil() {
        throw new IllegalStateException("工具类不允许实例化");
    }
    
    /**
     * 获取当前请求的操作用户ID
     * 
     * 在代理模式下，返回被代理人ID（老人ID）
     * 在普通模式下，从 token 解析并返回登录用户ID
     * 
     * @return 操作用户ID
     * @throws BusinessException 当无法获取用户ID时抛出
     */
    public static Long getCurrentUserId() {
        HttpServletRequest request = getRequest();
        return getUserId(request);
    }
    
    /**
     * 获取当前请求的操作用户ID
     * 
     * @param request HTTP 请求对象
     * @return 操作用户ID
     * @throws BusinessException 当无法获取用户ID时抛出
     */
    public static Long getUserId(HttpServletRequest request) {
        // 优先检查是否是代理模式
        Boolean isProxyMode = (Boolean) request.getAttribute(ProxyAuthInterceptor.ATTR_IS_PROXY_MODE);
        if (Boolean.TRUE.equals(isProxyMode)) {
            Long proxyUserId = (Long) request.getAttribute(ProxyAuthInterceptor.ATTR_PROXY_USER_ID);
            if (proxyUserId != null) {
                log.debug("代理模式，返回被代理人ID：{}", proxyUserId);
                return proxyUserId;
            }
        }
        
        // 非代理模式，从 Authorization header 解析用户ID
        String authorization = request.getHeader("Authorization");
        return parseUserIdFromToken(authorization);
    }
    
    /**
     * 获取实际操作人ID
     * 
     * 在代理模式下，返回实际操作人ID（子女ID）
     * 在普通模式下，返回登录用户ID
     * 
     * @return 实际操作人ID
     * @throws BusinessException 当无法获取用户ID时抛出
     */
    public static Long getRealUserId() {
        HttpServletRequest request = getRequest();
        return getRealUserId(request);
    }
    
    /**
     * 获取实际操作人ID
     * 
     * @param request HTTP 请求对象
     * @return 实际操作人ID
     * @throws BusinessException 当无法获取用户ID时抛出
     */
    public static Long getRealUserId(HttpServletRequest request) {
        // 检查是否是代理模式
        Boolean isProxyMode = (Boolean) request.getAttribute(ProxyAuthInterceptor.ATTR_IS_PROXY_MODE);
        if (Boolean.TRUE.equals(isProxyMode)) {
            Long realUserId = (Long) request.getAttribute(ProxyAuthInterceptor.ATTR_PROXY_REAL_USER_ID);
            if (realUserId != null) {
                log.debug("代理模式，返回实际操作人ID：{}", realUserId);
                return realUserId;
            }
        }
        
        // 非代理模式，从 Authorization header 解析用户ID
        String authorization = request.getHeader("Authorization");
        return parseUserIdFromToken(authorization);
    }
    
    /**
     * 判断当前是否是代理模式
     * 
     * @return 如果是代理模式返回 true，否则返回 false
     */
    public static boolean isProxyMode() {
        HttpServletRequest request = getRequest();
        return isProxyMode(request);
    }
    
    /**
     * 判断当前是否是代理模式
     * 
     * @param request HTTP 请求对象
     * @return 如果是代理模式返回 true，否则返回 false
     */
    public static boolean isProxyMode(HttpServletRequest request) {
        Boolean isProxyMode = (Boolean) request.getAttribute(ProxyAuthInterceptor.ATTR_IS_PROXY_MODE);
        return Boolean.TRUE.equals(isProxyMode);
    }
    
    /**
     * 获取当前 HTTP 请求对象
     * 
     * @return HTTP 请求对象
     * @throws BusinessException 当无法获取请求对象时抛出
     */
    private static HttpServletRequest getRequest() {
        ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) {
            throw new BusinessException(500, "无法获取请求上下文");
        }
        return attrs.getRequest();
    }
    
    /**
     * 从 Authorization header 解析用户ID
     * Token格式: Bearer client-jwt-token-{userId}-{timestamp}
     *
     * @param authorization Authorization header 值
     * @return 用户ID
     * @throws BusinessException 当令牌无效时抛出
     */
    private static Long parseUserIdFromToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            throw new BusinessException(401, "未登录或登录已过期，请重新登录");
        }
        
        String token = authorization;
        if (authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        
        try {
            // 普通 token 格式: client-jwt-token-{userId}-{timestamp}
            String[] parts = token.split("-");
            if (parts.length >= 4) {
                return Long.parseLong(parts[3]);
            }
        } catch (NumberFormatException e) {
            log.warn("解析token失败，token: {}", token);
        }
        
        throw new BusinessException(401, "无效的登录令牌，请重新登录");
    }
}
