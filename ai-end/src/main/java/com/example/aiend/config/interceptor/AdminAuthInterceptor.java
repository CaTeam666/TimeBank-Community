package com.example.aiend.config.interceptor;

import com.example.aiend.service.AdminAuthService;
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
 * 系统端认证拦截器
 * 用于验证系统端（管理后台）请求的令牌
 * 
 * 工作流程：
 * 1. 获取请求中的 Authorization Token
 * 2. 判断是否为系统端 Token（以 admin-jwt-token- 开头）
 * 3. 验证 Token 有效性（Redis 中是否存在且匹配）
 * 4. 验证通过则将用户ID设置到请求属性中，供后续使用
 *
 * @author AI-End
 * @since 2026-02-27
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AdminAuthInterceptor implements HandlerInterceptor {
    
    private final AdminAuthService adminAuthService;
    private final ObjectMapper objectMapper;
    
    /**
     * 请求属性：当前登录的管理员用户ID
     */
    public static final String ATTR_ADMIN_USER_ID = "adminUserId";
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String path = request.getRequestURI();
        String method = request.getMethod();
        
        // OPTIONS 请求直接放行（CORS 预检请求）
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }
        
        // 获取 Authorization header
        String authorization = request.getHeader("Authorization");
        
        // 如果没有 Token，返回 401
        if (authorization == null || authorization.isEmpty()) {
            log.warn("系统端请求缺少 Authorization header，path：{}", path);
            sendUnauthorizedResponse(response, "请先登录");
            return false;
        }
        
        // 判断是否是系统端 Token
        if (!adminAuthService.isAdminToken(authorization)) {
            log.warn("非系统端 Token，path：{}", path);
            sendUnauthorizedResponse(response, "无效的令牌");
            return false;
        }
        
        // 验证 Token
        String userId = adminAuthService.validateToken(authorization);
        if (userId == null) {
            log.warn("系统端 Token 验证失败，path：{}", path);
            sendUnauthorizedResponse(response, "令牌已过期或无效，请重新登录");
            return false;
        }
        
        // 将用户ID设置到请求属性中
        request.setAttribute(ATTR_ADMIN_USER_ID, userId);
        
        log.debug("系统端认证通过，userId：{}，path：{}", userId, path);
        return true;
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
