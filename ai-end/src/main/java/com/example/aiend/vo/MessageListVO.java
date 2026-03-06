package com.example.aiend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 消息列表响应VO
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageListVO {
    
    /**
     * 总记录数
     */
    private Long total;
    
    /**
     * 消息列表
     */
    private List<MessageVO> list;
}
