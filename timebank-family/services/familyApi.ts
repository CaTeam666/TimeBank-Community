import { get, post } from './request';
import { FamilyMember } from '../types';

export interface BindFamilyRequest {
    phone: string;
    relation: string;
    proofImg?: string;
}

export interface UnbindFamilyRequest {
    relationId: number;
}

export interface ToggleProxyRequest {
    parentId?: number;
    enable: boolean;
}

export interface PendingRequestItem {
    relationId: number;
    childId: number;
    childName: string;
    childAvatar: string;
    childPhone: string;
    relation: string;
    proofImg?: string;
    createTime: string;
}

export interface PendingRequestsResponse {
    total: number;
    requests: PendingRequestItem[];
}

export interface ReviewBindingRequest {
    relationId: number;
    action: 'approve' | 'reject';
    rejectReason?: string;
}


export const familyApi = {
    // 获取亲情账号列表
    getFamilyList: async (): Promise<FamilyMember[]> => {
        return get<FamilyMember[]>('/family/list');
    },

    // 申请绑定亲情账号
    bindFamily: async (params: BindFamilyRequest): Promise<{ relationId: number }> => {
        return post<{ relationId: number }>('/family/bind', params);
    },

    // 解绑亲情账号
    unbindFamily: async (params: UnbindFamilyRequest): Promise<void> => {
        return post<void>('/family/unbind', params);
    },

    // 切换代理模式
    toggleProxyMode: async (params: ToggleProxyRequest): Promise<{ proxyToken?: string }> => {
        return post<{ proxyToken?: string }>('/family/proxy/toggle', params);
    },

    // 获取待审核的绑定申请
    getPendingRequests: async (): Promise<PendingRequestsResponse> => {
        return get<PendingRequestsResponse>('/family/pending-requests');
    },

    // 审核绑定申请
    reviewBinding: async (params: ReviewBindingRequest): Promise<void> => {
        return post<void>('/family/review', params);
    }
};
