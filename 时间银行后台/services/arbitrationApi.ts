import { Arbitration, Task } from '../types';

// API响应通用结构
interface ApiResponse<T> {
    code: number;
    msg: string;
    data: T;
}

// 仲裁详情扩展接口 (包含任务快照)
export interface ArbitrationDetail extends Arbitration {
    taskDescription?: string;
    taskAddress?: string;
    taskDeadline?: string;
}

// 获取token的辅助函数
const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
};

export interface ArbitrationQueryParams {
    page?: number;
    pageSize?: number;
    status?: string;
    keyword?: string;
}

export interface ArbitrationListResponse {
    total: number;
    list: Arbitration[];
}

export const arbitrationApi = {
    /**
     * 获取仲裁列表
     */
    getArbitrations: async (params: ArbitrationQueryParams): Promise<ArbitrationListResponse> => {
        try {
            const queryParams = new URLSearchParams();
            if (params.page) queryParams.append('page', params.page.toString());
            if (params.pageSize) queryParams.append('pageSize', params.pageSize.toString());
            if (params.status && params.status !== 'ALL') queryParams.append('status', params.status);
            if (params.keyword) queryParams.append('keyword', params.keyword);

            const response = await fetch(`/api/arbitration/list?${queryParams.toString()}`, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<ArbitrationListResponse> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.msg || '获取仲裁列表失败');
            }
        } catch (error: any) {
            console.error('获取仲裁列表失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 获取仲裁详情
     * @param id 仲裁单ID
     */
    getArbitrationDetail: async (id: string): Promise<ArbitrationDetail> => {
        try {
            const response = await fetch(`/api/arbitration/${id}`, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<ArbitrationDetail> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.msg || '获取仲裁详情失败');
            }
        } catch (error: any) {
            console.error('获取仲裁详情失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 提交裁决
     * @param id 仲裁单ID
     * @param verdictType 裁决类型
     * @param reason 裁决理由
     */
    submitVerdict: async (
        id: string,
        verdictType: 'REJECT' | 'TO_VOLUNTEER' | 'TO_PUBLISHER',
        reason: string
    ): Promise<void> => {
        try {
            const response = await fetch('/api/arbitration/verdict', {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ id, verdictType, reason })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<void> = await response.json();

            if (res.code !== 200) {
                throw new Error(res.msg || '提交裁决失败');
            }
        } catch (error: any) {
            console.error('提交裁决失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    }
};
