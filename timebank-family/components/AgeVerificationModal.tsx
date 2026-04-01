import React from 'react';
import { Button } from './UIComponents';
import { ShieldAlert, ArrowRight, X } from 'lucide-react';

interface AgeVerificationModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void; // 切换为志愿者
  age: number;
}

export const AgeVerificationModal: React.FC<AgeVerificationModalProps> = ({ 
  isOpen, 
  onClose, 
  onConfirm, 
  age 
}) => {
  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-[100] flex items-end sm:items-center justify-center p-0 sm:p-4 bg-black/60 backdrop-blur-sm transition-opacity duration-300">
      <div 
        className="bg-white w-full max-w-md rounded-t-3xl sm:rounded-2xl overflow-hidden shadow-2xl transform transition-transform duration-500 ease-out translate-y-0 animate-slide-up"
        onClick={(e) => e.stopPropagation()}
      >
        {/* Header Illustration Area */}
        <div className="relative h-32 bg-gradient-to-br from-orange-400 to-orange-500 flex items-center justify-center overflow-hidden">
          <div className="absolute inset-0 opacity-10">
            <div className="absolute -top-10 -left-10 w-40 h-40 rounded-full bg-white"></div>
            <div className="absolute -bottom-10 -right-10 w-32 h-32 rounded-full bg-white"></div>
          </div>
          
          <div className="relative z-10 w-16 h-16 bg-white/20 backdrop-blur-md rounded-2xl flex items-center justify-center border border-white/30 shadow-lg">
            <ShieldAlert className="text-white" size={32} strokeWidth={2.5} />
          </div>
          
          <button 
            onClick={onClose}
            className="absolute top-4 right-4 p-2 text-white/80 hover:text-white hover:bg-white/10 rounded-full transition-colors"
          >
            <X size={20} />
          </button>
        </div>

        {/* Content */}
        <div className="p-8 text-center bg-white">
          <h3 className="text-2xl font-bold text-gray-800 mb-2">温馨提示</h3>
          <div className="flex justify-center mb-4">
            <div className="h-1 w-12 bg-orange-500 rounded-full"></div>
          </div>
          
          <div className="space-y-4 text-gray-600 leading-relaxed">
            <p>
              系统检测到您目前的年龄为 <span className="text-orange-600 font-bold text-lg">{age}</span> 岁。
            </p>
            <p className="text-sm">
              为了让您更早地通过志愿服务积累“积分”，为未来的品质生活打下基础，我们建议您注册为 <span className="font-semibold text-green-600">“志愿者”</span> 身份。
            </p>
            <p className="text-xs bg-gray-50 p-3 rounded-lg border border-gray-100 italic">
              “现在服务他人，未来享受回馈——这就是时间银行的魅力所在。”
            </p>
          </div>

          {/* Actions */}
          <div className="mt-8 space-y-3">
            <Button 
              fullWidth 
              size="lg" 
              onClick={onConfirm}
              className="group shadow-md hover:shadow-orange-200"
            >
              <span>切换为志愿者</span>
              <ArrowRight size={18} className="ml-2 group-hover:translate-x-1 transition-transform" />
            </Button>
            
            <button 
              onClick={onClose}
              className="w-full py-3 text-sm text-gray-400 hover:text-gray-600 font-medium transition-colors"
            >
              返回修改
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
