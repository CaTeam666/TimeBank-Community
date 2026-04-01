package com.example.aiend.mapper;

import com.example.aiend.vo.dashboard.DynamicVO;
import com.example.aiend.vo.dashboard.VolunteerRankingVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DashboardMapper {

    @Select("SELECT * FROM (" +
            "  SELECT id, 1 as type, real_name as userName, '提交了实名认证申请' as content, create_time as createTime FROM sys_identity_audit WHERE is_deleted = 0 " +
            "  UNION ALL " +
            "  SELECT r.id, 2 as type, child.realName as userName, '提交了亲情绑定申请' as content, r.create_time as createTime FROM sys_user_relation r LEFT JOIN sys_user child ON r.child_id = child.id WHERE r.is_deleted = 0 " +
            "  UNION ALL " +
            "  SELECT a.id, 3 as type, u.realName as userName, '发起了服务仲裁纠纷' as content, a.create_time as createTime FROM tb_appeal a LEFT JOIN sys_user u ON a.proposer_id = u.id WHERE a.is_deleted = 0 " +
            ") as combined " +
            "ORDER BY createTime DESC " +
            "LIMIT #{limit}")
    List<DynamicVO> selectDynamics(@Param("limit") Integer limit);

    @Select("SELECT t.volunteer_id as userId, u.realName as userName, u.avatar as avatar, COUNT(*) as completedOrders " +
            "FROM tb_task t " +
            "LEFT JOIN sys_user u ON t.volunteer_id = u.id " +
            "WHERE t.status = 3 AND t.is_deleted = 0 " +
            "  AND DATE_FORMAT(t.update_time, '%Y-%m') = DATE_FORMAT(CURDATE(), '%Y-%m') " +
            "GROUP BY t.volunteer_id, u.realName, u.avatar " +
            "ORDER BY completedOrders DESC " +
            "LIMIT 5")
    List<VolunteerRankingVO> selectVolunteerRanking();
}
