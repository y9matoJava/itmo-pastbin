package ru.itmo.pastbin.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;

/**
 * Глобальный обработчик исключений.
 * @ControllerAdvice - Spring автоматически перехватывает все исключения
 * из всех контроллеров и направляет сюда.
 *
 * Без этого класса клиент получает:
 *  500 Internal Server Error + длинный стектрейс
 *
 * С этим классом получает:
 *  404 Not Found + {"error": "паста не найдена: Ab9"}
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    /**
     * Перехватывает RuntimeException (паста не найдена, паста истекла).
     *
     * @ExceptionHandler — указывает Spring, какой тип исключений обрабатывать.
     * Когда любой контроллер бросает RuntimeException,
     * вместо стектрейса клиент получит аккуратный JSON.
     *
     * @param e исключение
     * @return JSON с сообщением об ошибке и HTTP-код 404
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handlerRuntimeException(RuntimeException e) {
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", e.getMessage()));
    }
}
