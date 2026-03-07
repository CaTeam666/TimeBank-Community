# Implementation Plan - Fix User Role Filter

The user reported that filtering users by role fails after changing the backend database schema for `role` to an ENUM with values ('老人', '志愿者', '子女代理人'). The frontend was likely sending the previous English enum values (e.g., 'ELDER').

## Proposed Changes

### Logic Fixes

#### [types.ts]
- **Issue**: The `UserRole` enum was defined with English string values (e.g., `ELDER = 'ELDER'`), which caused the frontend to send 'ELDER' as the query parameter.
- **Fix**: Update the `UserRole` enum values to match the Chinese strings expected by the backend ('老人', '志愿者', '子女代理人'). This ensures that:
    1. The `UserList` filter dropdown sends the correct Chinese string to the API.
    2. The `UserList` table correctly matches the role returned from the API (which is now Chinese) to the badge display logic.
    3. The mock data (using `UserRole` enum) automatically updates to use Chinese strings, maintaining consistency.

## Verification Plan

### Automated Tests
- None (No existing test suite was provided or modified).

### Manual Verification
- **Role Filter**:
    1. Open the User List page.
    2. Select "老人" in the role filter dropdown.
    3. Verify that the network request sends `role=老人` (encoded as `role=%E8%80%81%E4%BA%BA`).
    4. Verify that the list updates to show only users with role "老人".
- **Role Display**:
    1. Check that the role badges in the user table correctly display colors and text (e.g., "老人 (夕阳红)" badge for elderly users).
