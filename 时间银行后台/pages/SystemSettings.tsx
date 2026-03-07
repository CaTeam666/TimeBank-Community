import React, { useState, useEffect } from 'react';
import { systemApi, SystemSettings } from '../services/systemApi';
import { Save, Sliders, Gift, Award, AlertCircle, Loader2 } from 'lucide-react';

export const SystemSettingsPage: React.FC = () => {
    const [settings, setSettings] = useState<SystemSettings>({
        elderInitialCoins: 0,
        dailySignInReward: 0,
        monthlyRank1Reward: 0,
        transactionFeePercent: 0
    });
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);

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

    const handleSave = async () => {
        try {
            setSaving(true);
            await systemApi.updateSettings(settings);
            alert('系统配置已保存生效！');
        } catch (error) {
            alert('保存失败，请重试');
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
        </div>
    );
};
