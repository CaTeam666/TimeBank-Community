import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useTasks } from '../context/TaskContext';
import { useMessage } from '../context/MessageContext';
import { TaskType, TaskCategory } from '../types';
import { Card } from '../components/UIComponents';
import { taskApi } from '../services/taskApi';
import { MapPin, Clock, Filter, MessageCircle, Stethoscope, ShoppingBag, Briefcase, Zap, Bell } from 'lucide-react';
import { THEME_COLOR_TEXT, TASK_TYPE_MAP, TASK_TYPE_REVERSE_MAP } from '../constants';

export default function TaskHall() {
    const navigate = useNavigate();
    // Remove direct dependency on local context for LISTING, use API
    // const { tasks } = useTasks(); 
    const { unreadCount } = useMessage();
    const [activeTab, setActiveTab] = useState<string>('ALL'); // Use English enum key

    const [taskList, setTaskList] = useState<any[]>([]);
    const [loading, setLoading] = useState(false);
    const [categories, setCategories] = useState<TaskCategory[]>([]);
    const [categoriesLoading, setCategoriesLoading] = useState(false);

    // Fetch categories on mount
    const fetchCategories = async () => {
        setCategoriesLoading(true);
        try {
            const data = await taskApi.getTaskCategories();
            // Ensure "全部" is always the first option
            const allOption = { key: 'ALL', label: '全部' };
            const hasAll = data.some(cat => cat.key === 'ALL');
            const finalCategories = hasAll ? data : [allOption, ...data];
            setCategories(finalCategories);
        } catch (error) {
            console.error("Failed to fetch categories", error);
            // Fallback to hardcoded categories if API fails
            setCategories([
                { key: 'ALL', label: '全部' },
                { key: 'CHAT', label: '陪聊' },
                { key: 'CLEANING', label: '保洁' },
                { key: 'ERRAND', label: '跑腿' },
                { key: 'MEDICAL', label: '医疗陪护' },
                { key: 'OTHER', label: '其他' }
            ]);
        } finally {
            setCategoriesLoading(false);
        }
    };

    const fetchTasks = async () => {
        setLoading(true);
        try {
            // Use activeTab (English key) directly, pass undefined for 'ALL'
            const typeEnum = activeTab !== 'ALL' ? activeTab : undefined;
            const data = await taskApi.getTaskHallList(typeEnum);

            // Map backend task to frontend task interface if needed
            const mappedTasks = data.map(item => ({
                id: item.taskId,
                title: item.title,
                type: TASK_TYPE_REVERSE_MAP[item.type] || item.type || '其他', // Map English Enum back to Chinese
                coins: item.coins,
                location: item.location,
                date: item.date,
                timeRange: item.timeRange,
                publisherName: item.publisherName,
                status: item.status === 0 ? 'pending' : 'accepted', // 0: pending
                distance: item.distance || '未知距离',
                description: item.description,
                publisherId: item.publisherId,
                publisherAvatar: item.publisherAvatar || 'https://picsum.photos/200'
            }));
            setTaskList(mappedTasks);
        } catch (error) {
            console.error("Failed to fetch tasks", error);
        } finally {
            setLoading(false);
        }
    };

    React.useEffect(() => {
        fetchCategories();
    }, []);

    React.useEffect(() => {
        if (categories.length > 0) {
            fetchTasks();
        }
    }, [activeTab, categories]);

    // Icon mapping
    const getIcon = (type: TaskType) => {
        switch (type) {
            case '陪聊': return <MessageCircle className="text-green-500" />;
            case '保洁': return <Zap className="text-blue-500" />;
            case '跑腿': return <ShoppingBag className="text-orange-500" />;
            case '医疗陪护': return <Stethoscope className="text-red-500" />;
            default: return <Briefcase className="text-gray-500" />;
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 pb-20">
            {/* Sticky Header */}
            <div className="sticky top-0 z-30 bg-white shadow-sm">
                {/* Search/Location Bar */}
                <div className="px-4 py-3 flex items-center gap-3 text-sm text-gray-600 bg-white">
                    <div className="flex items-center gap-1 shrink-0">
                        <MapPin size={16} className={THEME_COLOR_TEXT} />
                        <span className="font-bold text-gray-800">幸福社区</span>
                    </div>

                    <div className="flex-1 bg-gray-100 rounded-full h-8 flex items-center px-3 text-gray-400">
                        搜索任务...
                    </div>

                    <button
                        onClick={() => navigate('/message/list')}
                        className="relative text-gray-600 active:opacity-50"
                    >
                        <Bell size={20} />
                        {unreadCount > 0 && (
                            <span className="absolute -top-1 -right-1 w-2 h-2 bg-red-500 rounded-full border border-white"></span>
                        )}
                    </button>
                </div>

                {/* Tabs */}
                <div className="flex overflow-x-auto px-2 pb-2 hide-scrollbar">
                    {categoriesLoading ? (
                        <div className="text-gray-400 text-sm px-4 py-2">加载中...</div>
                    ) : (
                        categories.map(cat => (
                            <button
                                key={cat.key}
                                onClick={() => setActiveTab(cat.key)}
                                className={`px-4 py-2 text-sm font-medium whitespace-nowrap transition-colors border-b-2 ${activeTab === cat.key
                                    ? `text-orange-500 border-orange-500`
                                    : 'text-gray-500 border-transparent'
                                    }`}
                            >
                                {cat.label}
                            </button>
                        ))
                    )}
                </div>

            </div>

            {/* Task List */}
            <div className="p-3 space-y-3">
                {loading ? (
                    <div className="text-center py-20 text-gray-400">加载中...</div>
                ) : taskList.length === 0 ? (
                    <div className="text-center py-20 text-gray-400">
                        <p>暂无相关任务</p>
                    </div>
                ) : (
                    taskList.map(task => (
                        <Card
                            key={task.id}
                            onClick={() => navigate(`/task/detail/${task.id}`)}
                            className="active:bg-gray-50 transition-colors"
                        >
                            <div className="flex gap-3">
                                {/* Left Icon */}
                                <div className="w-12 h-12 rounded-xl bg-gray-50 flex items-center justify-center shrink-0">
                                    {getIcon(task.type)}
                                </div>

                                {/* Content */}
                                <div className="flex-1 min-w-0">
                                    <div className="flex justify-between items-start">
                                        <h3 className="font-bold text-gray-800 truncate pr-2">{task.title}</h3>
                                        <span className="text-red-500 font-bold text-lg shrink-0 flex items-center gap-1">
                                            <Clock size={16} className="text-orange-500" />{task.coins}
                                        </span>
                                    </div>

                                    <div className="flex items-center gap-2 mt-1 text-xs text-gray-500">
                                        <span className="bg-gray-100 px-1.5 py-0.5 rounded text-gray-600">{task.publisherName}</span>
                                        <span>|</span>
                                        <span className="flex items-center gap-0.5">
                                            <MapPin size={10} /> {task.location}
                                        </span>
                                    </div>

                                    <div className="flex justify-between items-end mt-3">
                                        <div className="flex items-center gap-1 text-xs text-orange-600 bg-orange-50 px-2 py-1 rounded-lg">
                                            <Clock size={12} />
                                            {task.date} {task.timeRange}
                                        </div>
                                        <span className="text-xs text-gray-400">{task.distance}</span>
                                    </div>
                                </div>
                            </div>
                        </Card>
                    ))
                )}
            </div>
        </div>
    );
}
