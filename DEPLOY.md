# 🚀 Деплой itmo-pastbin на Яндекс Клауд

## Шаг 1 — Создание виртуальной машины

1. Открой [console.cloud.yandex.ru](https://console.cloud.yandex.ru)
2. Слева: **Compute Cloud → Виртуальные машины → Создать ВМ**
3. Настройки:
   - **Образ**: Ubuntu 22.04 LTS
   - **Конфигурация**: 2 vCPU, 4 GB RAM (хватит для старта)
   - **Диск**: 20 GB SSD
   - **Сеть**: добавь публичный IP-адрес
   - **SSH-ключ**: вставь свой публичный ключ (`~/.ssh/id_rsa.pub`)
4. Нажми **Создать ВМ**, запиши внешний IP-адрес

---

## Шаг 2 — Настройка группы безопасности (firewall)

В Яндекс Клауд: **VPC → Группы безопасности → Создать** (или редактируй существующую).

Добавь входящие правила:
| Порт | Протокол | Для чего |
|------|----------|----------|
| 22   | TCP      | SSH |
| 8080 | TCP      | API приложения |
| 9001 | TCP      | MinIO консоль (можно закрыть после настройки) |

Привяжи группу безопасности к своей VM.

---

## Шаг 3 — Подключение к VM и установка Docker

```bash
# Подключись по SSH (замени IP на свой)
ssh ubuntu@<ВАШ_IP>

# Обнови систему
sudo apt update && sudo apt upgrade -y

# Установи Docker
curl -fsSL https://get.docker.com | sudo sh

# Добавь пользователя в группу docker (чтобы не писать sudo)
sudo usermod -aG docker $USER

# Применить группу без перезахода (или перезайди по SSH)
newgrp docker

# Проверь
docker --version
docker compose version
```

---

## Шаг 4 — Загрузка кода и конфигурации

```bash
# Клонируй репозиторий
git clone https://github.com/y9matoJava/itmo-pastbin.git
cd itmo-pastbin

# Скопируй файлы деплоя в корень проекта
# (Dockerfile, docker-compose.yml, .env — из архива который ты скачал)
# Или создай вручную:

# Создай .env и заполни пароли
nano .env
# Вставь содержимое из файла .env (из этого архива) и ОБЯЗАТЕЛЬНО смени пароли!
```

---

## Шаг 5 — Настройка application.properties

Проверь, что `src/main/resources/application.properties` читает переменные окружения.
Если там хардкод — нужно исправить на:

```properties
spring.datasource.url=${SPRING_DATASOURCE_URL}
spring.datasource.username=${SPRING_DATASOURCE_USERNAME}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD}

spring.data.redis.host=${SPRING_DATA_REDIS_HOST}
spring.data.redis.port=${SPRING_DATA_REDIS_PORT}
spring.data.redis.password=${SPRING_DATA_REDIS_PASSWORD}

minio.url=${MINIO_URL}
minio.access-key=${MINIO_ACCESS_KEY}
minio.secret-key=${MINIO_SECRET_KEY}
minio.bucket=${MINIO_BUCKET}
```

---

## Шаг 6 — Запуск

```bash
# Собери и запусти все контейнеры (первый раз займёт 2-5 минут)
docker compose up -d --build

# Следи за логами
docker compose logs -f

# Проверь что всё запущено
docker compose ps
```

Всё должно быть в статусе `Up (healthy)`.

---

## Шаг 7 — Проверка

```bash
# Проверь что API отвечает (с самой VM)
curl http://localhost:8080/

# Или с твоего компьютера
curl http://<ВАШ_IP>:8080/
```

MinIO консоль: открой в браузере `http://<ВАШ_IP>:9001`
- Логин: значение `MINIO_USER` из `.env`
- Пароль: значение `MINIO_PASSWORD` из `.env`
- Создай bucket с именем из `MINIO_BUCKET` (по умолчанию `pastes`)

---

## Полезные команды

```bash
# Перезапустить всё
docker compose restart

# Посмотреть логи конкретного сервиса
docker compose logs -f app
docker compose logs -f postgres

# Остановить
docker compose down

# Остановить и удалить данные (осторожно!)
docker compose down -v

# Обновить приложение после изменений в коде
git pull
docker compose up -d --build app
```

---

## ⚠️ Важные замечания

1. **Пароли** — обязательно смени все пароли в `.env` перед запуском
2. **Порт 9001** (MinIO консоль) — после первоначальной настройки закрой его в группе безопасности
3. **Бэкапы** — данные хранятся в Docker volumes (`postgres_data`, `minio_data`). Периодически делай снапшоты диска VM в Яндекс Клауд
4. **HTTPS** — если хочешь публичный доступ с доменом, установи Nginx + Certbot для SSL
