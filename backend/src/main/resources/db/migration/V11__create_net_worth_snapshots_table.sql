CREATE TABLE net_worth_snapshots (
    id                BIGSERIAL PRIMARY KEY,
    user_id           BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    snapshot_date     DATE NOT NULL,
    total_assets      DECIMAL(15,2) NOT NULL,
    total_liabilities DECIMAL(15,2) NOT NULL,
    net_worth         DECIMAL(15,2) NOT NULL,
    created_at        TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    UNIQUE(user_id, snapshot_date)
);

CREATE INDEX idx_net_worth_snapshots_user_date ON net_worth_snapshots(user_id, snapshot_date DESC);
