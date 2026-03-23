CREATE TABLE recurring_transactions (
    id                      BIGSERIAL PRIMARY KEY,
    user_id                 BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id              BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    destination_account_id  BIGINT REFERENCES accounts(id) ON DELETE SET NULL,
    category_id             BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    type                    VARCHAR(10) NOT NULL,
    amount                  DECIMAL(15,2) NOT NULL,
    description             VARCHAR(255),
    merchant                VARCHAR(100),
    frequency               VARCHAR(10) NOT NULL,
    start_date              DATE NOT NULL,
    next_run_date           DATE NOT NULL,
    end_date                DATE,
    last_run_date           DATE,
    active                  BOOLEAN NOT NULL DEFAULT TRUE,
    idempotency_prefix      VARCHAR(100) NOT NULL,
    created_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at              TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_recurring_transfer_shape
        CHECK (
            (type <> 'TRANSFER' AND destination_account_id IS NULL)
            OR
            (type = 'TRANSFER' AND destination_account_id IS NOT NULL)
        )
);

CREATE INDEX idx_recurring_transactions_user_id
    ON recurring_transactions(user_id);

CREATE INDEX idx_recurring_transactions_due
    ON recurring_transactions(active, next_run_date);
