# Telegram Bot Assistant

Универсальный ассистент на базе телеграм-бота.

Создан для удобной работы с напоминаниями, заметками, записями о проделанной работе и пр.

Список используемых технологий:
JDK 17
Spring Boot 2.7.5
PostgreSQL
Project Reactor
_____________________________________________________

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
