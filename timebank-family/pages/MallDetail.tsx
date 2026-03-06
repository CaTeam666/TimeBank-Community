import React, { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useMall } from '../context/MallContext';
import { useAuth } from '../context/AuthContext';
import { NavBar, Button, Card, Modal } from '../components/UIComponents';
import { ShoppingBag, Minus, Plus, AlertCircle } from 'lucide-react';

export default function MallDetail() {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const { getProductById, createOrder } = useMall();
    const { state, dispatch } = useAuth();

    const product = getProductById(id || '');
    const [showCheckout, setShowCheckout] = useState(false);
    const [quantity, setQuantity] = useState(1);
    const [isProcessing, setIsProcessing] = useState(false);

    if (!product) return <div>商品不存在</div>;

    // Current User Context (handling proxy)
    const currentUser = state.isProxyMode && state.proxyTarget ? state.proxyTarget : state.currentUser;

    const totalPrice = product.price * quantity;
    const canAfford = currentUser && currentUser.balance >= totalPrice;

    const handleExchange = async () => {
        if (!currentUser) {
            navigate('/login');
            return;
        }
        if (!canAfford) return; // UI should disable, but double check
        if (product.stock < quantity) return;

        setIsProcessing(true);

        // 1. Create Order via API
        const success = await createOrder(currentUser.id, product, quantity);

        setIsProcessing(false);

        if (success) {
            // 2. Deduct Balance Locally (Optimistic update)
            dispatch({
                type: 'UPDATE_BALANCE',
                payload: { userId: currentUser.id, amount: currentUser.balance - totalPrice }
            });

            setShowCheckout(false);

            // 3. Success Feedback
            if (window.confirm("兑换成功！是否前往查看订单凭证？")) {
                navigate('/mall/orders');
            } else {
                navigate(-1);
            }
        } else {
            alert("兑换失败，请稍后重试。");
        }
    };

    return (
        <div className="min-h-screen bg-gray-50 pb-24">
            <NavBar title="商品详情" showBack />

            {/* Hero Image */}
            <div className="w-full aspect-square bg-white">
                <img src={product.image} className="w-full h-full object-cover" />
            </div>

            {/* Info Card */}
            <div className="px-4 -mt-4 relative z-10 space-y-3">
                <Card>
                    <div className="flex justify-between items-start">
                        <div className="flex items-baseline gap-1 text-red-500 font-bold">
                            <span className="text-sm">积分</span>
                            <span className="text-3xl">{product.price}</span>
                        </div>
                        <span className="text-xs text-gray-400 bg-gray-100 px-2 py-1 rounded-full">
                            库存 {product.stock}
                        </span>
                    </div>
                    <h1 className="text-lg font-bold text-gray-800 mt-2">{product.name}</h1>
                    <div className="flex items-center gap-4 mt-2 text-xs text-gray-500 border-t border-gray-100 pt-2">
                        <span>月销 {product.sales}</span>
                        <span>全场包邮</span>
                        <span>正品保障</span>
                    </div>
                </Card>

                {/* Description */}
                <div className="bg-white p-4 rounded-xl shadow-sm">
                    <h3 className="font-bold text-gray-800 mb-2 text-sm border-l-4 border-orange-500 pl-2">商品详情</h3>
                    <p className="text-sm text-gray-600 leading-relaxed">
                        {product.description}
                    </p>
                    <div className="mt-4 h-40 bg-gray-50 rounded-lg flex items-center justify-center text-gray-300 text-xs">
                        [商品详情长图占位]
                    </div>
                </div>
            </div>

            {/* Bottom Bar */}
            <div className="fixed bottom-0 left-0 right-0 max-w-md mx-auto bg-white border-t border-gray-100 p-3 z-50">
                <Button
                    fullWidth
                    onClick={() => setShowCheckout(true)}
                    disabled={product.stock <= 0}
                    className={product.stock <= 0 ? "bg-gray-300" : ""}
                >
                    {product.stock <= 0 ? '暂时缺货' : '立即兑换'}
                </Button>
            </div>

            {/* Checkout Modal */}
            <Modal isOpen={showCheckout} onClose={() => setShowCheckout(false)} title="确认订单">
                <div className="space-y-4">
                    <div className="flex gap-3">
                        <img src={product.image} className="w-20 h-20 rounded bg-gray-100 object-cover shrink-0" />
                        <div>
                            <p className="font-bold text-gray-800 line-clamp-1">{product.name}</p>
                            <p className="text-red-500 font-bold mt-1">{product.price} 积分</p>
                        </div>
                    </div>

                    <div className="flex justify-between items-center py-2 border-t border-b border-gray-50">
                        <span className="text-sm text-gray-600">兑换数量</span>
                        <div className="flex items-center gap-3">
                            <button
                                onClick={() => setQuantity(Math.max(1, quantity - 1))}
                                className="w-8 h-8 rounded bg-gray-100 flex items-center justify-center active:bg-gray-200"
                            >
                                <Minus size={14} />
                            </button>
                            <span className="w-8 text-center font-bold text-sm">{quantity}</span>
                            <button
                                onClick={() => setQuantity(Math.min(product.stock, quantity + 1))}
                                className="w-8 h-8 rounded bg-gray-100 flex items-center justify-center active:bg-gray-200"
                            >
                                <Plus size={14} />
                            </button>
                        </div>
                    </div>

                    <div className="space-y-2">
                        <div className="flex justify-between text-sm">
                            <span className="text-gray-500">合计支付</span>
                            <span className="font-bold text-red-500 text-lg">{totalPrice} 积分</span>
                        </div>
                        <div className="flex justify-between text-xs">
                            <span className="text-gray-500">当前余额</span>
                            <span className="text-gray-700">{currentUser?.balance} 积分</span>
                        </div>
                        {!canAfford && (
                            <div className="flex items-center gap-1 text-red-500 text-xs bg-red-50 p-2 rounded">
                                <AlertCircle size={12} />
                                余额不足，快去接单赚币吧！
                            </div>
                        )}
                        {currentUser?.balance && canAfford && (
                            <div className="flex justify-between text-xs text-green-600">
                                <span>兑换后余额</span>
                                <span>{currentUser.balance - totalPrice} 积分</span>
                            </div>
                        )}
                    </div>

                    <Button fullWidth onClick={handleExchange} disabled={!canAfford || isProcessing}>
                        {isProcessing ? '处理中...' : '确认支付'}
                    </Button>
                </div>
            </Modal>
        </div>
    );
}
