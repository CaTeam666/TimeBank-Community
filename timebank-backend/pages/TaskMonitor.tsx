import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Search, Eye, XCircle, Coins, AlertOctagon } from 'lucide-react';
import { Task, TaskStatus, TaskType } from '../types';
import { Badge } from '../components/ui/Badge';
import { missionMonitorApi } from '../services/missionMonitorApi';
import { Modal } from '../components/ui/Modal';

export const TaskMonitor: React.FC = () => {
  const [tasks, setTasks] = useState<Task[]>([]);
  const [loading, setLoading] = useState(false);
  const [total, setTotal] = useState(0);
  const [filters, setFilters] = useState({
    keyword: '',
    status: 'ALL',
    type: 'ALL',
    date: '',
    page: 1,
    pageSize: 10
  });

  const [closeTaskModalOpen, setCloseTaskModalOpen] = useState(false);
  const [selectedTask, setSelectedTask] = useState<Task | null>(null);
  const [closeReason, setCloseReason] = useState('');

  // Fetch tasks when filters change
  const fetchTasks = async () => {
    setLoading(true);
    try {
      const res = await missionMonitorApi.getTasks({
        keyword: filters.keyword,
        status: filters.status,
        type: filters.type,
        date: filters.date,
        page: filters.page,
        pageSize: filters.pageSize
      });
      setTasks(res.list || []);
      setTotal(res.total);
    } catch (error) {
      console.error('Failed to fetch tasks:', error);
      alert('获取任务列表失败，请稍后重试');
    } finally {
      setLoading(false);
    }
  };

  React.useEffect(() => {
    fetchTasks();
  }, [filters.page, filters.pageSize]);
  // Note: We don't auto-fetch on filter inputs to avoid too many requests while typing, 
  // but we do for pagination. For filters, we rely on the Search button or specific events.

  const handleSearch = () => {
    setFilters(prev => ({ ...prev, page: 1 })); // Reset to page 1 on search
    fetchTasks();
  };

  const handleReset = () => {
    setFilters({
      keyword: '',
      status: 'ALL',
      type: 'ALL',
      date: '',
      page: 1,
      pageSize: 10
    });
    // Need to trigger fetch after state update, but state update is async.
    // simpler way is to just call fetch param directly or use effect dependency carefully.
    // Here we can rely on next effect or force call.
    // Let's manually trigger fetch with initial params for immediate feedback
    missionMonitorApi.getTasks({ page: 1, pageSize: 10 }).then(res => {
      setTasks(res.list || []);
      setTotal(res.total);
    });
  };

  const openCloseModal = (task: Task) => {
    setSelectedTask(task);
    setCloseTaskModalOpen(true);
  };

  const handleForceClose = async () => {
    if (selectedTask) {
      try {
        await missionMonitorApi.forceCloseTask(selectedTask.id, closeReason);
        alert('任务已强制关闭，资金已自动退回发布者账户。');
        setCloseTaskModalOpen(false);
        setCloseReason('');
        setSelectedTask(null);
        fetchTasks(); // Refresh list
      } catch (error) {
        console.error('Failed to force close task:', error);
        alert('强制关闭失败');
      }
    }
  };

  const getStatusBadge = (status: TaskStatus) => {
    switch (status) {
      case TaskStatus.COMPLETED: return <Badge color="green">已完成</Badge>;
      case TaskStatus.IN_PROGRESS: return <Badge color="blue">进行中</Badge>;
      case TaskStatus.PENDING: return <Badge color="yellow">待接单</Badge>;
      case TaskStatus.WAITING_ACCEPTANCE: return <Badge color="blue">待验收</Badge>;
      case TaskStatus.EXPIRED: return <Badge color="gray">已过期</Badge>;
      case TaskStatus.COMPLAINT: return <Badge color="red">申诉中</Badge>;
      case TaskStatus.CANCELLED: return <Badge color="gray">已取消</Badge>;
      default: return <Badge color="gray">{status}</Badge>;
    }
  };

  const getTypeLabel = (type: TaskType) => {
    switch (type) {
      case TaskType.CHAT: return '陪聊';
      case TaskType.CLEANING: return '保洁';
      case TaskType.ERRAND: return '跑腿';
      case TaskType.MEDICAL: return '医疗陪护';
      default: return type;
    }
  };

  return (
    <div className="space-y-6">
      {/* Advanced Filter Bar */}
      <div className="bg-white p-5 rounded-lg shadow-sm border border-gray-100">
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 items-end">
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">关键词搜索</label>
            <div className="relative">
              <input
                type="text"
                placeholder="任务ID / 标题 / 发布人手机"
                className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 text-sm"
                value={filters.keyword}
                onChange={(e) => setFilters({ ...filters, keyword: e.target.value })}
              />
              <Search className="w-4 h-4 text-gray-400 absolute left-3 top-2.5" />
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">任务状态</label>
            <select
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 text-sm"
              value={filters.status}
              onChange={(e) => setFilters({ ...filters, status: e.target.value })}
            >
              <option value="ALL">全部状态</option>
              <option value={TaskStatus.PENDING}>待接单</option>
              <option value={TaskStatus.IN_PROGRESS}>进行中</option>
              <option value={TaskStatus.WAITING_ACCEPTANCE}>待验收</option>
              <option value={TaskStatus.COMPLETED}>已完成</option>
              <option value={TaskStatus.EXPIRED}>已过期</option>
              <option value={TaskStatus.COMPLAINT}>申诉中</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">任务类型</label>
            <select
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 text-sm"
              value={filters.type}
              onChange={(e) => setFilters({ ...filters, type: e.target.value })}
            >
              <option value="ALL">全部类型</option>
              <option value={TaskType.CHAT}>陪聊</option>
              <option value={TaskType.CLEANING}>保洁</option>
              <option value={TaskType.ERRAND}>跑腿</option>
              <option value={TaskType.MEDICAL}>医疗陪护</option>
            </select>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">发布时间</label>
            <input
              type="date"
              className="w-full px-3 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500 text-sm"
              value={filters.date}
              onChange={(e) => setFilters({ ...filters, date: e.target.value })}
            />
          </div>
        </div>

        <div className="flex gap-2 mt-4 justify-end">
          <button onClick={handleReset} className="px-4 py-2 bg-white border border-gray-300 text-gray-700 rounded-md hover:bg-gray-50 text-sm font-medium">重置条件</button>
          <button onClick={handleSearch} className="px-6 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 text-sm font-medium shadow-sm">查询任务</button>
        </div>
      </div>

      {/* Data Table */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
        {loading && <div className="p-4 text-center text-gray-500">加载中...</div>}
        {!loading && (
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">任务 ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">标题 / 类型</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">发布人</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">接单志愿者</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">任务积分</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">时间信息</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {tasks.map((task) => (
                <tr key={task.id} className="hover:bg-gray-50 transition-colors">
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                    {task.id}
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-sm font-medium text-gray-900 truncate max-w-[200px]" title={task.title}>{task.title}</div>
                    <div className="text-xs text-blue-600 mt-1 inline-block bg-blue-50 px-1.5 py-0.5 rounded">{getTypeLabel(task.type)}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="text-sm text-gray-900">{task.creatorName}</div>
                    <div className="text-xs text-gray-400">{task.creatorRealName}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                    {task.volunteerName ? (
                      <span className="text-gray-900 font-medium">{task.volunteerName}</span>
                    ) : (
                      <span className="text-gray-300">-</span>
                    )}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    <div className="flex items-center text-sm font-bold text-orange-600">
                      <Coins className="w-4 h-4 mr-1 text-orange-500" />
                      {task.coins}
                    </div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-xs text-gray-500">
                    <div><span className="text-gray-400">发:</span> {task.publishTime?.split(' ')[0] || '-'}</div>
                    <div><span className="text-gray-400">止:</span> {task.deadline?.split(' ')[0] || '-'}</div>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap">
                    {getStatusBadge(task.status)}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div className="flex items-center justify-end gap-2">
                      <Link
                        to={`/task/detail/${task.id}`}
                        className="text-blue-600 hover:text-blue-900 flex items-center"
                      >
                        <Eye className="w-4 h-4 mr-1" /> 详情
                      </Link>
                      {(task.status === TaskStatus.PENDING || task.status === TaskStatus.IN_PROGRESS) && (
                        <button
                          onClick={() => openCloseModal(task)}
                          className="text-red-600 hover:text-red-900 flex items-center ml-2"
                        >
                          <XCircle className="w-4 h-4 mr-1" /> 强制关闭
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
              {tasks.length === 0 && (
                <tr>
                  <td colSpan={8} className="px-6 py-10 text-center text-gray-500">
                    暂无任务数据
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        )}

        {/* Pagination - Simple implementation */}
        <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
          <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
            <div>
              <p className="text-sm text-gray-700">
                显示第 <span className="font-medium">{(filters.page - 1) * filters.pageSize + 1}</span> 到 <span className="font-medium">{Math.min(filters.page * filters.pageSize, total)}</span> 条，
                共 <span className="font-medium">{total}</span> 条
              </p>
            </div>
            <div>
              <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                <button
                  onClick={() => setFilters(prev => ({ ...prev, page: Math.max(1, prev.page - 1) }))}
                  disabled={filters.page === 1}
                  className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:bg-gray-100 disabled:cursor-not-allowed"
                >
                  上一页
                </button>
                <button
                  onClick={() => setFilters(prev => ({ ...prev, page: prev.page + 1 }))}
                  disabled={filters.page * filters.pageSize >= total}
                  className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:bg-gray-100 disabled:cursor-not-allowed"
                >
                  下一页
                </button>
              </nav>
            </div>
          </div>
        </div>

      </div>

      {/* Force Close Modal */}
      <Modal
        isOpen={closeTaskModalOpen}
        onClose={() => setCloseTaskModalOpen(false)}
        title="强制关闭任务警告"
        size="md"
      >
        <div className="text-center">
          <AlertOctagon className="w-12 h-12 text-red-500 mx-auto mb-4" />
          <p className="text-gray-700 font-medium mb-2">您正在进行高风险操作：强制关闭任务 {selectedTask?.id}</p>
          <p className="text-sm text-gray-500 mb-4">强制关闭后，已冻结的资金将自动全额退回给发布者。此操作不可逆。</p>

          <div className="text-left bg-gray-50 p-3 rounded mb-4">
            <label className="block text-sm font-medium text-gray-700 mb-1">操作理由 (必填)</label>
            <textarea
              className="w-full border border-gray-300 rounded p-2 text-sm focus:border-red-500 focus:ring-red-500"
              placeholder="请输入强制关闭的原因..."
              rows={3}
              value={closeReason}
              onChange={(e) => setCloseReason(e.target.value)}
            ></textarea>
          </div>

          <div className="flex justify-center gap-4">
            <button
              onClick={() => setCloseTaskModalOpen(false)}
              className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
            >
              取消
            </button>
            <button
              onClick={handleForceClose}
              disabled={!closeReason.trim()}
              className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              确认强制关闭
            </button>
          </div>
        </div>
      </Modal>
    </div>
  );
};
