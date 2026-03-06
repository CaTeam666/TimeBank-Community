package com.example.aiend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiend.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

/**
 * 任务 Mapper
 *
 * @author AI-End
 * @since 2025-12-26
 */
@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    
    /**
     * 乐观锁接取任务
     * 只有当任务状态为 0（待接取）时才能更新
     *
     * @param taskId      任务ID
     * @param volunteerId 志愿者ID
     * @return 影响行数（1:成功 0:失败/已被抢走）
     */
    @Update("UPDATE tb_task SET volunteer_id = #{volunteerId}, status = 1, update_time = NOW() " +
            "WHERE id = #{taskId} AND status = 0 AND is_deleted = 0")
    int acceptTaskWithOptimisticLock(@Param("taskId") Long taskId, @Param("volunteerId") Long volunteerId);
}
