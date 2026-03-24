CREATE TABLE goals (
    id             BIGSERIAL PRIMARY KEY,
    user_id        BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name           VARCHAR(100) NOT NULL,
    description    VARCHAR(255),
    target_amount  DECIMAL(15,2) NOT NULL,
    current_amount DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    target_date    DATE,
    status         VARCHAR(20) NOT NULL DEFAULT 'IN_PROGRESS',
    icon           VARCHAR(10) NOT NULL DEFAULT '🎯',
    color          VARCHAR(20) NOT NULL DEFAULT 'blue',
    created_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_goals_user_id ON goals(user_id);
