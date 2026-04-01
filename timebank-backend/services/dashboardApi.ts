// services/dashboardApi.ts

// API响应结构
interface ApiResponse<T> {
    code: number;
    message: string;
    data: T;
}

export interface KpiData {
  totalPopulation: number;
  todayExchangeCount: number;
  todayNewOrders: number;
  todayPointsCirculation: number;
}

export interface ActivityTrendItem {
  date: string;
  count: number;
}

export interface TaskTypeDistributionItem {
  typeName: string;
  count: number;
}

export interface DynamicsItem {
  id: number;
  type: number;
  userName: string;
  content: string;
  createTime: string;
}

const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
};

export const dashboardApi = {
    /**
     * 获取核心指标统计 (KPI卡片)
     */
    getKPIs: async (): Promise<KpiData> => {
        try {
            const response = await fetch('/api/admin/dashboard/kpi', {
                method: 'GET',
                headers: getAuthHeaders()
            });
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const res: ApiResponse<KpiData> = await response.json();
            if (res.code === 200) return res.data;
            throw new Error(res.message || '获取KPI数据失败');
        } catch (error: any) {
            console.error('获取KPI数据失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 获取近7日服务活跃度趋势
     */
    getActivityTrend: async (): Promise<ActivityTrendItem[]> => {
        try {
            const response = await fetch('/api/admin/dashboard/trend/activity', {
                method: 'GET',
                headers: getAuthHeaders()
            });
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const res: ApiResponse<ActivityTrendItem[]> = await response.json();
            if (res.code === 200) return res.data;
            throw new Error(res.message || '获取活跃度趋势失败');
        } catch (error: any) {
            console.error('获取活跃度趋势失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 获取任务类型分布
     */
    getTaskDistribution: async (): Promise<TaskTypeDistributionItem[]> => {
        try {
            const response = await fetch('/api/admin/dashboard/distribution/task-type', {
                method: 'GET',
                headers: getAuthHeaders()
            });
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const res: ApiResponse<TaskTypeDistributionItem[]> = await response.json();
            if (res.code === 200) return res.data;
            throw new Error(res.message || '获取任务分布失败');
        } catch (error: any) {
            console.error('获取任务分布失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 获取实时动态
     */
    getDynamics: async (limit: number = 20): Promise<DynamicsItem[]> => {
        try {
            const response = await fetch(`/api/admin/dashboard/dynamics?limit=${limit}`, {
                method: 'GET',
                headers: getAuthHeaders()
            });
            if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
            const res: ApiResponse<DynamicsItem[]> = await response.json();
            if (res.code === 200) return res.data;
            throw new Error(res.message || '获取实时动态失败');
        } catch (error: any) {
            console.error('获取实时动态失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    }
};
