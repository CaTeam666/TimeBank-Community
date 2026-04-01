# Implementation Plan - Real Login Interface

## Goal Description
Transition the authentication system from frontend mock to real backend API integration.

## Proposed Changes

### Documentation
#### [MODIFY] [api_docs.md](file:///c:/Users/CaTeam/Desktop/时间银行后台/doc/api_docs.md)
- Finalize the API specification for login (POST /api/auth/login).

### Services
#### [MODIFY] [auth.ts](file:///c:/Users/CaTeam/Desktop/时间银行后台/services/auth.ts)
- Remove `setTimeout` and mock logic.
- Implement `fetch` request to `/api/auth/login`.
- Handle token storage (localStorage) and error parsing.

### Configuration
#### [MODIFY] [vite.config.ts](file:///c:/Users/CaTeam/Desktop/时间银行后台/vite.config.ts)
- Configure `server.proxy` to forward `/api` requests to `http://localhost:8080`.
- **CRITICAL**: Add `rewrite: (path) => path.replace(/^\/api/, '')` to ensure backend receives `/auth/login` instead of `/api/auth/login`.

### Documentation
#### [MODIFY] [api_docs.md](file:///c:/Users/CaTeam/Desktop/时间银行后台/doc/api_docs.md)
- Clarify that frontend calls `/api/auth/login` and proxy maps it to `/auth/login`.

## Verification Plan
### Manual Verification
- Since the backend might not be running yet, verification will involve checking the Network tab in browser to see the correct request being sent to `/api/auth/login`.
