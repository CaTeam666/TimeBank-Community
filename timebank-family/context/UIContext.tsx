import React, { createContext, useContext, useState, ReactNode, useCallback } from 'react';
import { Toast, ConfirmModal } from '../components/UIComponents';

interface ToastState {
  message: string;
  type: 'success' | 'error' | 'info';
  isOpen: boolean;
}

interface ConfirmState {
  title: string;
  content: string;
  isOpen: boolean;
  onConfirm: () => void;
  confirmText?: string;
  cancelText?: string;
  type?: 'default' | 'danger';
}

interface UIContextType {
  showToast: (message: string, type?: 'success' | 'error' | 'info') => void;
  showConfirm: (options: {
    title: string;
    content: string;
    onConfirm: () => void;
    confirmText?: string;
    cancelText?: string;
    type?: 'default' | 'danger';
  }) => void;
}

const UIContext = createContext<UIContextType | undefined>(undefined);

export const UIProvider: React.FC<{ children: ReactNode }> = ({ children }) => {
  const [toast, setToast] = useState<ToastState>({
    message: '',
    type: 'info',
    isOpen: false,
  });

  const [confirm, setConfirm] = useState<ConfirmState>({
    title: '',
    content: '',
    isOpen: false,
    onConfirm: () => {},
  });

  const showToast = useCallback((message: string, type: 'success' | 'error' | 'info' = 'info') => {
    setToast({ message, type, isOpen: true });
  }, []);

  const hideToast = useCallback(() => {
    setToast(prev => ({ ...prev, isOpen: false }));
  }, []);

  const showConfirm = useCallback((options: {
    title: string;
    content: string;
    onConfirm: () => void;
    confirmText?: string;
    cancelText?: string;
    type?: 'default' | 'danger';
  }) => {
    setConfirm({ ...options, isOpen: true });
  }, []);

  const hideConfirm = useCallback(() => {
    setConfirm(prev => ({ ...prev, isOpen: false }));
  }, []);

  return (
    <UIContext.Provider value={{ showToast, showConfirm }}>
      {children}
      
      {/* Global Toast */}
      <Toast 
        message={toast.message} 
        type={toast.type} 
        isVisible={toast.isOpen} 
        onClose={hideToast} 
      />

      {/* Global Confirm Modal */}
      <ConfirmModal 
        isOpen={confirm.isOpen}
        title={confirm.title}
        content={confirm.content}
        confirmText={confirm.confirmText}
        cancelText={confirm.cancelText}
        type={confirm.type}
        onClose={hideConfirm}
        onConfirm={confirm.onConfirm}
      />
    </UIContext.Provider>
  );
};

export const useUI = () => {
  const context = useContext(UIContext);
  if (!context) {
    throw new Error('useUI must be used within a UIProvider');
  }
  return context;
};
