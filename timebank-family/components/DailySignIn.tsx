import React, { useEffect, useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { useMessage } from '../context/MessageContext';
import { userApi } from '../services/userApi';
import { Button } from './UIComponents';
import { X } from 'lucide-react';

export const DailySignIn: React.FC = () => {
  const { state } = useAuth();
  const [showPopup, setShowPopup] = useState(false);
  const [messageData, setMessageData] = useState('');
  const [isElderly, setIsElderly] = useState(true); // default to elderly for UI color scheme

  const { messages, showRewardPopup } = useMessage();

  useEffect(() => {
    // 如果正在显示奖励弹窗，或者有新人礼包标记，则暂不处理签到逻辑
    const hasWelcomeFlag = localStorage.getItem('showWelcomeReward') === 'true';
    if (showRewardPopup || hasWelcomeFlag) return;

    // 只有在已登录并且有 currentUser 的情况下执行签到逻辑
    const timer = setTimeout(() => {
      if (state.isAuthenticated && state.currentUser) {
        const { id } = state.currentUser;
        const sessionKey = `daily_sign_in_done_${id}`;
        const hasSignedIn = sessionStorage.getItem(sessionKey);

        if (!hasSignedIn) {
          // 设置标记，防止刷新时反复弹窗
          sessionStorage.setItem(sessionKey, 'true');

          userApi.dailyLogin()
            .then((res) => {
              if (res.code === 200) {
                  // 老人端签到成功
                  const rawMsg = res.data || res.message || '';
                  setMessageData(rawMsg.replace(/时间币/g, '积分'));
                  setIsElderly(true);
                  setShowPopup(true);
              } else if (res.code === 403) {
                  // 非老人用户，提示欢迎语
                  setMessageData('欢迎登录时间银行家族端，愿您度过美好的一天！');
                  setIsElderly(false);
                  setShowPopup(true);
              }
            })
            .catch((err) => {
              console.error('Failed to trigger daily login:', err);
            });
        }
      }
    }, 1000);

    return () => clearTimeout(timer);
  }, [state.isAuthenticated, state.currentUser, showRewardPopup]);



  if (!showPopup) return null;

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4">
      {/* Overlay */}
      <div className="absolute inset-0 bg-black/60 backdrop-blur-sm" onClick={() => setShowPopup(false)}></div>

      {/* Content */}
      <div className="relative w-full max-w-sm bg-transparent flex flex-col items-center animate-bounce-in">
        {isElderly && (
            <div className="absolute inset-0 flex justify-center -top-20 pointer-events-none">
            <div className="w-full h-full animate-pulse opacity-50 bg-[radial-gradient(circle,rgba(255,215,0,0.4)_0%,transparent_70%)] scale-150"></div>
            </div>
        )}

        <div className={`rounded-3xl p-1 w-full shadow-2xl relative overflow-hidden ${isElderly ? 'bg-red-500' : 'bg-blue-500'}`}>
          <div className={`absolute top-0 left-0 w-full h-32 rounded-t-3xl opacity-20 bg-gradient-to-b ${isElderly ? 'from-yellow-400 to-red-500' : 'from-blue-300 to-blue-500'}`}></div>

          <div className="bg-white rounded-[20px] p-6 flex flex-col items-center text-center relative z-10 m-1">
            <div className={`w-24 h-24 rounded-full flex items-center justify-center mb-4 shadow-inner ${isElderly ? 'bg-yellow-100' : 'bg-blue-50'}`}>
              <span className="text-6xl">{isElderly ? '💸' : '👋'}</span>
            </div>

            <h2 className={`text-2xl font-black mb-2 ${isElderly ? 'text-red-500' : 'text-blue-600'}`}>
              {isElderly ? '每日签到奖励' : '欢迎回来'}
            </h2>
            
            <p className="text-gray-600 text-lg mb-6 font-medium leading-relaxed px-2">
                {messageData}
            </p>

            <Button 
                fullWidth 
                onClick={() => setShowPopup(false)} 
                className={`border-none shadow-lg text-white ${isElderly ? 'bg-red-500 hover:bg-red-600 shadow-red-200' : 'bg-blue-500 hover:bg-blue-600 shadow-blue-200'}`}
            >
              我知道了
            </Button>
          </div>
        </div>

        <button
          onClick={() => setShowPopup(false)}
          className="mt-6 text-white/80 hover:text-white p-2 rounded-full border-2 border-white/30"
        >
          <X size={24} />
        </button>
      </div>
    </div>
  );
};
