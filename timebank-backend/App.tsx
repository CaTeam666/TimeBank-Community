import React from 'react';
import { HashRouter, Routes, Route, Navigate } from 'react-router-dom';
import { Layout } from './components/Layout';
import { UserList } from './pages/UserList';
import { IdentityAudit } from './pages/IdentityAudit';
import { FamilyBinding } from './pages/FamilyBinding';
import { TaskMonitor } from './pages/TaskMonitor';
import { TaskDetail } from './pages/TaskDetail';
import { ZombieTaskLogs } from './pages/ZombieTaskLogs';
import { ArbitrationList } from './pages/ArbitrationList';
import { AdjudicationWorkbench } from './pages/AdjudicationWorkbench';
import { EvidenceArchive } from './pages/EvidenceArchive';
import { ProductManagement } from './pages/ProductManagement';
import { OrderManagement } from './pages/OrderManagement';
import { RankingLogPage } from './pages/RankingLog';
import { Dashboard } from './pages/Dashboard';
import { FinancialAnalysis } from './pages/FinancialAnalysis';
import { SystemSettingsPage } from './pages/SystemSettings';
import { LoginPage } from './pages/Login';
import { RequireAuth } from './components/RequireAuth';

const App: React.FC = () => {
  return (
    <HashRouter>
      <Routes>
        {/* 未登录先进入登录页 */}
        <Route path="/login" element={<LoginPage />} />

        {/* 业务路由：需要登录 */}
        <Route
          path="/"
          element={
            <RequireAuth>
              <Layout />
            </RequireAuth>
          }
        >
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="dashboard" element={<Dashboard />} />

          <Route path="user/list" element={<UserList />} />
          <Route path="user/audit/identity" element={<IdentityAudit />} />
          <Route path="user/audit/binding" element={<FamilyBinding />} />

          <Route path="task/monitor" element={<TaskMonitor />} />
          <Route path="task/detail/:id" element={<TaskDetail />} />
          <Route path="task/zombie" element={<ZombieTaskLogs />} />

          <Route path="service/arbitration" element={<ArbitrationList />} />
          <Route path="service/arbitration/handle/:id" element={<AdjudicationWorkbench />} />
          <Route path="service/evidence" element={<EvidenceArchive />} />

          <Route path="mall/product" element={<ProductManagement />} />
          <Route path="mall/order" element={<OrderManagement />} />

          <Route path="incentive/ranking-log" element={<RankingLogPage />} />
          <Route path="statistics/finance" element={<FinancialAnalysis />} />
          <Route path="system/settings" element={<SystemSettingsPage />} />
        </Route>
      </Routes>
    </HashRouter>
  );
};

export default App;
