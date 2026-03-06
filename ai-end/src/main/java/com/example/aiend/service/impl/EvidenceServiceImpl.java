package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiend.dto.request.EvidenceQueryDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.entity.Task;
import com.example.aiend.entity.TaskEvidence;
import com.example.aiend.entity.User;
import com.example.aiend.mapper.TaskEvidenceMapper;
import com.example.aiend.mapper.TaskMapper;
import com.example.aiend.mapper.UserMapper;
import com.example.aiend.service.EvidenceService;
import com.example.aiend.vo.EvidenceListVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 存证服务实现类
 *
 * @author AI-End Team
 * @since 2024-12-26
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class EvidenceServiceImpl implements EvidenceService {

    private final TaskEvidenceMapper taskEvidenceMapper;
    private final TaskMapper taskMapper;
    private final UserMapper userMapper;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 获取存证列表
     *
     * @param queryDTO 查询参数
     * @return 分页存证列表
     */
    @Override
    public PageResponseDTO<EvidenceListVO> getEvidenceList(EvidenceQueryDTO queryDTO) {
        log.info("获取存证列表，查询参数：{}", queryDTO);

        LambdaQueryWrapper<TaskEvidence> queryWrapper = new LambdaQueryWrapper<>();

        // 关键词搜索 - 需要处理任务ID、志愿者姓名、志愿者ID
        if (StringUtils.hasText(queryDTO.getKeyword())) {
            String keyword = queryDTO.getKeyword().trim();
            
            // 尝试按ID搜索
            try {
                Long id = Long.parseLong(keyword);
                // 可能是任务ID或志愿者ID
                queryWrapper.and(w -> w.eq(TaskEvidence::getTaskId, id)
                        .or().eq(TaskEvidence::getVolunteerId, id));
            } catch (NumberFormatException e) {
                // 非数字，按志愿者姓名搜索
                // 先查询符合条件的志愿者ID列表
                LambdaQueryWrapper<User> userWrapper = new LambdaQueryWrapper<>();
                userWrapper.like(User::getNickname, keyword)
                        .or().like(User::getRealName, keyword);
                List<User> matchedUsers = userMapper.selectList(userWrapper);
                
                if (!matchedUsers.isEmpty()) {
                    List<Long> userIds = matchedUsers.stream()
                            .map(user -> Long.parseLong(user.getId()))
                            .collect(Collectors.toList());
                    queryWrapper.in(TaskEvidence::getVolunteerId, userIds);
                } else {
                    // 没有匹配的用户，返回空结果
                    return PageResponseDTO.<EvidenceListVO>builder()
                            .list(List.of())
                            .total(0L)
                            .page(queryDTO.getPage())
                            .pageSize(queryDTO.getPageSize())
                            .build();
                }
            }
        }

        // 按创建时间倒序排序
        queryWrapper.orderByDesc(TaskEvidence::getCreateTime);

        // 分页查询
        Page<TaskEvidence> page = new Page<>(queryDTO.getPage(), queryDTO.getPageSize());
        Page<TaskEvidence> evidencePage = taskEvidenceMapper.selectPage(page, queryWrapper);

        // 转换为VO
        List<EvidenceListVO> listVOs = evidencePage.getRecords().stream()
                .map(this::convertToEvidenceListVO)
                .collect(Collectors.toList());

        return PageResponseDTO.<EvidenceListVO>builder()
                .list(listVOs)
                .total(evidencePage.getTotal())
                .page(queryDTO.getPage())
                .pageSize(queryDTO.getPageSize())
                .build();
    }

    /**
     * 将实体转换为VO
     *
     * @param evidence 存证实体
     * @return 存证列表VO
     */
    private EvidenceListVO convertToEvidenceListVO(TaskEvidence evidence) {
        EvidenceListVO vo = new EvidenceListVO();
        vo.setId(String.valueOf(evidence.getId()));
        vo.setTaskId(String.valueOf(evidence.getTaskId()));
        vo.setVolunteerId(String.valueOf(evidence.getVolunteerId()));
        vo.setImageUrl(evidence.getImageUrl());

        // 格式化时间
        if (evidence.getCreateTime() != null) {
            vo.setCreateTime(evidence.getCreateTime().format(DATE_TIME_FORMATTER));
        }

        // 关联查询任务标题
        Task task = taskMapper.selectById(evidence.getTaskId());
        if (task != null) {
            vo.setTaskTitle(task.getTitle());
        }

        // 关联查询志愿者姓名
        User volunteer = userMapper.selectById(evidence.getVolunteerId());
        if (volunteer != null) {
            vo.setVolunteerName(volunteer.getNickname() != null ? volunteer.getNickname() : volunteer.getRealName());
        }

        return vo;
    }
}
