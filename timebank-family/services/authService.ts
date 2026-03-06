import { post, upload, get } from './request';
import { User, UserRole } from '../types';

interface LoginResponse {
    token: string;
    user: any; // Raw user from backend
}

// Map backend Chinese roles to Frontend English Enums
const mapRole = (cnRole: string): UserRole => {
    switch (cnRole) {
        case '老人': return UserRole.SENIOR;
        case '志愿者': return UserRole.VOLUNTEER;
        case '子女代理人': return UserRole.AGENT;
        default: return UserRole.VOLUNTEER;
    }
}

// Reverse Map for Registration
const mapRoleToCn = (role: UserRole): string => {
    switch (role) {
        case UserRole.SENIOR: return '老人';
        case UserRole.VOLUNTEER: return '志愿者';
        case UserRole.AGENT: return '子女代理人';
        default: return '志愿者';
    }
}

export const authService = {
    login: async (phone: string, password: string): Promise<User> => {
        const data = await post<LoginResponse>('/sys/login', { phone, password });

        // Save token
        localStorage.setItem('token', data.token);

        // Transform user data
        return {
            id: data.user.id.toString(),
            phone: data.user.phone,
            nickname: data.user.nickname,
            avatar: data.user.avatar || 'https://picsum.photos/200',
            role: mapRole(data.user.role), // Map role here
            balance: data.user.balance || 0,
            creditScore: 5.0, // Default or mock if not in DB
            realName: data.user.realName,
            idCardNumber: data.user.id_card, // Note casing from DB
        };
    },

    register: async (registerData: any): Promise<{ auditId: string, status: number }> => {
        const payload = {
            ...registerData,
            role: registerData.role // Backend requires English Enum (SENIOR, VOLUNTEER, AGENT)
        };

        const data = await post<{ auditId: string, status: number }>('/sys/register', payload);
        return data;
    },

    checkAuditStatus: async (auditId: string): Promise<{
        status: number;
        reject_reason?: string;
        token?: string;
        user?: any;
    }> => {
        const data = await get<any>('/sys/audit/result', { auditId });

        if (data.status === 1 && data.token) {
            localStorage.setItem('token', data.token);
            // Manually transform user if needed, similar to login
            data.user = {
                id: data.user.id.toString(),
                phone: data.user.phone,
                nickname: data.user.nickname,
                avatar: data.user.avatar || 'https://picsum.photos/200',
                role: mapRole(data.user.role),
                balance: data.user.balance || 0,
                creditScore: 5.0,
                realName: data.user.realName,
                idCardNumber: data.user.id_card,
            };
        }
        return data;
    },

    uploadImage: async (file: File): Promise<string> => {
        const data = await upload<{ url: string }>('/sys/upload', file);
        return data.url;
    }
};
