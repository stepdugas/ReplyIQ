-- Track which nurture emails have been sent to each user
CREATE TABLE nurture_emails (
    id          BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    email_day   INTEGER NOT NULL,
    sent_at     TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_nurture_user_day UNIQUE (user_id, email_day)
);

CREATE INDEX idx_nurture_user_id ON nurture_emails(user_id);
