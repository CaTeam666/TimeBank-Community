import { get } from './request';

// 消息类型枚举
export enum MessageType {
    FAMILY_BIND = 'FAMILY_BIND',
    TASK_VERIFY = 'TASK_VERIFY'
}

// 消息列表查询参数
export interface MessageListParams {
    type?: MessageType | string;
    pageNum?: number;
    pageSize?: number;
}

// 消息项
export interface MessageItem {
    id: number;
    type: string;
    typeName: string;
    bizId: number;
    title: string;
    content: string;
    route: string;
    createTime: string;
}

// 消息列表响应
export interface MessageListResponse {
    total: number;
    list: MessageItem[];
}

// 消息类型提示词映射
export const MESSAGE_TYPE_HINTS: Record<string, string> = {
    'FAMILY_BIND': '您收到一条亲情账号绑定申请,请及时查看并确认',
    'TASK_VERIFY': '您有任务待验收,请查看志愿者提交的服务凭证'
};

export const messageApi = {
    /**
     * 获取消息列表
     * @param params 查询参数
     * @returns 消息列表
     */
    getMessageList: async (params?: MessageListParams): Promise<MessageListResponse> => {
        const queryParams = new URLSearchParams();
        if (params?.type) {
            queryParams.append('type', params.type);
        }
        if (params?.pageNum) {
            queryParams.append('pageNum', params.pageNum.toString());
        }
        if (params?.pageSize) {
            queryParams.append('pageSize', params.pageSize.toString());
        }

        const query = queryParams.toString();
        const url = query ? `/message/list?${query}` : '/message/list';

        return get<MessageListResponse>(url);
    },

    /**
     * 获取未读消息数量
     * @returns 未读消息总数
     */
    getUnreadCount: async (): Promise<number> => {
        const response = await messageApi.getMessageList({ pageNum: 1, pageSize: 1 });
        return response.total;
    },

    /**
     * 获取消息类型的口语化提示词
     * @param type 消息类型
     * @param typeName 消息类型名称(后端返回)
     * @returns 口语化提示词
     */
    getMessageHint: (type: string, typeName: string): string => {
        return MESSAGE_TYPE_HINTS[type] || `您有新的${typeName}消息`;
    }
};
