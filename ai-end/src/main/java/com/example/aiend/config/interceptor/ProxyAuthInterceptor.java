package com.example.aiend.config.interceptor;

import com.example.aiend.common.constant.ProxyWhitelistConstant;
import com.example.aiend.common.util.ProxyTokenUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 代理模式权限验证拦截器
 * 用于验证代理模式请求的权限和白名单
 * 
 * 工作流程：
 * 1. 解析请求中的 Authorization Token
 * 2. 如果 token 是 proxyToken（以 proxy- 开头），解析代理信息
 * 3. 如果请求的接口在白名单中，使用 userId（被代理人ID）执行操作
 * 4. 如果请求的接口不在白名单中，使用 realUserId（实际操作人ID）执行操作
 * 5. 如果不是 proxyToken，直接放行（由其他拦截器或业务逻辑处理）
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ProxyAuthInterceptor implements HandlerInterceptor {
    
    /**
     * JSON 序列化工具
     */
    private final ObjectMapper objectMapper;
    
    /**
     * 请求属性：被代理人ID（老人ID）- 白名单接口执行操作时使用此ID
     */
    public static final String ATTR_PROXY_USER_ID = "proxyUserId";
    
    /**
     * 请求属性：实际操作人ID（子女ID）- 用于审计日志和非白名单接口
     */
    public static final String ATTR_PROXY_REAL_USER_ID = "proxyRealUserId";
    
    /**
     * 请求属性：是否是代理模式
     */
    public static final String ATTR_IS_PROXY_MODE = "isProxyMode";
    
    /**
     * 请求属性：是否是白名单接口
     */
    public static final String ATTR_IS_WHITELIST = "isProxyWhitelist";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 获取 Authorization header
        String authorization = request.getHeader("Authorization");
        
        // 如果没有 Authorization header，直接放行
        if (authorization == null || authorization.isEmpty()) {
            return true;
        }
        
        // 判断是否是代理令牌
        if (!ProxyTokenUtil.isProxyToken(authorization)) {
            // 不是代理令牌，直接放行
            return true;
        }
        
        // 是代理令牌，解析并处理
        String method = request.getMethod();
        String path = request.getRequestURI();
        
        log.info("检测到代理模式请求，method：{}，path：{}", method, path);
        
        try {
            // 解析代理令牌
            ProxyTokenUtil.ProxyTokenPayload payload = ProxyTokenUtil.parseProxyToken(authorization);
            
            // 检查接口是否在白名单中
            boolean isWhitelist = ProxyWhitelistConstant.isInWhitelist(method, path);
            
            // 将代理信息设置到请求属性中
            request.setAttribute(ATTR_PROXY_USER_ID, payload.getUserId());
            request.setAttribute(ATTR_PROXY_REAL_USER_ID, payload.getRealUserId());
            request.setAttribute(ATTR_IS_PROXY_MODE, true);
            request.setAttribute(ATTR_IS_WHITELIST, isWhitelist);
            
            if (isWhitelist) {
                // 白名单接口：使用被代理人ID（老人ID）
                log.info("代理模式-白名单接口，使用被代理人ID：{}，实际操作人ID：{}，path：{}", 
                        payload.getUserId(), payload.getRealUserId(), path);
            } else {
                // 非白名单接口：使用实际操作人ID（子女ID）
                log.info("代理模式-非白名单接口，使用实际操作人ID：{}，path：{}", 
                        payload.getRealUserId(), path);
            }
            
            return true;
            
        } catch (Exception e) {
            log.warn("代理令牌验证失败，error：{}", e.getMessage());
            sendUnauthorizedResponse(response, e.getMessage());
            return false;
        }
    }
    
    /**
     * 发送 401 Unauthorized 响应
     * 
     * @param response HTTP 响应
     * @param message 错误消息
     * @throws IOException 写入响应时可能抛出的异常
     */
    private void sendUnauthorizedResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        result.put("code", 401);
        result.put("message", message);
        result.put("data", null);
        result.put("timestamp", System.currentTimeMillis());
        
        response.getWriter().write(objectMapper.writeValueAsString(result));
    }
}
