import React, { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { useUI } from '../context/UIContext';
import { Input, Button, NavBar, Modal } from '../components/UIComponents';

import { authService } from '../services/authService';
import { UserRole } from '../types';
import { Camera, CheckCircle, Upload, User, UserPlus, Users, Clock, ShieldAlert, ArrowRight } from 'lucide-react';
import { calculateAge } from '../utils/idCardUtils';
import { AgeVerificationModal } from '../components/AgeVerificationModal';


export default function Register() {
  const navigate = useNavigate();
  const { dispatch } = useAuth();
  const { showToast } = useUI();


  // Steps: 1 = Basic Info, 2 = Role & ID
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);

  // Step 1 Data
  const [formData, setFormData] = useState({
    phone: '',
    password: '',
    nickname: ''
  });

  // Step 2 Data
  const [role, setRole] = useState<UserRole | null>(null);
  const [idInfo, setIdInfo] = useState({
    realName: '',
    idCard: ''
  });

  const [idImageFront, setIdImageFront] = useState<string | null>(null);
  const [idImageBack, setIdImageBack] = useState<string | null>(null);

  // Store actual files for upload
  const [frontFile, setFrontFile] = useState<File | null>(null);
  const [backFile, setBackFile] = useState<File | null>(null);

  const frontInputRef = useRef<HTMLInputElement>(null);
  const backInputRef = useRef<HTMLInputElement>(null);

  // Step 3 Data (Audit)
  const [auditId, setAuditId] = useState<string | null>(null);
  const [auditStatus, setAuditStatus] = useState<number>(0); // 0=Pending, 1=Pass, 2=Reject
  const [rejectReason, setRejectReason] = useState<string>('');
  const [polling, setPolling] = useState(false);
  const [ocrLoading, setOcrLoading] = useState(false);

  // Uploaded URLs
  const [frontUrl, setFrontUrl] = useState<string | null>(null);
  const [backUrl, setBackUrl] = useState<string | null>(null);


  // Age Verification Modal State
  const [showAgeModal, setShowAgeModal] = useState(false);
  const [detectedAge, setDetectedAge] = useState(0);



  // Polling Effect
  useEffect(() => {
    let interval: any;
    if (step === 3 && auditId && polling && auditStatus === 0) {
      interval = setInterval(async () => {
        try {
          const res = await authService.checkAuditStatus(auditId);
          if (res.status === 1) { // Passed
            setAuditStatus(1);
            setPolling(false);
            // Handle Success
            if (role === UserRole.SENIOR) {
              localStorage.setItem('showWelcomeReward', 'true');
            }
            dispatch({ type: 'LOGIN', payload: res.user });
            setTimeout(() => navigate('/user/profile'), 1500);

          } else if (res.status === 2) { // Rejected
            setAuditStatus(2);
            setRejectReason(res.reject_reason || '您的实名信息未通过审核，请检查后重新提交');
            setPolling(false);
          }
        } catch (err) {
          console.error("Audit Check Failed", err);
          // Don't stop polling on network error, or maybe stop after N retries
        }
      }, 3000); // Check every 3 seconds
    }
    return () => clearInterval(interval);
  }, [step, auditId, polling, auditStatus, role, dispatch, navigate]);

  // Handlers
  const handleNextStep = () => {
    if (formData.phone && formData.password && formData.nickname) {
      if (formData.phone.length !== 11) {
        showToast('请输入正确的11位手机号', 'error');
        return;
      }
      setStep(2);
    } else {
      showToast('请填写完整基本信息', 'info');
    }
  };


  const handleFileChange = (e: React.ChangeEvent<HTMLInputElement>, isFront: boolean) => {
    const file = e.target.files?.[0];
    if (!file) return;

    // Validation
    if (file.size > 5 * 1024 * 1024) {
      showToast("图片大小不能超过 5MB", "error");
      return;
    }
    if (!file.type.startsWith('image/')) {
      showToast("请上传图片文件", "error");
      return;
    }


    console.log(`Select File (${isFront ? 'Front' : 'Back'}):`, file.name, file.size, file.type);

    // Save file object
    if (isFront) setFrontFile(file);
    else setBackFile(file);

    // Convert to Base64/URL for preview
    const reader = new FileReader();
    reader.onloadend = async () => {
      const result = reader.result as string;
      if (isFront) {
        setIdImageFront(result);
        // Trigger OCR for front side
        try {
          setOcrLoading(true);
          const url = await authService.uploadImage(file, 'oss');
          setFrontUrl(url);
          const ocrData = await authService.ocrIdCard(url, 'face');
          if (ocrData.name || ocrData.idNum) {
            setIdInfo({
              realName: ocrData.name || idInfo.realName,
              idCard: ocrData.idNum || idInfo.idCard
            });
          }
        } catch (err) {
          console.error("OCR Failed", err);
        } finally {
          setOcrLoading(false);
        }
      } else {
        setIdImageBack(result);
        // Just upload for back side to avoid upload during register step
        try {
          const url = await authService.uploadImage(file, 'oss');
          setBackUrl(url);
        } catch (err) {
          console.error("Back side upload failed", err);
        }
      }
    };
    reader.readAsDataURL(file);
  };

  const validateStep2 = () => {
    return role && idInfo.realName && idInfo.idCard && idImageFront && idImageBack;
  };

  const handleRegister = async () => {
    if (!role || !frontFile || !backFile) return;

    // --- Age Check for SENIOR role ---
    if (role === UserRole.SENIOR) {
      const age = calculateAge(idInfo.idCard);
      if (age !== -1 && age < 60) {
        setDetectedAge(age);
        setShowAgeModal(true);
        return; // Intercept registration
      }
    }

    try {

      setLoading(true);

      // 1. Upload Images if not already uploaded
      const finalFrontUrl = frontUrl || (frontFile ? await authService.uploadImage(frontFile, 'oss') : null);
      const finalBackUrl = backUrl || (backFile ? await authService.uploadImage(backFile, 'oss') : null);

      if (!finalFrontUrl || !finalBackUrl) {
        throw new Error('请先上传身份证正反面照片');
      }

      // 2. Submit Register with URLs
      const res = await authService.register({
        ...formData,
        role: role,
        realName: idInfo.realName,
        idCard: idInfo.idCard,
        idCardFront: finalFrontUrl,
        idCardBack: finalBackUrl
      });

      setAuditId(res.auditId);
      setStep(3);
      setPolling(true);

    } catch (error: any) {
      showToast(error.message || '注册失败', 'error');
    } finally {
      setLoading(false);
    }

  };

  const handleSwitchToVolunteer = () => {
    setRole(UserRole.VOLUNTEER);
    setShowAgeModal(false);
    // After switching, the user can click Register again
  };


  return (
    <div className="min-h-screen bg-gray-50 pb-8">
      <NavBar title={step === 1 ? "注册账号" : "身份验证"} showBack />

      {/* Step 1: Basic Info */}
      {step === 1 && (
        <div className="p-6 space-y-6 bg-white min-h-[calc(100vh-46px)]">
          <Input
            label="手机号"
            value={formData.phone}
            onChange={(e) => setFormData({ ...formData, phone: e.target.value })}
            placeholder="请输入11位手机号"
          />
          <Input
            label="密码"
            type="password"
            value={formData.password}
            onChange={(e) => setFormData({ ...formData, password: e.target.value })}
            placeholder="设置登录密码"
          />
          <Input
            label="昵称"
            value={formData.nickname}
            onChange={(e) => setFormData({ ...formData, nickname: e.target.value })}
            placeholder="怎么称呼您"
          />
          <Button fullWidth size="lg" onClick={handleNextStep} disabled={!formData.phone || !formData.password}>下一步</Button>
        </div>
      )}

      {/* Step 2: Role & ID */}
      {step === 2 && (
        <div className="p-4 space-y-6">

          {/* Role Selection */}
          <div className="bg-white p-4 rounded-xl shadow-sm space-y-3">
            <h3 className="font-bold text-gray-800">请选择您的身份</h3>
            <div className="grid grid-cols-1 gap-3">
              <div
                onClick={() => setRole(UserRole.SENIOR)}
                className={`p-4 border-2 rounded-xl flex items-center gap-4 transition-all ${role === UserRole.SENIOR ? 'border-orange-500 bg-orange-50' : 'border-gray-100'}`}
              >
                <div className="w-10 h-10 rounded-full bg-orange-100 flex items-center justify-center text-orange-600">
                  <User size={20} />
                </div>
                <div>
                  <p className="font-bold text-gray-800">我是老人</p>
                  <p className="text-xs text-gray-500">发布需求，享受服务</p>
                </div>
                {role === UserRole.SENIOR && <CheckCircle className="ml-auto text-orange-500" size={20} />}
              </div>

              <div
                onClick={() => setRole(UserRole.VOLUNTEER)}
                className={`p-4 border-2 rounded-xl flex items-center gap-4 transition-all ${role === UserRole.VOLUNTEER ? 'border-green-500 bg-green-50' : 'border-gray-100'}`}
              >
                <div className="w-10 h-10 rounded-full bg-green-100 flex items-center justify-center text-green-600">
                  <UserPlus size={20} />
                </div>
                <div>
                  <p className="font-bold text-gray-800">我是志愿者</p>
                  <p className="text-xs text-gray-500">做志愿者，赚取积分</p>
                </div>
                {role === UserRole.VOLUNTEER && <CheckCircle className="ml-auto text-green-500" size={20} />}
              </div>

              <div
                onClick={() => setRole(UserRole.AGENT)}
                className={`p-4 border-2 rounded-xl flex items-center gap-4 transition-all ${role === UserRole.AGENT ? 'border-blue-500 bg-blue-50' : 'border-gray-100'}`}
              >
                <div className="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                  <Users size={20} />
                </div>
                <div>
                  <p className="font-bold text-gray-800">我是子女代理人</p>
                  <p className="text-xs text-gray-500">远程管理父母账户</p>
                </div>
                {role === UserRole.AGENT && <CheckCircle className="ml-auto text-blue-500" size={20} />}
              </div>
            </div>
          </div>

          {/* Manual Input */}
          <div className="bg-white p-4 rounded-xl shadow-sm space-y-4">
            <h3 className="font-bold text-gray-800">实名信息</h3>
            <Input
              label="真实姓名"
              value={idInfo.realName}
              onChange={(e) => setIdInfo({ ...idInfo, realName: e.target.value })}
              placeholder="请输入真实姓名"
            />
            <Input
              label="身份证号"
              value={idInfo.idCard}
              onChange={(e) => setIdInfo({ ...idInfo, idCard: e.target.value })}
              placeholder="请输入18位身份证号"
            />
          </div>

          {/* Images Upload */}
          <div className="bg-white p-4 rounded-xl shadow-sm space-y-4">
            <h3 className="font-bold text-gray-800">上传身份证</h3>

            <div className="grid grid-cols-2 gap-3">
              {/* Front */}
              <div
                onClick={() => frontInputRef.current?.click()}
                className="aspect-[1.6] bg-gray-50 border border-dashed border-gray-300 rounded-lg flex flex-col items-center justify-center text-gray-400 relative overflow-hidden"
              >
                {ocrLoading ? (
                  <div className="flex flex-col items-center">
                    <div className="w-8 h-8 border-4 border-orange-500 border-t-transparent rounded-full animate-spin mb-2"></div>
                    <span className="text-xs text-orange-500">正在识别身份...</span>
                  </div>
                ) : idImageFront ? (
                  <img src={idImageFront} className="w-full h-full object-cover" />
                ) : (
                  <>
                    <Camera size={24} className="mb-1" />
                    <span className="text-xs">人像面</span>
                  </>
                )}
                <input ref={frontInputRef} type="file" className="hidden" accept="image/*" onChange={(e) => handleFileChange(e, true)} />
              </div>

              {/* Back */}
              <div
                onClick={() => backInputRef.current?.click()}
                className="aspect-[1.6] bg-gray-50 border border-dashed border-gray-300 rounded-lg flex flex-col items-center justify-center text-gray-400 relative overflow-hidden"
              >
                {idImageBack ? (
                  <img src={idImageBack} className="w-full h-full object-cover" />
                ) : (
                  <>
                    <Camera size={24} className="mb-1" />
                    <span className="text-xs">国徽面</span>
                  </>
                )}
                <input ref={backInputRef} type="file" className="hidden" accept="image/*" onChange={(e) => handleFileChange(e, false)} />
              </div>
            </div>
          </div>

          <Button fullWidth size="lg" onClick={handleRegister} disabled={!validateStep2() || loading}>
            {loading ? '注册中...' : '确认并注册'}
          </Button>

        </div>
      )}

      {/* Step 3: Audit Waiting */}
      {step === 3 && (
        <div className="p-6 flex flex-col items-center justify-center min-h-[60vh] text-center space-y-6 bg-white m-4 rounded-xl shadow-sm">
          {auditStatus === 0 && (
            <>
              <div className="w-20 h-20 bg-blue-50 rounded-full flex items-center justify-center animate-pulse">
                <Clock className="text-blue-500" size={40} />
              </div>
              <div>
                <h3 className="text-xl font-bold text-gray-800">审核中，请稍候...</h3>
                <p className="text-gray-500 mt-2">系统正在核实您的身份信息，请耐心等待</p>
              </div>
            </>
          )}

          {auditStatus === 1 && (
            <>
              <div className="w-20 h-20 bg-green-50 rounded-full flex items-center justify-center">
                <CheckCircle className="text-green-500" size={40} />
              </div>
              <div>
                <h3 className="text-xl font-bold text-gray-800">审核通过！</h3>
                <p className="text-gray-500 mt-2">欢迎加入时间银行大家庭</p>
              </div>
            </>
          )}

          {auditStatus === 2 && (
            <>
              <div className="w-20 h-20 bg-red-50 rounded-full flex items-center justify-center">
                <ShieldAlert className="text-red-500" size={40} />
              </div>
              <div>
                <h3 className="text-xl font-bold text-gray-800">审核未通过</h3>
                <p className="text-red-500 mt-2 font-medium bg-red-50 p-3 rounded-lg text-sm">
                  原因: {rejectReason}
                </p>
              </div>
              <Button fullWidth onClick={() => { setStep(2); setAuditStatus(0); }} className="bg-gray-800">修改并重试</Button>
            </>
          )}
        </div>
      )}

      {/* Age Verification Modal */}
      <AgeVerificationModal 
        isOpen={showAgeModal}
        age={detectedAge}
        onClose={() => setShowAgeModal(false)}
        onConfirm={handleSwitchToVolunteer}
      />

    </div>
  );
}