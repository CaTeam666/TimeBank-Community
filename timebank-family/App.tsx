import React from 'react';
import { HashRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import { TaskProvider } from './context/TaskContext';
import { MallProvider } from './context/MallContext';
import { MessageProvider } from './context/MessageContext';
import { Layout } from './components/Layout';
import { RewardPopup } from './components/RewardPopup';

import Login from './pages/Login';
import Register from './pages/Register';
import Profile from './pages/Profile';
import Settings from './pages/Settings';
import Family from './pages/Family';
import TaskHall from './pages/TaskHall';
import TaskDetail from './pages/TaskDetail';
import TaskPublish from './pages/TaskPublish';
import TaskOrders from './pages/TaskOrders';
import TaskExecute from './pages/TaskExecute';
import TaskReview from './pages/TaskReview';
import MallHome from './pages/MallHome';
import MallDetail from './pages/MallDetail';
import MallOrders from './pages/MallOrders';
import Leaderboard from './pages/Leaderboard';
import MessageList from './pages/MessageList';
import FamilyBindingReview from './pages/FamilyBindingReview';


const App = () => {
  return (
    <AuthProvider>
      <MessageProvider>
        <TaskProvider>
          <MallProvider>
            <HashRouter>
              <Layout>
                <RewardPopup />
                <Routes>
                  <Route path="/login" element={<Login />} />
                  <Route path="/register" element={<Register />} />

                  {/* Module 1: User & Family */}
                  <Route path="/user/profile" element={<Profile />} />
                  <Route path="/settings" element={<Settings />} />
                  <Route path="/user/family" element={<Family />} />
                  <Route path="/family/binding-review" element={<FamilyBindingReview />} />

                  {/* Module 2: Task Market */}
                  <Route path="/task/hall" element={<TaskHall />} />
                  <Route path="/task/detail/:id" element={<TaskDetail />} />
                  <Route path="/task/publish" element={<TaskPublish />} />

                  {/* Module 3: Execution & Verification */}
                  <Route path="/task/orders" element={<TaskOrders />} />
                  <Route path="/task/execute/:id" element={<TaskExecute />} />
                  <Route path="/task/review/:id" element={<TaskReview />} />

                  {/* Module 4: Mall & Redemption */}
                  <Route path="/mall/home" element={<MallHome />} />
                  <Route path="/mall/detail/:id" element={<MallDetail />} />
                  <Route path="/mall/orders" element={<MallOrders />} />

                  {/* Module 5: Incentive & Message */}
                  <Route path="/incentive/rank" element={<Leaderboard />} />
                  <Route path="/message/list" element={<MessageList />} />

                  {/* Default redirect to Login */}
                  <Route path="*" element={<Navigate to="/login" replace />} />
                </Routes>
              </Layout>
            </HashRouter>
          </MallProvider>
        </TaskProvider>
      </MessageProvider>
    </AuthProvider>
  );
};

export default App;
