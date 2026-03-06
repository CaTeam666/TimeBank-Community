import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { Input, Button, NavBar } from '../components/UIComponents';
import { authService } from '../services/authService';

export default function Login() {
  const navigate = useNavigate();
  const { dispatch } = useAuth();
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);

    try {
      const user = await authService.login(phone, password);
      dispatch({ type: 'LOGIN', payload: user });

      // Redirect based on role
      if (user.role === 'SENIOR') {
        // Maybe different dashboard in future, for now same
      }
      navigate('/user/profile');
    } catch (error: any) {
      alert(error.message || '登录失败，请检查手机号或密码');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-white">
      <NavBar title="登录" />
      <div className="p-6">
        <div className="mt-8 mb-10 text-center">
          <h2 className="text-2xl font-bold text-gray-800">欢迎回来</h2>
          <p className="text-gray-500 mt-2">时间银行社区</p>
        </div>

        <form onSubmit={handleLogin} className="space-y-6">
          <Input
            label="手机号"
            placeholder="请输入手机号"
            type="tel"
            value={phone}
            onChange={(e) => setPhone(e.target.value)}
          />
          <Input
            label="密码"
            placeholder="请输入密码"
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
          />

          <Button fullWidth size="lg" disabled={!phone || !password || loading}>
            {loading ? '登录中...' : '登录'}
          </Button>
        </form>

        <div className="mt-6 text-center">
          <p className="text-gray-500 text-sm">
            还没有账号？{' '}
            <button onClick={() => navigate('/register')} className="text-orange-500 font-semibold">
              立即注册
            </button>
          </p>
        </div>

        <div className="mt-12 p-4 bg-gray-50 rounded-lg text-xs text-gray-400">
          <p className="font-bold mb-1">演示账号说明：</p>
          <p>志愿者：任意手机号（如 13800000000）/ 任意密码</p>
          <p>长者：手机号包含 '60' / 任意密码</p>
        </div>
      </div>
    </div>
  );
}