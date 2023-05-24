
CREATE TABLE "user" (
                        id                          BIGSERIAL PRIMARY KEY,
                        tg_id                       VARCHAR(255) NOT NULL,
                        first_name                  VARCHAR(255) NULL,
                        last_name                   VARCHAR(255) NULL,
                        user_name                   VARCHAR(255) NOT NULL,
                        created_at                  TIMESTAMP WITH TIME ZONE,
                        updated_at                  TIMESTAMP WITH TIME ZONE
);

CREATE TABLE "role" (
                        id           BIGSERIAL PRIMARY KEY,
                        code         INTEGER UNIQUE,
                        name         VARCHAR(30) NOT NULL
);

CREATE TABLE user_role (
                           id          BIGSERIAL PRIMARY KEY,
                           user_id     BIGINT REFERENCES "user" (id) ON DELETE CASCADE,
                           role_id     BIGINT REFERENCES "role" (id) ON DELETE CASCADE
);

INSERT INTO role (name, code) VALUES ('DEFAULT', 1);
INSERT INTO role (name, code) VALUES ('ADMIN', 2);
INSERT INTO role (name, code) VALUES ('SYSTEM', 3);

CREATE TABLE message_type (
                              id               BIGSERIAL PRIMARY KEY,
                              code             INTEGER UNIQUE,
                              value            VARCHAR(255) NOT NULL
);

INSERT INTO message_type (code, value) VALUES (1, 'UNKNOWN');
INSERT INTO message_type (code, value) VALUES (2, 'LOG');
INSERT INTO message_type (code, value) VALUES (3, 'COMMAND');
INSERT INTO message_type (code, value) VALUES (4, 'SCHEDULED_TASK');
INSERT INTO message_type (code, value) VALUES (5, 'GPT_RESPONSE');
INSERT INTO message_type (code, value) VALUES (6, 'TELEGRAM_RESPONSE');

CREATE TABLE message_branch_type (
                                     id               BIGSERIAL PRIMARY KEY,
                                     code             INTEGER UNIQUE,
                                     value            VARCHAR(255) NOT NULL
);

INSERT INTO message_branch_type (code, value) VALUES (1, 'REMINDER_BRANCH');
INSERT INTO message_branch_type (code, value) VALUES (2, 'GPT_BRANCH');

CREATE TABLE message_branch (
                                id                       BIGSERIAL PRIMARY KEY,
                                message_branch_type_id   BIGINT REFERENCES message_branch_type (id) ON DELETE CASCADE NOT NULL,
                                user_id                  BIGINT REFERENCES "user" (id) ON DELETE CASCADE NOT NULL,
                                chat_id                  VARCHAR(64),
                                closed                   BOOLEAN,
                                locked                   BOOLEAN,
                                created_at               TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE message (
                         id                  BIGSERIAL PRIMARY KEY,
                         message_type_id     BIGINT REFERENCES message_type (id) ON DELETE CASCADE NOT NULL,
                         user_id             BIGINT REFERENCES "user" (id) ON DELETE CASCADE NULL,
                         message_branch_id   BIGINT REFERENCES message_branch (id) ON DELETE SET NULL NULL,
                         created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
                         text                TEXT,
                         chat_id             VARCHAR(64),
                         message_id          VARCHAR(64)
);

CREATE TABLE message_request (
                                 id                  BIGSERIAL PRIMARY KEY,
                                 user_id             BIGINT REFERENCES "user" (id) ON DELETE CASCADE NOT NULL,
                                 created_at          TIMESTAMP WITH TIME ZONE NOT NULL,
                                 message_id          BIGINT REFERENCES message (id) ON DELETE CASCADE NOT NULL
);

CREATE TABLE message_response (
                                  id               BIGSERIAL PRIMARY KEY,
                                  created_at       TIMESTAMP WITH TIME ZONE NOT NULL,
                                  message_id       BIGINT REFERENCES message (id) ON DELETE CASCADE NOT NULL
);

CREATE TABLE scheduled_task_type (
                                     id               BIGSERIAL PRIMARY KEY,
                                     code             INTEGER UNIQUE,
                                     value            VARCHAR(255)  NOT NULL
);

INSERT INTO scheduled_task_type (code, value) VALUES (1, 'DAILY');
INSERT INTO scheduled_task_type (code, value) VALUES (2, 'WEEKDAYS');
INSERT INTO scheduled_task_type (code, value) VALUES (3, 'WEEKDAYS_LOG');
INSERT INTO scheduled_task_type (code, value) VALUES (4, 'WEEKLY');

CREATE TABLE scheduled_task (
                                id                          BIGSERIAL PRIMARY KEY,
                                scheduled_task_type_id      BIGINT REFERENCES scheduled_task_type (id) ON DELETE CASCADE NOT NULL,
                                user_id                     BIGINT REFERENCES "user" (id) ON DELETE CASCADE NOT NULL,
                                active                      BOOLEAN NOT NULL,
                                next_start_time             TIMESTAMP WITH TIME ZONE NOT NULL,
                                cron_pattern                VARCHAR(100),
                                insistent                   BOOLEAN NOT NULL,
                                message_id                  BIGINT REFERENCES message (id) ON DELETE CASCADE NOT NULL
);

ALTER TABLE message ADD COLUMN scheduled_task_id  BIGINT REFERENCES scheduled_task (id) ON DELETE SET NULL NULL;
