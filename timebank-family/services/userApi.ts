import { get } from './request';
import { User } from '../types';

export const userApi = {
    getUserBalance: async (userId?: string): Promise<number> => {
        const params: Record<string, string> = {};
        if (userId) {
            params.userId = userId;
        }
        const data = await get<{ balance: number }>('/user/balance', params);
        return data.balance;
    },
    getUserInfo: async (userId?: string): Promise<User> => {
        const params: Record<string, string> = {};
        if (userId) {
            params.userId = userId;
        }
        return get<User>('/user/info', params);
    },
    dailyLogin: async (): Promise<{ code: number; message: string; data: string | null }> => {
        // 由于 request.ts 统一在 code != 200 时抛出异常，这里使用独立的 fetch 处理以捕获 400/403 业务状态
        const proxyToken = localStorage.getItem('proxyToken');
        const normalToken = localStorage.getItem('token');
        const token = proxyToken || normalToken;

        const response = await fetch('/api/client/user/daily-login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                ...(token && { Authorization: `Bearer ${token}` }),
            },
        });

        if (!response.ok) {
            throw new Error(`HTTP Error: ${response.status}`);
        }

        const data = await response.json();
        return data;
    }
};
