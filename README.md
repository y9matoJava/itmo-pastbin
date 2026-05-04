# 📋 ITMO Pastbin

Высоконагруженный сервис для хранения и обмена текстовыми блоками (аналог Pastebin), разработанный с применением продвинутых архитектурных решений.

## ✨ Возможности

- **Создание паст** — загрузка текстового блока с заголовком и временем жизни (TTL)
- **Короткие ссылки** — уникальные Base62-хеши для доступа по URL
- **Автоочистка** — фоновое удаление просроченных паст из всех хранилищ
- **Кэширование** — мгновенная отдача популярных паст из Redis-кэша
- **Rate Limiting** — защита от спама (макс. 10 запросов на создание в минуту с одного IP)

## 🛠 Стек технологий

| Технология | Назначение |
|---|---|
| **Java 17+** | Основной язык |
| **Spring Boot** | Фреймворк (Web, Data JPA, Cache) |
| **PostgreSQL 15** | Хранение метаданных (хеш, даты, статус) |
| **Redis 7** | Кэш, атомарный счётчик хешей, Rate Limiter |
| **MinIO** | S3-совместимое хранилище текстов паст |
| **Docker Compose** | Контейнеризация инфраструктуры |
| **Flyway** | Управление миграциями БД |

## 📐 Архитектура

```
Клиент → PasteController (REST API)
              │
              ├── POST /api/pastes → PasteService.createPaste()
              │       ├── HashGeneratorService (Redis INCR → Base62)
              │       ├── StorageService.upload() → MinIO
              │       └── PasteRepository.save() → PostgreSQL
              │
              └── GET /api/pastes/{hash} → PasteService.getByHash()
                      ├── @Cacheable → Redis Cache (попадание? → мгновенный ответ)
                      ├── PasteRepository.findByHash() → PostgreSQL
                      └── StorageService.download() → MinIO
```

### Ключевые архитектурные решения

- **Разделение хранения**: метаданные в PostgreSQL, тексты в MinIO — БД не перегружается большими объёмами данных
- **Base62-хеши без коллизий**: атомарный счётчик Redis (`INCR`) гарантирует уникальность без проверок в БД (O(1))
- **Кэширование через `@Cacheable`**: популярные пасты отдаются из Redis за ~0.1мс, минуя PostgreSQL и MinIO
- **Rate Limiting через Interceptor**: Redis-счётчик с TTL блокирует IP при превышении лимита

## 📁 Структура проекта

```
src/main/java/ru/itmo/pastbin/
├── PastbinApplication.java          # Точка входа (@EnableCaching, @EnableScheduling)
├── config/
│   ├── MinioConfig.java             # Bean MinioClient
│   ├── RedisConfig.java             # Настройка CacheManager и RedisTemplate
│   └── WebConfig.java               # Регистрация Interceptor-ов
├── controller/
│   ├── PasteController.java         # REST API (POST, GET)
│   └── GlobalExceptionHandler.java  # Обработка ошибок (@ControllerAdvice)
├── dto/
│   ├── PasteRequestDto.java         # Входящий JSON (title, content, ttlMinutes)
│   └── PasteResponseDto.java        # Исходящий JSON + объект кэша Redis
├── entity/
│   └── Paste.java                   # JPA-сущность (метаданные пасты)
├── interceptor/
│   └── RateLimitingInterceptor.java # Ограничение 10 POST-запросов/мин на IP
├── job/
│   └── PasteCleanupJob.java         # @Scheduled: очистка просроченных паст (60 сек)
├── repository/
│   └── PasteRepository.java         # JPA-репозиторий с кастомными методами
├── service/
│   ├── HashGeneratorService.java    # Redis INCR → Base62 хеш
│   ├── PasteService.java            # Бизнес-логика (создание, получение)
│   └── StorageService.java          # Загрузка/скачивание/удаление файлов MinIO
└── util/
    └── Base62Encoder.java           # Конвертация числа в строку Base62
```

## 🚀 Запуск

### Предварительные требования
- Java 17+
- Maven
- Docker и Docker Compose

### 1. Запуск инфраструктуры

```bash
docker-compose up -d
```

Поднимаются три контейнера:
| Контейнер | Порт | Назначение |
|---|---|---|
| `pastbin-db` | 5432 | PostgreSQL |
| `pastbin-cache` | 6379 | Redis |
| `pastbin-s3` | 9000 / 9001 | MinIO (API / Web-консоль) |

### 2. Запуск приложения

```bash
./mvnw spring-boot:run
```

Приложение запустится на `http://localhost:8080`.

## 📡 API

### Создание пасты

```http
POST /api/pastes
Content-Type: application/json

{
  "title": "Мой код",
  "content": "System.out.println(\"Hello World\");",
  "ttlMinutes": 30
}
```

**Ответ (200 OK):**
```json
{
  "hash": "bLY2q3",
  "title": "Мой код",
  "content": "System.out.println(\"Hello World\");",
  "createdAt": "2026-05-04T13:29:15",
  "expireAt": "2026-05-04T13:59:15"
}
```

### Получение пасты

```http
GET /api/pastes/{hash}
```

**Ответ (200 OK):**
```json
{
  "hash": "bLY2q3",
  "title": "Мой код",
  "content": "System.out.println(\"Hello World\");",
  "createdAt": "2026-05-04T13:29:15",
  "expireAt": "2026-05-04T13:59:15"
}
```

### Ошибки

**Паста не найдена (404):**
```json
{
  "error": "паста не найдена: abc123"
}
```

**Лимит запросов превышен (429):**
```json
{
  "error": "Превышен лимит запросов. Попробуйте через минуту."
}
```

## ⚙️ Конфигурация

Основные параметры в `application.yaml`:

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/pastbin_db
    username: user
    password: password
  data:
    redis:
      host: localhost
      port: 6379

minio:
  url: http://localhost:9000
  access-key: minioadmin
  secret-key: minioadmin
  bucket: pastes
```

## 🧪 Проверка работоспособности

| # | Тест | Ожидаемый результат |
|---|------|---------------------|
| 1 | `POST /api/pastes` с JSON | 200 + JSON с хешем |
| 2 | `GET /api/pastes/{hash}` | 200 + текст пасты |
| 3 | Повторный GET (в логах нет SQL) | Ответ из Redis-кэша |
| 4 | GET с несуществующим хешем | 404 + JSON с ошибкой |
| 5 | 11+ POST-запросов за минуту | 429 Too Many Requests |
| 6 | Ожидание истечения TTL | CleanupJob удаляет из MinIO + PostgreSQL + Redis |
