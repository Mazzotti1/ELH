CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS medias (
    id              BIGSERIAL PRIMARY KEY,
    guild_id        VARCHAR(20)  NOT NULL,
    channel_id      VARCHAR(20)  NOT NULL,
    message_id      VARCHAR(20)  NOT NULL,
    author_id       VARCHAR(20)  NOT NULL,
    author_name     VARCHAR(100) NOT NULL,
    s3_key          VARCHAR(500) NOT NULL,
    s3_thumbnail_key VARCHAR(500),
    permanent_url   VARCHAR(1000) NOT NULL,
    media_type      VARCHAR(10)  NOT NULL CHECK (media_type IN ('IMAGE','VIDEO','LINK')),
    mime_type       VARCHAR(100),
    size_bytes      BIGINT,
    original_url    VARCHAR(1000),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_medias_guild_id    ON medias(guild_id);
CREATE INDEX idx_medias_channel_id  ON medias(channel_id);
CREATE INDEX idx_medias_author_id   ON medias(author_id);
CREATE INDEX idx_medias_created_at  ON medias(created_at DESC);
CREATE INDEX idx_medias_media_type  ON medias(media_type);

CREATE TABLE IF NOT EXISTS media_tags (
    media_id    BIGINT      NOT NULL REFERENCES medias(id) ON DELETE CASCADE,
    tag         VARCHAR(50) NOT NULL,
    PRIMARY KEY (media_id, tag)
);

CREATE INDEX idx_media_tags_tag ON media_tags(tag);

CREATE TABLE IF NOT EXISTS media_reactions (
    id          BIGSERIAL PRIMARY KEY,
    media_id    BIGINT      NOT NULL REFERENCES medias(id) ON DELETE CASCADE,
    emoji       VARCHAR(50) NOT NULL,
    count       INT         NOT NULL DEFAULT 0,
    updated_at  TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    UNIQUE(media_id, emoji)
);

CREATE TABLE IF NOT EXISTS polls (
    id              BIGSERIAL PRIMARY KEY,
    guild_id        VARCHAR(20)  NOT NULL,
    channel_id      VARCHAR(20)  NOT NULL,
    title           VARCHAR(200) NOT NULL,
    discord_message_id VARCHAR(20),
    status          VARCHAR(10)  NOT NULL DEFAULT 'OPEN'
                        CHECK (status IN ('OPEN','CLOSED')),
    winner_media_id BIGINT REFERENCES medias(id),
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    closes_at       TIMESTAMPTZ  NOT NULL,
    closed_at       TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS poll_candidates (
    poll_id     BIGINT NOT NULL REFERENCES polls(id) ON DELETE CASCADE,
    media_id    BIGINT NOT NULL REFERENCES medias(id),
    vote_count  INT    NOT NULL DEFAULT 0,
    PRIMARY KEY (poll_id, media_id)
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id          BIGSERIAL PRIMARY KEY,
    guild_id    VARCHAR(20)  NOT NULL,
    channel_id  VARCHAR(20)  NOT NULL,
    author_id   VARCHAR(20)  NOT NULL,
    role        VARCHAR(10)  NOT NULL CHECK (role IN ('user','assistant')),
    content     TEXT         NOT NULL,
    tokens_used INT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_chat_session ON chat_messages(guild_id, channel_id, author_id, created_at DESC);

CREATE OR REPLACE FUNCTION update_updated_at()
RETURNS TRIGGER AS $$
BEGIN NEW.updated_at = NOW(); RETURN NEW; END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_medias_updated_at
    BEFORE UPDATE ON medias
    FOR EACH ROW EXECUTE FUNCTION update_updated_at();
