---
description: 启动时间银行项目的前端、后台、后端API和MCP工具
---

# 时间银行项目完整启动流程

本workflow用于启动时间银行项目的所有相关服务，包括用户端前端、管理后台、后端API服务和MySQL MCP工具。

## 项目结构

项目包含以下四个主要组件：

1. **timebank-family**: 用户端前端（时间银行家庭端）
   - 位置：`c:\Users\CaTeam\Desktop\ai project\timebank-family`
   - 技术栈：React + Vite + TypeScript
   - 端口：3000
   - 访问地址：http://localhost:3000

2. **时间银行后台**: 管理后台系统
   - 位置：`c:\Users\CaTeam\Desktop\ai project\时间银行后台`
   - 技术栈：React + Vite + TypeScript
   - 端口：3002
   - 访问地址：http://localhost:3002

3. **ai-end**: 后端API服务
   - 位置：`E:\AI-final\ai-end`
   - 技术栈：Spring Boot + MyBatis-Plus + MySQL
   - 端口：默认 8080

4. **MySQL_MCP**: MySQL数据库MCP服务器
   - 位置：`C:\Users\CaTeam\MySQL_MCP`
   - 技术栈：Node.js + MySQL2
   - 用途：为AI助手提供数据库访问能力

## 启动步骤

### 1. 启动MySQL MCP工具（建议最先启动）

```bash
cd C:\Users\CaTeam\MySQL_MCP
npm start
```

MCP工具启动后将在后台运行，为AI助手提供数据库操作能力。

### 2. 启动后端API服务（ai-end）

使用Maven或IDE启动Spring Boot应用：

**使用Maven命令行**：
```bash
cd E:\AI-final\ai-end
mvn spring-boot:run
```

**或使用IDE（IntelliJ IDEA/Eclipse）**：
- 打开项目 `E:\AI-final\ai-end`
- 找到主类（通常是带有 `@SpringBootApplication` 注解的类）
- 右键 Run 或 Debug

后端API默认运行在 http://localhost:8080

### 3. 启动用户端前端（timebank-family）

```bash
cd "c:\Users\CaTeam\Desktop\ai project\timebank-family"
npm run dev
```

启动后会自动在浏览器打开 http://localhost:3000

### 4. 启动管理后台（时间银行后台）

在新的终端窗口中：

```bash
cd "c:\Users\CaTeam\Desktop\ai project\时间银行后台"
npm run dev
```

启动后会自动在浏览器打开 http://localhost:3002

## 首次运行前提条件

### 安装依赖

#### 用户端前端
```bash
cd "c:\Users\CaTeam\Desktop\ai project\timebank-family"
npm install
```

#### 管理后台
```bash
cd "c:\Users\CaTeam\Desktop\ai project\时间银行后台"
npm install
```

#### MySQL MCP工具
```bash
cd C:\Users\CaTeam\MySQL_MCP
npm install
npm run build
```

#### 后端API服务
确保已安装：
- Java 17 或更高版本
- Maven 3.6+
- MySQL数据库

```bash
cd E:\AI-final\ai-end
mvn clean install
```

## 环境配置

### 前端项目（.env.local）
确保 `GEMINI_API_KEY` 已正确设置：
```
GEMINI_API_KEY=your_api_key_here
```

### 后端API（application.yml / application.properties）
配置数据库连接、Redis等：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/timebank?useSSL=false
    username: root
    password: your_password
```

### MySQL MCP（.env）
参考 `.env.example` 配置数据库连接信息。

## 启动顺序建议

推荐按以下顺序启动服务以确保依赖关系正确：

1. **MySQL MCP工具** - 提供数据库访问能力
2. **后端API服务** - 提供数据接口
3. **用户端前端** - 依赖后端API
4. **管理后台** - 依赖后端API

## 自动打开浏览器

Vite开发服务器默认会自动在浏览器中打开页面。

如需禁用自动打开，可在 `vite.config.ts` 中设置：
```typescript
server: {
  open: false  // 禁用自动打开浏览器
}
```

## 停止服务

- **前端/后台/MCP**: 在对应的终端窗口中按 `Ctrl + C`
- **后端API**: 在终端按 `Ctrl + C` 或通过IDE停止

## 快速启动脚本

你可以创建批处理脚本来同时启动所有服务：

**start-all.bat**（仅供参考，需手动创建）：
```batch
@echo off
echo 正在启动MySQL MCP工具...
start cmd /k "cd C:\Users\CaTeam\MySQL_MCP && npm start"

echo 正在启动后端API服务...
start cmd /k "cd E:\AI-final\ai-end && mvn spring-boot:run"

timeout /t 10

echo 正在启动用户端前端...
start cmd /k "cd c:\Users\CaTeam\Desktop\ai project\timebank-family && npm run dev"

echo 正在启动管理后台...
start cmd /k "cd c:\Users\CaTeam\Desktop\ai project\时间银行后台 && npm run dev"

echo 所有服务启动完成！
```

## 故障排除

### 端口冲突
- 前端：Vite会自动选择下一个可用端口
- 后端：修改 `application.yml` 中的 `server.port`
- MCP：检查配置文件中的端口设置

### 依赖问题
- Node.js项目：删除 `node_modules` 和 `package-lock.json`，重新 `npm install`
- Maven项目：运行 `mvn clean install -U`

### 数据库连接失败
- 确保MySQL服务正在运行
- 检查数据库连接配置（用户名、密码、端口）
- 确认数据库和表已正确创建

### MCP工具无法启动
- 检查Node.js版本是否 >= 18.0.0
- 确保已运行 `npm run build`
- 检查 `.env` 文件配置是否正确

## 开发调试

### 查看日志
- **前端**: 浏览器开发者工具控制台
- **后端**: IDE控制台或终端输出
- **MCP**: 检查日志文件（如有配置）

### 热重载
- 前端项目支持热重载，代码修改后自动刷新
- 后端需配置 `spring-boot-devtools` 才支持热重载

## 服务健康检查

启动完成后，可以通过以下方式验证服务状态：

- 用户端前端：访问 http://localhost:3000
- 管理后台：访问 http://localhost:3002  
- 后端API：访问 http://localhost:8080/actuator/health（如已配置Actuator）
- MCP工具：检查终端输出无错误信息

