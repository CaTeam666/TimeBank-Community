import { get, post } from './request';
import { Product, ExchangeOrder } from '../types';

export const mallApi = {
    // Get product list
    getProducts: (params?: { keyword?: string; category?: string }) => {
        return get<Product[]>('/mall/products', params);
    },

    // Get product detail
    getProductDetail: (id: number) => {
        return get<Product>('/mall/product/detail', { id: id.toString() });
    },

    // Exchange product (Create Order)
    exchangeProduct: (data: { userId: string; productId: number; quantity: number }) => {
        return post<{ orderNo: string; verifyCode: string }>('/mall/exchange', data);
    },

    // Get user orders with status mapping
    getUserOrders: async (userId: string) => {
        // Define the raw API response type locally
        interface ApiExchangeOrder extends Omit<ExchangeOrder, 'status' | 'redemptionCode'> {
            status: number; // 0, 1, 2
            verifyCode: string;
        }

        const rawData = await get<ApiExchangeOrder[]>('/mall/orders', { userId });

        // Map status number to string literal and verifyCode to redemptionCode
        return rawData.map(order => ({
            ...order,
            status: mapStatus(order.status),
            redemptionCode: order.verifyCode // Map backend field to frontend model
        })) as ExchangeOrder[];
    }
};

function mapStatus(status: number): ExchangeOrder['status'] {
    switch (status) {
        case 0: return 'pending_pickup';
        case 1: return 'completed';
        case 2: return 'cancelled';
        default: return 'completed'; // Fallback
    }
}
