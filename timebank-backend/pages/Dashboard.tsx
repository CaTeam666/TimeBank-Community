import React, { useState, useEffect } from 'react';
import { 
  Users, Clock, ShoppingCart, Coins, 
  ArrowUp, ArrowDown, Activity, Bell, Gift
} from 'lucide-react';
import { 
  LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, 
  PieChart, Pie, Cell, Legend, BarChart, Bar 
} from 'recharts';
import { rankingApi } from '../services/rankingApi';
import { dashboardApi, KpiData, DynamicsItem } from '../services/dashboardApi';

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
  const [topVolunteers, setTopVolunteers] = useState<{name: string, hours: number}[]>([]);
  const [kpiData, setKpiData] = useState<KpiData>({
    totalPopulation: 0,
    todayExchangeCount: 0,
    todayNewOrders: 0,
    todayPointsCirculation: 0
  });
  const [trendData, setTrendData] = useState<{name: string, value: number}[]>([]);
  const [distributionData, setDistributionData] = useState<{name: string, value: number}[]>([]);
  const [dynamicsData, setDynamicsData] = useState<DynamicsItem[]>([]);

  useEffect(() => {
    const fetchTopRanking = async () => {
      try {
        const data = await rankingApi.getTopRanking();
        if (data && data.list) {
          const mappedData = data.list.map(item => ({
            name: item.volunteerName,
            hours: item.orderCount
          }));
          setTopVolunteers(mappedData);
        }
      } catch (error) {
        console.error('获取实时排行榜失败:', error);
      }
    };
    
    const fetchDashboardData = async () => {
      try {
        const [kpiRes, trendRes, distRes, dynamicsRes] = await Promise.allSettled([
          dashboardApi.getKPIs(),
          dashboardApi.getActivityTrend(),
          dashboardApi.getTaskDistribution(),
          dashboardApi.getDynamics(20)
        ]);

        if (kpiRes.status === 'fulfilled' && kpiRes.value) setKpiData(kpiRes.value);
        if (trendRes.status === 'fulfilled' && trendRes.value) {
          setTrendData(trendRes.value.map(item => ({
            name: item.date.slice(5),
            value: item.count
          })));
        }
        if (distRes.status === 'fulfilled' && distRes.value) {
          setDistributionData(distRes.value.map(item => ({
            name: item.typeName,
            value: item.count
          })));
        }
        if (dynamicsRes.status === 'fulfilled' && dynamicsRes.value) {
          setDynamicsData(dynamicsRes.value);
        }
      } catch (error) {
        console.error('获取看板数据失败:', error);
      }
    };

    fetchTopRanking();
    fetchDashboardData();
  }, []);

  return (
    <div className="space-y-6">
      {/* KPI Cards */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <KPICard 
          title="社区总人口" 
          value={kpiData.totalPopulation.toLocaleString()} 
          subValue="-" 
          isUp={true} 
          icon={Users} 
          color="bg-blue-500" 
        />
        <KPICard 
          title="爱心超市兑换数" 
          value={kpiData.todayExchangeCount.toLocaleString()} 
          subValue="-" 
          isUp={true} 
          icon={Gift} 
          color="bg-green-500" 
        />
        <KPICard 
          title="今日新增订单" 
          value={kpiData.todayNewOrders.toLocaleString()} 
          subValue="-" 
          isUp={true} 
          icon={ShoppingCart} 
          color="bg-purple-500" 
        />
        <KPICard 
          title="资金池总流通 (币)" 
          value={kpiData.todayPointsCirculation.toLocaleString()} 
          subValue="-" 
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
              <LineChart data={trendData}>
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
                    data={distributionData}
                    cx="50%"
                    cy="50%"
                    innerRadius={60}
                    outerRadius={100}
                    fill="#8884d8"
                    paddingAngle={5}
                    dataKey="value"
                  >
                    {distributionData.map((entry, index) => (
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
                  {dynamicsData.length > 0 ? (
                    dynamicsData.map((item, idx) => (
                      <div key={item.id + '-' + idx} className="flex items-center space-x-3 text-sm border-b border-gray-50 pb-3">
                          <span className="text-gray-400 font-mono text-xs">[{item.createTime.slice(11, 16)}]</span>
                          <span className="flex-1">
                              <span className="font-medium text-gray-900">{item.userName}</span> {item.content}
                          </span>
                      </div>
                    ))
                  ) : (
                    <div className="text-gray-400 text-sm text-center">暂无动态</div>
                  )}
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
                  data={topVolunteers}
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
