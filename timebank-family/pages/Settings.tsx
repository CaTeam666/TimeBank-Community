import React from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { NavBar, Button, Card } from '../components/UIComponents';
import { LogOut, ChevronRight, User, Shield, HelpCircle, FileText } from 'lucide-react';

export default function Settings() {
    const navigate = useNavigate();
    const { dispatch } = useAuth();
    const [showLogoutConfirm, setShowLogoutConfirm] = React.useState(false);

    const handleLogoutClick = () => {
        setShowLogoutConfirm(true);
    };

    const confirmLogout = () => {
        dispatch({ type: 'LOGOUT' });
        navigate('/login');
    };

    const MenuRow = ({ icon: Icon, label, onClick, danger = false }: any) => (
        <div
            onClick={onClick}
            className="flex items-center justify-between p-4 bg-white active:bg-gray-50 border-b border-gray-100 last:border-none cursor-pointer"
        >
            <div className="flex items-center gap-3">
                <Icon size={20} className={danger ? "text-red-500" : "text-gray-500"} />
                <span className={`text-base font-medium ${danger ? "text-red-600" : "text-gray-800"}`}>{label}</span>
            </div>
            <ChevronRight size={18} className="text-gray-300" />
        </div>
    );

    return (
        <div className="min-h-screen bg-gray-50 pb-8 relative">
            <NavBar title="设置" showBack />

            <div className="mt-4 space-y-4">
                <div className="bg-white border-y border-gray-100">
                    <MenuRow icon={User} label="个人资料" onClick={() => { }} />
                    <MenuRow icon={Shield} label="账号安全" onClick={() => { }} />
                </div>

                <div className="bg-white border-y border-gray-100">
                    <MenuRow icon={FileText} label="用户协议" onClick={() => { }} />
                    <MenuRow icon={HelpCircle} label="帮助与反馈" onClick={() => { }} />
                </div>

                <div className="px-4 mt-8">
                    <Button
                        fullWidth
                        className="bg-white border border-red-200 text-red-600 hover:bg-red-50 font-bold shadow-sm"
                        onClick={handleLogoutClick}
                    >
                        <div className="flex items-center justify-center gap-2">
                            <LogOut size={18} />
                            退出登录
                        </div>
                    </Button>
                </div>

                <div className="text-center text-xs text-gray-400 mt-8">
                    Version 1.0.0
                </div>
            </div>

            {/* Logout Confirmation Modal */}
            {showLogoutConfirm && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 animate-fade-in">
                    <div className="bg-white rounded-2xl w-full max-w-xs p-6 text-center shadow-2xl scale-100 transition-transform">
                        <div className="w-12 h-12 rounded-full bg-red-50 flex items-center justify-center mx-auto mb-4">
                            <LogOut className="text-red-500" size={24} />
                        </div>
                        <h3 className="text-lg font-bold text-gray-900 mb-2">确认退出登录？</h3>
                        <p className="text-gray-500 text-sm mb-6">退出后您需要重新输入账号密码登录。</p>

                        <div className="flex gap-3">
                            <Button
                                variant="secondary"
                                fullWidth
                                onClick={() => setShowLogoutConfirm(false)}
                            >
                                取消
                            </Button>
                            <Button
                                variant="danger"
                                fullWidth
                                onClick={confirmLogout}
                            >
                                确认退出
                            </Button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
