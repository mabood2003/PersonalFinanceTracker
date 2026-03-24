# Personal Finance Tracker

A full-stack personal finance application for tracking accounts, transactions, budgets, and spending analytics.

This project uses a Spring Boot REST API with JWT authentication and a React + TypeScript frontend. It supports day-to-day money tracking plus advanced features like transfer double-entry ledger handling, recurring transaction automation, and scheduled budget cycle rollover.

## Table of Contents

1. [What This Project Does](#what-this-project-does)
2. [Key Features](#key-features)
3. [Tech Stack](#tech-stack)
4. [Architecture](#architecture)
5. [Project Structure](#project-structure)
6. [Getting Started](#getting-started)
7. [Run with Docker (Recommended)](#run-with-docker-recommended)
8. [Run Locally (Dev Mode)](#run-locally-dev-mode)
9. [First-Time Walkthrough](#first-time-walkthrough)
10. [API Overview](#api-overview)
11. [Recurring Jobs and Scheduling](#recurring-jobs-and-scheduling)
12. [Testing](#testing)
13. [Troubleshooting](#troubleshooting)
14. [Notes for Contributors](#notes-for-contributors)

## What This Project Does

The app helps users:

- Create and manage financial accounts (checking, savings, credit card, cash, investment)
- Record income, expenses, and account-to-account transfers
- Set category budgets and monitor budget utilization
- View monthly summaries, category spending breakdowns, and spending trends
- Automate recurring transactions and budget cycle rollover

The frontend currently provides pages for Authentication, Dashboard, Accounts, Transactions, and Budgets. Recurring transactions are available through backend API endpoints (Swagger/Postman), and can be integrated into the UI later.

## Key Features

### Authentication and Security

- Register/login with JWT-based auth
- Stateless Spring Security setup
- Protected API routes by default
- CORS configured for local frontend origin (`http://localhost:5173`)

### Accounts

- CRUD for account records
- Supports `CHECKING`, `SAVINGS`, `CREDIT_CARD`, `CASH`, `INVESTMENT`
- Monetary values stored with `BigDecimal`
- Credit card accounts are treated as liabilities in frontend net-worth calculation

### Transactions

- CRUD for `INCOME`, `EXPENSE`, and `TRANSFER`
- Dynamic filtering, search, sorting, and pagination
- Atomic balance updates with `@Transactional`

### Transfers (Double-Entry Ledger + Idempotency)

- Transfers create two linked ledger entries:
  - `OUT` leg (source account debit)
  - `IN` leg (destination account credit)
- `idempotencyKey` supported for safe retry handling
- Unique transfer constraints enforced at DB level
- Transfer updates/deletes correctly reverse and re-apply balance effects

### Budgets

- Budget CRUD by category and period (`WEEKLY`, `MONTHLY`)
- Budget progress calculation:
  - amount spent
  - amount remaining
  - percent used
  - status (`ON_TRACK`, `WARNING`, `EXCEEDED`)
- Optional auto-renew cycle support

### Recurring Transactions and Scheduled Budget Cycles

- Define recurring income/expense/transfer rules (`DAILY`, `WEEKLY`, `MONTHLY`)
- Scheduled job auto-posts due recurring entries
- Scheduled job rolls budget cycles forward when expired

### Analytics

- Monthly summary (income, expenses, net savings, savings rate, top category)
- Category breakdown over date range
- Multi-month spending trend
- Implemented with JPQL aggregation queries

### Data and Schema Management

- PostgreSQL primary database
- Flyway SQL migrations (`V1` to `V9`)
- Default categories seeded automatically

## Tech Stack

### Backend

- Java 21
- Spring Boot 3.3
- Spring Web, Spring Data JPA, Spring Security, Validation
- JWT (`jjwt`)
- Flyway
- PostgreSQL
- OpenAPI/Swagger (`springdoc`)
- MapStruct + Lombok

### Frontend

- React 18
- TypeScript
- Vite
- Tailwind CSS
- Axios
- Recharts
- React Router

### DevOps / Runtime

- Docker and Docker Compose

## Architecture

Backend follows a layered architecture:

- Controller layer: HTTP endpoints and request/response handling
- Service layer: business logic and transaction orchestration
- Repository layer: persistence and query logic
- Mapper layer: entity-to-DTO conversions

Core backend design points:

- DTO-first API design
- Centralized exception handling (`GlobalExceptionHandler`)
- JPA Specifications for dynamic transaction filtering
- JPQL aggregation queries for analytics
- Flyway for schema versioning and repeatable environment setup

## Project Structure

```text
PersonalFinanceTracker/
  backend/                       # Spring Boot API
    src/main/java/com/moabood/financetracker/
      auth/                      # Login/register
      account/                   # Account domain
      transaction/               # Transactions + transfers
      budget/                    # Budget domain + scheduler
      recurring/                 # Recurring transaction domain + scheduler
      analytics/                 # Reporting endpoints
      config/                    # Security/JWT/OpenAPI/CORS
      common/                    # Shared response + error handling
      mapper/                    # DTO mappers
    src/main/resources/
      application*.yml           # Profile-specific config
      db/migration/              # Flyway SQL migrations
  frontend/                      # React app
    src/
      api/                       # API clients
      pages/                     # Main views
      components/                # Reusable UI parts
      context/                   # Auth state management
  docker-compose.yml             # Full stack local runtime
```

## Getting Started

### Prerequisites

Choose one path:

- Docker path:
  - Docker Desktop (or Docker Engine + Compose plugin)
- Local path:
  - Java 21
  - Maven 3.9+
  - Node.js 20+
  - PostgreSQL 16+

Default ports used:

- Frontend: `5173`
- Backend API: `8080`
- PostgreSQL: `5432`

## Run with Docker (Recommended)

From repository root:

```bash
docker compose up --build
```

Then open:

- Frontend: `http://localhost:5173`
- API docs (Swagger): `http://localhost:8080/swagger-ui.html`

To stop:

```bash
docker compose down
```

To stop and remove DB volume:

```bash
docker compose down -v
```

## Run Locally (Dev Mode)

### 1. Start PostgreSQL

Create database:

```sql
CREATE DATABASE finance_tracker;
```

Default dev credentials expected by backend profile:

- username: `postgres`
- password: `postgres`
- url: `jdbc:postgresql://localhost:5432/finance_tracker`

### 2. Start backend

```bash
cd backend
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

Backend base URL:

- `http://localhost:8080/api/v1`

Swagger UI:

- `http://localhost:8080/swagger-ui.html`

### 3. Start frontend

In a second terminal:

```bash
cd frontend
npm install
npm run dev
```

Frontend URL:

- `http://localhost:5173`

## First-Time Walkthrough

After startup, this is the fastest way to verify everything works:

1. Open `http://localhost:5173` and register a new user.
2. Create at least one account in **Accounts**.
3. Add income/expense/transfer entries in **Transactions**.
4. Create a budget in **Budgets**.
5. Open **Dashboard** to verify summary cards and charts update.
6. (Optional) Open Swagger and create recurring transactions through `/api/v1/recurring-transactions`.

## Environment Variables

### Backend

- `SPRING_PROFILES_ACTIVE` (example: `dev`, `prod`)
- `JWT_SECRET` (set a strong value in non-dev environments)
- `DATABASE_URL` (used in `prod` profile)
- `DATABASE_USERNAME` (used in `prod` profile)
- `DATABASE_PASSWORD` (used in `prod` profile)
- `RECURRING_TRANSACTIONS_CRON` (optional cron override)
- `BUDGET_CYCLES_CRON` (optional cron override)

Default cron values in `application.yml`:

- recurring transactions: `0 0 1 * * *` (daily at 01:00)
- budget cycles: `0 10 1 * * *` (daily at 01:10)

## API Overview

Base path: `/api/v1`

Public auth endpoints:

- `POST /auth/register`
- `POST /auth/login`

Protected endpoints:

- `GET /users/me`
- `GET|POST|PUT|DELETE /accounts`
- `GET|POST|PUT|DELETE /transactions`
- `GET|POST|PUT|DELETE /budgets`
- `GET|POST|PUT|DELETE /categories`
- `GET /analytics/monthly-summary`
- `GET /analytics/category-breakdown`
- `GET /analytics/spending-trend`
- `GET|POST|PUT|DELETE /recurring-transactions`

For complete request/response schemas, use Swagger at:

- `http://localhost:8080/swagger-ui.html`

## Recurring Jobs and Scheduling

Scheduling is enabled globally (`@EnableScheduling`) in the Spring Boot application.

- `RecurringTransactionScheduler` posts due recurring transactions.
- `BudgetCycleScheduler` advances expired auto-renew budget cycles.

Notes:

- Recurring transfer entries generate deterministic idempotency keys using a stored prefix + run date.
- If a recurring item reaches `endDate`, it is marked inactive.

## Testing

### Backend tests

```bash
cd backend
mvn test
```

Backend test stack includes:

- JUnit 5
- Mockito
- Spring Boot test slices (`WebMvcTest`, `DataJpaTest`)
- H2 in-memory DB for test profile

### Frontend validation

No dedicated frontend test suite is configured yet. You can run a production build/typecheck:

```bash
cd frontend
npm install
npm run build
```

## Troubleshooting

### Frontend cannot reach backend

- Confirm backend is running on `http://localhost:8080`
- Confirm frontend is running on `http://localhost:5173`
- Check browser console/network tab for 401/404/CORS errors

### JWT/CORS issues

- Ensure requests include `Authorization: Bearer <token>`
- Backend CORS currently allows `http://localhost:5173`

### Database connection errors

- Verify Postgres is running and credentials match `application-dev.yml`
- Confirm DB exists: `finance_tracker`

### Flyway migration failures

- Inspect migration history table `flyway_schema_history`
- Ensure DB user has schema migration permissions
- Reset local DB if needed during development (`docker compose down -v` then restart)

## Notes for Contributors

- Keep monetary operations in `BigDecimal`
- Preserve transaction atomicity with `@Transactional`
- For transfer changes, always validate both ledger legs and account balance effects
- Add/update tests for any business logic changes in services
