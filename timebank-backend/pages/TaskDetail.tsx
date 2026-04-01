import React from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { ArrowLeft, Phone, MapPin, Clock, CreditCard, User } from 'lucide-react';
import { Task, TaskStatus } from '../types';
import { Badge } from '../components/ui/Badge';
import { missionMonitorApi } from '../services/missionMonitorApi';

export const TaskDetail: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [task, setTask] = React.useState<Task | null>(null);
    const [loading, setLoading] = React.useState(true);
    const [error, setError] = React.useState('');

    React.useEffect(() => {
        const fetchTask = async () => {
            if (!id) return;
            try {
                setLoading(true);
                const data = await missionMonitorApi.getTaskDetail(id);
                setTask(data);
            } catch (err) {
                console.error(err);
                setError('获取任务详情失败');
            } finally {
                setLoading(false);
            }
        };
        fetchTask();
    }, [id]);

    if (loading) return <div className="p-8 text-center text-gray-500">加载中...</div>;
    if (error) return <div className="p-8 text-center text-red-500">{error}</div>;
    if (!task) return <div className="p-8 text-center text-gray-500">未找到任务信息</div>;

    // Stepper Logic
    const steps = [
        { label: '发布', status: 'completed' },
        { label: '抢单', status: task.status !== TaskStatus.PENDING ? 'completed' : 'current' },
        { label: '执行', status: (task.status === TaskStatus.WAITING_ACCEPTANCE || task.status === TaskStatus.COMPLETED) ? 'completed' : (task.status === TaskStatus.IN_PROGRESS ? 'current' : 'upcoming') },
        { label: '验收', status: task.status === TaskStatus.COMPLETED ? 'completed' : (task.status === TaskStatus.WAITING_ACCEPTANCE ? 'current' : 'upcoming') },
        { label: '结算', status: task.status === TaskStatus.COMPLETED ? 'completed' : 'upcoming' },
    ];

    const getStepColor = (status: string) => {
        if (status === 'completed') return 'bg-blue-600 text-white border-blue-600';
        if (status === 'current') return 'bg-white text-blue-600 border-blue-600';
        return 'bg-white text-gray-400 border-gray-300';
    };

    return (
        <div className="space-y-6">
            {/* Header */}
            <div className="flex items-center gap-4">
                <button onClick={() => navigate(-1)} className="p-2 hover:bg-gray-200 rounded-full transition-colors">
                    <ArrowLeft className="w-5 h-5 text-gray-600" />
                </button>
                <div>
                    <h2 className="text-xl font-bold text-gray-900 flex items-center gap-3">
                        任务详情 #{task.id}
                        <Badge color={task.status === TaskStatus.COMPLETED ? 'green' : 'blue'}>{task.status}</Badge>
                    </h2>
                </div>
            </div>

            {/* Stepper */}
            <div className="bg-white p-8 rounded-lg shadow-sm border border-gray-100">
                <div className="flex items-center justify-between relative">
                    {/* Connecting Line */}
                    <div className="absolute left-0 top-1/2 transform -translate-y-1/2 w-full h-1 bg-gray-200 -z-10"></div>

                    {steps.map((step, index) => (
                        <div key={index} className="flex flex-col items-center bg-white px-4">
                            <div className={`w-8 h-8 rounded-full border-2 flex items-center justify-center text-sm font-bold ${getStepColor(step.status)}`}>
                                {index + 1}
                            </div>
                            <span className={`mt-2 text-sm font-medium ${step.status === 'upcoming' ? 'text-gray-400' : 'text-gray-900'}`}>{step.label}</span>
                        </div>
                    ))}
                </div>
            </div>

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Left Column: Info & People */}
                <div className="lg:col-span-2 space-y-6">
                    {/* Basic Info */}
                    <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
                        <div className="px-6 py-4 bg-gray-50 border-b border-gray-100 font-medium text-gray-900">
                            基础信息
                        </div>
                        <div className="p-6 space-y-4">
                            <div>
                                <h3 className="text-lg font-bold text-gray-900 mb-2">{task.title}</h3>
                                <p className="text-gray-600 text-sm leading-relaxed">{task.description}</p>
                            </div>
                            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 pt-4 border-t border-gray-100">
                                <div className="flex items-start">
                                    <MapPin className="w-5 h-5 text-gray-400 mr-2 mt-0.5" />
                                    <div>
                                        <span className="block text-xs text-gray-500">服务地址</span>
                                        <span className="text-sm font-medium text-gray-900">{task.address}</span>
                                    </div>
                                </div>
                                <div className="flex items-start">
                                    <Clock className="w-5 h-5 text-gray-400 mr-2 mt-0.5" />
                                    <div>
                                        <span className="block text-xs text-gray-500">截止时间</span>
                                        <span className="text-sm font-medium text-gray-900">{task.deadline}</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                    </div>

                    {/* People Info */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        {/* Creator */}
                        <div className="bg-white rounded-lg shadow-sm border border-gray-100 p-6">
                            <div className="flex items-center mb-4">
                                <div className="bg-orange-100 p-2 rounded-full mr-3">
                                    <User className="w-5 h-5 text-orange-600" />
                                </div>
                                <h4 className="font-bold text-gray-900">发布人 (老人)</h4>
                            </div>
                            <div className="flex items-center mb-4">
                                <img src={task.creatorAvatar} className="w-12 h-12 rounded-full mr-4" alt="Creator" />
                                <div>
                                    <div className="font-medium text-gray-900">{task.creatorName}</div>
                                    <div className="text-xs text-gray-500">信用分: {task.creatorCredit}</div>
                                </div>
                            </div>
                            <div className="flex items-center justify-between bg-gray-50 p-3 rounded text-sm">
                                <span className="text-gray-600">{task.creatorPhone}</span>
                                <a href={`tel:${task.creatorPhone}`} className="text-blue-600 hover:bg-blue-50 p-1 rounded">
                                    <Phone className="w-4 h-4" />
                                </a>
                            </div>
                        </div>

                        {/* Volunteer */}
                        <div className="bg-white rounded-lg shadow-sm border border-gray-100 p-6">
                            <div className="flex items-center mb-4">
                                <div className="bg-green-100 p-2 rounded-full mr-3">
                                    <User className="w-5 h-5 text-green-600" />
                                </div>
                                <h4 className="font-bold text-gray-900">接单志愿者</h4>
                            </div>
                            {task.volunteerId ? (
                                <>
                                    <div className="flex items-center mb-4">
                                        <img src={task.volunteerAvatar} className="w-12 h-12 rounded-full mr-4" alt="Volunteer" />
                                        <div>
                                            <div className="font-medium text-gray-900">{task.volunteerName}</div>
                                            <div className="text-xs text-gray-500">信用分: {task.volunteerCredit}</div>
                                        </div>
                                    </div>
                                    <div className="flex items-center justify-between bg-gray-50 p-3 rounded text-sm">
                                        <span className="text-gray-600">{task.volunteerPhone}</span>
                                        <a href={`tel:${task.volunteerPhone}`} className="text-blue-600 hover:bg-blue-50 p-1 rounded">
                                            <Phone className="w-4 h-4" />
                                        </a>
                                    </div>
                                </>
                            ) : (
                                <div className="h-32 flex items-center justify-center text-gray-400 bg-gray-50 rounded border border-dashed">
                                    暂无接单
                                </div>
                            )}
                        </div>
                    </div>
                </div>

                {/* Right Column: Finance & Timeline */}
                <div className="lg:col-span-1 space-y-6">
                    {/* Finance Card */}
                    <div className="bg-gradient-to-br from-blue-600 to-blue-800 rounded-lg shadow-lg p-6 text-white">
                        <div className="flex items-center justify-between mb-4">
                            <h4 className="font-medium opacity-90">资金状态</h4>
                            <CreditCard className="w-5 h-5 opacity-70" />
                        </div>
                        <div className="text-4xl font-bold mb-2 flex items-end">
                            {task.coins} <span className="text-lg ml-2 font-normal opacity-80">积分</span>
                        </div>
                        <div className="inline-block px-3 py-1 bg-white/20 rounded-full text-xs font-medium backdrop-blur-sm">
                            {task.status === TaskStatus.PENDING ? '资金冻结中' :
                                task.status === TaskStatus.COMPLETED ? '已支付给志愿者' :
                                    task.status === TaskStatus.CANCELLED ? '已退回发布者' : '托管中'}
                        </div>
                    </div>

                    {/* Timeline */}
                    <div className="bg-white rounded-lg shadow-sm border border-gray-100 h-full max-h-[500px] overflow-y-auto">
                        <div className="px-6 py-4 border-b border-gray-100 font-medium text-gray-900">
                            操作日志
                        </div>
                        <div className="p-6">
                            <ol className="relative border-l border-gray-200 ml-2">
                                {task.logs.map((log, idx) => (
                                    <li key={log.id} className="mb-6 ml-6">
                                        <span className="absolute flex items-center justify-center w-3 h-3 bg-blue-100 rounded-full -left-1.5 ring-4 ring-white">
                                            <span className="w-1.5 h-1.5 bg-blue-600 rounded-full"></span>
                                        </span>
                                        <time className="block mb-1 text-xs font-normal leading-none text-gray-400">{log.time}</time>
                                        <p className="text-sm font-normal text-gray-600">{log.content}</p>
                                    </li>
                                ))}
                            </ol>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};
