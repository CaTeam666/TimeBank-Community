import { IdentityAuditTask, AuditStatus } from '../types';

// API响应结构
interface ApiResponse<T> {
    code: number;
    message: string;
    data: T;
}

// 列表响应数据
interface IdentityAuditListData {
    list: IdentityAuditTask[];
    total: number;
    page: number;
    pageSize: number;
}

// 列表查询参数
interface IdentityAuditListParams {
    page?: number;
    pageSize?: number;
    status?: AuditStatus;
}

// 审核操作参数
interface AuditActionParams {
    status: AuditStatus;
    rejectReason?: string;
}

// 获取token的辅助函数
const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
};

export const identityApi = {
    /**
     * 获取实名审核列表
     * @param params 查询参数
     * @returns Promise<IdentityAuditListData>
     */
    getAuditList: async (params: IdentityAuditListParams = {}): Promise<IdentityAuditListData> => {
        try {
            const queryParams = new URLSearchParams();

            if (params.page) queryParams.append('page', params.page.toString());
            if (params.pageSize) queryParams.append('pageSize', params.pageSize.toString());
            if (params.status) queryParams.append('status', params.status);

            const url = `/api/identity-audits?${queryParams.toString()}`;

            const response = await fetch(url, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<IdentityAuditListData> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.message || '获取审核列表失败');
            }
        } catch (error: any) {
            console.error('获取审核列表失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 获取实名审核详情
     * @param id 审核任务ID
     * @returns Promise<IdentityAuditTask>
     */
    getAuditDetail: async (id: string): Promise<IdentityAuditTask> => {
        try {
            const response = await fetch(`/api/identity-audits/${id}`, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<IdentityAuditTask> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.message || '获取审核详情失败');
            }
        } catch (error: any) {
            console.error('获取审核详情失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 提交审核结果
     * @param id 审核任务ID
     * @param params 审核参数
     * @returns Promise<void>
     */
    submitAuditResult: async (id: string, params: AuditActionParams): Promise<void> => {
        try {
            const response = await fetch(`/api/identity-audits/${id}/audit`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify(params)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<void> = await response.json();

            if (res.code !== 200) {
                throw new Error(res.message || '提交审核结果失败');
            }
        } catch (error: any) {
            console.error('提交审核结果失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    }
};
