import React, { useState, useEffect } from 'react';
import { orderApi } from '../services/orderApi';
import { ExchangeOrder, OrderStatus } from '../types';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { Coins, CheckCircle, Search } from 'lucide-react';

export const OrderManagement: React.FC = () => {
    const [orders, setOrders] = useState<ExchangeOrder[]>([]);
    const [loading, setLoading] = useState(false);
    const [filterStatus, setFilterStatus] = useState<string>('ALL');
    const [selectedOrder, setSelectedOrder] = useState<ExchangeOrder | null>(null);
    const [isVerifyModalOpen, setIsVerifyModalOpen] = useState(false);
    const [keyword, setKeyword] = useState('');

    const fetchOrders = async () => {
        setLoading(true);
        try {
            const data = await orderApi.getOrders({
                status: filterStatus === 'ALL' ? undefined : filterStatus,
                keyword: keyword || undefined
            });
            setOrders(data.list); // API returns { list, total, ... }
        } catch (error) {
            console.error('Failed to fetch orders', error);
            alert('获取订单列表失败');
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchOrders();
    }, [filterStatus]); // Re-fetch when filter changes

    // Debounce keyword search or simple enter key could be implemented. 
    // For simplicity, let's add a search button or trigger on Enter.
    const handleSearch = (e: React.KeyboardEvent) => {
        if (e.key === 'Enter') {
            fetchOrders();
        }
    };

    const handleVerify = async () => {
        if (selectedOrder) {
            try {
                await orderApi.verifyOrder(selectedOrder.id);
                alert('核销成功！');
                setIsVerifyModalOpen(false);
                setSelectedOrder(null);
                fetchOrders();
            } catch (error) {
                console.error('Verify failed', error);
                alert('核销失败');
            }
        }
    };

    const handleCancel = async (id: string) => {
        if (confirm('确定要取消此订单吗？积分将退回给志愿者。')) {
            try {
                await orderApi.cancelOrder(id);
                fetchOrders();
            } catch (error) {
                console.error('Cancel failed', error);
                alert('取消失败');
            }
        }
    };

    const getStatusBadge = (status: OrderStatus) => {
        switch (status) {
            case OrderStatus.PENDING: return <Badge color="yellow">待核销</Badge>;
            case OrderStatus.COMPLETED: return <Badge color="green">已完成</Badge>;
            case OrderStatus.CANCELLED: return <Badge color="gray">已取消</Badge>;
        }
    };

    return (
        <div className="space-y-6">
            {/* Filters */}
            <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-100 flex items-center justify-between">
                <div className="flex gap-2">
                    {[
                        { label: '全部订单', value: 'ALL', color: 'blue' },
                        { label: '待核销', value: OrderStatus.PENDING, color: 'yellow' },
                        { label: '已完成', value: OrderStatus.COMPLETED, color: 'green' },
                        { label: '已取消', value: OrderStatus.CANCELLED, color: 'gray' }
                    ].map(tab => (
                        <button
                            key={tab.value}
                            onClick={() => setFilterStatus(tab.value)}
                            className={`px-4 py-2 rounded-md text-sm font-medium ${filterStatus === tab.value ? `bg-${tab.color}-100 text-${tab.color}-700` : 'text-gray-600 hover:bg-gray-50'}`}
                        >
                            {tab.label}
                        </button>
                    ))}
                </div>
                <div className="relative w-64">
                    <input
                        type="text"
                        placeholder="搜索订单号 / 志愿者"
                        className="w-full pl-10 pr-3 py-2 border border-gray-300 rounded-md text-sm"
                        value={keyword}
                        onChange={(e) => setKeyword(e.target.value)}
                        onKeyDown={handleSearch}
                    />
                    <Search className="absolute left-3 top-2.5 w-4 h-4 text-gray-400" />
                </div>
            </div>

            {/* Order Table */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
                {loading ? (
                    <div className="p-8 text-center text-gray-500">加载中...</div>
                ) : (
                    <table className="min-w-full divide-y divide-gray-200">
                        <thead className="bg-gray-50">
                            <tr>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">订单信息</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">商品</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">消耗积分</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">核销码</th>
                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
                                <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
                            </tr>
                        </thead>
                        <tbody className="bg-white divide-y divide-gray-200">
                            {orders.length === 0 ? (
                                <tr><td colSpan={6} className="text-center py-8 text-gray-500">暂无订单数据</td></tr>
                            ) : (
                                orders.map((order) => (
                                    <tr key={order.id} className="hover:bg-gray-50">
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="text-sm font-medium text-gray-900">{order.orderNo || order.id}</div>
                                            <div className="text-xs text-gray-500 mt-1">{order.createTime}</div>
                                            <div className="text-sm text-gray-600 mt-1 flex items-center">
                                                <span className="bg-gray-100 text-gray-600 px-1.5 py-0.5 rounded text-xs mr-2">兑换人</span>
                                                {order.volunteerName}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4">
                                            <div className="flex items-center">
                                                <img src={order.productImage || 'https://via.placeholder.com/150'} className="w-10 h-10 rounded object-cover mr-3" alt="" />
                                                <div>
                                                    <div className="text-sm text-gray-900">{order.productName}</div>
                                                    <div className="text-xs text-gray-400">ID: {order.productId}</div>
                                                </div>
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <div className="flex items-center text-sm font-bold text-orange-600">
                                                <Coins className="w-4 h-4 mr-1 text-orange-500" />
                                                {order.cost}
                                            </div>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            <span className="font-mono text-lg font-bold tracking-widest text-gray-800">{order.verifyCode}</span>
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap">
                                            {getStatusBadge(order.status)}
                                        </td>
                                        <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                            {order.status === OrderStatus.PENDING && (
                                                <div className="flex items-center justify-end gap-2">
                                                    <button
                                                        onClick={() => { setSelectedOrder(order); setIsVerifyModalOpen(true); }}
                                                        className="text-blue-600 hover:text-blue-900"
                                                    >
                                                        核销
                                                    </button>
                                                    <span className="text-gray-300">|</span>
                                                    <button
                                                        onClick={() => handleCancel(order.id)}
                                                        className="text-red-600 hover:text-red-900"
                                                    >
                                                        取消
                                                    </button>
                                                </div>
                                            )}
                                        </td>
                                    </tr>
                                ))
                            )}
                        </tbody>
                    </table>
                )}
            </div>

            {/* Verify Modal */}
            <Modal
                isOpen={isVerifyModalOpen}
                onClose={() => setIsVerifyModalOpen(false)}
                title="确认核销"
                size="sm"
            >
                <div className="text-center">
                    <CheckCircle className="w-12 h-12 text-blue-500 mx-auto mb-4" />
                    <p className="text-lg font-medium text-gray-900 mb-2">请确认核销信息</p>

                    <div className="bg-gray-50 p-4 rounded-lg text-left mb-6 text-sm space-y-2">
                        <div className="flex justify-between">
                            <span className="text-gray-500">志愿者:</span>
                            <span className="font-medium">{selectedOrder?.volunteerName}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-gray-500">兑换商品:</span>
                            <span className="font-medium">{selectedOrder?.productName}</span>
                        </div>
                        <div className="flex justify-between">
                            <span className="text-gray-500">消耗积分:</span>
                            <span className="font-medium text-orange-600">{selectedOrder?.cost}</span>
                        </div>
                        <div className="flex justify-between border-t border-gray-200 pt-2 mt-2">
                            <span className="text-gray-500">核销码:</span>
                            <span className="font-mono font-bold text-lg">{selectedOrder?.verifyCode}</span>
                        </div>
                    </div>

                    <div className="flex gap-3 justify-center">
                        <button onClick={() => setIsVerifyModalOpen(false)} className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50">
                            取消
                        </button>
                        <button onClick={handleVerify} className="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700">
                            确认核销
                        </button>
                    </div>
                </div>
            </Modal>
        </div>
    );
};
