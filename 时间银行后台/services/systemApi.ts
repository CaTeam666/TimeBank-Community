// 系统设置对象
export interface SystemSettings {
    elderInitialCoins: number;
    dailySignInReward: number;
    monthlyRank1Reward: number;
    transactionFeePercent: number;
}



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

export const systemApi = {
    /**
     * 获取系统配置
     * @returns Promise<SystemSettings>
     */
    getSettings: async (): Promise<SystemSettings> => {
        try {
            const response = await fetch('/api/settings', {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<SystemSettings> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.message || '获取配置失败');
            }
        } catch (error: any) {
            console.error('获取系统配置失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 更新系统配置
     * @param settings 部分或全部配置项
     * @returns Promise<void>
     */
    updateSettings: async (settings: Partial<SystemSettings>): Promise<void> => {
        try {
            const response = await fetch('/api/settings', {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify(settings)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<void> = await response.json();

            if (res.code !== 200) {
                throw new Error(res.message || '保存配置失败');
            }
        } catch (error: any) {
            console.error('保存系统配置失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    }
};
