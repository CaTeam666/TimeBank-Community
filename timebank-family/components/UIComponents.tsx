import React from 'react';
import { useAuth } from '../context/AuthContext';
import { useNavigate, useLocation } from 'react-router-dom';
import { Home, Users, PlusCircle, Settings, ChevronLeft, User as UserIcon, ClipboardList } from 'lucide-react';
import { THEME_COLOR_BG, THEME_COLOR_TEXT } from '../constants';

// --- Button ---
interface ButtonProps extends React.ButtonHTMLAttributes<HTMLButtonElement> {
  variant?: 'primary' | 'secondary' | 'outline' | 'danger';
  size?: 'sm' | 'md' | 'lg';
  fullWidth?: boolean;
}

export const Button: React.FC<ButtonProps> = ({
  children,
  variant = 'primary',
  size = 'md',
  fullWidth = false,
  className = '',
  ...props
}) => {
  const baseStyle = "rounded-lg font-medium transition-colors focus:outline-none flex items-center justify-center";

  const variants = {
    primary: `${THEME_COLOR_BG} text-white hover:opacity-90 active:scale-95 transition-transform`,
    secondary: "bg-gray-100 text-gray-800 hover:bg-gray-200",
    outline: `border border-current ${THEME_COLOR_TEXT} bg-transparent hover:bg-orange-50`,
    danger: "bg-red-500 text-white hover:bg-red-600"
  };

  const sizes = {
    sm: "px-3 py-1.5 text-sm",
    md: "px-4 py-2 text-base",
    lg: "px-6 py-3 text-lg"
  };

  return (
    <button
      className={`${baseStyle} ${variants[variant]} ${sizes[size]} ${fullWidth ? 'w-full' : ''} ${className} disabled:opacity-50 disabled:grayscale`}
      {...props}
    >
      {children}
    </button>
  );
};

// --- Input ---
interface InputProps extends React.InputHTMLAttributes<HTMLInputElement> {
  label?: string;
  error?: string;
}

export const Input: React.FC<InputProps> = ({ label, error, className = '', ...props }) => {
  return (
    <div className="mb-4">
      {label && <label className="block text-sm font-medium text-gray-700 mb-1">{label}</label>}
      <input
        className={`w-full px-4 py-2 rounded-lg border bg-white focus:ring-2 focus:ring-orange-500 focus:border-transparent outline-none transition-shadow ${error ? 'border-red-500' : 'border-gray-200'} ${className}`}
        {...props}
      />
      {error && <p className="mt-1 text-xs text-red-500">{error}</p>}
    </div>
  );
};

// --- Navbar ---
interface NavBarProps {
  title: string;
  showBack?: boolean;
  rightAction?: React.ReactNode;
}

export const NavBar: React.FC<NavBarProps> = ({ title, showBack = false, rightAction }) => {
  const navigate = useNavigate();
  return (
    <div className="h-12 px-4 flex items-center justify-between bg-white border-b border-gray-100 sticky top-0 z-50">
      <div className="w-10 flex items-center">
        {showBack && (
          <button onClick={() => navigate(-1)} className="p-1 -ml-2 text-gray-600 active:opacity-50">
            <ChevronLeft size={24} />
          </button>
        )}
      </div>
      <h1 className="text-lg font-semibold text-gray-800">{title}</h1>
      <div className="w-10 flex justify-end">
        {rightAction}
      </div>
    </div>
  );
};

// --- TabBar ---
export const TabBar: React.FC = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const { state } = useAuth();

  // Hide on auth pages
  if (['/login', '/register'].includes(location.pathname)) return null;

  const isActive = (path: string) => location.pathname === path;

  return (
    <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-100 pb-safe-area z-40">
      <div className="flex justify-around items-center h-16 max-w-md mx-auto">
        <button
          onClick={() => navigate('/task/hall')}
          className={`flex flex-col items-center justify-center w-full h-full ${isActive('/task/hall') ? THEME_COLOR_TEXT : 'text-gray-400'}`}
        >
          <Home size={24} />
          <span className="text-[10px] mt-1">任务大厅</span>
        </button>

        <div className="relative -top-5">
          <button
            onClick={() => navigate('/task/publish')}
            className={`flex items-center justify-center w-14 h-14 rounded-full ${THEME_COLOR_BG} text-white shadow-lg shadow-orange-200 active:scale-95 transition-transform`}
          >
            <PlusCircle size={32} />
          </button>
        </div>

        <button
          onClick={() => navigate('/user/profile')}
          className={`flex flex-col items-center justify-center w-full h-full ${isActive('/user/profile') ? THEME_COLOR_TEXT : 'text-gray-400'}`}
        >
          <UserIcon size={24} />
          <span className="text-[10px] mt-1">我的</span>
        </button>
      </div>

      {/* Proxy Mode Indicator */}
      {state.isProxyMode && state.proxyTarget && (
        <div className="absolute bottom-16 left-0 right-0 bg-orange-100 text-orange-800 text-xs py-1 text-center border-t border-orange-200">
          当前正在代理: <strong>{state.proxyTarget.nickname}</strong>
        </div>
      )}
    </div>
  );
};

// --- Card ---
export const Card: React.FC<{ children: React.ReactNode, className?: string, onClick?: () => void }> = ({ children, className = '', onClick }) => (
  <div onClick={onClick} className={`bg-white rounded-xl shadow-sm p-4 ${className}`}>
    {children}
  </div>
);

// --- Modal/Dialog ---
interface ModalProps {
  isOpen: boolean;
  onClose: () => void;
  title?: string;
  children: React.ReactNode;
}

export const Modal: React.FC<ModalProps> = ({ isOpen, onClose, title, children }) => {
  if (!isOpen) return null;
  return (
    <div className="fixed inset-0 z-[60] flex items-center justify-center p-4 bg-black/50">
      <div className="bg-white rounded-2xl w-full max-w-sm overflow-hidden animate-fade-in">
        {title && <div className="p-4 border-b border-gray-100 font-bold text-center">{title}</div>}
        <div className="p-4">
          {children}
        </div>
        <div className="p-4 bg-gray-50 flex justify-end">
          <Button variant="secondary" onClick={onClose} size="sm">关闭</Button>
        </div>
      </div>
    </div>
  );
};

// --- Toast ---
interface ToastProps {
  message: string;
  type?: 'success' | 'error' | 'info';
  isVisible: boolean;
  onClose: () => void;
}

export const Toast: React.FC<ToastProps> = ({ message, type = 'info', isVisible, onClose }) => {
  React.useEffect(() => {
    if (isVisible) {
      const timer = setTimeout(onClose, 2000);
      return () => clearTimeout(timer);
    }
  }, [isVisible, onClose]);

  if (!isVisible) return null;

  const bgColors = {
    success: 'bg-green-500',
    error: 'bg-red-500',
    info: 'bg-gray-800'
  };

  return (
    <div className="fixed top-1/2 left-1/2 transform -translate-x-1/2 -translate-y-1/2 z-[70] animate-fade-in">
      <div className={`${bgColors[type]} text-white px-6 py-3 rounded-lg shadow-lg flex items-center gap-2 min-w-[200px] justify-center`}>
        <span>{message}</span>
      </div>
    </div>
  );
};