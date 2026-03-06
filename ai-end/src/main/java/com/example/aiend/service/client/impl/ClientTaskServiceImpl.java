package com.example.aiend.service.client.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.aiend.common.enums.MessageTypeEnum;
import com.example.aiend.common.enums.TaskStatusEnum;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.common.scheduler.TaskDelayDeleteScheduler;
import com.example.aiend.dto.request.client.TaskPublishDTO;
import com.example.aiend.dto.response.client.AppealDetailDTO;
import com.example.aiend.dto.response.client.MyAcceptedTaskDTO;
import com.example.aiend.dto.response.client.MyPublishedTaskDTO;
import com.example.aiend.dto.response.client.TaskCategoryDTO;
import com.example.aiend.dto.response.client.TaskDetailResponseDTO;
import com.example.aiend.dto.response.client.TaskHallItemDTO;
import com.example.aiend.dto.response.client.TaskPublishResponseDTO;
import com.example.aiend.dto.response.client.UserBalanceResponseDTO;
import com.example.aiend.dto.response.client.ReviewDetailDTO;
import com.example.aiend.entity.CoinLog;
import com.example.aiend.entity.Task;
import com.example.aiend.entity.TaskEvidence;
import com.example.aiend.entity.TaskReview;
import com.example.aiend.entity.User;
import com.example.aiend.entity.Appeal;
import com.example.aiend.mapper.CoinLogMapper;
import com.example.aiend.mapper.TaskEvidenceMapper;
import com.example.aiend.mapper.TaskMapper;
import com.example.aiend.mapper.TaskReviewMapper;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.mapper.AppealMapper;
import com.example.aiend.service.MessageService;
import com.example.aiend.service.client.ClientTaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 用户端任务服务实现类
 * 处理任务发布、任务大厅查询等业务
 *
 * @author AI-End
 * @since 2025-12-29
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ClientTaskServiceImpl implements ClientTaskService {
    
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;
    private final TaskEvidenceMapper taskEvidenceMapper;
    private final TaskReviewMapper taskReviewMapper;
    private final CoinLogMapper coinLogMapper;
    private final AppealMapper appealMapper;
    private final MessageService messageService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;
    private final TaskDelayDeleteScheduler taskDelayDeleteScheduler;
    
    /**
     * Redis 任务大厅缓存 Key
     */
    private static final String TASK_HALL_KEY = "task:hall:list";
    
    /**
     * Redis 单个任务缓存 Key 前缀
     */
    private static final String TASK_ITEM_KEY_PREFIX = "task:item:";
    
    /**
     * Redis 任务分类 ZSet Key 前缀
     */
    private static final String TASK_CATEGORY_KEY_PREFIX = "task:category:";
    
    /**
     * 缓存过期时间（小时）
     */
    private static final long CACHE_EXPIRE_HOURS = 24;
    
    /**
     * 中文类型到英文类型的映射（用于查询过滤）
     */
    private static final Map<String, String> TYPE_MAPPING = new HashMap<>();
    
    /**
     * 英文类型到中文类型的映射（用于存储）
     */
    private static final Map<String, String> TYPE_TO_CHINESE = new HashMap<>();
    
    static {
        // 中文 -> 英文（输入兼容）
        TYPE_MAPPING.put("陪聊", "CHAT");
        
        TYPE_MAPPING.put("保洁", "CLEANING");
        
        TYPE_MAPPING.put("跑腿", "ERRAND");
      
        TYPE_MAPPING.put("医疗陪护", "MEDICAL");
        
        TYPE_MAPPING.put("其他", "OTHER");
       
        
        // 英文 -> 标准中文（用于统一存储）
        TYPE_TO_CHINESE.put("CHAT", "陪聊");
        TYPE_TO_CHINESE.put("CLEANING", "保洁");
        TYPE_TO_CHINESE.put("ERRAND", "跑腿");
        TYPE_TO_CHINESE.put("MEDICAL", "医疗陪护");
        TYPE_TO_CHINESE.put("OTHER", "其他");
    }
    
    /**
     * 发布任务
     * 1. 扣除发布者时间币
     * 2. 保存任务到数据库
     * 3. 同步到 Redis 缓存
     *
     * @param publishDTO 任务发布请求
     * @param publisherId 发布者ID
     * @return 发布响应
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public TaskPublishResponseDTO publishTask(TaskPublishDTO publishDTO, Long publisherId) {
        log.info("发布任务，发布者ID：{}，任务标题：{}", publisherId, publishDTO.getTitle());
        
        // 查询发布者信息
        User publisher = userMapper.selectById(String.valueOf(publisherId));
        if (publisher == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 检查余额是否充足
        if (publisher.getBalance() == null || publisher.getBalance() < publishDTO.getCoins()) {
            throw new BusinessException(400, "时间币余额不足");
        }
        
        // 扣除时间币并冻结（从余额转到冻结余额）
        publisher.setBalance(publisher.getBalance() - publishDTO.getCoins());
        Integer currentFrozen = publisher.getFrozenBalance() != null ? publisher.getFrozenBalance() : 0;
        publisher.setFrozenBalance(currentFrozen + publishDTO.getCoins());
        publisher.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(publisher);
        log.info("时间币冻结成功，用户ID：{}，冻结：{}，剩余余额：{}，冻结余额：{}", 
                publisherId, publishDTO.getCoins(), publisher.getBalance(), publisher.getFrozenBalance());
        
        // 解析服务时间
        LocalDateTime serviceTime = parseServiceTime(publishDTO.getDate(), publishDTO.getTimeRange());
        
        // 创建任务
        Task task = new Task();
        task.setTitle(publishDTO.getTitle());
        task.setType(convertTaskType(publishDTO.getType()));  // 转换任务类型
        task.setDescription(publishDTO.getDescription());
        task.setPrice(publishDTO.getCoins());
        task.setLocation(publishDTO.getLocation());
        task.setPublisherId(publisherId);
        task.setStatus(0);  // 待接取
        task.setServiceTime(serviceTime);
        task.setCreateTime(LocalDateTime.now());
        task.setIsDeleted(0);
        
        taskMapper.insert(task);
        log.info("任务发布成功，任务ID：{}", task.getId());
        
        // 同步到 Redis 缓存
        TaskHallItemDTO taskItem = convertToTaskHallItem(task, publisher.getNickname(), publishDTO.getDate(), publishDTO.getTimeRange());
        syncTaskToRedis(taskItem);
        
        return TaskPublishResponseDTO.builder()
                .taskId(String.valueOf(task.getId()))
                .build();
    }
    
    /**
     * 获取任务大厅列表
     * 优先从 Redis ZSet 获取，Redis 没有则查询数据库
     *
     * @param type 任务类型过滤（可选，英文枥举值）
     * @return 任务列表
     */
    @Override
    public List<TaskHallItemDTO> getTaskHallList(String type) {
        log.info("获取任务大厅列表，类型过滤：{}", type);
        
        // 如果没有指定类型或者是 ALL，返回所有类型
        if (!StringUtils.hasText(type) || "ALL".equalsIgnoreCase(type)) {
            return getAllTasksFromRedisOrDb();
        }
        
        // 按分类从 Redis ZSet 获取
        List<TaskHallItemDTO> taskList = getTasksByCategory(type);
        
        log.info("返回任务列表，数量：{}", taskList.size());
        return taskList;
    }
    
    /**
     * 获取所有任务（优先Redis）
     * 只返回状态为 0（待接取）的任务
     */
    private List<TaskHallItemDTO> getAllTasksFromRedisOrDb() {
        // 尝试从 Redis 各分类 ZSet 中聚合数据
        List<TaskHallItemDTO> taskList = getAllTasksFromRedisZSets();
        
        if (taskList == null || taskList.isEmpty()) {
            // Redis 没有数据，从数据库查询（数据库查询已限制 status = 0）
            log.info("Redis 无缓存，从数据库查询");
            taskList = getTaskListFromDb();
            
            // 同步到 Redis ZSet 按分类存储
            if (!taskList.isEmpty()) {
                syncTasksByCategory(taskList);
            }
        }
        
        return taskList;
    }
    
    /**
     * 按分类从 Redis ZSet 获取任务
     */
    private List<TaskHallItemDTO> getTasksByCategory(String categoryKey) {
        try {
            // 转换为中文类型
            String chineseType = TYPE_TO_CHINESE.get(categoryKey.toUpperCase());
            if (chineseType == null) {
                log.warn("未知的任务类型：{}", categoryKey);
                return new ArrayList<>();
            }
            
            String key = TASK_CATEGORY_KEY_PREFIX + chineseType;
            Set<Object> taskData = redisTemplate.opsForZSet().range(key, 0, -1);
            
            if (taskData != null && !taskData.isEmpty()) {
                log.info("从 Redis ZSet 获取到 {} 个任务，分类：{}", taskData.size(), chineseType);
                return taskData.stream()
                        .map(data -> objectMapper.convertValue(data, TaskHallItemDTO.class))
                        .filter(t -> t.getStatus() != null && t.getStatus() == 0)
                        .collect(Collectors.toList());
            }
            
            // Redis 没有，从数据库查询
            log.info("Redis ZSet 无缓存，从数据库查询分类：{}", chineseType);
            List<TaskHallItemDTO> taskList = getTaskListFromDbByType(chineseType);
            
            // 同步到 Redis ZSet
            if (!taskList.isEmpty()) {
                syncTasksByCategory(taskList);
            }
            
            return taskList;
        } catch (Exception e) {
            log.error("从 Redis ZSet 获取任务失败，分类：{}", categoryKey, e);
            // 失败时降级到数据库查询
            String chineseType = TYPE_TO_CHINESE.get(categoryKey.toUpperCase());
            return chineseType != null ? getTaskListFromDbByType(chineseType) : new ArrayList<>();
        }
    }
    
    /**
     * 获取用户余额
     *
     * @param userId 用户ID
     * @return 用户余额
     */
    @Override
    public UserBalanceResponseDTO getUserBalance(Long userId) {
        log.info("获取用户余额，用户ID：{}", userId);
        
        User user = userMapper.selectById(String.valueOf(userId));
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        return UserBalanceResponseDTO.builder()
                .balance(user.getBalance() != null ? user.getBalance() : 0)
                .build();
    }
    
    /**
     * 从 Redis 获取任务列表
     */
    private List<TaskHallItemDTO> getTaskListFromRedis() {
        try {
            Object data = redisTemplate.opsForValue().get(TASK_HALL_KEY);
            if (data instanceof List) {
                // 使用 ObjectMapper 进行类型安全转换，解决 LinkedHashMap 无法转换为 DTO 的问题
                return objectMapper.convertValue(data, new TypeReference<List<TaskHallItemDTO>>() {});
            }
        } catch (Exception e) {
            log.error("从 Redis 获取任务列表失败", e);
        }
        return null;
    }
    
    /**
     * 从 Redis 所有分类 ZSet 中聚合获取全部任务
     * 只返回状态为 0（待接取）的任务
     */
    private List<TaskHallItemDTO> getAllTasksFromRedisZSets() {
        try {
            List<TaskHallItemDTO> allTasks = new ArrayList<>();
            
            // 遍历所有分类，从对应的 ZSet 中获取数据
            for (String chineseType : TYPE_TO_CHINESE.values()) {
                String key = TASK_CATEGORY_KEY_PREFIX + chineseType;
                Set<Object> taskData = redisTemplate.opsForZSet().reverseRange(key, 0, -1);  // 按 score 降序，新任务在前
                
                if (taskData != null && !taskData.isEmpty()) {
                    List<TaskHallItemDTO> categoryTasks = taskData.stream()
                            .map(data -> objectMapper.convertValue(data, TaskHallItemDTO.class))
                            .filter(t -> t.getStatus() != null && t.getStatus() == 0)  // 只保留待接取状态的任务
                            .collect(Collectors.toList());
                    allTasks.addAll(categoryTasks);
                    log.info("从 Redis ZSet 获取分类 {} 待接取任务数量：{}", chineseType, categoryTasks.size());
                }
            }
            
            if (!allTasks.isEmpty()) {
                log.info("从 Redis ZSet 聚合获取到待接取任务总数：{}", allTasks.size());
                // 按创建时间降序排序
                allTasks.sort((a, b) -> {
                    if (a.getCreateTime() == null || b.getCreateTime() == null) {
                        return 0;
                    }
                    return b.getCreateTime().compareTo(a.getCreateTime());
                });
                return allTasks;
            }
            
        } catch (Exception e) {
            log.error("从 Redis ZSet 聚合获取任务失败", e);
        }
        return null;
    }
    
    /**
     * 从数据库获取任务列表
     */
    private List<TaskHallItemDTO> getTaskListFromDb() {
        // 查询待接取的任务
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getStatus, 0)  // 待接取
                .orderByDesc(Task::getCreateTime);
        
        List<Task> tasks = taskMapper.selectList(queryWrapper);
        
        // 转换为 DTO
        List<TaskHallItemDTO> result = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (Task task : tasks) {
            // 查询发布者昵称
            User publisher = userMapper.selectById(String.valueOf(task.getPublisherId()));
            String publisherName = publisher != null ? publisher.getNickname() : "未知用户";
            
            String date = task.getServiceTime() != null ? task.getServiceTime().format(dateFormatter) : "";
            String timeRange = task.getServiceTime() != null ? task.getServiceTime().format(timeFormatter) : "";
            
            TaskHallItemDTO item = convertToTaskHallItem(task, publisherName, date, timeRange);
            result.add(item);
        }
        
        return result;
    }
    
    /**
     * 按类型从数据库获取任务列表
     */
    private List<TaskHallItemDTO> getTaskListFromDbByType(String chineseType) {
        // 查询指定类型的待接取任务
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getStatus, 0)  // 待接取
                .eq(Task::getType, chineseType)
                .orderByDesc(Task::getCreateTime);
        
        List<Task> tasks = taskMapper.selectList(queryWrapper);
        
        // 转换为 DTO
        List<TaskHallItemDTO> result = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (Task task : tasks) {
            // 查询发布者昵称
            User publisher = userMapper.selectById(String.valueOf(task.getPublisherId()));
            String publisherName = publisher != null ? publisher.getNickname() : "未知用户";
            
            String date = task.getServiceTime() != null ? task.getServiceTime().format(dateFormatter) : "";
            String timeRange = task.getServiceTime() != null ? task.getServiceTime().format(timeFormatter) : "";
            
            TaskHallItemDTO item = convertToTaskHallItem(task, publisherName, date, timeRange);
            result.add(item);
        }
        
        return result;
    }
    
    /**
     * 同步单个任务到 Redis（同时同步到全局列表和 ZSet 分类缓存）
     */
    private void syncTaskToRedis(TaskHallItemDTO taskItem) {
        try {
            // 1. 同步到全局列表缓存（兼容旧版本）
            List<TaskHallItemDTO> taskList = getTaskListFromRedis();
            if (taskList == null) {
                taskList = new ArrayList<>();
            }
            
            // 添加新任务到列表头部
            taskList.add(0, taskItem);
            
            // 保存到 Redis
            redisTemplate.opsForValue().set(TASK_HALL_KEY, taskList, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            // 2. 同步到 ZSet 分类缓存（按任务类型）
            String categoryKey = TASK_CATEGORY_KEY_PREFIX + taskItem.getType();
            double score = System.currentTimeMillis();
            redisTemplate.opsForZSet().add(categoryKey, taskItem, score);
            redisTemplate.expire(categoryKey, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            // 3. 同时缓存单个任务
            redisTemplate.opsForValue().set(TASK_ITEM_KEY_PREFIX + taskItem.getTaskId(), taskItem, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            log.info("任务同步到 Redis 成功（全局+ZSet），任务ID：{}，类型：{}", taskItem.getTaskId(), taskItem.getType());
        } catch (Exception e) {
            log.error("同步任务到 Redis 失败", e);
        }
    }
    
    /**
     * 同步所有任务到 Redis
     */
    private void syncAllTasksToRedis(List<TaskHallItemDTO> taskList) {
        try {
            redisTemplate.opsForValue().set(TASK_HALL_KEY, taskList, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("任务列表同步到 Redis 成功，数量：{}", taskList.size());
        } catch (Exception e) {
            log.error("同步任务列表到 Redis 失败", e);
        }
    }
    
    /**
     * 按分类同步任务到 Redis ZSet
     * 只同步状态为 0（待接取）的任务
     */
    private void syncTasksByCategory(List<TaskHallItemDTO> taskList) {
        try {
            // 先过滤出待接取状态的任务
            List<TaskHallItemDTO> pendingTasks = taskList.stream()
                    .filter(t -> t.getStatus() != null && t.getStatus() == 0)
                    .collect(Collectors.toList());
            
            if (pendingTasks.isEmpty()) {
                log.warn("没有待接取任务需要同步到 Redis");
                return;
            }
            
            // 按类型分组
            Map<String, List<TaskHallItemDTO>> tasksByType = pendingTasks.stream()
                    .collect(Collectors.groupingBy(TaskHallItemDTO::getType));
            
            // 分别存储到对应的 ZSet
            for (Map.Entry<String, List<TaskHallItemDTO>> entry : tasksByType.entrySet()) {
                String type = entry.getKey();
                List<TaskHallItemDTO> tasks = entry.getValue();
                
                String key = TASK_CATEGORY_KEY_PREFIX + type;
                // 先清空旧数据
                redisTemplate.delete(key);
                
                // 使用时间戳作为 score，新任务在前
                for (TaskHallItemDTO task : tasks) {
                    double score = System.currentTimeMillis();
                    redisTemplate.opsForZSet().add(key, task, score);
                }
                
                // 设置过期时间
                redisTemplate.expire(key, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                
                log.info("分类 {} 待接取任务同步到 Redis ZSet 成功，数量：{}", type, tasks.size());
            }
            
            // 同时保存全部待接取任务列表
            redisTemplate.opsForValue().set(TASK_HALL_KEY, pendingTasks, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            
            log.info("所有待接取任务按分类同步到 Redis ZSet 成功，总数：{}", pendingTasks.size());
        } catch (Exception e) {
            log.error("按分类同步任务到 Redis ZSet 失败", e);
        }
    }
    
    /**
     * 转换为任务大厅项 DTO
     */
    private TaskHallItemDTO convertToTaskHallItem(Task task, String publisherName, String date, String timeRange) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        
        return TaskHallItemDTO.builder()
                .taskId(String.valueOf(task.getId()))
                .title(task.getTitle())
                .type(task.getType())
                .description(task.getDescription())
                .coins(task.getPrice())
                .location(task.getLocation())
                .date(date)
                .timeRange(timeRange)
                .status(task.getStatus())
                .publisherId(String.valueOf(task.getPublisherId()))
                .publisherName(publisherName)
                .createTime(task.getCreateTime() != null ? task.getCreateTime().format(formatter) : "")
                .build();
    }
    
    /**
     * 解析服务时间
     */
    private LocalDateTime parseServiceTime(String date, String timeRange) {
        try {
            LocalDate serviceDate = LocalDate.parse(date, DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            // 从时间段中提取开始时间
            String startTime = timeRange.split("-")[0].trim();
            LocalTime serviceTimeOfDay = LocalTime.parse(startTime, DateTimeFormatter.ofPattern("HH:mm"));
            return LocalDateTime.of(serviceDate, serviceTimeOfDay);
        } catch (Exception e) {
            log.warn("解析服务时间失败，使用当前时间：date={}, timeRange={}", date, timeRange);
            return LocalDateTime.now();
        }
    }
    
    /**
     * 转换任务类型
     * 支持中文和英文类型，统一转换为中文存储
     *
     * @param type 原始类型
     * @return 中文类型
     */
    private String convertTaskType(String type) {
        if (type == null) {
            return "其他";
        }
        String trimmedType = type.trim();
        
        // 如果是英文类型，转换为中文
        String chineseType = TYPE_TO_CHINESE.get(trimmedType.toUpperCase());
        if (chineseType != null) {
            return chineseType;
        }
        
        // 如果已经是中文类型，直接返回
        if (TYPE_MAPPING.containsKey(trimmedType)) {
            // 统一转换为标准中文
            String englishType = TYPE_MAPPING.get(trimmedType);
            return TYPE_TO_CHINESE.getOrDefault(englishType, "其他");
        }
        
        // 未知类型默认为"其他"
        return "其他";
    }
    
    /**
     * 获取任务详情
     * 优先从 Redis 获取，Redis 没有则查询数据库
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    @Override
    public TaskDetailResponseDTO getTaskDetail(Long taskId) {
        log.info("获取任务详情，任务ID：{}", taskId);
        
        // 先从 Redis 获取
        TaskDetailResponseDTO taskDetail = getTaskDetailFromRedis(taskId);
        if (taskDetail != null) {
            log.info("从 Redis 获取任务详情成功");
            return taskDetail;
        }
        
        // Redis 没有，从数据库查询
        log.info("Redis 无缓存，从数据库查询");
        taskDetail = getTaskDetailFromDb(taskId);
        
        // 同步到 Redis
        if (taskDetail != null) {
            syncTaskDetailToRedis(taskDetail);
        }
        
        return taskDetail;
    }
    
    /**
     * 从 Redis 获取任务详情
     */
    private TaskDetailResponseDTO getTaskDetailFromRedis(Long taskId) {
        try {
            String key = TASK_ITEM_KEY_PREFIX + taskId;
            Object data = redisTemplate.opsForValue().get(key);
            if (data != null) {
                // 使用 ObjectMapper 进行类型安全转换
                return objectMapper.convertValue(data, TaskDetailResponseDTO.class);
            }
        } catch (Exception e) {
            log.error("从 Redis 获取任务详情失败", e);
        }
        return null;
    }
    
    /**
     * 从数据库获取任务详情
     */
    private TaskDetailResponseDTO getTaskDetailFromDb(Long taskId) {
        // 查询任务
        Task task = taskMapper.selectById(String.valueOf(taskId));
        if (task == null || task.getIsDeleted() == 1) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 查询发布者信息
        User publisher = userMapper.selectById(String.valueOf(task.getPublisherId()));
        if (publisher == null) {
            throw new BusinessException(404, "发布者不存在");
        }
        
        // 格式化日期和时间
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        String date = task.getServiceTime() != null ? task.getServiceTime().format(dateFormatter) : "";
        String timeRange = task.getServiceTime() != null ? task.getServiceTime().format(timeFormatter) : "";
        
        return TaskDetailResponseDTO.builder()
                .taskId(String.valueOf(task.getId()))
                .title(task.getTitle())
                .type(task.getType())
                .description(task.getDescription())
                .coins(task.getPrice())
                .location(task.getLocation())
                .date(date)
                .timeRange(timeRange)
                .status(task.getStatus())
                .publisherId(String.valueOf(task.getPublisherId()))
                .publisherName(publisher.getNickname())
                .publisherAvatar(publisher.getAvatar())
                .build();
    }
    
    /**
     * 同步任务详情到 Redis
     */
    private void syncTaskDetailToRedis(TaskDetailResponseDTO taskDetail) {
        try {
            String key = TASK_ITEM_KEY_PREFIX + taskDetail.getTaskId();
            redisTemplate.opsForValue().set(key, taskDetail, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
            log.info("任务详情已同步到 Redis，任务ID：{}", taskDetail.getTaskId());
        } catch (Exception e) {
            log.error("同步任务详情到 Redis 失败", e);
        }
    }
    
    /**
     * 获取任务分类列表
     *
     * @return 分类列表（不包含“全部”分类）
     */
    @Override
    public List<TaskCategoryDTO> getTaskCategories() {
        List<TaskCategoryDTO> categories = new ArrayList<>();
        
        // 陪聊
        categories.add(TaskCategoryDTO.builder()
                .key("CHAT")
                .label("陪聊")
                .icon("message-circle")
                .build());
        
        // 保洁
        categories.add(TaskCategoryDTO.builder()
                .key("CLEANING")
                .label("保洁")
                .icon("zap")
                .build());
        
        // 跑腿
        categories.add(TaskCategoryDTO.builder()
                .key("ERRAND")
                .label("跑腿")
                .icon("shopping-bag")
                .build());
        
        // 医疗陪护
        categories.add(TaskCategoryDTO.builder()
                .key("MEDICAL")
                .label("医疗陪护")
                .icon("stethoscope")
                .build());
        
        // 其他
        categories.add(TaskCategoryDTO.builder()
                .key("OTHER")
                .label("其他")
                .icon("briefcase")
                .build());
        
        return categories;
    }
    
    /**
     * 获取 Redis 数据状态（调试接口）
     *
     * @return Redis 数据状态信息
     */
    @Override
    public Object getRedisDataStatus() {
        Map<String, Object> status = new HashMap<>();
        
        try {
            // 检查各分类 ZSet 中的数据量
            Map<String, Long> categoryCount = new HashMap<>();
            for (String chineseType : TYPE_TO_CHINESE.values()) {
                String key = TASK_CATEGORY_KEY_PREFIX + chineseType;
                Long count = redisTemplate.opsForZSet().size(key);
                categoryCount.put(chineseType, count != null ? count : 0);
            }
            status.put("categoryCount", categoryCount);
            
            // 检查总列表数据
            Object hallData = redisTemplate.opsForValue().get(TASK_HALL_KEY);
            if (hallData instanceof List) {
                status.put("hallListCount", ((List<?>) hallData).size());
            } else {
                status.put("hallListCount", 0);
            }
            
            // 从数据库查询待接取任务总数
            LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(Task::getStatus, 0);
            Long dbCount = taskMapper.selectCount(queryWrapper);
            status.put("dbPendingTaskCount", dbCount);
            
            // 计算总任务数（所有分类 ZSet 中的任务总数）
            long totalRedisCount = categoryCount.values().stream()
                    .mapToLong(Long::longValue)
                    .sum();
            status.put("totalRedisTaskCount", totalRedisCount);
            
            // 对比结果
            status.put("dataConsistent", totalRedisCount == dbCount);
            
            log.info("Redis 数据状态：{}", status);
            return status;
            
        } catch (Exception e) {
            log.error("获取 Redis 数据状态失败", e);
            status.put("error", e.getMessage());
            return status;
        }
    }
    
    /**
     * 接取任务
     * 志愿者抢单，使用乐观锁确保任务未被他人接走
     * 任务状态从 0（待接取）变为 1（进行中）
     *
     * @param taskId      任务ID
     * @param volunteerId 志愿者ID
     * @return 接单是否成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean acceptTask(Long taskId, Long volunteerId) {
        log.info("接取任务，任务ID：{}，志愿者ID：{}", taskId, volunteerId);
        
        // 查询志愿者信息
        User volunteer = userMapper.selectById(String.valueOf(volunteerId));
        if (volunteer == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 使用乐观锁接取任务
        int affectedRows = taskMapper.acceptTaskWithOptimisticLock(taskId, volunteerId);
        
        if (affectedRows > 0) {
            log.info("抢单成功，任务ID：{}，志愿者ID：{}", taskId, volunteerId);
            
            // 从 Redis 缓存中移除该任务（因为已不是待接取状态）
            removeTaskFromRedisCache(taskId);
            
            return true;
        } else {
            log.warn("抢单失败，任务已被抢走或不存在，任务ID：{}", taskId);
            return false;
        }
    }
    
    /**
     * 从 Redis 缓存中移除任务
     * 当任务被接取后，需要从任务大厅缓存中移除
     *
     * @param taskId 任务ID
     */
    private void removeTaskFromRedisCache(Long taskId) {
        try {
            // 1. 查询任务信息，获取任务类型
            Task task = taskMapper.selectById(String.valueOf(taskId));
            if (task == null) {
                return;
            }
            
            String taskType = task.getType();
            
            // 2. 从分类 ZSet 中移除该任务
            if (taskType != null) {
                String categoryKey = TASK_CATEGORY_KEY_PREFIX + taskType;
                Set<Object> taskData = redisTemplate.opsForZSet().range(categoryKey, 0, -1);
                
                if (taskData != null) {
                    for (Object data : taskData) {
                        TaskHallItemDTO item = objectMapper.convertValue(data, TaskHallItemDTO.class);
                        if (item != null && String.valueOf(taskId).equals(item.getTaskId())) {
                            redisTemplate.opsForZSet().remove(categoryKey, data);
                            log.info("从分类 ZSet 中移除任务，分类：{}，任务ID：{}", taskType, taskId);
                            break;
                        }
                    }
                }
            }
            
            // 3. 删除单个任务缓存
            String itemKey = TASK_ITEM_KEY_PREFIX + taskId;
            redisTemplate.delete(itemKey);
            log.info("删除单个任务缓存，任务ID：{}", taskId);
            
            // 4. 更新总列表缓存（移除该任务）
            Object hallData = redisTemplate.opsForValue().get(TASK_HALL_KEY);
            if (hallData instanceof List) {
                List<TaskHallItemDTO> taskList = objectMapper.convertValue(hallData, 
                        new TypeReference<List<TaskHallItemDTO>>() {});
                List<TaskHallItemDTO> filteredList = taskList.stream()
                        .filter(t -> !String.valueOf(taskId).equals(t.getTaskId()))
                        .collect(Collectors.toList());
                redisTemplate.opsForValue().set(TASK_HALL_KEY, filteredList, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                log.info("更新总列表缓存，移除任务ID：{}", taskId);
            }
            
        } catch (Exception e) {
            log.error("从 Redis 缓存中移除任务失败，任务ID：{}", taskId, e);
            // 缓存操作失败不影响业务流程
        }
    }
    
    /**
     * 获取我的接单列表
     * 当前用户作为志愿者接取的任务列表
     *
     * @param userId 当前用户ID
     * @param status 任务状态筛选（可选，1:进行中 2:待确认 3:已完成）
     * @return 任务列表
     */
    @Override
    public List<MyAcceptedTaskDTO> getMyAcceptedTasks(Long userId, Integer status) {
        log.info("获取我的接单列表，用户ID：{}，状态筛选：{}", userId, status);
        
        // 构建查询条件
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getVolunteerId, userId)
                .orderByDesc(Task::getCreateTime);
        
        // 如果指定了状态，添加状态过滤
        if (status != null) {
            queryWrapper.eq(Task::getStatus, status);
        } else {
            // 不指定状态时，只返回 1:进行中、2:待确认、3:已完成
            queryWrapper.in(Task::getStatus, 1, 2, 3);
        }
        
        List<Task> tasks = taskMapper.selectList(queryWrapper);
        
        // 转换为 DTO
        List<MyAcceptedTaskDTO> result = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (Task task : tasks) {
            // 查询发布者信息
            User publisher = userMapper.selectById(String.valueOf(task.getPublisherId()));
            String publisherName = publisher != null ? publisher.getNickname() : "未知用户";
            String publisherAvatar = publisher != null ? publisher.getAvatar() : null;
            
            String date = task.getServiceTime() != null ? task.getServiceTime().format(dateFormatter) : "";
            String timeRange = task.getServiceTime() != null ? task.getServiceTime().format(timeFormatter) : "";
            
            MyAcceptedTaskDTO item = MyAcceptedTaskDTO.builder()
                    .taskId(String.valueOf(task.getId()))
                    .title(task.getTitle())
                    .type(task.getType())
                    .description(task.getDescription())
                    .coins(task.getPrice())
                    .location(task.getLocation())
                    .date(date)
                    .timeRange(timeRange)
                    .status(task.getStatus())
                    .publisherId(String.valueOf(task.getPublisherId()))
                    .publisherName(publisherName)
                    .publisherAvatar(publisherAvatar)
                    .build();
            result.add(item);
        }
        
        log.info("返回我的接单列表，数量：{}", result.size());
        return result;
    }
    
    /**
     * 获取我的发布列表
     * 当前用户作为发布者发布的任务列表
     *
     * @param userId 当前用户ID
     * @param status 任务状态筛选（可选，0:待接取 1:进行中 2:待确认 3:已完成）
     * @return 任务列表
     */
    @Override
    public List<MyPublishedTaskDTO> getMyPublishedTasks(Long userId, Integer status) {
        log.info("获取我的发布列表，用户ID：{}，状态筛选：{}", userId, status);
        
        // 构建查询条件
        LambdaQueryWrapper<Task> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Task::getPublisherId, userId)
                .orderByDesc(Task::getCreateTime);
        
        // 如果指定了状态，添加状态过滤
        if (status != null) {
            queryWrapper.eq(Task::getStatus, status);
        }
        
        List<Task> tasks = taskMapper.selectList(queryWrapper);
        
        // 转换为 DTO
        List<MyPublishedTaskDTO> result = new ArrayList<>();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        
        for (Task task : tasks) {
            // 查询志愿者信息（如果已被接单）
            String volunteerId = null;
            String volunteerName = null;
            String volunteerAvatar = null;
            
            if (task.getVolunteerId() != null) {
                User volunteer = userMapper.selectById(String.valueOf(task.getVolunteerId()));
                if (volunteer != null) {
                    volunteerId = String.valueOf(task.getVolunteerId());
                    volunteerName = volunteer.getNickname();
                    volunteerAvatar = volunteer.getAvatar();
                }
            }
            
            String date = task.getServiceTime() != null ? task.getServiceTime().format(dateFormatter) : "";
            String timeRange = task.getServiceTime() != null ? task.getServiceTime().format(timeFormatter) : "";
            
            MyPublishedTaskDTO item = MyPublishedTaskDTO.builder()
                    .taskId(String.valueOf(task.getId()))
                    .title(task.getTitle())
                    .type(task.getType())
                    .description(task.getDescription())
                    .coins(task.getPrice())
                    .location(task.getLocation())
                    .date(date)
                    .timeRange(timeRange)
                    .status(task.getStatus())
                    .volunteerId(volunteerId)
                    .volunteerName(volunteerName)
                    .volunteerAvatar(volunteerAvatar)
                    .build();
            result.add(item);
        }
        
        log.info("返回我的发布列表，数量：{}", result.size());
        return result;
    }
    
    /**
     * 取消并删除任务
     * 仅在待接单状态下可操作，任务变为已取消后10分钟后自动删除
     *
     * @param taskId      任务ID
     * @param publisherId 发布者ID
     * @return 是否取消成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean cancelAndDeleteTask(Long taskId, Long publisherId) {
        log.info("取消并删除任务，任务ID：{}，发布者ID：{}", taskId, publisherId);
        
        // 1. 查询任务
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 2. 校验是否为任务发布者
        if (!publisherId.equals(task.getPublisherId())) {
            throw new BusinessException(403, "无权操作此任务");
        }
        
        // 3. 校验任务状态（仅待接单状态可取消）
        if (!TaskStatusEnum.PENDING.getCode().equals(task.getStatus())) {
            throw new BusinessException(400, "仅待接单状态的任务可以取消");
        }
        
        // 4. 更新任务状态为已取消
        task.setStatus(TaskStatusEnum.CANCELLED.getCode());
        task.setUpdateTime(LocalDateTime.now());
        int affectedRows = taskMapper.updateById(task);
        
        if (affectedRows > 0) {
            log.info("任务已取消，任务ID：{}", taskId);
            
            // 5. 从 Redis 缓存中移除任务
            removeTaskFromRedisCache(taskId);
            
            // 6. 加入延迟删除队列（10分钟后自动删除）
            taskDelayDeleteScheduler.addToDelayDeleteQueue(taskId);
            
            return true;
        } else {
            log.error("取消任务失败，任务ID：{}", taskId);
            return false;
        }
    }
    
    /**
     * 提交服务凭证
     * 志愿者上传服务完成凭证照片，任务状态变为“待验收”
     *
     * @param taskId      任务ID
     * @param volunteerId 志愿者ID
     * @param imageUrl    凭证图片URL
     * @return 是否提交成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitEvidence(Long taskId, Long volunteerId, String imageUrl) {
        log.info("提交服务凭证，任务ID：{}，志愿者ID：{}", taskId, volunteerId);
        
        // 1. 查询任务
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 2. 校验是否为该任务的志愿者
        if (!volunteerId.equals(task.getVolunteerId())) {
            throw new BusinessException(403, "无权操作此任务");
        }
        
        // 3. 校验任务状态（仅进行中状态可提交凭证）
        if (!TaskStatusEnum.IN_PROGRESS.getCode().equals(task.getStatus())) {
            throw new BusinessException(400, "仅进行中的任务可以提交凭证");
        }
        
        // 4. 查询是否已有凭证记录（可能是签到时创建的）
        LambdaQueryWrapper<TaskEvidence> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskEvidence::getTaskId, taskId)
                .eq(TaskEvidence::getVolunteerId, volunteerId);
        TaskEvidence existingEvidence = taskEvidenceMapper.selectOne(queryWrapper);
        
        if (existingEvidence != null) {
            // 已有记录，更新图片URL
            existingEvidence.setImageUrl(imageUrl);
            existingEvidence.setUpdateTime(LocalDateTime.now());
            taskEvidenceMapper.updateById(existingEvidence);
            log.info("更新已有凭证记录，添加图片URL");
        } else {
            // 无记录，新建凭证记录
            TaskEvidence evidence = new TaskEvidence();
            evidence.setTaskId(taskId);
            evidence.setVolunteerId(volunteerId);
            evidence.setImageUrl(imageUrl);
            evidence.setCreateTime(LocalDateTime.now());
            evidence.setUpdateTime(LocalDateTime.now());
            taskEvidenceMapper.insert(evidence);
            log.info("创建新凭证记录");
        }
        
        // 5. 更新任务状态为待验收
        task.setStatus(TaskStatusEnum.WAITING_CONFIRM.getCode());
        task.setUpdateTime(LocalDateTime.now());
        int affectedRows = taskMapper.updateById(task);
        
        if (affectedRows > 0) {
            // 6. 清除Redis缓存，避免返回过期状态
            invalidateTaskCache(taskId);
            
            // 7. 创建任务验收消息，通知发布者
            User volunteer = userMapper.selectById(String.valueOf(volunteerId));
            String volunteerName = volunteer != null ? volunteer.getRealName() : "志愿者";
            
            String title = "任务待验收";
            String content = volunteerName + "已完成服务并提交凭证，请及时验收";
            
            messageService.createMessage(
                    task.getPublisherId(),
                    MessageTypeEnum.TASK_VERIFY.getCode(),
                    taskId,
                    title,
                    content
            );
            
            log.info("已创建任务验收消息，接收人：{}，任务ID：{}", task.getPublisherId(), taskId);
            log.info("服务凭证提交成功，任务ID：{}，状态变更为待验收", taskId);
            return true;
        } else {
            log.error("更新任务状态失败，任务ID：{}", taskId);
            return false;
        }
    }
    
    /**
     * 志愿者签到
     * 志愿者到达服务地点后进行签到，记录签到时间和信息
     *
     * @param taskId      任务ID
     * @param volunteerId 志愿者ID
     * @param checkInInfo 签到信息
     * @return 是否签到成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean checkIn(Long taskId, Long volunteerId, String checkInInfo) {
        log.info("志愿者签到，任务ID：{}，志愿者ID：{}", taskId, volunteerId);
        
        // 1. 查询任务
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 2. 校验是否为该任务的志愿者
        if (!volunteerId.equals(task.getVolunteerId())) {
            throw new BusinessException(403, "无权操作此任务");
        }
        
        // 3. 校验任务状态（仅进行中状态可签到）
        if (!TaskStatusEnum.IN_PROGRESS.getCode().equals(task.getStatus())) {
            throw new BusinessException(400, "仅进行中的任务可以签到");
        }
        
        // 4. 查询是否已有凭证记录
        LambdaQueryWrapper<TaskEvidence> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskEvidence::getTaskId, taskId)
                .eq(TaskEvidence::getVolunteerId, volunteerId);
        TaskEvidence existingEvidence = taskEvidenceMapper.selectOne(queryWrapper);
        
        if (existingEvidence != null) {
            // 已有记录，检查是否已签到
            if (existingEvidence.getCheckInTime() != null) {
                throw new BusinessException(400, "已经签到过，不能重复签到");
            }
            // 更新签到信息
            existingEvidence.setCheckInInfo(checkInInfo);
            existingEvidence.setCheckInTime(LocalDateTime.now());
            existingEvidence.setUpdateTime(LocalDateTime.now());
            int affectedRows = taskEvidenceMapper.updateById(existingEvidence);
            
            if (affectedRows > 0) {
                log.info("志愿者签到成功（更新已有记录），任务ID：{}", taskId);
                return true;
            }
            return false;
        } else {
            // 无记录，新建凭证记录（仅包含签到信息）
            TaskEvidence evidence = new TaskEvidence();
            evidence.setTaskId(taskId);
            evidence.setVolunteerId(volunteerId);
            evidence.setCheckInInfo(checkInInfo);
            evidence.setCheckInTime(LocalDateTime.now());
            evidence.setCreateTime(LocalDateTime.now());
            evidence.setUpdateTime(LocalDateTime.now());
            int affectedRows = taskEvidenceMapper.insert(evidence);
            
            if (affectedRows > 0) {
                log.info("志愿者签到成功（新建记录），任务ID：{}", taskId);
                return true;
            }
            return false;
        }
    }
    
    /**
     * 获取验收详情
     * 查询任务、志愿者信息、服务凭证
     *
     * @param taskId 任务ID
     * @return 验收详情
     */
    @Override
    public ReviewDetailDTO getReviewDetail(Long taskId) {
        log.info("获取验收详情，任务ID：{}", taskId);
        
        // 1. 查询任务
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 2. 查询志愿者信息
        User volunteer = null;
        if (task.getVolunteerId() != null) {
            volunteer = userMapper.selectById(String.valueOf(task.getVolunteerId()));
        }
        
        // 3. 查询服务凭证
        LambdaQueryWrapper<TaskEvidence> evidenceQuery = new LambdaQueryWrapper<>();
        evidenceQuery.eq(TaskEvidence::getTaskId, taskId)
                .eq(TaskEvidence::getIsDeleted, 0);
        List<TaskEvidence> evidences = taskEvidenceMapper.selectList(evidenceQuery);
        
        // 提取图片URL列表和签到时间
        List<String> photos = new ArrayList<>();
        String checkInTime = null;
        String finishTime = null;
        
        for (TaskEvidence evidence : evidences) {
            if (StringUtils.hasText(evidence.getImageUrl())) {
                photos.add(evidence.getImageUrl());
            }
            if (evidence.getCheckInTime() != null && checkInTime == null) {
                checkInTime = evidence.getCheckInTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
            if (evidence.getCreateTime() != null) {
                finishTime = evidence.getCreateTime().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        }
        
        return ReviewDetailDTO.builder()
                .taskId(String.valueOf(task.getId()))
                .title(task.getTitle())
                .description(task.getDescription())
                .coins(task.getPrice())
                .status(task.getStatus())
                .evidencePhotos(photos)
                .checkInTime(checkInTime)
                .finishTime(finishTime)
                .volunteerId(task.getVolunteerId() != null ? String.valueOf(task.getVolunteerId()) : null)
                .volunteerName(volunteer != null ? volunteer.getNickname() : null)
                .volunteerAvatar(volunteer != null ? volunteer.getAvatar() : null)
                .build();
    }
    
    /**
     * 确认验收
     * 1. 更新任务状态为已完成
     * 2. 扣除发布者冻结时间币
     * 3. 增加志愿者时间币
     * 4. 记录流水
     *
     * @param taskId      任务ID
     * @param publisherId 发布者ID
     * @param rating      评分
     * @param review      评价
     * @return 是否验收成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean confirmReview(Long taskId, Long publisherId, Integer rating, String review) {
        log.info("确认验收，任务ID：{}，发布者ID：{}，评分：{}", taskId, publisherId, rating);
        
        // 1. 查询任务
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 2. 校验是否为发布者
        if (!publisherId.equals(task.getPublisherId())) {
            throw new BusinessException(403, "无权操作此任务");
        }
        
        // 3. 校验任务状态（必须是待确认状态）
        if (!TaskStatusEnum.WAITING_CONFIRM.getCode().equals(task.getStatus())) {
            throw new BusinessException(400, "任务状态不正确，无法验收");
        }
        
        // 4. 查询发布者
        User publisher = userMapper.selectById(String.valueOf(publisherId));
        if (publisher == null) {
            throw new BusinessException(404, "发布者不存在");
        }
        
        // 5. 查询志愿者
        User volunteer = userMapper.selectById(String.valueOf(task.getVolunteerId()));
        if (volunteer == null) {
            throw new BusinessException(404, "志愿者不存在");
        }
        
        Integer coins = task.getPrice();
        
        // 6. 扣除发布者冻结时间币
        Integer frozenBalance = publisher.getFrozenBalance() != null ? publisher.getFrozenBalance() : 0;
        if (frozenBalance < coins) {
            throw new BusinessException(400, "冻结余额不足");
        }
        publisher.setFrozenBalance(frozenBalance - coins);
        publisher.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(publisher);
        log.info("扣除发布者冻结余额，用户ID：{}，扣除：{}", publisherId, coins);
        
        // 7. 增加志愿者时间币
        Integer volunteerBalance = volunteer.getBalance() != null ? volunteer.getBalance() : 0;
        volunteer.setBalance(volunteerBalance + coins);
        volunteer.setUpdateTime(LocalDateTime.now());
        userMapper.updateById(volunteer);
        log.info("增加志愿者余额，用户ID：{}，增加：{}", task.getVolunteerId(), coins);
        
        // 8. 记录发布者支出流水
        CoinLog publisherLog = new CoinLog();
        publisherLog.setUserId(publisherId);
        publisherLog.setAmount(-coins);  // 负数表示支出
        publisherLog.setType(2);  // 2:任务支出
        publisherLog.setTaskId(taskId);
        publisherLog.setCreateTime(LocalDateTime.now());
        publisherLog.setUpdateTime(LocalDateTime.now());
        coinLogMapper.insert(publisherLog);
        
        // 9. 记录志愿者收入流水
        CoinLog volunteerLog = new CoinLog();
        volunteerLog.setUserId(task.getVolunteerId());
        volunteerLog.setAmount(coins);  // 正数表示收入
        volunteerLog.setType(1);  // 1:任务收入
        volunteerLog.setTaskId(taskId);
        volunteerLog.setCreateTime(LocalDateTime.now());
        volunteerLog.setUpdateTime(LocalDateTime.now());
        coinLogMapper.insert(volunteerLog);
        
        // 10. 更新任务状态为已完成
        task.setStatus(TaskStatusEnum.COMPLETED.getCode());
        task.setUpdateTime(LocalDateTime.now());
        int affectedRows = taskMapper.updateById(task);
        
        if (affectedRows > 0) {
            // 11. 清除Redis缓存，避免返回过期状态
            invalidateTaskCache(taskId);
            
            // 12. 删除对应的任务验收消息
            messageService.deleteMessageByBizId(taskId, MessageTypeEnum.TASK_VERIFY.getCode());
            log.info("已删除任务验收消息，任务ID：{}", taskId);
            
            log.info("任务验收成功，任务ID：{}，时间币已转账：{}", taskId, coins);
            return true;
        }
        return false;
    }
    
    /**
     * 失效任务缓存
     * 在任务状态变更后调用，清除相关的Redis缓存
     *
     * @param taskId 任务ID
     */
    private void invalidateTaskCache(Long taskId) {
        try {
            // 删除单个任务详情缓存
            String itemKey = TASK_ITEM_KEY_PREFIX + taskId;
            redisTemplate.delete(itemKey);
            log.info("已清除任务缓存，任务ID：{}", taskId);
        } catch (Exception e) {
            log.error("清除任务缓存失败，任务ID：{}", taskId, e);
            // 缓存操作失败不影响业务流程
        }
    }
    
    /**
     * 提交志愿者评价
     * 发布者对完成任务的志愿者提交评价
     *
     * @param taskId      任务ID
     * @param publisherId 发布者ID
     * @param volunteerId 志愿者ID
     * @param rating      评分
     * @param content     评价内容
     * @return 是否提交成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitReview(Long taskId, Long publisherId, Long volunteerId, Integer rating, String content) {
        log.info("提交志愿者评价，任务ID：{}，发布者ID：{}，志愿者ID：{}，评分：{}",
                taskId, publisherId, volunteerId, rating);
        
        // 1. 查询任务
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 2. 校验是否为发布者
        if (!publisherId.equals(task.getPublisherId())) {
            throw new BusinessException(403, "无权操作此任务");
        }
        
        // 3. 校验任务状态（必须是已完成状态）
        if (!TaskStatusEnum.COMPLETED.getCode().equals(task.getStatus())) {
            throw new BusinessException(400, "仅已完成的任务可以评价");
        }
        
        // 4. 校验志愿者ID是否匹配
        if (!volunteerId.equals(task.getVolunteerId())) {
            throw new BusinessException(400, "志愿者ID不匹配");
        }
        
        // 5. 检查是否已评价（一个任务只能评价一次）
        LambdaQueryWrapper<TaskReview> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(TaskReview::getTaskId, taskId);
        TaskReview existingReview = taskReviewMapper.selectOne(queryWrapper);
        
        if (existingReview != null) {
            throw new BusinessException(400, "该任务已评价，不能重复评价");
        }
        
        // 6. 插入评价记录
        TaskReview review = new TaskReview();
        review.setTaskId(taskId);
        review.setPublisherId(publisherId);
        review.setVolunteerId(volunteerId);
        review.setRating(rating);
        review.setContent(content);
        review.setCreateTime(LocalDateTime.now());
        review.setUpdateTime(LocalDateTime.now());
        
        int affectedRows = taskReviewMapper.insert(review);
        
        if (affectedRows > 0) {
            log.info("志愿者评价提交成功，任务ID：{}，评分：{}", taskId, rating);
            return true;
        }
        return false;
    }
    
    /**
     * 提交申诉
     * 当任务出现争议时，发布者或志愿者可以提交申诉
     *
     * @param taskId 任务ID
     * @param userId 申诉发起人 ID
     * @param reason 申诉理由
     * @return 是否提交成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean submitAppeal(Long taskId, Long userId, String reason) {
        log.info("提交申诉，任务ID：{}，用户ID：{}", taskId, userId);
        
        // 1. 查询任务
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 2. 校验用户是否为任务参与者（发布者或志愿者）
        if (!userId.equals(task.getPublisherId()) && !userId.equals(task.getVolunteerId())) {
            throw new BusinessException(403, "无权对此任务提交申诉");
        }
        
        // 3. 校验任务状态（只有进行中、待验收、已完成的任务可以申诉）
        Integer status = task.getStatus();
        if (!TaskStatusEnum.IN_PROGRESS.getCode().equals(status) 
                && !TaskStatusEnum.WAITING_CONFIRM.getCode().equals(status)
                && !TaskStatusEnum.COMPLETED.getCode().equals(status)) {
            throw new BusinessException(400, "当前任务状态不允许申诉");
        }
        
        // 4. 检查是否已经存在申诉（一个任务只能申诉一次）
        LambdaQueryWrapper<Appeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Appeal::getTaskId, taskId)
                    .eq(Appeal::getStatus, 0);  // status=0 表示待处理
        Appeal existingAppeal = appealMapper.selectOne(queryWrapper);
        
        if (existingAppeal != null) {
            throw new BusinessException(400, "该任务已存在待处理的申诉，无法重复申诉");
        }
        
        // 5. 插入申诉记录
        Appeal appeal = new Appeal();
        appeal.setTaskId(taskId);
        appeal.setProposerId(userId);
        appeal.setReason(reason);
        appeal.setStatus(0);  // 0:待处理
        appeal.setCreateTime(LocalDateTime.now());
        appeal.setUpdateTime(LocalDateTime.now());
        
        int appealRows = appealMapper.insert(appeal);
        
        if (appealRows <= 0) {
            throw new BusinessException(500, "申诉提交失败");
        }
        
        // 6. 更新任务状态为申诉中 (status=4)
        task.setStatus(4);
        task.setUpdateTime(LocalDateTime.now());
        int taskRows = taskMapper.updateById(task);
        
        if (taskRows > 0) {
            // 7. 清除Redis缓存，避免返回过期状态
            invalidateTaskCache(taskId);
            
            log.info("申诉提交成功，任务ID：{}，申诉ID：{}", taskId, appeal.getId());
            return true;
        }
        return false;
    }
    
    /**
     * 提交申诉回应
     * 被申诉方针对申诉内容提交回应
     *
     * @param taskId  任务ID
     * @param userId  回应人ID
     * @param content 回应内容
     * @return 是否提交成功
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean replyAppeal(Long taskId, Long userId, String content) {
        log.info("提交申诉回应，任务ID：{}，用户ID：{}", taskId, userId);
        
        // 1. 查询任务
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 2. 校验任务状态（必须是申诉中状态 status=4）
        if (!Integer.valueOf(4).equals(task.getStatus())) {
            throw new BusinessException(400, "当前任务不在申诉中状态");
        }
        
        // 3. 查询申诉记录
        LambdaQueryWrapper<Appeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Appeal::getTaskId, taskId)
                    .eq(Appeal::getStatus, 0);  // status=0 表示待处理
        Appeal appeal = appealMapper.selectOne(queryWrapper);
        
        if (appeal == null) {
            throw new BusinessException(404, "申诉记录不存在");
        }
        
        // 4. 校验回应人是否为被申诉方（不能是申诉发起人）
        if (userId.equals(appeal.getProposerId())) {
            throw new BusinessException(400, "申诉发起人不能回应自己的申诉");
        }
        
        // 5. 校验回应人是否为任务参与者（发布者或志愿者）
        if (!userId.equals(task.getPublisherId()) && !userId.equals(task.getVolunteerId())) {
            throw new BusinessException(403, "无权回应此申诉");
        }
        
        // 6. 检查是否已经回应过
        if (appeal.getDefendantResponse() != null && !appeal.getDefendantResponse().isEmpty()) {
            throw new BusinessException(400, "已经回应过，不能重复回应");
        }
        
        // 7. 更新申诉记录
        appeal.setDefendantResponse(content);
        appeal.setResponseTime(LocalDateTime.now());
        appeal.setUpdateTime(LocalDateTime.now());
        
        int affectedRows = appealMapper.updateById(appeal);
        
        if (affectedRows > 0) {
            log.info("申诉回应提交成功，任务ID：{}，申诉ID：{}", taskId, appeal.getId());
            return true;
        }
        return false;
    }
    
    /**
     * 获取申诉详情
     * 根据任务ID获取关联的申诉信息，包括申诉发起人信息
     *
     * @param taskId 任务ID
     * @return 申诉详情
     */
    @Override
    public AppealDetailDTO getAppealDetail(Long taskId) {
        log.info("获取申诉详情，任务ID：{}", taskId);
        
        // 1. 查询任务是否存在
        Task task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new BusinessException(404, "任务不存在");
        }
        
        // 2. 查询任务关联的申诉记录
        LambdaQueryWrapper<Appeal> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Appeal::getTaskId, taskId)
                    .orderByDesc(Appeal::getCreateTime)
                    .last("LIMIT 1");  // 获取最新一条
        Appeal appeal = appealMapper.selectOne(queryWrapper);
        
        if (appeal == null) {
            throw new BusinessException(404, "该任务没有申诉记录");
        }
        
        // 3. 查询申诉发起人信息
        User proposer = userMapper.selectById(String.valueOf(appeal.getProposerId()));
        String proposerName = proposer != null ? proposer.getNickname() : "未知用户";
        String proposerAvatar = proposer != null ? proposer.getAvatar() : null;
        
        // 4. 查询被申诉方信息（任务的另一方参与者）
        Long defendantId = null;
        if (appeal.getProposerId().equals(task.getPublisherId())) {
            // 发起人是发布者，被申诉方是志愿者
            defendantId = task.getVolunteerId();
        } else if (appeal.getProposerId().equals(task.getVolunteerId())) {
            // 发起人是志愿者，被申诉方是发布者
            defendantId = task.getPublisherId();
        }
        
        String defendantName = null;
        String defendantAvatar = null;
        if (defendantId != null) {
            User defendant = userMapper.selectById(String.valueOf(defendantId));
            if (defendant != null) {
                defendantName = defendant.getNickname();
                defendantAvatar = defendant.getAvatar();
            }
        }
        
        // 5. 格式化时间
        DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String createTimeStr = appeal.getCreateTime() != null 
                ? appeal.getCreateTime().format(formatter) 
                : null;
        String responseTimeStr = appeal.getResponseTime() != null
                ? appeal.getResponseTime().format(formatter)
                : null;
        
        // 6. 构建响应DTO
        return AppealDetailDTO.builder()
                .id(String.valueOf(appeal.getId()))
                .taskId(String.valueOf(appeal.getTaskId()))
                .proposerId(String.valueOf(appeal.getProposerId()))
                .proposerName(proposerName)
                .proposerAvatar(proposerAvatar)
                .reason(appeal.getReason())
                .defendantName(defendantName)
                .defendantAvatar(defendantAvatar)
                .defendantResponse(appeal.getDefendantResponse())
                .responseTime(responseTimeStr)
                .status(appeal.getStatus())
                .createTime(createTimeStr)
                .handlingResult(appeal.getHandlingResult())
                .handlingReason(appeal.getHandlingReason())
                .build();
    }
}
