package com.example.aiend.controller.client;

import com.example.aiend.common.Result;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.config.interceptor.ProxyAuthInterceptor;
import com.example.aiend.dto.request.client.FamilyBindDTO;
import com.example.aiend.dto.request.client.FamilyReviewDTO;
import com.example.aiend.dto.request.client.FamilyUnbindDTO;
import com.example.aiend.dto.request.client.ProxyToggleDTO;
import com.example.aiend.dto.response.client.FamilyBindResponseVO;
import com.example.aiend.dto.response.client.FamilyMemberVO;
import com.example.aiend.dto.response.client.PendingConfirmCountVO;
import com.example.aiend.dto.response.client.PendingRequestListVO;
import com.example.aiend.dto.response.client.ProxyToggleResponseVO;
import com.example.aiend.service.client.FamilyService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 亲情代理控制器
 * 处理亲情账号绑定、解绑、代理切换等用户端请求
 *
 * @author AI-End
 * @since 2026-02-07
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
@RequestMapping("/family")
public class FamilyController {
    
    private final FamilyService familyService;
    
    /**
     * 申请绑定亲情账号
     * 子女用户通过手机号申请绑定长者账号
     *
     * @param authorization 认证令牌（Bearer token）
     * @param bindDTO 绑定申请请求参数
     * @param request HTTP请求对象（用于获取代理模式信息）
     * @return 绑定申请响应（包含关系记录ID）
     */
    @PostMapping("/bind")
    public Result<FamilyBindResponseVO> bind(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody FamilyBindDTO bindDTO,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request, authorization);
        log.info("申请绑定亲情账号，userId：{}，phone：{}", userId, bindDTO.getPhone());
        FamilyBindResponseVO response = familyService.bind(userId, bindDTO);
        return Result.success(response, "绑定申请已提交，等待审核");
    }
    
    /**
     * 获取亲情账号列表
     * 获取当前用户已绑定的亲情账号列表（仅返回已通过审核的绑定关系）
     *
     * @param authorization 认证令牌（Bearer token）
     * @param request HTTP请求对象（用于获取代理模式信息）
     * @return 亲情账号列表
     */
    @GetMapping("/list")
    public Result<List<FamilyMemberVO>> getList(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request, authorization);
        log.info("获取亲情账号列表，userId：{}", userId);
        List<FamilyMemberVO> list = familyService.getList(userId);
        return Result.success(list, "获取成功");
    }
    
    /**
     * 解绑亲情账号
     * 解除与长者的绑定关系
     *
     * @param authorization 认证令牌（Bearer token）
     * @param unbindDTO 解绑请求参数
     * @param request HTTP请求对象（用于获取代理模式信息）
     * @return 操作结果
     */
    @PostMapping("/unbind")
    public Result<Void> unbind(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody FamilyUnbindDTO unbindDTO,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request, authorization);
        log.info("解绑亲情账号，userId：{}，relationId：{}", userId, unbindDTO.getRelationId());
        familyService.unbind(userId, unbindDTO);
        return Result.success(null, "解绑成功");
    }
    
    /**
     * 切换代理模式
     * 开启或关闭代理模式，子女可代替长者进行操作
     *
     * @param authorization 认证令牌（Bearer token）
     * @param toggleDTO 代理模式切换请求参数
     * @param request HTTP请求对象（用于获取代理模式信息）
     * @return 代理模式响应（包含代理令牌）
     */
    @PostMapping("/proxy/toggle")
    public Result<ProxyToggleResponseVO> toggleProxy(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody ProxyToggleDTO toggleDTO,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request, authorization);
        log.info("切换代理模式，userId：{}，enable：{}", userId, toggleDTO.getEnable());
        ProxyToggleResponseVO response = familyService.toggleProxy(userId, toggleDTO);
        String message = Boolean.TRUE.equals(toggleDTO.getEnable()) ? "已切换为代理模式" : "已退出代理模式";
        return Result.success(response, message);
    }
    
    /**
     * 获取待审核的绑定申请
     * 获取其他用户向当前用户发起的待审核绑定申请
     *
     * @param authorization 认证令牌（Bearer token）
     * @param request HTTP请求对象（用于获取代理模式信息）
     * @return 待审核申请列表
     */
    @GetMapping("/pending-requests")
    public Result<PendingRequestListVO> getPendingRequests(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request, authorization);
        log.info("获取待审核的绑定申请，userId：{}", userId);
        PendingRequestListVO response = familyService.getPendingRequests(userId);
        return Result.success(response, "获取成功");
    }
    
    /**
     * 审核绑定申请
     * 被绑定方审核绑定申请（通过或拒绝）
     *
     * @param authorization 认证令牌（Bearer token）
     * @param reviewDTO 审核请求参数
     * @param request HTTP请求对象（用于获取代理模式信息）
     * @return 操作结果
     */
    @PostMapping("/review")
    public Result<Void> review(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Valid @RequestBody FamilyReviewDTO reviewDTO,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request, authorization);
        log.info("审核绑定申请，userId：{}，relationId：{}，action：{}", 
                userId, reviewDTO.getRelationId(), reviewDTO.getAction());
        familyService.review(userId, reviewDTO);
        return Result.success(null, "审核成功");
    }
    
    /**
     * 获取待确认的绑定申请数量
     * 获取当前用户作为长者(parent_id)收到的待确认绑定申请数量
     * 用于个人页面消息红点提示
     *
     * @param authorization 认证令牌（Bearer token）
     * @param request HTTP请求对象（用于获取代理模式信息）
     * @return 待确认申请数量
     */
    @GetMapping("/pending-confirm-count")
    public Result<PendingConfirmCountVO> getPendingConfirmCount(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) {
        Long userId = getUserIdFromRequest(request, authorization);
        log.info("获取待确认的绑定申请数量，userId：{}", userId);
        PendingConfirmCountVO response = familyService.getPendingConfirmCount(userId);
        return Result.success(response, "获取成功");
    }
    
    /**
     * 从请求中获取用户ID
     * 优先检查代理模式，代理模式下使用 realUserId（子女ID）
     * 非代理模式下从 Authorization header 解析用户ID
     *
     * @param request HTTP请求对象
     * @param authorization Authorization header值
     * @return 用户ID
     * @throws BusinessException 当无法获取用户ID时抛出异常
     */
    private Long getUserIdFromRequest(HttpServletRequest request, String authorization) {
        // 检查是否是代理模式
        Boolean isProxyMode = (Boolean) request.getAttribute(ProxyAuthInterceptor.ATTR_IS_PROXY_MODE);
        if (Boolean.TRUE.equals(isProxyMode)) {
            // 代理模式下，亲情功能使用实际操作人ID（子女ID）
            Long realUserId = (Long) request.getAttribute(ProxyAuthInterceptor.ATTR_PROXY_REAL_USER_ID);
            if (realUserId != null) {
                log.debug("代理模式，使用实际操作人ID：{}", realUserId);
                return realUserId;
            }
        }
        
        // 非代理模式，从 Authorization header 解析用户ID
        return parseUserIdFromToken(authorization);
    }
    
    /**
     * 从 Authorization header 解析用户ID
     * Token格式: Bearer client-jwt-token-{userId}-{timestamp}
     *
     * @param authorization Authorization header值
     * @return 用户ID
     * @throws BusinessException 当token无效或未提供时抛出异常
     */
    private Long parseUserIdFromToken(String authorization) {
        if (!StringUtils.hasText(authorization)) {
            throw new BusinessException(401, "未登录或登录已过期，请重新登录");
        }
        
        // 移除 "Bearer " 前缀
        String token = authorization;
        if (authorization.startsWith("Bearer ")) {
            token = authorization.substring(7);
        }
        
        // 解析 token，格式: client-jwt-token-{userId}-{timestamp}
        try {
            String[] parts = token.split("-");
            // parts: ["client", "jwt", "token", "{userId}", "{timestamp}"]
            if (parts.length >= 4) {
                return Long.parseLong(parts[3]);
            }
        } catch (NumberFormatException e) {
            log.warn("解析token失败，token: {}", token);
        }
        
        throw new BusinessException(401, "无效的登录令牌，请重新登录");
    }
}
