package com.example.aiend.controller.client;

import com.example.aiend.common.Result;
import com.example.aiend.service.client.ClientUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 客户端用户操作控制器
 * 提供诸如每日老人登录触发签到等个性化请求
 *
 * @author AI-End
 * @since 2026-03-22
 */
@RestController
@RequestMapping("/client/user")
@Slf4j
@RequiredArgsConstructor
public class ClientUserController {

    private final ClientUserService clientUserService;

    /**
     * 每日签到（老人专属）
     * 前端在确认是老人的身份登录后调用此接口领取每日基础奖励
     *
     * @return 签到结果及奖励信息
     */
    @PostMapping("/daily-login")
    public Result<String> dailyLoginReward() {
        log.info("收到老人每日签到请求");
        String message = clientUserService.dailyLoginReward();
        return Result.success(message, message);
    }
}
