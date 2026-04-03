# TimeBank Admin Dashboard

This package is the admin-side web application for the TimeBank Community project. It is used by operators and reviewers to manage users, tasks, disputes, system settings, reward workflows, and dashboard metrics.

## Scope

The admin dashboard focuses on internal operations rather than resident-facing interactions.

Main modules include:

- Dashboard and KPI views
- Identity audit and review workflows
- Family binding review
- Task monitoring and zombie task logs
- Arbitration and evidence archive
- Product and order management
- Ranking and reward monitoring
- System settings and operational controls

## Tech Stack

- React
- TypeScript
- Vite
- REST API integration with the Spring Boot backend in `../ai-end`

## Local Development

```bash
npm install
npm run dev
```

Default local port:

- `3002`

The Vite dev server proxies backend requests to:

- `http://localhost:8080`

## Directory Notes

```text
timebank-backend/
├── components/   # shared layout and UI components
├── pages/        # admin pages
├── services/     # API wrappers
├── utils/        # auth and request helpers
└── doc/          # interface and implementation docs
```

## Related Applications

- Backend: `../ai-end`
- User client: `../timebank-family`
