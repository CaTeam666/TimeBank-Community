# 时间银行社区平台

时间银行社区平台是一个围绕“时间银行”业务模式构建的全栈社区服务系统，包含居民用户端、运营管理端和 Spring Boot 后端，覆盖任务发布与接单、亲情代理、积分兑换、申诉仲裁、排行榜、实名审核等完整业务流程。

## 项目简介

本仓库采用单仓库结构，包含 3 个主要子项目：

- `ai-end`
  基于 Spring Boot 的后端服务，提供 REST API、MySQL 持久化、Redis 缓存、OCR 接入、排行榜任务和核心业务流程。
- `timebank-family`
  基于 React + TypeScript 的用户端应用，包含任务大厅、亲情代理、爱心商城、排行榜、个人中心和消息中心等功能。
- `timebank-backend`
  基于 React + TypeScript 的管理端应用，面向运营和审核人员，提供审核、仲裁、配置、奖励管理、数据看板等后台能力。

## 核心功能

- 用户注册、登录与个人资料管理
- 亲情代理与家庭绑定审核流程
- 任务发布、接单、执行、评价与申诉处理
- 积分 / 时间币流水与商城兑换
- 管理端数据看板、监控、排行与异常数据处理
- OCR 实名认证与文件上传流程

## 技术栈

- 后端：Java 17、Spring Boot、MyBatis-Plus、MySQL、Redis、Druid
- 前端：React、TypeScript、Vite
- 基础能力：阿里云 OCR、对象存储风格文件服务、定时任务

## 仓库结构

```text
TimeBank-Community/
+-- ai-end/              # Spring Boot 后端
+-- timebank-family/     # 用户端前端
\-- timebank-backend/    # 管理端前端
```

## 本地开发

### 1. 启动后端

```bash
cd ai-end
mvn spring-boot:run
```

默认端口：`8080`

本地依赖：

- MySQL
- Redis

敏感配置已改为环境变量读取，示例：

```bash
DB_USERNAME=root
DB_PASSWORD=你的数据库密码
ALIYUN_OCR_ACCESS_KEY_ID=你的阿里云 Key
ALIYUN_OCR_ACCESS_KEY_SECRET=你的阿里云 Secret
```

### 2. 启动用户端

```bash
cd timebank-family
npm install
npm run dev
```

默认端口：`3000`

### 3. 启动管理端

```bash
cd timebank-backend
npm install
npm run dev
```

默认端口：`3002`

## 简历亮点

- 独立完成用户端、管理端、后端三端协同的全栈项目，而非单一页面型作品
- 将亲情代理、任务履约、申诉仲裁、奖励管理、实名审核、排行榜等业务流程串联为完整系统
- 具备从前端交互、接口设计到后台运营支持的完整落地能力
- 已整理为统一单仓库结构，便于展示、维护和持续迭代

## 后续规划

- 补充部署文档和在线演示地址
- 增加系统架构图与页面截图
- 在仓库根目录补充接口总览和数据库结构说明

## 许可证

本项目采用 MIT License。

- 标准英文许可证见 `LICENSE`
- 中文说明见 `LICENSE.zh-CN`
