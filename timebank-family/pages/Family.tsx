import React, { useState, useEffect } from 'react';
import { useAuth } from '../context/AuthContext';
import { useUI } from '../context/UIContext';
import { NavBar, Card, Button, Modal, Input } from '../components/UIComponents';

import { Plus, UserCheck, Trash2, Camera, Repeat, Bell } from 'lucide-react';
import { FamilyMember } from '../types';
import { familyApi, BindFamilyRequest } from '../services/familyApi';
import { authService } from '../services/authService';

export default function Family() {
    const { state, dispatch } = useAuth();
    const { showToast } = useUI();

    const [showBindModal, setShowBindModal] = useState(false);
    const [bindForm, setBindForm] = useState({ 
        phone: '', 
        relation: '', 
        proofImgs: [] as Array<{ preview: string; remote: string }> 
    });

    const [pendingCount, setPendingCount] = useState(0);
    const [familyMembers, setFamilyMembers] = useState<FamilyMember[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [submitting, setSubmitting] = useState(false);
    const [uploading, setUploading] = useState(false);
    const fileInputRef = React.useRef<HTMLInputElement>(null);


    // 加载亲情账号数据
    useEffect(() => {
        loadFamilyData();
    }, []);

    const loadFamilyData = async () => {
        try {
            setLoading(true);
            setError(null);

            // 并行加载列表和待审核数量
            const [members, pending] = await Promise.all([
                familyApi.getFamilyList(),
                familyApi.getPendingRequests()
            ]);

            setFamilyMembers(members);
            setPendingCount(pending.total);
        } catch (err: any) {
            setError(err.message || '加载失败');
            console.error('Load family data error:', err);
        } finally {
            setLoading(false);
        }
    };

    const handleToggleProxy = async (member: FamilyMember) => {
        try {
            const isCurrentlyActing = state.isProxyMode && state.proxyTarget?.id === member.id;

            // 退出代理时，先清除 proxyToken，确保退出请求使用普通 token
            // 否则退出代理的接口会携带代理令牌，后端不认可导致 401
            if (isCurrentlyActing) {
                localStorage.removeItem('proxyToken');
            }

            const result = await familyApi.toggleProxyMode({
                parentId: isCurrentlyActing ? undefined : member.id,
                enable: !isCurrentlyActing
            });

            if (result.proxyToken) {
                // 保存代理token
                localStorage.setItem('proxyToken', result.proxyToken);
            } else {
                localStorage.removeItem('proxyToken');
            }

            // 更新本地状态
            dispatch({
                type: 'TOGGLE_PROXY',
                payload: isCurrentlyActing ? null : member
            });
        } catch (err: any) {
            alert(err.message || '切换失败');
        }
    };

    const handleUnbind = async (member: FamilyMember) => {
        if (!window.confirm("确认解绑该账号吗？")) return;

        if (!member.relationId) {
            alert('无效的绑定关系');
            return;
        }

        try {
            setLoading(true);
            await familyApi.unbindFamily({ relationId: member.relationId });
            alert('解绑成功');
            await loadFamilyData();
        } catch (err: any) {
            alert(err.message || '解绑失败');
        } finally {
            setLoading(false);
        }
    };

    const handleSubmitBind = async () => {
        if (!bindForm.phone || !bindForm.relation) {
            showToast('请填写完整信息', 'info');
            return;
        }

        try {
            setSubmitting(true);
            if (bindForm.proofImgs.length < 2) {
                showToast('请至少上传2张证明材料照片', 'error');
                return;
            }


            const params: BindFamilyRequest = {
                phone: bindForm.phone,
                relation: bindForm.relation,
                proofImg: bindForm.proofImgs.map(img => img.remote).filter(Boolean).join(',')
            };


            await familyApi.bindFamily(params);

            setShowBindModal(false);
            setBindForm({ phone: '', relation: '', proofImgs: [] });
            showToast('绑定申请已提交，等待审核', 'success');

            // 重新加载数据
            await loadFamilyData();
        } catch (err: any) {
            showToast(err.message || '提交失败，请重试', 'error');
        } finally {
            setSubmitting(false);
        }

    };

    const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
        const target = e.target;
        const file = target?.files?.[0];
        if (!file) return;

        // 1. 生成本地预览 URL，确保 UI 秒级展示且永不因为上传后的替换而消失
        let localUrl = '';
        try {
            localUrl = URL.createObjectURL(file);
            // 此时 remote 为空，预览使用 localUrl
            setBindForm(prev => ({ 
                ...prev, 
                proofImgs: [...prev.proofImgs, { preview: localUrl, remote: '' }] 
            }));
        } catch (err) {
            console.error("Failed to create local preview:", err);
        }

        try {
            setUploading(true);
            // 2. 上传到后端获取正式 URL
            const remoteUrl = await authService.uploadImage(file);
            
            if (remoteUrl) {
                // 3. 更新对应的 remote 属性。注意：preview 保持为 localUrl
                setBindForm(prev => ({
                    ...prev,
                    proofImgs: prev.proofImgs.map(img => 
                        img.preview === localUrl ? { ...img, remote: remoteUrl } : img
                    )
                }));
            } else {
                throw new Error('未获取到有效的图片地址');
            }

        } catch (err: any) {
            console.error("Upload process failed:", err);
            // 上传失败时，从列表中彻底移除该预览项
            setBindForm(prev => ({
                ...prev,
                proofImgs: prev.proofImgs.filter(img => img.preview !== localUrl)
            }));
            alert(err.message || '图片上传失败，请重新尝试');
        } finally {
            setUploading(false);
            // 清理 input，确保可以重复触发
            if (target) target.value = '';
            // 延迟清理 blob，避免渲染冲突 (可选)
            // setTimeout(() => URL.revokeObjectURL(localUrl), 1000);
        }
    };




    const handleRemoveImage = (index: number) => {
        setBindForm(prev => ({
            ...prev,
            proofImgs: prev.proofImgs.filter((_, i) => i !== index)

        }));
    };

    return (
        <div className="min-h-screen bg-gray-50 pb-24">
            <NavBar title="亲情账号" showBack>
                <button
                    className="relative p-2 hover:bg-gray-100 rounded-full transition-colors"
                    onClick={() => alert('查看绑定申请 (功能开发中)')}
                >
                    <Bell size={20} className="text-gray-700" />
                    {pendingCount > 0 && (
                        <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs w-5 h-5 rounded-full flex items-center justify-center font-bold">
                            {pendingCount > 99 ? '99+' : pendingCount}
                        </span>
                    )}
                </button>
            </NavBar>

            <div className="p-4 space-y-4">
                {/* Proxy Mode Banner */}
                {state.isProxyMode && state.proxyTarget && (
                    <div className="bg-orange-100 border-l-4 border-orange-500 p-3 rounded-r-lg">
                        <div className="flex items-center justify-between">
                            <div className="flex items-center gap-2">
                                <UserCheck size={18} className="text-orange-600" />
                                <span className="text-sm font-medium text-orange-800">
                                    正在代理 {state.proxyTarget.nickname} 操作
                                </span>
                            </div>
                            <button
                                onClick={() => handleToggleProxy(state.proxyTarget!)}
                                className="text-xs text-orange-600 underline hover:text-orange-700 transition-colors"
                            >
                                退出代理
                            </button>
                        </div>
                    </div>
                )}

                {/* Header Info */}
                <div className="bg-blue-50 p-4 rounded-xl text-blue-800 text-sm flex gap-3">
                    <Repeat className="shrink-0" size={20} />
                    <p>绑定长者账号，协助TA发布需求或管理积分。</p>
                </div>

                {/* Loading State */}
                {loading ? (
                    <div className="text-center py-12">
                        <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-orange-500 mx-auto"></div>
                        <p className="text-gray-400 mt-4">加载中...</p>
                    </div>
                ) : error ? (
                    /* Error State */
                    <div className="text-center py-12">
                        <p className="text-red-500 mb-4">{error}</p>
                        <Button onClick={loadFamilyData}>重试</Button>
                    </div>
                ) : familyMembers.length === 0 ? (
                    /* Empty State */
                    <div className="text-center py-12 text-gray-400">
                        <p>暂无绑定的亲情账号。</p>
                    </div>
                ) : (
                    /* Member List */
                    familyMembers.map(member => {
                        const isActing = state.isProxyMode && state.proxyTarget?.id === member.id;

                        return (
                            <Card key={member.id} className={`relative overflow-hidden transition-all ${isActing ? 'ring-2 ring-orange-500 ring-offset-2' : ''}`}>
                                <div className="flex items-center gap-4">
                                    <img src={member.avatar} alt="Avatar" className="w-12 h-12 rounded-full bg-gray-200 object-cover" />
                                    <div className="flex-1">
                                        <div className="flex justify-between items-start">
                                            <h3 className="font-bold text-gray-800">{member.nickname} <span className="text-xs font-normal text-gray-500">({member.relation})</span></h3>
                                            <span className="font-mono font-bold text-orange-500">{member.balance} <span className="text-xs font-normal text-gray-400">积分</span></span>
                                        </div>
                                        <p className="text-xs text-gray-400 mt-1">手机号: {member.phone}</p>
                                    </div>
                                </div>

                                <div className="mt-4 flex gap-3 pt-3 border-t border-gray-100">
                                    <button
                                        onClick={() => handleToggleProxy(member)}
                                        className={`flex-1 flex items-center justify-center gap-2 py-2 rounded-lg text-sm font-medium transition-colors ${isActing ? 'bg-orange-500 text-white' : 'bg-gray-100 text-gray-700 hover:bg-gray-200'}`}
                                    >
                                        <UserCheck size={16} />
                                        {isActing ? '退出代理' : '代TA操作'}
                                    </button>
                                    <button
                                        onClick={() => handleUnbind(member)}
                                        className="p-2 text-gray-400 hover:text-red-500 transition-colors"
                                    >
                                        <Trash2 size={18} />
                                    </button>
                                </div>
                            </Card>
                        );
                    })
                )}
            </div>

            {/* Add Button */}
            <div className="fixed bottom-20 left-4 right-4 max-w-md mx-auto">
                <Button fullWidth onClick={() => setShowBindModal(true)} className="shadow-lg">
                    <Plus size={20} className="mr-2" />
                    添加亲情账号
                </Button>
            </div>

            {/* Binding Modal */}
            <Modal
                isOpen={showBindModal}
                onClose={() => setShowBindModal(false)}
                title="绑定亲情账号"
            >
                <div className="space-y-4">
                    <Input
                        label="长者手机号"
                        placeholder="请输入手机号"
                        value={bindForm.phone}
                        onChange={(e) => setBindForm({ ...bindForm, phone: e.target.value })}
                    />
                    <Input
                        label="关系 (如: 父亲)"
                        placeholder="请输入关系"
                        value={bindForm.relation}
                        onChange={(e) => setBindForm({ ...bindForm, relation: e.target.value })}
                    />

                    <div>
                        <label className="block text-sm font-medium text-gray-700 mb-2">证明材料 (必填，至少2张)</label>
                        <p className="text-xs text-gray-400 mb-2">请上传户口本首页及证明关系的相关页</p>
                        
                        <div className="grid grid-cols-3 gap-2 mb-2">
                            {bindForm.proofImgs.map((img, idx) => (
                                <div key={idx} className="aspect-square bg-gray-100 rounded-lg relative group overflow-hidden border border-gray-200">
                                    <img src={img.preview} className="w-full h-full object-cover" />
                                    <button
                                        onClick={() => handleRemoveImage(idx)}
                                        className="absolute top-1 right-1 bg-red-500 text-white rounded-full p-1 opacity-80 hover:opacity-100 z-10"
                                    >
                                        <Trash2 size={12} />
                                    </button>
                                    {!img.remote && (
                                        <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
                                            <div className="w-5 h-5 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                                        </div>
                                    )}
                                </div>
                            ))}

                            
                            {/* Add More Button */}
                             <div
                                onClick={() => fileInputRef.current?.click()}
                                className="aspect-square bg-gray-50 border-2 border-dashed border-gray-200 rounded-lg flex flex-col items-center justify-center text-gray-400 cursor-pointer hover:bg-gray-100 transition-colors"
                            >
                                {uploading ? (
                                    <div className="w-5 h-5 border-2 border-orange-500 border-t-transparent rounded-full animate-spin"></div>
                                ) : (
                                    <Plus size={20} />
                                )}
                            </div>
                        </div>
                        
                        <input
                            ref={fileInputRef}
                            type="file"
                            className="hidden"
                            accept="image/*"
                            onChange={handleFileChange}
                        />
                    </div>

                    <Button
                        fullWidth
                        onClick={handleSubmitBind}
                        disabled={!bindForm.phone || !bindForm.relation || submitting}
                    >
                        {submitting ? '提交中...' : '提交申请'}
                    </Button>
                </div>
            </Modal>
        </div>
    );
}