package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.aiend.common.enums.MessageTypeEnum;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.entity.Message;
import com.example.aiend.mapper.MessageMapper;
import com.example.aiend.service.MessageService;
import com.example.aiend.vo.MessageVO;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 消息服务实现类
 * 实现消息的创建、查询、状态更新等功能
 * 支持 Redis 缓存，按消息类型分别缓存
 *
 * @author AI-End
 * @since 2026-02-07
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {
    
    private final MessageMapper messageMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    
    /**
     * Redis 消息缓存 Key 前缀（按用户+类型）
     * 格式: message:user:{userId}:type:{type}
     */
    private static final String MESSAGE_USER_TYPE_KEY_PREFIX = "message:user:";
    
    /**
     * Redis 用户全部消息缓存 Key 前缀
     * 格式: message:user:{userId}:all
     */
    private static final String MESSAGE_USER_ALL_KEY_PREFIX = "message:user:";
    
    /**
     * 缓存过期时间（小时）
     */
    private static final long CACHE_EXPIRE_HOURS = 24;
    
    /**
     * 消息状态：待处理
     */
    private static final Integer STATUS_PENDING = 0;
    
    /**
     * 消息状态：已处理
     */
    private static final Integer STATUS_PROCESSED = 1;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long createMessage(Long receiverId, String type, Long bizId, String title, String content) {
        log.info("准备创建/更新消息，接收人：{}，类型：{}，业务ID：{}", receiverId, type, bizId);
        
        // 1. 检查是否存在同类型且待处理的相同业务消息（幂等去重）
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getReceiverId, receiverId)
                .eq(Message::getType, type)
                .eq(Message::getBizId, bizId)
                .eq(Message::getStatus, STATUS_PENDING)
                .eq(Message::getIsDeleted, 0);
        
        Message existingMessage = messageMapper.selectOne(queryWrapper);
        
        if (existingMessage != null) {
            log.info("发现已存在待处理消息，执行更新操作，消息ID：{}", existingMessage.getId());
            existingMessage.setTitle(title);
            existingMessage.setContent(content);
            existingMessage.setUpdateTime(LocalDateTime.now());
            messageMapper.updateById(existingMessage);
            
            // 同步更新到 Redis
            syncMessageToRedis(existingMessage);
            return existingMessage.getId();
        }

        // 2. 不存在则创建新消息实体
        Message message = Message.builder()
                .receiverId(receiverId)
                .type(type)
                .bizId(bizId)
                .title(title)
                .content(content)
                .status(STATUS_PENDING)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .isDeleted(0)
                .build();
        
        // 3. 保存到数据库
        messageMapper.insert(message);
        log.info("新消息保存到数据库成功，消息ID：{}", message.getId());
        
        // 4. 同步到 Redis 缓存
        syncMessageToRedis(message);
        
        return message.getId();
    }
    
    @Override
    public List<MessageVO> getPendingMessages(Long receiverId) {
        log.info("获取用户待处理消息，用户ID：{}", receiverId);
        
        // 1. 尝试从 Redis 获取全部消息
        String allKey = MESSAGE_USER_ALL_KEY_PREFIX + receiverId + ":all";
        List<MessageVO> cachedMessages = getMessagesFromRedis(allKey);
        
        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            log.info("从 Redis 获取到用户消息，数量：{}", cachedMessages.size());
            return cachedMessages;
        }
        
        // 2. Redis 未命中，从数据库查询
        log.info("Redis 缓存未命中，从数据库查询");
        List<Message> messages = getMessagesFromDb(receiverId, null);
        
        // 3. 转换为 VO
        List<MessageVO> messageVOs = convertToVOList(messages);
        
        // 4. 同步到 Redis
        if (!messageVOs.isEmpty()) {
            saveMessagesToRedis(allKey, messageVOs);
        }
        
        return messageVOs;
    }
    
    @Override
    public List<MessageVO> getPendingMessagesByType(Long receiverId, String type) {
        log.info("获取用户指定类型待处理消息，用户ID：{}，类型：{}", receiverId, type);
        
        // 1. 尝试从 Redis 获取指定类型消息
        String typeKey = MESSAGE_USER_TYPE_KEY_PREFIX + receiverId + ":type:" + type;
        List<MessageVO> cachedMessages = getMessagesFromRedis(typeKey);
        
        if (cachedMessages != null && !cachedMessages.isEmpty()) {
            log.info("从 Redis 获取到指定类型消息，类型：{}，数量：{}", type, cachedMessages.size());
            return cachedMessages;
        }
        
        // 2. Redis 未命中，从数据库查询
        log.info("Redis 缓存未命中，从数据库查询类型：{}", type);
        List<Message> messages = getMessagesFromDb(receiverId, type);
        
        // 3. 转换为 VO
        List<MessageVO> messageVOs = convertToVOList(messages);
        
        // 4. 同步到 Redis
        if (!messageVOs.isEmpty()) {
            saveMessagesToRedis(typeKey, messageVOs);
        }
        
        return messageVOs;
    }
    
    @Override
    public Integer getPendingMessageCount(Long receiverId) {
        log.info("获取用户待处理消息数量，用户ID：{}", receiverId);
        
        // 优先从缓存获取
        List<MessageVO> messages = getPendingMessages(receiverId);
        return messages.size();
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsProcessed(Long messageId, Long receiverId) {
        log.info("标记消息为已处理，消息ID：{}，用户ID：{}", messageId, receiverId);
        
        // 1. 查询消息
        Message message = messageMapper.selectById(messageId);
        if (message == null) {
            throw new BusinessException(404, "消息不存在");
        }
        
        // 2. 校验权限
        if (!receiverId.equals(message.getReceiverId())) {
            throw new BusinessException(403, "无权操作此消息");
        }
        
        // 3. 更新状态
        message.setStatus(STATUS_PROCESSED);
        message.setUpdateTime(LocalDateTime.now());
        messageMapper.updateById(message);
        
        // 4. 清除用户的 Redis 缓存
        clearUserMessageCache(receiverId);
        
        log.info("消息已标记为已处理，消息ID：{}", messageId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAsProcessedByBizId(Long bizId, String type) {
        log.info("根据业务ID标记消息为已处理，业务ID：{}，类型：{}", bizId, type);
        
        // 1. 查询对应的消息
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getBizId, bizId)
                .eq(Message::getType, type)
                .eq(Message::getStatus, STATUS_PENDING)
                .eq(Message::getIsDeleted, 0);
        
        Message message = messageMapper.selectOne(queryWrapper);
        if (message == null) {
            log.info("未找到对应的待处理消息，业务ID：{}，类型：{}", bizId, type);
            return;
        }
        
        // 2. 更新状态
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Message::getBizId, bizId)
                .eq(Message::getType, type)
                .eq(Message::getStatus, STATUS_PENDING)
                .set(Message::getStatus, STATUS_PROCESSED)
                .set(Message::getUpdateTime, LocalDateTime.now());
        
        messageMapper.update(null, updateWrapper);
        
        // 3. 清除用户的 Redis 缓存
        clearUserMessageCache(message.getReceiverId());
        
        log.info("已根据业务ID标记消息为已处理，业务ID：{}，类型：{}", bizId, type);
    }
    
    @Override
    public void refreshCache(Long receiverId) {
        log.info("刷新用户消息缓存，用户ID：{}", receiverId);
        
        // 1. 清除旧缓存
        clearUserMessageCache(receiverId);
        
        // 2. 重新加载并缓存
        getPendingMessages(receiverId);
        
        log.info("用户消息缓存已刷新，用户ID：{}", receiverId);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public MessageVO getMessageDetail(Long messageId, Long receiverId) {
        log.info("获取消息详情，消息ID：{}，用户ID：{}", messageId, receiverId);
        
        // 1. 查询消息
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getIsDeleted() == 1) {
            throw new BusinessException(404, "消息不存在");
        }
        
        // 2. 校验权限
        if (!receiverId.equals(message.getReceiverId())) {
            throw new BusinessException(403, "无权查看此消息");
        }
        
        // 3. 如果是未读消息，标记为已读
        if (STATUS_PENDING.equals(message.getStatus())) {
            message.setStatus(STATUS_PROCESSED);
            message.setUpdateTime(LocalDateTime.now());
            messageMapper.updateById(message);
            
            // 清除缓存
            clearUserMessageCache(receiverId);
            
            log.info("消息已自动标记为已读，消息ID：{}", messageId);
        }
        
        return convertToVO(message);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteMessage(Long messageId, Long receiverId) {
        log.info("删除消息，消息ID：{}，用户ID：{}", messageId, receiverId);
        
        // 1. 查询消息
        Message message = messageMapper.selectById(messageId);
        if (message == null || message.getIsDeleted() == 1) {
            throw new BusinessException(404, "消息不存在");
        }
        
        // 2. 校验权限
        if (!receiverId.equals(message.getReceiverId())) {
            throw new BusinessException(403, "无权删除此消息");
        }
        
        // 3. 逻辑删除
        message.setIsDeleted(1);
        message.setUpdateTime(LocalDateTime.now());
        int rows = messageMapper.updateById(message);
        
        // 4. 清除缓存
        clearUserMessageCache(receiverId);
        
        log.info("消息删除成功，消息ID：{}", messageId);
        return rows > 0;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markAsProcessedBatch(List<Long> messageIds, Long receiverId) {
        log.info("批量标记消息为已读，消息IDs：{}，用户ID：{}", messageIds, receiverId);
        
        if (messageIds == null || messageIds.isEmpty()) {
            return 0;
        }
        
        // 批量更新（只更新属于该用户的消息）
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.in(Message::getId, messageIds)
                .eq(Message::getReceiverId, receiverId)
                .eq(Message::getStatus, STATUS_PENDING)
                .eq(Message::getIsDeleted, 0)
                .set(Message::getStatus, STATUS_PROCESSED)
                .set(Message::getUpdateTime, LocalDateTime.now());
        
        int rows = messageMapper.update(null, updateWrapper);
        
        // 清除缓存
        clearUserMessageCache(receiverId);
        
        log.info("批量标记已读成功，影响行数：{}", rows);
        return rows;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int markAsProcessedByType(String type, Long receiverId) {
        log.info("根据类型批量标记消息为已读，类型：{}，用户ID：{}", type, receiverId);
        
        // 批量更新该类型的所有未读消息
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Message::getType, type)
                .eq(Message::getReceiverId, receiverId)
                .eq(Message::getStatus, STATUS_PENDING)
                .eq(Message::getIsDeleted, 0)
                .set(Message::getStatus, STATUS_PROCESSED)
                .set(Message::getUpdateTime, LocalDateTime.now());
        
        int rows = messageMapper.update(null, updateWrapper);
        
        // 清除缓存
        clearUserMessageCache(receiverId);
        
        log.info("根据类型批量标记已读成功，类型：{}，影响行数：{}", type, rows);
        return rows;
    }
    
    @Override
    public Map<String, Integer> getPendingMessageCountByType(Long receiverId) {
        log.info("获取用户待处理消息数量（按类型分组），用户ID：{}", receiverId);
        
        // 从缓存或数据库获取所有待处理消息
        List<MessageVO> messages = getPendingMessages(receiverId);
        
        // 按类型分组统计
        Map<String, Integer> countByType = new HashMap<>();
        for (MessageTypeEnum typeEnum : MessageTypeEnum.values()) {
            countByType.put(typeEnum.getCode(), 0);
        }
        
        for (MessageVO message : messages) {
            String type = message.getType();
            countByType.put(type, countByType.getOrDefault(type, 0) + 1);
        }
        
        return countByType;
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteMessageByBizId(Long bizId, String type) {
        log.info("根据业务ID删除消息，业务ID：{}，类型：{}", bizId, type);
        
        // 1. 查询对应的消息
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getBizId, bizId)
                .eq(Message::getType, type)
                .eq(Message::getIsDeleted, 0);
        
        Message message = messageMapper.selectOne(queryWrapper);
        if (message == null) {
            log.info("未找到对应的消息，业务ID：{}，类型：{}", bizId, type);
            return;
        }
        
        // 2. 逻辑删除消息
        LambdaUpdateWrapper<Message> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Message::getBizId, bizId)
                .eq(Message::getType, type)
                .eq(Message::getIsDeleted, 0)
                .set(Message::getIsDeleted, 1)
                .set(Message::getUpdateTime, LocalDateTime.now());
        
        messageMapper.update(null, updateWrapper);
        
        // 3. 清除用户的 Redis 缓存
        clearUserMessageCache(message.getReceiverId());
        
        log.info("已根据业务ID删除消息，业务ID：{}，类型：{}", bizId, type);
    }
    
    /**
     * 同步单条消息到 Redis
     * 更新用户的全部消息缓存和类型消息缓存
     *
     * @param message 消息实体
     */
    private void syncMessageToRedis(Message message) {
        try {
            MessageVO messageVO = convertToVO(message);
            Long receiverId = message.getReceiverId();
            String type = message.getType();
            
            // 1. 更新用户全部消息缓存
            String allKey = MESSAGE_USER_ALL_KEY_PREFIX + receiverId + ":all";
            List<MessageVO> allMessages = getMessagesFromRedis(allKey);
            if (allMessages == null) {
                allMessages = new ArrayList<>();
            }
            // 移除可能存在的同ID旧消息
            allMessages.removeIf(m -> m.getId().equals(message.getId()));
            allMessages.add(0, messageVO);  // 新消息/更新的消息放在最前面
            saveMessagesToRedis(allKey, allMessages);
            
            // 2. 更新用户类型消息缓存
            String typeKey = MESSAGE_USER_TYPE_KEY_PREFIX + receiverId + ":type:" + type;
            List<MessageVO> typeMessages = getMessagesFromRedis(typeKey);
            if (typeMessages == null) {
                typeMessages = new ArrayList<>();
            }
            // 移除可能存在的同ID旧消息
            typeMessages.removeIf(m -> m.getId().equals(message.getId()));
            typeMessages.add(0, messageVO);  // 新消息/更新的消息放在最前面
            saveMessagesToRedis(typeKey, typeMessages);
            
            log.info("消息同步到 Redis 成功，消息ID：{}，类型：{}", message.getId(), type);
        } catch (Exception e) {
            log.error("同步消息到 Redis 失败，消息ID：{}", message.getId(), e);
            // Redis 操作失败不影响业务流程
        }
    }
    
    /**
     * 从 Redis 获取消息列表
     *
     * @param key Redis key
     * @return 消息列表
     */
    private List<MessageVO> getMessagesFromRedis(String key) {
        try {
            Object data = redisTemplate.opsForValue().get(key);
            if (data instanceof List) {
                return objectMapper.convertValue(data, new TypeReference<List<MessageVO>>() {});
            }
        } catch (Exception e) {
            log.error("从 Redis 获取消息失败，key：{}", key, e);
        }
        return null;
    }
    
    /**
     * 保存消息列表到 Redis
     *
     * @param key      Redis key
     * @param messages 消息列表
     */
    private void saveMessagesToRedis(String key, List<MessageVO> messages) {
        try {
            redisTemplate.opsForValue().set(key, messages, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("消息列表保存到 Redis，key：{}，数量：{}", key, messages.size());
        } catch (Exception e) {
            log.error("保存消息到 Redis 失败，key：{}", key, e);
        }
    }
    
    /**
     * 清除用户的消息缓存
     *
     * @param receiverId 接收人ID
     */
    private void clearUserMessageCache(Long receiverId) {
        try {
            // 清除全部消息缓存
            String allKey = MESSAGE_USER_ALL_KEY_PREFIX + receiverId + ":all";
            redisTemplate.delete(allKey);
            
            // 清除各类型消息缓存
            for (MessageTypeEnum typeEnum : MessageTypeEnum.values()) {
                String typeKey = MESSAGE_USER_TYPE_KEY_PREFIX + receiverId + ":type:" + typeEnum.getCode();
                redisTemplate.delete(typeKey);
            }
            
            log.info("已清除用户消息缓存，用户ID：{}", receiverId);
        } catch (Exception e) {
            log.error("清除用户消息缓存失败，用户ID：{}", receiverId, e);
        }
    }
    
    /**
     * 从数据库查询消息
     *
     * @param receiverId 接收人ID
     * @param type       消息类型（可选）
     * @return 消息列表
     */
    private List<Message> getMessagesFromDb(Long receiverId, String type) {
        LambdaQueryWrapper<Message> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Message::getReceiverId, receiverId)
                .eq(Message::getStatus, STATUS_PENDING)
                .eq(Message::getIsDeleted, 0);
        
        if (type != null) {
            queryWrapper.eq(Message::getType, type);
        }
        
        queryWrapper.orderByDesc(Message::getCreateTime);
        
        return messageMapper.selectList(queryWrapper);
    }
    
    /**
     * 将消息实体转换为 VO
     *
     * @param message 消息实体
     * @return 消息 VO
     */
    private MessageVO convertToVO(Message message) {
        MessageTypeEnum typeEnum = MessageTypeEnum.fromCode(message.getType());
        
        return MessageVO.builder()
                .id(message.getId())
                .receiverId(message.getReceiverId())
                .type(message.getType())
                .typeName(typeEnum != null ? typeEnum.getName() : message.getType())
                .bizId(message.getBizId())
                .title(message.getTitle())
                .content(message.getContent())
                .status(message.getStatus())
                .route(typeEnum != null ? typeEnum.getRoute() : null)
                .createTime(message.getCreateTime() != null 
                        ? message.getCreateTime().format(DATE_TIME_FORMATTER) : null)
                .build();
    }
    
    /**
     * 批量将消息实体转换为 VO
     *
     * @param messages 消息实体列表
     * @return 消息 VO 列表
     */
    private List<MessageVO> convertToVOList(List<Message> messages) {
        return messages.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }
}
