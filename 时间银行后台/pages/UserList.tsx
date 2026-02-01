import React, { useState, useEffect } from 'react';
import { Search, RotateCw, Eye, Coins, Lock, Unlock } from 'lucide-react';
import { User, UserRole, AccountStatus } from '../types';
import { userApi } from '../services/userApi';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';

export const UserList: React.FC = () => {
  const [users, setUsers] = useState<User[]>([]);
  const [filters, setFilters] = useState({
    keyword: '',
    role: 'ALL',
    status: 'ALL'
  });

  // 新增状态：加载、错误、分页
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [pagination, setPagination] = useState({
    total: 0,
    page: 1,
    pageSize: 10
  });

  // Action States
  const [selectedUser, setSelectedUser] = useState<User | null>(null);
  const [isDetailDrawerOpen, setIsDetailDrawerOpen] = useState(false);
  const [isBalanceModalOpen, setIsBalanceModalOpen] = useState(false);
  const [balanceAdjustment, setBalanceAdjustment] = useState(0);
  const [balanceReason, setBalanceReason] = useState('');

  // 获取用户列表
  const fetchUsers = async () => {
    try {
      setLoading(true);
      setError(null);

      const data = await userApi.getUserList({
        page: pagination.page,
        pageSize: pagination.pageSize,
        keyword: filters.keyword || undefined,
        role: filters.role !== 'ALL' ? filters.role as UserRole : undefined,
        status: filters.status !== 'ALL' ? filters.status as AccountStatus : undefined
      });

      setUsers(data.list);
      setPagination({
        total: data.total,
        page: data.page,
        pageSize: data.pageSize
      });
    } catch (err: any) {
      setError(err.message || '获取用户列表失败');
      console.error('Failed to fetch users:', err);
    } finally {
      setLoading(false);
    }
  };

  // 组件加载时获取数据
  useEffect(() => {
    fetchUsers();
  }, [pagination.page, pagination.pageSize]);

  const handleSearch = () => {
    // 搜索时重置到第一页
    setPagination({ ...pagination, page: 1 });
    fetchUsers();
  };

  const handleReset = () => {
    setFilters({ keyword: '', role: 'ALL', status: 'ALL' });
    setPagination({ ...pagination, page: 1 });
    // 重置后立即获取数据
    setTimeout(() => fetchUsers(), 0);
  };

  const toggleStatus = async (id: string) => {
    try {
      const user = users.find(u => u.id === id);
      if (!user) return;

      const newStatus = user.status === AccountStatus.NORMAL
        ? AccountStatus.FROZEN
        : AccountStatus.NORMAL;

      await userApi.updateUserStatus(id, newStatus);

      // 更新成功后刷新列表
      fetchUsers();
    } catch (err: any) {
      alert(`操作失败: ${err.message}`);
    }
  };

  const handleBalanceAdjust = async () => {
    if (!selectedUser || balanceAdjustment === 0) return;

    try {
      const result = await userApi.adjustUserBalance(
        selectedUser.id,
        { amount: balanceAdjustment, reason: balanceReason || undefined }
      );

      setIsBalanceModalOpen(false);
      setBalanceAdjustment(0);
      setBalanceReason('');

      alert(`余额调整成功！当前余额: ${result.currentBalance}`);

      // 刷新列表
      fetchUsers();
    } catch (err: any) {
      alert(`余额调整失败: ${err.message}`);
    }
  };

  const getRoleBadge = (role: UserRole) => {
    switch (role) {
      case UserRole.ELDER: return <Badge color="elder-red">老人 (夕阳红)</Badge>;
      case UserRole.VOLUNTEER: return <Badge color="volunteer-green">志愿者</Badge>;
      case UserRole.CHILD_AGENT: return <Badge color="child-blue">子女代理人</Badge>;
      default: return <Badge color="gray">{role}</Badge>;
    }
  };

  const getStatusText = (status: AccountStatus) => {
    return status === AccountStatus.NORMAL ? '正常' : '冻结';
  };

  return (
    <div className="space-y-6">
      {/* Search Bar */}
      <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-100 flex flex-wrap gap-4 items-end">
        <div className="w-64">
          <label className="block text-sm font-medium text-gray-700 mb-1">关键词搜索</label>
          <div className="relative">
            <input
              type="text"
              placeholder="昵称 / 手机号 / 真实姓名"
              className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 text-sm"
              value={filters.keyword}
              onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
            />
            <Search className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
          </div>
        </div>

        <div className="w-48">
          <label className="block text-sm font-medium text-gray-700 mb-1">用户角色</label>
          <select
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 text-sm"
            value={filters.role}
            onChange={(e) => setFilters({ ...filters, role: e.target.value })}
          >
            <option value="ALL">全部角色</option>
            <option value={UserRole.ELDER}>老人</option>
            <option value={UserRole.VOLUNTEER}>志愿者</option>
            <option value={UserRole.CHILD_AGENT}>子女代理人</option>
          </select>
        </div>

        <div className="w-48">
          <label className="block text-sm font-medium text-gray-700 mb-1">账号状态</label>
          <select
            className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 text-sm"
            value={filters.status}
            onChange={(e) => setFilters({ ...filters, status: e.target.value })}
          >
            <option value="ALL">全部状态</option>
            <option value={AccountStatus.NORMAL}>正常</option>
            <option value={AccountStatus.FROZEN}>冻结</option>
          </select>
        </div>

        <div className="flex gap-2 pb-0.5">
          <button onClick={handleSearch} className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm font-medium">查询</button>
          <button onClick={handleReset} className="px-4 py-2 bg-white border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 text-sm font-medium">重置</button>
        </div>
      </div>

      {/* 错误提示 */}
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg">
          <p className="text-sm">{error}</p>
        </div>
      )}

      {/* 加载提示 */}
      {loading && (
        <div className="bg-blue-50 border border-blue-200 text-blue-700 px-4 py-3 rounded-lg">
          <p className="text-sm">正在加载用户数据...</p>
        </div>
      )}

      {/* Data Table */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">用户</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">角色</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">积分余额</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">注册时间</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {users.map((user) => (
              <tr key={user.id} className="hover:bg-gray-50 transition-colors">
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center">
                    <div className="flex-shrink-0 h-10 w-10">
                      <img className="h-10 w-10 rounded-full object-cover" src={user.avatar} alt="" />
                    </div>
                    <div className="ml-4">
                      <div className="text-sm font-medium text-gray-900">{user.realName} <span className="text-gray-400 text-xs">({user.nickname})</span></div>
                      <div className="text-sm text-gray-500">{user.phone}</div>
                      <div className="text-xs text-gray-400">ID: {user.id}</div>
                    </div>
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {getRoleBadge(user.role)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="flex items-center text-sm text-gray-900 font-mono">
                    <Coins className="w-4 h-4 text-yellow-500 mr-1.5" />
                    {user.balance}
                  </div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {user.registerTime}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <span className={`px-2 inline-flex text-xs leading-5 font-semibold rounded-full ${user.status === AccountStatus.NORMAL ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'}`}>
                    {getStatusText(user.status)}
                  </span>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  <div className="flex items-center justify-end gap-3">
                    <button
                      onClick={() => { setSelectedUser(user); setIsDetailDrawerOpen(true); }}
                      className="text-gray-400 hover:text-blue-600" title="查看详情">
                      <Eye className="w-5 h-5" />
                    </button>
                    <button
                      onClick={() => { setSelectedUser(user); setIsBalanceModalOpen(true); }}
                      className="text-gray-400 hover:text-yellow-600" title="资金调整">
                      <RotateCw className="w-5 h-5" />
                    </button>
                    <button
                      onClick={() => toggleStatus(user.id)}
                      className={`${user.status === AccountStatus.NORMAL ? 'text-green-500 hover:text-red-600' : 'text-red-500 hover:text-green-600'}`}
                      title={user.status === AccountStatus.NORMAL ? "冻结账号" : "解冻账号"}
                    >
                      {user.status === AccountStatus.NORMAL ? <Unlock className="w-5 h-5" /> : <Lock className="w-5 h-5" />}
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>

        {/* 分页控件 */}
        <div className="px-6 py-4 bg-gray-50 border-t border-gray-200 flex items-center justify-between">
          <div className="text-sm text-gray-700">
            共 <span className="font-medium">{pagination.total}</span> 条记录，
            当前第 <span className="font-medium">{pagination.page}</span> 页
          </div>
          <div className="flex gap-2">
            <button
              onClick={() => setPagination({ ...pagination, page: pagination.page - 1 })}
              disabled={pagination.page === 1 || loading}
              className="px-3 py-1 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              上一页
            </button>
            <button
              onClick={() => setPagination({ ...pagination, page: pagination.page + 1 })}
              disabled={pagination.page * pagination.pageSize >= pagination.total || loading}
              className="px-3 py-1 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              下一页
            </button>
          </div>
        </div>
      </div>

      {/* Balance Adjustment Modal */}
      <Modal
        isOpen={isBalanceModalOpen}
        onClose={() => setIsBalanceModalOpen(false)}
        title="调整用户资金"
        size="sm"
      >
        <div className="space-y-4">
          <div className="bg-gray-50 p-3 rounded-md mb-4">
            <p className="text-sm text-gray-500">目标用户: <span className="font-semibold text-gray-900">{selectedUser?.realName}</span></p>
            <p className="text-sm text-gray-500">当前余额: <span className="font-mono font-semibold text-gray-900">{selectedUser?.balance}</span></p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">调整金额 (+/-)</label>
            <input
              type="number"
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              placeholder="-50 或 50"
              value={balanceAdjustment}
              onChange={(e) => setBalanceAdjustment(parseInt(e.target.value) || 0)}
            />
            <p className="mt-1 text-xs text-gray-500">正数增加，负数扣除。</p>
          </div>
          <div>
            <label className="block text-sm font-medium text-gray-700">调整原因 (可选)</label>
            <input
              type="text"
              className="mt-1 block w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
              placeholder="例如：参与活动奖励"
              value={balanceReason}
              onChange={(e) => setBalanceReason(e.target.value)}
            />
          </div>
          <div className="flex justify-end gap-3 mt-6">
            <button
              onClick={() => setIsBalanceModalOpen(false)}
              className="px-4 py-2 border border-gray-300 rounded-md text-sm font-medium text-gray-700 hover:bg-gray-50"
            >
              取消
            </button>
            <button
              onClick={handleBalanceAdjust}
              className="px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-blue-600 hover:bg-blue-700"
            >
              确认调整
            </button>
          </div>
        </div>
      </Modal>

      {/* User Detail Drawer (Simulated with fixed positioned div) */}
      {isDetailDrawerOpen && selectedUser && (
        <div className="fixed inset-0 overflow-hidden z-50">
          <div className="absolute inset-0 overflow-hidden">
            <div className="absolute inset-0 bg-gray-500 bg-opacity-75 transition-opacity" onClick={() => setIsDetailDrawerOpen(false)}></div>
            <section className="absolute inset-y-0 right-0 pl-10 max-w-full flex">
              <div className="w-screen max-w-md">
                <div className="h-full flex flex-col bg-white shadow-xl overflow-y-scroll">
                  <div className="py-6 px-4 bg-blue-600 sm:px-6">
                    <div className="flex items-center justify-between">
                      <h2 className="text-lg font-medium text-white">用户档案</h2>
                      <button onClick={() => setIsDetailDrawerOpen(false)} className="text-blue-200 hover:text-white">
                        <span className="sr-only">关闭面板</span>
                        <svg className="h-6 w-6" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                    </div>
                    <div className="mt-4 flex items-center">
                      <img src={selectedUser.avatar} alt="" className="h-16 w-16 rounded-full border-2 border-white" />
                      <div className="ml-4">
                        <h3 className="text-xl font-bold text-white">{selectedUser.realName}</h3>
                        <p className="text-blue-100">{selectedUser.phone}</p>
                      </div>
                    </div>
                  </div>
                  <div className="relative flex-1 py-6 px-4 sm:px-6">
                    <dl className="grid grid-cols-1 gap-x-4 gap-y-8 sm:grid-cols-2">
                      <div className="sm:col-span-1">
                        <dt className="text-sm font-medium text-gray-500">用户 ID</dt>
                        <dd className="mt-1 text-sm text-gray-900">{selectedUser.id}</dd>
                      </div>
                      <div className="sm:col-span-1">
                        <dt className="text-sm font-medium text-gray-500">昵称</dt>
                        <dd className="mt-1 text-sm text-gray-900">{selectedUser.nickname}</dd>
                      </div>
                      <div className="sm:col-span-1">
                        <dt className="text-sm font-medium text-gray-500">角色</dt>
                        <dd className="mt-1 text-sm text-gray-900">{getRoleBadge(selectedUser.role)}</dd>
                      </div>
                      <div className="sm:col-span-1">
                        <dt className="text-sm font-medium text-gray-500">当前余额</dt>
                        <dd className="mt-1 text-sm font-bold text-gray-900">{selectedUser.balance} 积分</dd>
                      </div>
                      <div className="sm:col-span-2">
                        <dt className="text-sm font-medium text-gray-500">注册时间</dt>
                        <dd className="mt-1 text-sm text-gray-900">{selectedUser.registerTime}</dd>
                      </div>
                    </dl>
                  </div>
                </div>
              </div>
            </section>
          </div>
        </div>
      )}
    </div>
  );
};