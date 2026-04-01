package com.example.aiend.controller.client;

import com.example.aiend.common.Result;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.config.interceptor.ProxyAuthInterceptor;
import com.example.aiend.dto.request.client.TaskAcceptDTO;
import com.example.aiend.dto.request.client.TaskDeleteDTO;
import com.example.aiend.dto.request.client.EvidenceSubmitDTO;
import com.example.aiend.dto.request.client.CheckInDTO;
import com.example.aiend.dto.request.client.TaskPublishDTO;
import com.example.aiend.dto.request.client.ReviewConfirmDTO;
import com.example.aiend.dto.request.client.ReviewSubmitDTO;
import com.example.aiend.dto.request.client.AppealSubmitDTO;
import com.example.aiend.dto.request.client.AppealReplyDTO;
import com.example.aiend.dto.response.client.MyAcceptedTaskDTO;
import com.example.aiend.dto.response.client.AppealDetailDTO;
import com.example.aiend.dto.response.client.MyPublishedTaskDTO;
import com.example.aiend.dto.response.client.TaskCategoryDTO;
import com.example.aiend.dto.response.client.TaskDetailResponseDTO;
import com.example.aiend.dto.response.client.TaskHallItemDTO;
import com.example.aiend.dto.response.client.TaskPublishResponseDTO;
import com.example.aiend.dto.response.client.UserBalanceResponseDTO;
import com.example.aiend.dto.response.client.FileUploadResponseDTO;
import com.example.aiend.dto.response.client.ReviewDetailDTO;
import com.example.aiend.dto.response.client.UserInfoDTO;
import com.example.aiend.service.client.ClientTaskService;
import com.example.aiend.service.client.ClientAuthService;
import com.example.aiend.service.client.FileUploadService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 用户端任务控制器
 * 处理任务发布、任务大厅查询、用户余额等请求
 *
 * @author AI-End
 * @since 2025-12-29
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@Validated
public class ClientTaskController {
    
    private final ClientTaskService clientTaskService;
    private final ClientAuthService clientAuthService;
    private final FileUploadService fileUploadService;
    
    /**
     * 发布任务
     * 支持代理模式：代理模式下自动使用被代理人ID作为发布者
     *
     * @param publishDTO 任务发布请求
     * @param headerUserId 用户ID（从请求头获取，作为备选）
     * @param request HTTP请求对象（用于获取代理模式信息）
     * @return 发布响应（包含任务ID）
     * @throws BusinessException 当无法获取发布者ID时抛出
     */
    @PostMapping("/task/publish")
    public Result<TaskPublishResponseDTO> publishTask(
            @Valid @RequestBody TaskPublishDTO publishDTO,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            HttpServletRequest request) {
        
        Long publisherId;
        Long proxyUserId = null;
        
        // 优先检查代理模式
        Boolean isProxyMode = (Boolean) request.getAttribute(ProxyAuthInterceptor.ATTR_IS_PROXY_MODE);
        if (Boolean.TRUE.equals(isProxyMode)) {
            // 代理模式：使用被代理人ID（老人ID）作为发布者，记录代理人ID
            publisherId = (Long) request.getAttribute(ProxyAuthInterceptor.ATTR_PROXY_USER_ID);
            proxyUserId = (Long) request.getAttribute(ProxyAuthInterceptor.ATTR_PROXY_REAL_USER_ID);
            log.info("代理模式发布任务，被代理人ID：{}，实际操作人ID：{}", publisherId, proxyUserId);
        } else {
            // 非代理模式：优先使用请求体中的 publisherId，如果没有则使用请求头中的用户ID
            publisherId = publishDTO.getPublisherId();
            if (publisherId == null) {
                publisherId = headerUserId;
            }
        }
        
        // 如果都没有，抛出业务异常
        if (publisherId == null) {
            throw new BusinessException("无法获取发布者信息，请提供 publisherId 或在请求头中传入 X-User-Id");
        }
        
        log.info("任务发布请求，发布者ID：{}，任务标题：{}", publisherId, publishDTO.getTitle());
        
        TaskPublishResponseDTO response = clientTaskService.publishTask(publishDTO, publisherId, proxyUserId);
        return Result.success(response, "发布成功");
    }
    
    /**
     * 获取任务大厅列表
     * 优先从 Redis 获取，Redis 没有则查询数据库
     *
     * @param type 任务类型过滤（可选）
     * @return 任务列表
     */
    @GetMapping("/task/hall")
    public Result<List<TaskHallItemDTO>> getTaskHallList(
            @RequestParam(required = false) String type) {
        log.info("获取任务大厅列表请求，类型过滤：{}", type);
        List<TaskHallItemDTO> taskList = clientTaskService.getTaskHallList(type);
        return Result.success(taskList, "查询成功");
    }
    
    /**
     * 获取用户余额
     * 支持代理模式：代理模式下自动查询被代理人的余额
     *
     * @param userId 用户ID（Query参数，如果不传则自动获取）
     * @param request HTTP请求对象（用于获取代理模式信息）
     * @return 用户余额
     * @throws BusinessException 当无法获取用户ID时抛出
     */
    @GetMapping("/user/balance")
    public Result<UserBalanceResponseDTO> getUserBalance(
            @RequestParam(required = false) Long userId,
            HttpServletRequest request) {
        
        // 优先检查代理模式
        Boolean isProxyMode = (Boolean) request.getAttribute(ProxyAuthInterceptor.ATTR_IS_PROXY_MODE);
        if (Boolean.TRUE.equals(isProxyMode)) {
            // 代理模式：使用被代理人ID（老人ID）
            userId = (Long) request.getAttribute(ProxyAuthInterceptor.ATTR_PROXY_USER_ID);
            log.info("代理模式查询余额，被代理人ID：{}", userId);
        }
        
        // 如果没有传用户ID，抛出业务异常
        if (userId == null) {
            throw new BusinessException("无法获取当前用户信息，请提供 userId 参数或通过Token认证");
        }
        
        log.info("获取用户余额请求，用户ID：{}", userId);
        
        UserBalanceResponseDTO response = clientTaskService.getUserBalance(userId);
        return Result.success(response, "查询成功");
    }
    
    /**
     * 获取任务详情
     * 优先从 Redis 获取，Redis 没有则查询数据库
     *
     * @param taskId 任务ID
     * @return 任务详情
     * @throws BusinessException 当任务不存在时抛出
     */
    @GetMapping("/task/detail")
    public Result<TaskDetailResponseDTO> getTaskDetail(
            @RequestParam Long taskId) {
        log.info("获取任务详情请求，任务ID：{}", taskId);
        
        TaskDetailResponseDTO response = clientTaskService.getTaskDetail(taskId);
        return Result.success(response, "查询成功");
    }
    
    /**
     * 获取任务分类列表
     *
     * @return 分类列表
     */
    @GetMapping("/task/categories")
    public Result<List<TaskCategoryDTO>> getTaskCategories() {
        log.info("获取任务分类列表请求");
        
        List<TaskCategoryDTO> categories = clientTaskService.getTaskCategories();
        return Result.success(categories, "查询成功");
    }
    
    /**
     * 测试接口：检查 Redis 数据状态
     *
     * @return Redis 数据状态信息
     */
    @GetMapping("/task/debug/redis-status")
    public Result<Object> checkRedisStatus() {
        log.info("检查 Redis 数据状态");
        
        Object status = clientTaskService.getRedisDataStatus();
        return Result.success(status, "查询成功");
    }
    
    /**
     * 抢单/接取任务
     * 志愿者接取处于“待接取”状态的任务
     * 接取成功后，任务状态变更为“进行中”
     *
     * @param acceptDTO 接单请求参数
     * @return 是否抢单成功
     */
    @PostMapping("/task/accept")
    public Result<Boolean> acceptTask(@Valid @RequestBody TaskAcceptDTO acceptDTO) {
        log.info("接取任务请求，任务ID：{}，志愿者ID：{}", acceptDTO.getTaskId(), acceptDTO.getUserId());
        
        Long taskId = Long.parseLong(acceptDTO.getTaskId());
        Long volunteerId = Long.parseLong(acceptDTO.getUserId());
        
        boolean success = clientTaskService.acceptTask(taskId, volunteerId);
        
        if (success) {
            return Result.success(true, "抢单成功");
        } else {
            return Result.error(500, "手慢了，任务已被抢走", false);
        }
    }
    
    /**
     * 获取我的接单列表
     * 当前用户作为志愿者接取的任务列表
     * 支持代理模式：代理模式下自动查询被代理人的接单列表
     *
     * @param userId 当前用户ID
     * @param status 任务状态筛选（可选）
     * @param request HTTP请求对象（用于获取代理模式信息）
     * @return 任务列表
     */
    @GetMapping("/task/my/accepted")
    public Result<List<MyAcceptedTaskDTO>> getMyAcceptedTasks(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        
        Long userIdLong;
        
        // 优先检查代理模式
        Boolean isProxyMode = (Boolean) request.getAttribute(ProxyAuthInterceptor.ATTR_IS_PROXY_MODE);
        if (Boolean.TRUE.equals(isProxyMode)) {
            // 代理模式：使用被代理人ID（老人ID）
            userIdLong = (Long) request.getAttribute(ProxyAuthInterceptor.ATTR_PROXY_USER_ID);
            log.info("代理模式查询接单列表，被代理人ID：{}", userIdLong);
        } else {
            // 非代理模式：使用请求参数中的用户ID
            if (userId == null || userId.isEmpty()) {
                throw new BusinessException("用户ID不能为空");
            }
            userIdLong = Long.parseLong(userId);
        }
        
        log.info("获取我的接单列表请求，用户ID：{}，状态筛选：{}", userIdLong, status);
        
        List<MyAcceptedTaskDTO> taskList = clientTaskService.getMyAcceptedTasks(userIdLong, status);
        return Result.success(taskList, "查询成功");
    }
    
    /**
     * 获取我的发布列表
     * 当前用户作为发布者发布的任务列表
     * 支持代理模式：代理模式下自动查询被代理人的发布列表
     *
     * @param userId 当前用户ID
     * @param status 任务状态筛选（可选）
     * @param request HTTP请求对象（用于获取代理模式信息）
     * @return 任务列表
     */
    @GetMapping("/task/my/published")
    public Result<List<MyPublishedTaskDTO>> getMyPublishedTasks(
            @RequestParam(required = false) String userId,
            @RequestParam(required = false) Integer status,
            HttpServletRequest request) {
        
        Long userIdLong;
        
        // 优先检查代理模式
        Boolean isProxyMode = (Boolean) request.getAttribute(ProxyAuthInterceptor.ATTR_IS_PROXY_MODE);
        if (Boolean.TRUE.equals(isProxyMode)) {
            // 代理模式：使用被代理人ID（老人ID）
            userIdLong = (Long) request.getAttribute(ProxyAuthInterceptor.ATTR_PROXY_USER_ID);
            log.info("代理模式查询发布列表，被代理人ID：{}", userIdLong);
        } else {
            // 非代理模式：使用请求参数中的用户ID
            if (userId == null || userId.isEmpty()) {
                throw new BusinessException("用户ID不能为空");
            }
            userIdLong = Long.parseLong(userId);
        }
        
        log.info("获取我的发布列表请求，用户ID：{}，状态筛选：{}", userIdLong, status);
        
        List<MyPublishedTaskDTO> taskList = clientTaskService.getMyPublishedTasks(userIdLong, status);
        return Result.success(taskList, "查询成功");
    }
    
    /**
     * 取消/删除任务
     * 发布者取消并删除已发布的任务（仅在待接单状态下可操作）
     * 任务变为已取消后10分钟后自动删除
     *
     * @param deleteDTO 删除请求参数
     * @return 是否删除成功
     */
    @PostMapping("/task/cancel")
    public Result<Boolean> deleteTask(@Valid @RequestBody TaskDeleteDTO deleteDTO) {
        log.info("取消/删除任务请求，任务ID：{}，用户ID：{}", deleteDTO.getTaskId(), deleteDTO.getUserId());
        
        Long taskId = Long.parseLong(deleteDTO.getTaskId());
        Long publisherId = Long.parseLong(deleteDTO.getUserId());
        
        boolean success = clientTaskService.cancelAndDeleteTask(taskId, publisherId);
        
        if (success) {
            return Result.success(true, "删除成功");
        } else {
            return Result.error(500, "删除失败", false);
        }
    }
    
    /**
     * 提交服务凭证
     * 志愿者上传服务完成凭证照片，任务状态变为“待验收”
     *
     * @param submitDTO 提交请求参数
     * @return 是否提交成功
     */
    @PostMapping("/task/evidence/submit")
    public Result<Boolean> submitEvidence(@Valid @RequestBody EvidenceSubmitDTO submitDTO) {
        log.info("提交服务凭证请求，任务ID：{}，志愿者ID：{}", submitDTO.getTaskId(), submitDTO.getUserId());
        
        Long taskId = Long.parseLong(submitDTO.getTaskId());
        Long volunteerId = Long.parseLong(submitDTO.getUserId());
        
        boolean success = clientTaskService.submitEvidence(taskId, volunteerId, submitDTO.getImageUrl());
        
        if (success) {
            return Result.success(true, "提交成功");
        } else {
            return Result.error(500, "提交失败", false);
        }
    }
    
    /**
     * 志愿者签到
     * 志愿者到达服务地点后进行签到，记录签到时间和信息
     *
     * @param checkInDTO 签到请求参数
     * @return 是否签到成功
     */
    @PostMapping("/task/checkin")
    public Result<Boolean> checkIn(@Valid @RequestBody CheckInDTO checkInDTO) {
        log.info("志愿者签到请求，任务ID：{}，志愿者ID：{}", checkInDTO.getTaskId(), checkInDTO.getUserId());
        
        Long taskId = Long.parseLong(checkInDTO.getTaskId());
        Long volunteerId = Long.parseLong(checkInDTO.getUserId());
        
        boolean success = clientTaskService.checkIn(taskId, volunteerId, checkInDTO.getCheckInInfo());
        
        if (success) {
            return Result.success(true, "签到成功");
        } else {
            return Result.error(500, "签到失败", false);
        }
    }
    
    /**
     * 文件上传
     * 用于上传服务凭证等图片文件
     *
     * @param file 文件对象
     * @return 文件访问URL
     */
    @PostMapping("/file/upload")
    public Result<FileUploadResponseDTO> uploadFile(@RequestParam("file") MultipartFile file) {
        log.info("文件上传请求，文件名：{}", file.getOriginalFilename());
        FileUploadResponseDTO response = fileUploadService.uploadFile(file);
        return Result.success(response);
    }
    
    /**
     * 获取验收详情
     * 获取待验收任务的详细信息，包括服务凭证、签到时间、完成时间、志愿者信息
     *
     * @param taskId 任务ID
     * @return 验收详情
     */
    @GetMapping("/task/review/detail")
    public Result<ReviewDetailDTO> getReviewDetail(@RequestParam("taskId") String taskId) {
        log.info("获取验收详情请求，任务ID：{}", taskId);
        
        Long taskIdLong = Long.parseLong(taskId);
        ReviewDetailDTO detail = clientTaskService.getReviewDetail(taskIdLong);
        return Result.success(detail);
    }
    
    /**
     * 确认验收
     * 发布者确认验收任务，将任务状态变更为“已完成”，并完成时间币结算
     *
     * @param confirmDTO 确认验收请求参数
     * @return 是否验收成功
     */
    @PostMapping("/task/review/confirm")
    public Result<Boolean> confirmReview(
            @Valid @RequestBody ReviewConfirmDTO confirmDTO,
            HttpServletRequest request) {
        
        Long publisherId;
        
        // 代理模式：代理人可验收代为发布的任务
        Boolean isProxyMode = (Boolean) request.getAttribute(ProxyAuthInterceptor.ATTR_IS_PROXY_MODE);
        if (Boolean.TRUE.equals(isProxyMode)) {
            publisherId = (Long) request.getAttribute(ProxyAuthInterceptor.ATTR_PROXY_USER_ID);
            Long realUserId = (Long) request.getAttribute(ProxyAuthInterceptor.ATTR_PROXY_REAL_USER_ID);
            log.info("代理模式确认验收，被代理人ID：{}，实际操作人ID：{}，任务ID：{}", publisherId, realUserId, confirmDTO.getTaskId());
        } else {
            publisherId = Long.parseLong(confirmDTO.getUserId());
            log.info("确认验收请求，任务ID：{}，用户ID：{}", confirmDTO.getTaskId(), publisherId);
        }
        
        Long taskId = Long.parseLong(confirmDTO.getTaskId());
        
        boolean success = clientTaskService.confirmReview(taskId, publisherId, confirmDTO.getRating(), confirmDTO.getReview());
        
        if (success) {
            return Result.success(true, "验收成功");
        } else {
            return Result.error(500, "验收失败", false);
        }
    }
    
    /**
     * 提交志愿者评价
     * 发布者对完成任务的志愿者提交评价
     *
     * @param submitDTO 评价提交请求参数
     * @return 是否提交成功
     */
    @PostMapping("/task/review/submit")
    public Result<Boolean> submitReview(@Valid @RequestBody ReviewSubmitDTO submitDTO) {
        log.info("提交志愿者评价请求，任务ID：{}，发布者ID：{}，志愿者ID：{}",
                submitDTO.getTaskId(), submitDTO.getPublisherId(), submitDTO.getVolunteerId());
        
        Long taskId = Long.parseLong(submitDTO.getTaskId());
        Long publisherId = Long.parseLong(submitDTO.getPublisherId());
        Long volunteerId = Long.parseLong(submitDTO.getVolunteerId());
        
        boolean success = clientTaskService.submitReview(taskId, publisherId, volunteerId,
                submitDTO.getRating(), submitDTO.getContent());
        
        if (success) {
            return Result.success(true, "评价成功");
        } else {
            return Result.error(500, "评价失败", false);
        }
    }
    
    /**
     * 提交申诉
     * 当任务出现争议时，发布者或志愿者可以提交申诉
     *
     * @param submitDTO 申诉提交请求参数
     * @return 是否提交成功
     */
    @PostMapping("/task/appeal/submit")
    public Result<Boolean> submitAppeal(@Valid @RequestBody AppealSubmitDTO submitDTO) {
        log.info("提交申诉请求，任务ID：{}，用户ID：{}",
                submitDTO.getTaskId(), submitDTO.getUserId());
        
        Long taskId = Long.parseLong(submitDTO.getTaskId());
        Long userId = Long.parseLong(submitDTO.getUserId());
        
        boolean success = clientTaskService.submitAppeal(taskId, userId, submitDTO.getReason(), submitDTO.getEvidenceImg());
        
        if (success) {
            return Result.success(true, "申诉提交成功");
        } else {
            return Result.error(500, "申诉提交失败", false);
        }
    }
    
    /**
     * 提交申诉回应
     * 被申诉方针对申诉内容提交回应
     *
     * @param replyDTO 申诉回应请求参数
     * @return 是否提交成功
     */
    @PostMapping("/task/appeal/reply")
    public Result<Boolean> replyAppeal(@Valid @RequestBody AppealReplyDTO replyDTO) {
        log.info("提交申诉回应请求，任务ID：{}，用户ID：{}",
                replyDTO.getTaskId(), replyDTO.getUserId());
        
        Long taskId = Long.parseLong(replyDTO.getTaskId());
        Long userId = Long.parseLong(replyDTO.getUserId());
        
        boolean success = clientTaskService.replyAppeal(taskId, userId, replyDTO.getContent(), replyDTO.getEvidenceImg());
        
        if (success) {
            return Result.success(true, "回应提交成功");
        } else {
            return Result.error(500, "回应提交失败", false);
        }
    }
    
    /**
     * 获取申诉详情
     * 获取该任务关联的申诉详细信息，包括申诉发起人信息
     *
     * @param taskId 任务ID
     * @return 申诉详情
     */
    @GetMapping("/task/appeal/detail")
    public Result<AppealDetailDTO> getAppealDetail(@RequestParam("taskId") String taskId) {
        log.info("获取申诉详情请求，任务ID：{}", taskId);
        
        Long taskIdLong = Long.parseLong(taskId);
        AppealDetailDTO detail = clientTaskService.getAppealDetail(taskIdLong);
        return Result.success(detail, "查询成功");
    }
    
    /**
     * 获取用户信息
     * 获取当前用户的详细信息（包含状态、余额等）
     *
     * @param userId 用户ID（可选，如果不传则通过Token自动解析）
     * @return 用户信息
     */
    @GetMapping("/user/info")
    public Result<UserInfoDTO> getUserInfo(
            @RequestParam(required = false) String userId) {
        log.info("获取用户信息请求，userId：{}", userId);
        
        // 如果没有传userId，这里应该从 Token 中解析
        // 目前简化处理，必须传userId
        if (userId == null || userId.isEmpty()) {
            throw new BusinessException(400, "用户ID不能为空");
        }
        
        UserInfoDTO response = clientAuthService.getUserInfo(userId);
        return Result.success(response, "查询成功");
    }
}
