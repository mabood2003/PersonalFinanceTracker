ALTER TABLE transactions
    ADD COLUMN idempotency_key VARCHAR(100),
    ADD COLUMN transfer_group_id UUID,
    ADD COLUMN transfer_leg VARCHAR(3);

ALTER TABLE transactions
    ADD CONSTRAINT chk_transactions_transfer_shape
    CHECK (
        (type <> 'TRANSFER' AND transfer_group_id IS NULL AND transfer_leg IS NULL)
        OR
        (type = 'TRANSFER' AND transfer_group_id IS NOT NULL AND transfer_leg IN ('OUT', 'IN'))
    );

CREATE UNIQUE INDEX ux_transactions_transfer_idempotency
    ON transactions(user_id, idempotency_key, transfer_leg)
    WHERE idempotency_key IS NOT NULL;

CREATE INDEX idx_transactions_transfer_group_id
    ON transactions(transfer_group_id);
