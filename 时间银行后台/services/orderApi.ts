import { ExchangeOrder, OrderStatus } from '../types';

// API响应结构
interface ApiResponse<T> {
    code: number;
    message: string;
    data: T;
}

// 订单列表响应数据
interface OrderListData {
    list: ExchangeOrder[];
    total: number;
    page: number;
    pageSize: number;
}

// 订单列表查询参数
interface OrderListParams {
    page?: number;
    pageSize?: number;
    keyword?: string;
    status?: string;
}

// 获取token的辅助函数
const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
};

export const orderApi = {
    /**
     * 获取订单列表
     * @param params 查询参数
     * @returns Promise<OrderListData>
     */
    getOrders: async (params: OrderListParams = {}): Promise<OrderListData> => {
        try {
            const queryParams = new URLSearchParams();
            if (params.page) queryParams.append('page', params.page.toString());
            if (params.pageSize) queryParams.append('pageSize', params.pageSize.toString());
            if (params.keyword) queryParams.append('keyword', params.keyword);
            if (params.status && params.status !== 'ALL') queryParams.append('status', params.status);

            const response = await fetch(`/api/order/list?${queryParams.toString()}`, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<OrderListData> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.message || '获取订单列表失败');
            }
        } catch (error: any) {
            console.error('获取订单列表失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 订单核销
     * @param id 订单ID
     * @returns Promise<void>
     */
    verifyOrder: async (id: string): Promise<void> => {
        try {
            const response = await fetch('/api/order/verify', {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ id })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<void> = await response.json();

            if (res.code !== 200) {
                throw new Error(res.message || '核销失败');
            }
        } catch (error: any) {
            console.error('核销失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 取消订单
     * @param id 订单ID
     * @returns Promise<void>
     */
    cancelOrder: async (id: string): Promise<void> => {
        try {
            const response = await fetch('/api/order/cancel', {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ id })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<void> = await response.json();

            if (res.code !== 200) {
                throw new Error(res.message || '取消订单失败');
            }
        } catch (error: any) {
            console.error('取消订单失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    }
};
