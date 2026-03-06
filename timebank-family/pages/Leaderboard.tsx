import React, { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { NavBar, Card } from '../components/UIComponents';
import { RankItem } from '../types';
import { Trophy, Clock, FileText } from 'lucide-react';

const MOCK_RANKINGS: RankItem[] = [
  { id: 'u_101', rank: 1, nickname: '张大妈', avatar: 'https://picsum.photos/200?random=10', value: 120 },
  { id: 'u_102', rank: 2, nickname: '李志愿者', avatar: 'https://picsum.photos/200?random=11', value: 98 },
  { id: 'u_103', rank: 3, nickname: '王叔叔', avatar: 'https://picsum.photos/200?random=12', value: 85 },
  { id: 'u_104', rank: 4, nickname: '赵小妹', avatar: 'https://picsum.photos/200?random=13', value: 72 },
  { id: 'u_105', rank: 5, nickname: '陈志愿者', avatar: 'https://picsum.photos/200?random=1', value: 64 }, // Current User Mock
  { id: 'u_106', rank: 6, nickname: '钱大爷', avatar: 'https://picsum.photos/200?random=14', value: 45 },
  { id: 'u_107', rank: 7, nickname: '孙阿姨', avatar: 'https://picsum.photos/200?random=15', value: 30 },
];

export default function Leaderboard() {
  const { state } = useAuth();
  const [activeTab, setActiveTab] = useState<'duration' | 'count'>('duration');

  const currentUser = state.currentUser;

  // Find current user rank (mock logic)
  const myRankItem = MOCK_RANKINGS.find(r => r.nickname === currentUser?.nickname) || {
    rank: 15, value: 12, nickname: currentUser?.nickname || '我'
  };

  const rankDiff = 10; // Mock difference

  const getMedalColor = (rank: number) => {
    switch (rank) {
      case 1: return 'text-yellow-500 bg-yellow-50';
      case 2: return 'text-gray-400 bg-gray-50';
      case 3: return 'text-orange-600 bg-orange-50';
      default: return 'text-gray-500';
    }
  };

  const getTrophy = (rank: number) => {
    if (rank > 3) return <span className="font-bold font-mono text-lg w-6 text-center">{rank}</span>;
    return <Trophy size={20} className={rank === 1 ? 'fill-yellow-500' : rank === 2 ? 'fill-gray-400' : 'fill-orange-600'} />;
  };

  return (
    <div className="min-h-screen bg-gray-50 pb-20">
      <NavBar title="荣誉排行榜" showBack />

      {/* Header Card */}
      <div className="bg-gradient-to-r from-blue-600 to-blue-500 p-4 text-white">
        <div className="flex items-center gap-4 mb-3">
          <div className="relative">
            <img src={currentUser?.avatar} className="w-14 h-14 rounded-full border-2 border-white/50" />
            <div className="absolute -bottom-1 -right-1 bg-yellow-400 text-yellow-900 text-xs font-bold px-1.5 rounded-full border border-white">
              No.{myRankItem.rank}
            </div>
          </div>
          <div>
            <p className="font-bold text-lg">
              {activeTab === 'duration' ? `${myRankItem.value} 小时` : `${myRankItem.value} 单`}
            </p>
            <p className="text-blue-100 text-xs mt-1">
              距离上一名还差 {rankDiff} {activeTab === 'duration' ? '小时' : '单'}
            </p>
          </div>
        </div>
        <div className="bg-white/10 rounded-lg p-2 text-xs flex items-center gap-2">
          <span className="bg-yellow-400 text-yellow-900 px-1 rounded font-bold">激励</span>
          <span className="opacity-90">每月 1 号结算，前 3 名奖励 100~300 积分！</span>
        </div>
      </div>

      {/* Tabs */}
      <div className="bg-white flex border-b border-gray-100 sticky top-12 z-20">
        <button
          onClick={() => setActiveTab('duration')}
          className={`flex-1 py-3 text-sm font-medium border-b-2 transition-colors flex items-center justify-center gap-2 ${activeTab === 'duration' ? 'text-blue-600 border-blue-600' : 'text-gray-500 border-transparent'
            }`}
        >
          <Clock size={16} /> 服务时长
        </button>
        <button
          onClick={() => setActiveTab('count')}
          className={`flex-1 py-3 text-sm font-medium border-b-2 transition-colors flex items-center justify-center gap-2 ${activeTab === 'count' ? 'text-blue-600 border-blue-600' : 'text-gray-500 border-transparent'
            }`}
        >
          <FileText size={16} /> 接单数量
        </button>
      </div>

      {/* Rank List */}
      <div className="p-3 space-y-3">
        {MOCK_RANKINGS.map((item, index) => {
          // Mock varying values for demo based on tab
          const displayValue = activeTab === 'duration' ? item.value : Math.round(item.value / 2);
          const unit = activeTab === 'duration' ? '小时' : '单';

          return (
            <Card key={item.id} className={`flex items-center gap-4 py-3 ${item.rank <= 3 ? 'border border-blue-100 shadow-md' : ''}`}>
              <div className={`w-8 h-8 flex items-center justify-center rounded-full shrink-0 ${getMedalColor(item.rank)}`}>
                {getTrophy(item.rank)}
              </div>

              <img src={item.avatar} className="w-10 h-10 rounded-full bg-gray-200 object-cover" />

              <div className="flex-1 min-w-0">
                <p className="font-bold text-gray-800 truncate">{item.nickname}</p>
                {item.rank <= 3 && <p className="text-[10px] text-orange-500 font-medium">月度之星</p>}
              </div>

              <div className="text-right">
                <p className="font-bold text-blue-600 text-lg">{displayValue}</p>
                <p className="text-xs text-gray-400">{unit}</p>
              </div>
            </Card>
          );
        })}
      </div>
    </div>
  );
}
