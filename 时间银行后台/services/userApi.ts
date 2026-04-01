import { User, UserRole, AccountStatus } from '../types';

// API响应结构
interface ApiResponse<T> {
  code: number;
  message: string;
  data: T;
}

// 用户列表响应数据
interface UserListData {
  list: User[];
  total: number;
  page: number;
  pageSize: number;
}

// 用户列表查询参数
interface UserListParams {
  page?: number;
  pageSize?: number;
  keyword?: string;
  role?: string;
  status?: string;
}

// 余额调整参数
interface BalanceAdjustmentParams {
  amount: number;
  reason?: string;
}

// 获取token的辅助函数
const getAuthHeaders = (): HeadersInit => {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token ? { 'Authorization': `Bearer ${token}` } : {})
  };
};

export const userApi = {
  /**
   * 获取用户列表
   * @param params 查询参数
   * @returns Promise<UserListData>
   */
  getUserList: async (params: UserListParams = {}): Promise<UserListData> => {
    try {
      const queryParams = new URLSearchParams();
      
      if (params.page) queryParams.append('page', params.page.toString());
      if (params.pageSize) queryParams.append('pageSize', params.pageSize.toString());
      if (params.keyword) queryParams.append('keyword', params.keyword);
      if (params.role && params.role !== 'ALL') queryParams.append('role', params.role);
      if (params.status && params.status !== 'ALL') queryParams.append('status', params.status);

      const url = `/api/users?${queryParams.toString()}`;
      
      const response = await fetch(url, {
        method: 'GET',
        headers: getAuthHeaders()
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const res: ApiResponse<UserListData> = await response.json();

      if (res.code === 200) {
        return res.data;
      } else {
        throw new Error(res.message || '获取用户列表失败');
      }
    } catch (error: any) {
      console.error('获取用户列表失败:', error);
      throw new Error(error.message || '网络请求失败');
    }
  },

  /**
   * 获取用户详情
   * @param id 用户ID
   * @returns Promise<User>
   */
  getUserDetail: async (id: string): Promise<User> => {
    try {
      const response = await fetch(`/api/users/${id}`, {
        method: 'GET',
        headers: getAuthHeaders()
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const res: ApiResponse<User> = await response.json();

      if (res.code === 200) {
        return res.data;
      } else {
        throw new Error(res.message || '获取用户详情失败');
      }
    } catch (error: any) {
      console.error('获取用户详情失败:', error);
      throw new Error(error.message || '网络请求失败');
    }
  },

  /**
   * 更新用户状态
   * @param id 用户ID
   * @param status 目标状态
   * @returns Promise<void>
   */
  updateUserStatus: async (id: string, status: AccountStatus): Promise<void> => {
    try {
      const response = await fetch(`/api/users/${id}/status`, {
        method: 'PATCH',
        headers: getAuthHeaders(),
        body: JSON.stringify({ status })
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const res: ApiResponse<void> = await response.json();

      if (res.code !== 200) {
        throw new Error(res.message || '更新用户状态失败');
      }
    } catch (error: any) {
      console.error('更新用户状态失败:', error);
      throw new Error(error.message || '网络请求失败');
    }
  },

  /**
   * 调整用户余额
   * @param id 用户ID
   * @param params 调整参数
   * @returns Promise<{ currentBalance: number }>
   */
  adjustUserBalance: async (
    id: string, 
    params: BalanceAdjustmentParams
  ): Promise<{ currentBalance: number }> => {
    try {
      const response = await fetch(`/api/users/${id}/balance-adjustment`, {
        method: 'POST',
        headers: getAuthHeaders(),
        body: JSON.stringify(params)
      });

      if (!response.ok) {
        throw new Error(`HTTP error! status: ${response.status}`);
      }

      const res: ApiResponse<{ currentBalance: number }> = await response.json();

      if (res.code === 200) {
        return res.data;
      } else {
        throw new Error(res.message || '调整用户余额失败');
      }
    } catch (error: any) {
      console.error('调整用户余额失败:', error);
      throw new Error(error.message || '网络请求失败');
    }
  }
};
