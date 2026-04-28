# ITMO Pastebin Project

Pastebin-like сервис для хранения текста и шаринга по ссылке.

---

## 🚀 Возможности
- Создание текстовых блоков (paste)
- Доступ к тексту по короткой ссылке (/p/{hash})
- Поддержка времени жизни (TTL)
- Автоматическое удаление устаревших данных
- Кэширование популярных постов (подготовлено)
- Масштабируемая архитектура

---

## 🏗 Архитектура
Проект спроектирован с учетом масштабируемости и высокой нагрузки.
### Основные компоненты:
- Spring Boot API - обработка запросов
- PostgreSQL - хранение метаданных (hash, даты, статус)
- MinIO (S3) - хранение текстового содержимого
- Redis - кэширование популярных данных

---

### Поток создания paste:
CLient -> API -> Hash Generator -> PostgreSQL + MinIO

### Потом чтения paste:
Client -> API -> Redis -> PostgreSQL -> MinIO

---

## 🛠 Технологии
- Java 17+
- Spring Boot
- Spring Data JPA
- PostgreSQL
- Redis
- MinIO (S3 - совместимое хранилище)
- Docker / Docker Compose

---

## ⚙️ Запуск проекта
1) Клонирование репозитория
```bash
git clone https://github.com/y9matoJava/itmo-pastbin.git
cd itmo-pastbin
```
3) Запуск инфраструктуры (БД, Redis, MinIO)
```bash
docker-compose up
```
5) Запуск приложения
```bash
./mvnw spring-boot:run
```
---

## 📡 API (в разработке)
