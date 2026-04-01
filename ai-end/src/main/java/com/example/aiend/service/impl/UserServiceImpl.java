package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.BalanceAdjustmentDTO;
import com.example.aiend.dto.request.UserQueryDTO;
import com.example.aiend.dto.request.UserStatusUpdateDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.entity.User;
import com.example.aiend.entity.CoinLog;
import com.example.aiend.mapper.CoinLogMapper;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.service.UserService;
import com.example.aiend.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 *
 * @author AI-End
 * @since 2025-12-21
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    
    private final UserMapper userMapper;
    private final CoinLogMapper coinLogMapper;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public PageResponseDTO<UserVO> getUserList(UserQueryDTO queryDTO) {
        log.info("查询用户列表，查询条件：{}", queryDTO);
        
        // 构建查询条件
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        
        // 关键词搜索：匹配昵称、手机号或真实姓名
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            queryWrapper.and(wrapper -> wrapper
                    .like(User::getNickname, queryDTO.getKeyword())
                    .or()
                    .like(User::getPhone, queryDTO.getKeyword())
                    .or()
                    .like(User::getRealName, queryDTO.getKeyword())
            );
        }
        
        // 角色筛选
        if (StringUtils.hasText(queryDTO.getRole())) {
            queryWrapper.eq(User::getRole, queryDTO.getRole());
        }
        
        // 状态筛选（需要将字符串状态转换为数字）
        if (StringUtils.hasText(queryDTO.getStatus())) {
            Integer statusValue = "NORMAL".equals(queryDTO.getStatus()) ? 1 : 0;
            queryWrapper.eq(User::getStatus, statusValue);
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(User::getCreateTime);
        
        // 分页查询
        Page<User> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<User> userPage = userMapper.selectPage(page, queryWrapper);
        
        // 转换为 VO
        List<UserVO> userVOList = userPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 构建分页响应
        return PageResponseDTO.<UserVO>builder()
                .list(userVOList)
                .total(userPage.getTotal())
                .page(queryDTO.getPage())
                .pageSize(queryDTO.getPageSize())
                .build();
    }
    
    @Override
    public UserVO getUserDetail(String id) {
        log.info("获取用户详情，用户ID：{}", id);
        
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        return convertToVO(user);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateUserStatus(String id, UserStatusUpdateDTO updateDTO) {
        log.info("更新用户状态，用户ID：{}，目标状态：{}", id, updateDTO.getStatus());
        
        // 查询用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 转换状态值
        Integer statusValue = "NORMAL".equals(updateDTO.getStatus()) ? 1 : 0;
        
        // 更新状态
        user.setStatus(statusValue);
        int updateCount = userMapper.updateById(user);
        
        if (updateCount == 0) {
            throw new BusinessException(500, "状态更新失败");
        }
        
        log.info("用户状态更新成功，用户ID：{}", id);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Map<String, Integer> adjustBalance(String id, BalanceAdjustmentDTO adjustmentDTO) {
        log.info("调整用户余额，用户ID：{}，调整金额：{}，原因：{}", 
                id, adjustmentDTO.getAmount(), adjustmentDTO.getReason());
        
        // 查询用户是否存在
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        
        // 计算新余额
        Integer currentBalance = user.getBalance() != null ? user.getBalance() : 0;
        Integer newBalance = currentBalance + adjustmentDTO.getAmount();
        
        // 余额不能为负数
        if (newBalance < 0) {
            throw new BusinessException(400, "余额不足，无法扣除");
        }
        
        // 更新余额
        user.setBalance(newBalance);
        int updateCount = userMapper.updateById(user);
        
        if (updateCount == 0) {
            throw new BusinessException(500, "余额调整失败");
        }
        
        log.info("用户余额调整成功，用户ID：{}，当前余额：{}", id, newBalance);
        
        
        // 记录系统调整流水
        CoinLog coinLog = new CoinLog();
        coinLog.setUserId(Long.parseLong(id));
        coinLog.setAmount(adjustmentDTO.getAmount()); // 正数为增加，负数为减少
        coinLog.setType(4); // 4：系统调整
        coinLog.setCreateTime(LocalDateTime.now());
        coinLog.setUpdateTime(LocalDateTime.now());
        coinLogMapper.insert(coinLog);
        
        log.info("用户余额调整流水记录成功，用户ID：{}，流水金额：{}", id, adjustmentDTO.getAmount());
        
        // 返回当前余额
        Map<String, Integer> result = new HashMap<>();
        result.put("currentBalance", newBalance);
        return result;
    }
    
    /**
     * 将 User 实体转换为 UserVO
     *
     * @param user 用户实体
     * @return 用户VO
     */
    private UserVO convertToVO(User user) {
        String registerTime = user.getCreateTime() != null 
                ? user.getCreateTime().format(DATE_TIME_FORMATTER) 
                : null;
        
        String status = (user.getStatus() != null && user.getStatus() == 1) ? "NORMAL" : "FROZEN";
        
        return UserVO.builder()
                .id(user.getId())
                .avatar(user.getAvatar())
                .nickname(user.getNickname())
                .realName(user.getRealName())
                .phone(user.getPhone())
                .role(user.getRole())
                .balance(user.getBalance())
                .registerTime(registerTime)
                .status(status)
                .build();
    }
}
