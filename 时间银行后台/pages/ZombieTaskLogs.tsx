import React, { useState, useEffect } from 'react';
import { zombieApi } from '../services/zombieApi';
import { ZombieTaskLog } from '../types';
import { CheckCircle, XCircle, AlertTriangle, RefreshCw, Zap } from 'lucide-react';

export const ZombieTaskLogs: React.FC = () => {
  const [logs, setLogs] = useState<ZombieTaskLog[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const data = await zombieApi.getZombieLogs();
      setLogs(data);
    } catch (error) {
      console.error('Failed to fetch zombie logs', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  const handleCheckZombie = async () => {
    try {
      setLoading(true);
      await zombieApi.checkZombieTasks();
      alert('已触发系统检测，请稍后刷新查看结果');
      fetchLogs();
    } catch (error) {
      console.error('Check failed', error);
      alert('触发检测失败');
    } finally {
      setLoading(false);
    }
  };

  const handleRetryRefund = async (logId: string) => {
    if (!confirm('确认重新发起退款操作吗？')) return;

    try {
      await zombieApi.retryRefund(logId);
      alert('退款指令已重新发送');
      // Refresh local state or fetch again
      setLogs(prevLogs =>
        prevLogs.map(log =>
          log.id === logId ? { ...log, refundStatus: 'SUCCESS' } : log
        )
      );
    } catch (error) {
      console.error('Retry refund failed', error);
      alert('重试退款失败');
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white p-4 rounded-lg shadow-sm border border-l-4 border-l-yellow-400 border-gray-100 flex items-start justify-between">
        <div className="flex items-start">
          <AlertTriangle className="w-6 h-6 text-yellow-500 mr-3 mt-0.5" />
          <div>
            <h3 className="text-sm font-bold text-gray-900">关于僵尸任务</h3>
            <p className="text-sm text-gray-500 mt-1">
              系统每晚 00:00 自动扫描超过24小时无人接单的任务，并执行自动关闭和资金退回。此处记录了所有被系统自动清理的任务及退款状态。
            </p>
          </div>
        </div>
        <button
          onClick={handleCheckZombie}
          className="flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-yellow-600 hover:bg-yellow-700"
        >
          <Zap className="w-4 h-4 mr-2" />
          立即检测
        </button>
      </div>

      <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
        {loading ? (
          <div className="p-8 text-center text-gray-500">加载中...</div>
        ) : (
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">日志ID</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">关联任务</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">自动关闭时间</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">自动退款金额</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">退款状态</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {logs.length === 0 ? (
                <tr>
                  <td colSpan={6} className="px-6 py-8 text-center text-gray-500">暂无异常日志</td>
                </tr>
              ) : (
                logs.map((log) => (
                  <tr key={log.id} className={log.refundStatus === 'FAILURE' ? 'bg-red-50' : 'hover:bg-gray-50'}>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                      {log.id}
                    </td>
                    <td className="px-6 py-4">
                      <div className="text-sm font-medium text-gray-900">{log.taskTitle}</div>
                      <div className="text-xs text-gray-500 font-mono">ID: {log.taskId}</div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {log.closedTime}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-gray-900">
                      {log.refundAmount} 币
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {log.refundStatus === 'SUCCESS' ? (
                        <span className="flex items-center text-green-600 text-sm">
                          <CheckCircle className="w-4 h-4 mr-1" /> 成功
                        </span>
                      ) : (
                        <span className="flex items-center text-red-600 text-sm font-medium">
                          <XCircle className="w-4 h-4 mr-1" /> 失败
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      {log.refundStatus === 'FAILURE' && (
                        <button
                          onClick={() => handleRetryRefund(log.id)}
                          className="text-white bg-red-600 hover:bg-red-700 px-3 py-1 rounded text-xs flex items-center ml-auto"
                        >
                          <RefreshCw className="w-3 h-3 mr-1" /> 重试退款
                        </button>
                      )}
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};
