export enum UserRole {
  VOLUNTEER = 'VOLUNTEER',
  SENIOR = 'SENIOR',
  AGENT = 'AGENT'
}

export interface User {
  id: string;
  phone: string;
  nickname: string;
  avatar: string;
  role: UserRole;
  balance: number;
  creditScore: number;
  realName?: string;
  idCardNumber?: string;
  status: number; // 0: Disabled, 1: Enabled
}

export interface FamilyMember {
  id: string;
  relationId?: number; // 关系记录ID
  nickname: string;
  avatar: string;
  balance: number;
  relation: string; // e.g., "Father", "Mother"
  phone: string;
  status?: number; // 关系状态（1:已通过）
}

export interface AuthState {
  currentUser: User | null;
  isAuthenticated: boolean;
  isProxyMode: boolean;
  proxyTarget: FamilyMember | null;
  familyMembers: FamilyMember[];
}

export type AuthAction =
  | { type: 'LOGIN'; payload: User }
  | { type: 'LOGOUT' }
  | { type: 'UPDATE_BALANCE'; payload: { userId: string; amount: number } } // Changed to support multiple users
  | { type: 'TOGGLE_PROXY'; payload: FamilyMember | null }
  | { type: 'ADD_FAMILY_MEMBER'; payload: FamilyMember }
  | { type: 'REMOVE_FAMILY_MEMBER'; payload: string };

// --- Module 2 & 3 Types ---

export type TaskType = '陪聊' | '保洁' | '跑腿' | '医疗陪护' | '其他';

export type TaskStatus = 'pending' | 'accepted' | 'waiting_acceptance' | 'completed' | 'appealing' | 'cancelled';

export interface TaskCategory {
  key: string;
  label: string;
  icon?: string;
}

export interface Task {
  id: string;
  publisherId: string;
  publisherName: string;
  publisherAvatar: string;
  type: TaskType;
  title: string;
  description: string;
  coins: number;
  status: TaskStatus;
  location: string;
  locationDetail?: string; // Visible only after accept
  distance: string;
  date: string;
  timeRange: string;
  createdAt: number;
  acceptorId?: string;
  acceptorName?: string;
  acceptorAvatar?: string;

  // Module 3 fields
  checkInTime?: number;
  evidencePhotos?: string[]; // base64 or urls
  finishTime?: number;
  rating?: number;
  review?: string;
  appealReason?: string;
  contactPhone?: string; // For execution page
}

// --- Module 4 Types (Mall) ---

export type ProductCategory = '粮油副食' | '日用百货' | '虚拟券卡' | '医疗健康';

export interface Product {
  id: number;
  name: string;
  image: string;
  price: number;
  category: ProductCategory;
  stock: number;
  description: string;
  sales: number; // monthly sales
}

export interface ExchangeOrder {
  id: string;
  userId: string;
  productId: number;
  productName: string;
  productImage: string;
  price: number;
  quantity: number;
  totalPrice: number;
  status: 'pending_pickup' | 'completed' | 'cancelled';
  redemptionCode: string; // e.g., "8829 1023"
  createdAt: number;
}

// --- Module 5 Types (Gamification & Notification) ---

export type MessageType = 'TASK' | 'MONEY' | 'SYSTEM' | 'FAMILY';

export interface Message {
  id: string;
  userId: string;
  type: MessageType;
  title: string;
  content: string;
  isRead: boolean;
  createdAt: number;
  link?: string; // route path
  payload?: any; // extra data
}

export interface RankItem {
  id: string;
  rank: number;
  nickname: string;
  avatar: string;
  value: number; // hours or count
}
