import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Gavel } from 'lucide-react';
import { Badge } from '../components/ui/Badge';
import { ArbitrationStatus } from '../types';
import { arbitrationApi } from '../services/arbitrationApi';
import { Arbitration } from '../types';

export const ArbitrationList: React.FC = () => {
    const [arbitrations, setArbitrations] = useState<Arbitration[]>([]);
    const [loading, setLoading] = useState(true);
    const [total, setTotal] = useState(0);
    const [params, setParams] = useState({
        page: 1,
        pageSize: 10,
        status: 'ALL'
    });

    useEffect(() => {
        fetchArbitrations();
    }, [params.page, params.pageSize, params.status]);

    const fetchArbitrations = async () => {
        setLoading(true);
        try {
            const res = await arbitrationApi.getArbitrations({
                page: params.page,
                pageSize: params.pageSize,
                status: params.status === 'ALL' ? undefined : params.status
            });
            setArbitrations(res.list);
            setTotal(res.total);
        } catch (error) {
            console.error('Failed to fetch arbitrations', error);
        } finally {
            setLoading(false);
        }
    };

    // Mock checking urgency based on date (assuming today is fixed or real)
    const isUrgent = (dateStr: string) => {
        const now = new Date().getTime();
        const created = new Date(dateStr.replace(' ', 'T')).getTime();
        return (now - created) > 24 * 60 * 60 * 1000;
    };

    return (
        <div className="space-y-6">
            {/* Arbitration List */}
            <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
                <div className="px-6 py-4 border-b border-gray-100 font-medium text-gray-900 bg-gray-50 flex justify-between items-center">
                    <span>申诉案件列表</span>
                    <div className="flex gap-2 text-sm">
                        <button
                            onClick={() => setParams(p => ({ ...p, status: 'ALL', page: 1 }))}
                            className={`px-3 py-1 rounded ${params.status === 'ALL' ? 'bg-blue-100 text-blue-700' : 'text-gray-600 hover:bg-gray-100'}`}
                        >全部</button>
                        <button
                            onClick={() => setParams(p => ({ ...p, status: ArbitrationStatus.PENDING, page: 1 }))}
                            className={`px-3 py-1 rounded ${params.status === ArbitrationStatus.PENDING ? 'bg-blue-100 text-blue-700' : 'text-gray-600 hover:bg-gray-100'}`}
                        >待处理</button>
                        <button
                            onClick={() => setParams(p => ({ ...p, status: ArbitrationStatus.RESOLVED, page: 1 }))}
                            className={`px-3 py-1 rounded ${params.status === ArbitrationStatus.RESOLVED ? 'bg-blue-100 text-blue-700' : 'text-gray-600 hover:bg-gray-100'}`}
                        >已处理</button>
                    </div>
                </div>

                {loading ? (
                    <div className="p-8 text-center text-gray-500">加载中...</div>
                ) : (
                    <>
                        <table className="min-w-full divide-y divide-gray-200">
                            <thead className="bg-gray-50">
                                <tr>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">申诉 ID</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">关联任务</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">发起方</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">申诉类型</th>
                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">提交时间</th>
                                    <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
                                </tr>
                            </thead>
                            <tbody className="bg-white divide-y divide-gray-200">
                                {arbitrations.map((item) => {
                                    const urgent = isUrgent(item.createTime);
                                    return (
                                        <tr key={item.id} className="hover:bg-gray-50">
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                                                {item.id}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <Link to={`/task/detail/${item.taskId}`} className="text-blue-600 hover:underline text-sm font-medium">
                                                    #{item.taskId}
                                                </Link>
                                                <div className="text-xs text-gray-500 truncate max-w-[150px]">{item.taskTitle}</div>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <div className="flex items-center">
                                                    <span className={`mr-2 px-1.5 py-0.5 rounded text-xs font-bold ${item.initiatorRole === 'VOLUNTEER' ? 'bg-green-100 text-green-700' : 'bg-orange-100 text-orange-700'}`}>
                                                        {item.initiatorRole === 'VOLUNTEER' ? '志愿者' : '发布者'}
                                                    </span>
                                                    <span className="text-sm text-gray-700">{item.initiatorName}</span>
                                                </div>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-700">
                                                {item.type}
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap">
                                                <div className={`text-sm ${urgent ? 'text-red-600 font-bold' : 'text-gray-500'}`}>
                                                    {item.createTime}
                                                    {urgent && <span className="ml-2 text-xs bg-red-100 text-red-600 px-1 py-0.5 rounded">超时</span>}
                                                </div>
                                            </td>
                                            <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                                                {item.status === ArbitrationStatus.PENDING ? (
                                                    <Link
                                                        to={`/service/arbitration/handle/${item.id}`}
                                                        className="inline-flex items-center px-3 py-1.5 border border-transparent text-xs font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700"
                                                    >
                                                        <Gavel className="w-3 h-3 mr-1" />
                                                        介入裁决
                                                    </Link>
                                                ) : (
                                                    <span className="text-gray-400 cursor-not-allowed">已结案</span>
                                                )}
                                            </td>
                                        </tr>
                                    );
                                })}
                                {arbitrations.length === 0 && (
                                    <tr><td colSpan={6} className="px-6 py-10 text-center text-gray-500">暂无申诉数据</td></tr>
                                )}
                            </tbody>
                        </table>
                        {/* Pagination */}
                        <div className="bg-white px-4 py-3 flex items-center justify-between border-t border-gray-200 sm:px-6">
                            <div className="hidden sm:flex-1 sm:flex sm:items-center sm:justify-between">
                                <div>
                                    <p className="text-sm text-gray-700">
                                        显示第 <span className="font-medium">{(params.page - 1) * params.pageSize + 1}</span> 到 <span className="font-medium">{Math.min(params.page * params.pageSize, total)}</span> 条，共 <span className="font-medium">{total}</span> 条
                                    </p>
                                </div>
                                <div>
                                    <nav className="relative z-0 inline-flex rounded-md shadow-sm -space-x-px" aria-label="Pagination">
                                        <button
                                            onClick={() => setParams(prev => ({ ...prev, page: Math.max(1, prev.page - 1) }))}
                                            disabled={params.page === 1}
                                            className="relative inline-flex items-center px-2 py-2 rounded-l-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:bg-gray-100 disabled:cursor-not-allowed"
                                        >
                                            上一页
                                        </button>
                                        <button
                                            onClick={() => setParams(prev => ({ ...prev, page: prev.page + 1 }))}
                                            disabled={params.page * params.pageSize >= total}
                                            className="relative inline-flex items-center px-2 py-2 rounded-r-md border border-gray-300 bg-white text-sm font-medium text-gray-500 hover:bg-gray-50 disabled:bg-gray-100 disabled:cursor-not-allowed"
                                        >
                                            下一页
                                        </button>
                                    </nav>
                                </div>
                            </div>
                        </div>
                    </>
                )}
            </div>
        </div>
    );
};
