CREATE TABLE notification (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    event_id UUID NOT NULL UNIQUE,
    wedding_id INTEGER NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(150) NOT NULL,
    message TEXT NOT NULL,
    created_at TIMESTAMPTZ DEFAULT now() NOT NULL,
    read_at TIMESTAMPTZ
);

CREATE INDEX idx_notification_wedding_created_at
    ON notification(wedding_id, created_at);