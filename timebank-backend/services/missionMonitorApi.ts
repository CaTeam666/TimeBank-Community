import { Task, TaskStatus, TaskType } from '../types';

// API响应通用结构
interface ApiResponse<T> {
    code: number;
    msg: string;
    data: T;
}

export interface TaskQueryParams {
    page?: number;
    pageSize?: number;
    keyword?: string;
    status?: string;
    type?: string;
    date?: string;
}

export interface TaskResponse {
    total: number;
    list: Task[];
}

// 获取token的辅助函数
const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
};

export const missionMonitorApi = {
    // 获取任务列表
    getTasks: async (params: TaskQueryParams): Promise<TaskResponse> => {
        try {
            const queryParams = new URLSearchParams();
            if (params.page) queryParams.append('page', params.page.toString());
            if (params.pageSize) queryParams.append('pageSize', params.pageSize.toString());
            if (params.keyword) queryParams.append('keyword', params.keyword);

            // Map status string to number for backend
            const statusMap: Record<string, number> = {
                'PENDING': 0,
                'IN_PROGRESS': 1,
                'WAITING_ACCEPTANCE': 2,
                'COMPLETED': 3,
                'COMPLAINT': 4,
                'CANCELLED': 5,
                'EXPIRED': 6
            };

            // Map type string to chinese/backend expected value if needed, 
            // or confirm if backend expects 'ERRAND' vs 1. 
            // Assuming Type might also need mapping, or it uses the enum keys.
            // Previous TaskHall context mentioned Chinese mapping, let's stick to status first or check if type is an issue.
            // Let's assume Type is fine (usually string or enum name) or check constants.
            // But status is definitely suspect given user's "0 or 1" comment.

            if (params.status && params.status !== 'ALL') {
                const numericStatus = statusMap[params.status];
                if (numericStatus !== undefined) {
                    queryParams.append('status', numericStatus.toString());
                } else {
                    // Fallback for number strings or unknown
                    queryParams.append('status', params.status);
                }
            }

            if (params.type && params.type !== 'ALL') queryParams.append('type', params.type);
            if (params.date) queryParams.append('date', params.date);

            const response = await fetch(`/api/mission/list?${queryParams.toString()}`, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<TaskResponse> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.msg || '获取任务列表失败');
            }
        } catch (error: any) {
            console.error('获取任务列表失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    // 强制关闭任务
    forceCloseTask: async (taskId: string, reason: string): Promise<void> => {
        try {
            const response = await fetch('/api/mission/force-close', {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ taskId, reason })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<void> = await response.json();

            if (res.code !== 200) {
                throw new Error(res.msg || '强制关闭任务失败');
            }
        } catch (error: any) {
            console.error('强制关闭任务失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    // 获取任务详情
    getTaskDetail: async (taskId: string): Promise<Task> => {
        try {
            const response = await fetch(`/api/mission/${taskId}`, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<Task> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.msg || '获取任务详情失败');
            }
        } catch (error: any) {
            console.error('获取任务详情失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    }
};
