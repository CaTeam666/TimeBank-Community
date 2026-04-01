import React, { useState, useEffect, useCallback } from 'react';
import { useAuth } from '../context/AuthContext';
import { NavBar, Card, Toast, Modal, Button } from '../components/UIComponents';
import { Trophy, Calendar, Award, Star } from 'lucide-react';
import { rankingApi, RankingItem, MonthlyRankingData, UserOrderStats } from '../services/rankingApi';

// ==================== 内联加载组件 ====================
const LoadingSpinner: React.FC<{ text?: string }> = ({ text = '加载中...' }) => (
  <div className="flex flex-col items-center justify-center py-16 text-gray-400">
    <div className="w-8 h-8 border-4 border-blue-200 border-t-blue-500 rounded-full animate-spin mb-3" />
    <p className="text-sm">{text}</p>
  </div>
);

// ==================== 奖励规则常量 ====================
const REWARD_RULES = [
  { rank: 1, amount: 1000 },
  { rank: 2, amount: 500 },
  { rank: 3, amount: 300 },
  { rank: 4, amount: 300 },
  { rank: 5, amount: 300 },
];

// ==================== 月份选项生成（近12个月）====================
function getPastMonths(count: number): string[] {
  const months: string[] = [];
  const now = new Date();
  for (let i = 1; i <= count; i++) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1);
    const year = d.getFullYear();
    const month = String(d.getMonth() + 1).padStart(2, '0');
    months.push(`${year}-${month}`);
  }
  return months;
}

// ==================== 奖杯/排名图标 ====================
const RankIcon: React.FC<{ rank: number }> = ({ rank }) => {
  if (rank === 1) return <Trophy size={22} className="fill-yellow-500 text-yellow-500" />;
  if (rank === 2) return <Trophy size={22} className="fill-gray-400 text-gray-400" />;
  if (rank === 3) return <Trophy size={22} className="fill-orange-500 text-orange-500" />;
  return <span className="font-bold font-mono text-base w-6 text-center text-gray-500">{rank}</span>;
};

const rankBgColor = (rank: number) => {
  if (rank === 1) return 'bg-yellow-50 border border-yellow-200';
  if (rank === 2) return 'bg-gray-50 border border-gray-200';
  if (rank === 3) return 'bg-orange-50 border border-orange-200';
  return 'bg-white border border-gray-100';
};

// ==================== 排行榜单行 ====================
const RankRow: React.FC<{ item: RankingItem }> = ({ item }) => (
  <div className={`flex items-center gap-3 px-4 py-3 rounded-xl shadow-sm ${rankBgColor(item.rank)}`}>
    <div className="w-8 h-8 flex items-center justify-center rounded-full shrink-0">
      <RankIcon rank={item.rank} />
    </div>
    <img
      src={item.volunteerAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(item.volunteerName)}&background=dbeafe&color=2563eb&size=64`}
      alt={item.volunteerName}
      className="w-10 h-10 rounded-full bg-blue-100 object-cover shrink-0"
    />
    <div className="flex-1 min-w-0">
      <p className="font-bold text-gray-800 truncate">{item.volunteerName}</p>
      {item.rank <= 3 && (
        <div className="flex items-center gap-1 mt-0.5">
          <Star size={10} className="fill-orange-400 text-orange-400" />
          <span className="text-[10px] text-orange-500 font-medium">月度之星</span>
        </div>
      )}
    </div>
    <div className="text-right shrink-0">
      <p className="font-bold text-blue-600 text-xl">{item.orderCount}</p>
      <p className="text-xs text-gray-400">单</p>
    </div>
  </div>
);

// ==================== 主页面 ====================
export default function Leaderboard() {
  const { state } = useAuth();
  const currentUser = state.currentUser;

  // 主榜数据
  const [rankData, setRankData] = useState<MonthlyRankingData | null>(null);
  const [myStats, setMyStats] = useState<UserOrderStats | null>(null);
  const [loading, setLoading] = useState(true);

  // Toast 提示
  const [toast, setToast] = useState({ visible: false, message: '', type: 'info' as 'success' | 'error' | 'info' });
  const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
    setToast({ visible: true, message, type });
  };
  const hideToast = () => setToast(prev => ({ ...prev, visible: false }));

  // 历史月度 Modal
  const [historyModalOpen, setHistoryModalOpen] = useState(false);
  const [pastMonths] = useState(() => getPastMonths(12));
  const [selectedPeriod, setSelectedPeriod] = useState<string | null>(null);
  const [historyData, setHistoryData] = useState<MonthlyRankingData | null>(null);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyDetailOpen, setHistoryDetailOpen] = useState(false);

  // 加载主数据
  const loadData = useCallback(async () => {
    setLoading(true);
    try {
      const [top, stats] = await Promise.all([
        rankingApi.getTopRanking(),
        rankingApi.getMyStats(),
      ]);
      setRankData(top);
      setMyStats(stats);
    } catch (err: any) {
      showToast(err?.message || '数据加载失败，请稍后重试', 'error');
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    loadData();
  }, [loadData]);

  // 查询历史月份
  const handleSelectMonth = async (period: string) => {
    setSelectedPeriod(period);
    setHistoryModalOpen(false);
    setHistoryLoading(true);
    setHistoryDetailOpen(true);
    try {
      const data = await rankingApi.getMonthlyRanking(period);
      setHistoryData(data);
    } catch (err: any) {
      setHistoryDetailOpen(false);
      showToast(err?.message || '该月份暂无排行数据', 'error');
    } finally {
      setHistoryLoading(false);
    }
  };

  // 当前月份（用于展示）
  const currentPeriod = rankData?.period || (() => {
    const d = new Date();
    return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}`;
  })();

  // 距上一名差距
  const myRank = myStats?.currentRank;
  const myCount = myStats?.currentMonthCount ?? 0;
  const rankAboveCount = myRank && rankData?.list
    ? (rankData.list.find(r => r.rank === myRank - 1)?.orderCount ?? null)
    : null;
  const gap = rankAboveCount !== null ? rankAboveCount - myCount : null;

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      {/* 导航栏，右侧历史按钮 */}
      <NavBar
        title="接单排行榜"
        showBack
        rightAction={
          <button
            onClick={() => setHistoryModalOpen(true)}
            className="p-1 text-blue-600 active:opacity-50"
            title="历史排行"
          >
            <Calendar size={22} />
          </button>
        }
      />

      {/* 顶部个人卡片 */}
      <div className="bg-gradient-to-r from-blue-600 to-indigo-500 px-5 pt-5 pb-6 text-white">
        <div className="flex items-center gap-4 mb-4">
          <div className="relative">
            <img
              src={currentUser?.avatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(currentUser?.nickname || '我')}&background=93c5fd&color=1e3a8a&size=80`}
              alt="我的头像"
              className="w-14 h-14 rounded-full border-2 border-white/60 object-cover"
            />
            <div className="absolute -bottom-1 -right-1 bg-yellow-400 text-yellow-900 text-xs font-bold px-1.5 py-0.5 rounded-full border border-white min-w-[28px] text-center">
              {myStats ? (myRank ? `No.${myRank}` : '--') : '--'}
            </div>
          </div>
          <div className="flex-1 min-w-0">
            <p className="font-semibold text-lg truncate">{currentUser?.nickname || '我'}</p>
            <p className="text-blue-100 text-sm mt-0.5">
              本月接单 <span className="text-white font-bold text-xl mx-1">{myCount}</span> 单
            </p>
            {gap !== null && gap > 0 && (
              <p className="text-blue-200 text-xs mt-1">
                距上一名还差 <span className="text-yellow-300 font-bold">{gap}</span> 单
              </p>
            )}
            {myRank === 1 && (
              <p className="text-yellow-300 text-xs mt-1 flex items-center gap-1">
                <Trophy size={12} className="fill-yellow-300" /> 当前排名第一🎉
              </p>
            )}
          </div>
          <div className="text-right shrink-0">
            <p className="text-blue-100 text-xs">累计接单</p>
            <p className="text-white font-bold text-2xl">{myStats?.totalOrderCount ?? '--'}</p>
            <p className="text-blue-200 text-xs">单</p>
          </div>
        </div>

        {/* 奖励提示 */}
        <div className="bg-white/15 rounded-xl p-3 flex items-start gap-2 text-xs">
          <Award size={16} className="text-yellow-300 shrink-0 mt-0.5" />
          <div>
            <span className="font-semibold text-yellow-200">每月奖励：</span>
            <span className="opacity-90">
              前5名分别获得 1000 / 500 / 300 / 300 / 300 积分奖励，每月1日自动发放！
            </span>
          </div>
        </div>
      </div>

      {/* 排行标题 */}
      <div className="flex items-center justify-between px-4 pt-4 pb-2">
        <h2 className="font-bold text-gray-700 text-sm">
          📋 {currentPeriod} 月度前5名
        </h2>
        <button
          onClick={loadData}
          className="text-xs text-blue-500 active:opacity-60"
        >
          刷新
        </button>
      </div>

      {/* 排行列表 */}
      <div className="px-3 space-y-2.5">
        {loading ? (
          <LoadingSpinner text="加载排行数据..." />
        ) : !rankData || rankData.list.length === 0 ? (
          <div className="text-center py-16 text-gray-400">
            <Trophy size={36} className="mx-auto mb-3 text-gray-300" />
            <p className="text-sm">本月暂无排行数据</p>
          </div>
        ) : (
          rankData.list.map(item => <RankRow key={item.volunteerId} item={item} />)
        )}
      </div>

      {/* ===== 历史月份选择 Modal ===== */}
      <Modal
        isOpen={historyModalOpen}
        onClose={() => setHistoryModalOpen(false)}
        title="查看历史排行"
      >
        <p className="text-sm text-gray-500 mb-3">选择月份查看对应排行</p>
        <div className="grid grid-cols-3 gap-2 max-h-64 overflow-y-auto">
          {pastMonths.map(p => (
            <button
              key={p}
              onClick={() => handleSelectMonth(p)}
              className="py-2 px-2 rounded-lg border border-gray-200 text-sm text-gray-700 hover:bg-blue-50 hover:border-blue-400 hover:text-blue-600 active:scale-95 transition-all font-medium"
            >
              {p}
            </button>
          ))}
        </div>
      </Modal>

      {/* ===== 历史排行详情 Modal ===== */}
      <Modal
        isOpen={historyDetailOpen}
        onClose={() => { setHistoryDetailOpen(false); setHistoryData(null); setSelectedPeriod(null); }}
        title={`${selectedPeriod || ''} 历史排行`}
      >
        {historyLoading ? (
          <LoadingSpinner text="查询中..." />
        ) : !historyData || historyData.list.length === 0 ? (
          <div className="text-center py-8 text-gray-400">
            <p className="text-sm">该月份暂无排行数据</p>
          </div>
        ) : (
          <div className="space-y-2">
            {historyData.list.map(item => {
              const reward = historyData.rewardInfo?.find(r => r.rank === item.rank);
              return (
                <div key={item.volunteerId} className={`flex items-center gap-3 px-3 py-2.5 rounded-xl ${rankBgColor(item.rank)}`}>
                  <div className="w-7 h-7 flex items-center justify-center shrink-0">
                    <RankIcon rank={item.rank} />
                  </div>
                  <img
                    src={item.volunteerAvatar || `https://ui-avatars.com/api/?name=${encodeURIComponent(item.volunteerName)}&background=dbeafe&color=2563eb&size=48`}
                    alt={item.volunteerName}
                    className="w-9 h-9 rounded-full object-cover bg-blue-100 shrink-0"
                  />
                  <div className="flex-1 min-w-0">
                    <p className="font-bold text-gray-800 text-sm truncate">{item.volunteerName}</p>
                    <p className="text-xs text-gray-500">{item.orderCount} 单</p>
                  </div>
                  {reward && (
                    <div className="text-right shrink-0">
                      <span className="bg-yellow-100 text-yellow-700 text-xs font-bold px-2 py-0.5 rounded-full">
                        +{reward.rewardAmount} 币
                      </span>
                    </div>
                  )}
                </div>
              );
            })}

            {/* 奖励规则说明（当有奖励时显示） */}
            {historyData.rewardInfo && historyData.rewardInfo.length > 0 && (
              <div className="mt-3 bg-blue-50 rounded-xl p-3 text-xs text-blue-700">
                <p className="font-semibold mb-1 flex items-center gap-1">
                  <Award size={12} /> 已发放奖励
                </p>
                <p className="text-blue-600 opacity-80">本月奖励已自动发放至对应志愿者账户。</p>
              </div>
            )}
          </div>
        )}
      </Modal>

      {/* ===== Toast 提示 ===== */}
      <Toast
        message={toast.message}
        type={toast.type}
        isVisible={toast.visible}
        onClose={hideToast}
      />
    </div>
  );
}
