import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { NavBar, Card, Button } from '../components/UIComponents';
import { familyApi, PendingRequestItem, ReviewBindingRequest } from '../services/familyApi';
import { UserCheck, UserX, Phone, Calendar, Image as ImageIcon, AlertCircle } from 'lucide-react';

export default function FamilyBindingReview() {
    const navigate = useNavigate();
    const [requests, setRequests] = useState<PendingRequestItem[]>([]);
    const [loading, setLoading] = useState(true);
    const [showRejectModal, setShowRejectModal] = useState(false);
    const [selectedRequest, setSelectedRequest] = useState<PendingRequestItem | null>(null);
    const [rejectReason, setRejectReason] = useState('');
    const [processing, setProcessing] = useState(false);

    // 获取待审核申请列表
    useEffect(() => {
        loadRequests();
    }, []);



    const loadRequests = async () => {
        try {
            setLoading(true);
            const response = await familyApi.getPendingRequests();
            setRequests(response.requests);
        } catch (err: any) {
            console.error('Failed to load pending requests:', err);
            // 如果是401未授权错误,跳转到登录页
            if (err.status === 401 || err.message?.includes('401')) {
                navigate('/login', { replace: true });
            }
        } finally {
            setLoading(false);
        }
    };

    // 同意申请
    const handleApprove = async (request: PendingRequestItem) => {
        if (processing) return;

        if (window.confirm(`确认同意 ${request.childName} 的绑定申请?`)) {
            try {
                setProcessing(true);
                await familyApi.reviewBinding({
                    relationId: request.relationId,
                    action: 'approve'
                });
                alert('已同意绑定申请');
                loadRequests(); // 重新加载列表
            } catch (err: any) {
                console.error('Failed to approve:', err);
                alert(err.message || '操作失败,请重试');
            } finally {
                setProcessing(false);
            }
        }
    };

    // 打开拒绝弹窗
    const openRejectModal = (request: PendingRequestItem) => {
        setSelectedRequest(request);
        setRejectReason('');
        setShowRejectModal(true);
    };

    // 拒绝申请
    const handleReject = async () => {
        if (!selectedRequest) return;
        if (!rejectReason.trim()) {
            alert('请输入拒绝原因');
            return;
        }

        try {
            setProcessing(true);
            await familyApi.reviewBinding({
                relationId: selectedRequest.relationId,
                action: 'reject',
                rejectReason: rejectReason.trim()
            });
            alert('已拒绝绑定申请');
            setShowRejectModal(false);
            setSelectedRequest(null);
            setRejectReason('');
            loadRequests(); // 重新加载列表
        } catch (err: any) {
            console.error('Failed to reject:', err);
            alert(err.message || '操作失败,请重试');
        } finally {
            setProcessing(false);
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 pb-20">
            <NavBar title="待审核的绑定申请" showBack />

            <div className="p-4 space-y-3">
                {loading ? (
                    <div className="text-center py-20 text-gray-400">
                        <AlertCircle size={48} className="mx-auto mb-2 opacity-20 animate-pulse" />
                        <p>加载中...</p>
                    </div>
                ) : requests.length === 0 ? (
                    <div className="text-center py-20 text-gray-400">
                        <UserCheck size={48} className="mx-auto mb-2 opacity-20" />
                        <p>暂无待审核申请</p>
                    </div>
                ) : (
                    requests.map(request => (
                        <Card key={request.relationId} className="p-4">
                            {/* 申请人信息 */}
                            <div className="flex items-start gap-3 mb-4">
                                <img
                                    src={request.childAvatar}
                                    alt={request.childName}
                                    className="w-14 h-14 rounded-full object-cover border-2 border-gray-100"
                                />
                                <div className="flex-1">
                                    <div className="flex items-center gap-2 mb-1">
                                        <h3 className="text-lg font-bold text-gray-900">
                                            {request.childName}
                                        </h3>
                                        <span className="px-2 py-0.5 bg-blue-50 text-blue-600 text-xs rounded-full font-medium">
                                            {request.relation}
                                        </span>
                                    </div>
                                    <div className="flex items-center gap-4 text-sm text-gray-500">
                                        <div className="flex items-center gap-1">
                                            <Phone size={14} />
                                            <span>{request.childPhone}</span>
                                        </div>
                                        <div className="flex items-center gap-1">
                                            <Calendar size={14} />
                                            <span>{new Date(request.createTime).toLocaleString()}</span>
                                        </div>
                                    </div>
                                </div>
                            </div>

                            {/* 证明材料 */}
                            {request.proofImg && (
                                <div className="mb-4">
                                    <div className="flex items-center gap-1 text-sm text-gray-600 mb-2">
                                        <ImageIcon size={14} />
                                        <span>证明材料</span>
                                    </div>
                                    <img
                                        src={request.proofImg}
                                        alt="证明材料"
                                        className="w-full max-w-xs rounded-lg border border-gray-200 cursor-pointer hover:opacity-90 transition-opacity"
                                        onClick={() => window.open(request.proofImg, '_blank')}
                                    />
                                </div>
                            )}

                            {/* 操作按钮 */}
                            <div className="flex gap-3">
                                <Button
                                    variant="success"
                                    fullWidth
                                    onClick={() => handleApprove(request)}
                                    disabled={processing}
                                    className="flex items-center justify-center gap-2"
                                >
                                    <UserCheck size={18} />
                                    同意
                                </Button>
                                <Button
                                    variant="danger"
                                    fullWidth
                                    onClick={() => openRejectModal(request)}
                                    disabled={processing}
                                    className="flex items-center justify-center gap-2"
                                >
                                    <UserX size={18} />
                                    拒绝
                                </Button>
                            </div>
                        </Card>
                    ))
                )}
            </div>

            {/* 拒绝原因弹窗 */}
            {showRejectModal && selectedRequest && (
                <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/60 backdrop-blur-sm animate-fade-in">
                    <div className="bg-white rounded-2xl w-full max-w-sm overflow-hidden shadow-2xl transform scale-100 transition-all">
                        <div className="p-6">
                            <h3 className="text-xl font-bold text-gray-900 mb-2">拒绝申请</h3>
                            <p className="text-sm text-gray-500 mb-4">
                                拒绝 {selectedRequest.childName} 的绑定申请
                            </p>

                            <textarea
                                value={rejectReason}
                                onChange={(e) => setRejectReason(e.target.value)}
                                placeholder="请输入拒绝原因..."
                                className="w-full px-4 py-3 border border-gray-200 rounded-lg focus:ring-2 focus:ring-red-500 focus:border-transparent resize-none"
                                rows={4}
                                autoFocus
                            />

                            <div className="flex gap-3 mt-4">
                                <Button
                                    variant="secondary"
                                    fullWidth
                                    onClick={() => {
                                        setShowRejectModal(false);
                                        setSelectedRequest(null);
                                        setRejectReason('');
                                    }}
                                    disabled={processing}
                                >
                                    取消
                                </Button>
                                <Button
                                    variant="danger"
                                    fullWidth
                                    onClick={handleReject}
                                    disabled={processing || !rejectReason.trim()}
                                >
                                    {processing ? '处理中...' : '确认拒绝'}
                                </Button>
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}
