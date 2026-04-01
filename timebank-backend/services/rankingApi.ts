import { RankingLog } from '../types';

// API响应结构
interface ApiResponse<T> {
    code: number;
    message: string;
    data: T;
}

// 列表响应数据
interface RankingLogListData {
    list: RankingLog[];
    total: number;
    page: number;
    pageSize: number;
}

// 列表查询参数
interface RankingLogListParams {
    page?: number;
    pageSize?: number;
    period?: string;
}

export interface RankingItem {
  rank: number;
  volunteerId: string;
  volunteerName: string;
  volunteerAvatar: string | null;
  orderCount: number;
}

export interface TopRankingData {
  period: string;
  list: RankingItem[];
}

// 获取token的辅助函数
const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
};

export const rankingApi = {
    /**
     * 获取奖励发放日志
     * @param params 查询参数
     * @returns Promise<RankingLogListData>
     */
    getLogs: async (params: RankingLogListParams = {}): Promise<RankingLogListData> => {
        try {
            const queryParams = new URLSearchParams();
            if (params.page) queryParams.append('page', params.page.toString());
            if (params.pageSize) queryParams.append('pageSize', params.pageSize.toString());
            if (params.period) queryParams.append('period', params.period);

            const response = await fetch(`/api/ranking/logs?${queryParams.toString()}`, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<RankingLogListData> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.message || '获取奖励日志失败');
            }
        } catch (error: any) {
            console.error('获取奖励日志失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 手动触发补发
     * @param id 日志ID
     * @returns Promise<void>
     */
    retryDistribution: async (id: string): Promise<void> => {
        try {
            const response = await fetch('/api/ranking/retry', {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ id })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<void> = await response.json();

            if (res.code !== 200) {
                throw new Error(res.message || '补发指令提交失败');
            }
        } catch (error: any) {
            console.error('补发指令提交失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 手动触发月度排名统计
     * @param period 期数，格式 YYYY-MM
     * @returns Promise<void>
     */
    triggerMonthlyStatistics: async (period: string): Promise<void> => {
        try {
            const response = await fetch(`/api/ranking/statistics?period=${period}`, {
                method: 'POST',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<void> = await response.json();

            if (res.code !== 200) {
                throw new Error(res.message || '月度排名统计触发失败');
            }
        } catch (error: any) {
            console.error('月度排名统计触发失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 获取当前接单数前五名排行（实时）
     * @returns Promise<TopRankingData>
     */
    getTopRanking: async (): Promise<TopRankingData> => {
        try {
            const response = await fetch(`/api/client/ranking/top`, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<TopRankingData> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.message || '获取实时排行榜失败');
            }
        } catch (error: any) {
            console.error('获取实时排行榜失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    }
};
