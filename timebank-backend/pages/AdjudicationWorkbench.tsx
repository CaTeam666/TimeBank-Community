import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { ArrowLeft, MapPin, Calendar, ZoomIn, RotateCw, Shield, AlertTriangle, CheckCircle, XCircle } from 'lucide-react';
import { Modal } from '../components/ui/Modal';
import { arbitrationApi, ArbitrationDetail } from '../services/arbitrationApi';

export const AdjudicationWorkbench: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();

    const [arbitration, setArbitration] = useState<ArbitrationDetail | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');

    // Image Viewer State
    const [isViewerOpen, setIsViewerOpen] = useState(false);
    const [currentImage, setCurrentImage] = useState('');
    const [rotation, setRotation] = useState(0);

    // Verdict State
    const [verdictReason, setVerdictReason] = useState('');

    useEffect(() => {
        const fetchDetail = async () => {
            if (!id) return;
            try {
                setLoading(true);
                const data = await arbitrationApi.getArbitrationDetail(id);
                setArbitration(data);
            } catch (err) {
                console.error(err);
                setError('获取仲裁案件详情失败');
            } finally {
                setLoading(false);
            }
        };
        fetchDetail();
    }, [id]);

    if (loading) return <div className="p-8 text-center text-gray-500">加载中...</div>;
    if (error) return <div className="p-8 text-center text-red-500">{error}</div>;
    if (!arbitration) return <div className="p-8 text-center">案件不存在</div>;

    const openViewer = (img: string) => {
        setCurrentImage(img);
        setRotation(0);
        setIsViewerOpen(true);
    };

    const handleVerdict = async (type: 'REJECT' | 'TO_VOLUNTEER' | 'TO_PUBLISHER') => {
        if (!verdictReason.trim()) {
            alert('请填写判决备注，说明裁决理由。');
            return;
        }
        if (confirm('确定提交此裁决吗？此操作不可撤销。')) {
            try {
                await arbitrationApi.submitVerdict(arbitration.id, type, verdictReason);
                alert(`裁决已提交。操作类型: ${type}。`);
                navigate('/service/arbitration');
            } catch (err) {
                console.error(err);
                alert('提交裁决失败，请重试');
            }
        }
    };

    return (
        <div className="h-[calc(100vh-8rem)] flex flex-col">
            {/* Header */}
            <div className="flex items-center justify-between mb-4 flex-shrink-0">
                <div className="flex items-center gap-3">
                    <button onClick={() => navigate(-1)} className="p-1 hover:bg-gray-200 rounded-full">
                        <ArrowLeft className="w-5 h-5 text-gray-600" />
                    </button>
                    <h2 className="text-xl font-bold text-gray-900 flex items-center">
                        裁决工作台
                        <span className="ml-3 text-sm font-normal text-gray-500 font-mono bg-gray-100 px-2 py-1 rounded">{arbitration.id}</span>
                    </h2>
                </div>
                <div className="text-sm text-gray-500">
                    申诉提交于: {arbitration.createTime}
                </div>
            </div>

            <div className="flex-1 flex gap-6 overflow-hidden">
                {/* Left Column: Evidence */}
                <div className="w-1/2 flex flex-col gap-4 overflow-y-auto pr-2">
                    {/* Task Recap */}
                    <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-100">
                        <h3 className="font-bold text-gray-800 mb-3 border-b pb-2 flex justify-between">
                            任务概况
                            <Link to={`/task/detail/${arbitration.taskId}`} className="text-blue-600 text-xs font-normal hover:underline">查看完整详情</Link>
                        </h3>
                        <div className="space-y-2 text-sm">
                            <p className="font-medium text-lg">{arbitration.taskTitle}</p>
                            <p className="text-gray-600 bg-gray-50 p-2 rounded">{arbitration.taskDescription || '暂无描述'}</p>
                            <div className="flex gap-4 text-gray-500 mt-2">
                                <span className="flex items-center"><MapPin className="w-4 h-4 mr-1" /> {arbitration.taskAddress || '未知地址'}</span>
                                <span className="flex items-center"><Calendar className="w-4 h-4 mr-1" /> {arbitration.taskDeadline || '未知时间'}</span>
                            </div>
                        </div>
                    </div>

                    {/* Photos - Split into Proposer and Defendant */}
                    <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-100 flex-1 flex flex-col gap-4">
                        <h3 className="font-bold text-gray-800 border-b pb-2">案件证据照片 (双向)</h3>
                        
                        {/* Proposer Evidence */}
                        <div>
                            <p className="text-xs font-bold text-gray-500 mb-2 uppercase tracking-wider">申诉发起方提供：</p>
                            <div className="grid grid-cols-2 gap-3">
                                {arbitration.evidenceImages && arbitration.evidenceImages.length > 0 ? (
                                    arbitration.evidenceImages.map((img, idx) => (
                                        <div key={idx} className="group relative border rounded-lg overflow-hidden h-32 bg-gray-100 cursor-pointer" onClick={() => openViewer(img)}>
                                            <img src={img} alt="Proposer Evidence" className="w-full h-full object-cover transition-transform group-hover:scale-105" />
                                            <div className="absolute inset-0 bg-black bg-opacity-0 group-hover:bg-opacity-20 flex items-center justify-center transition-all">
                                                <ZoomIn className="text-white opacity-0 group-hover:opacity-100 w-6 h-6" />
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="col-span-2 text-gray-400 text-center py-2 text-xs border border-dashed rounded italic">未提供图片凭证</div>
                                )}
                            </div>
                        </div>

                        <div className="border-t border-gray-50"></div>

                        {/* Defendant Evidence */}
                        <div>
                            <p className="text-xs font-bold text-blue-600 mb-2 uppercase tracking-wider">被申诉方回应提供：</p>
                            <div className="grid grid-cols-2 gap-3">
                                {arbitration.defendantEvidenceImages && arbitration.defendantEvidenceImages.length > 0 ? (
                                    arbitration.defendantEvidenceImages.map((img, idx) => (
                                        <div key={idx} className="group relative border border-blue-100 rounded-lg overflow-hidden h-32 bg-blue-50 cursor-pointer" onClick={() => openViewer(img)}>
                                            <img src={img} alt="Defendant Evidence" className="w-full h-full object-cover transition-transform group-hover:scale-105" />
                                            <div className="absolute inset-0 bg-blue-900 bg-opacity-0 group-hover:bg-opacity-20 flex items-center justify-center transition-all">
                                                <ZoomIn className="text-white opacity-0 group-hover:opacity-100 w-6 h-6" />
                                            </div>
                                        </div>
                                    ))
                                ) : (
                                    <div className="col-span-2 text-gray-400 text-center py-2 text-xs border border-dashed rounded italic">被申诉方未提供图片反证</div>
                                )}
                            </div>
                        </div>
                    </div>
                </div>

                {/* Right Column: Statements & Verdict */}
                <div className="w-1/2 flex flex-col gap-4">
                    {/* Statements */}
                    <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-100 flex-1 overflow-y-auto">
                        <h3 className="font-bold text-gray-800 mb-4 border-b pb-2">双方陈述</h3>

                        {/* Initiator */}
                        <div className="mb-6">
                            <div className="flex items-center gap-2 mb-2">
                                <span className={`px-2 py-0.5 rounded text-xs font-bold ${arbitration.initiatorRole === 'VOLUNTEER' ? 'bg-green-100 text-green-700' : 'bg-orange-100 text-orange-700'}`}>
                                    申诉方 ({arbitration.initiatorName})
                                </span>
                                <span className="text-xs text-gray-400">{arbitration.createTime}</span>
                            </div>
                            <div className="bg-red-50 p-3 rounded-tr-lg rounded-br-lg rounded-bl-lg border border-red-100 text-gray-800 text-sm">
                                <p className="font-bold mb-1 text-red-800">申诉理由：{arbitration.type}</p>
                                {arbitration.description}
                            </div>
                        </div>

                        {/* Defendant */}
                        {arbitration.defendantResponse ? (
                            <div className="flex flex-col items-end mb-6">
                                <div className="flex items-center gap-2 mb-2">
                                    <span className="text-xs text-gray-400">已回应</span>
                                    <span className="px-2 py-0.5 rounded text-xs font-bold bg-gray-100 text-gray-700">
                                        被申诉方
                                    </span>
                                </div>
                                <div className="bg-blue-50 p-3 rounded-tl-lg rounded-bl-lg rounded-br-lg border border-blue-100 text-gray-800 text-sm w-full">
                                    <p className="mb-2 leading-relaxed">{arbitration.defendantResponse}</p>
                                    {/* Small Thumbnails in response area for context */}
                                    {arbitration.defendantEvidenceImages && arbitration.defendantEvidenceImages.length > 0 && (
                                        <div className="flex gap-2 mt-2 pt-2 border-t border-blue-100">
                                            {arbitration.defendantEvidenceImages.map((img, idx) => (
                                                <img 
                                                    key={idx} 
                                                    src={img} 
                                                    className="w-12 h-12 object-cover rounded border border-blue-200 cursor-pointer" 
                                                    onClick={() => openViewer(img)}
                                                />
                                            ))}
                                        </div>
                                    )}
                                </div>
                            </div>
                        ) : (
                            <div className="text-center py-4 bg-gray-50 rounded border border-dashed text-gray-400 text-sm">
                                等待被申诉方回应...
                            </div>
                        )}
                    </div>

                    {/* Verdict Actions */}
                    <div className="bg-white p-6 rounded-lg shadow-lg border border-blue-100">
                        <div className="mb-4">
                            <label className="block text-sm font-bold text-gray-700 mb-2">判决备注 / 理由 (必填)</label>
                            <textarea
                                className="w-full border border-gray-300 rounded p-2 text-sm h-20 focus:ring-blue-500 focus:border-blue-500"
                                placeholder="请输入您的裁决依据，将发送给双方..."
                                value={verdictReason}
                                onChange={(e) => setVerdictReason(e.target.value)}
                            ></textarea>
                        </div>

                        <div className="grid grid-cols-3 gap-3">
                            <button
                                onClick={() => handleVerdict('REJECT')}
                                className="flex flex-col items-center justify-center p-3 border border-gray-300 rounded hover:bg-gray-50 text-gray-700 transition-colors"
                            >
                                <Shield className="w-5 h-5 mb-1" />
                                <span className="text-xs font-bold">驳回申诉</span>
                                <span className="text-[10px] text-gray-400">维持原状态</span>
                            </button>

                            <button
                                onClick={() => handleVerdict('TO_VOLUNTEER')}
                                className="flex flex-col items-center justify-center p-3 border border-green-200 bg-green-50 rounded hover:bg-green-100 text-green-700 transition-colors"
                            >
                                <CheckCircle className="w-5 h-5 mb-1" />
                                <span className="text-xs font-bold">强制结算</span>
                                <span className="text-[10px] text-green-600/70">判给志愿者</span>
                            </button>

                            <button
                                onClick={() => handleVerdict('TO_PUBLISHER')}
                                className="flex flex-col items-center justify-center p-3 border border-red-200 bg-red-50 rounded hover:bg-red-100 text-red-700 transition-colors"
                            >
                                <XCircle className="w-5 h-5 mb-1" />
                                <span className="text-xs font-bold">取消订单</span>
                                <span className="text-[10px] text-red-600/70">退款给发布者</span>
                            </button>
                        </div>
                    </div>
                </div>
            </div>

            {/* Image Viewer Modal */}
            {isViewerOpen && (
                <div className="fixed inset-0 z-[60] bg-black bg-opacity-90 flex flex-col items-center justify-center p-4">
                    <div className="absolute top-4 right-4 flex gap-4">
                        <button onClick={() => setRotation(r => r + 90)} className="text-white hover:text-blue-400">
                            <RotateCw className="w-8 h-8" />
                        </button>
                        <button onClick={() => setIsViewerOpen(false)} className="text-white hover:text-red-400">
                            <XCircle className="w-8 h-8" />
                        </button>
                    </div>
                    <img
                        src={currentImage}
                        className="max-h-[85vh] max-w-[90vw] transition-transform duration-300"
                        style={{ transform: `rotate(${rotation}deg)` }}
                        alt="Full View"
                    />
                </div>
            )}
        </div>
    );
};

