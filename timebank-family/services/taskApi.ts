import { post, get } from './request';
import { TaskType, Task, TaskCategory } from '../types';

export interface TaskPublishRequest {
    title: string;
    type: TaskType;
    description: string;
    coins: number;
    location: string;
    date: string;
    timeRange: string;
    publisherId?: string;
}

export interface PublishResponse {
    taskId: string;
}

export interface BalanceResponse {
    balance: number;
}

export const taskApi = {
    publishTask: async (data: TaskPublishRequest): Promise<PublishResponse> => {
        return post<PublishResponse>('/task/publish', data);
    },

    getUserBalance: async (userId?: string): Promise<number> => {
        const params: Record<string, string> = {};
        if (userId) {
            params.userId = userId;
        }
        const data = await get<BalanceResponse>('/user/balance', params);
        return data.balance;
    },

    getTaskHallList: async (type?: string): Promise<any[]> => {
        const params: Record<string, string> = {};
        if (type && type !== '全部') {
            params.type = type;
        }
        const data = await get<any[]>('/task/hall', params);
        return data;
    },

    getTaskDetail: async (taskId: string): Promise<any> => {
        return get<any>('/task/detail', { taskId });
    },

    getTaskCategories: async (): Promise<TaskCategory[]> => {
        return get<TaskCategory[]>('/task/categories');
    },

    acceptTask: async (taskId: string, userId: string): Promise<boolean> => {
        const res = await post<{ data: boolean }>('/task/accept', { taskId, userId });
        // The post generic T is usually the response body type. 
        // Our request.ts says: if (data && data.code === 200) return data.data;
        // So `post` returns `data.data`.
        // If the API returns { code: 200, data: true }, `post` returns true.
        // We cast it to expected type.
        return res as unknown as boolean;
    },

    getMyAcceptedOrders: async (userId: string, status?: number): Promise<any[]> => {
        const params: Record<string, string> = { userId };
        if (status !== undefined) {
            params.status = status.toString();
        }
        return get<any[]>('/task/my/accepted', params);
    },

    getMyPublishedOrders: async (userId: string, status?: number): Promise<any[]> => {
        const params: Record<string, string> = { userId };
        if (status !== undefined) {
            params.status = status.toString();
        }
        return get<any[]>('/task/my/published', params);
    },

    cancelTask: async (taskId: string, userId: string): Promise<boolean> => {
        return post<boolean>('/task/cancel', { taskId, userId });
    },

    submitEvidence: async (taskId: string, userId: string, imageUrl: string): Promise<boolean> => {
        return post<boolean>('/task/evidence/submit', { taskId, userId, imageUrl });
    },

    checkIn: async (taskId: string, userId: string, checkInInfo?: string): Promise<boolean> => {
        return post<boolean>('/task/checkin', { taskId, userId, checkInInfo });
    },

    // 获取验收详情
    getReviewDetail: async (taskId: string): Promise<TaskReviewDetail> => {
        return get<TaskReviewDetail>('/task/review/detail', { taskId });
    },

    // 确认验收
    confirmReview: async (taskId: string, userId: string, rating: number, review?: string): Promise<boolean> => {
        return post<boolean>('/task/review/confirm', { taskId, userId, rating, review });
    },

    // 提交志愿者评价
    submitVolunteerReview: async (
        taskId: string,
        publisherId: string,
        volunteerId: string,
        rating: number,
        content?: string
    ): Promise<boolean> => {
        return post<boolean>('/task/review/submit', { taskId, publisherId, volunteerId, rating, content });
    },

    // 提交申诉
    submitAppeal: async (taskId: string, userId: string, reason: string): Promise<boolean> => {
        return post<boolean>('/task/appeal/submit', { taskId, userId, reason });
    },

    // 提交申诉回应
    replyAppeal: async (taskId: string, userId: string, content: string): Promise<boolean> => {
        return post<boolean>('/task/appeal/reply', { taskId, userId, content });
    },

    // 获取申诉详情
    getAppealDetail: async (taskId: string): Promise<AppealDetail> => {
        return get<AppealDetail>('/task/appeal/detail', { taskId });
    }
};

export interface AppealDetail {
    id: string;
    taskId: string;
    proposerId: string;
    proposerName: string;
    proposerAvatar: string;
    reason: string;
    defendantName?: string;
    defendantAvatar?: string;
    defendantResponse?: string;
    responseTime?: string;
    status: number;
    createTime: string;
    handlingResult?: string;
    handlingReason?: string;
}

// 验收详情响应类型
export interface TaskReviewDetail {
    taskId: string;
    title: string;
    description: string;
    coins: number;
    status: number;
    evidencePhotos: string[];
    checkInTime: string | null;
    finishTime: string | null;
    volunteerId: string;
    volunteerName: string;
    volunteerAvatar: string;
    appeal?: {
        reason: string;
        createTime: string;
        response?: string;
        responseTime?: string;
        publisherName?: string;
        status: number;
    };
}
