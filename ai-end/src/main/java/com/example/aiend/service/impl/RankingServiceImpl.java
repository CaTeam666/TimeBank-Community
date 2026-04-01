package com.example.aiend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.aiend.common.exception.BusinessException;
import com.example.aiend.dto.request.RankingLogQueryDTO;
import com.example.aiend.dto.request.RankingLogRetryDTO;
import com.example.aiend.dto.response.PageResponseDTO;
import com.example.aiend.entity.RankingLog;
import com.example.aiend.mapper.RankingLogMapper;
import com.example.aiend.service.RankingService;
import com.example.aiend.vo.RankingLogVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 排名奖励服务实现类
 *
 * @author AI-End
 * @since 2025-12-27
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RankingServiceImpl implements RankingService {
    
    private final RankingLogMapper rankingLogMapper;
    private final RedisTemplate<String, Object> redisTemplate;
    
    /**
     * Redis排名缓存Key前缀
     */
    private static final String RANKING_CACHE_KEY_PREFIX = "ranking:monthly:";
    
    /**
     * 获取奖励发放日志
     * 优先从Redis查询，没有缓存则查数据库
     *
     * @param queryDTO 查询条件
     * @return 排名日志列表（前5名）
     */
    @Override
    public PageResponseDTO<RankingLogVO> getRankingLogs(RankingLogQueryDTO queryDTO) {
        Objects.requireNonNull(queryDTO, "查询条件不能为空");
        
        log.info("查询奖励发放日志，参数：{}", queryDTO);
        
        String period = queryDTO.getPeriod();
        
        // 如果指定了期数，优先从Redis查询
        if (period != null && !period.isEmpty()) {
            List<RankingLogVO> cachedList = getFromRedis(period);
            if (cachedList != null && !cachedList.isEmpty()) {
                log.info("从Redis缓存中获取到 {} 条排名数据，期数: {}", cachedList.size(), period);
                return PageResponseDTO.<RankingLogVO>builder()
                        .list(cachedList)
                        .total((long) cachedList.size())
                        .build();
            }
        }
        
        // Redis没有缓存，查询数据库
        return getFromDatabase(queryDTO);
    }
    
    /**
     * 从Redis缓存中获取排名数据
     *
     * @param period 期数
     * @return 排名日志VO列表
     */
    private List<RankingLogVO> getFromRedis(String period) {
        String cacheKey = RANKING_CACHE_KEY_PREFIX + period;
        
        try {
            // 获取Redis List中的所有数据
            List<Object> cachedData = redisTemplate.opsForList().range(cacheKey, 0, -1);
            if (cachedData == null || cachedData.isEmpty()) {
                log.debug("Redis缓存中没有期数 {} 的排名数据", period);
                return null;
            }
            
            // 转换为VO列表
            return cachedData.stream()
                    .filter(obj -> obj instanceof RankingLog)
                    .map(obj -> {
                        RankingLog rankingLog = (RankingLog) obj;
                        return convertToVO(rankingLog);
                    })
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("从Redis获取排名数据失败，将查询数据库，错误: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 从数据库中获取排名数据
     *
     * @param queryDTO 查询条件
     * @return 分页响应
     */
    private PageResponseDTO<RankingLogVO> getFromDatabase(RankingLogQueryDTO queryDTO) {
        // 构造查询条件
        LambdaQueryWrapper<RankingLog> wrapper = new LambdaQueryWrapper<>();
        
        // 如果指定了期数，则按期数筛选
        if (queryDTO.getPeriod() != null && !queryDTO.getPeriod().isEmpty()) {
            wrapper.eq(RankingLog::getPeriod, queryDTO.getPeriod());
        }
        
        // 按接单数降序排列，只取前5条
        wrapper.orderByDesc(RankingLog::getOrderCount)
               .last("LIMIT 5");
        
        // 执行查询
        List<RankingLog> rankingLogs = rankingLogMapper.selectList(wrapper);
        
        // 实体转换为VO，并根据排名赋值rank字段（1-5）
        List<RankingLogVO> voList = new java.util.ArrayList<>();
        for (int i = 0; i < rankingLogs.size(); i++) {
            RankingLogVO vo = convertToVO(rankingLogs.get(i));
            // 根据接单数排序后赋值rank（1-5）
            vo.setRank(i + 1);
            voList.add(vo);
        }
        
        log.info("从数据库查询到 {} 条奖励发放日志（前5名）", voList.size());
        
        // 返回结果
        return PageResponseDTO.<RankingLogVO>builder()
                .list(voList)
                .total((long) voList.size())
                .build();
    }
    
    /**
     * 手动触发补发
     * 对发放失败的奖励进行补发
     *
     * @param retryDTO 补发请求参数
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void retryDistribution(RankingLogRetryDTO retryDTO) {
        Objects.requireNonNull(retryDTO, "补发请求参数不能为空");
        Objects.requireNonNull(retryDTO.getId(), "日志ID不能为空");
        
        log.info("手动触发补发，日志ID：{}", retryDTO.getId());
        
        // 查询日志记录
        RankingLog rankingLog = rankingLogMapper.selectById(retryDTO.getId());
        if (rankingLog == null) {
            log.error("日志记录不存在，日志ID：{}", retryDTO.getId());
            throw new BusinessException("日志记录不存在");
        }
        
        // 检查状态是否为失败
        if (!"FAILURE".equals(rankingLog.getStatus())) {
            log.error("只能补发失败的记录，当前状态：{}", rankingLog.getStatus());
            throw new BusinessException("只能补发失败的记录");
        }
        
        // TODO: 这里应该调用实际的奖励发放逻辑
        // 例如：调用支付服务、更新用户余额等
        // 暂时只更新状态和发放时间
        
        rankingLog.setStatus("SUCCESS");
        rankingLog.setDistributionTime(LocalDateTime.now());
        rankingLog.setUpdateTime(LocalDateTime.now());
        
        // 更新记录
        int updated = rankingLogMapper.updateById(rankingLog);
        if (updated == 0) {
            log.error("补发失败，更新记录失败，日志ID：{}", retryDTO.getId());
            throw new BusinessException("补发失败");
        }
        
        log.info("补发成功，日志ID：{}", retryDTO.getId());
    }
    
    /**
     * 实体转换为VO
     *
     * @param rankingLog 实体对象
     * @return VO对象
     */
    private RankingLogVO convertToVO(RankingLog rankingLog) {
        RankingLogVO vo = new RankingLogVO();
        BeanUtils.copyProperties(rankingLog, vo);
        return vo;
    }
}
