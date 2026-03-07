import { User, UserRole, AccountStatus, IdentityAuditTask, AuditStatus, FamilyBindingTask, Task, TaskStatus, TaskType, ZombieTaskLog, Arbitration, ArbitrationStatus, ServiceEvidence, Product, ExchangeOrder, OrderStatus, RankingLog, SystemSettings, Transaction } from '../types';

export const MOCK_USERS: User[] = [
  {
    id: 'U1001',
    avatar: 'https://picsum.photos/id/64/100/100',
    nickname: '张爷爷',
    realName: '张伟',
    phone: '13800138000',
    role: UserRole.ELDER,
    balance: 120,
    registerTime: '2023-10-15 09:30:00',
    status: AccountStatus.NORMAL
  },
  {
    id: 'U1002',
    avatar: 'https://picsum.photos/id/65/100/100',
    nickname: '志愿者小李',
    realName: '李娜',
    phone: '13912345678',
    role: UserRole.VOLUNTEER,
    balance: 450,
    registerTime: '2023-11-01 14:20:00',
    status: AccountStatus.NORMAL
  },
  {
    id: 'U1003',
    avatar: 'https://picsum.photos/id/66/100/100',
    nickname: '代理人陈博',
    realName: '陈博',
    phone: '15098765432',
    role: UserRole.CHILD_AGENT,
    balance: 0,
    registerTime: '2023-12-10 11:00:00',
    status: AccountStatus.FROZEN
  }
];

export const MOCK_IDENTITY_AUDITS: IdentityAuditTask[] = [
  {
    id: 'AUD-001',
    userId: 'U2024',
    userName: '王芳',
    submitTime: '2024-05-20 10:00',
    ocrAge: 68,
    idCardFront: 'https://picsum.photos/id/100/400/250',
    idCardBack: 'https://picsum.photos/id/101/400/250',
    ocrName: '王芳',
    ocrIdNumber: '110101195601011234',
    status: AuditStatus.PENDING
  },
  {
    id: 'AUD-002',
    userId: 'U2025',
    userName: '刘洋',
    submitTime: '2024-05-21 15:30',
    ocrAge: 25,
    idCardFront: 'https://picsum.photos/id/102/400/250',
    idCardBack: 'https://picsum.photos/id/103/400/250',
    ocrName: '刘洋',
    ocrIdNumber: '110101199901011234',
    status: AuditStatus.PENDING
  }
];

export const MOCK_BINDING_TASKS: FamilyBindingTask[] = [
  {
    serialNo: 'BIN-2024052001',
    childName: '张小',
    childPhone: '13811112222',
    elderName: '张大',
    elderPhone: '13933334444',
    proofImage: 'https://picsum.photos/id/180/400/600',
    applyTime: '2024-05-20 09:00',
    status: AuditStatus.PENDING
  },
  {
    serialNo: 'BIN-2024052002',
    childName: '李四',
    childPhone: '15566667777',
    elderName: '王五',
    elderPhone: '18899990000',
    proofImage: 'https://picsum.photos/id/181/400/600',
    applyTime: '2024-05-19 16:20',
    status: AuditStatus.APPROVED
  }
];

export const MOCK_TASKS: Task[] = [
  {
    id: 'T1001',
    title: '需要有人帮忙打扫客厅和厨房',
    description: '腰不好了，需要志愿者帮忙打扫一下卫生，大概2小时。',
    creatorId: 'U1001',
    creatorName: '张爷爷',
    creatorRealName: '张伟',
    creatorPhone: '13800138000',
    creatorAvatar: 'https://picsum.photos/id/64/100/100',
    creatorCredit: 98,
    coins: 50,
    publishTime: '2024-05-22 09:00',
    deadline: '2024-05-23 18:00',
    status: TaskStatus.PENDING,
    type: TaskType.CLEANING,
    address: '幸福小区3号楼2单元501',
    logs: [
      { id: 'L1', time: '2024-05-22 09:00', content: '张爷爷 发布了任务' },
      { id: 'L2', time: '2024-05-22 09:05', content: '系统冻结 50 积分' }
    ]
  },
  {
    id: 'T1002',
    title: '去医院取药陪同',
    description: '周五上午去市医院取药，需要有人陪同排队。',
    creatorId: 'U1005',
    creatorName: '王奶奶',
    creatorRealName: '王桂兰',
    creatorPhone: '13800138005',
    creatorAvatar: 'https://picsum.photos/id/200/100/100',
    creatorCredit: 100,
    volunteerId: 'U1002',
    volunteerName: '志愿者小李',
    volunteerPhone: '13912345678',
    volunteerAvatar: 'https://picsum.photos/id/65/100/100',
    volunteerCredit: 95,
    coins: 80,
    publishTime: '2024-05-21 14:00',
    deadline: '2024-05-24 12:00',
    status: TaskStatus.IN_PROGRESS,
    type: TaskType.MEDICAL,
    address: '阳光花园8号楼102',
    logs: [
      { id: 'L3', time: '2024-05-21 14:00', content: '王奶奶 发布了任务' },
      { id: 'L4', time: '2024-05-21 14:30', content: '志愿者小李 接单成功' },
      { id: 'L5', time: '2024-05-22 10:00', content: '志愿者小李 到达服务地点' }
    ]
  },
  {
    id: 'T1003',
    title: '想找人聊聊天',
    description: '儿女不在身边，想找个年轻人聊聊天。',
    creatorId: 'U1006',
    creatorName: '刘大爷',
    creatorRealName: '刘海',
    creatorPhone: '13800138006',
    creatorAvatar: 'https://picsum.photos/id/201/100/100',
    creatorCredit: 90,
    coins: 30,
    publishTime: '2024-05-18 10:00',
    deadline: '2024-05-18 20:00',
    status: TaskStatus.EXPIRED,
    type: TaskType.CHAT,
    address: '和平社区活动中心',
    logs: [
      { id: 'L6', time: '2024-05-18 10:00', content: '刘大爷 发布了任务' },
      { id: 'L7', time: '2024-05-18 20:00', content: '任务无人接单，系统自动过期' },
      { id: 'L8', time: '2024-05-18 20:01', content: '资金 30 积分 已自动解冻退回' }
    ]
  },
  {
    id: 'T1004',
    title: '代买生活用品',
    description: '需要买米和油送到家里。',
    creatorId: 'U1007',
    creatorName: '赵阿姨',
    creatorRealName: '赵敏',
    creatorPhone: '13800138007',
    creatorAvatar: 'https://picsum.photos/id/202/100/100',
    creatorCredit: 92,
    volunteerId: 'U1008',
    volunteerName: '志愿者小刚',
    volunteerPhone: '13988887777',
    volunteerAvatar: 'https://picsum.photos/id/203/100/100',
    volunteerCredit: 88,
    coins: 40,
    publishTime: '2024-05-20 16:00',
    deadline: '2024-05-21 18:00',
    status: TaskStatus.COMPLAINT,
    type: TaskType.ERRAND,
    address: '西城家园1号楼606',
    logs: [
      { id: 'L9', time: '2024-05-20 16:00', content: '赵阿姨 发布了任务' },
      { id: 'L10', time: '2024-05-20 16:30', content: '志愿者小刚 接单成功' },
      { id: 'L11', time: '2024-05-20 18:00', content: '赵阿姨 发起申诉：未按时送达且联系不上' }
    ]
  }
];

export const MOCK_ZOMBIE_LOGS: ZombieTaskLog[] = [
  {
    id: 'ZL001',
    taskId: 'T1003',
    taskTitle: '想找人聊聊天',
    closedTime: '2024-05-18 20:00:00',
    refundAmount: 30,
    refundStatus: 'SUCCESS'
  },
  {
    id: 'ZL002',
    taskId: 'T0998',
    taskTitle: '帮忙搬运旧家具',
    closedTime: '2024-05-15 12:00:00',
    refundAmount: 100,
    refundStatus: 'FAILURE'
  }
];

// 仲裁数据
export const MOCK_ARBITRATIONS: Arbitration[] = [
  {
    id: 'ARB-20240520-01',
    taskId: 'T1004',
    taskTitle: '代买生活用品',
    initiatorId: 'U1007',
    initiatorName: '赵阿姨',
    initiatorRole: 'PUBLISHER',
    type: '虚假服务',
    description: '我要求买五常大米，但他买了散装米，而且到现在都没送到门口，电话也不接。',
    createTime: '2024-05-20 18:00',
    status: ArbitrationStatus.PENDING,
    defendantResponse: '我在路上堵车了，手机没电自动关机，但我买的确实是超市里最好的米，发票都在。',
    evidenceImages: [
      'https://picsum.photos/id/400/800/600',
      'https://picsum.photos/id/401/800/600'
    ]
  },
  {
    id: 'ARB-20240521-02',
    taskId: 'T1002',
    taskTitle: '去医院取药陪同',
    initiatorId: 'U1002',
    initiatorName: '志愿者小李',
    initiatorRole: 'VOLUNTEER',
    type: '拒不验收',
    description: '我已经陪同完了，药也取到了送回家了，但老人说我态度不好，拒绝点验收，不给我结算。',
    createTime: '2024-05-24 13:00', // 假设现在是5月24
    status: ArbitrationStatus.PENDING,
    evidenceImages: [
      'https://picsum.photos/id/402/600/800'
    ]
  }
];

// 服务存证
export const MOCK_EVIDENCE: ServiceEvidence[] = [
  {
    id: 'EV-001',
    taskId: 'T0990',
    taskTitle: '日常保洁服务',
    volunteerName: '志愿者小红',
    volunteerId: 'U1020',
    imageUrl: 'https://picsum.photos/id/510/400/600',
    createTime: '2024-05-10 14:30'
  },
  {
    id: 'EV-002',
    taskId: 'T0991',
    taskTitle: '陪同就医',
    volunteerName: '志愿者小李',
    volunteerId: 'U1002',
    imageUrl: 'https://picsum.photos/id/511/400/300',
    createTime: '2024-05-11 10:15'
  },
  {
    id: 'EV-003',
    taskId: 'T0992',
    taskTitle: '代买蔬菜',
    volunteerName: '志愿者小刚',
    volunteerId: 'U1008',
    imageUrl: 'https://picsum.photos/id/512/300/400',
    createTime: '2024-05-12 09:00'
  },
  {
    id: 'EV-004',
    taskId: 'T0993',
    taskTitle: '修理水龙头',
    volunteerName: '志愿者大强',
    volunteerId: 'U1030',
    imageUrl: 'https://picsum.photos/id/513/500/500',
    createTime: '2024-05-13 16:20'
  },
  {
    id: 'EV-005',
    taskId: 'T0994',
    taskTitle: '读报陪伴',
    volunteerName: '志愿者小红',
    volunteerId: 'U1020',
    imageUrl: 'https://picsum.photos/id/514/400/400',
    createTime: '2024-05-14 15:00'
  },
  {
    id: 'EV-006',
    taskId: 'T0995',
    taskTitle: '遛狗',
    volunteerName: '志愿者小明',
    volunteerId: 'U1040',
    imageUrl: 'https://picsum.photos/id/515/300/500',
    createTime: '2024-05-15 08:30'
  }
];

// 商品
export const MOCK_PRODUCTS: Product[] = [
  {
    id: 'P1001',
    name: '金龙鱼大米 5kg',
    description: '优质东北大米，口感软糯。',
    image: 'https://picsum.photos/id/225/200/200',
    price: 200,
    stock: 5,
    status: 'ON_SHELF',
    salesCount: 150
  },
  {
    id: 'P1002',
    name: '鲁花花生油 5L',
    description: '物理压榨，一级压榨花生油。',
    image: 'https://picsum.photos/id/226/200/200',
    price: 450,
    stock: 20,
    status: 'ON_SHELF',
    salesCount: 80
  },
  {
    id: 'P1003',
    name: '心相印抽纸 1提',
    description: '3层120抽*6包，原生木浆。',
    image: 'https://picsum.photos/id/227/200/200',
    price: 50,
    stock: 100,
    status: 'ON_SHELF',
    salesCount: 300
  },
  {
    id: 'P1004',
    name: '社区理发券',
    description: '凭券可到社区合作理发店免费理发一次。',
    image: 'https://picsum.photos/id/228/200/200',
    price: 30,
    stock: 0,
    status: 'OFF_SHELF',
    salesCount: 500
  }
];

// 订单
export const MOCK_ORDERS: ExchangeOrder[] = [
  {
    id: 'O20240524001',
    orderNo: 'EX-20240524-8832',
    volunteerId: 'U1002',
    volunteerName: '志愿者小李',
    productId: 'P1001',
    productName: '金龙鱼大米 5kg',
    productImage: 'https://picsum.photos/id/225/200/200',
    cost: 200,
    createTime: '2024-05-24 10:00:00',
    verifyCode: '883291',
    status: OrderStatus.PENDING
  },
  {
    id: 'O20240523002',
    orderNo: 'EX-20240523-1122',
    volunteerId: 'U1008',
    volunteerName: '志愿者小刚',
    productId: 'P1003',
    productName: '心相印抽纸 1提',
    productImage: 'https://picsum.photos/id/227/200/200',
    cost: 50,
    createTime: '2024-05-23 16:45:00',
    verifyCode: '112233',
    status: OrderStatus.COMPLETED
  }
];

// 排行榜日志
export const MOCK_RANKING_LOGS: RankingLog[] = [
  {
    id: 'RL-202404-01',
    period: '2024-04',
    rank: 1,
    volunteerId: 'U1002',
    volunteerName: '志愿者小李',
    volunteerAvatar: 'https://picsum.photos/id/65/100/100',
    serviceHours: 120,
    rewardAmount: 100,
    distributionTime: '2024-05-01 00:00:10',
    status: 'SUCCESS'
  },
  {
    id: 'RL-202404-02',
    period: '2024-04',
    rank: 2,
    volunteerId: 'U1008',
    volunteerName: '志愿者小刚',
    volunteerAvatar: 'https://picsum.photos/id/203/100/100',
    serviceHours: 98,
    rewardAmount: 80,
    distributionTime: '2024-05-01 00:00:12',
    status: 'SUCCESS'
  },
  {
    id: 'RL-202404-03',
    period: '2024-04',
    rank: 3,
    volunteerId: 'U1020',
    volunteerName: '志愿者小红',
    volunteerAvatar: 'https://picsum.photos/id/510/400/600',
    serviceHours: 85,
    rewardAmount: 50,
    distributionTime: '2024-05-01 00:00:15',
    status: 'FAILURE' // 模拟失败
  }
];

// 系统配置 Mock
export const MOCK_SYSTEM_SETTINGS: SystemSettings = {
  elderInitialCoins: 500,
  dailySignInReward: 5,
  monthlyRank1Reward: 100,
  transactionFeePercent: 0
};

// 交易 Mock (大额监控)
export const MOCK_TRANSACTIONS: Transaction[] = [
  {
    id: 'TX1001',
    userId: 'U1002',
    userName: '志愿者小李',
    type: 'EARN',
    amount: 50,
    timestamp: '2024-05-24 10:30',
    note: '完成任务: 需要有人帮忙打扫...'
  },
  {
    id: 'TX1002',
    userId: 'U1002',
    userName: '志愿者小李',
    type: 'SPEND',
    amount: 200,
    timestamp: '2024-05-24 12:00',
    note: '兑换商品: 金龙鱼大米'
  },
  {
    id: 'TX1003',
    userId: 'U1008',
    userName: '志愿者小刚',
    type: 'EARN',
    amount: 300,
    timestamp: '2024-05-23 09:00',
    note: '系统奖励: 月度排行榜第一名'
  },
  {
    id: 'TX1004',
    userId: 'U9999',
    userName: '异常账户',
    type: 'EARN',
    amount: 5000,
    timestamp: '2024-05-22 01:00',
    note: '未知来源'
  }
];

// 仪表盘数据 Mock
export const MOCK_DASHBOARD_DATA = {
  serviceActivity: [
    { name: '5-18', value: 24 },
    { name: '5-19', value: 30 },
    { name: '5-20', value: 45 },
    { name: '5-21', value: 38 },
    { name: '5-22', value: 50 },
    { name: '5-23', value: 42 },
    { name: '5-24', value: 55 },
  ],
  taskDistribution: [
    { name: '陪聊', value: 40 },
    { name: '保洁', value: 30 },
    { name: '跑腿', value: 20 },
    { name: '医疗', value: 10 },
  ],
  topVolunteers: [
    { name: '小李', hours: 120 },
    { name: '小刚', hours: 98 },
    { name: '小红', hours: 85 },
    { name: '大强', hours: 72 },
    { name: '小明', hours: 60 },
  ],
  financialFlow: [
    { name: '5-18', earned: 300, consumed: 150 },
    { name: '5-19', earned: 400, consumed: 200 },
    { name: '5-20', earned: 350, consumed: 300 },
    { name: '5-21', earned: 500, consumed: 250 },
    { name: '5-22', earned: 450, consumed: 400 },
    { name: '5-23', earned: 600, consumed: 350 },
    { name: '5-24', earned: 550, consumed: 500 },
  ]
};
