package com.example.aiend.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 未读消息数量响应VO
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UnreadCountVO {
    
    /**
     * 未读消息总数
     */
    private Integer total;
    
    /**
     * 按类型分组的未读数量
     */
    private List<TypeCountItem> details;
    
    /**
     * 类型未读数量项
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TypeCountItem {
        /**
         * 消息类型
         */
        private String type;
        
        /**
         * 类型名称
         */
        private String typeName;
        
        /**
         * 该类型未读数量
         */
        private Integer count;
    }
}
