import React, { useState } from 'react';
import { useMall } from '../context/MallContext';
import { useAuth } from '../context/AuthContext';
import { NavBar, Card, Button, Modal } from '../components/UIComponents';
import { ExchangeOrder } from '../types';
import { QrCode, Calendar, ChevronRight, PackageCheck } from 'lucide-react';

export default function MallOrders() {
    const { getOrdersByUserId, fetchUserOrders } = useMall();
    const { state } = useAuth();

    const [activeTab, setActiveTab] = useState<'pending' | 'completed'>('pending');
    const [selectedOrder, setSelectedOrder] = useState<ExchangeOrder | null>(null);

    // Determine current acting user
    const currentUser = state.isProxyMode && state.proxyTarget ? state.proxyTarget : state.currentUser;

    // Fetch orders on mount
    React.useEffect(() => {
        if (currentUser) {
            fetchUserOrders(currentUser.id);
        }
    }, [currentUser?.id]); // eslint-disable-line react-hooks/exhaustive-deps

    if (!currentUser) return null;

    const orders = getOrdersByUserId(currentUser.id);

    const filteredOrders = orders.filter(o => {
        if (activeTab === 'pending') return o.status === 'pending_pickup';
        // 'completed' tab includes verified (completed) and cancelled
        return o.status === 'completed' || o.status === 'cancelled';
    });

    return (
        <div className="min-h-screen bg-gray-50 pb-20">
            <NavBar title="我的兑换" showBack />

            {/* Tabs */}
            <div className="bg-white flex border-b border-gray-100">
                <button
                    onClick={() => setActiveTab('pending')}
                    className={`flex-1 py-3 text-sm font-medium border-b-2 transition-colors ${activeTab === 'pending' ? 'text-orange-500 border-orange-500' : 'text-gray-500 border-transparent'
                        }`}
                >
                    待核销
                </button>
                <button
                    onClick={() => setActiveTab('completed')}
                    className={`flex-1 py-3 text-sm font-medium border-b-2 transition-colors ${activeTab === 'completed' ? 'text-orange-500 border-orange-500' : 'text-gray-500 border-transparent'
                        }`}
                >
                    已完成
                </button>
            </div>

            {/* List */}
            <div className="p-4 space-y-4">
                {filteredOrders.length === 0 ? (
                    <div className="text-center py-20 text-gray-400">
                        <PackageCheck size={48} className="mx-auto mb-2 opacity-20" />
                        <p>暂无{activeTab === 'pending' ? '待领取' : '历史'}订单</p>
                    </div>
                ) : (
                    filteredOrders.map(order => (
                        <Card key={order.id}>
                            <div className="flex gap-3 pb-3 border-b border-gray-100">
                                <img src={order.productImage} className="w-16 h-16 rounded bg-gray-100 object-cover" />
                                <div className="flex-1">
                                    <h3 className="font-bold text-gray-800 text-sm line-clamp-1">{order.productName}</h3>
                                    <p className="text-xs text-gray-500 mt-1">数量: x{order.quantity}</p>
                                    <p className="text-sm font-bold text-gray-800 mt-1">实付: {order.totalPrice} 币</p>
                                </div>
                            </div>
                            <div className="pt-3 flex justify-between items-center">
                                <div className="text-xs text-gray-400 flex items-center gap-1">
                                    <Calendar size={12} />
                                    {new Date(order.createdAt).toLocaleDateString()}
                                </div>
                                {order.status === 'pending_pickup' ? (
                                    <Button size="sm" onClick={() => setSelectedOrder(order)}>
                                        <QrCode size={16} className="mr-1" />
                                        去领取
                                    </Button>
                                ) : order.status === 'cancelled' ? (
                                    <span className="text-sm text-gray-400 font-bold bg-gray-100 px-2 py-1 rounded">已取消</span>
                                ) : (
                                    <span className="text-sm text-green-600 font-bold bg-green-50 px-2 py-1 rounded">已领取</span>
                                )}
                            </div>
                        </Card>
                    ))
                )}
            </div>

            {/* Redemption Modal */}
            {selectedOrder && (
                <Modal isOpen={!!selectedOrder} onClose={() => setSelectedOrder(null)} title="核销凭证">
                    <div className="flex flex-col items-center py-4 space-y-4">
                        <div className="w-48 h-48 bg-white p-2 rounded-lg border-2 border-orange-500 shadow-sm">
                            {/* Using external API for generic QR code visualization */}
                            <img
                                src={`https://api.qrserver.com/v1/create-qr-code/?size=150x150&data=TB_ORDER:${selectedOrder.id}`}
                                alt="QR Code"
                                className="w-full h-full"
                            />
                        </div>

                        <div className="text-center">
                            <p className="text-gray-500 text-xs mb-1">向工作人员出示二维码或核销码</p>
                            <p className="text-2xl font-mono font-bold text-gray-800 tracking-wider bg-gray-100 px-4 py-2 rounded">
                                {selectedOrder.redemptionCode}
                            </p>
                        </div>

                        <div className="w-full bg-orange-50 p-3 rounded text-xs text-orange-700 text-center">
                            请前往社区服务中心，出示此码领取商品。
                        </div>
                    </div>
                </Modal>
            )}
        </div>
    );
}
