export enum UserRole {
  ELDER = '老人',
  VOLUNTEER = '志愿者',
  CHILD_AGENT = '子女代理人',
  ADMIN = 'ADMIN'
}

export enum AccountStatus {
  NORMAL = 'NORMAL',
  FROZEN = 'FROZEN'
}

export interface User {
  id: string;
  avatar: string;
  nickname: string;
  realName: string;
  phone: string;
  role: UserRole;
  balance: number;
  registerTime: string;
  status: AccountStatus;
}

export enum AuditStatus {
  PENDING = 'PENDING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED'
}

export interface IdentityAuditTask {
  id: string;
  userId: string;
  userName: string;
  submitTime: string;
  ocrAge: number;
  idCardFront: string;
  idCardBack: string;
  ocrName: string;
  ocrIdNumber: string;
  status: AuditStatus;
}

export interface FamilyBindingTask {
  serialNo: string;
  childName: string;
  childPhone: string;
  elderName: string;
  elderPhone: string;
  proofImage: string;
  applyTime: string;
  status: AuditStatus;
  rejectReason?: string;
}

// 任务相关定义

export enum TaskStatus {
  PENDING = 'PENDING', // 待接单
  IN_PROGRESS = 'IN_PROGRESS', // 进行中
  WAITING_ACCEPTANCE = 'WAITING_ACCEPTANCE', // 待验收
  COMPLETED = 'COMPLETED', // 已完成
  EXPIRED = 'EXPIRED', // 已过期
  COMPLAINT = 'COMPLAINT', // 申诉中
  CANCELLED = 'CANCELLED' // 已取消/强制关闭
}

export enum TaskType {
  CHAT = 'CHAT', // 陪聊
  CLEANING = 'CLEANING', // 保洁
  ERRAND = 'ERRAND', // 跑腿
  MEDICAL = 'MEDICAL' // 医疗陪护
}

export interface TaskLog {
  id: string;
  time: string;
  content: string;
}

export interface Task {
  id: string;
  title: string;
  description: string;
  creatorId: string;
  creatorName: string;
  creatorRealName: string;
  creatorPhone: string;
  creatorAvatar: string;
  creatorCredit: number;
  volunteerId?: string;
  volunteerName?: string;
  volunteerPhone?: string;
  volunteerAvatar?: string;
  volunteerCredit?: number;
  coins: number;
  publishTime: string;
  deadline: string;
  status: TaskStatus;
  type: TaskType;
  address: string;
  logs: TaskLog[];
}

export interface ZombieTaskLog {
  id: string;
  taskId: string;
  taskTitle: string;
  closedTime: string;
  refundAmount: number;
  refundStatus: 'SUCCESS' | 'FAILURE';
}

// 仲裁与存证相关

export enum ArbitrationStatus {
  PENDING = 'PENDING',
  RESOLVED = 'RESOLVED'
}

export interface Arbitration {
  id: string;
  taskId: string;
  taskTitle: string;
  initiatorId: string;
  initiatorName: string;
  initiatorRole: 'PUBLISHER' | 'VOLUNTEER'; // 发布者 or 志愿者
  type: string; // 拒不验收 / 虚假服务 / 态度恶劣
  description: string; // 申诉详情
  createTime: string;
  status: ArbitrationStatus;
  defendantResponse?: string; // 被申诉人回应
  evidenceImages: string[]; // 证据图片
}

export interface ServiceEvidence {
  id: string;
  taskId: string;
  taskTitle: string;
  volunteerName: string;
  volunteerId: string;
  imageUrl: string;
  createTime: string;
}

// 商城与激励相关

export interface Product {
  id: string;
  name: string;
  description: string;
  image: string;
  price: number; // Time coins
  stock: number;
  status: 'ON_SHELF' | 'OFF_SHELF';
  salesCount: number;
  category: '粮油副食' | '日用百货' | '医疗健康' | '虚拟券卡';
}

export enum OrderStatus {
  PENDING = 'PENDING', // 待核销
  COMPLETED = 'COMPLETED', // 已完成
  CANCELLED = 'CANCELLED' // 已取消
}

export interface ExchangeOrder {
  id: string;
  orderNo: string;
  volunteerId: string;
  volunteerName: string;
  productId: string;
  productName: string;
  productImage: string;
  cost: number;
  createTime: string;
  verifyCode: string; // 核销码
  status: OrderStatus;
}

export interface RankingLog {
  id: string;
  period: string; // e.g., "2024-05"
  rank: number;
  volunteerId: string;
  volunteerName: string;
  volunteerAvatar: string;
  orderCount: number;
  rewardAmount: number;
  distributionTime: string;
  status: 'SUCCESS' | 'FAILURE';
}

// 系统设置与统计相关

export interface SystemSettings {
  elderInitialCoins: number;
  dailySignInReward: number;
  monthlyRank1Reward: number;
  transactionFeePercent: number;
}

export interface Transaction {
  id: string;
  userId: string;
  userName: string;
  type: 'EARN' | 'SPEND'; // 赚取 (产出) / 消费 (消耗)
  amount: number;
  relatedId?: string; // task id or order id
  timestamp: string;
  note: string;
}
