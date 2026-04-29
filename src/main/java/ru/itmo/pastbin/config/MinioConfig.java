package ru.itmo.pastbin.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Конфигурация подключения к MinIO
 *
 * Spring считывает параметры из application.yaml (секция minio:)
 * и создает готовый к использованию Bean MinioClient
 *
 * MinioCLient - HTTP клиет, который может общаться
 * с любым S3-совместимым хранилищем
 */

@Configuration
public class MinioConfig {

    /** URL адрес MinIO сервера*/
    @Value("${minio.url}")
    private String url;

    /** Клюс доступа (аналог логина) */
    @Value("${minio.acces-key}")
    private String accesKey;

    /** Секретный ключ (аналог пароля) */
    @Value("${minio.secret-key}")
    private String secretKey;

    /**
     * Создает и регистрирует MinioClient как Spring Bean.
     *
     * После этого любой сервис можем получить MinioClient
     * через конструктор injection
     *
     * @return настроенный экземпляр MinioClient
     */
    @Bean
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(url)         // куда подключаемся
                .credentials(accesKey, secretKey)       // с какими данными
                .build();
    }
}
