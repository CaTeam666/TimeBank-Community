import { get, post } from './request';
import { FamilyMember } from '../types';

export interface BindFamilyRequest {
    phone: string;
    relation: string;
    proofImg: string; // 证明材料图片URL列表，多个以逗号分隔，要求至少2张
}

export interface UnbindFamilyRequest {
    relationId: number;
}

export interface ToggleProxyRequest {
    parentId?: string;
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
        return get<FamilyMember[]>('/family/list', undefined, true);
    },

    // 申请绑定亲情账号
    bindFamily: async (params: BindFamilyRequest): Promise<{ relationId: number }> => {
        return post<{ relationId: number }>('/family/bind', params, true);
    },

    // 解绑亲情账号
    unbindFamily: async (params: UnbindFamilyRequest): Promise<void> => {
        return post<void>('/family/unbind', params, true);
    },

    // 切换代理模式
    toggleProxyMode: async (params: ToggleProxyRequest): Promise<{ proxyToken?: string }> => {
        return post<{ proxyToken?: string }>('/family/proxy/toggle', params, true);
    },

    // 获取待审核的绑定申请
    getPendingRequests: async (): Promise<PendingRequestsResponse> => {
        return get<PendingRequestsResponse>('/family/pending-requests', undefined, true);
    },

    // 审核绑定申请
    reviewBinding: async (params: ReviewBindingRequest): Promise<void> => {
        return post<void>('/family/review', params, true);
    }
};
