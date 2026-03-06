import React from 'react';
import { useMessage } from '../context/MessageContext';
import { Button } from './UIComponents';
import { X } from 'lucide-react';

export const RewardPopup: React.FC = () => {
  const { showRewardPopup, closeRewardPopup } = useMessage();

  if (!showRewardPopup) return null;

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
      {/* Overlay */}
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={closeRewardPopup}></div>

      {/* Content */}
      <div className="relative w-full max-w-sm bg-transparent flex flex-col items-center animate-bounce-in">
        {/* Confetti / Decor (Simplified CSS) */}
        <div className="absolute inset-0 flex justify-center -top-20 pointer-events-none">
          <div className="w-full h-full animate-pulse opacity-50 bg-[radial-gradient(circle,rgba(255,215,0,0.4)_0%,transparent_70%)] scale-150"></div>
        </div>

        <div className="bg-red-500 rounded-3xl p-1 w-full shadow-2xl relative overflow-hidden">
          <div className="absolute top-0 left-0 w-full h-32 bg-gradient-to-b from-yellow-400 to-red-500 rounded-t-3xl opacity-20"></div>

          <div className="bg-white rounded-[20px] p-6 flex flex-col items-center text-center relative z-10 m-1">
            <div className="w-24 h-24 bg-yellow-100 rounded-full flex items-center justify-center mb-4 shadow-inner">
              <span className="text-6xl">🎁</span>
            </div>

            <h2 className="text-2xl font-black text-gray-800 mb-2">恭喜获得月度奖励！</h2>
            <p className="text-gray-500 text-sm mb-6">感谢您对社区的辛勤付出</p>

            <div className="bg-red-50 px-6 py-4 rounded-xl mb-6 border border-red-100">
              <span className="text-3xl font-bold text-red-500">+100</span>
              <span className="text-sm font-medium text-red-400 ml-1">积分</span>
            </div>

            <Button fullWidth onClick={closeRewardPopup} className="bg-red-500 hover:bg-red-600 border-none shadow-lg shadow-red-200">
              开心收下
            </Button>
          </div>
        </div>

        <button
          onClick={closeRewardPopup}
          className="mt-6 text-white/80 hover:text-white p-2 rounded-full border-2 border-white/30"
        >
          <X size={24} />
        </button>
      </div>
    </div>
  );
};
