package com.example.aiend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiend.entity.DailyCoinStats;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * 每日时间币统计Mapper接口
 *
 * @author AI-End
 * @since 2026-03-22
 */
@Mapper
public interface DailyCoinStatsMapper extends BaseMapper<DailyCoinStats> {

    /**
     * 根据日期范围查询统计数据
     *
     * @param startDate 开始日期
     * @param endDate   结束日期
     * @return 统计数据列表
     */
    @Select("<script>" +
            "SELECT * FROM tb_daily_coin_stats " +
            "<where> " +
            "  <if test='startDate != null'>" +
            "    AND stat_date &gt;= #{startDate}" +
            "  </if>" +
            "  <if test='endDate != null'>" +
            "    AND stat_date &lt;= #{endDate}" +
            "  </if>" +
            "</where> " +
            "ORDER BY stat_date DESC" +
            "</script>")
    List<DailyCoinStats> selectByDateRange(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
