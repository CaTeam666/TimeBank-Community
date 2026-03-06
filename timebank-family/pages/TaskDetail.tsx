import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTasks } from '../context/TaskContext';
import { useAuth } from '../context/AuthContext';
import { NavBar, Button, Card } from '../components/UIComponents';
import { MapPin, Clock, ShieldCheck, MessageCircle, AlertCircle, CheckCircle2 } from 'lucide-react';
import { THEME_COLOR_TEXT, TASK_TYPE_REVERSE_MAP } from '../constants';
import { taskApi } from '../services/taskApi';
import { Task, TaskStatus } from '../types';

export default function TaskDetail() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { acceptTask } = useTasks();
    const { state } = useAuth();

    const [loading, setLoading] = useState(false);
    const [task, setTask] = useState<Task | null>(null);

    // Map backend status to frontend status
    const mapStatus = (status: number | string): TaskStatus => {
        const numStatus = typeof status === 'string' ? parseInt(status, 10) : status;
        console.log('mapStatus called with:', status, '-> parsed:', numStatus);
        switch (numStatus) {
            case 0: return 'pending';           // 待接取
            case 1: return 'accepted';          // 进行中
            case 2: return 'waiting_acceptance'; // 待验收
            case 3: return 'completed';         // 已完成
            case 4: return 'appealing';         // 申诉中
            case 5: return 'cancelled';         // 申诉裁决后已完成 (UI显示为已取消)
            case 6: return 'cancelled';         // 已取消
            default: return 'pending';
        }
    };

    const [appealDetail, setAppealDetail] = useState<any>(null);

    useEffect(() => {
        const fetchTask = async () => {
            if (!id) return;
            try {
                const data = await taskApi.getTaskDetail(id);
                console.log('API returned task data:', data);

                // Parse status to ensure numeric comparison
                const currentStatus = Number(data.status);

                // Try to fetch appeal details regardless of status (to show history/results if any)
                try {
                    const ad = await taskApi.getAppealDetail(data.taskId);
                    if (ad) {
                        console.log('Appeal details found:', ad);
                        setAppealDetail(ad);
                    }
                } catch (e) {
                    // It's normal to not have appeal details for most tasks
                    console.log('No appeal details found or fetch failed (normal for non-appealed tasks)');
                }

                // Map backend data to frontend Task type
                const mappedTask: Task = {
                    id: data.taskId,
                    title: data.title,
                    type: TASK_TYPE_REVERSE_MAP[data.type] || data.type || '其他', // Map to Chinese for frontend consistency
                    description: data.description,
                    coins: data.coins,
                    status: mapStatus(data.status),
                    location: data.location,
                    distance: '未知距离', // Detail API might not return this
                    date: data.date,
                    timeRange: data.timeRange,
                    createdAt: Date.now(), // Approximate
                    acceptorId: data.volunteerId, // Map volunteerId for check
                    publisherId: data.publisherId,
                    publisherName: data.publisherName,
                    publisherAvatar: data.publisherAvatar || 'https://picsum.photos/200'
                };
                setTask(mappedTask);
            } catch (error) {
                console.error("Failed to fetch task detail", error);
            }
        };
        fetchTask();
    }, [id]);

    const getStatusLabel = (status: string): string => {
        switch (status) {
            case 'pending': return '待接单';
            case 'accepted': return '进行中';
            case 'waiting_acceptance': return '待验收';
            case 'completed': return '已完成';
            case 'appealing': return '申诉中';
            case 'cancelled': return '已取消';
            default: return '未知状态';
        }
    };

    const getStatusColor = (status: string): string => {
        switch (status) {
            case 'pending': return 'bg-gray-100 text-gray-600';
            case 'accepted': return 'bg-blue-50 text-blue-600';
            case 'waiting_acceptance': return 'bg-orange-50 text-orange-600';
            case 'completed': return 'bg-green-50 text-green-600';
            case 'appealing': return 'bg-red-50 text-red-600';
            case 'cancelled': return 'bg-gray-100 text-gray-400';
            default: return 'bg-gray-100 text-gray-500';
        }
    };

    if (!task) {
        return <div className="p-10 text-center">加载中...</div>;
    }

    const handleAccept = async () => {
        if (!state.currentUser) {
            alert("请先登录");
            navigate('/login');
            return;
        }

        if (task.publisherId === state.currentUser.id || (state.isProxyMode && task.publisherId === state.proxyTarget?.id)) {
            alert("您不能接自己发布的任务");
            return;
        }

        if (window.confirm("确定要接下这个任务吗？接单后请准时履约。")) {
            setLoading(true);
            try {
                await taskApi.acceptTask(task.id, state.currentUser!.id);
                alert("抢单成功！请尽快联系发布者。");
                navigate('/task/orders');
            } catch (error: any) {
                console.error("Accept task failed", error);
                alert(error.message || "手慢了，任务已被抢走");
            } finally {
                setLoading(false);
            }
        }
    };

    const isAcceptedByMe = task.status === 'accepted' && task.acceptorId === state.currentUser?.id;

    return (
        <div className="min-h-screen bg-gray-50 pb-24">
            <NavBar title="任务详情" showBack />

            <div className="px-4 mt-4 space-y-3">
                {/* Adjudication Result Card - Only for appeal completed tasks */}
                {appealDetail && appealDetail.handlingResult && (
                    <div className="bg-green-50 p-4 rounded-xl flex items-start gap-3 border border-green-100">
                        <ShieldCheck className="text-green-600 mt-1" size={24} />
                        <div>
                            <p className="font-bold text-green-800 mb-1">申诉裁决结果：{appealDetail.handlingResult}</p>
                            <p className="text-sm text-green-700 bg-white/50 p-2 rounded">
                                {appealDetail.handlingReason}
                            </p>
                        </div>
                    </div>
                )}

                {/* Main Info Card */}
                {/* ... existing Card ... */}
                <Card>
                    <div className="flex justify-between items-start mb-2">
                        <h1 className="text-xl font-bold text-gray-800 leading-tight">{task.title}</h1>
                        <span className="text-2xl font-bold text-red-500 shrink-0">
                            <span className="text-sm font-normal text-gray-500 mr-1">悬赏</span>
                            {task.coins}
                        </span>
                    </div>

                    <div className="flex gap-2 mb-4">
                        <span className="px-2 py-0.5 bg-gray-100 text-gray-600 text-xs rounded-full">{TASK_TYPE_REVERSE_MAP[task.type] || task.type}</span>
                    </div>

                    <p className="text-gray-600 text-sm leading-relaxed mb-4 border-t border-gray-100 pt-3">
                        {task.description}
                    </p>

                    <div className="space-y-3 text-sm">
                        <div className="flex items-start gap-3">
                            <Clock className="text-gray-400 shrink-0 mt-0.5" size={16} />
                            <div>
                                <p className="font-medium text-gray-700">服务时间</p>
                                <p className="text-gray-500">{task.date} {task.timeRange}</p>
                            </div>
                        </div>
                        <div className="flex items-start gap-3">
                            <MapPin className="text-gray-400 shrink-0 mt-0.5" size={16} />
                            <div>
                                <p className="font-medium text-gray-700">服务地点</p>
                                <p className={`text-gray-500 ${isAcceptedByMe ? 'text-green-600 font-bold' : ''}`}>
                                    {isAcceptedByMe ? task.locationDetail : task.location}
                                </p>
                            </div>
                        </div>
                    </div>
                </Card>

                {/* Safety Notice */}
                <div className="bg-orange-50 px-4 py-2 rounded-lg flex items-center gap-2 text-xs text-orange-700">
                    <AlertCircle size={14} />
                    <span className="marquee">接单后请准时服务，无故爽约将扣除信用分。</span>
                </div>

                {/* Publisher Info */}
                <Card className="flex items-center gap-4">
                    <img src={task.publisherAvatar} alt="Avatar" className="w-10 h-10 rounded-full bg-gray-200" />
                    <div>
                        <p className="font-bold text-gray-800">{task.publisherName}</p>
                    </div>
                </Card>
            </div>

            {/* Bottom Action Bar */}
            <div className="fixed bottom-0 left-0 right-0 max-w-md mx-auto bg-white border-t border-gray-100 p-3 flex gap-3 z-50">
                <button className="flex flex-col items-center justify-center px-4 text-gray-500 text-xs gap-1">
                    <MessageCircle size={20} />
                    客服
                </button>

                {isAcceptedByMe ? (
                    <Button fullWidth className="bg-green-500" onClick={() => navigate(`/task/execute/${task.id}`)}>
                        <CheckCircle2 size={18} className="mr-2" />
                        已接单，去执行
                    </Button>
                ) : task.status === 'pending' ? (
                    <Button fullWidth onClick={handleAccept} disabled={loading}>
                        {loading ? '抢单中...' : '立即抢单'}
                    </Button>
                ) : (
                    <Button fullWidth variant="secondary" disabled className="flex items-center justify-center gap-2">
                        <span className={`px-2 py-0.5 rounded text-xs ${getStatusColor(task.status)}`}>
                            {getStatusLabel(task.status)}
                        </span>
                    </Button>
                )}
            </div>
        </div>
    );
}
