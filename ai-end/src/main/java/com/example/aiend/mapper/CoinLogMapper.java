package com.example.aiend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiend.entity.CoinLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Param;

/**
 * 时间币流水Mapper接口
 * 
 * @author AI-End
 * @since 2026-01-02
 */
@Mapper
public interface CoinLogMapper extends BaseMapper<CoinLog> {
    
    /**
     * 统计今日指定类型的金额总和
     *
     * @param type 类型（1:任务收入 2:任务支出 3:兑换支出 4:系统调整）
     * @return 金额总和
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM tb_coin_log WHERE type = #{type} AND DATE(create_time) = CURDATE() AND is_deleted = 0")
    Integer sumTodayAmountByType(Integer type);
    
    /**
     * 统计今日交易笔数
     *
     * @return 交易笔数
     */
    @Select("SELECT COUNT(*) FROM tb_coin_log WHERE DATE(create_time) = CURDATE() AND is_deleted = 0")
    Integer countTodayTransactions();
    
    /**
     * 统计所有收入总和（总流通量）
     *
     * @return 收入总和
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM tb_coin_log WHERE type = 1 AND is_deleted = 0")
    Integer sumAllIncome();
    
    /**
     * 按指定日期统计指定类型的金额总和
     *
     * @param type 类型
     * @param date 日期
     * @return 金额总和
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM tb_coin_log WHERE type = #{type} AND DATE(create_time) = #{date} AND is_deleted = 0")
    Integer sumAmountByTypeAndDate(@Param("type") Integer type, @Param("date") java.time.LocalDate date);
    
    /**
     * 按指定日期统计交易笔数
     *
     * @param date 日期
     * @return 交易笔数
     */
    @Select("SELECT COUNT(*) FROM tb_coin_log WHERE DATE(create_time) = #{date} AND is_deleted = 0")
    Integer countTransactionsByDate(@Param("date") java.time.LocalDate date);

    /**
     * 统计今日总流通量（这里统计任务收入和系统调整等产生的流通，主要是所有 type 为 1 的流入流通）
     */
    @Select("SELECT COALESCE(SUM(amount), 0) FROM tb_coin_log WHERE DATE(create_time) = CURDATE() AND type IN (1) AND is_deleted = 0")
    Integer sumTodayCirculation();
}
