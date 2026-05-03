package ru.itmo.pastbin.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Interceptor для ограничения частоты запросов
 *
 * Защищает эндпоинт создания паст от злоупотреблений
 * Использует Redis как быстрое хранилище счетчиков
 *
 * Алгоритм:
 * 1) Для каждого IP создается ключ в redis: "rate_limit:{ip}"
 * 2) При каждом запросе значение увеличивается на 1
 * 3) При пермов запросе устанавливается TTl = 1 минута
 * 4) Если значения > MAX_REQUESTS отклоняем запрос
 * 5) Через минуту ключ автоматически удаляется - счетчик сбрасывается
 */
@Component
public class RateLimitingInterceptor {
    /** Максимальное количество запросов на создание паст в минуту с одного IP*/
    private static final int MAX_REQUESTS = 10;

    /** префик ключа в Redis */
    private static final String KEY_PREFIX = "rate_limit:";

    /** окно ограничения 1 минута */
    private static final long WINDOW_SECONDS = 60;

    private final StringRedisTemplate redisTemplate;

    public RateLimitingInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /** вызывается перед каждым запросом к контроллеру.
     *
     * @return true запрос пропускается дальше в контроллер
     *  false - запрос блокируется, контроллен не вызывается
     */
    @Override
    public boolean preHandle(HttpServletRequest request,
                             HttpServletResponse response,
                             Object handler) throws Exception {
        // получаем ip адрес клиента
        String clientIp = request.getRemoteAddr();
        String key = KEY_PREFIX + clientIp;

        // атомарно увеличиваем счетчик на 1
        Long requestCount = redisTemplate.opsForValue().increment(key);

        if (requestCount != null && requestCount == 1) {
            //
            //
            redisTemplate.expire(key, WINDOW_SECONDS, TimeUnit.SECONDS);
        }
    }
}
