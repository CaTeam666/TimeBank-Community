import React, { useState, useEffect } from 'react';
import { rankingApi } from '../services/rankingApi';
import { RankingLog } from '../types';
import { Trophy, RefreshCw, CheckCircle, XCircle, AlertTriangle, Medal } from 'lucide-react';
import { Badge } from '../components/ui/Badge';

export const RankingLogPage: React.FC = () => {
  const [logs, setLogs] = useState<RankingLog[]>([]);
  const [loading, setLoading] = useState(false);

  const fetchLogs = async () => {
    setLoading(true);
    try {
      const data = await rankingApi.getLogs({ page: 1, pageSize: 10 });
      // Sort by orderCount descending
      const sortedList = data.list.sort((a, b) => b.orderCount - a.orderCount);
      setLogs(sortedList);
    } catch (error) {
      console.error('Failed to fetch ranking logs', error);
      alert('获取奖励日志失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs();
  }, []);

  const handleRetry = async (id: string) => {
    if (confirm('是否手动触发奖励发放？系统将尝试重新转账。')) {
      try {
        await rankingApi.retryDistribution(id);
        alert('补发指令已提交');
        fetchLogs();
      } catch (error) {
        console.error('Retry failed', error);
        alert('补发失败');
      }
    }
  };

  const handleRefresh = () => {
    fetchLogs();
  };

  return (
    <div className="space-y-6">
      {/* Header / Info */}
      <div className="bg-gradient-to-r from-purple-600 to-indigo-600 rounded-lg shadow-lg p-6 text-white flex items-center justify-between">
        <div>
          <h2 className="text-xl font-bold flex items-center mb-2">
            <Trophy className="w-6 h-6 mr-2 text-yellow-300" />
            每月激励自动发放监控
          </h2>
          <p className="text-purple-100 text-sm opacity-90">
            系统将于每月 1 日 00:00 自动统计上月排名并下发奖励。此处记录了自动发放的执行日志。
          </p>
        </div>
        <div className="hidden md:block text-right">
          <div className="text-3xl font-bold text-yellow-300">{logs.filter(l => l.status === 'SUCCESS').length}</div>
          <div className="text-xs text-purple-200">本月已发放记录</div>
        </div>
      </div>

      {/* Table */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 font-medium text-gray-900 bg-gray-50 flex justify-between items-center">
          <span>发放记录</span>
          <button
            onClick={handleRefresh}
            className={`text-xs text-blue-600 hover:text-blue-800 flex items-center ${loading ? 'opacity-50 cursor-not-allowed' : ''}`}
            disabled={loading}
          >
            <RefreshCw className={`w-3 h-3 mr-1 ${loading ? 'animate-spin' : ''}`} /> 刷新日志
          </button>
        </div>

        {loading && logs.length === 0 ? (
          <div className="p-8 text-center text-gray-500">加载中...</div>
        ) : (
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">期数</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">排名</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">获奖志愿者</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">接单数</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">发放奖金</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">发放时间</th>
                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {logs.length === 0 ? (
                <tr><td colSpan={8} className="text-center py-8 text-gray-500">暂无奖励发放记录</td></tr>
              ) : (
                logs.map((log) => (
                  <tr key={log.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {log.period}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex justify-start">
                        {log.rank === 1 ? (
                          <Medal className="w-8 h-8 text-yellow-500" strokeWidth={2} />
                        ) : log.rank === 2 ? (
                          <Medal className="w-8 h-8 text-gray-400" strokeWidth={2} />
                        ) : log.rank === 3 ? (
                          <Medal className="w-8 h-8 text-orange-500" strokeWidth={2} />
                        ) : log.rank === 4 ? (
                          <Medal className="w-8 h-8 text-blue-500" strokeWidth={2} />
                        ) : log.rank === 5 ? (
                          <Medal className="w-8 h-8 text-purple-500" strokeWidth={2} />
                        ) : (
                          <div className="w-8 h-8 rounded-full bg-gray-100 flex items-center justify-center font-bold text-gray-600">
                            {log.rank}
                          </div>
                        )}
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <img src={log.volunteerAvatar || 'https://via.placeholder.com/150'} className="w-8 h-8 rounded-full mr-2" alt="" />
                        <span className="text-sm text-gray-900">{log.volunteerName}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                      {log.orderCount} 单
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-orange-600">
                      +{log.rewardAmount} 币
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-xs text-gray-500">
                      {log.distributionTime}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      {log.status === 'SUCCESS' ? (
                        <Badge color="green"><CheckCircle className="w-3 h-3 mr-1" /> 成功</Badge>
                      ) : (
                        <Badge color="red"><XCircle className="w-3 h-3 mr-1" /> 失败</Badge>
                      )}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      {log.status === 'FAILURE' && (
                        <button
                          onClick={() => handleRetry(log.id)}
                          className="text-red-600 hover:text-red-900 flex items-center justify-end w-full"
                        >
                          <AlertTriangle className="w-4 h-4 mr-1" />
                          手动补发
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
