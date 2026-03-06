package com.example.aiend.dto.response.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 待确认绑定申请数量响应类
 * 用于个人页面消息红点提示
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PendingConfirmCountVO {
    
    /**
     * 待确认申请数量
     */
    private Integer count;
}
