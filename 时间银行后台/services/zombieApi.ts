import { ZombieTaskLog } from '../types';

// API响应结构
interface ApiResponse<T> {
    code: number;
    message: string;
    data: T;
}

// 获取token的辅助函数
const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
};

export const zombieApi = {
    /**
     * 获取僵尸任务日志列表
     * @returns Promise<ZombieTaskLog[]>
     */
    getZombieLogs: async (): Promise<ZombieTaskLog[]> => {
        try {
            const response = await fetch('/api/anomaly/zombie/logs', {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<ZombieTaskLog[]> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.message || '获取僵尸任务日志失败');
            }
        } catch (error: any) {
            console.error('获取僵尸任务日志失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 手动触发僵尸任务检测
     * @returns Promise<void>
     */
    checkZombieTasks: async (): Promise<void> => {
        try {
            const response = await fetch('/api/anomaly/zombie/check', {
                method: 'POST',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<null> = await response.json();

            if (res.code !== 200) {
                throw new Error(res.message || '触发检测失败');
            }
        } catch (error: any) {
            console.error('触发检测失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 手动重试退款
     * @param logId 日志ID
     * @returns Promise<void>
     */
    retryRefund: async (logId: string): Promise<void> => {
        try {
            const response = await fetch(`/api/anomaly/zombie/retry/${logId}`, {
                method: 'POST',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<boolean> = await response.json();

            if (res.code === 200 && res.data === true) {
                return;
            } else {
                throw new Error(res.message || '重试退款失败');
            }
        } catch (error: any) {
            console.error('重试退款失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    }
};
