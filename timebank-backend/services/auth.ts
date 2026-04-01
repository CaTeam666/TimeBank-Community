import { User, UserRole, AccountStatus } from '../types';

// 真实接口响应结构
interface LoginResponse {
    code: number;
    msg: string;
    data: {
        token: string;
        user: User;
    };
}

export const authService = {
    /**
     * 登录接口
     * @param username 账号
     * @param password 密码
     * @returns Promise<{ token: string; user: User }>
     */
    login: async (username: string, password: string): Promise<{ token: string; user: User }> => {
        try {
            const response = await fetch('/api/auth/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({ username, password })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: LoginResponse = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.msg || '登录失败');
            }
        } catch (error: any) {
            console.error('Login failed:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 登出接口
     */
    logout: () => {
        localStorage.removeItem('token');
        localStorage.removeItem('user');
    }
};
