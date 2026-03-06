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
    }
};
