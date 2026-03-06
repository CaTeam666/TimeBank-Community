import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Task, TaskStatus } from '../types';
import { NavBar, Card, Button } from '../components/UIComponents';
import { MapPin, Clock, User, Filter, Loader2 } from 'lucide-react';
import { taskApi } from '../services/taskApi';
import { TASK_TYPE_REVERSE_MAP } from '../constants';

interface OrderTask {
    id: string;
    title: string;
    type: string;
    coins: number;
    location: string;
    date: string;
    timeRange: string;
    status: TaskStatus;
    publisherName?: string;
    acceptorName?: string;
}

export default function TaskOrders() {
    const navigate = useNavigate();
    const { state } = useAuth();

    const [mainTab, setMainTab] = useState<0 | 1>(0);
    const [statusFilter, setStatusFilter] = useState<string>('all');
    const [tasks, setTasks] = useState<OrderTask[]>([]);
    const [loading, setLoading] = useState(false);

    const currentUser = state.currentUser;

    // Map backend status to frontend status
    const mapStatus = (status: number): TaskStatus => {
        switch (status) {
            case 0: return 'pending';
            case 1: return 'accepted';
            case 2: return 'waiting_acceptance';
            case 3: return 'completed';
            case 4: return 'appealing';
            case 5: return 'cancelled';
            case 6: return 'cancelled';
            default: return 'pending';
        }
    };

    const fetchTasks = async () => {
        setLoading(true);
        try {
            let statusParam: number | undefined;
            if (statusFilter === 'pending') statusParam = 0;
            else if (statusFilter === 'in_progress') statusParam = 1;
            else if (statusFilter === 'waiting') statusParam = 2;
            else if (statusFilter === 'completed') statusParam = 3;
            else if (statusFilter === 'appealing') statusParam = 4;
            else if (statusFilter === 'cancelled') statusParam = 6;

            let data: any[];
            if (statusParam === undefined) {
                // "All" filter: Fetch default + Appeal/Cancelled related statuses manually
                // because backend might exclude them by default
                const fetchFn = mainTab === 0 ? taskApi.getMyAcceptedOrders : taskApi.getMyPublishedOrders;

                const [defaultList, appealingList, appealCompletedList, cancelledList] = await Promise.all([
                    fetchFn(currentUser.id, undefined),
                    fetchFn(currentUser.id, 4), // Appealing
                    fetchFn(currentUser.id, 5), // Appeal Completed
                    fetchFn(currentUser.id, 6)  // Cancelled
                ]);

                // Merge and Deduplicate by taskId
                const allItems = [...defaultList, ...appealingList, ...appealCompletedList, ...cancelledList];
                const uniqueItems = Array.from(new Map(allItems.map(item => [item.taskId, item])).values());

                // Sort by date desc (assuming backend returns sorted, but merging breaks it)
                // If date is 'YYYY-MM-DD', it might not be enough for precise sort, but let's try our best or rely on id/createTime if available. 
                // Taking simple approach: just use the order
                data = uniqueItems;
            } else {
                if (mainTab === 0) {
                    data = await taskApi.getMyAcceptedOrders(currentUser.id, statusParam);
                } else {
                    data = await taskApi.getMyPublishedOrders(currentUser.id, statusParam);
                }
            }

            const mappedTasks: OrderTask[] = data.map((item: any) => ({
                id: item.taskId,
                title: item.title,
                type: TASK_TYPE_REVERSE_MAP[item.type] || item.type,
                coins: item.coins,
                location: item.location,
                date: item.date,
                timeRange: item.timeRange,
                status: mapStatus(item.status),
                publisherName: item.publisherName,
                acceptorName: item.volunteerName || item.acceptorName
            }));
            setTasks(mappedTasks);
        } catch (error) {
            console.error('Failed to fetch orders', error);
            setTasks([]);
        } finally {
            setLoading(false);
        }
    };


    // Fetch tasks when tab or filter changes
    useEffect(() => {
        if (!currentUser) return;
        fetchTasks();
    }, [currentUser, mainTab, statusFilter]);

    const handleCancelTask = async (taskId: string, e: React.MouseEvent) => {
        e.stopPropagation(); // Stop card click
        if (window.confirm('确定要取消该任务吗？取消后任务将变为已取消状态。')) {
            try {
                await taskApi.cancelTask(taskId, currentUser.id);
                alert('任务已取消');
                fetchTasks(); // Refresh list
            } catch (error) {
                console.error('Failed to cancel task', error);
                alert('取消失败，请重试');
            }
        }
    };

    if (!currentUser) return (
        <div className="p-8 text-center text-gray-500">
            请先登录查看订单
            <Button onClick={() => navigate('/login')} className="mt-4">去登录</Button>
        </div>
    );

    const getStatusLabel = (status: TaskStatus) => {
        switch (status) {
            case 'pending': return '待接单';
            case 'accepted': return '进行中';
            case 'waiting_acceptance': return '待验收';
            case 'completed': return '已完成';
            case 'appealing': return '申诉中';
            case 'cancelled': return '已取消';
            default: return status;
        }
    };

    const getStatusColor = (status: TaskStatus) => {
        switch (status) {
            case 'pending': return 'text-gray-600 bg-gray-100';
            case 'accepted': return 'text-blue-600 bg-blue-50';
            case 'waiting_acceptance': return 'text-orange-600 bg-orange-50';
            case 'completed': return 'text-green-600 bg-green-50';
            case 'appealing': return 'text-red-600 bg-red-50';
            case 'cancelled': return 'text-gray-400 bg-gray-100';
            default: return 'text-gray-500 bg-gray-100';
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 pb-20">
            <NavBar title="我的订单" showBack />

            {/* Main Switch */}
            <div className="bg-white px-4 pt-2 pb-0 flex border-b border-gray-100 sticky top-12 z-20">
                <button
                    onClick={() => { setMainTab(0); setStatusFilter('all'); }}
                    className={`flex-1 py-3 text-center font-medium border-b-2 transition-colors ${mainTab === 0 ? 'text-orange-500 border-orange-500' : 'text-gray-500 border-transparent'}`}
                >
                    我接的单
                </button>
                <button
                    onClick={() => { setMainTab(1); setStatusFilter('all'); }}
                    className={`flex-1 py-3 text-center font-medium border-b-2 transition-colors ${mainTab === 1 ? 'text-orange-500 border-orange-500' : 'text-gray-500 border-transparent'}`}
                >
                    我发的单
                </button>
            </div>

            {/* Status Filter */}
            <div className="bg-white py-3 px-4 flex flex-wrap gap-2 sticky top-[98px] z-20 shadow-sm">
                {[
                    { id: 'all', label: '全部' },
                    { id: 'pending', label: '待接单', show: mainTab === 1 },
                    { id: 'in_progress', label: '进行中' },
                    { id: 'waiting', label: '待验收' },
                    { id: 'completed', label: '已完成' },
                    { id: 'appealing', label: '申诉中' },
                    { id: 'cancelled', label: '已取消' }
                ].filter(f => f.show !== false).map(filter => (
                    <button
                        key={filter.id}
                        onClick={() => setStatusFilter(filter.id)}
                        className={`px-3 py-1 rounded-full text-xs font-medium whitespace-nowrap transition-colors ${statusFilter === filter.id
                            ? 'bg-orange-100 text-orange-600'
                            : 'bg-gray-100 text-gray-600'
                            }`}
                    >
                        {filter.label}
                    </button>
                ))}
            </div>

            {/* List */}
            <div className="p-4 space-y-4">
                {loading ? (
                    <div className="flex flex-col items-center justify-center py-20 text-gray-400">
                        <Loader2 size={32} className="animate-spin mb-3" />
                        <p className="text-sm">加载中...</p>
                    </div>
                ) : tasks.length === 0 ? (
                    <div className="flex flex-col items-center justify-center py-20 text-gray-400">
                        <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mb-3 text-gray-300">
                            <Filter size={32} />
                        </div>
                        <p className="text-sm">
                            {mainTab === 0
                                ? (statusFilter === 'all' ? "您还没有接过任何订单" : "没有符合状态的接单记录")
                                : (statusFilter === 'all' ? "您还没有发布过任务" : "没有符合状态的发布记录")
                            }
                        </p>
                        {mainTab === 1 && statusFilter === 'all' && (
                            <Button size="sm" className="mt-4" onClick={() => navigate('/task/publish')}>去发布任务</Button>
                        )}
                        {mainTab === 0 && statusFilter === 'all' && (
                            <Button size="sm" className="mt-4" onClick={() => navigate('/task/hall')}>去任务大厅抢单</Button>
                        )}
                    </div>
                ) : (
                    tasks.map(task => (
                        <Card key={task.id} onClick={() => {
                            if (mainTab === 0 && task.status === 'accepted') {
                                navigate(`/task/execute/${task.id}`);
                            } else if (mainTab === 0 && task.status === 'waiting_acceptance') {
                                // 志愿者查看待验收任务详情（等待发布者验收）
                                navigate(`/task/detail/${task.id}`);
                            } else if (mainTab === 1 && task.status === 'waiting_acceptance') {
                                navigate(`/task/review/${task.id}`);
                            } else if (task.status === 'appealing') {
                                navigate(`/task/review/${task.id}`);
                            } else {
                                navigate(`/task/detail/${task.id}`);
                            }
                        }}>
                            <div className="flex justify-between items-start mb-3">
                                <div className="flex items-center gap-2">
                                    <span className={`text-xs px-2 py-1 rounded ${getStatusColor(task.status)}`}>
                                        {getStatusLabel(task.status)}
                                    </span>
                                    <h3 className="font-bold text-gray-800 line-clamp-1">{task.title}</h3>
                                </div>
                                <span className="font-bold text-red-500 shrink-0">¥{task.coins}</span>
                            </div>

                            <div className="space-y-2 text-xs text-gray-500 mb-4 bg-gray-50 p-2 rounded-lg">
                                <div className="flex items-center gap-2">
                                    <Clock size={12} className="text-gray-400" /> {task.date} {task.timeRange}
                                </div>
                                <div className="flex items-center gap-2">
                                    <MapPin size={12} className="text-gray-400" /> {task.location}
                                </div>
                                {mainTab === 1 && (
                                    <div className="flex items-center gap-2 text-blue-600 mt-1">
                                        <User size={12} />
                                        {task.acceptorName ? `接单人：${task.acceptorName}` : '暂无接单人'}
                                    </div>
                                )}
                                {mainTab === 0 && (
                                    <div className="flex items-center gap-2 mt-1">
                                        <User size={12} /> 发布人：{task.publisherName}
                                    </div>
                                )}
                            </div>

                            <div className="flex justify-end pt-2">
                                {mainTab === 0 && task.status === 'accepted' && (
                                    <Button size="sm" className="w-24" onClick={(e) => { e.stopPropagation(); navigate(`/task/execute/${task.id}`); }}>去履约</Button>
                                )}
                                {mainTab === 1 && task.status === 'waiting_acceptance' && (
                                    <Button size="sm" className="w-24" onClick={(e) => { e.stopPropagation(); navigate(`/task/review/${task.id}`); }}>去验收</Button>
                                )}
                                {mainTab === 1 && task.status === 'pending' && (
                                    <Button size="sm" variant="secondary" className="w-24 text-xs" onClick={(e) => handleCancelTask(task.id, e)}>取消任务</Button>
                                )}
                                {task.status === 'completed' && (
                                    <Button size="sm" variant="outline" className="w-24 text-xs" onClick={(e) => { e.stopPropagation(); navigate(`/task/detail/${task.id}`); }}>查看凭证</Button>
                                )}
                            </div>
                        </Card>
                    ))
                )}
            </div>
        </div>
    );
}