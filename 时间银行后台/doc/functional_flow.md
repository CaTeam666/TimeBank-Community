# Time Bank Admin - Functional Flow Chart

This document outlines the core functional flows of the Time Bank Administration System.

```mermaid
graph TD
    %% Styling
    classDef process fill:#e1f5fe,stroke:#01579b,stroke-width:2px;
    classDef decision fill:#fff9c4,stroke:#fbc02d,stroke-width:2px;
    classDef endEvent fill:#e8f5e9,stroke:#2e7d32,stroke-width:2px;
    classDef adminAction fill:#f3e5f5,stroke:#7b1fa2,stroke-width:2px,stroke-dasharray: 5 5;

    subgraph UserManagement["User Management Lifecycle"]
        direction TB
        Register(User Registration) --> IdentityCheck{Identity Audit?}
        IdentityCheck -- Pending --> AdminAudit[Admin Identity Review]:::adminAction
        AdminAudit -- PASS --> ActiveUser(Active User):::endEvent
        AdminAudit -- REJECT --> RejectUser(Audit Failed)
        
        ActiveUser --> RoleCheck{Is Child Agent?}:::decision
        RoleCheck -- Yes --> BindReq[Family Binding Request]
        BindReq --> AdminBind[Admin Binding Review]:::adminAction
        AdminBind -- PASS --> Bound(Relationship Bound):::endEvent
        AdminBind -- REJECT --> RejectBind(Binding Failed)
    end

    subgraph TaskSystem["Task Management & Service Flow"]
        direction TB
        Publish[Elder/Agent Publishes Task] --> TaskPool(Task Pool / Pending)
        TaskPool -->|Volunteer Claims| InProgress(In Progress)
        InProgress -->|Upload Evidence| ServiceDone(Service Delivered)
        ServiceDone --> WaitAccept(Waiting Acceptance)
        
        WaitAccept -- "Accepts" --> Complete(Task Completed):::endEvent
        WaitAccept -- "Rejects/Dispute" --> Arbitration[Arbitration Case]
        
        TaskPool -- "Timeout (>48h)" --> Zombie[Zombie Task Detected]
        Zombie --> AutoClose[Auto-Close & Refund]:::process
        
        Arbitration --> AdminJudge[Admin Adjudication]:::adminAction
        AdminJudge -- "Valid Service" --> Complete
        AdminJudge -- "Invalid Service" --> Cancelled(Task Cancelled/Refund):::endEvent
        
        Complete --> Settlement[Coin Settlement]:::process
    end

    subgraph MallSystem["Incentive & Mall System"]
        direction TB
        EarnCoins[Volunteer Earns Coins] --> BrowseMall[Browse Mall]
        BrowseMall --> Exchange[Redeem Product]
        Exchange --> Order(Order Pending Verification):::process
        Order --> Verify[Offline Code Verification]:::adminAction
        Verify --> OrderDone(Order Completed):::endEvent
        
        Monthly[Monthly Cycle] --> RankCalc[Calculate Rankings]:::process
        RankCalc --> RewardDist[Distribute Rewards]:::process
    end

    subgraph AdminOversight["Admin Core Functions"]
        Dashboard[Dashboard Monitor]
        Finance[Financial Analysis]
        Settings[System Settings]
        Evidence[Evidence Archive]
    end

    %% Connections between Subgraphs
    ActiveUser --> Publish
    ActiveUser -->|Volunteer| TaskPool
    Settlement --> EarnCoins
    Settlement --> Finance
    Exchange --> Finance
    ServiceDone -.-> Evidence
    
    %% Admin Interaction Hints
    AdminAudit -.-> Dashboard
    AdminJudge -.-> Dashboard
```

## Module Description

### 1. User Management
- **Identity Audit**: Review user real-name information and OCR data from ID cards.
- **Family Binding**: Review requests from Child Agents to bind with Elder accounts for managing tasks on their behalf.

### 2. Task Management
- **Lifecycle**: Controls the flow from task creation to completion.
- **Zombie Tasks**: Automatically identifies and closes tasks that have been pending for too long to ensure system liquidity.
- **Arbitration**: Handles disputes between Elders and Volunteers with evidence-based adjudication.

### 3. Incentive System
- **Mall**: Allows volunteers to redeem their "Time Coins" for real-world products or services.
- **Offline Verification**: Ensures physical delivery of goods through verification codes.
- **Ranking**: Monthly automated rewards for top-performing volunteers.

### 4. System Administration
- **Financial Analysis**: Monitors the economic health of the Time Bank (Minting vs. Burning of tokens).
- **Settings**: Global configuration of exchange rates, reward amounts, and fees.
