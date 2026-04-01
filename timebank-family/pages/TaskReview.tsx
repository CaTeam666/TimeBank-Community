import React, { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { NavBar, Button, Card, Modal, Input } from '../components/UIComponents';
import { CheckCircle2, AlertTriangle, Star, Loader2, Camera, X } from 'lucide-react';
import { taskApi, TaskReviewDetail } from '../services/taskApi';

export default function TaskReview() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { state } = useAuth();

    const [showRateModal, setShowRateModal] = useState(false);
    const [showAppealModal, setShowAppealModal] = useState(false);

    // Rating form
    const [rating, setRating] = useState(5);
    const [review, setReview] = useState('');

    // Appeal form
    const [appealReason, setAppealReason] = useState('');
    const [appealFile, setAppealFile] = useState<File | null>(null);
    const [appealPreview, setAppealPreview] = useState<string | null>(null);
    
    // Reply form
    const [replyContent, setReplyContent] = useState('');
    const [replyFile, setReplyFile] = useState<File | null>(null);
    const [replyPreview, setReplyPreview] = useState<string | null>(null);

    const fileInputRef = React.useRef<HTMLInputElement>(null);
    const replyFileInputRef = React.useRef<HTMLInputElement>(null);

    // Data states
    const [task, setTask] = useState<TaskReviewDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);

    const [appealDetail, setAppealDetail] = useState<any>(null);

    // 加载验收详情
    useEffect(() => {
        const fetchReviewDetail = async () => {
            if (!id) return;

            try {
                setLoading(true);
                setError(null);
                const data = await taskApi.getReviewDetail(id);

                // If adjudication is completed (status 5), redirect to task detail to show result
                if (data.status === 5) {
                    navigate(`/task/detail/${id}`, { replace: true });
                    return;
                }

                setTask(data);

                // Fetch extra appeal details if applicable
                if (data.status === 4) {
                    try {
                        const ad = await taskApi.getAppealDetail(data.taskId);
                        setAppealDetail(ad);
                    } catch (e) {
                        console.error('Failed to fetch appeal detail', e);
                    }
                }

            } catch (err) {
                console.error('获取验收详情失败:', err);
                setError('加载验收详情失败，请重试');
            } finally {
                setLoading(false);
            }
        };

        fetchReviewDetail();
    }, [id]);

    const handleConfirm = async () => {
        // ... (rest of handleConfirm)
        console.log('handleConfirm called', { task, user: state.currentUser });

        if (!task) {
            alert('任务数据未加载，请刷新页面');
            return;
        }
        if (!state.currentUser?.id) {
            alert('用户未登录，请重新登录');
            return;
        }

        try {
            setSubmitting(true);
            // 1. 确认验收
            await taskApi.confirmReview(task.taskId, state.currentUser.id.toString(), rating, review);

            // 2. 提交评价（验收成功后）
            await taskApi.submitVolunteerReview(
                task.taskId,
                state.currentUser.id.toString(),
                task.volunteerId,
                rating,
                review
            );

            setShowRateModal(false);
            alert(`验收成功！已支付 ${task.coins} 币给志愿者。`);
            navigate('/task/orders');
        } catch (err) {
            console.error('验收失败:', err);
            alert('验收失败，请重试');
        } finally {
            setSubmitting(false);
        }
    };

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>, type: 'appeal' | 'reply') => {
        const file = e.target.files?.[0];
        if (file) {
            if (type === 'appeal') {
                setAppealFile(file);
                const reader = new FileReader();
                reader.onloadend = () => setAppealPreview(reader.result as string);
                reader.readAsDataURL(file);
            } else {
                setReplyFile(file);
                const reader = new FileReader();
                reader.onloadend = () => setReplyPreview(reader.result as string);
                reader.readAsDataURL(file);
            }
        }
    };

    const handleAppeal = async () => {
        if (!state.currentUser || !task) return;
        if (!appealFile) {
            alert('请上传证据图片');
            return;
        }

        try {
            setSubmitting(true);
            const { upload } = await import('../services/request');
            
            // 1. Upload image
            const uploadRes = await upload<string>('/file/upload', appealFile);
            const imageUrl = typeof uploadRes === 'string' ? uploadRes : (uploadRes as any).url || String(uploadRes);

            // 2. Submit appeal
            await taskApi.submitAppeal(task.taskId, state.currentUser.id.toString(), appealReason, imageUrl);
            
            // 申诉成功后，更新本地任务状态为 4（申诉中）
            setTask({ ...task, status: 4 });
            setShowAppealModal(false);
            alert("申诉已提交，客服将介入处理。");
            navigate('/task/orders');
        } catch (err: any) {
            console.error('申诉失败:', err);
            alert(err.message || '申诉提交失败，请重试');
        } finally {
            setSubmitting(false);
        }
    };

    const handleReply = async () => {
        if (!state.currentUser || !task) return;
        if (!replyFile) {
            alert('请上传证据图片以支持您的回应');
            return;
        }

        try {
            setSubmitting(true);
            const { upload } = await import('../services/request');

            // 1. Upload image
            const uploadRes = await upload<string>('/file/upload', replyFile);
            const imageUrl = typeof uploadRes === 'string' ? uploadRes : (uploadRes as any).url || String(uploadRes);

            // 2. Submit reply
            await taskApi.replyAppeal(task.taskId, state.currentUser.id.toString(), replyContent, imageUrl);
            
            // 更新本地状态显示回应
            setTask({
                ...task,
                appeal: {
                    ...task.appeal!,
                    response: replyContent,
                    responseTime: new Date().toISOString()
                }
            });
            // 重新获取详情以显示新图片
            const ad = await taskApi.getAppealDetail(task.taskId);
            setAppealDetail(ad);
            alert("回应已提交");
        } catch (err: any) {
            console.error('回应失败:', err);
            alert(err.message || '回应提交失败，请重试');
        } finally {
            setSubmitting(false);
        }
    };

    // 加载状态
    if (loading) {
        return (
            <div className="min-h-screen bg-gray-50 flex items-center justify-center">
                <Loader2 className="animate-spin text-orange-500" size={32} />
            </div>
        );
    }

    // 错误状态
    if (error || !task) {
        return (
            <div className="min-h-screen bg-gray-50">
                <NavBar title="验收确认" showBack />
                <div className="p-4 text-center text-gray-500">
                    {error || '任务不存在'}
                </div>
            </div>
        );
    }

    // 申诉详情视图
    if (task.status === 4) {
        return (
            <div className="min-h-screen bg-gray-50 pb-24">
                <NavBar title="申诉详情" showBack />
                <div className="p-4 space-y-4">
                    {/* Appeal Status - Only show if defendant has responded */}
                    {(task.appeal?.response || appealDetail?.defendantResponse) && (
                        <div className="bg-red-50 p-4 rounded-xl flex items-center gap-3 border border-red-100">
                            <AlertTriangle className="text-red-500" size={24} />
                            <div>
                                <p className="font-bold text-red-800">申诉处理中</p>
                                <p className="text-xs text-red-600">平台客服已介入，请耐心等待处理结果。</p>
                            </div>
                        </div>
                    )}

                    {/* Publisher Appeal Information */}
                    <Card>
                        <h3 className="font-bold text-gray-800 mb-3 flex justify-between items-center">
                            <span>申诉信息</span>
                            {appealDetail && (
                                <div className="flex items-center gap-2 text-xs font-normal text-gray-500">
                                    <span className="text-gray-400">发起人:</span>
                                    <img src={appealDetail.proposerAvatar || '/default-avatar.png'} className="w-5 h-5 rounded-full" />
                                    <span>{appealDetail.proposerName}</span>
                                </div>
                            )}
                        </h3>
                        <div className="bg-gray-50 p-3 rounded-lg text-sm text-gray-700 whitespace-pre-wrap">
                            {appealDetail?.reason || task.appeal?.reason || '加载申诉内容中...'}
                        </div>
                        {/* Initiator Evidence Images */}
                        {(appealDetail?.evidenceImg || task.appeal?.evidenceImg) && (
                            <div className="mt-3">
                                <p className="text-xs text-gray-500 mb-2">图片证据：</p>
                                <div className="flex gap-2">
                                    <img 
                                        src={appealDetail?.evidenceImg || task.appeal?.evidenceImg} 
                                        className="w-20 h-20 object-cover rounded border"
                                        onClick={() => window.open(appealDetail?.evidenceImg || task.appeal?.evidenceImg)}
                                    />
                                </div>
                            </div>
                        )}
                        <p className="text-xs text-gray-400 mt-2 text-right">
                            提交时间: {task.appeal?.createTime ? new Date(task.appeal.createTime).toLocaleString() : '未知'}
                        </p>
                    </Card>

                    {/* Volunteer Response Information */}
                    <Card>
                        <h3 className="font-bold text-gray-800 mb-3 flex items-center justify-between">
                            <span>申诉回应</span>
                            <span className="text-xs font-normal text-gray-500">{appealDetail?.defendantName || '被申诉方'}</span>
                        </h3>
                        {task.appeal?.response || appealDetail?.defendantResponse ? (
                            <>
                                <div className="bg-gray-50 p-3 rounded-lg text-sm text-gray-700 whitespace-pre-wrap">
                                    {task.appeal?.response || appealDetail?.defendantResponse}
                                </div>
                                {/* Defendant Evidence Images */}
                                {(appealDetail?.defendantEvidenceImg || task.appeal?.defendantEvidenceImg) && (
                                    <div className="mt-3">
                                        <p className="text-xs text-gray-500 mb-2">响应证据：</p>
                                        <img 
                                            src={appealDetail?.defendantEvidenceImg || task.appeal?.defendantEvidenceImg} 
                                            className="w-20 h-20 object-cover rounded border"
                                            onClick={() => window.open(appealDetail?.defendantEvidenceImg || task.appeal?.defendantEvidenceImg)}
                                        />
                                    </div>
                                )}
                                <p className="text-xs text-gray-400 mt-2 text-right">
                                    回应时间: {(task.appeal?.responseTime || appealDetail?.responseTime) ? new Date(task.appeal?.responseTime || appealDetail?.responseTime!).toLocaleString() : '未知'}
                                </p>
                            </>
                        ) : (
                            // Only show reply input if current user is NOT the proposer
                            (appealDetail && String(state.currentUser?.id) !== String(appealDetail.proposerId)) ? (
                                <div className="space-y-3">
                                    <textarea
                                        className="w-full border p-2 rounded-lg text-sm h-24 bg-white focus:ring-2 focus:ring-orange-500"
                                        placeholder="请针对申诉内容进行说明..."
                                        value={replyContent}
                                        onChange={e => setReplyContent(e.target.value)}
                                    />
                                    {/* Reply Evidence Upload */}
                                    <div className="space-y-2">
                                        <p className="text-sm text-gray-700">响应证据 (必填)</p>
                                        <div 
                                            onClick={() => replyFileInputRef.current?.click()}
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
                                            ref={replyFileInputRef} 
                                            className="hidden" 
                                            accept="image/*" 
                                            onChange={(e) => handleFileChange(e, 'reply')}
                                        />
                                    </div>
                                    <Button fullWidth onClick={handleReply} loading={submitting} disabled={submitting || !replyContent || !replyFile}>
                                        {submitting ? '提交中...' : '提交回应'}
                                    </Button>
                                    <p className="text-xs text-gray-400 text-center">提交后平台客服将进行公正裁决</p>
                                </div>
                            ) : (
                                <div className="text-center py-8 bg-gray-50 rounded-lg border border-dashed border-gray-200">
                                    <p className="text-gray-400 text-sm">等待对方回应中...</p>
                                </div>
                            )
                        )}
                    </Card>
                </div>

                <div className="fixed bottom-0 left-0 right-0 p-4 bg-white border-t border-gray-100 max-w-md mx-auto z-50">
                    <Button fullWidth onClick={() => navigate('/task/orders')}>
                        返回订单列表
                    </Button>
                </div>
            </div >
        );
    }

    return (
        <div className="min-h-screen bg-gray-50 pb-24">
            <NavBar title="验收确认" showBack />

            <div className="p-4 space-y-4">
                {/* Status Header */}
                <div className="bg-orange-50 p-4 rounded-xl flex items-center gap-3 border border-orange-100">
                    <CheckCircle2 className="text-orange-500" size={24} />
                    <div>
                        <p className="font-bold text-orange-800">志愿者已标记完成</p>
                        <p className="text-xs text-orange-600">请核对凭证，确认无误后资金将自动划转。</p>
                    </div>
                </div>

                {/* Evidence */}
                <Card>
                    <h3 className="font-bold text-gray-800 mb-3">服务凭证</h3>
                    {task.evidencePhotos && task.evidencePhotos.length > 0 ? (
                        <div className="grid grid-cols-2 gap-2">
                            {task.evidencePhotos.map((photo, idx) => (
                                <img
                                    key={idx}
                                    src={photo}
                                    alt={`Evidence ${idx}`}
                                    className="w-full h-32 object-cover rounded-lg border border-gray-100"
                                    onClick={() => {
                                        const w = window.open("");
                                        w?.document.write(`<img src="${photo}" style="width:100%"/>`);
                                    }}
                                />
                            ))}
                        </div>
                    ) : (
                        <p className="text-gray-400 text-sm">暂无上传图片</p>
                    )}

                    <div className="mt-4 pt-4 border-t border-gray-100 flex items-center justify-between text-sm text-gray-500">
                        <span>签到时间</span>
                        <span>{task.checkInTime ? new Date(task.checkInTime).toLocaleTimeString() : '未记录'}</span>
                    </div>
                    <div className="mt-2 flex items-center justify-between text-sm text-gray-500">
                        <span>完成时间</span>
                        <span>{task.finishTime ? new Date(task.finishTime).toLocaleTimeString() : '刚刚'}</span>
                    </div>
                </Card>

                {/* Volunteer Info */}
                <Card className="flex items-center gap-4">
                    <img src={task.volunteerAvatar || '/default-avatar.png'} className="w-12 h-12 rounded-full bg-gray-200" alt="Volunteer" />
                    <div>
                        <p className="font-bold text-gray-800">{task.volunteerName}</p>
                        <p className="text-xs text-gray-500">本次服务志愿者</p>
                    </div>
                </Card>
            </div>

            {/* Action Bar */}
            <div className="fixed bottom-0 left-0 right-0 p-4 bg-white border-t border-gray-100 max-w-md mx-auto flex gap-3 z-50">
                <Button
                    variant="outline"
                    className="flex-1 border-red-200 text-red-500"
                    onClick={() => setShowAppealModal(true)}
                >
                    <AlertTriangle size={16} className="mr-1" /> 申请售后
                </Button>
                <Button
                    className="flex-[2]"
                    onClick={() => setShowRateModal(true)}
                >
                    确认验收 & 支付
                </Button>
            </div>

            {/* Rating Modal */}
            <Modal isOpen={showRateModal} onClose={() => setShowRateModal(false)} title="评价志愿者">
                <div className="flex flex-col items-center py-4 space-y-4">
                    <img src={task.volunteerAvatar || '/default-avatar.png'} className="w-16 h-16 rounded-full" />
                    <p className="font-bold text-gray-800">给 {task.volunteerName} 打分</p>

                    <div className="flex gap-2">
                        {[1, 2, 3, 4, 5].map(star => (
                            <button key={star} onClick={() => setRating(star)} className="focus:outline-none transition-transform active:scale-110">
                                <Star
                                    size={32}
                                    className={star <= rating ? "fill-yellow-400 text-yellow-400" : "text-gray-300"}
                                />
                            </button>
                        ))}
                    </div>

                    <textarea
                        className="w-full border p-2 rounded-lg text-sm h-20"
                        placeholder="说说TA的表现如何..."
                        value={review}
                        onChange={e => setReview(e.target.value)}
                    />

                    <Button fullWidth onClick={handleConfirm} disabled={submitting}>
                        {submitting ? '提交中...' : '提交评价'}
                    </Button>
                </div>
            </Modal>

            {/* Appeal Modal */}
            <Modal isOpen={showAppealModal} onClose={() => setShowAppealModal(false)} title="申诉/售后">
                <div className="space-y-4">
                    <p className="text-sm text-gray-600 bg-gray-50 p-2 rounded">
                        请如实填写问题，恶意申诉将影响您的信用分。
                    </p>
                    <Input
                        label="申诉理由"
                        placeholder="例如：照片作假、未按时到达..."
                        value={appealReason}
                        onChange={e => setAppealReason(e.target.value)}
                    />
                    {/* Appeal Evidence Upload */}
                    <div className="space-y-2">
                        <p className="text-sm text-gray-700">上传凭证照片 (必填)</p>
                        <div 
                            onClick={() => fileInputRef.current?.click()}
                            className="aspect-video bg-gray-50 border-2 border-dashed border-gray-200 rounded-xl flex flex-col items-center justify-center relative overflow-hidden"
                        >
                            {appealPreview ? (
                                <>
                                    <img src={appealPreview} className="w-full h-full object-cover" alt="Preview" />
                                    <div className="absolute inset-0 bg-black/20 flex items-center justify-center">
                                        <div className="bg-white/80 p-2 rounded-full">
                                            <Camera size={24} className="text-gray-700" />
                                        </div>
                                    </div>
                                    <button 
                                        onClick={(e) => { e.stopPropagation(); setAppealFile(null); setAppealPreview(null); }}
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
                            onChange={(e) => handleFileChange(e, 'appeal')}
                        />
                    </div>
                    <Button 
                        fullWidth 
                        onClick={handleAppeal} 
                        variant="danger" 
                        loading={submitting}
                        disabled={!appealReason || !appealFile || submitting}
                    >
                        {submitting ? '提交中...' : '提交申诉'}
                    </Button>
                </div>
            </Modal>

        </div>
    );
}
