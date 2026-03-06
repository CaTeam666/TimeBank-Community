package com.example.aiend.controller.client;

import com.example.aiend.common.Result;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.service.MessageService;
import com.example.aiend.vo.MessageListVO;
import com.example.aiend.vo.MessageVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 消息中心控制器
 * 提供消息列表查询功能，消息在业务处理完成后自动删除
 *
 * @author AI-End
 * @since 2026-02-07
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/message")
public class MessageController {
    
    private final MessageService messageService;
    
    /**
     * 获取个人消息列表
     * 支持按类型筛选，优先从Redis缓存获取
     *
     * @param authorization 认证令牌（Bearer token）
     * @param type          消息类型（可选）
     * @param pageNum       页码（默认1）
     * @param pageSize      每页条数（默认10）
     * @return 消息列表
     */
    @GetMapping("/list")
    public Result<MessageListVO> getMessageList(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestParam(required = false) String type,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        
        Long userId = parseUserIdFromToken(authorization);
        log.info("获取消息列表，用户ID：{}，类型：{}", userId, type);
        
        // 获取消息列表
        List<MessageVO> messages;
        if (StringUtils.hasText(type)) {
            messages = messageService.getPendingMessagesByType(userId, type);
        } else {
            messages = messageService.getPendingMessages(userId);
        }
        
        // 简单分页处理
        int total = messages.size();
        int fromIndex = (pageNum - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, total);
        
        List<MessageVO> pagedList = new ArrayList<>();
        if (fromIndex < total) {
            pagedList = messages.subList(fromIndex, toIndex);
        }
        
        MessageListVO result = MessageListVO.builder()
                .total((long) total)
                .list(pagedList)
                .build();
        
        return Result.success(result, "获取成功");
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
