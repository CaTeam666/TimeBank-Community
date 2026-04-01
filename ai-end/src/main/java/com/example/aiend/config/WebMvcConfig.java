package com.example.aiend.config;

import com.example.aiend.config.interceptor.AdminAuthInterceptor;
import com.example.aiend.config.interceptor.ProxyAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web MVC 配置类
 * 配置静态资源映射、拦截器等
 *
 * @author AI-End
 * @since 2025-12-28
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {
    
    /**
     * 代理模式权限验证拦截器
     */
    private final ProxyAuthInterceptor proxyAuthInterceptor;
    
    /**
     * 系统端认证拦截器
     */
    private final AdminAuthInterceptor adminAuthInterceptor;
    
    /**
     * 文件上传根目录
     */
    @Value("${file.upload.path:uploads}")
    private String uploadPath;
    
    /**
     * 配置静态资源映射
     * 将上传目录映射为可访问的静态资源
     *
     * @param registry 资源处理器注册器
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 将 /uploads/** 映射到本地上传目录
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadPath + "/");
    }
    
    /**
     * 配置拦截器
     * 注册代理模式拦截器和系统端认证拦截器
     *
     * @param registry 拦截器注册器
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 注册系统端认证拦截器，拦截系统端接口
        registry.addInterceptor(adminAuthInterceptor)
                .addPathPatterns(
                        "/users/**",       // 用户管理
                        "/identity/**",    // 实名审核
                        "/missions/**",    // 任务监控
                        "/arbitration/**", // 纠纷仲裁
                        "/evidence/**",    // 历史存证
                        "/product/**",     // 商品管理
                        "/order/**",       // 订单核销
                        "/anomaly/**",     // 异常数据
                        "/settings/**",    // 系统设置
                        "/ranking/**",     // 奖励监控
                        "/relations/**"    // 亲情绑定
                )
                .excludePathPatterns(
                        "/auth/**"         // 排除登录接口
                );
        
        // 注册代理模式权限验证拦截器，拦截客户端接口
        registry.addInterceptor(proxyAuthInterceptor)
                .addPathPatterns("/client/**")
                .excludePathPatterns(
                        "/uploads/**",     // 排除静态资源
                        "/error/**",       // 排除错误页面
                        "/actuator/**"     // 排除监控端点
                );
    }
}
