import React, { useState, useEffect } from 'react';
import { Search, User, Calendar } from 'lucide-react';
import { Link } from 'react-router-dom';
import { evidenceApi } from '../services/evidenceApi';
import { ServiceEvidence } from '../types';

export const EvidenceArchive: React.FC = () => {
    const [searchTerm, setSearchTerm] = useState('');
    const [evidenceList, setEvidenceList] = useState<ServiceEvidence[]>([]);
    const [loading, setLoading] = useState(true);
    const [total, setTotal] = useState(0);
    const [params, setParams] = useState({
        page: 1,
        pageSize: 12, // More items for masonry
    });

    useEffect(() => {
        const fetchEvidence = async () => {
            setLoading(true);
            try {
                const res = await evidenceApi.getEvidenceList({
                    ...params,
                    keyword: searchTerm
                });
                setEvidenceList(res.list);
                setTotal(res.total);
            } catch (error) {
                console.error('Failed to fetch evidence', error);
            } finally {
                setLoading(false);
            }
        };

        // Debounce search
        const timer = setTimeout(() => {
            fetchEvidence();
        }, 300);

        return () => clearTimeout(timer);
    }, [params.page, params.pageSize, searchTerm]);

    return (
        <div className="space-y-6">
            {/* Search Bar */}
            <div className="bg-white p-4 rounded-lg shadow-sm border border-gray-100 flex items-center gap-4">
                <div className="flex-1 relative">
                    <input
                        type="text"
                        className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                        placeholder="搜索 任务ID / 志愿者姓名 / 志愿者ID"
                        value={searchTerm}
                        onChange={(e) => {
                            setSearchTerm(e.target.value);
                            setParams(p => ({ ...p, page: 1 })); // Reset to page 1 on search
                        }}
                    />
                    <Search className="w-5 h-5 text-gray-400 absolute left-3 top-2.5" />
                </div>
                <div className="text-sm text-gray-500">
                    共归档 {total} 份服务凭证
                </div>
            </div>

            {loading && evidenceList.length === 0 ? (
                <div className="text-center py-10 text-gray-500">加载中...</div>
            ) : (
                <>
                    {/* Masonry Layout */}
                    <div className="columns-2 md:columns-3 lg:columns-4 gap-4 space-y-4">
                        {evidenceList.map((ev) => (
                            <div key={ev.id} className="break-inside-avoid bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden group hover:shadow-lg transition-shadow">
                                <div className="relative">
                                    <img src={ev.imageUrl} alt={ev.taskTitle} className="w-full h-auto object-cover" />
                                    <div className="absolute inset-0 bg-gradient-to-t from-black/70 via-transparent to-transparent opacity-0 group-hover:opacity-100 transition-opacity flex flex-col justify-end p-4">
                                        <Link to={`/task/detail/${ev.taskId}`} className="text-white font-bold hover:underline mb-1">
                                            任务 #{ev.taskId}
                                        </Link>
                                        <p className="text-white/80 text-xs">{ev.taskTitle}</p>
                                    </div>
                                </div>
                                <div className="p-3">
                                    <div className="flex items-center justify-between mb-2">
                                        <div className="flex items-center text-sm font-medium text-gray-900">
                                            <User className="w-3 h-3 mr-1 text-blue-500" />
                                            {ev.volunteerName}
                                        </div>
                                    </div>
                                    <div className="space-y-1">
                                        <div className="flex items-center text-xs text-gray-500">
                                            <Calendar className="w-3 h-3 mr-1" />
                                            {ev.createTime}
                                        </div>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>

                    {/* Simple Pagination */}
                    {total > params.pageSize && (
                        <div className="flex justify-center mt-6 gap-2">
                            <button
                                onClick={() => setParams(p => ({ ...p, page: Math.max(1, p.page - 1) }))}
                                disabled={params.page === 1}
                                className="px-4 py-2 border rounded bg-white hover:bg-gray-50 disabled:bg-gray-100 disabled:text-gray-400"
                            >
                                上一页
                            </button>
                            <span className="px-4 py-2 text-gray-600">第 {params.page} 页</span>
                            <button
                                onClick={() => setParams(p => ({ ...p, page: p.page + 1 }))}
                                disabled={params.page * params.pageSize >= total}
                                className="px-4 py-2 border rounded bg-white hover:bg-gray-50 disabled:bg-gray-100 disabled:text-gray-400"
                            >
                                下一页
                            </button>
                        </div>
                    )}

                    {!loading && evidenceList.length === 0 && (
                        <div className="text-center py-10 text-gray-500">暂无符合条件的服务凭证</div>
                    )}
                </>
            )}
        </div>
    );
};
