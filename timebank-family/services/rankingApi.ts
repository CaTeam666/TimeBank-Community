import { get } from './request';

// =====================
// 数据类型定义
// =====================

/** 排行项 */
export interface RankingItem {
  rank: number;
  volunteerId: string;
  volunteerName: string;
  volunteerAvatar: string | null;
  orderCount: number;
}

/** 月度排行（含奖励信息） */
export interface MonthlyRankingData {
  period: string;
  list: RankingItem[];
  rewardInfo?: {
    rank: number;
    rewardAmount: number;
  }[];
}

/** 当前用户接单统计 */
export interface UserOrderStats {
  userId: string;
  totalOrderCount: number;
  currentMonthCount: number;
  currentRank: number | null;
}

// =====================
// API 方法
// =====================

export const rankingApi = {
  /**
   * 获取当月接单数前5名排行（实时）
   */
  getTopRanking: async (): Promise<MonthlyRankingData> => {
    return get<MonthlyRankingData>('/client/ranking/top');
  },

  /**
   * 根据月份查看历史排行
   * @param period 格式 YYYY-MM，如 "2026-02"
   */
  getMonthlyRanking: async (period: string): Promise<MonthlyRankingData> => {
    return get<MonthlyRankingData>('/client/ranking/monthly', { period });
  },

  /**
   * 获取当前用户接单统计（总数、当月数、当前排名）
   */
  getMyStats: async (): Promise<UserOrderStats> => {
    return get<UserOrderStats>('/client/ranking/my-stats');
  },
};
