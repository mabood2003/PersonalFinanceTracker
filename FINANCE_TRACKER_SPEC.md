# Personal Finance Tracker — Full Project Specification

> **Purpose**: This document is a complete, unambiguous specification for building a Personal Finance Tracker application. It is designed to be handed directly to an AI coding assistant (Claude Code) or a developer and executed without further clarification. Every entity, field, endpoint, config choice, validation rule, and file is specified.

> **Goal**: Demonstrate production-grade Spring Boot competence for job interviews at SOTI and CIBC. This is NOT a tutorial project — it should look like something a professional built.

---

## Tech Stack (Non-Negotiable)

### Backend
- **Java 21** (LTS)
- **Spring Boot 3.3.x** (latest stable 3.x)
- **Spring Data JPA** with **Hibernate** (ORM)
- **Spring Security 6** with **JWT** (stateless auth)
- **PostgreSQL 16** (production DB)
- **H2** (in-memory, for tests only)
- **Flyway** (database migrations — NOT Liquibase, NOT auto-ddl)
- **Maven** (build tool — NOT Gradle)
- **MapStruct** (DTO ↔ Entity mapping)
- **Lombok** (boilerplate reduction)
- **SpringDoc OpenAPI 2.x** (Swagger UI auto-generated docs)
- **Jakarta Bean Validation** (request validation)

### Frontend
- **React 18** with **Vite** (NOT Create React App — it's dead)
- **TypeScript** (strict mode)
- **Tailwind CSS** (styling)
- **Recharts** (charts/graphs)
- **Axios** (HTTP client)
- **React Router v6** (routing)

### Infrastructure
- **Docker Compose** (PostgreSQL + backend + frontend)
- **Git** with conventional commits

---

## Project Structure

```
finance-tracker/
├── docker-compose.yml
├── README.md
├── .gitignore
│
├── backend/
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/
│       ├── main/
│       │   ├── java/com/moabood/financetracker/
│       │   │   ├── FinanceTrackerApplication.java
│       │   │   │
│       │   │   ├── config/
│       │   │   │   ├── SecurityConfig.java
│       │   │   │   ├── JwtAuthenticationFilter.java
│       │   │   │   ├── JwtService.java
│       │   │   │   ├── CorsConfig.java
│       │   │   │   └── OpenApiConfig.java
│       │   │   │
│       │   │   ├── auth/
│       │   │   │   ├── AuthController.java
│       │   │   │   ├── AuthService.java
│       │   │   │   ├── RegisterRequest.java
│       │   │   │   ├── LoginRequest.java
│       │   │   │   └── AuthResponse.java
│       │   │   │
│       │   │   ├── user/
│       │   │   │   ├── User.java                    (entity)
│       │   │   │   ├── UserRepository.java
│       │   │   │   ├── UserService.java
│       │   │   │   ├── UserController.java
│       │   │   │   └── UserDto.java
│       │   │   │
│       │   │   ├── account/
│       │   │   │   ├── Account.java                  (entity)
│       │   │   │   ├── AccountType.java              (enum)
│       │   │   │   ├── AccountRepository.java
│       │   │   │   ├── AccountService.java
│       │   │   │   ├── AccountController.java
│       │   │   │   ├── AccountDto.java
│       │   │   │   └── CreateAccountRequest.java
│       │   │   │
│       │   │   ├── transaction/
│       │   │   │   ├── Transaction.java              (entity)
│       │   │   │   ├── TransactionType.java          (enum)
│       │   │   │   ├── TransactionRepository.java
│       │   │   │   ├── TransactionService.java
│       │   │   │   ├── TransactionController.java
│       │   │   │   ├── TransactionDto.java
│       │   │   │   ├── CreateTransactionRequest.java
│       │   │   │   └── TransactionFilterRequest.java
│       │   │   │
│       │   │   ├── category/
│       │   │   │   ├── Category.java                 (entity)
│       │   │   │   ├── CategoryRepository.java
│       │   │   │   ├── CategoryService.java
│       │   │   │   ├── CategoryController.java
│       │   │   │   ├── CategoryDto.java
│       │   │   │   └── CreateCategoryRequest.java
│       │   │   │
│       │   │   ├── budget/
│       │   │   │   ├── Budget.java                   (entity)
│       │   │   │   ├── BudgetPeriod.java             (enum)
│       │   │   │   ├── BudgetRepository.java
│       │   │   │   ├── BudgetService.java
│       │   │   │   ├── BudgetController.java
│       │   │   │   ├── BudgetDto.java
│       │   │   │   ├── BudgetProgressDto.java
│       │   │   │   └── CreateBudgetRequest.java
│       │   │   │
│       │   │   ├── analytics/
│       │   │   │   ├── AnalyticsController.java
│       │   │   │   ├── AnalyticsService.java
│       │   │   │   ├── MonthlySummaryDto.java
│       │   │   │   ├── CategoryBreakdownDto.java
│       │   │   │   └── SpendingTrendDto.java
│       │   │   │
│       │   │   ├── common/
│       │   │   │   ├── GlobalExceptionHandler.java
│       │   │   │   ├── ErrorResponse.java
│       │   │   │   ├── PagedResponse.java
│       │   │   │   └── BaseEntity.java
│       │   │   │
│       │   │   └── mapper/
│       │   │       ├── UserMapper.java
│       │   │       ├── AccountMapper.java
│       │   │       ├── TransactionMapper.java
│       │   │       ├── CategoryMapper.java
│       │   │       └── BudgetMapper.java
│       │   │
│       │   └── resources/
│       │       ├── application.yml
│       │       ├── application-dev.yml
│       │       ├── application-test.yml
│       │       ├── application-prod.yml
│       │       └── db/migration/
│       │           ├── V1__create_users_table.sql
│       │           ├── V2__create_accounts_table.sql
│       │           ├── V3__create_categories_table.sql
│       │           ├── V4__create_transactions_table.sql
│       │           ├── V5__create_budgets_table.sql
│       │           └── V6__seed_default_categories.sql
│       │
│       └── test/
│           └── java/com/moabood/financetracker/
│               ├── auth/
│               │   └── AuthControllerTest.java        (@WebMvcTest)
│               ├── transaction/
│               │   ├── TransactionServiceTest.java    (Mockito unit test)
│               │   ├── TransactionControllerTest.java (@WebMvcTest)
│               │   └── TransactionRepositoryTest.java (@DataJpaTest)
│               ├── budget/
│               │   └── BudgetServiceTest.java         (Mockito unit test)
│               └── analytics/
│                   └── AnalyticsServiceTest.java      (Mockito unit test)
│
└── frontend/
    ├── package.json
    ├── tsconfig.json
    ├── vite.config.ts
    ├── tailwind.config.js
    ├── Dockerfile
    ├── index.html
    └── src/
        ├── main.tsx
        ├── App.tsx
        ├── api/
        │   ├── axiosConfig.ts          (base URL, JWT interceptor)
        │   ├── authApi.ts
        │   ├── accountApi.ts
        │   ├── transactionApi.ts
        │   ├── budgetApi.ts
        │   └── analyticsApi.ts
        ├── context/
        │   └── AuthContext.tsx          (JWT storage, login/logout/refresh)
        ├── components/
        │   ├── Layout.tsx              (sidebar + main content)
        │   ├── ProtectedRoute.tsx
        │   ├── TransactionTable.tsx
        │   ├── TransactionForm.tsx
        │   ├── BudgetCard.tsx
        │   ├── SpendingChart.tsx        (Recharts pie chart by category)
        │   ├── TrendChart.tsx           (Recharts line chart monthly)
        │   └── AccountCard.tsx
        └── pages/
            ├── LoginPage.tsx
            ├── RegisterPage.tsx
            ├── DashboardPage.tsx        (summary cards + charts)
            ├── TransactionsPage.tsx     (table + filters + add form)
            ├── BudgetsPage.tsx          (budget cards with progress bars)
            └── AccountsPage.tsx
```

---

## Database Schema (Flyway Migrations)

### V1__create_users_table.sql

```sql
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    first_name      VARCHAR(100) NOT NULL,
    last_name       VARCHAR(100) NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);
```

### V2__create_accounts_table.sql

```sql
CREATE TABLE accounts (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name            VARCHAR(100) NOT NULL,
    account_type    VARCHAR(20) NOT NULL,  -- CHECKING, SAVINGS, CREDIT_CARD, CASH, INVESTMENT
    balance         DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency        VARCHAR(3) NOT NULL DEFAULT 'CAD',
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_accounts_user_id ON accounts(user_id);
```

### V3__create_categories_table.sql

```sql
CREATE TABLE categories (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT REFERENCES users(id) ON DELETE CASCADE,  -- NULL = system default
    name            VARCHAR(50) NOT NULL,
    icon            VARCHAR(30),           -- emoji or icon name for frontend
    color           VARCHAR(7),            -- hex color code e.g. #FF5733
    is_default      BOOLEAN NOT NULL DEFAULT FALSE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_categories_user_id ON categories(user_id);
```

### V4__create_transactions_table.sql

```sql
CREATE TABLE transactions (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id      BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    category_id     BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    type            VARCHAR(10) NOT NULL,  -- INCOME, EXPENSE, TRANSFER
    amount          DECIMAL(15,2) NOT NULL,
    description     VARCHAR(255),
    merchant        VARCHAR(100),
    transaction_date DATE NOT NULL,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_category_id ON transactions(category_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_user_date ON transactions(user_id, transaction_date);
CREATE INDEX idx_transactions_user_type_date ON transactions(user_id, type, transaction_date);
```

### V5__create_budgets_table.sql

```sql
CREATE TABLE budgets (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id     BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    amount_limit    DECIMAL(15,2) NOT NULL,
    period          VARCHAR(10) NOT NULL DEFAULT 'MONTHLY',  -- MONTHLY, WEEKLY
    start_date      DATE NOT NULL,
    end_date        DATE,                  -- NULL = ongoing/recurring
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, category_id, period)
);

CREATE INDEX idx_budgets_user_id ON budgets(user_id);
```

### V6__seed_default_categories.sql

```sql
INSERT INTO categories (name, icon, color, is_default) VALUES
    ('Groceries',       '🛒', '#4CAF50', TRUE),
    ('Dining Out',      '🍽️', '#FF9800', TRUE),
    ('Transportation',  '🚗', '#2196F3', TRUE),
    ('Housing',         '🏠', '#9C27B0', TRUE),
    ('Utilities',       '💡', '#607D8B', TRUE),
    ('Entertainment',   '🎬', '#E91E63', TRUE),
    ('Healthcare',      '🏥', '#00BCD4', TRUE),
    ('Shopping',        '🛍️', '#FF5722', TRUE),
    ('Education',       '📚', '#3F51B5', TRUE),
    ('Salary',          '💰', '#4CAF50', TRUE),
    ('Freelance',       '💻', '#8BC34A', TRUE),
    ('Investment',      '📈', '#FFC107', TRUE),
    ('Other Income',    '💵', '#CDDC39', TRUE),
    ('Other Expense',   '📋', '#795548', TRUE);
```

---

## Entity Definitions (JPA)

### BaseEntity.java (Mapped Superclass)

```java
@MappedSuperclass
@Getter
@Setter
public abstract class BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;
}
```

### User.java

```java
@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User extends BaseEntity implements UserDetails {

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "first_name", nullable = false, length = 100)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 100)
    private String lastName;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Account> accounts = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Transaction> transactions = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Budget> budgets = new ArrayList<>();

    // UserDetails implementation
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() { return passwordHash; }

    @Override
    public String getUsername() { return email; }
}
```

### Account.java

```java
@Entity
@Table(name = "accounts")
@Getter @Setter @NoArgsConstructor
public class Account extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_type", nullable = false, length = 20)
    private AccountType accountType;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    @Column(nullable = false, length = 3)
    private String currency = "CAD";
}
```

### AccountType.java (enum)

```java
public enum AccountType {
    CHECKING, SAVINGS, CREDIT_CARD, CASH, INVESTMENT
}
```

### Category.java

```java
@Entity
@Table(name = "categories")
@Getter @Setter @NoArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")  // nullable — NULL means system default
    private User user;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 30)
    private String icon;

    @Column(length = 7)
    private String color;

    @Column(name = "is_default", nullable = false)
    private boolean isDefault = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
}
```

### Transaction.java

```java
@Entity
@Table(name = "transactions")
@Getter @Setter @NoArgsConstructor
public class Transaction extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private TransactionType type;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;

    @Column(length = 255)
    private String description;

    @Column(length = 100)
    private String merchant;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;
}
```

### TransactionType.java (enum)

```java
public enum TransactionType {
    INCOME, EXPENSE, TRANSFER
}
```

### Budget.java

```java
@Entity
@Table(name = "budgets",
       uniqueConstraints = @UniqueConstraint(columns = {"user_id", "category_id", "period"}))
@Getter @Setter @NoArgsConstructor
public class Budget extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(name = "amount_limit", nullable = false, precision = 15, scale = 2)
    private BigDecimal amountLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private BudgetPeriod period = BudgetPeriod.MONTHLY;

    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;  // null = ongoing
}
```

### BudgetPeriod.java (enum)

```java
public enum BudgetPeriod {
    WEEKLY, MONTHLY
}
```

---

## REST API Endpoints (Complete Specification)

**Base URL**: `/api/v1`
**All endpoints except auth require JWT in `Authorization: Bearer <token>` header.**
**All endpoints are user-scoped — users can only access their own data. Enforce this in the service layer, not just by trusting the URL.**

### Authentication

| Method | Path | Request Body | Response | Status |
|--------|------|-------------|----------|--------|
| POST | `/auth/register` | `RegisterRequest` | `AuthResponse` | 201 |
| POST | `/auth/login` | `LoginRequest` | `AuthResponse` | 200 |

**RegisterRequest**:
```json
{
  "email": "mo@example.com",          // @NotBlank, @Email
  "password": "SecurePass123!",       // @NotBlank, @Size(min=8)
  "firstName": "Mo",                  // @NotBlank
  "lastName": "Abood"                 // @NotBlank
}
```

**LoginRequest**:
```json
{
  "email": "mo@example.com",          // @NotBlank, @Email
  "password": "SecurePass123!"        // @NotBlank
}
```

**AuthResponse**:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "email": "mo@example.com",
  "firstName": "Mo",
  "lastName": "Abood"
}
```

### Accounts

| Method | Path | Request Body | Response | Status |
|--------|------|-------------|----------|--------|
| GET | `/accounts` | — | `List<AccountDto>` | 200 |
| GET | `/accounts/{id}` | — | `AccountDto` | 200 |
| POST | `/accounts` | `CreateAccountRequest` | `AccountDto` | 201 |
| PUT | `/accounts/{id}` | `CreateAccountRequest` | `AccountDto` | 200 |
| DELETE | `/accounts/{id}` | — | — | 204 |

**CreateAccountRequest**:
```json
{
  "name": "TD Chequing",              // @NotBlank, @Size(max=100)
  "accountType": "CHECKING",          // @NotNull, enum validation
  "balance": 2500.00,                 // @NotNull
  "currency": "CAD"                   // @Size(min=3, max=3), default "CAD"
}
```

**AccountDto**:
```json
{
  "id": 1,
  "name": "TD Chequing",
  "accountType": "CHECKING",
  "balance": 2500.00,
  "currency": "CAD",
  "createdAt": "2026-03-22T10:00:00Z"
}
```

### Categories

| Method | Path | Request Body | Response | Status |
|--------|------|-------------|----------|--------|
| GET | `/categories` | — | `List<CategoryDto>` | 200 |
| POST | `/categories` | `CreateCategoryRequest` | `CategoryDto` | 201 |
| PUT | `/categories/{id}` | `CreateCategoryRequest` | `CategoryDto` | 200 |
| DELETE | `/categories/{id}` | — | — | 204 |

**GET /categories** returns both system defaults (is_default=true) AND user's custom categories.
**Users cannot modify or delete system default categories.** Enforce in service layer.

**CreateCategoryRequest**:
```json
{
  "name": "Subscriptions",            // @NotBlank, @Size(max=50)
  "icon": "📱",                       // optional
  "color": "#673AB7"                  // optional, @Pattern(regexp="^#[0-9A-Fa-f]{6}$")
}
```

### Transactions

| Method | Path | Request Body | Response | Status |
|--------|------|-------------|----------|--------|
| GET | `/transactions` | query params | `PagedResponse<TransactionDto>` | 200 |
| GET | `/transactions/{id}` | — | `TransactionDto` | 200 |
| POST | `/transactions` | `CreateTransactionRequest` | `TransactionDto` | 201 |
| PUT | `/transactions/{id}` | `CreateTransactionRequest` | `TransactionDto` | 200 |
| DELETE | `/transactions/{id}` | — | — | 204 |

**GET /transactions query parameters** (all optional):
- `page` (int, default 0)
- `size` (int, default 20, max 100)
- `sort` (string, default "transactionDate,desc")
- `type` (INCOME | EXPENSE | TRANSFER)
- `categoryId` (Long)
- `accountId` (Long)
- `startDate` (yyyy-MM-dd)
- `endDate` (yyyy-MM-dd)
- `minAmount` (BigDecimal)
- `maxAmount` (BigDecimal)
- `search` (String — searches description and merchant fields, case-insensitive LIKE)

**Implement filtering using Spring Data JPA Specifications (Criteria API)**. Do NOT build query strings manually.

**CreateTransactionRequest**:
```json
{
  "accountId": 1,                     // @NotNull
  "categoryId": 3,                    // optional (nullable)
  "type": "EXPENSE",                  // @NotNull
  "amount": 45.99,                    // @NotNull, @Positive
  "description": "Weekly groceries",  // optional, @Size(max=255)
  "merchant": "No Frills",            // optional, @Size(max=100)
  "transactionDate": "2026-03-22"     // @NotNull
}
```

**CRITICAL BUSINESS LOGIC**: When a transaction is created, updated, or deleted, the associated account balance MUST be updated accordingly. This must be done within a @Transactional method in the service layer. Specifically:
- EXPENSE: subtract amount from account balance
- INCOME: add amount to account balance
- TRANSFER: not implemented in v1 (reject with 400 if attempted)
- UPDATE: reverse the old transaction effect, apply the new one
- DELETE: reverse the transaction effect

**PagedResponse<T>**:
```json
{
  "content": [...],
  "page": 0,
  "size": 20,
  "totalElements": 147,
  "totalPages": 8,
  "last": false
}
```

### Budgets

| Method | Path | Request Body | Response | Status |
|--------|------|-------------|----------|--------|
| GET | `/budgets` | — | `List<BudgetProgressDto>` | 200 |
| POST | `/budgets` | `CreateBudgetRequest` | `BudgetDto` | 201 |
| PUT | `/budgets/{id}` | `CreateBudgetRequest` | `BudgetDto` | 200 |
| DELETE | `/budgets/{id}` | — | — | 204 |

**GET /budgets** does NOT just return the raw budget data. It returns each budget WITH its current spending progress calculated. This is the key feature.

**CreateBudgetRequest**:
```json
{
  "categoryId": 1,                    // @NotNull
  "amountLimit": 500.00,              // @NotNull, @Positive
  "period": "MONTHLY",                // @NotNull
  "startDate": "2026-03-01"           // @NotNull
}
```

**BudgetProgressDto** (what GET /budgets returns):
```json
{
  "id": 1,
  "category": {
    "id": 1,
    "name": "Groceries",
    "icon": "🛒",
    "color": "#4CAF50"
  },
  "amountLimit": 500.00,
  "amountSpent": 312.47,
  "amountRemaining": 187.53,
  "percentUsed": 62.5,
  "period": "MONTHLY",
  "startDate": "2026-03-01",
  "status": "ON_TRACK"               // ON_TRACK | WARNING (>75%) | EXCEEDED (>100%)
}
```

**Budget spending calculation**: Sum all EXPENSE transactions for the authenticated user, matching the budget's category_id, within the current period (current month for MONTHLY, current week for WEEKLY).

### Analytics

| Method | Path | Query Params | Response | Status |
|--------|------|-------------|----------|--------|
| GET | `/analytics/monthly-summary` | `year`, `month` | `MonthlySummaryDto` | 200 |
| GET | `/analytics/category-breakdown` | `startDate`, `endDate`, `type` | `List<CategoryBreakdownDto>` | 200 |
| GET | `/analytics/spending-trend` | `months` (default 6) | `List<SpendingTrendDto>` | 200 |

**MonthlySummaryDto**:
```json
{
  "year": 2026,
  "month": 3,
  "totalIncome": 4500.00,
  "totalExpenses": 2847.33,
  "netSavings": 1652.67,
  "savingsRate": 36.7,
  "transactionCount": 47,
  "topCategory": {
    "name": "Groceries",
    "amount": 623.45
  }
}
```

**CategoryBreakdownDto**:
```json
{
  "categoryId": 1,
  "categoryName": "Groceries",
  "categoryIcon": "🛒",
  "categoryColor": "#4CAF50",
  "totalAmount": 623.45,
  "percentage": 21.9,
  "transactionCount": 12
}
```

**SpendingTrendDto**:
```json
{
  "year": 2026,
  "month": 3,
  "totalIncome": 4500.00,
  "totalExpenses": 2847.33
}
```

**Analytics queries MUST use custom @Query with JPQL aggregations**, not loading all transactions into memory and computing in Java. Use SUM, GROUP BY, etc. in the query.

---

## Security Configuration

### JWT Setup
- **Algorithm**: HMAC-SHA256
- **Secret**: loaded from `app.jwt.secret` property (env var in prod)
- **Expiration**: 24 hours (configurable via `app.jwt.expiration-ms`)
- **Token contains**: subject = user email, issuedAt, expiration

### SecurityFilterChain Configuration
```
- POST /api/v1/auth/** → permitAll
- GET /swagger-ui/**, /v3/api-docs/** → permitAll
- Everything else → authenticated
- Session management: STATELESS
- CORS: allow frontend origin
- CSRF: disabled (stateless JWT)
```

### JwtAuthenticationFilter (extends OncePerRequestFilter)
1. Extract token from Authorization header
2. Validate token (not expired, valid signature)
3. Load UserDetails from UserDetailsService
4. Set SecurityContextHolder authentication
5. Continue filter chain

### Password: BCryptPasswordEncoder (Spring Security default)

---

## Error Handling (GlobalExceptionHandler)

**Every error response uses this format**:
```json
{
  "timestamp": "2026-03-22T10:30:00Z",
  "status": 404,
  "error": "Not Found",
  "message": "Transaction not found with id: 99",
  "path": "/api/v1/transactions/99"
}
```

**Handle these exceptions**:
| Exception | Status | When |
|-----------|--------|------|
| `ResourceNotFoundException` (custom) | 404 | Entity not found by ID |
| `AccessDeniedException` | 403 | User tries to access another user's data |
| `MethodArgumentNotValidException` | 400 | @Valid fails — return field-level errors |
| `DataIntegrityViolationException` | 409 | Unique constraint violation (duplicate budget, email, etc.) |
| `BadCredentialsException` | 401 | Wrong email/password at login |
| `HttpMessageNotReadableException` | 400 | Malformed JSON, invalid enum value |
| `Exception` (catch-all) | 500 | Unexpected errors — log full stack trace, return generic message |

For validation errors (400), include field-level detail:
```json
{
  "timestamp": "...",
  "status": 400,
  "error": "Validation Failed",
  "message": "Request validation failed",
  "fieldErrors": {
    "email": "must be a valid email address",
    "amount": "must be greater than 0"
  }
}
```

---

## Application Configuration

### application.yml (shared)
```yaml
spring:
  application:
    name: finance-tracker

  jpa:
    open-in-view: false                   # CRITICAL: disable OSIV anti-pattern
    hibernate:
      ddl-auto: validate                  # Flyway handles schema, Hibernate only validates
    properties:
      hibernate:
        format_sql: true

  jackson:
    serialization:
      write-dates-as-timestamps: false    # ISO 8601 date format
    default-property-inclusion: non_null

app:
  jwt:
    secret: ${JWT_SECRET:default-dev-secret-change-in-production-this-must-be-at-least-256-bits}
    expiration-ms: 86400000               # 24 hours
```

### application-dev.yml
```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/finance_tracker
    username: postgres
    password: postgres
  jpa:
    show-sql: true

logging:
  level:
    com.moabood.financetracker: DEBUG
    org.springframework.security: DEBUG

springdoc:
  swagger-ui:
    path: /swagger-ui.html
```

### application-test.yml
```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
  jpa:
    hibernate:
      ddl-auto: create-drop              # OK for tests
  flyway:
    enabled: false                        # Use auto-ddl in tests for speed

app:
  jwt:
    secret: test-secret-key-that-is-long-enough-for-hmac-sha256-algorithm
    expiration-ms: 3600000
```

### application-prod.yml
```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
  jpa:
    show-sql: false

logging:
  level:
    com.moabood.financetracker: INFO
```

---

## Maven Dependencies (pom.xml)

**Group ID**: `com.moabood`
**Artifact ID**: `finance-tracker`
**Java version**: 21

```xml
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-web</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-data-jpa</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-security</artifactId></dependency>
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-validation</artifactId></dependency>

    <!-- Database -->
    <dependency><groupId>org.postgresql</groupId><artifactId>postgresql</artifactId><scope>runtime</scope></dependency>
    <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-core</artifactId></dependency>
    <dependency><groupId>org.flywaydb</groupId><artifactId>flyway-database-postgresql</artifactId></dependency>

    <!-- JWT -->
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-api</artifactId><version>0.12.6</version></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-impl</artifactId><version>0.12.6</version><scope>runtime</scope></dependency>
    <dependency><groupId>io.jsonwebtoken</groupId><artifactId>jjwt-jackson</artifactId><version>0.12.6</version><scope>runtime</scope></dependency>

    <!-- Utilities -->
    <dependency><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId><optional>true</optional></dependency>
    <dependency><groupId>org.mapstruct</groupId><artifactId>mapstruct</artifactId><version>1.5.5.Final</version></dependency>

    <!-- API Documentation -->
    <dependency><groupId>org.springdoc</groupId><artifactId>springdoc-openapi-starter-webmvc-ui</artifactId><version>2.6.0</version></dependency>

    <!-- Testing -->
    <dependency><groupId>org.springframework.boot</groupId><artifactId>spring-boot-starter-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>org.springframework.security</groupId><artifactId>spring-security-test</artifactId><scope>test</scope></dependency>
    <dependency><groupId>com.h2database</groupId><artifactId>h2</artifactId><scope>test</scope></dependency>
</dependencies>
```

**IMPORTANT**: Configure MapStruct + Lombok annotation processor in maven-compiler-plugin:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-compiler-plugin</artifactId>
    <configuration>
        <annotationProcessorPaths>
            <path><groupId>org.projectlombok</groupId><artifactId>lombok</artifactId></path>
            <path><groupId>org.projectlombok</groupId><artifactId>lombok-mapstruct-binding</artifactId><version>0.2.0</version></path>
            <path><groupId>org.mapstruct</groupId><artifactId>mapstruct-processor</artifactId><version>1.5.5.Final</version></path>
        </annotationProcessorPaths>
    </configuration>
</plugin>
```

---

## Docker Compose

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:16-alpine
    container_name: finance-tracker-db
    environment:
      POSTGRES_DB: finance_tracker
      POSTGRES_USER: postgres
      POSTGRES_PASSWORD: postgres
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U postgres"]
      interval: 5s
      timeout: 5s
      retries: 5

  backend:
    build: ./backend
    container_name: finance-tracker-api
    ports:
      - "8080:8080"
    environment:
      SPRING_PROFILES_ACTIVE: dev
      SPRING_DATASOURCE_URL: jdbc:postgresql://postgres:5432/finance_tracker
      JWT_SECRET: change-this-to-a-real-secret-in-production-256-bits-minimum
    depends_on:
      postgres:
        condition: service_healthy

  frontend:
    build: ./frontend
    container_name: finance-tracker-ui
    ports:
      - "5173:5173"
    depends_on:
      - backend

volumes:
  pgdata:
```

### Backend Dockerfile

```dockerfile
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .
RUN chmod +x mvnw && ./mvnw dependency:go-offline
COPY src src
RUN ./mvnw package -DskipTests

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### Frontend Dockerfile

```dockerfile
FROM node:20-alpine
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
EXPOSE 5173
CMD ["npm", "run", "dev", "--", "--host"]
```

---

## Frontend Specification

### Pages & Components

#### LoginPage.tsx
- Email + password form
- Submit calls POST /auth/login
- On success: store JWT in localStorage, redirect to /dashboard
- Link to register page
- Show error toast on bad credentials

#### RegisterPage.tsx
- Email, password, first name, last name form
- Client-side validation (email format, password min 8 chars)
- Submit calls POST /auth/register
- On success: auto-login and redirect to /dashboard

#### DashboardPage.tsx (Main landing page after login)
- **Summary cards** (top row): Total Balance (sum all accounts), Income this month, Expenses this month, Net Savings this month — use MonthlySummaryDto
- **Spending by Category** (pie chart): Recharts PieChart using CategoryBreakdownDto for current month expenses
- **Income vs Expenses Trend** (line chart): Recharts LineChart using SpendingTrendDto for last 6 months, two lines (income green, expenses red)
- **Recent Transactions** (table, last 5): mini table with date, merchant, category, amount — link to full transactions page
- **Budget Alerts**: show any budgets with status WARNING or EXCEEDED

#### TransactionsPage.tsx
- **Filter bar** at top: date range picker, type dropdown (All/Income/Expense), category dropdown, search text input
- **Add Transaction button**: opens modal/slide-over with CreateTransactionRequest form
- **Transaction table**: sortable columns (date, description, merchant, category, amount, type, account). Paginated.
- **Each row**: click to edit (opens same form pre-filled), delete button with confirm dialog
- Amount column: green for income, red for expense

#### BudgetsPage.tsx
- **Add Budget button**: form with category dropdown (exclude categories that already have a budget), amount, period
- **Budget cards grid**: each card shows category name + icon, progress bar (green < 75%, yellow 75-100%, red > 100%), amount spent / limit, amount remaining
- Click card to edit limit or delete

#### AccountsPage.tsx
- **Add Account button**: form with name, type dropdown, initial balance, currency
- **Account cards**: show name, type icon, current balance, currency
- Edit and delete options

### AuthContext.tsx
- Store JWT token in localStorage
- Provide login(), logout(), register() functions
- On app load: check if token exists and is not expired
- If expired: logout and redirect to /login

### axiosConfig.ts
- Base URL: `http://localhost:8080/api/v1`
- Request interceptor: attach `Authorization: Bearer <token>` header
- Response interceptor: if 401, logout and redirect to /login

### Layout.tsx
- Left sidebar with navigation: Dashboard, Transactions, Budgets, Accounts
- Top bar with user name and logout button
- Main content area (renders current route)
- Responsive: sidebar collapses to hamburger on mobile

### Styling
- Tailwind CSS utility classes
- Color scheme: dark sidebar (#1E293B), white content area, blue accent (#3B82F6)
- Consistent card styling: white bg, subtle shadow, rounded corners
- Clean, professional — not flashy. Think banking app, not startup.

---

## Testing Specification

### What to test (minimum — these are the ones you would talk about in interviews):

#### TransactionServiceTest.java (Unit test with Mockito)
- `shouldCreateTransaction_andUpdateAccountBalance`
- `shouldDeleteTransaction_andReverseAccountBalance`
- `shouldThrowResourceNotFound_whenTransactionDoesNotExist`
- `shouldThrowAccessDenied_whenUserDoesNotOwnTransaction`

#### TransactionControllerTest.java (@WebMvcTest)
- `shouldReturn201_whenValidTransactionCreated`
- `shouldReturn400_whenAmountIsNegative`
- `shouldReturn400_whenRequiredFieldsMissing`
- `shouldReturn404_whenTransactionNotFound`
- `shouldReturnPaginatedResults`

#### TransactionRepositoryTest.java (@DataJpaTest)
- `shouldFindTransactionsByUserAndDateRange`
- `shouldSumExpensesByCategory`

#### BudgetServiceTest.java (Unit test with Mockito)
- `shouldCalculateBudgetProgress`
- `shouldReturnWarningStatus_whenOver75Percent`
- `shouldReturnExceededStatus_whenOver100Percent`

#### AuthControllerTest.java (@WebMvcTest)
- `shouldReturn201_whenValidRegistration`
- `shouldReturn409_whenEmailAlreadyExists`
- `shouldReturn200_withToken_whenValidLogin`
- `shouldReturn401_whenInvalidCredentials`

#### AnalyticsServiceTest.java (Unit test with Mockito)
- `shouldReturnMonthlySummary`
- `shouldReturnCategoryBreakdown_sortedByAmount`

---

## README.md Structure

```markdown
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
\```bash
# Clone and start everything
git clone https://github.com/moabood/finance-tracker.git
cd finance-tracker
docker compose up -d

# Frontend: http://localhost:5173
# API Docs: http://localhost:8080/swagger-ui.html
\```

## API Documentation
Interactive Swagger UI available at `/swagger-ui.html` when running.

## Architecture
[Brief description of layered architecture: Controller → Service → Repository]
[Mention: DTO pattern, centralized error handling, JPA Specifications for filtering]

## Testing
\```bash
cd backend
./mvnw test
\```
```

---

## Build Order (Day-by-Day Execution Plan)

### Day 1: Foundation + Core API

1. Initialize Spring Boot project (start.spring.io or manual pom.xml)
2. Set up project structure (all packages/directories)
3. Docker Compose with PostgreSQL
4. application.yml configs (dev, test, prod)
5. BaseEntity, all entities, all enums
6. Flyway migrations V1-V6
7. All repositories
8. All DTOs + MapStruct mappers
9. GlobalExceptionHandler + ErrorResponse + ResourceNotFoundException
10. CategoryService + CategoryController (simplest CRUD — warm up)
11. AccountService + AccountController
12. TransactionService + TransactionController (including balance updates + Specifications for filtering)
13. Verify everything compiles and basic CRUD works via Swagger

### Day 2: Auth + Budgets + Analytics + Tests

1. JwtService, JwtAuthenticationFilter, SecurityConfig
2. AuthController + AuthService (register, login)
3. Test auth flow end-to-end via Swagger
4. BudgetService + BudgetController (including progress calculation)
5. AnalyticsService + AnalyticsController (all 3 endpoints)
6. Write all tests listed above
7. Verify all tests pass

### Day 3: Frontend + Polish

1. Scaffold React + Vite + TypeScript + Tailwind
2. AuthContext + axiosConfig + ProtectedRoute
3. LoginPage + RegisterPage
4. Layout with sidebar navigation
5. DashboardPage (summary cards + both charts)
6. TransactionsPage (table + filters + add/edit form)
7. BudgetsPage (cards with progress bars)
8. AccountsPage
9. Docker Compose for full stack
10. README.md
11. Final pass: check all endpoints work through UI, fix any bugs

---

## Non-Negotiable Quality Rules

1. **No field injection.** Constructor injection everywhere.
2. **No `spring.jpa.hibernate.ddl-auto: create` in dev/prod.** Flyway only. `validate` in dev/prod. `create-drop` only in test profile.
3. **`open-in-view: false`**. Always. OSIV is an anti-pattern.
4. **DTOs everywhere.** Never expose entities in API responses. Never accept entities in request bodies.
5. **User-scoped data access.** Every service method that touches user data must verify ownership. Extract the authenticated user from SecurityContextHolder and filter by user_id.
6. **@Transactional on service methods that modify data.** Not on controllers, not on repositories.
7. **BigDecimal for money.** Never float, never double.
8. **Proper HTTP status codes.** 201 for creates, 204 for deletes, 400 for validation, 404 for not found, 409 for conflicts, 401/403 for auth.
9. **No business logic in controllers.** Controllers delegate to services. Period.
10. **Pagination on list endpoints that could grow.** Transactions must be paginated. Accounts and budgets are fine as full lists (bounded by user).
