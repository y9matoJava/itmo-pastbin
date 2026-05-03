package ru.itmo.pastbin.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

/** Конфигурация Redis как кэш хранилище
 *
 * @EnableCaching - включает поддежку аннотация @Cacheable, @CacheEvict и т.д.
 * Без этой аннотации спринг будет игнорировать все кеш аннотации в сервисах
 *
 * Принцип работы кэша:
 *   1) Метод помечен @Cacheable("pastes")
 *   2) Spring перехватывает вызов и проверяет: есть ли результат в Redis?
 *   3) Если ДА — возвращает из Redis, метод НЕ вызывается
 *   4) Если НЕТ — вызывает метод, результат кладёт в Redis
 */
@Configuration
@EnableCaching
public class RedisConfig {
    /** создает кэш менеджер с настройками сериализации и TTlю
     *
     * @param connectionFactory - спринг автоматически создает его
     *                             на основе настроек spring.dara.redis в yaml
     * @return настроенный менеджер кеша редис
     */
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        // Используем builder-паттерн для GenericJacksonJsonRedisSerializer (Spring Data Redis 4.x).
        // Builder внутри создает tools.jackson.databind.ObjectMapper с правильной настройкой
        // полиморфной типизации для корректной сериализации/десериализации кешируемых объектов.
        GenericJacksonJsonRedisSerializer jsonSerializer = GenericJacksonJsonRedisSerializer.builder()
                .build();

        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                // TTl кэша 10 минут. После этого запись удалится из Redis
                // и следующий запрос снова пойдет в БД + MinIO
                .entryTtl(Duration.ofMinutes(10))
                // ключи кэша сериализуем как строки
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(new StringRedisSerializer())
                )
                // значения сериализуем как json (читаемо + совместимо между версиями)
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair
                                .fromSerializer(jsonSerializer)
                );
        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }
}

