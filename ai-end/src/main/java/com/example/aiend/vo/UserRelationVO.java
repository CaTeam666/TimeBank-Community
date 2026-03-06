package com.example.aiend.vo;

import lombok.Builder;
import lombok.Data;

/**
 * 用户关系（亲情绑定）视图对象 VO
 *
 * @author AI-End
 * @since 2025-12-25
 */
@Data
@Builder
public class UserRelationVO {
    
    /**
     * 关系ID
     */
    private Long id;
    
    /**
     * 子女/代理人用户ID
     */
    private Long childId;
    
    /**
     * 子女/代理人姓名
     */
    private String childName;
    
    /**
     * 子女/代理人手机号
     */
    private String childPhone;
    
    /**
     * 父母/老人用户ID
     */
    private Long parentId;
    
    /**
     * 父母/老人姓名
     */
    private String parentName;
    
    /**
     * 父母/老人手机号
     */
    private String parentPhone;
    
    /**
     * 证明材料图片URL
     */
    private String proofImg;
    
    /**
     * 关系状态（0:待审核 1:已通过 2:已拒绝）
     */
    private Integer status;
    
    /**
     * 拒绝原因
     */
    private String rejectReason;
    
    /**
     * 创建时间
     */
    private String createTime;
}
