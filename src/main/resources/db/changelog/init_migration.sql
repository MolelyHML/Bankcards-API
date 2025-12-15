CREATE TABLE app_user
(
    id        UUID         NOT NULL,
    username  VARCHAR(255) NOT NULL UNIQUE,
    full_name VARCHAR(255) NOT NULL,
    password  VARCHAR(255) NOT NULL,
    role      VARCHAR(64)  NOT NULL,
    CONSTRAINT pk_app_user PRIMARY KEY (id)
);

CREATE TABLE card
(
    id         UUID           NOT NULL,
    pan        VARCHAR(19)    NOT NULL UNIQUE,
    status     VARCHAR(255),
    balance    DECIMAL(19, 4) NOT NULL,
    expired_at date,
    owner_id   UUID           NOT NULL,
    CONSTRAINT pk_card PRIMARY KEY (id)
);

ALTER TABLE card
    ADD CONSTRAINT FK_CARD_ON_OWNER FOREIGN KEY (owner_id) REFERENCES app_user (id);