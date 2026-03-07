import React, { useState, useEffect } from 'react';
import { IdentityAuditTask, AuditStatus } from '../types';
import { identityApi } from '../services/identityApi';
import { Badge } from '../components/ui/Badge';
import { Modal } from '../components/ui/Modal';
import { RefreshCw, CheckCircle, XCircle, RotateCw, Loader } from 'lucide-react';

export const IdentityAudit: React.FC = () => {
  const [activeTab, setActiveTab] = useState<AuditStatus>(AuditStatus.PENDING);
  const [tasks, setTasks] = useState<IdentityAuditTask[]>([]);
  const [loading, setLoading] = useState(false);

  const [selectedTask, setSelectedTask] = useState<IdentityAuditTask | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [rejectReason, setRejectReason] = useState('');

  // Image Viewer State
  const [rotation, setRotation] = useState(0);

  // Fetch Tasks based on active tab
  const fetchTasks = async () => {
    setLoading(true);
    try {
      // Fetching up to 50 items for now to keep it simple without full pagination UI yet
      const res = await identityApi.getAuditList({ status: activeTab, page: 1, pageSize: 50 });
      setTasks(res.list);
    } catch (error) {
      console.error('Failed to fetch tasks:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTasks();
  }, [activeTab]);

  const handleAudit = async (status: AuditStatus) => {
    if (!selectedTask) return;

    try {
      await identityApi.submitAuditResult(selectedTask.id, {
        status,
        rejectReason: status === AuditStatus.REJECTED ? rejectReason : undefined
      });

      // Simulating the automated coin distribution logic on the frontend presentation
      // In a real app, this should be handled by backend and returned in response or notification
      if (status === AuditStatus.APPROVED && selectedTask.ocrAge > 65) {
        alert(`用户年龄为 ${selectedTask.ocrAge} 岁。系统将自动发放 500 积分。`);
      }

      if (status === AuditStatus.REJECTED) {
        console.log(`驳回原因: ${rejectReason}`);
      }

      setIsModalOpen(false);
      setSelectedTask(null);
      setRejectReason('');
      setRotation(0);

      // Refresh data
      fetchTasks();

    } catch (error) {
      alert('操作失败，请重试');
      console.error(error);
    }
  };

  const rotateImage = () => setRotation(prev => prev + 90);

  return (
    <div className="space-y-6">
      {/* Tabs */}
      <div className="border-b border-gray-200">
        <nav className="-mb-px flex space-x-8">
          <button
            onClick={() => setActiveTab(AuditStatus.PENDING)}
            className={`whitespace-nowrap pb-4 px-1 border-b-2 font-medium text-sm flex items-center ${activeTab === AuditStatus.PENDING
              ? 'border-blue-500 text-blue-600'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
          >
            待审核
          </button>
          <button
            onClick={() => setActiveTab(AuditStatus.APPROVED)}
            className={`whitespace-nowrap pb-4 px-1 border-b-2 font-medium text-sm ${activeTab === AuditStatus.APPROVED
              ? 'border-green-500 text-green-600'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
          >
            已通过
          </button>
          <button
            onClick={() => setActiveTab(AuditStatus.REJECTED)}
            className={`whitespace-nowrap pb-4 px-1 border-b-2 font-medium text-sm ${activeTab === AuditStatus.REJECTED
              ? 'border-red-500 text-red-600'
              : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
              }`}
          >
            已驳回
          </button>
        </nav>
      </div>

      {/* Grid of Tasks */}
      {loading ? (
        <div className="flex justify-center items-center py-20">
          <Loader className="w-8 h-8 text-blue-500 animate-spin" />
        </div>
      ) : (
        <>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {tasks.map(task => (
              <div key={task.id} className="bg-white rounded-lg shadow-sm border border-gray-200 hover:shadow-md transition-shadow p-6">
                <div className="flex justify-between items-start mb-4">
                  <div>
                    <h3 className="text-lg font-medium text-gray-900">{task.userName}</h3>
                    <p className="text-sm text-gray-500">ID: {task.userId}</p>
                  </div>
                  <Badge color={task.ocrAge > 65 ? 'orange' : 'blue'}>{task.ocrAge} 岁</Badge>
                </div>

                <div className="space-y-2 mb-6">
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">提交时间:</span>
                    <span className="text-gray-900">{task.submitTime}</span>
                  </div>
                  <div className="flex justify-between text-sm">
                    <span className="text-gray-500">任务编号:</span>
                    <span className="text-gray-900 font-mono text-xs">{task.id}</span>
                  </div>
                </div>

                <button
                  onClick={() => { setSelectedTask(task); setIsModalOpen(true); }}
                  className="w-full inline-flex justify-center items-center px-4 py-2 border border-transparent shadow-sm text-sm font-medium rounded-md text-white bg-blue-600 hover:bg-blue-700 focus:outline-none"
                >
                  审核详情
                </button>
              </div>
            ))}
          </div>

          {tasks.length === 0 && (
            <div className="text-center py-12 text-gray-500 bg-white rounded-lg border border-dashed border-gray-300">
              暂无该状态的任务。
            </div>
          )}
        </>
      )}

      {/* Audit Modal */}
      {selectedTask && (
        <Modal
          isOpen={isModalOpen}
          onClose={() => setIsModalOpen(false)}
          title="实名认证审核"
          size="2xl"
        >
          <div className="flex flex-col lg:flex-row gap-6 h-[600px]">
            {/* Left: Images */}
            <div className="lg:w-1/2 flex flex-col gap-4 bg-gray-100 p-4 rounded-lg overflow-y-auto">
              <div className="flex justify-between items-center mb-2">
                <span className="text-sm font-medium text-gray-700">上传证件</span>
                <button onClick={rotateImage} className="text-gray-600 hover:text-blue-600 flex items-center text-xs">
                  <RotateCw className="w-4 h-4 mr-1" /> 旋转
                </button>
              </div>

              <div className="relative group overflow-hidden rounded-lg border border-gray-300 bg-black flex items-center justify-center min-h-[200px]">
                <img
                  src={selectedTask.idCardFront}
                  className="transition-transform duration-300 max-w-full"
                  style={{ transform: `rotate(${rotation}deg)` }}
                  alt="Front"
                />
                <div className="absolute top-2 left-2 bg-black bg-opacity-50 text-white text-xs px-2 py-1 rounded">人像面</div>
              </div>

              <div className="relative group overflow-hidden rounded-lg border border-gray-300 bg-black flex items-center justify-center min-h-[200px]">
                <img
                  src={selectedTask.idCardBack}
                  className="transition-transform duration-300 max-w-full"
                  style={{ transform: `rotate(${rotation}deg)` }}
                  alt="Back"
                />
                <div className="absolute top-2 left-2 bg-black bg-opacity-50 text-white text-xs px-2 py-1 rounded">国徽面</div>
              </div>
            </div>

            {/* Right: Info & Actions */}
            <div className="lg:w-1/2 flex flex-col">
              <div className="flex-1 space-y-6">
                <div className="bg-blue-50 p-4 rounded-md border border-blue-100">
                  <h4 className="text-sm font-semibold text-blue-900 mb-3 flex items-center">
                    <RefreshCw className="w-4 h-4 mr-2" />
                    系统自动识别信息
                  </h4>
                  <dl className="grid grid-cols-1 gap-x-4 gap-y-4 sm:grid-cols-2">
                    <div className="sm:col-span-1">
                      <dt className="text-xs font-medium text-blue-500 uppercase">姓名</dt>
                      <dd className="mt-1 text-sm font-bold text-gray-900">{selectedTask.ocrName}</dd>
                    </div>
                    <div className="sm:col-span-1">
                      <dt className="text-xs font-medium text-blue-500 uppercase">年龄</dt>
                      <dd className="mt-1 text-sm font-bold text-gray-900">{selectedTask.ocrAge} 岁</dd>
                    </div>
                    <div className="sm:col-span-2">
                      <dt className="text-xs font-medium text-blue-500 uppercase">身份证号</dt>
                      <dd className="mt-1 text-sm font-bold text-gray-900 tracking-wider font-mono">{selectedTask.ocrIdNumber}</dd>
                    </div>
                  </dl>
                </div>

                {selectedTask.ocrAge > 65 && (
                  <div className="bg-yellow-50 border-l-4 border-yellow-400 p-4">
                    <div className="flex">
                      <div className="flex-shrink-0">
                        <span className="text-yellow-400 text-xl">⚠️</span>
                      </div>
                      <div className="ml-3">
                        <p className="text-sm text-yellow-700">
                          <span className="font-bold">符合老人福利标准：</span> 用户年龄大于65岁。通过后将自动发放 500 积分。
                        </p>
                      </div>
                    </div>
                  </div>
                )}

                {activeTab === AuditStatus.PENDING && (
                  <div className="mt-4">
                    <label className="block text-sm font-medium text-gray-700 mb-2">驳回原因 (若驳回)</label>
                    <textarea
                      className="w-full border border-gray-300 rounded-md shadow-sm py-2 px-3 focus:outline-none focus:ring-blue-500 focus:border-blue-500 sm:text-sm"
                      rows={3}
                      placeholder="例如：图片模糊，非本人..."
                      value={rejectReason}
                      onChange={(e) => setRejectReason(e.target.value)}
                    />
                  </div>
                )}
              </div>

              {activeTab === AuditStatus.PENDING && (
                <div className="mt-6 flex justify-end gap-3 border-t pt-4">
                  <button
                    onClick={() => handleAudit(AuditStatus.REJECTED)}
                    disabled={!rejectReason}
                    className="flex items-center px-4 py-2 border border-red-300 text-red-700 rounded-md hover:bg-red-50 disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    <XCircle className="w-4 h-4 mr-2" />
                    驳回
                  </button>
                  <button
                    onClick={() => handleAudit(AuditStatus.APPROVED)}
                    className="flex items-center px-4 py-2 bg-green-600 text-white rounded-md hover:bg-green-700 shadow-sm"
                  >
                    <CheckCircle className="w-4 h-4 mr-2" />
                    通过并认证
                  </button>
                </div>
              )}
            </div>
          </div>
        </Modal>
      )}
    </div>
  );
};