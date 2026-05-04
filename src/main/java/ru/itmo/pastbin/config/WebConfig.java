package ru.itmo.pastbin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import ru.itmo.pastbin.interceptor.RateLimitingInterceptor;

/**
 * Конфигурация Spring MVC.
 *
 * Здесь мы регистрируем Interceptor-ы
 * и указываем, к каким URL паатернам они применяются
 *
 * WebMvcConfigurer интерфейс, позволяющий настраивать
 * поведение Spring MVC без переопределения стаднартной конфигурации
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final RateLimitingInterceptor rateLimitingInterceptor;

    public WebConfig(RateLimitingInterceptor rateLimitingInterceptor) {
        this.rateLimitingInterceptor = rateLimitingInterceptor;

    }

    /**
     * Регистрация interceptor-ов
     *
     * addPathPattens указываем, к каким URL применять:
     *  "/api/pastes" только POST запросы на создание паст
     *
     *  excludePathPattens можно исключить URL
     *  но interceptor и так можно настроить проверять только POST,
     *     для простоты мы ограничим только сам путь создания.
     *     Примечание: interceptor срабатывает на ВСЕ HTTP-методы для указанного пути.
     *     Если нужно ограничить только POST, это можно сделать
     *     проверкой request.getMethod() внутри preHandle().
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rateLimitingInterceptor)
                .addPathPatterns("/api/pastes");
    }
}
