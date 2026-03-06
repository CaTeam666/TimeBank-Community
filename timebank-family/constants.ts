import { UserRole } from './types';

export const APP_NAME = "时间银行";

// Mock Data
export const MOCK_USER_VOLUNTEER = {
  id: 'u_001',
  phone: '13800138000',
  nickname: '陈志愿者',
  avatar: 'https://picsum.photos/200/200?random=1',
  role: UserRole.VOLUNTEER,
  balance: 1250,
  creditScore: 4.8,
  realName: '陈小明',
  idCardNumber: '510101199501011234',
  status: 1
};

export const MOCK_USER_SENIOR = {
  id: 'u_002',
  phone: '13900139000',
  nickname: '张爷爷',
  avatar: 'https://picsum.photos/200/200?random=2',
  role: UserRole.SENIOR,
  balance: 5500,
  creditScore: 5.0,
  realName: '张伟',
  idCardNumber: '510101195501011234',
  status: 1
};

export const MOCK_FAMILY_MEMBERS = [
  {
    id: 'f_001',
    nickname: '父亲',
    avatar: 'https://picsum.photos/200/200?random=2',
    balance: 5500,
    relation: '父亲',
    phone: '13900139000'
  },
  {
    id: 'f_002',
    nickname: '李奶奶',
    avatar: 'https://picsum.photos/200/200?random=3',
    balance: 320,
    relation: '祖母',
    phone: '13900139001'
  }
];

export const THEME_COLOR = "orange-500";
export const THEME_COLOR_BG = "bg-orange-500";
export const THEME_COLOR_TEXT = "text-orange-500";

export const TASK_TYPE_MAP: Record<string, string> = {
  '陪聊': 'CHAT',
  '保洁': 'CLEANING',
  '跑腿': 'ERRAND',
  '医疗陪护': 'MEDICAL',
  '其他': 'OTHER'
};

export const TASK_TYPE_REVERSE_MAP: Record<string, string> = {
  'CHAT': '陪聊',
  'CLEANING': '保洁',
  'ERRAND': '跑腿',
  'MEDICAL': '医疗陪护',
  'OTHER': '其他'
};
