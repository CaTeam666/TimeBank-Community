import React, { useState, useEffect } from 'react';
import { systemApi, SystemSettings } from '../services/systemApi';
import { Save, Sliders, Gift, Award, AlertCircle, Loader2, Key } from 'lucide-react';
import { Modal } from '../components/ui/Modal';

export const SystemSettingsPage: React.FC = () => {
    const [settings, setSettings] = useState<SystemSettings>({
        elderInitialCoins: 0,
        dailySignInReward: 0,
        monthlyRank1Reward: 0,
        transactionFeePercent: 0,
        zombieTaskTimeoutHours: 24,
        taskAutoAcceptDays: 3,
        familyBindingMaxLimit: 3,
        proxyDailyActionLimit: 5,
        orderCancelTimeoutHours: 24
    });
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [isPasswordModalOpen, setIsPasswordModalOpen] = useState(false);
    const [adminPassword, setAdminPassword] = useState('');

    useEffect(() => {
        loadSettings();
    }, []);

    const loadSettings = async () => {
        try {
            setLoading(true);
            const data = await systemApi.getSettings();
            setSettings(data);
        } catch (error) {
            alert('加载配置失败，请重试');
        } finally {
            setLoading(false);
        }
    };

    const handleSave = () => {
        setAdminPassword('');
        setIsPasswordModalOpen(true);
    };

    const confirmSave = async () => {
        if (!adminPassword.trim()) {
            alert('请输入管理员密码');
            return;
        }

        try {
            setSaving(true);
            await systemApi.updateSettings(adminPassword, settings);
            alert('系统配置已保存生效！');
            setIsPasswordModalOpen(false);
        } catch (error: any) {
            alert(error.message || '保存失败，请检查密码或重试');
        } finally {
            setSaving(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64 text-gray-500">
                <Loader2 className="w-8 h-8 animate-spin mr-2" />
                正在加载配置...
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto space-y-8">
            <div className="flex items-center justify-between">
                <h2 className="text-2xl font-bold text-gray-800 flex items-center">
                    <Sliders className="w-6 h-6 mr-3 text-gray-600" />
                    系统核心参数配置
                </h2>
            </div>

            <div className="grid grid-cols-1 gap-8">
                {/* Welfare Config */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
                    <div className="px-6 py-4 border-b border-gray-100 bg-gray-50 flex items-center">
                        <Gift className="w-5 h-5 text-blue-500 mr-2" />
                        <h3 className="font-bold text-gray-800">福利参数配置</h3>
                    </div>
                    <div className="p-6 space-y-6">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    老人初始赠送积分
                                </label>
                                <div className="relative rounded-md shadow-sm">
                                    <input
                                        type="number"
                                        className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-4 pr-12 sm:text-sm border-gray-300 rounded-md py-2 border"
                                        value={isNaN(settings.elderInitialCoins) ? '' : settings.elderInitialCoins}
                                        onChange={(e) => setSettings({ ...settings, elderInitialCoins: e.target.value === '' ? NaN : parseInt(e.target.value) })}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                                        <span className="text-gray-500 sm:text-sm">币</span>
                                    </div>
                                </div>
                                <p className="mt-1 text-xs text-gray-500">新注册且通过实名认证的老人用户自动获得。</p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    每日签到基础奖励
                                </label>
                                <div className="relative rounded-md shadow-sm">
                                    <input
                                        type="number"
                                        className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-4 pr-12 sm:text-sm border-gray-300 rounded-md py-2 border"
                                        value={isNaN(settings.dailySignInReward) ? '' : settings.dailySignInReward}
                                        onChange={(e) => setSettings({ ...settings, dailySignInReward: e.target.value === '' ? NaN : parseInt(e.target.value) })}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                                        <span className="text-gray-500 sm:text-sm">币</span>
                                    </div>
                                </div>
                                <p className="mt-1 text-xs text-gray-500">所有用户每日登录签到可获得的奖励。</p>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Incentive Config */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
                    <div className="px-6 py-4 border-b border-gray-100 bg-gray-50 flex items-center">
                        <Award className="w-5 h-5 text-orange-500 mr-2" />
                        <h3 className="font-bold text-gray-800">激励与交易配置</h3>
                    </div>
                    <div className="p-6 space-y-6">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    月度排行榜第一名奖金
                                </label>
                                <div className="relative rounded-md shadow-sm">
                                    <input
                                        type="number"
                                        className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-4 pr-12 sm:text-sm border-gray-300 rounded-md py-2 border"
                                        value={isNaN(settings.monthlyRank1Reward) ? '' : settings.monthlyRank1Reward}
                                        onChange={(e) => setSettings({ ...settings, monthlyRank1Reward: e.target.value === '' ? NaN : parseInt(e.target.value) })}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                                        <span className="text-gray-500 sm:text-sm">币</span>
                                    </div>
                                </div>
                                <p className="mt-1 text-xs text-gray-500">每月自动发放给服务时长第一名的志愿者。</p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    月度排行榜第二名奖金
                                </label>
                                <div className="relative rounded-md shadow-sm">
                                    <input
                                        type="number"
                                        className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-4 pr-12 sm:text-sm border-gray-300 rounded-md py-2 border"
                                        value={isNaN(settings.monthlyRank2Reward) ? '' : settings.monthlyRank2Reward}
                                        onChange={(e) => setSettings({ ...settings, monthlyRank2Reward: e.target.value === '' ? NaN : parseInt(e.target.value) })}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                                        <span className="text-gray-500 sm:text-sm">币</span>
                                    </div>
                                </div>
                                <p className="mt-1 text-xs text-gray-500">每月自动发放给服务时长第二名的志愿者。</p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    月度排行榜第三名奖金
                                </label>
                                <div className="relative rounded-md shadow-sm">
                                    <input
                                        type="number"
                                        className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-4 pr-12 sm:text-sm border-gray-300 rounded-md py-2 border"
                                        value={isNaN(settings.monthlyRank3Reward) ? '' : settings.monthlyRank3Reward}
                                        onChange={(e) => setSettings({ ...settings, monthlyRank3Reward: e.target.value === '' ? NaN : parseInt(e.target.value) })}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                                        <span className="text-gray-500 sm:text-sm">币</span>
                                    </div>
                                </div>
                                <p className="mt-1 text-xs text-gray-500">每月自动发放给服务时长第三名的志愿者。</p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    月度排行榜第四名奖金
                                </label>
                                <div className="relative rounded-md shadow-sm">
                                    <input
                                        type="number"
                                        className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-4 pr-12 sm:text-sm border-gray-300 rounded-md py-2 border"
                                        value={isNaN(settings.monthlyRank4Reward) ? '' : settings.monthlyRank4Reward}
                                        onChange={(e) => setSettings({ ...settings, monthlyRank4Reward: e.target.value === '' ? NaN : parseInt(e.target.value) })}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                                        <span className="text-gray-500 sm:text-sm">币</span>
                                    </div>
                                </div>
                                <p className="mt-1 text-xs text-gray-500">每月自动发放给服务时长第四名的志愿者。</p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    月度排行榜第五名奖金
                                </label>
                                <div className="relative rounded-md shadow-sm">
                                    <input
                                        type="number"
                                        className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-4 pr-12 sm:text-sm border-gray-300 rounded-md py-2 border"
                                        value={isNaN(settings.monthlyRank5Reward) ? '' : settings.monthlyRank5Reward}
                                        onChange={(e) => setSettings({ ...settings, monthlyRank5Reward: e.target.value === '' ? NaN : parseInt(e.target.value) })}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                                        <span className="text-gray-500 sm:text-sm">币</span>
                                    </div>
                                </div>
                                <p className="mt-1 text-xs text-gray-500">每月自动发放给服务时长第五名的志愿者。</p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    任务发布预扣手续费比例
                                </label>
                                <div className="relative rounded-md shadow-sm">
                                    <input
                                        type="number"
                                        className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-4 pr-12 sm:text-sm border-gray-300 rounded-md py-2 border"
                                        value={isNaN(settings.transactionFeePercent) ? '' : settings.transactionFeePercent}
                                        onChange={(e) => setSettings({ ...settings, transactionFeePercent: e.target.value === '' ? NaN : parseInt(e.target.value) })}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                                        <span className="text-gray-500 sm:text-sm">%</span>
                                    </div>
                                </div>
                                <p className="mt-1 text-xs text-gray-500">发布任务时额外冻结的积分比例（0 为免费）。</p>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Task and Order Config */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
                    <div className="px-6 py-4 border-b border-gray-100 bg-gray-50 flex items-center">
                        <AlertCircle className="w-5 h-5 text-red-500 mr-2" />
                        <h3 className="font-bold text-gray-800">任务与订单配置</h3>
                    </div>
                    <div className="p-6 space-y-6">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    僵尸任务超时判定时长
                                </label>
                                <div className="relative rounded-md shadow-sm">
                                    <input
                                        type="number"
                                        className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-4 pr-12 sm:text-sm border-gray-300 rounded-md py-2 border"
                                        value={isNaN(settings.zombieTaskTimeoutHours) ? '' : settings.zombieTaskTimeoutHours}
                                        onChange={(e) => setSettings({ ...settings, zombieTaskTimeoutHours: e.target.value === '' ? NaN : parseInt(e.target.value) })}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                                        <span className="text-gray-500 sm:text-sm">小时</span>
                                    </div>
                                </div>
                                <p className="mt-1 text-xs text-gray-500">任务发布后超过最长未接单时间将被系统自动关闭退款。</p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    任务完成自动验收期限
                                </label>
                                <div className="relative rounded-md shadow-sm">
                                    <input
                                        type="number"
                                        className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-4 pr-12 sm:text-sm border-gray-300 rounded-md py-2 border"
                                        value={isNaN(settings.taskAutoAcceptDays) ? '' : settings.taskAutoAcceptDays}
                                        onChange={(e) => setSettings({ ...settings, taskAutoAcceptDays: e.target.value === '' ? NaN : parseInt(e.target.value) })}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                                        <span className="text-gray-500 sm:text-sm">天</span>
                                    </div>
                                </div>
                                <p className="mt-1 text-xs text-gray-500">志愿者完成任务后，若老人不确认验收，系统将在此期限后自动验收发放积分。</p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    订单自动取消时长
                                </label>
                                <div className="relative rounded-md shadow-sm">
                                    <input
                                        type="number"
                                        className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-4 pr-12 sm:text-sm border-gray-300 rounded-md py-2 border"
                                        value={isNaN(settings.orderCancelTimeoutHours) ? '' : settings.orderCancelTimeoutHours}
                                        onChange={(e) => setSettings({ ...settings, orderCancelTimeoutHours: e.target.value === '' ? NaN : parseInt(e.target.value) })}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                                        <span className="text-gray-500 sm:text-sm">小时</span>
                                    </div>
                                </div>
                                <p className="mt-1 text-xs text-gray-500">商品兑换订单生成后迟迟未核销，将被自动取消并退回积分。</p>
                            </div>
                        </div>
                    </div>
                </div>

                {/* Audit & Proxy Config */}
                <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
                    <div className="px-6 py-4 border-b border-gray-100 bg-gray-50 flex items-center">
                        <Sliders className="w-5 h-5 text-indigo-500 mr-2" />
                        <h3 className="font-bold text-gray-800">审核与代理配置</h3>
                    </div>
                    <div className="p-6 space-y-6">
                        <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    亲属绑定数量上限
                                </label>
                                <div className="relative rounded-md shadow-sm">
                                    <input
                                        type="number"
                                        className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-4 pr-12 sm:text-sm border-gray-300 rounded-md py-2 border"
                                        value={isNaN(settings.familyBindingMaxLimit) ? '' : settings.familyBindingMaxLimit}
                                        onChange={(e) => setSettings({ ...settings, familyBindingMaxLimit: e.target.value === '' ? NaN : parseInt(e.target.value) })}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                                        <span className="text-gray-500 sm:text-sm">人</span>
                                    </div>
                                </div>
                                <p className="mt-1 text-xs text-gray-500">每个老人账号允许绑定的子女代理人最高数量限制。</p>
                            </div>

                            <div>
                                <label className="block text-sm font-medium text-gray-700 mb-2">
                                    代理人每日操作次数限制
                                </label>
                                <div className="relative rounded-md shadow-sm">
                                    <input
                                        type="number"
                                        className="focus:ring-blue-500 focus:border-blue-500 block w-full pl-4 pr-12 sm:text-sm border-gray-300 rounded-md py-2 border"
                                        value={isNaN(settings.proxyDailyActionLimit) ? '' : settings.proxyDailyActionLimit}
                                        onChange={(e) => setSettings({ ...settings, proxyDailyActionLimit: e.target.value === '' ? NaN : parseInt(e.target.value) })}
                                    />
                                    <div className="absolute inset-y-0 right-0 pr-3 flex items-center pointer-events-none">
                                        <span className="text-gray-500 sm:text-sm">次</span>
                                    </div>
                                </div>
                                <p className="mt-1 text-xs text-gray-500">为防滥用，子女单日代操作上限设定。</p>
                            </div>
                        </div>
                    </div>
                </div>
            </div>

            <div className="flex justify-end pt-4">
                <button
                    onClick={handleSave}
                    disabled={saving}
                    className={`flex items-center px-6 py-3 border border-transparent text-base font-medium rounded-md shadow-sm text-white bg-blue-600 hover:bg-blue-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-blue-500 ${saving ? 'opacity-70 cursor-not-allowed' : ''}`}
                >
                    {saving ? <Loader2 className="w-5 h-5 mr-2 animate-spin" /> : <Save className="w-5 h-5 mr-2" />}
                    {saving ? '保存中...' : '保存系统配置'}
                </button>
            </div>

            {/* Password Verification Modal */}
            <Modal
                isOpen={isPasswordModalOpen}
                onClose={() => setIsPasswordModalOpen(false)}
                title="管理员身份验证"
                size="sm"
            >
                <div className="text-center">
                    <Key className="w-12 h-12 text-indigo-500 mx-auto mb-4" />
                    <p className="text-lg font-medium text-gray-900 mb-2">保存敏感系统配置</p>
                    <p className="text-sm text-gray-500 mb-6">请输入管理员登录密码以确认此操作。</p>

                    <div className="mb-6 text-left">
                        <input
                            type="password"
                            placeholder="请输入管理员密码"
                            value={adminPassword}
                            onChange={(e) => setAdminPassword(e.target.value)}
                            onKeyDown={(e) => e.key === 'Enter' && confirmSave()}
                            className="w-full px-4 py-2 border border-gray-300 rounded-md focus:ring-blue-500 focus:border-blue-500"
                            autoFocus
                        />
                    </div>

                    <div className="flex gap-3 justify-end">
                        <button
                            onClick={() => setIsPasswordModalOpen(false)}
                            className="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50"
                            disabled={saving}
                        >
                            取消
                        </button>
                        <button
                            onClick={confirmSave}
                            disabled={saving || !adminPassword.trim()}
                            className="flex items-center justify-center px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 disabled:opacity-50"
                        >
                            {saving ? <Loader2 className="w-4 h-4 mr-2 animate-spin" /> : null}
                            确认保存
                        </button>
                    </div>
                </div>
            </Modal>
        </div>
    );
};
