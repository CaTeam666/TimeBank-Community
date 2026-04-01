import React from 'react';
import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { Users, FileCheck, ShieldCheck, LayoutDashboard, Menu, Activity, AlertTriangle, Gavel, Image, ShoppingBag, Trophy, ClipboardList, PieChart, Settings } from 'lucide-react';

const SidebarItem = ({ to, icon: Icon, label }: { to: string; icon: any; label: string }) => {
  return (
    <NavLink
      to={to}
      className={({ isActive }) =>
        `flex items-center px-6 py-3 text-sm font-medium transition-colors ${
          isActive
            ? 'bg-blue-50 text-blue-600 border-r-4 border-blue-600'
            : 'text-gray-600 hover:bg-gray-50 hover:text-gray-900'
        }`
      }
    >
      <Icon className="w-5 h-5 mr-3" />
      {label}
    </NavLink>
  );
};

export const Layout: React.FC = () => {
  const location = useLocation();
  const getPageTitle = (pathname: string) => {
      if (pathname === '/user/list') return '用户档案管理';
      if (pathname === '/user/audit/identity') return '实名认证审核';
      if (pathname === '/user/audit/binding') return '亲情账号绑定审核';
      if (pathname === '/task/monitor') return '全域任务监控中心';
      if (pathname.startsWith('/task/detail/')) return '任务详情概览';
      if (pathname === '/task/zombie') return '过期与异常任务日志';
      if (pathname === '/service/arbitration') return '纠纷仲裁中心';
      if (pathname.startsWith('/service/arbitration/handle/')) return '裁决详情工作台';
      if (pathname === '/service/evidence') return '历史服务存证档案';
      if (pathname === '/mall/product') return '爱心商品管理';
      if (pathname === '/mall/order') return '兑换订单核销';
      if (pathname === '/incentive/ranking-log') return '排行榜奖励监控';
      if (pathname === '/statistics/finance') return '资金风控分析';
      if (pathname === '/system/settings') return '系统参数设置';
      return '数据驾驶舱';
  };

  return (
    <div className="flex h-screen bg-gray-100">
      {/* Sidebar */}
      <div className="w-64 bg-white shadow-lg flex flex-col z-10">
        <div className="h-16 flex items-center px-6 border-b border-gray-100">
          <LayoutDashboard className="w-8 h-8 text-blue-600 mr-2" />
          <span className="text-xl font-bold text-gray-800">时间银行</span>
        </div>
        
        <div className="flex-1 py-6 space-y-1 overflow-y-auto">
          <SidebarItem to="/dashboard" icon={LayoutDashboard} label="数据驾驶舱" />

          <div className="px-6 mt-6 mb-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
            用户中心
          </div>
          <SidebarItem to="/user/list" icon={Users} label="用户列表" />
          
          <div className="px-6 mt-6 mb-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
            审核中心
          </div>
          <SidebarItem to="/user/audit/identity" icon={FileCheck} label="实名审核" />
          <SidebarItem to="/user/audit/binding" icon={ShieldCheck} label="亲情绑定" />

          <div className="px-6 mt-6 mb-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
            任务管理
          </div>
          <SidebarItem to="/task/monitor" icon={Activity} label="任务监控" />
          <SidebarItem to="/task/zombie" icon={AlertTriangle} label="异常日志" />

          <div className="px-6 mt-6 mb-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
            服务监督
          </div>
          <SidebarItem to="/service/arbitration" icon={Gavel} label="纠纷仲裁" />
          <SidebarItem to="/service/evidence" icon={Image} label="服务存证" />

          <div className="px-6 mt-6 mb-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
            爱心商城
          </div>
          <SidebarItem to="/mall/product" icon={ShoppingBag} label="商品管理" />
          <SidebarItem to="/mall/order" icon={ClipboardList} label="订单核销" />

          <div className="px-6 mt-6 mb-2 text-xs font-semibold text-gray-400 uppercase tracking-wider">
            统计与设置
          </div>
          <SidebarItem to="/incentive/ranking-log" icon={Trophy} label="奖励监控" />
          <SidebarItem to="/statistics/finance" icon={PieChart} label="资金分析" />
          <SidebarItem to="/system/settings" icon={Settings} label="系统设置" />
        </div>
        
        <div className="p-4 border-t border-gray-100">
          <div className="flex items-center">
            <img src="https://picsum.photos/id/64/40/40" alt="Admin" className="w-8 h-8 rounded-full" />
            <div className="ml-3">
              <p className="text-sm font-medium text-gray-700">管理员</p>
              <p className="text-xs text-gray-500">系统管理员</p>
            </div>
          </div>
        </div>
      </div>

      {/* Main Content */}
      <div className="flex-1 flex flex-col overflow-hidden">
        <header className="h-16 bg-white border-b border-gray-200 flex items-center justify-between px-8">
          <h1 className="text-2xl font-bold text-gray-800">{getPageTitle(location.pathname)}</h1>
          <button className="p-2 rounded-full hover:bg-gray-100 text-gray-600">
            <Menu className="w-6 h-6" />
          </button>
        </header>
        
        <main className="flex-1 overflow-y-auto p-8">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
