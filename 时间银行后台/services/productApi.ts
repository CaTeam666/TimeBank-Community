import { Product } from '../types';

// API响应结构
interface ApiResponse<T> {
    code: number;
    message: string;
    data: T;
}

// 获取token的辅助函数
const getAuthHeaders = (): HeadersInit => {
    const token = localStorage.getItem('token');
    return {
        'Content-Type': 'application/json',
        ...(token ? { 'Authorization': `Bearer ${token}` } : {})
    };
};

export const productApi = {
    /**
     * 获取商品列表
     * @returns Promise<Product[]>
     */
    getProducts: async (): Promise<Product[]> => {
        try {
            const response = await fetch('/api/product/list', {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<Product[]> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.message || '获取商品列表失败');
            }
        } catch (error: any) {
            console.error('获取商品列表失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 获取商品详情
     * @param id 商品ID
     * @returns Promise<Product>
     */
    getProductDetail: async (id: string): Promise<Product> => {
        try {
            const response = await fetch(`/api/product/${id}`, {
                method: 'GET',
                headers: getAuthHeaders()
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<Product> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.message || '获取商品详情失败');
            }
        } catch (error: any) {
            console.error('获取商品详情失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 创建商品
     * @param product 商品数据
     * @returns Promise<Product>
     */
    createProduct: async (product: Partial<Product>): Promise<Product> => {
        try {
            const response = await fetch('/api/product/create', {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify(product)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<Product> = await response.json();

            if (res.code === 200) {
                return res.data;
            } else {
                throw new Error(res.message || '创建商品失败');
            }
        } catch (error: any) {
            console.error('创建商品失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 更新商品
     * @param product 商品数据
     * @returns Promise<void>
     */
    updateProduct: async (product: Partial<Product>): Promise<void> => {
        try {
            const response = await fetch('/api/product/update', {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify(product)
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<void> = await response.json();

            if (res.code !== 200) {
                throw new Error(res.message || '更新商品失败');
            }
        } catch (error: any) {
            console.error('更新商品失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 更新商品状态（上下架）
     * @param id 商品ID
     * @param status 状态
     * @returns Promise<void>
     */
    updateProductStatus: async (id: string, status: 'ON_SHELF' | 'OFF_SHELF'): Promise<void> => {
        try {
            const response = await fetch('/api/product/status', {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ id, status })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<void> = await response.json();

            if (res.code !== 200) {
                throw new Error(res.message || '更新商品状态失败');
            }
        } catch (error: any) {
            console.error('更新商品状态失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    },

    /**
     * 删除商品
     * @param id 商品ID
     * @returns Promise<void>
     */
    deleteProduct: async (id: string): Promise<void> => {
        try {
            const response = await fetch('/api/product/delete', {
                method: 'POST',
                headers: getAuthHeaders(),
                body: JSON.stringify({ id })
            });

            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }

            const res: ApiResponse<void> = await response.json();

            if (res.code !== 200) {
                throw new Error(res.message || '删除商品失败');
            }
        } catch (error: any) {
            console.error('删除商品失败:', error);
            throw new Error(error.message || '网络请求失败');
        }
    }
};
