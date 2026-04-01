import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useTasks } from '../context/TaskContext';
import { useAuth } from '../context/AuthContext';
import { useUI } from '../context/UIContext';
import { NavBar, Button, Card, Modal } from '../components/UIComponents';

import { MapPin, Clock, ShieldCheck, MessageCircle, AlertCircle, CheckCircle2, Camera, X, Loader2 } from 'lucide-react';
import { THEME_COLOR_TEXT, TASK_TYPE_REVERSE_MAP } from '../constants';
import { taskApi } from '../services/taskApi';
import { Task, TaskStatus } from '../types';

export default function TaskDetail() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { acceptTask } = useTasks();
    const { state } = useAuth();
    const { showToast, showConfirm } = useUI();


    const [loading, setLoading] = useState(false);
    const [task, setTask] = useState<Task | null>(null);

    // Reply state
    const [showReplyModal, setShowReplyModal] = useState(false);
    const [replyContent, setReplyContent] = useState('');
    const [replyLoading, setReplyLoading] = useState(false);
    const [replyFile, setReplyFile] = useState<File | null>(null);
    const [replyPreview, setReplyPreview] = useState<string | null>(null);
    const fileInputRef = React.useRef<HTMLInputElement>(null);

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            setReplyFile(file);
            const reader = new FileReader();
            reader.onloadend = () => setReplyPreview(reader.result as string);
            reader.readAsDataURL(file);
        }
    };

    const handleReplySubmit = async () => {
        if (!replyContent.trim()) {
            showToast("请输入回应内容", "info");
            return;
        }
        if (!replyFile) {
            showToast("请上传证据照片", "info");
            return;
        }

        setReplyLoading(true);
        try {
            const { upload } = await import('../services/request');
            // 1. Upload image
            const uploadRes = await upload<string>('/file/upload', replyFile);
            const imageUrl = typeof uploadRes === 'string' ? uploadRes : (uploadRes as any).url || String(uploadRes);

            // 2. Submit reply
            await taskApi.replyAppeal(String(task?.id), String(state.currentUser?.id), replyContent, imageUrl);
            
            showToast("回应已提交", "success");
            setShowReplyModal(false);
            // Refresh detail
            window.location.reload();
        } catch (error: any) {
            console.error("Reply appeal failed", error);
            showToast(error.message || "提交失败，请重试", "error");
        } finally {
            setReplyLoading(false);
        }
    };

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
            showToast("请先登录", "info");
            navigate('/login');
            return;
        }

        if (task.publisherId === state.currentUser.id || (state.isProxyMode && task.publisherId === state.proxyTarget?.id)) {
            showToast("您不能接自己发布的任务", "error");
            return;
        }

        showConfirm({
            title: "确认接单",
            content: "确定要接下这个任务吗？接单后请准时履约。",
            onConfirm: async () => {
                setLoading(true);
                try {
                    await taskApi.acceptTask(task.id, state.currentUser!.id);
                    showToast("抢单成功！请尽快联系发布者", "success");
                    navigate('/task/orders');
                } catch (error: any) {
                    console.error("Accept task failed", error);
                    showToast(error.message || "手慢了，任务已被抢走", "error");
                } finally {
                    setLoading(false);
                }
            }
        });
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

                {/* Appeal Detail Card - Show when in appealing or completed with appeal history */}
                {appealDetail && (
                    <Card className="border-l-4 border-red-500">
                        <div className="flex justify-between items-center mb-4">
                            <h3 className="font-bold text-gray-800 flex items-center gap-2">
                                <AlertCircle size={18} className="text-red-500" />
                                申诉处理进度
                            </h3>
                            <div className="flex items-center gap-2">
                                {task.status === 'appealing' && 
                                 appealDetail.status === 0 && 
                                 state.currentUser?.id !== appealDetail.proposerId && 
                                 !appealDetail.defendantResponse && (
                                    <Button size="sm" onClick={() => setShowReplyModal(true)}>
                                        回应申诉
                                    </Button>
                                )}
                                <span className={`text-xs px-2 py-0.5 rounded-full ${getStatusColor('appealing')}`}>
                                    {appealDetail.status === 0 ? '待处理' : '已结案'}
                                </span>
                            </div>
                        </div>

                        <div className="space-y-4">
                            {/* Proposer Section */}
                            <div className="bg-gray-50 p-3 rounded-lg">
                                <div className="flex items-center gap-2 mb-2">
                                    <img src={appealDetail.proposerAvatar} className="w-5 h-5 rounded-full" />
                                    <span className="text-xs font-bold text-gray-700">{appealDetail.proposerName} (发起申诉)</span>
                                </div>
                                <p className="text-sm text-gray-600 mb-2">{appealDetail.reason}</p>
                                {appealDetail.evidenceImg && (
                                    <img 
                                        src={appealDetail.evidenceImg} 
                                        className="w-full h-32 object-cover rounded border" 
                                        onClick={() => window.open(appealDetail.evidenceImg)}
                                    />
                                )}
                            </div>

                            {/* Defendant Section */}
                            {appealDetail.defendantResponse && (
                                <div className="bg-blue-50 p-3 rounded-lg border border-blue-100">
                                    <div className="flex items-center gap-2 mb-2">
                                        <img src={appealDetail.defendantAvatar} className="w-5 h-5 rounded-full" />
                                        <span className="text-xs font-bold text-blue-700">{appealDetail.defendantName} (回应申诉)</span>
                                    </div>
                                    <p className="text-sm text-blue-800 mb-2">{appealDetail.defendantResponse}</p>
                                    {appealDetail.defendantEvidenceImg && (
                                        <img 
                                            src={appealDetail.defendantEvidenceImg} 
                                            className="w-full h-32 object-cover rounded border border-blue-200" 
                                            onClick={() => window.open(appealDetail.defendantEvidenceImg)}
                                        />
                                    )}
                                </div>
                            )}
                        </div>
                    </Card>
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
            {/* Reply Appeal Modal */}
            <Modal
                isOpen={showReplyModal}
                onClose={() => !replyLoading && setShowReplyModal(false)}
                title="回应申诉"
            >
                <div className="space-y-4 pt-2">
                    <div className="bg-blue-50 p-3 rounded-lg flex items-start gap-2 mb-2">
                        <AlertCircle size={16} className="text-blue-600 mt-0.5" />
                        <p className="text-xs text-blue-700">请针对申诉理由提供您的说明及证据照片，这有助于仲裁员做出公正裁决。</p>
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-1">回应说明</label>
                        <textarea
                            className="w-full px-3 py-2 border rounded-lg focus:ring-2 focus:ring-orange-500 min-h-[100px] text-sm"
                            placeholder="请描述您的回应理由..."
                            value={replyContent}
                            onChange={(e) => setReplyContent(e.target.value)}
                        />
                    </div>

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">上传证据照片</label>
                        <div 
                            onClick={() => fileInputRef.current?.click()}
                            className="aspect-video bg-gray-50 border-2 border-dashed border-gray-200 rounded-xl flex flex-col items-center justify-center relative overflow-hidden"
                        >
                            {replyPreview ? (
                                <>
                                    <img src={replyPreview} className="w-full h-full object-cover" alt="Preview" />
                                    <div className="absolute inset-0 bg-black/20 flex items-center justify-center">
                                        <div className="bg-white/80 p-2 rounded-full">
                                            <Camera size={24} className="text-gray-700" />
                                        </div>
                                    </div>
                                    <button 
                                        onClick={(e) => { e.stopPropagation(); setReplyFile(null); setReplyPreview(null); }}
                                        className="absolute top-2 right-2 bg-red-500 text-white p-1 rounded-full"
                                    >
                                        <X size={16} />
                                    </button>
                                </>
                            ) : (
                                <>
                                    <Camera size={32} className="text-gray-300 mb-2" />
                                    <p className="text-xs text-gray-400">点击拍照或选择照片</p>
                                </>
                            )}
                        </div>
                        <input 
                            type="file" 
                            ref={fileInputRef} 
                            className="hidden" 
                            accept="image/*" 
                            onChange={handleFileChange}
                        />
                    </div>

                    <div className="flex gap-3 pt-2">
                        <Button 
                            variant="secondary" 
                            fullWidth 
                            onClick={() => setShowReplyModal(false)}
                            disabled={replyLoading}
                        >
                            取消
                        </Button>
                        <Button 
                            fullWidth 
                            onClick={handleReplySubmit}
                            loading={replyLoading}
                        >
                            提交回应
                        </Button>
                    </div>
                </div>
            </Modal>
        </div>
    );
}
