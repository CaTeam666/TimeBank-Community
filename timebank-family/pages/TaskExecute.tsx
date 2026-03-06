import React, { useState, useRef, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { NavBar, Button, Card, Modal } from '../components/UIComponents';
import { Phone, MapPin, Camera, AlertCircle, Clock, CheckCircle2, Loader2 } from 'lucide-react';
import { taskApi } from '../services/taskApi';
import { upload } from '../services/request';

interface TaskDetail {
    id: string;
    title: string;
    locationDetail?: string;
    contactPhone?: string;
}

export default function TaskExecute() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { state } = useAuth();
    const fileInputRef = useRef<HTMLInputElement>(null);

    const [task, setTask] = useState<TaskDetail | null>(null);
    const [taskLoading, setTaskLoading] = useState(true);
    const [checkInTime, setCheckInTime] = useState<number | null>(null);
    const [evidenceImage, setEvidenceImage] = useState<string | null>(null);
    const [evidenceFile, setEvidenceFile] = useState<File | null>(null);
    // Appeal state
    const [showAppealModal, setShowAppealModal] = useState(false);
    const [appealReason, setAppealReason] = useState('');
    const [appealLoading, setAppealLoading] = useState(false);

    // Original loading states
    const [loading, setLoading] = useState(false);
    const [checkInLoading, setCheckInLoading] = useState(false);

    useEffect(() => {
        if (!id) return;
        const fetchTask = async () => {
            setTaskLoading(true);
            try {
                const data = await taskApi.getTaskDetail(id);
                setTask({
                    id: data.taskId,
                    title: data.title,
                    locationDetail: data.location,
                    contactPhone: data.contactPhone || '139********'
                });
            } catch (error) {
                console.error('Failed to fetch task', error);
                setTask(null);
            } finally {
                setTaskLoading(false);
            }
        };
        fetchTask();
    }, [id]);

    if (taskLoading) return (
        <div className="min-h-screen bg-gray-50 flex items-center justify-center">
            <Loader2 size={32} className="animate-spin text-orange-500" />
        </div>
    );

    if (!task || !state.currentUser) return (
        <div className="min-h-screen bg-gray-50 p-8 text-center text-gray-500">
            任务未找到或未登录
            <Button onClick={() => navigate('/task/orders')} className="mt-4">返回订单列表</Button>
        </div>
    );

    const handleCheckIn = async () => {
        setCheckInLoading(true);
        try {
            await taskApi.checkIn(task.id, state.currentUser!.id, '已到达服务地点');
            setCheckInTime(Date.now());
            alert('签到成功');
        } catch (error) {
            console.error('Failed to check in', error);
            alert('签到失败，请重试');
        } finally {
            setCheckInLoading(false);
        }
    };

    const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>) => {
        const file = e.target.files?.[0];
        if (file) {
            setEvidenceFile(file);
            const reader = new FileReader();
            reader.onloadend = () => {
                setEvidenceImage(reader.result as string);
            };
            reader.readAsDataURL(file);
        }
    };

    const handleSubmit = async () => {
        if (!evidenceFile || !evidenceImage) {
            alert("请上传服务现场照片作为凭证");
            return;
        }
        if (window.confirm("确认服务已完成？提交后将通知发布者验收。")) {
            setLoading(true);
            try {
                // 1. Upload image first
                const imageUrl = await upload<string>('/file/upload', evidenceFile);
                console.log('Uploaded imageUrl:', imageUrl);
                // 2. Submit evidence
                await taskApi.submitEvidence(task.id, state.currentUser!.id, imageUrl);
                alert("提交成功，请耐心等待验收");
                navigate('/task/orders');
            } catch (error) {
                console.error('Failed to submit evidence', error);
                alert('提交失败，请重试');
            } finally {
                setLoading(false);
            }
        }
    };

    const handleAppeal = async () => {
        if (!appealReason.trim()) {
            alert("请填写申诉理由");
            return;
        }
        setAppealLoading(true);
        try {
            await taskApi.submitAppeal(task.id, state.currentUser!.id, appealReason);
            alert("申诉已提交，请等待处理");
            setShowAppealModal(false);
            navigate('/task/orders');
        } catch (error) {
            console.error('Failed to submit appeal', error);
            alert("申诉提交失败，请重试");
        } finally {
            setAppealLoading(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 pb-24">
            <NavBar title="服务履约" showBack />

            {/* Appeal Button */}
            <div className="bg-red-50 p-3 flex justify-between items-center px-4">
                <span className="text-red-600 text-sm font-bold flex items-center gap-2">
                    <AlertCircle size={16} /> 遇到问题？
                </span>
                <button
                    onClick={() => setShowAppealModal(true)}
                    className="bg-red-500 text-white text-xs px-3 py-1.5 rounded-full font-bold shadow-sm active:scale-95 transition-transform"
                >
                    申请申诉
                </button>
            </div>

            <div className="p-4 space-y-4">
                {/* Info Card with Privacy Revealed */}
                <Card className="border-l-4 border-blue-500">
                    <h2 className="font-bold text-gray-800 text-lg mb-2">{task.title}</h2>
                    <div className="space-y-3">
                        <div className="flex items-start gap-3">
                            <MapPin className="text-blue-500 mt-1" size={18} />
                            <div>
                                <p className="font-bold text-gray-800">{task.locationDetail}</p>
                                <p className="text-xs text-gray-500">精确位置已显示</p>
                            </div>
                        </div>
                        <div className="flex items-center gap-3">
                            <Phone className="text-green-500" size={18} />
                            <div className="flex-1">
                                <p className="font-bold text-gray-800 text-lg">{task.contactPhone || '139********'}</p>
                            </div>
                            <a href={`tel:${task.contactPhone}`} className="bg-green-100 text-green-700 px-3 py-1 rounded-full text-xs font-bold">
                                一键拨号
                            </a>
                        </div>
                    </div>
                </Card>

                {/* Step 1: Check In */}
                <Card className={checkInTime ? "bg-green-50 border border-green-100" : ""}>
                    <div className="flex justify-between items-center mb-3">
                        <h3 className="font-bold text-gray-800 flex items-center gap-2">
                            <Clock size={18} className="text-orange-500" />
                            步骤一：到达签到
                        </h3>
                        {checkInTime && <CheckCircle2 className="text-green-500" size={20} />}
                    </div>
                    {checkInTime ? (
                        <p className="text-sm text-green-700">
                            已于 {new Date(checkInTime).toLocaleTimeString()} 签到成功
                        </p>
                    ) : (
                        <Button variant="outline" fullWidth onClick={handleCheckIn}>
                            我已到达，点击签到
                        </Button>
                    )}
                </Card>

                {/* Step 2: Evidence */}
                <Card className={!checkInTime ? "opacity-50" : ""}>
                    <h3 className="font-bold text-gray-800 flex items-center gap-2 mb-3">
                        <Camera size={18} className="text-blue-500" />
                        步骤二：上传凭证
                    </h3>
                    {!checkInTime ? (
                        <p className="text-xs text-orange-500 mb-4">请先完成签到后再上传凭证</p>
                    ) : (
                        <p className="text-xs text-gray-400 mb-4">请拍摄服务现场照片（如打扫干净的房屋、陪同就医场景），作为结算依据。</p>
                    )}

                    <div
                        onClick={() => checkInTime && fileInputRef.current?.click()}
                        className={`w-full aspect-video bg-gray-100 rounded-lg border-2 border-dashed border-gray-300 flex flex-col items-center justify-center relative overflow-hidden transition-colors ${checkInTime ? 'active:bg-gray-200 cursor-pointer' : 'cursor-not-allowed'}`}
                    >
                        {evidenceImage ? (
                            <img src={evidenceImage} alt="Evidence" className="w-full h-full object-cover" />
                        ) : (
                            <>
                                <Camera size={32} className="text-gray-400 mb-2" />
                                <span className="text-gray-500 text-xs">{checkInTime ? '点击拍照/上传' : '请先签到'}</span>
                            </>
                        )}
                        <input
                            type="file"
                            ref={fileInputRef}
                            className="hidden"
                            accept="image/*"
                            capture="environment"
                            onChange={handleFileChange}
                            disabled={!checkInTime}
                        />
                    </div>
                </Card>
            </div>

            {/* Submit Button */}
            <div className="fixed bottom-0 left-0 right-0 p-4 bg-white border-t border-gray-100 max-w-md mx-auto z-50">
                <Button fullWidth size="lg" onClick={handleSubmit} disabled={loading || !evidenceImage}>
                    {loading ? '提交中...' : '确认完成服务'}
                </Button>
            </div>

            {/* Appeal Modal */}
            <Modal isOpen={showAppealModal} onClose={() => setShowAppealModal(false)} title="申请申诉">
                <div className="space-y-4">
                    <p className="text-sm text-gray-600 bg-gray-50 p-2 rounded">
                        如果遇到雇主失联、任务信息不符等问题，可在此提交申诉。
                    </p>
                    <textarea
                        className="w-full border p-2 rounded-lg text-sm h-32"
                        placeholder="请详细描述您遇到的问题..."
                        value={appealReason}
                        onChange={e => setAppealReason(e.target.value)}
                    />
                    <Button fullWidth onClick={handleAppeal} variant="danger" disabled={appealLoading || !appealReason.trim()}>
                        {appealLoading ? '提交中...' : '确认提交申诉'}
                    </Button>
                </div>
            </Modal>
        </div>
    );
}
