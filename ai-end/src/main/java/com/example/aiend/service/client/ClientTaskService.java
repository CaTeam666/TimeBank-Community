package com.example.aiend.service.client;

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

import java.util.List;

/**
 * 用户端任务服务接口
 * 处理任务发布、任务大厅查询等业务
 *
 * @author AI-End
 * @since 2025-12-29
 */
public interface ClientTaskService {
    
    /**
     * 发布任务
     * 将任务保存到数据库并同步到 Redis 缓存
     *
     * @param publishDTO 任务发布请求
     * @param publisherId 发布者ID
     * @return 发布响应（包含任务ID）
     */
    TaskPublishResponseDTO publishTask(TaskPublishDTO publishDTO, Long publisherId, Long proxyUserId);
    
    /**
     * 获取任务大厅列表
     * 优先从 Redis 获取，Redis 没有则查询数据库
     *
     * @param type 任务类型过滤（可选）
     * @return 任务列表
     */
    List<TaskHallItemDTO> getTaskHallList(String type);
    
    /**
     * 获取用户余额
     *
     * @param userId 用户ID
     * @return 用户余额
     */
    UserBalanceResponseDTO getUserBalance(Long userId);
    
    /**
     * 获取任务详情
     * 优先从 Redis 获取，Redis 没有则查询数据库
     *
     * @param taskId 任务ID
     * @return 任务详情
     */
    TaskDetailResponseDTO getTaskDetail(Long taskId);
    
    /**
     * 获取任务分类列表
     *
     * @return 分类列表
     */
    List<TaskCategoryDTO> getTaskCategories();
    
    /**
     * 获取 Redis 数据状态（调试接口）
     *
     * @return Redis 数据状态信息
     */
    Object getRedisDataStatus();
    
    /**
     * 接取任务
     * 志愿者抢单，任务状态从 0（待接取）变为 1（进行中）
     *
     * @param taskId      任务ID
     * @param volunteerId 志愿者ID
     * @return 接单是否成功
     */
    boolean acceptTask(Long taskId, Long volunteerId);
    
    /**
     * 获取我的接单列表
     * 当前用户作为志愿者接取的任务列表
     *
     * @param userId 当前用户ID
     * @param status 任务状态筛选（可选，1:进行中 2:待确认 3:已完成）
     * @return 任务列表
     */
    List<MyAcceptedTaskDTO> getMyAcceptedTasks(Long userId, Integer status);
    
    /**
     * 获取我的发布列表
     * 当前用户作为发布者发布的任务列表
     *
     * @param userId 当前用户ID
     * @param status 任务状态筛选（可选，0:待接取 1:进行中 2:待确认 3:已完成）
     * @return 任务列表
     */
    List<MyPublishedTaskDTO> getMyPublishedTasks(Long userId, Integer status);
    
    /**
     * 取消并删除任务
     * 仅在待接单状态下可操作，任务变为已取消后10分钟后自动删除
     *
     * @param taskId      任务ID
     * @param publisherId 发布者ID
     * @return 是否取消成功
     */
    boolean cancelAndDeleteTask(Long taskId, Long publisherId);
    
    /**
     * 提交服务凭证
     * 志愿者上传服务完成凭证照片，任务状态变为“待验收”
     *
     * @param taskId      任务ID
     * @param volunteerId 志愿者ID
     * @param imageUrl    凭证图片URL
     * @return 是否提交成功
     */
    boolean submitEvidence(Long taskId, Long volunteerId, String imageUrl);
    
    /**
     * 志愿者签到
     * 志愿者到达服务地点后进行签到，记录签到时间和信息
     *
     * @param taskId      任务ID
     * @param volunteerId 志愿者ID
     * @param checkInInfo 签到信息
     * @return 是否签到成功
     */
    boolean checkIn(Long taskId, Long volunteerId, String checkInInfo);
    
    /**
     * 获取验收详情
     * 获取待验收任务的详细信息，包括服务凭证、签到时间、完成时间、志愿者信息
     *
     * @param taskId 任务ID
     * @return 验收详情
     */
    ReviewDetailDTO getReviewDetail(Long taskId);
    
    /**
     * 确认验收
     * 发布者确认验收任务，将任务状态变更为“已完成”，并完成时间币结算
     * 1. 更新任务状态为已完成
     * 2. 扣除发布者冻结时间币
     * 3. 增加志愿者时间币
     * 4. 记录流水
     *
     * @param taskId      任务ID
     * @param publisherId 发布者ID
     * @param rating      评分 (1-5)
     * @param review      评价内容（可选）
     * @return 是否验收成功
     */
    boolean confirmReview(Long taskId, Long publisherId, Integer rating, String review);
    
    /**
     * 提交志愿者评价
     * 发布者对完成任务的志愿者提交评价
     *
     * @param taskId       任务ID
     * @param publisherId  发布者ID（评价人）
     * @param volunteerId  志愿者ID（被评价人）
     * @param rating       评分 (1-5)
     * @param content      评价内容
     * @return 是否提交成功
     */
    boolean submitReview(Long taskId, Long publisherId, Long volunteerId, Integer rating, String content);
    
    /**
     * 提交申诉
     * 当任务出现争议时，发布者或志愿者可以提交申诉
     *
     * @param taskId    任务ID
     * @param userId    申诉发起人 ID
     * @param reason    申诉理由
     * @param evidenceImg 申诉证据图片URL
     * @return 是否提交成功
     */
    boolean submitAppeal(Long taskId, Long userId, String reason, String evidenceImg);
    
    /**
     * 提交申诉回应
     * 被申诉方针对申诉内容提交回应
     *
     * @param taskId   任务ID
     * @param userId   回应人ID
     * @param content  回应内容
     * @param evidenceImg 回应证据图片URL
     * @return 是否提交成功
     */
    boolean replyAppeal(Long taskId, Long userId, String content, String evidenceImg);
    
    /**
     * 获取申诉详情
     * 根据任务ID获取关联的申诉信息，包括申诉发起人信息
     *
     * @param taskId 任务ID
     * @return 申诉详情
     */
    AppealDetailDTO getAppealDetail(Long taskId);
}
