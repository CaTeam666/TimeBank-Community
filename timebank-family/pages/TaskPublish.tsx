import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useTasks } from '../context/TaskContext';
import { NavBar, Button, Input, Card, Toast } from '../components/UIComponents';
import { TaskType, Task } from '../types';
import { Users } from 'lucide-react';
import { taskApi, TaskPublishRequest } from '../services/taskApi';
import { THEME_COLOR_BG, APP_NAME, TASK_TYPE_MAP } from '../constants'; // Import map

export default function TaskPublish() {
    const navigate = useNavigate();
    const { state, dispatch } = useAuth();
    const { addTask } = useTasks();

    // Always use current logged-in user
    const currentUser = state.currentUser;

    // Default to today
    const today = new Date().toISOString().split('T')[0];

    const [formData, setFormData] = useState<TaskPublishRequest>({
        title: '',
        type: '陪聊' as TaskType,
        date: today,
        timeRange: '09:00 - 10:00',
        location: '',
        description: '',
        coins: 20
    });

    // Validation State
    const [errors, setErrors] = useState<Partial<Record<keyof TaskPublishRequest, string>>>({});

    const [loading, setLoading] = useState(false);
    const [balance, setBalance] = useState(0);

    // Toast State
    const [toast, setToast] = useState({ show: false, message: '', type: 'info' as 'success' | 'error' | 'info' });

    const showToast = (message: string, type: 'success' | 'error' | 'info' = 'info') => {
        setToast({ show: true, message, type });
    };

    useEffect(() => {
        // Fetch latest balance
        const fetchBalance = async () => {
            try {
                const b = await taskApi.getUserBalance(currentUser?.id);
                setBalance(b);
            } catch (error) {
                console.error("Failed to fetch balance", error);
                // Fallback to local state if API fails
                if (currentUser) {
                    setBalance(currentUser.balance);
                }
            }
        };
        if (currentUser) {
            setBalance(currentUser.balance); // Initial set from local state
            fetchBalance();
        }
    }, [currentUser]);

    const validate = () => {
        const newErrors: Partial<Record<keyof TaskPublishRequest, string>> = {};
        if (!formData.title.trim()) newErrors.title = "请输入任务标题";
        if (!formData.location.trim()) newErrors.location = "请输入详细地址";
        if (!formData.description.trim()) newErrors.description = "请输入任务描述";
        if (!formData.date) newErrors.date = "请选择日期";
        if (!formData.timeRange) newErrors.timeRange = "请输入时间段";

        setErrors(newErrors);
        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = async () => {
        if (!currentUser) return;

        if (!validate()) {
            showToast("请完善填写信息", "error");
            return;
        }

        if (formData.coins > balance) {
            showToast("余额不足，请充值", "error");
            return;
        }

        // Use a custom confirmation or standard confirm. Standard confirm is okay for critical actions, 
        // but let's stick to standard confirm for now as "UI interface hints" usually refers to validation.
        if (window.confirm(`确认发布任务？将冻结 ${formData.coins} 积分`)) {
            setLoading(true);

            try {
                const res = await taskApi.publishTask({
                    ...formData,
                    type: TASK_TYPE_MAP[formData.type] as any || 'OTHER', // Map to English Enum
                    publisherId: currentUser.id
                });

                const newTask: Task = {
                    id: res.taskId || `t_${Date.now()}`,
                    publisherId: currentUser.id,
                    publisherName: currentUser.nickname,
                    publisherAvatar: currentUser.avatar,
                    type: formData.type,
                    title: formData.title,
                    description: formData.description,
                    coins: Number(formData.coins),
                    status: 'pending',
                    location: formData.location,
                    locationDetail: formData.location,
                    distance: '0.1km',
                    date: formData.date,
                    timeRange: formData.timeRange,
                    createdAt: Date.now()
                };

                dispatch({
                    type: 'UPDATE_BALANCE',
                    payload: { userId: currentUser.id, amount: balance - Number(formData.coins) }
                });

                addTask(newTask);

                // 根据是否在代理模式显示不同的成功提示
                const successMessage = state.isProxyMode && state.proxyTarget
                    ? `已为 ${state.proxyTarget.nickname} 发布任务！`
                    : '发布成功！';
                showToast(successMessage, "success");

                // Delay navigation slightly to show toast
                setTimeout(() => {
                    navigate('/task/hall');
                }, 1000);

            } catch (error: any) {
                console.error("Publish failed", error);
                showToast(`发布失败: ${error.message || '未知错误'}`, "error");
            } finally {
                setLoading(false);
            }
        }
    };

    if (!currentUser) return null;

    return (
        <div className="min-h-screen bg-gray-50 pb-20">
            <NavBar title="发布任务" showBack />

            <Toast
                message={toast.message}
                type={toast.type}
                isVisible={toast.show}
                onClose={() => setToast({ ...toast, show: false })}
            />

            <div className="p-4 space-y-4">
                {/* Proxy Mode Banner */}
                {state.isProxyMode && state.proxyTarget && (
                    <div className="bg-orange-100 border-l-4 border-orange-500 p-3 rounded-r-lg">
                        <div className="flex items-center gap-2">
                            <Users size={18} className="text-orange-600" />
                            <span className="text-sm font-medium text-orange-800">
                                您正在为 <span className="font-bold">{state.proxyTarget.nickname}</span> 发布任务
                            </span>
                        </div>
                    </div>
                )}

                {/* Form */}
                <div className="bg-white rounded-xl p-4 shadow-sm space-y-4">
                    <Input
                        label="任务标题"
                        placeholder="例如：帮我买袋米"
                        value={formData.title}
                        onChange={e => {
                            setFormData({ ...formData, title: e.target.value });
                            if (errors.title) setErrors({ ...errors, title: '' });
                        }}
                        error={errors.title}
                    />

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">服务类型</label>
                        <div className="flex flex-wrap gap-2">
                            {['陪聊', '保洁', '跑腿', '医疗陪护', '其他'].map(type => (
                                <button
                                    key={type}
                                    onClick={() => setFormData({ ...formData, type: type as TaskType })}
                                    className={`px-3 py-1.5 rounded text-sm ${formData.type === type ? 'bg-orange-100 text-orange-600 font-medium' : 'bg-gray-100 text-gray-600'
                                        }`}
                                >
                                    {type}
                                </button>
                            ))}
                        </div>
                    </div>

                    <div className="grid grid-cols-2 gap-3">
                        <Input
                            label="日期"
                            type="date"
                            min={today}
                            value={formData.date}
                            onChange={e => {
                                setFormData({ ...formData, date: e.target.value });
                                if (errors.date) setErrors({ ...errors, date: '' });
                            }}
                            error={errors.date}
                        />
                        <Input
                            label="时间段"
                            placeholder="如 14:00-16:00"
                            value={formData.timeRange}
                            onChange={e => {
                                setFormData({ ...formData, timeRange: e.target.value });
                                if (errors.timeRange) setErrors({ ...errors, timeRange: '' });
                            }}
                            error={errors.timeRange}
                        />
                    </div>

                    <Input
                        label="详细地址"
                        placeholder="请输入街道门牌号"
                        value={formData.location}
                        onChange={e => {
                            setFormData({ ...formData, location: e.target.value });
                            if (errors.location) setErrors({ ...errors, location: '' });
                        }}
                        error={errors.location}
                    />

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">任务描述</label>
                        <textarea
                            className={`w-full p-3 border rounded-lg text-sm h-24 focus:outline-none focus:border-orange-500 ${errors.description ? 'border-red-500' : 'border-gray-200'}`}
                            placeholder="请详细描述需求..."
                            value={formData.description}
                            onChange={e => {
                                setFormData({ ...formData, description: e.target.value });
                                if (errors.description) setErrors({ ...errors, description: '' });
                            }}
                        ></textarea>
                        {errors.description && <p className="mt-1 text-xs text-red-500">{errors.description}</p>}
                    </div>

                    <div>
                        <div className="flex justify-between items-center mb-2">
                            <label className="block text-sm font-medium text-gray-700">悬赏金额 (积分)</label>
                            <span className="text-xs text-orange-500 font-medium bg-orange-50 px-2 py-0.5 rounded">
                                余额: {balance}
                            </span>
                        </div>
                        <div className="flex items-center gap-4">
                            <button
                                onClick={() => setFormData(p => ({ ...p, coins: Math.max(10, p.coins - 10) }))}
                                className="w-8 h-8 rounded bg-gray-100 flex items-center justify-center text-lg font-bold"
                            >-</button>
                            <span className="text-xl font-bold w-12 text-center">{formData.coins}</span>
                            <button
                                onClick={() => setFormData(p => ({ ...p, coins: p.coins + 10 }))}
                                className="w-8 h-8 rounded bg-gray-100 flex items-center justify-center text-lg font-bold"
                            >+</button>
                        </div>
                    </div>
                </div>

                {/* Submit Bar */}
                <div className="fixed bottom-0 left-0 right-0 bg-white border-t p-4 flex items-center justify-between z-50">
                    <div>
                        <p className="text-xs text-gray-500">预冻结资金</p>
                        <p className={`text-xl font-bold ${formData.coins > balance ? 'text-red-500' : 'text-orange-500'}`}>
                            {formData.coins} <span className="text-xs">积分</span>
                        </p>
                    </div>
                    <Button onClick={handleSubmit} disabled={loading} className="w-32">
                        {loading ? '发布中...' : '确认发布'}
                    </Button>
                </div>
            </div>
        </div>
    );
}