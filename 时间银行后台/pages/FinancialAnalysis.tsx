import React from 'react';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts';
import { MOCK_DASHBOARD_DATA, MOCK_TRANSACTIONS } from '../services/mockData';
import { Badge } from '../components/ui/Badge';
import { AlertTriangle, TrendingUp } from 'lucide-react';

export const FinancialAnalysis: React.FC = () => {
  const highValueTransactions = MOCK_TRANSACTIONS.filter(t => t.amount > 200);

  return (
    <div className="space-y-6">
      <div className="bg-white p-4 rounded-lg shadow-sm border border-l-4 border-l-orange-500 border-gray-100 flex items-center justify-between">
        <div className="flex items-center">
          <TrendingUp className="w-6 h-6 text-orange-500 mr-3" />
          <div>
            <h3 className="text-lg font-bold text-gray-900">资金健康监控</h3>
            <p className="text-sm text-gray-500">实时监测积分的产出与消耗比例，防止通货膨胀。</p>
          </div>
        </div>
        <div className="text-right">
          <p className="text-sm text-gray-500">本周净产出</p>
          <p className="text-2xl font-bold text-green-600">+1,250 币</p>
        </div>
      </div>

      {/* Area Chart */}
      <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
        <h3 className="text-lg font-bold text-gray-800 mb-6">资金流量趋势 (产出 vs 消耗)</h3>
        <div className="h-96">
          <ResponsiveContainer width="100%" height="100%">
            <AreaChart
              data={MOCK_DASHBOARD_DATA.financialFlow}
              margin={{ top: 10, right: 30, left: 0, bottom: 0 }}
            >
              <defs>
                <linearGradient id="colorEarned" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#10b981" stopOpacity={0.8} />
                  <stop offset="95%" stopColor="#10b981" stopOpacity={0} />
                </linearGradient>
                <linearGradient id="colorConsumed" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor="#f59e0b" stopOpacity={0.8} />
                  <stop offset="95%" stopColor="#f59e0b" stopOpacity={0} />
                </linearGradient>
              </defs>
              <XAxis dataKey="name" />
              <YAxis />
              <CartesianGrid strokeDasharray="3 3" vertical={false} />
              <Tooltip />
              <Area type="monotone" dataKey="earned" name="发放/赚取" stroke="#10b981" fillOpacity={1} fill="url(#colorEarned)" />
              <Area type="monotone" dataKey="consumed" name="消费/兑换" stroke="#f59e0b" fillOpacity={1} fill="url(#colorConsumed)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* High Value Transactions */}
      <div className="bg-white rounded-lg shadow-sm border border-gray-100 overflow-hidden">
        <div className="px-6 py-4 border-b border-gray-100 bg-red-50 flex items-center">
          <AlertTriangle className="w-5 h-5 text-red-500 mr-2" />
          <h3 className="font-bold text-red-800">大额异常交易监控 ({'>'} 200 币)</h3>
        </div>
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">流水号</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">用户</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">类型</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">金额</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">备注</th>
              <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">时间</th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {highValueTransactions.map((tx) => (
              <tr key={tx.id} className="hover:bg-gray-50">
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500 font-mono">
                  {tx.id}
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  <div className="text-sm font-medium text-gray-900">{tx.userName}</div>
                  <div className="text-xs text-gray-500">ID: {tx.userId}</div>
                </td>
                <td className="px-6 py-4 whitespace-nowrap">
                  {tx.type === 'EARN' ? (
                    <Badge color="green">赚取</Badge>
                  ) : (
                    <Badge color="orange">消费</Badge>
                  )}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm font-bold text-red-600">
                  {tx.amount} 币
                </td>
                <td className="px-6 py-4 text-sm text-gray-600 max-w-xs truncate">
                  {tx.note}
                </td>
                <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-500">
                  {tx.timestamp}
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
