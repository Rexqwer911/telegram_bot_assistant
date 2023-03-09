# tg_bot_daily_logger

Initial scripts for PostgreSQL:
create user tguser with encrypted password 'password';
CREATE DATABASE tgbase
WITH OWNER = tguser
   ENCODING = 'UTF8'
   TABLESPACE = pg_default
   LC_COLLATE = 'en_US.UTF-8'
   LC_CTYPE = 'en_US.UTF-8'
   CONNECTION LIMIT = -1
   TEMPLATE template0;
