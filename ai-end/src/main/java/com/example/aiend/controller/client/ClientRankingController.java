package com.example.aiend.controller.client;

import com.example.aiend.common.Result;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.config.interceptor.ProxyAuthInterceptor;
import com.example.aiend.service.client.ClientRankingService;
import com.example.aiend.vo.MonthlyRankingVO;
import com.example.aiend.vo.UserOrderStatsVO;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 用户端排行榜控制器
 * 提供志愿者接单排行榜查询功能
 *
 * @author AI-End
 * @since 2026-02-27
 */
@RestController
@RequestMapping("/client/ranking")
@Slf4j
@RequiredArgsConstructor
@Validated
public class ClientRankingController {

    private final ClientRankingService clientRankingService;

    /**
     * 查询当前接单数前五名排行
     * 获取当前月份（实时）接单数前5名的志愿者排行
     *
     * @return 当月前五名排行榜数据
     */
    @GetMapping("/top")
    public Result<MonthlyRankingVO> getCurrentTopRanking() {
        log.info("查询当前接单数前五名排行");

        MonthlyRankingVO result = clientRankingService.getCurrentTopRanking();
        return Result.success(result, "查询成功");
    }

    /**
     * 根据月份查看历史排行
     * 查询指定月份的排行榜数据（优先从缓存获取）
     *
     * @param period 期数 (必填，格式: YYYY-MM，如 2026-02)
     * @return 指定月份的排行榜数据
     */
    @GetMapping("/monthly")
    public Result<MonthlyRankingVO> getMonthlyRanking(
            @RequestParam("period") String period) {
        log.info("查询月度排行榜，期数: {}", period);

        MonthlyRankingVO result = clientRankingService.getMonthlyRanking(period);
        return Result.success(result, "查询成功");
    }

    /**
     * 查询当前用户接单数
     * 获取当前登录用户的接单统计信息
     * 支持代理模式：代理模式下自动查询被代理人的统计
     *
     * @param userId        用户ID（Query参数，可选）
     * @param authorization 认证令牌（Bearer token）
     * @param request       HTTP请求对象（用于获取代理模式信息）
     * @return 用户接单统计数据
     */
    @GetMapping("/my-stats")
    public Result<UserOrderStatsVO> getMyOrderStats(
            @RequestParam(required = false) String userId,
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletRequest request) {

        Long userIdLong;

        // 优先检查代理模式
        Boolean isProxyMode = (Boolean) request.getAttribute(ProxyAuthInterceptor.ATTR_IS_PROXY_MODE);
        if (Boolean.TRUE.equals(isProxyMode)) {
            // 代理模式：使用被代理人ID（老人ID）
            userIdLong = (Long) request.getAttribute(ProxyAuthInterceptor.ATTR_PROXY_USER_ID);
            log.info("代理模式查询接单统计，被代理人ID：{}", userIdLong);
        } else {
            // 非代理模式：优先使用请求参数，其次从Token解析
            if (userId != null && !userId.isEmpty()) {
                userIdLong = Long.parseLong(userId);
            } else {
                userIdLong = parseUserIdFromToken(authorization);
            }
        }

        log.info("查询用户接单统计，用户ID: {}", userIdLong);

        UserOrderStatsVO result = clientRankingService.getUserOrderStats(userIdLong);
        return Result.success(result, "查询成功");
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
