---
name: start-skill
description: 启动时间银行项目的所有服务。使用此skill当用户想要启动项目、运行开发服务器、开始开发环境，或说"启动项目"、"/startup"等类似指令时。执行时应该直接在命令行运行各个服务的启动命令。
---

# 时间银行项目启动Skill

## 概述

此skill用于启动时间银行项目的所有相关服务，包括用户端前端、管理后台、后端API服务和MySQL MCP工具。

**执行方式**: 直接在命令行运行各服务的启动命令，每个服务在后台运行。

## 项目组件

时间银行项目包含以下四个组件：

1. **MySQL_MCP**: MySQL数据库MCP服务器
   - 位置: `C:\Users\CaTeam\MySQL_MCP`
   - 命令: `npm start`
   - 用途: 为AI助手提供数据库访问能力

2. **ai-end**: 后端API服务
   - 位置: `E:\AI-final\ai-end`
   - 命令: `mvn spring-boot:run`
   - 端口: 8080
   - 访问: http://localhost:8080

3. **timebank-family**: 用户端前端
   - 位置: `c:\Users\CaTeam\Desktop\ai project\timebank-family`
   - 命令: `npm run dev`
   - 端口: 3000
   - 访问: http://localhost:3000

4. **时间银行后台**: 管理后台系统
   - 位置: `c:\Users\CaTeam\Desktop\ai project\时间银行后台`
   - 命令: `npm run dev`
   - 端口: 3002
   - 访问: http://localhost:3002

## AI助手执行指南

当用户请求启动项目时，按以下顺序执行命令。每个命令应该在对应的目录下运行，并作为后台进程启动：

### 1. 启动MySQL MCP工具
```bash
# 工作目录: C:\Users\CaTeam\MySQL_MCP
# 命令: npm start
```

### 2. 启动后端API服务
```bash
# 工作目录: E:\AI-final\ai-end
# 方式1（推荐）: 使用Maven Wrapper或IDE启动
# 命令: ./mvnw spring-boot:run 或通过IDE运行主类

# 方式2: 使用Maven编译后运行
# 步骤1: mvn clean package -DskipTests
# 步骤2: java -jar target/ai-end-1.0.0.jar

# 方式3: 通过IDE启动
# 打开项目，找到带@SpringBootApplication注解的主类，右键Run
```

**注意**: 如果`mvn spring-boot:run`失败，建议：
1. 使用IDE（IntelliJ IDEA/Eclipse）直接运行主类
2. 或先编译:`mvn clean package -DskipTests`，然后运行jar包


### 3. 启动用户端前端
```bash
# 工作目录: c:\Users\CaTeam\Desktop\ai project\timebank-family
# 命令: npm run dev
```

### 4. 启动管理后台
```bash
# 工作目录: c:\Users\CaTeam\Desktop\ai project\时间银行后台
# 命令: npm run dev
```

**重要说明**:
- 每个命令都应该作为后台进程运行（WaitMsBeforeAsync设置为500或更小）
- 按上述顺序启动以确保依赖关系
- 前端服务（步骤3和4）会自动在浏览器中打开对应页面

## 启动顺序说明

推荐按以下顺序启动以确保依赖关系正确：

1. **MySQL MCP工具** - 提供数据库访问能力，首先启动
2. **后端API服务** - 提供数据接口，依赖数据库
3. **用户端前端** - 依赖后端API
4. **管理后台** - 依赖后端API

## 首次运行准备

### 安装依赖

#### 前端项目依赖
```bash
# 用户端前端
cd "c:\Users\CaTeam\Desktop\ai project\timebank-family"
npm install

# 管理后台
cd "c:\Users\CaTeam\Desktop\ai project\时间银行后台"
npm install
```

#### MySQL MCP工具依赖
```bash
cd C:\Users\CaTeam\MySQL_MCP
npm install
npm run build
```

#### 后端API依赖
确保已安装 Java 17、Maven 3.6+、MySQL数据库，然后：
```bash
cd E:\AI-final\ai-end
mvn clean install
```

### 环境配置

#### 前端项目 (.env.local)
```
GEMINI_API_KEY=your_api_key_here
```

#### 后端API (application.yml)
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/timebank?useSSL=false
    username: root
    password: your_password
```

#### MySQL MCP (.env)
参考 `.env.example` 配置数据库连接信息

## 服务端口配置

确保以下端口可用：

| 服务 | 端口 | 可配置性 |
|------|------|---------|
| MySQL MCP | 3001 | 可在.env中配置 |
| 后端API | 8080 | 可在application.yml中配置 |
| 用户端前端 | 3000 | 可在vite.config.ts中配置 |
| 管理后台 | 3002 | 可在vite.config.ts中配置 |

## 自动打开浏览器

Vite开发服务器默认会自动在浏览器中打开页面。

如需禁用，可在 `vite.config.ts` 中设置：
```typescript
server: {
  open: false,  // 禁用自动打开浏览器
  port: 3000    // 指定端口
}
```

## 停止服务

使用以下方法停止服务：

- **Node.js服务** (前端/后台/MCP): 在终端按 `Ctrl + C`
- **Spring Boot** (后端API): 在终端按 `Ctrl + C` 或通过IDE停止
- **查找并结束进程**:
  ```bash
  # 查找Node.js进程
  Get-Process | Where-Object {$_.ProcessName -like "*node*"}
  
  # 查找Java进程
  Get-Process | Where-Object {$_.ProcessName -like "*java*"}
  ```

## 健康检查

启动完成后验证服务状态：

| 服务 | 检查方法 | 预期结果 |
|------|---------|---------|
| MySQL MCP | 终端输出 | 显示"MCP服务器已启动" |
| 后端API | 访问 http://localhost:8080 | 返回200或重定向 |
| 用户端前端 | 访问 http://localhost:3000 | 看到登录页面 |
| 管理后台 | 访问 http://localhost:3002 | 看到后台界面 |

## 故障排除

### 端口已被占用
- **Vite服务**: 会自动选择下一个可用端口（3001、3003等）
- **后端API**: 修改 `application.yml` 中的 `server.port`
- **MCP工具**: 修改 `.env` 中的端口配置

### 命令执行失败
- **PowerShell权限问题**: 确保执行策略允许运行脚本
- **npm命令失败**: 检查Node.js是否正确安装
- **mvn命令失败**: 检查Maven是否正确安装并配置环境变量

### 依赖问题
- **Node.js项目**: 删除 `node_modules` 和 `package-lock.json`，重新 `npm install`
- **Maven项目**: 运行 `mvn clean install -U` 强制更新依赖

### 数据库连接失败
- 确保MySQL服务正在运行
- 检查数据库连接配置（用户名、密码、端口）
- 确认数据库和表已正确创建

### MCP工具无法启动
- 检查Node.js版本 >= 18.0.0
- 确保已运行 `npm run build`
- 检查 `.env` 文件配置是否正确

## 开发调试

### 查看日志
- **前端**: 浏览器开发者工具控制台
- **后端**: IDE控制台或终端输出
- **MCP**: 终端输出或日志文件

### 热重载
- 前端项目支持热重载，代码修改后自动刷新
- 后端需配置 `spring-boot-devtools` 才支持热重载

## 手动启动参考

如果AI自动启动失败，用户可以手动在命令行执行：

```bash
# 1. MySQL MCP
cd C:\Users\CaTeam\MySQL_MCP
npm start

# 2. 后端API
cd E:\AI-final\ai-end
mvn spring-boot:run

# 3. 用户端前端
cd "c:\Users\CaTeam\Desktop\ai project\timebank-family"
npm run dev

# 4. 管理后台
cd "c:\Users\CaTeam\Desktop\ai project\时间银行后台"
npm run dev
```

## 注意事项

1. 确保所有项目依赖已安装
2. 确保MySQL数据库服务正在运行
3. 确保各端口未被占用
4. 首次运行前请配置好环境变量
5. 建议按推荐顺序启动以避免依赖问题
6. 每个服务应该在后台运行，不阻塞后续命令执行
