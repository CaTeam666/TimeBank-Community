import React, { useState, useEffect } from 'react';
import { familyApi } from '../services/familyApi';
import { FamilyBindingTask, AuditStatus } from '../types';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { CheckCircle, XCircle } from 'lucide-react';

export const FamilyBinding: React.FC = () => {
  const [tasks, setTasks] = useState<FamilyBindingTask[]>([]);
  const [loading, setLoading] = useState(false);
  const [selectedTask, setSelectedTask] = useState<FamilyBindingTask | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState('');

  const fetchTasks = async () => {
    try {
      setLoading(true);
      const res = await familyApi.getFamilyBindingList({ page: 1, pageSize: 100 }); // Fetch all for now or implement pagination later
      setTasks(res.list);
    } catch (error) {
      console.error('Failed to fetch family binding tasks', error);
      // Optional: Add toast notification here
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTasks();
  }, []);

  const handleAudit = async (status: AuditStatus) => {
    if (selectedTask) {
      try {
        await familyApi.auditFamilyBinding(selectedTask.serialNo, status, rejectReason);
        setIsModalOpen(false);
        setRejectReason('');
        setSelectedTask(null);
        fetchTasks(); // Refresh list
      } catch (error) {
        alert('审核操作失败，请重试');
      }
    }
  };

  const getStatusBadge = (status: AuditStatus) => {
    switch (status) {
      case AuditStatus.APPROVED: return <Badge color="green">已绑定</Badge>;
      case AuditStatus.REJECTED: return <Badge color="red">已拒绝</Badge>;
      default: return <Badge color="yellow">待审核</Badge>;
    }
  };

  return (
    <div className="space-y-6">
      <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 bg-gray-50 flex justify-between items-center">
          <h2 className="text-lg font-medium text-gray-900">亲情绑定申请</h2>
        </div>
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">申请流水号</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">发起人 (子女)</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">目标人 (老人)</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">申请时间</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">状态</th>
              <th className="px-6 py-3 text-right text-xs font-medium text-gray-500 uppercase tracking-wider">操作</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {tasks.map((task) => (
              <tr key={task.serialNo} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                  {task.serialNo}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium text-gray-900">{task.childName}</div>
                  <div className="text-xs text-gray-500">{task.childPhone}</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium text-gray-900">{task.elderName}</div>
                  <div className="text-xs text-gray-500">{task.elderPhone}</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {task.applyTime}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {getStatusBadge(task.status)}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                  {task.status === AuditStatus.PENDING ? (
                    <button
                      onClick={() => { setSelectedTask(task); setIsModalOpen(true); }}
                      className="text-blue-600 hover:text-blue-900 font-medium"
                    >
                      审核
                    </button>
                  ) : (
                    <span className="text-gray-400 cursor-not-allowed">已完成</span>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>

      {/* Binding Audit Modal */}
      {selectedTask && (
        <Modal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          title={`审核绑定申请: ${selectedTask.serialNo}`}
          size="lg"
        >
          <div className="space-y-6">
            <div className="grid grid-cols-2 gap-4 bg-gray-50 p-4 rounded-lg">
              <div>
                <h4 className="text-xs font-uppercase text-gray-500 mb-1">申请人 (子女)</h4>
                <p className="font-semibold">{selectedTask.childName}</p>
                <p className="text-sm text-gray-600">{selectedTask.childPhone}</p>
              </div>
              <div>
                <h4 className="text-xs font-uppercase text-gray-500 mb-1">目标人 (老人)</h4>
                <p className="font-semibold">{selectedTask.elderName}</p>
                <p className="text-sm text-gray-600">{selectedTask.elderPhone}</p>
              </div>
            </div>

            <div>
              <h4 className="text-sm font-medium text-gray-900 mb-2">证明材料</h4>
              <div className="flex flex-wrap gap-4 border border-gray-200 rounded-lg p-4 bg-gray-100 justify-center">
                {selectedTask.proofImages && selectedTask.proofImages.map((img, index) => (
                  <div key={index} className="relative group">
                    <img
                      src={img}
                      alt={`Proof ${index + 1}`}
                      className="max-h-48 object-contain rounded shadow-sm hover:shadow-md transition-shadow cursor-zoom-in"
                      onClick={() => window.open(img, '_blank')}
                    />
                  </div>
                ))}
              </div>
              <p className="text-xs text-gray-500 mt-1 text-center">点击图片可查看原图（户口本 / 亲属合照）</p>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">拒绝理由 (若拒绝)</label>
              <input
                type="text"
                className="w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                placeholder="例如：证明材料不足、非直系亲属..."
                value={rejectReason}
                onChange={(e) => setRejectReason(e.target.value)}
              />
            </div>

            <div className="flex justify-end gap-3 pt-4 border-t">
              <button
                onClick={() => handleAudit(AuditStatus.REJECTED)}
                disabled={!rejectReason}
                className="flex items-center px-4 py-2 border border-red-300 text-red-700 rounded-md hover:bg-red-50 disabled:opacity-50"
              >
                <XCircle className="w-4 h-4 mr-2" />
                拒绝申请
              </button>
              <button
                onClick={() => handleAudit(AuditStatus.APPROVED)}
                className="flex items-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 shadow-sm"
              >
                <CheckCircle className="w-4 h-4 mr-2" />
                通过绑定
              </button>
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};