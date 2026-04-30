package ru.itmo.pastbin.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import ru.itmo.pastbin.util.Base62Encoder;

/**
 * Сервис генерации уникальных короткий хешей для URL.
 *
 * Принцип работы:
 * 1) В Redis хранится атомарный счетчик "paste:hash:counter"
 * 2) При каждом вызове выполняется команда INCR (атоматический инкремент)
 *  - Redis гарантирует, что два потока никогда не получат одно и то же число
 * 3) Полученное число кодируется в Base62 строку
 *
 * Преимущества перед случайно генерацией:
 * - Нет коллизий
 * - Нет запросов PostgreSQL для проверки уникальности
 * - Атомарность - безопасно при многопоточном доступе
 * - алгоритм O(1) по времени
 */
@Service
public class HashGeneratorService {
    /**
     * Ключ счетчика Redis.
     * Команда INCR увеличивает значение на 1 и возвращает новое значение.
     */
    private static final String COUNTER_KEY = "paste:hash:counter";

    private final StringRedisTemplate redisTemplate;

    public HashGeneratorService(StringRedisTemplate stringRedisTemplate) {
        this.redisTemplate = stringRedisTemplate;
    }

    /**
     * Генерирует следующий уникальный хеш.
     *
     * Redis INCR - атомарная операция:
     * даже если 100 запросов придут одновременно,
     * КАЖДЫЙ получит свое уникальное число
     *
     * @return уникальная Base62 строка
     */
    public String generateHash() {
        Long counter = redisTemplate.opsForValue().increment(COUNTER_KEY);
        if (counter == null) {
            throw new RuntimeException("Не удалось получить значение счетчика из Redis");
        }
        // конвертируем число в короткую строку
        return Base62Encoder.encode(counter);
    }
}
