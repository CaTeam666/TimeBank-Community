import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { NavBar } from '../components/UIComponents';
import { ClipboardList, Bell, Heart, ChevronRight, CheckCheck, Coins } from 'lucide-react';
import { messageApi, MessageItem } from '../services/messageApi';

export default function MessageList() {
    const navigate = useNavigate();

    // 后端消息列表状态
    const [apiMessages, setApiMessages] = useState<MessageItem[]>([]);
    const [loading, setLoading] = useState(true);

    // 获取消息列表
    useEffect(() => {
        messageApi.getMessageList()
            .then(response => {
                setApiMessages(response.list);
                setLoading(false);
            })
            .catch(err => {
                console.error('Failed to fetch messages:', err);
                setLoading(false);
            });
    }, []);

    const handleMessageClick = (msg: MessageItem) => {
        // 根据消息类型决定跳转路径,优先使用硬编码路径以确保准确性
        let targetRoute = msg.route;

        // 为不同消息类型硬编码跳转路径
        switch (msg.type) {
            case 'FAMILY_BIND':
                targetRoute = '/family/binding-review';
                break;
            case 'TASK_VERIFY':
                targetRoute = '/task/orders'; // 或者其他验收页面路径
                break;
            default:
                // 其他类型使用后端返回的route
                targetRoute = msg.route;
        }

        if (targetRoute) {
            console.log(`Navigating to: ${targetRoute} for message type: ${msg.type}`);
            navigate(targetRoute);
        } else {
            console.warn(`No route defined for message type: ${msg.type}`);
        }
    };

    const getIcon = (type: string) => {
        switch (type) {
            case 'FAMILY_BIND': return <div className="w-10 h-10 rounded-full bg-pink-100 text-pink-500 flex items-center justify-center"><Heart size={20} /></div>;
            case 'TASK_VERIFY': return <div className="w-10 h-10 rounded-full bg-blue-100 text-blue-500 flex items-center justify-center"><ClipboardList size={20} /></div>;
            case 'TASK': return <div className="w-10 h-10 rounded-full bg-blue-100 text-blue-500 flex items-center justify-center"><ClipboardList size={20} /></div>;
            case 'MONEY': return <div className="w-10 h-10 rounded-full bg-yellow-100 text-yellow-600 flex items-center justify-center"><Coins size={20} /></div>;
            case 'SYSTEM': return <div className="w-10 h-10 rounded-full bg-red-100 text-red-500 flex items-center justify-center"><Bell size={20} /></div>;
            case 'FAMILY': return <div className="w-10 h-10 rounded-full bg-pink-100 text-pink-500 flex items-center justify-center"><Heart size={20} /></div>;
            default: return <div className="w-10 h-10 rounded-full bg-gray-100 text-gray-500 flex items-center justify-center"><Bell size={20} /></div>;
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 pb-20">
            <NavBar
                title="消息中心"
                showBack
            />

            <div className="p-3 space-y-3">
                {loading ? (
                    <div className="text-center py-20 text-gray-400">
                        <Bell size={48} className="mx-auto mb-2 opacity-20 animate-pulse" />
                        <p>加载中...</p>
                    </div>
                ) : apiMessages.length === 0 ? (
                    <div className="text-center py-20 text-gray-400">
                        <Bell size={48} className="mx-auto mb-2 opacity-20" />
                        <p>暂无消息</p>
                    </div>
                ) : (
                    apiMessages.map(msg => (
                        <div
                            key={msg.id}
                            onClick={() => handleMessageClick(msg)}
                            className="bg-white rounded-xl p-4 flex gap-3 relative active:bg-gray-50 transition-colors cursor-pointer"
                        >
                            {/* Icon */}
                            <div className="shrink-0">
                                {getIcon(msg.type)}
                            </div>

                            {/* Content */}
                            <div className="flex-1 min-w-0">
                                <div className="flex justify-between items-start mb-1">
                                    <h3 className="text-sm font-bold text-gray-900 truncate pr-2">
                                        {msg.title}
                                    </h3>
                                    <span className="text-[10px] text-gray-400 shrink-0">
                                        {new Date(msg.createTime).toLocaleDateString() === new Date().toLocaleDateString()
                                            ? new Date(msg.createTime).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })
                                            : new Date(msg.createTime).toLocaleDateString()}
                                    </span>
                                </div>
                                {/* 使用口语化提示词 */}
                                <p className="text-xs text-gray-600 line-clamp-2 leading-relaxed font-medium">
                                    {messageApi.getMessageHint(msg.type, msg.typeName)}
                                </p>
                                {msg.content && (
                                    <p className="text-xs text-gray-400 line-clamp-1 leading-relaxed mt-1">
                                        {msg.content}
                                    </p>
                                )}
                            </div>

                            {/* Arrow */}
                            {msg.route && (
                                <div className="flex items-center text-gray-300">
                                    <ChevronRight size={16} />
                                </div>
                            )}
                        </div>
                    ))
                )}
            </div>
        </div>
    );
}
