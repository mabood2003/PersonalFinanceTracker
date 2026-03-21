CREATE TABLE budgets (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id     BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    amount_limit    DECIMAL(15,2) NOT NULL,
    period          VARCHAR(10) NOT NULL DEFAULT 'MONTHLY',
    start_date      DATE NOT NULL,
    end_date        DATE,
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, category_id, period)
);

CREATE INDEX idx_budgets_user_id ON budgets(user_id);
