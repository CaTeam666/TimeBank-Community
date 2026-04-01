import { FamilyBindingTask, AuditStatus } from '../types';

// API响应结构
interface ApiResponse<T> {
    code: number;
    message: string;
    data: T;
}

// 绑定列表响应数据
interface FamilyBindingListData {
    list: any[]; // Raw data from backend
    total: number;
}

// 绑定列表查询参数
interface FamilyBindingParams {
    page?: number;
    pageSize?: number;
    status?: number; // 0:待管理员审核, 1:待老人确认, 2:已绑定, 3:已拒绝
    keyword?: string;
}

// 获取token的辅助函数
const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
};

export const familyApi = {
    /**
     * 获取亲情绑定申请列表
     * @param params 查询参数
     * @returns Promise<{ list: FamilyBindingTask[], total: number }>
     */
    getFamilyBindingList: async (params: FamilyBindingParams = {}): Promise<{ list: FamilyBindingTask[], total: number }> => {
        try {
            const queryParams = new URLSearchParams();
            if (params.page) queryParams.append('page', params.page.toString());
            if (params.pageSize) queryParams.append('pageSize', params.pageSize.toString());
            if (typeof params.status === 'number') queryParams.append('status', params.status.toString());
            if (params.keyword) queryParams.append('keyword', params.keyword);

            const url = `/api/user-relations?${queryParams.toString()}`;

            const response = await fetch(url, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<FamilyBindingListData> = await response.json();

            if (res.code === 200) {
                // Map backend data to frontend model
                const mappedList: FamilyBindingTask[] = res.data.list.map(item => ({
                    serialNo: item.id.toString(), // Assuming ID is the serial number
                    childName: item.childName,
                    childPhone: item.childPhone,
                    elderName: item.parentName,
                    elderPhone: item.parentPhone,
                    proofImages: item.proofImg ? item.proofImg.split(',') : [],
                    applyTime: item.createTime,
                    status: mapStatus(item.status),
                    rejectReason: item.rejectReason
                }));

                return {
                    list: mappedList,
                    total: res.data.total
                };
            } else {
                throw new Error(res.message || '获取绑定列表失败');
            }
        } catch (error: any) {
            console.error('获取绑定列表失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 审核亲情绑定申请
     * @param id 申请ID (serialNo)
     * @param status 审核结果 (APPROVED/REJECTED)
     * @param rejectReason 拒绝原因
     * @returns Promise<void>
     */
    auditFamilyBinding: async (id: string, status: AuditStatus, rejectReason?: string): Promise<void> => {
        try {
            const apiStatus = status === AuditStatus.APPROVED ? 1 : 2;

            const response = await fetch(`/api/user-relations/${id}/audit`, {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({
                    status: apiStatus,
                    rejectReason
                })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<void> = await response.json();

            if (res.code !== 200) {
                throw new Error(res.message || '审核操作失败');
            }
        } catch (error: any) {
            console.error('审核操作失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    }
};

// Helper: Map backend status number to AuditStatus enum
function mapStatus(status: number): AuditStatus {
    switch (status) {
        case 1:
        case 2:
            return AuditStatus.APPROVED;
        case 3:
            return AuditStatus.REJECTED;
        default:
            return AuditStatus.PENDING;
    }
}
