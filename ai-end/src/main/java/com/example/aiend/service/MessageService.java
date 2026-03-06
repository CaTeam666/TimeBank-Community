package com.example.aiend.service;

import com.example.aiend.vo.MessageVO;

import java.util.List;

/**
 * 消息服务接口
 * 提供消息的创建、查询、状态更新等功能
 * 支持 Redis 缓存，按消息类型分别缓存
 *
 * @author AI-End
 * @since 2026-02-07
 */
public interface MessageService {
    
    /**
     * 创建消息
     * 同时写入数据库和 Redis 缓存
     *
     * @param receiverId 接收人ID
     * @param type       消息类型（如：FAMILY_BIND, TASK_VERIFY）
     * @param bizId      业务ID（关联的具体数据主键）
     * @param title      消息标题
     * @param content    消息内容
     * @return 消息ID
     */
    Long createMessage(Long receiverId, String type, Long bizId, String title, String content);
    
    /**
     * 获取用户的待处理消息列表
     * 优先从 Redis 缓存获取，缓存未命中则查询数据库
     *
     * @param receiverId 接收人ID
     * @return 待处理消息列表
     */
    List<MessageVO> getPendingMessages(Long receiverId);
    
    /**
     * 获取用户指定类型的待处理消息列表
     * 优先从 Redis 缓存获取
     *
     * @param receiverId 接收人ID
     * @param type       消息类型
     * @return 待处理消息列表
     */
    List<MessageVO> getPendingMessagesByType(Long receiverId, String type);
    
    /**
     * 获取用户的待处理消息数量
     *
     * @param receiverId 接收人ID
     * @return 待处理消息数量
     */
    Integer getPendingMessageCount(Long receiverId);
    
    /**
     * 将消息标记为已处理
     * 同时更新数据库和清除 Redis 缓存
     *
     * @param messageId  消息ID
     * @param receiverId 接收人ID（用于权限校验）
     */
    void markAsProcessed(Long messageId, Long receiverId);
    
    /**
     * 根据业务ID和类型将消息标记为已处理
     * 用于业务完成后自动处理对应消息
     *
     * @param bizId 业务ID
     * @param type  消息类型
     */
    void markAsProcessedByBizId(Long bizId, String type);
    
    /**
     * 刷新用户消息缓存
     * 从数据库重新加载并更新 Redis 缓存
     *
     * @param receiverId 接收人ID
     */
    void refreshCache(Long receiverId);
    
    /**
     * 获取消息详情
     * 同时自动标记该消息为已读
     *
     * @param messageId  消息ID
     * @param receiverId 接收人ID（用于权限校验）
     * @return 消息详情
     */
    MessageVO getMessageDetail(Long messageId, Long receiverId);
    
    /**
     * 删除消息（逻辑删除）
     *
     * @param messageId  消息ID
     * @param receiverId 接收人ID（用于权限校验）
     * @return 是否删除成功
     */
    boolean deleteMessage(Long messageId, Long receiverId);
    
    /**
     * 批量标记消息为已读
     *
     * @param messageIds 消息ID列表
     * @param receiverId 接收人ID
     * @return 成功标记的数量
     */
    int markAsProcessedBatch(List<Long> messageIds, Long receiverId);
    
    /**
     * 根据类型批量标记消息为已读
     *
     * @param type       消息类型
     * @param receiverId 接收人ID
     * @return 成功标记的数量
     */
    int markAsProcessedByType(String type, Long receiverId);
    
    /**
     * 获取用户待处理消息数量（按类型分组统计）
     *
     * @param receiverId 接收人ID
     * @return 按类型分组的未读消息数量
     */
    java.util.Map<String, Integer> getPendingMessageCountByType(Long receiverId);
    
    /**
     * 根据业务ID和类型删除消息
     * 用于业务处理完成后自动删除对应消息
     *
     * @param bizId 业务ID
     * @param type  消息类型
     */
    void deleteMessageByBizId(Long bizId, String type);
}
