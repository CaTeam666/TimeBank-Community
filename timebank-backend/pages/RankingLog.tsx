import React, { useState, useEffect } from 'react';
import { rankingApi } from '../services/rankingApi';
import { RankingLog } from '../types';
import { Trophy, CheckCircle, XCircle, AlertTriangle, Medal, PlayCircle, Calendar } from 'lucide-react';
import { Badge } from '../components/ui/Badge';

// 接口文档定义的奖励规则（按排名，固定值）
const RANK_REWARDS: Record<number, number> = { 1: 1000, 2: 500, 3: 300, 4: 300, 5: 300 };

// 根据名次返回差异化徽章（每期最多5名）
const getRankBadge = (rank: number): React.ReactElement => {
  const configs: Record<number, { icon: React.ReactElement; label: string; wrapClass: string; labelClass: string }> = {
    1: {
      icon: <Trophy className="w-5 h-5 text-yellow-500" strokeWidth={2} />,
      label: '第1名',
      wrapClass: 'bg-yellow-50 border border-yellow-200',
      labelClass: 'text-yellow-700 font-bold',
    },
    2: {
      icon: <Medal className="w-5 h-5 text-slate-400" strokeWidth={2} />,
      label: '第2名',
      wrapClass: 'bg-slate-50 border border-slate-200',
      labelClass: 'text-slate-500 font-semibold',
    },
    3: {
      icon: <Medal className="w-5 h-5 text-orange-400" strokeWidth={2} />,
      label: '第3名',
      wrapClass: 'bg-orange-50 border border-orange-200',
      labelClass: 'text-orange-600 font-semibold',
    },
    4: {
      icon: <span className="text-xs font-bold text-indigo-600">4</span>,
      label: '第4名',
      wrapClass: 'bg-indigo-50 border border-indigo-200',
      labelClass: 'text-indigo-600',
    },
    5: {
      icon: <span className="text-xs font-bold text-blue-500">5</span>,
      label: '第5名',
      wrapClass: 'bg-blue-50 border border-blue-200',
      labelClass: 'text-blue-500',
    },
  };

  const cfg = configs[rank];
  if (!cfg) {
    return (
      <div className="inline-flex items-center gap-1 px-2 py-1 rounded-full bg-gray-100 border border-gray-200">
        <span className="text-xs font-bold text-gray-500">#{rank}</span>
      </div>
    );
  }

  return (
    <div className={`inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full ${cfg.wrapClass}`}>
      {cfg.icon}
      <span className={`text-xs ${cfg.labelClass}`}>{cfg.label}</span>
    </div>
  );
};

export const RankingLogPage: React.FC = () => {
  const [logs, setLogs] = useState<RankingLog[]>([]);
  const [loading, setLoading] = useState(false);

  // 月度统计触发相关状态
  const [showStatisticsModal, setShowStatisticsModal] = useState(false);
  const [selectedPeriod, setSelectedPeriod] = useState('');
  const [statisticsLoading, setStatisticsLoading] = useState(false);
  // 弹窗步骤：1=选期数 2=二次确认 3=成功
  const [modalStep, setModalStep] = useState<1 | 2 | 3>(1);

  // 查询期数：默认上个月
  const getDefaultPeriod = () => {
    const now = new Date();
    const y = now.getFullYear();
    const m = String(now.getMonth()).padStart(2, '0'); // getMonth() 返回0-11，不+1就是上个月
    return m === '00' ? `${y - 1}-12` : `${y}-${m}`;
  };
  const [queryPeriod, setQueryPeriod] = useState<string>(getDefaultPeriod());

  const fetchLogs = async (period: string) => {
    setLoading(true);
    try {
      const data = await rankingApi.getLogs({ period, page: 1, pageSize: 10 });
      // 期数内按 rank 升序
      const sorted = data.list.sort((a, b) => a.rank - b.rank);
      setLogs(sorted);
    } catch (error) {
      console.error('Failed to fetch ranking logs', error);
      alert('获取奖励日志失败');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchLogs(queryPeriod);
  }, [queryPeriod]);

  const handleRetry = async (id: string) => {
    if (confirm('是否手动触发奖励发放？系统将尝试重新转账。')) {
      try {
        await rankingApi.retryDistribution(id);
        alert('补发指令已提交');
        fetchLogs(queryPeriod);
      } catch (error) {
        console.error('Retry failed', error);
        alert('补发失败');
      }
    }
  };

  // 打开月度统计弹窗，默认选上个月
  const handleOpenStatisticsModal = () => {
    const now = new Date();
    const lastMonth = new Date(now.getFullYear(), now.getMonth() - 1, 1);
    const yyyy = lastMonth.getFullYear();
    const mm = String(lastMonth.getMonth() + 1).padStart(2, '0');
    setSelectedPeriod(`${yyyy}-${mm}`);
    setModalStep(1);
    setShowStatisticsModal(true);
  };

  const handleTriggerStatistics = async () => {
    if (!selectedPeriod) { alert('请选择期数'); return; }
    setStatisticsLoading(true);
    try {
      await rankingApi.triggerMonthlyStatistics(selectedPeriod);
      setModalStep(3); // 成功态
      if (selectedPeriod === queryPeriod) fetchLogs(queryPeriod);
    } catch (error: any) {
      console.error('触发月度统计失败', error);
      alert(error.message || '触发失败，请稍后重试');
    } finally {
      setStatisticsLoading(false);
    }
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

      {/* 操作区：手动触发月度排名 */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-100 p-4 flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-gray-700">手动触发月度排名统计</p>
          <p className="text-xs text-gray-400 mt-0.5">用于补跑指定月份的排名数据，统计接单数前5名的志愿者并发放时间币奖励</p>
        </div>
        <button
          onClick={handleOpenStatisticsModal}
          className="flex items-center px-4 py-2 bg-indigo-600 text-white text-sm rounded-lg hover:bg-indigo-700 transition-colors"
        >
          <PlayCircle className="w-4 h-4 mr-1.5" />
          触发月度排名
        </button>
      </div>

      {/* Table 区 */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
        {/* 期数选择工具栏 */}
        <div className="px-6 py-4 border-b border-gray-100 bg-gray-50 flex items-center gap-4">
          <span className="text-sm font-medium text-gray-700 whitespace-nowrap">发放记录</span>
          <div className="flex items-center gap-2 ml-auto">
            <Calendar className="w-4 h-4 text-indigo-400" />
            <input
              type="month"
              value={queryPeriod}
              max={new Date().toISOString().slice(0, 7)}
              onChange={(e) => setQueryPeriod(e.target.value)}
              className="border border-gray-300 rounded-lg px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400"
            />
            {loading && (
              <svg className="animate-spin w-4 h-4 text-indigo-400" fill="none" viewBox="0 0 24 24">
                <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
              </svg>
            )}
          </div>
        </div>

        {loading ? (
          <div className="p-8 text-center text-gray-400">查询中...</div>
        ) : logs.length === 0 ? (
          <div className="p-8 text-center text-gray-400">{queryPeriod} 暂无奖励发放记录</div>
        ) : (
          <div className="overflow-y-auto max-h-[520px]">
            <table className="min-w-full">
              <thead className="bg-gray-50 sticky top-0 z-10">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">排名</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">获奖志愿者</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">接单数</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">发放奖金</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">发放时间</th>
                  <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
                  <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
                </tr>
              </thead>
              <tbody className="bg-white divide-y divide-gray-100">
                {logs.map((log) => (
                  <tr key={log.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      {getRankBadge(log.rank)}
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
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="text-sm font-bold text-orange-600">
                        +{RANK_REWARDS[log.rank] ?? '?'} 币
                      </span>
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
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* 月度排名统计弹窗 */}
      {showStatisticsModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-40">
          <div className="bg-white rounded-xl shadow-xl w-full max-w-md p-6">

            {/* Step 1: 选期数 */}
            {modalStep === 1 && (
              <>
                <h3 className="text-lg font-bold text-gray-900 mb-1">触发月度排名统计</h3>
                <p className="text-sm text-gray-500 mb-5">
                  将统计所选月份接单数前 5 名的志愿者，并按奖励规则发放时间币。
                </p>

                <label className="block text-sm font-medium text-gray-700 mb-1.5">
                  <Calendar className="w-4 h-4 inline mr-1 text-indigo-500" />
                  选择统计期数
                </label>
                <input
                  type="month"
                  value={selectedPeriod}
                  onChange={(e) => setSelectedPeriod(e.target.value)}
                  className="w-full border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-400 mb-6"
                />

                <div className="flex justify-end gap-3">
                  <button onClick={() => setShowStatisticsModal(false)} className="px-4 py-2 text-sm text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors">
                    取消
                  </button>
                  <button
                    onClick={() => { if (!selectedPeriod) { alert('请选择期数'); return; } setModalStep(2); }}
                    disabled={!selectedPeriod}
                    className="flex items-center px-4 py-2 text-sm text-white bg-indigo-600 rounded-lg hover:bg-indigo-700 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    <PlayCircle className="w-4 h-4 mr-1.5" />
                    确认触发
                  </button>
                </div>
              </>
            )}

            {/* Step 2: 二次确认 UI */}
            {modalStep === 2 && (
              <>
                <div className="flex flex-col items-center text-center mb-6">
                  <div className="w-14 h-14 rounded-full bg-amber-100 flex items-center justify-center mb-4">
                    <AlertTriangle className="w-7 h-7 text-amber-500" />
                  </div>
                  <h3 className="text-lg font-bold text-gray-900 mb-1">确认执行操作</h3>
                  <p className="text-sm text-gray-500">此操作将立即对以下期数进行排名并发币</p>
                </div>

                {/* 操作详情卡片 */}
                <div className="bg-gray-50 border border-gray-200 rounded-xl p-4 mb-6 space-y-3">
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-gray-500">统计期数</span>
                    <span className="font-bold text-indigo-700 text-base">{selectedPeriod}</span>
                  </div>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-gray-500">奖励范围</span>
                    <span className="font-medium text-gray-700">接单数前 5 名志愿者</span>
                  </div>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-gray-500">最高奖励</span>
                    <span className="font-bold text-orange-500">+1000 时间币</span>
                  </div>
                  <div className="flex items-center justify-between text-sm">
                    <span className="text-gray-500">总发放</span>
                    <span className="font-bold text-orange-500">+{1000 + 500 + 300 + 300 + 300} 时间币</span>
                  </div>
                </div>

                <div className="bg-amber-50 border border-amber-200 rounded-lg px-4 py-3 text-xs text-amber-700 mb-6 flex items-start gap-2">
                  <AlertTriangle className="w-3.5 h-3.5 mt-0.5 flex-shrink-0" />
                  <span>此操作不可撤销，如果该期已经发放过，将会重复发放奖励。请确认期数无误后再执行。</span>
                </div>

                <div className="flex justify-end gap-3">
                  <button
                    onClick={() => setModalStep(1)}
                    disabled={statisticsLoading}
                    className="px-4 py-2 text-sm text-gray-600 bg-gray-100 rounded-lg hover:bg-gray-200 transition-colors"
                  >
                    返回修改
                  </button>
                  <button
                    onClick={handleTriggerStatistics}
                    disabled={statisticsLoading}
                    className="flex items-center px-4 py-2 text-sm text-white bg-amber-500 rounded-lg hover:bg-amber-600 disabled:opacity-50 disabled:cursor-not-allowed transition-colors"
                  >
                    {statisticsLoading ? (
                      <>
                        <svg className="animate-spin w-4 h-4 mr-1.5" fill="none" viewBox="0 0 24 24">
                          <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                          <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                        </svg>
                        发放中...
                      </>
                    ) : (
                      <>
                        <CheckCircle className="w-4 h-4 mr-1.5" />
                        确定执行
                      </>
                    )}
                  </button>
                </div>
              </>
            )}

            {/* Step 3: 成功 UI */}
            {modalStep === 3 && (
              <>
                <div className="flex flex-col items-center text-center py-4">
                  <div className="w-16 h-16 rounded-full bg-green-100 flex items-center justify-center mb-4">
                    <CheckCircle className="w-8 h-8 text-green-500" />
                  </div>
                  <h3 className="text-lg font-bold text-gray-900 mb-2">触发成功</h3>
                  <p className="text-sm text-gray-500 mb-1">
                    <span className="font-bold text-indigo-700">{selectedPeriod}</span> 月度排名统计已提交
                  </p>
                  <p className="text-xs text-gray-400 mb-6">系统正在后台统计并发放奖励，稍后可在记录中查看</p>

                  <button
                    onClick={() => setShowStatisticsModal(false)}
                    className="w-full py-2.5 text-sm font-medium text-white bg-green-500 rounded-lg hover:bg-green-600 transition-colors"
                  >
                    关闭
                  </button>
                </div>
              </>
            )}

          </div>
        </div>
      )}
    </div>
  );
};
