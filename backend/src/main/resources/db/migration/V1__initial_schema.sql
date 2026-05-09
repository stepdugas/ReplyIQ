-- Users table
CREATE TABLE users (
    id              BIGSERIAL PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    name            VARCHAR(255) NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,
    stripe_customer_id VARCHAR(255),
    trial_end_date  TIMESTAMP,
    subscription_status VARCHAR(50) NOT NULL DEFAULT 'trialing',
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW()
);

-- OAuth tokens for Google Business Profile access
CREATE TABLE oauth_tokens (
    id              BIGSERIAL PRIMARY KEY,
    user_id         BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    access_token    TEXT NOT NULL,
    refresh_token   TEXT NOT NULL,
    expires_at      TIMESTAMP NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_oauth_tokens_user UNIQUE (user_id)
);

-- Connected Google Business Profile locations
CREATE TABLE locations (
    id                  BIGSERIAL PRIMARY KEY,
    user_id             BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    google_location_id  VARCHAR(255) NOT NULL,
    location_name       VARCHAR(255) NOT NULL,
    address             TEXT,
    tone_preference     VARCHAR(50) NOT NULL DEFAULT 'professional',
    auto_post           BOOLEAN NOT NULL DEFAULT FALSE,
    connected_at        TIMESTAMP NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_locations_google_id UNIQUE (user_id, google_location_id)
);

-- Reviews pulled from Google Business Profile
CREATE TABLE reviews (
    id                  BIGSERIAL PRIMARY KEY,
    location_id         BIGINT NOT NULL REFERENCES locations(id) ON DELETE CASCADE,
    google_review_id    VARCHAR(255) NOT NULL UNIQUE,
    reviewer_name       VARCHAR(255) NOT NULL,
    star_rating         INTEGER NOT NULL CHECK (star_rating BETWEEN 1 AND 5),
    review_text         TEXT,
    posted_at           TIMESTAMP NOT NULL,
    reply_text          TEXT,
    reply_status        VARCHAR(50) NOT NULL DEFAULT 'needs_reply',
    replied_at          TIMESTAMP,
    created_at          TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at          TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Indexes for common queries
CREATE INDEX idx_reviews_location_id ON reviews(location_id);
CREATE INDEX idx_reviews_reply_status ON reviews(reply_status);
CREATE INDEX idx_reviews_posted_at ON reviews(posted_at DESC);
CREATE INDEX idx_locations_user_id ON locations(user_id);
