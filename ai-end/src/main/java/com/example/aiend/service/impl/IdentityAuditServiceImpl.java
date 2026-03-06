package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiend.common.enums.AuditStatusEnum;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.IdentityAuditQueryDTO;
import com.example.aiend.dto.request.IdentityAuditResultDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.entity.IdentityAudit;
import com.example.aiend.mapper.IdentityAuditMapper;
import com.example.aiend.service.IdentityAuditService;
import com.example.aiend.vo.IdentityAuditVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 实名认证审核服务实现类
 *
 * @author AI-End
 * @since 2025-12-24
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IdentityAuditServiceImpl implements IdentityAuditService {
    
    private final IdentityAuditMapper identityAuditMapper;
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    
    @Override
    public PageResponseDTO<IdentityAuditVO> getAuditList(IdentityAuditQueryDTO queryDTO) {
        log.info("查询实名审核列表，查询条件：{}", queryDTO);
        
        // 构建查询条件
        LambdaQueryWrapper<IdentityAudit> queryWrapper = new LambdaQueryWrapper<>();
        
        // 状态筛选
        if (StringUtils.hasText(queryDTO.getStatus())) {
            AuditStatusEnum statusEnum = AuditStatusEnum.fromStatus(queryDTO.getStatus());
            queryWrapper.eq(IdentityAudit::getStatus, statusEnum.getCode());
        }
        
        // 按创建时间倒序排列
        queryWrapper.orderByDesc(IdentityAudit::getCreateTime);
        
        // 分页查询
        Page<IdentityAudit> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        IPage<IdentityAudit> auditPage = identityAuditMapper.selectPage(page, queryWrapper);
        
        // 转换为 VO
        List<IdentityAuditVO> auditVOList = auditPage.getRecords().stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        
        // 构建分页响应
        return PageResponseDTO.<IdentityAuditVO>builder()
                .list(auditVOList)
                .total(auditPage.getTotal())
                .page(queryDTO.getPage())
                .pageSize(queryDTO.getPageSize())
                .build();
    }
    
    @Override
    public IdentityAuditVO getAuditDetail(Long id) {
        log.info("获取实名审核详情，审核ID：{}", id);
        
        IdentityAudit audit = identityAuditMapper.selectById(id);
        if (audit == null) {
            throw new BusinessException(404, "审核任务不存在");
        }
        
        return convertToVO(audit);
    }
    
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void submitAuditResult(Long id, IdentityAuditResultDTO resultDTO) {
        log.info("提交审核结果，审核ID：{}，审核状态：{}", id, resultDTO.getStatus());
        
        // 查询审核任务是否存在
        IdentityAudit audit = identityAuditMapper.selectById(id);
        if (audit == null) {
            throw new BusinessException(404, "审核任务不存在");
        }
        
        // 检查当前状态是否为待审核
        if (!AuditStatusEnum.PENDING.getCode().equals(audit.getStatus())) {
            throw new BusinessException(400, "该审核任务已处理，无法重复操作");
        }
        
        // 如果是驳回，检查驳回原因
        if ("REJECTED".equals(resultDTO.getStatus()) && !StringUtils.hasText(resultDTO.getRejectReason())) {
            throw new BusinessException(400, "驳回时必须填写驳回原因");
        }
        
        // 更新审核状态
        AuditStatusEnum statusEnum = AuditStatusEnum.fromStatus(resultDTO.getStatus());
        audit.setStatus(statusEnum.getCode());
        audit.setAuditTime(LocalDateTime.now());
        
        // 如果是驳回，设置驳回原因
        if ("REJECTED".equals(resultDTO.getStatus())) {
            audit.setRejectReason(resultDTO.getRejectReason());
        }
        
        int updateCount = identityAuditMapper.updateById(audit);
        
        if (updateCount == 0) {
            throw new BusinessException(500, "审核结果提交失败");
        }
        
        log.info("审核结果提交成功，审核ID：{}，审核状态：{}", id, resultDTO.getStatus());
    }
    
    /**
     * 将 IdentityAudit 实体转换为 IdentityAuditVO
     *
     * @param audit 审核实体
     * @return 审核VO
     */
    private IdentityAuditVO convertToVO(IdentityAudit audit) {
        String submitTime = audit.getCreateTime() != null 
                ? audit.getCreateTime().format(DATE_TIME_FORMATTER) 
                : null;
        
        // 转换状态码为状态字符串
        String status = AuditStatusEnum.fromCode(audit.getStatus()).getStatus();
        
        return IdentityAuditVO.builder()
                .id(String.valueOf(audit.getId()))
                .userId(String.valueOf(audit.getUserId()))
                .userName(audit.getRealName())
                .submitTime(submitTime)
                .ocrAge(audit.getAge())
                .idCardFront(audit.getIdCardFront())
                .idCardBack(audit.getIdCardBack())
                .ocrName(audit.getRealName())
                .ocrIdNumber(audit.getIdCard())
                .status(status)
                .build();
    }
}
