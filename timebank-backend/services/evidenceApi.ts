import { ServiceEvidence } from '../types';

// API响应通用结构
interface ApiResponse<T> {
    code: number;
    msg: string;
    data: T;
}

export interface EvidenceQueryParams {
    page?: number;
    pageSize?: number;
    keyword?: string;
}

export interface EvidenceListResponse {
    total: number;
    list: ServiceEvidence[];
}

// 获取token的辅助函数
const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
};

export const evidenceApi = {
    /**
     * 获取服务存证列表
     */
    getEvidenceList: async (params: EvidenceQueryParams): Promise<EvidenceListResponse> => {
        try {
            const queryParams = new URLSearchParams();
            if (params.page) queryParams.append('page', params.page.toString());
            if (params.pageSize) queryParams.append('pageSize', params.pageSize.toString());
            if (params.keyword) queryParams.append('keyword', params.keyword);

            const response = await fetch(`/api/evidence/list?${queryParams.toString()}`, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<EvidenceListResponse> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.msg || '获取服务存证列表失败');
            }
        } catch (error: any) {
            console.error('获取服务存证列表失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    }
};
