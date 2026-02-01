import React from 'react';
import { 
  Users, Clock, ShoppingCart, Coins, 
  ArrowUp, ArrowDown, Activity, Bell 
} from 'lucide-react';
import { 
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, 
  PieChart, Pie, Cell, Legend, BarChart, Bar 
} from 'recharts';
import { MOCK_DASHBOARD_DATA } from '../services/mockData';

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042'];

const KPICard = ({ title, value, subValue, isUp, icon: Icon, color }: any) => (
  <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100">
    <div className="flex items-center justify-between mb-4">
      <div className={`p-3 rounded-full ${color}`}>
        <Icon className="w-6 h-6 text-white" />
      </div>
      <div className={`flex items-center text-sm font-medium ${isUp ? 'text-green-600' : 'text-red-600'}`}>
        {isUp ? <ArrowUp className="w-4 h-4 mr-1" /> : <ArrowDown className="w-4 h-4 mr-1" />}
        {subValue}
      </div>
    </div>
    <div className="text-gray-500 text-sm font-medium">{title}</div>
    <div className="text-2xl font-bold text-gray-900 mt-1">{value}</div>
  </div>
);

export const Dashboard: React.FC = () => {
  return (
    <div className="space-y-6">
      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <KPICard 
          title="社区总人口" 
          value="3,580" 
          subValue="1.2%" 
          isUp={true} 
          icon={Users} 
          color="bg-blue-500" 
        />
        <KPICard 
          title="累计服务时长 (小时)" 
          value="12,450" 
          subValue="5.4%" 
          isUp={true} 
          icon={Clock} 
          color="bg-green-500" 
        />
        <KPICard 
          title="今日新增订单" 
          value="45" 
          subValue="0.8%" 
          isUp={false} 
          icon={ShoppingCart} 
          color="bg-purple-500" 
        />
        <KPICard 
          title="资金池总流通 (币)" 
          value="580,000" 
          subValue="12.5%" 
          isUp={true} 
          icon={Coins} 
          color="bg-orange-500" 
        />
      </div>

      {/* Charts Row 1 */}
      <div className="grid grid-cols-1 lg:grid-cols-5 gap-6">
        {/* Line Chart */}
        <div className="lg:col-span-3 bg-white p-6 rounded-lg shadow-sm border border-gray-100">
          <h3 className="text-lg font-bold text-gray-800 mb-6 flex items-center">
            <Activity className="w-5 h-5 mr-2 text-blue-500" />
            近7日服务活跃度趋势
          </h3>
          <div className="h-80">
            <ResponsiveContainer width="100%" height="100%">
              <LineChart data={MOCK_DASHBOARD_DATA.serviceActivity}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#e5e7eb" />
                <XAxis dataKey="name" axisLine={false} tickLine={false} dy={10} />
                <YAxis axisLine={false} tickLine={false} />
                <Tooltip 
                  contentStyle={{ backgroundColor: '#fff', borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }} 
                />
                <Line 
                  type="monotone" 
                  dataKey="value" 
                  stroke="#3b82f6" 
                  strokeWidth={3} 
                  dot={{ r: 4, fill: '#3b82f6', strokeWidth: 2, stroke: '#fff' }} 
                  activeDot={{ r: 6 }} 
                />
              </LineChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Pie Chart */}
        <div className="lg:col-span-2 bg-white p-6 rounded-lg shadow-sm border border-gray-100">
           <h3 className="text-lg font-bold text-gray-800 mb-6">任务类型分布</h3>
           <div className="h-80 flex justify-center items-center">
             <ResponsiveContainer width="100%" height="100%">
                <PieChart>
                  <Pie
                    data={MOCK_DASHBOARD_DATA.taskDistribution}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    fill="#8884d8"
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {MOCK_DASHBOARD_DATA.taskDistribution.map((entry, index) => (
                      <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                    ))}
                  </Pie>
                  <Tooltip />
                  <Legend verticalAlign="bottom" height={36}/>
                </PieChart>
             </ResponsiveContainer>
           </div>
        </div>
      </div>

      {/* Charts Row 2 */}
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Real-time Feeds */}
        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 flex flex-col h-96">
            <h3 className="text-lg font-bold text-gray-800 mb-4 flex items-center">
              <Bell className="w-5 h-5 mr-2 text-yellow-500" />
              实时动态
            </h3>
            <div className="flex-1 overflow-hidden relative">
               <div className="absolute inset-x-0 top-0 h-10 bg-gradient-to-b from-white to-transparent z-10 pointer-events-none"></div>
               <div className="absolute inset-x-0 bottom-0 h-10 bg-gradient-to-t from-white to-transparent z-10 pointer-events-none"></div>
               <div className="animate-marquee space-y-4 py-4">
                  {/* Mock scrolling items repeated for effect */}
                  {[1, 2, 3, 4, 5, 6, 7, 8].map((i) => (
                      <div key={i} className="flex items-center space-x-3 text-sm border-b border-gray-50 pb-3">
                          <span className="text-gray-400 font-mono text-xs">[10:0{i}]</span>
                          <span className="flex-1">
                              <span className="font-medium text-gray-900">张阿姨</span> 发布了 <span className="text-blue-600">陪聊需求</span>
                          </span>
                      </div>
                  ))}
               </div>
            </div>
        </div>

        {/* Top Volunteers */}
        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-100 h-96">
            <h3 className="text-lg font-bold text-gray-800 mb-2">本月志愿者荣誉榜 Top 5</h3>
            <div className="h-full pb-6">
              <ResponsiveContainer width="100%" height="90%">
                <BarChart
                  layout="vertical"
                  data={MOCK_DASHBOARD_DATA.topVolunteers}
                  margin={{ top: 20, right: 30, left: 20, bottom: 5 }}
                >
                  <CartesianGrid strokeDasharray="3 3" horizontal={false} />
                  <XAxis type="number" hide />
                  <YAxis dataKey="name" type="category" width={50} tick={{fontSize: 12}} />
                  <Tooltip cursor={{fill: 'transparent'}} />
                  <Bar dataKey="hours" fill="#10b981" radius={[0, 4, 4, 0]} barSize={20} label={{ position: 'right', fill: '#666', fontSize: 12 }} />
                </BarChart>
              </ResponsiveContainer>
            </div>
        </div>
      </div>
    </div>
  );
};
