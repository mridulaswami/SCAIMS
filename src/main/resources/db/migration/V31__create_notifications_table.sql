CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    recipient_id UUID NOT NULL,
    actor_id UUID,
    title VARCHAR(255) NOT NULL,
    message TEXT NOT NULL,
    type VARCHAR(64) NOT NULL,
    priority VARCHAR(32) NOT NULL DEFAULT 'MEDIUM',
    target_type VARCHAR(64),
    target_id VARCHAR(128),
    is_read BOOLEAN NOT NULL DEFAULT FALSE,
    read_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_notifications_recipient
        FOREIGN KEY (recipient_id)
            REFERENCES users (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_notifications_actor
        FOREIGN KEY (actor_id)
            REFERENCES users (id)
            ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient_created_at
    ON notifications (recipient_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_notifications_recipient_is_read
    ON notifications (recipient_id, is_read);
