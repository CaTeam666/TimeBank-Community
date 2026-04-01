import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useMall } from '../context/MallContext';
import { NavBar, Card, Input } from '../components/UIComponents';
import { ProductCategory } from '../types';
import { Search, ShoppingBag, Coins } from 'lucide-react';

export default function MallHome() {
    const navigate = useNavigate();
    const { state } = useAuth();
    const { products, loadProducts } = useMall();

    const [searchTerm, setSearchTerm] = useState('');
    const [activeCategory, setActiveCategory] = useState<ProductCategory | '全部'>('全部');

    const categories: (ProductCategory | '全部')[] = ['全部', '粮油副食', '日用百货', '医疗健康', '虚拟券卡'];

    // Identify current user balance
    const currentUser = state.isProxyMode && state.proxyTarget ? state.proxyTarget : state.currentUser;
    const balance = currentUser?.balance || 0;

    // Trigger API call when filters change
    React.useEffect(() => {
        const queryParams: { keyword?: string; category?: string } = {};
        if (searchTerm) queryParams.keyword = searchTerm;
        if (activeCategory !== '全部') queryParams.category = activeCategory;

        loadProducts(queryParams);
    }, [searchTerm, activeCategory]);

    return (
        <div className="min-h-screen bg-gray-50 flex flex-col">
            <NavBar title="爱心超市" showBack />

            {/* Asset Header */}
            <div className="bg-orange-500 px-4 py-4 text-white">
                <div className="flex justify-between items-center mb-2">
                    <span className="text-orange-100 text-sm">当前可用余额</span>
                    <div className="bg-white/20 px-2 py-1 rounded text-xs">
                        {state.isProxyMode ? `代理: ${currentUser?.nickname}` : '我的账户'}
                    </div>
                </div>
                <div className="flex items-baseline gap-2">
                    <span className="text-3xl font-bold">{balance.toLocaleString()}</span>
                    <span className="text-sm opacity-80">积分</span>
                </div>
            </div>

            {/* Search */}
            <div className="bg-white p-2 border-b border-gray-100 sticky top-12 z-20">
                <div className="relative">
                    <Search size={16} className="absolute left-3 top-3 text-gray-400" />
                    <input
                        className="w-full bg-gray-100 rounded-full py-2 pl-9 pr-4 text-sm focus:outline-none"
                        placeholder="搜索商品..."
                        value={searchTerm}
                        onChange={e => setSearchTerm(e.target.value)}
                    // Debounce could be added here, but for now direct state update triggers effect
                    />
                </div>
            </div>

            {/* Main Content: Sidebar + Grid */}
            <div className="flex flex-1 overflow-hidden pb-safe-area">
                {/* Sidebar */}
                <div className="w-24 bg-gray-100 overflow-y-auto hide-scrollbar pb-20">
                    {categories.map(cat => (
                        <button
                            key={cat}
                            onClick={() => setActiveCategory(cat)}
                            className={`w-full py-4 text-xs font-medium relative ${activeCategory === cat
                                ? 'bg-white text-orange-600 font-bold'
                                : 'text-gray-600'
                                }`}
                        >
                            {activeCategory === cat && (
                                <div className="absolute left-0 top-1/2 -translate-y-1/2 w-1 h-4 bg-orange-500 rounded-r"></div>
                            )}
                            {cat}
                        </button>
                    ))}
                </div>

                {/* Product Grid */}
                <div className="flex-1 overflow-y-auto p-3 pb-20">
                    <div className="grid grid-cols-2 gap-3">
                        {products.map(product => (
                            <div
                                key={product.id}
                                onClick={() => navigate(`/mall/detail/${product.id}`)}
                                className="bg-white rounded-lg shadow-sm overflow-hidden flex flex-col active:scale-95 transition-transform"
                            >
                                <img src={product.image} className="w-full aspect-square object-cover" loading="lazy" />
                                <div className="p-2 flex flex-col flex-1">
                                    <h3 className="text-sm font-bold text-gray-800 line-clamp-2 mb-1">{product.name}</h3>
                                    <div className="mt-auto">
                                        <div className="flex items-baseline gap-1 text-red-500 font-bold">
                                            <span className="text-xs">¥</span>
                                            <span className="text-lg">{product.price}</span>
                                        </div>
                                        <div className="flex justify-between items-center mt-1">
                                            <span className="text-[10px] text-gray-400">剩 {product.stock} 件</span>
                                            <div className="w-6 h-6 rounded-full bg-orange-100 text-orange-500 flex items-center justify-center">
                                                <ShoppingBag size={12} />
                                            </div>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                    {products.length === 0 && (
                        <div className="flex flex-col items-center justify-center h-48 text-gray-400 text-sm">
                            <p>暂无相关商品</p>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
