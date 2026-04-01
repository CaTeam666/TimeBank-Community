package com.example.aiend.service.client;

/**
 * 用户端个人服务接口
 * 处理用户端的个人相关业务逻辑（如签到等）
 *
 * @author AI-End
 * @since 2026-03-22
 */
public interface ClientUserService {
    
    /**
     * 老人每日签到奖励
     *
     * @return 奖励发放结果信息
     */
    String dailyLoginReward();
}
