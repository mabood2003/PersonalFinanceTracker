# 💰 Finance Tracker

Personal finance management application built with Spring Boot 3 and React.

## Tech Stack
- **Backend**: Java 21, Spring Boot 3.3, Spring Security (JWT), Spring Data JPA, PostgreSQL, Flyway
- **Frontend**: React 18, TypeScript, Tailwind CSS, Recharts
- **Infrastructure**: Docker Compose

## Features
- JWT-authenticated REST API
- Multi-account tracking (chequing, savings, credit card, cash, investment)
- Transaction management with filtering, search, and pagination
- Budget tracking with real-time spending progress
- Analytics dashboard (monthly summaries, category breakdown, spending trends)

## Quick Start

```bash
# Clone and start everything
git clone https://github.com/moabood/finance-tracker.git
cd finance-tracker
docker compose up -d

# Frontend: http://localhost:5173
# API Docs: http://localhost:8080/swagger-ui.html
```

## API Documentation
Interactive Swagger UI available at `/swagger-ui.html` when running.

## Architecture
Layered architecture: **Controller → Service → Repository**
- DTO pattern — entities never leave the service layer
- Centralized error handling via `GlobalExceptionHandler`
- JPA Specifications (Criteria API) for dynamic transaction filtering
- Flyway for all schema migrations — no Hibernate DDL in dev/prod

## Testing
```bash
cd backend
./mvnw test
```
