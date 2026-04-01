package com.example.aiend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiend.entity.Task;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
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

    /**
     * 统计今日已完成的任务数
     */
    @Select("SELECT COUNT(*) FROM tb_task WHERE status = 3 AND DATE(update_time) = CURDATE() AND is_deleted = 0")
    Integer countTodayCompletedOrders();

    /**
     * 按日期统计近7天的活跃趋势
     */
    @Select("SELECT DATE_FORMAT(update_time, '%Y-%m-%d') as date, COUNT(*) as count " +
            "FROM tb_task WHERE status = 3 AND update_time >= DATE_SUB(CURDATE(), INTERVAL 6 DAY) AND is_deleted = 0 " +
            "GROUP BY DATE_FORMAT(update_time, '%Y-%m-%d') " +
            "ORDER BY date ASC")
    java.util.List<com.example.aiend.vo.dashboard.TrendVO> selectActivityTrend();

    /**
     * 按任务类型分类统计分布
     */
    @Select("SELECT type as typeName, COUNT(*) as count FROM tb_task WHERE is_deleted = 0 GROUP BY type")
    java.util.List<com.example.aiend.vo.dashboard.TaskTypeDistributionVO> selectTaskTypeDistribution();
}
