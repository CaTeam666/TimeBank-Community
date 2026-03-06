import React, { useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useMessage } from '../context/MessageContext';
import { useNavigate } from 'react-router-dom';
import { Card, Button, NavBar } from '../components/UIComponents';
import { Settings, Coins, History, FileText, UserCircle, Headset, ShieldAlert, Users, Trophy, Bell } from 'lucide-react';
import { UserRole } from '../types';
import { userApi } from '../services/userApi';
import { messageApi } from '../services/messageApi';


export default function Profile() {
    const { state, dispatch } = useAuth();
    const { unreadCount } = useMessage();
    const navigate = useNavigate();
    const user = state.currentUser;

    const [showFreezeModal, setShowFreezeModal] = React.useState(false);
    const [messageUnreadCount, setMessageUnreadCount] = React.useState(0);


    useEffect(() => {
        if (user?.id) {
            // Check balance
            userApi.getUserBalance(user.id).then(balance => {
                dispatch({
                    type: 'UPDATE_BALANCE',
                    payload: { userId: user.id, amount: balance }
                });
            }).catch(console.error);

            // Check account status
            userApi.getUserInfo(user.id).then(userInfo => {
                if (userInfo.status === 0) {
                    setShowFreezeModal(true);
                }
            }).catch(console.error);

            // 获取未读消息数量
            messageApi.getUnreadCount().then(count => {
                setMessageUnreadCount(count);
            }).catch(err => {
                console.error('Failed to get unread message count:', err);
                setMessageUnreadCount(0);
            });
        }
    }, [user?.id, dispatch, navigate]);

    if (!user) {
        return (
            <div className="p-8 text-center">
                <p>请先登录。</p>
                <Button onClick={() => navigate('/login')} className="mt-4">去登录</Button>
            </div>
        );
    }

    const roleColor = {
        [UserRole.SENIOR]: 'bg-red-100 text-red-700',
        [UserRole.VOLUNTEER]: 'bg-blue-100 text-blue-700',
        [UserRole.AGENT]: 'bg-purple-100 text-purple-700',
    };

    const roleLabel = {
        [UserRole.SENIOR]: '长者',
        [UserRole.VOLUNTEER]: '志愿者',
        [UserRole.AGENT]: '代理人',
    };

    // 是否显示红点：消息未读或有待确认绑定
    const hasNotification = unreadCount > 0 || messageUnreadCount > 0;


    return (
        <div className="min-h-screen bg-gray-50 pb-24">
            {/* Custom Header Area */}
            <div className="bg-white px-6 pt-12 pb-6 mb-4 relative">
                <div className="flex justify-between items-start mb-4">
                    <div className="relative">
                        <img src={user.avatar} alt="Avatar" className="w-20 h-20 rounded-full border-4 border-gray-50 object-cover" />
                        <div className="absolute bottom-0 right-0 bg-white p-1 rounded-full shadow-sm border border-gray-100">
                            <Settings size={14} className="text-gray-500" />
                        </div>
                    </div>

                    <div className="flex gap-2">
                        <button
                            onClick={() => navigate('/message/list')}
                            className="p-2 text-gray-600 relative active:opacity-50"
                        >
                            <Bell size={24} />
                            {hasNotification && (
                                <span className="absolute top-1 right-1 w-2.5 h-2.5 bg-red-500 rounded-full border border-white"></span>
                            )}
                        </button>
                        <button className="text-gray-400 p-2" onClick={() => navigate('/settings')}>
                            <Settings size={24} />
                        </button>
                    </div>
                </div>

                <div>
                    <div className="flex items-center gap-2 mb-1">
                        <h1 className="text-2xl font-bold text-gray-900">{user.nickname}</h1>
                        <span className={`px-2 py-0.5 rounded-full text-xs font-bold ${roleColor[user.role]}`}>
                            {roleLabel[user.role]}
                        </span>
                    </div>
                    <div className="flex items-center text-yellow-500 text-sm font-medium">
                        {'★'.repeat(Math.floor(user.creditScore))}
                        <span className="ml-1 text-gray-400">({user.creditScore.toFixed(1)} 信用分)</span>
                    </div>
                </div>
            </div>

            <div className="px-4 space-y-4">
                {/* Assets Card */}
                <Card className="bg-gradient-to-br from-gray-900 to-gray-800 text-white border-none shadow-lg relative overflow-hidden">
                    <div className="absolute top-0 right-0 p-8 opacity-10">
                        <Coins size={120} />
                    </div>
                    <div className="relative z-10">
                        <p className="text-gray-400 text-sm mb-1">积分余额</p>
                        <div className="flex items-baseline gap-1 mb-6">
                            <span className="text-4xl font-bold">{user.balance.toLocaleString()}</span>
                            <span className="text-sm font-medium opacity-80">积分</span>
                        </div>

                        <div className="flex gap-3">
                            {user.role === UserRole.VOLUNTEER && (
                                <button className="flex-1 bg-white/20 hover:bg-white/30 backdrop-blur-sm py-2 rounded-lg text-sm font-medium transition-colors">
                                    充值
                                </button>
                            )}
                            <button
                                onClick={() => navigate('/mall/home')}
                                className="flex-1 bg-white/10 hover:bg-white/20 backdrop-blur-sm py-2 rounded-lg text-sm font-medium transition-colors"
                            >
                                兑换
                            </button>
                            <button
                                onClick={() => navigate('/mall/orders')}
                                className="flex-1 bg-white/10 hover:bg-white/20 backdrop-blur-sm py-2 rounded-lg text-sm font-medium transition-colors"
                            >
                                记录
                            </button>
                        </div>
                    </div>
                </Card>

                {/* Function Grid */}
                <div className="grid grid-cols-2 gap-3">
                    <Card onClick={() => navigate('/incentive/rank')} className="flex flex-col items-center justify-center py-6 active:bg-gray-50 border border-yellow-100 bg-yellow-50/50">
                        <div className="w-10 h-10 rounded-full bg-yellow-100 text-yellow-600 flex items-center justify-center mb-2">
                            <Trophy size={20} />
                        </div>
                        <span className="text-sm font-bold text-gray-800">荣誉排行</span>
                    </Card>
                    <Card onClick={() => navigate('/task/orders')} className="flex flex-col items-center justify-center py-6 active:bg-gray-50">
                        <div className="w-10 h-10 rounded-full bg-blue-50 text-blue-500 flex items-center justify-center mb-2">
                            <FileText size={20} />
                        </div>
                        <span className="text-sm font-medium text-gray-700">我的发布</span>
                    </Card>
                    <Card onClick={() => navigate('/task/orders')} className="flex flex-col items-center justify-center py-6 active:bg-gray-50">
                        <div className="w-10 h-10 rounded-full bg-green-50 text-green-500 flex items-center justify-center mb-2">
                            <UserCircle size={20} />
                        </div>
                        <span className="text-sm font-medium text-gray-700">我的接单</span>
                    </Card>
                    <Card onClick={() => navigate('/user/family')} className="flex flex-col items-center justify-center py-6 active:bg-gray-50">
                        <div className="w-10 h-10 rounded-full bg-orange-50 text-orange-500 flex items-center justify-center mb-2">
                            <Users size={20} />
                        </div>
                        <span className="text-sm font-medium text-gray-700">亲情账号</span>
                    </Card>
                </div>

                {/* Support */}
                <Card className="flex items-center justify-between p-4 active:bg-gray-50">
                    <div className="flex items-center gap-3">
                        <Headset className="text-gray-400" />
                        <span className="text-gray-700 font-medium">联系客服</span>
                    </div>
                    <div className="text-gray-400 text-sm">9:00 - 18:00</div>
                </Card>
            </div>

            {/* Account Freeze Modal */}
            {
                showFreezeModal && (
                    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4 bg-black/80 backdrop-blur-sm animate-fade-in">
                        <div className="bg-white rounded-2xl w-full max-w-sm overflow-hidden shadow-2xl transform scale-100 transition-all">
                            <div className="p-8 flex flex-col items-center text-center">
                                <div className="w-20 h-20 bg-red-50 rounded-full flex items-center justify-center mb-6">
                                    <ShieldAlert size={40} className="text-red-500" />
                                </div>

                                <h3 className="text-xl font-bold text-gray-900 mb-2">账号已冻结</h3>

                                <p className="text-gray-500 mb-8 leading-relaxed">
                                    系统检测到您的账号存在异常，已被临时冻结。
                                    <br />
                                    如有疑问，请联系管理员或客服。
                                </p>

                                <Button
                                    variant="danger"
                                    fullWidth
                                    size="lg"
                                    onClick={() => {
                                        dispatch({ type: 'LOGOUT' });
                                        navigate('/login');
                                    }}
                                    className="shadow-lg shadow-red-100"
                                >
                                    退出登录
                                </Button>
                            </div>
                        </div>
                    </div>
                )
            }
        </div >
    );
}
