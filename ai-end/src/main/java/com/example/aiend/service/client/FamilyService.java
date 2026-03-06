package com.example.aiend.service.client;

import com.example.aiend.dto.request.client.FamilyBindDTO;
import com.example.aiend.dto.request.client.FamilyReviewDTO;
import com.example.aiend.dto.request.client.FamilyUnbindDTO;
import com.example.aiend.dto.request.client.ProxyToggleDTO;
import com.example.aiend.dto.response.client.FamilyBindResponseVO;
import com.example.aiend.dto.response.client.FamilyMemberVO;
import com.example.aiend.dto.response.client.PendingConfirmCountVO;
import com.example.aiend.dto.response.client.PendingRequestListVO;
import com.example.aiend.dto.response.client.ProxyToggleResponseVO;

import java.util.List;

/**
 * 亲情代理服务接口
 * 处理亲情账号绑定、解绑、代理切换等业务逻辑
 *
 * @author AI-End
 * @since 2026-02-07
 */
public interface FamilyService {
    
    /**
     * 申请绑定亲情账号
     * 子女用户通过手机号申请绑定长者账号
     *
     * @param childId 子女用户ID（当前登录用户）
     * @param bindDTO 绑定申请请求参数
     * @return 绑定申请响应（包含关系记录ID）
     */
    FamilyBindResponseVO bind(Long childId, FamilyBindDTO bindDTO);
    
    /**
     * 获取亲情账号列表
     * 获取当前用户已绑定的亲情账号列表（仅返回已通过审核的绑定关系）
     *
     * @param childId 子女用户ID（当前登录用户）
     * @return 亲情账号列表
     */
    List<FamilyMemberVO> getList(Long childId);
    
    /**
     * 解绑亲情账号
     * 解除与长者的绑定关系（逻辑删除）
     *
     * @param childId 子女用户ID（当前登录用户）
     * @param unbindDTO 解绑请求参数
     */
    void unbind(Long childId, FamilyUnbindDTO unbindDTO);
    
    /**
     * 切换代理模式
     * 开启或关闭代理模式，子女可代替长者进行操作
     *
     * @param childId 子女用户ID（当前登录用户）
     * @param toggleDTO 代理模式切换请求参数
     * @return 代理模式响应（包含代理令牌）
     */
    ProxyToggleResponseVO toggleProxy(Long childId, ProxyToggleDTO toggleDTO);
    
    /**
     * 获取待审核的绑定申请
     * 获取其他用户向当前用户发起的待审核绑定申请
     *
     * @param parentId 当前用户ID（作为被绑定方）
     * @return 待审核申请列表
     */
    PendingRequestListVO getPendingRequests(Long parentId);
    
    /**
     * 审核绑定申请
     * 被绑定方审核绑定申请（通过或拒绝）
     *
     * @param parentId 当前用户ID（作为被绑定方）
     * @param reviewDTO 审核请求参数
     */
    void review(Long parentId, FamilyReviewDTO reviewDTO);
    
    /**
     * 获取待确认的绑定申请数量
     * 获取当前用户作为长者(parent_id)收到的待确认绑定申请数量
     * 用于个人页面消息红点提示
     *
     * @param parentId 当前用户ID（作为被绑定方）
     * @return 待确认申请数量
     */
    PendingConfirmCountVO getPendingConfirmCount(Long parentId);
}
