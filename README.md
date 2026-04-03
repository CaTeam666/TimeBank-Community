# TimeBank Community

TimeBank Community is a full-stack community service platform built around the "time bank" model. It includes a resident-facing client, an admin dashboard, and a Spring Boot backend for task publishing, family proxy workflows, reward management, dispute handling, ranking, and real-name verification.

## Project Overview

This repository is organized as a monorepo with three main applications:

- `ai-end`
  Spring Boot backend providing REST APIs, MySQL persistence, Redis caching, OCR integration, ranking jobs, and business workflows.
- `timebank-family`
  React + TypeScript user client for task hall, family proxy, mall redemption, leaderboard, profile, and message center.
- `timebank-backend`
  React + TypeScript admin dashboard for review, arbitration, settings, reward monitoring, dashboards, and operations support.

## Core Features

- User registration, login, and profile management
- Family proxy and family binding review flows
- Task publishing, accepting, execution, review, and appeal handling
- Points / time-coin accounting and mall redemption
- Admin-side dashboards, monitoring, ranking, and abnormal data handling
- OCR-assisted identity verification and file upload workflows

## Tech Stack

- Backend: Java 17, Spring Boot, MyBatis-Plus, MySQL, Redis, Druid
- Frontend: React, TypeScript, Vite
- Infra / integrations: Aliyun OCR, OSS-style file hosting, scheduled jobs

## Repository Structure

```text
TimeBank-Community/
├── ai-end/               # Spring Boot backend
├── timebank-family/      # User-facing web client
└── timebank-backend/     # Admin dashboard
```

## Local Development

### 1. Backend

```bash
cd ai-end
mvn spring-boot:run
```

Default backend port: `8080`

Required local dependencies:

- MySQL
- Redis

Sensitive configuration has been converted to environment variables. Example variables:

```bash
DB_USERNAME=root
DB_PASSWORD=your_password
ALIYUN_OCR_ACCESS_KEY_ID=your_key
ALIYUN_OCR_ACCESS_KEY_SECRET=your_secret
```

### 2. User Client

```bash
cd timebank-family
npm install
npm run dev
```

Default frontend port: `3000`

### 3. Admin Dashboard

```bash
cd timebank-backend
npm install
npm run dev
```

Default frontend port: `3002`

## Resume-Oriented Highlights

- Designed and implemented a multi-module full-stack platform instead of a single isolated page application
- Combined business workflows across user client, admin client, and backend services in one codebase
- Implemented family proxy, dispute handling, ranking, reward management, OCR identity verification, and operations dashboards
- Restructured the repository into a unified monorepo for maintainability and cleaner delivery

## Roadmap

- Add deployment documentation and public demo link
- Add architecture diagram and product screenshots
- Add API overview and database schema summary at the repository root

## License

MIT
