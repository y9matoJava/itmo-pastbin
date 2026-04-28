# ITMO Pastebin Project

Pastebin-like сервис для хранения текста и шаринга по ссылке.

## Технологии
- Java (Sptring Boot)
- PostgreSQL
- Redis
- Minio (S3)

## Архитектура
- Метаданные хранятся в PostgreSQL
- Текст хранится в MinIO
- Redis используется для кэширования

## Запуск
docker-compose up
