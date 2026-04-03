# 时间银行用户端

该子项目是时间银行社区平台的居民用户端应用，基于 React + TypeScript + Vite 构建，面向普通用户和家庭成员，提供任务参与、亲情代理、商城兑换、排行榜、消息通知等核心能力。

## 项目简介

用户端围绕社区服务和家庭协助场景展开，当前主要功能包括：

- 用户注册、登录与个人信息维护
- 任务发布、接单、执行、评价
- 亲情代理与家庭成员绑定
- 爱心商城与商品兑换
- 排行榜与激励展示
- 消息通知与个人中心

## 本地运行

前置要求：

- Node.js 16 及以上

启动方式：

```bash
npm install
npm run dev
```

默认访问地址：

- `http://localhost:3000`

如需本地环境变量，可在项目根目录创建 `.env.local`。

## 项目结构

```text
timebank-family/
+-- components/    # 公共组件
+-- context/       # 全局状态管理
+-- pages/         # 页面组件
+-- services/      # 接口服务封装
+-- doc/           # 接口文档
+-- types.ts       # TypeScript 类型定义
\-- constants.ts   # 常量配置
```

## 技术栈

- React 18
- TypeScript
- Vite
- React Context API
- Axios

## 关联项目

- 管理端：`../timebank-backend`
- 后端服务：`../ai-end`

## 开发者

- CaTeam666

## 许可证

本项目遵循 MIT License。
