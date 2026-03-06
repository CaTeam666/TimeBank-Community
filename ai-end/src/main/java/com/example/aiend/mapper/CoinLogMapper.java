package com.example.aiend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.aiend.entity.CoinLog;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

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
}
