# 时间银行 - 用户端

这是时间银行项目的**用户端应用**，基于 React + TypeScript + Vite 构建。

## 项目简介

时间银行用户端是一个家庭任务管理与积分兑换系统，包含以下核心功能：

- 👤 **用户认证**：登录、注册、家庭管理
- 📋 **任务系统**：任务发布、接单、执行、审核
- 🏪 **爱心超市**：积分商城、商品兑换
- 📊 **排行榜**：家庭成员积分排名
- 💬 **消息通知**：系统消息推送
- 🤖 **AI助手**：集成 Gemini AI 智能辅助

## 本地运行

**前置要求：** Node.js >= 16

1. 安装依赖：
   ```bash
   npm install
   ```

2. 配置环境变量：
   - 在项目根目录创建 `.env.local` 文件
   - 添加 Gemini API Key：
     ```
     GEMINI_API_KEY=你的API密钥
     ```

3. 启动应用：
   ```bash
   npm run dev
   ```

4. 打开浏览器访问：`http://localhost:5173`

## 项目结构

```
timebank-family/
├── components/      # 公共组件
├── context/         # 全局状态管理
├── pages/           # 页面组件
├── services/        # API服务封装
├── doc/             # 接口文档
├── types.ts         # TypeScript类型定义
└── constants.ts     # 常量配置
```

## 技术栈

- **前端框架**：React 18
- **语言**：TypeScript
- **构建工具**：Vite
- **状态管理**：React Context API
- **HTTP客户端**：Axios
- **AI集成**：Google Gemini AI

## 相关项目

- **管理端**：[待补充链接]
- **后端服务**：[待补充链接]

## 开发者

CaTeam666

## 许可证

MIT License
