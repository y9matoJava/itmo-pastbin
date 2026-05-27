package ru.itmo.pastbin.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
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

    /**
     * Настройка CORS — разрешаем браузерные запросы к REST API
     * с любого origin (актуально при разработке локально).
     * На проде можно ограничить конкретным доменом.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedHeaders("*");
    }

    /**
     * Явное объявление обработчика статических ресурсов.
     * Spring Boot и так обрабатывает /static/**, но явная
     * регистрация гарантирует корректную работу с PageController.
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/static/**")
                .addResourceLocations("classpath:/static/");
    }
}
