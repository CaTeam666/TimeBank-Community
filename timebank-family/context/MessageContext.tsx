import React, { createContext, useContext, useState, useEffect, ReactNode } from 'react';
import { Message, MessageType } from '../types';
import { useAuth } from './AuthContext';

const MOCK_MESSAGES: Message[] = [
  {
    id: 'm_1',
    userId: 'u_001', // Volunteer
    type: 'TASK',
    title: '任务被接单',
    content: '您发布的[陪诊任务]已被志愿者张三接单，点击查看详情。',
    isRead: false,
    createdAt: Date.now() - 1000 * 60 * 30, // 30 mins ago
    link: '/task/orders'
  },
  {
    id: 'm_2',
    userId: 'u_001',
    type: 'MONEY',
    title: '月度奖励到账',
    content: '恭喜！您获得 [10月度金牌志愿者] 奖励，100 积分 已到账。',
    isRead: false,
    createdAt: Date.now() - 1000 * 60 * 60 * 2, // 2 hours ago
    link: '/user/profile'
  },
  {
    id: 'm_3',
    userId: 'u_001',
    type: 'SYSTEM',
    title: '社区活动通知',
    content: '社区将在重阳节举办包饺子活动，欢迎报名参加。',
    isRead: true,
    createdAt: Date.now() - 1000 * 60 * 60 * 24, // 1 day ago
  },
  {
    id: 'm_4',
    userId: 'u_002', // Senior
    type: 'FAMILY',
    title: '亲情绑定申请',
    content: '用户 138****0000 申请绑定您为亲情账号，请及时审核。',
    isRead: false,
    createdAt: Date.now() - 1000 * 60 * 10,
    link: '/user/family'
  }
];

interface MessageContextType {
  messages: Message[];
  unreadCount: number;
  markAsRead: (id: string) => void;
  markAllAsRead: () => void;
  showRewardPopup: boolean;
  closeRewardPopup: () => void;
  addMessage: (msg: Omit<Message, 'id' | 'createdAt' | 'isRead'>) => void;
}

const MessageContext = createContext<MessageContextType | undefined>(undefined);

export const MessageProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const { state: authState } = useAuth();
  const [messages, setMessages] = useState<Message[]>(MOCK_MESSAGES);
  const [showRewardPopup, setShowRewardPopup] = useState(false);

  const currentUser = authState.currentUser;

  // Filter messages for current user
  const userMessages = currentUser
    ? messages.filter(m => m.userId === currentUser.id)
    : [];

  const unreadCount = userMessages.filter(m => !m.isRead).length;

  // Simulate Triggering a Reward Popup on "Login" (Mount) if there's a specific unread money message
  useEffect(() => {
    if (currentUser) {
      // Demo logic: If user has an unread 'MONEY' message containing "奖励", show popup
      const hasReward = userMessages.some(m => !m.isRead && m.type === 'MONEY' && (m.title.includes('奖励') || m.title.includes('礼包')));

      if (hasReward) {
        // Delay slightly for effect
        const timer = setTimeout(() => setShowRewardPopup(true), 1000);
        return () => clearTimeout(timer);
      }
    }
  }, [currentUser?.id]);

  const markAsRead = (id: string) => {
    setMessages(prev => prev.map(m => m.id === id ? { ...m, isRead: true } : m));
  };

  const markAllAsRead = () => {
    if (!currentUser) return;
    setMessages(prev => prev.map(m => m.userId === currentUser.id ? { ...m, isRead: true } : m));
  };

  const closeRewardPopup = () => {
    setShowRewardPopup(false);
    // Mark the reward message as read so it doesn't show again immediately
    const rewardMsg = userMessages.find(m => !m.isRead && m.type === 'MONEY' && (m.title.includes('奖励') || m.title.includes('礼包')));

    if (rewardMsg) {
      markAsRead(rewardMsg.id);
    }
  };

  const addMessage = (msg: Omit<Message, 'id' | 'createdAt' | 'isRead'>) => {
    const newMessage: Message = {
      ...msg,
      id: `m_${Date.now()}`,
      createdAt: Date.now(),
      isRead: false
    };
    setMessages(prev => [newMessage, ...prev]);
  };

  return (
    <MessageContext.Provider value={{
      messages: userMessages,
      unreadCount,
      markAsRead,
      markAllAsRead,
      showRewardPopup,
      closeRewardPopup,
      addMessage
    }}>
      {children}
    </MessageContext.Provider>
  );
};

export const useMessage = () => {
  const context = useContext(MessageContext);
  if (!context) {
    throw new Error('useMessage must be used within a MessageProvider');
  }
  return context;
};
