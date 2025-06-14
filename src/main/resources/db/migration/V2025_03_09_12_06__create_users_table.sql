CREATE TABLE users
(
    id               BIGINT GENERATED BY DEFAULT AS IDENTITY,
    telegram_id      BIGINT NOT NULL,
    chat_id          BIGINT NOT NULL,
    state            TEXT   NOT NULL,
    trello_member_id TEXT,
    token            TEXT,
    token_secret     TEXT,

    CONSTRAINT pk_users PRIMARY KEY (id)
);