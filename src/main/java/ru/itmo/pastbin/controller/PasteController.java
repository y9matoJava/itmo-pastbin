package ru.itmo.pastbin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.pastbin.dto.PasteRequestDto;
import ru.itmo.pastbin.dto.PasteResponseDto;
import ru.itmo.pastbin.entity.Paste;
import ru.itmo.pastbin.service.PasteService;

/**
 * REST контроллер для работы с пастами
 *
 * Предоставляет два эндпоинта:
 * - POST /api/pastes - создание новой пасты
 * - GET /api/pastes/{hash} - получение пасты по хешу
 */

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pastes")
public class PasteController {
    private final PasteService pasteService;

    /**
     * Создание новой пасты
     *
     * пример запроса:
     * POST /api/pastes
     * Content-type: application/json
     * {
     *     "title": "Мой код",
     *     "content": "print('hello')";
     *     "ttlMinutes": 30
     * }
     *
     * @param request DTO с данными от клиента
     * @return DTO с метаданными созданной пасты
     */
    @PostMapping
    public PasteResponseDto createPaste(@RequestBody PasteRequestDto request) {
        Paste paste = pasteService.createPaste(
                request.getTitle(),
                request.getContent(),
                request.getTtlMinutes()
        );
        return new PasteResponseDto(
                paste.getHash(),
                paste.getTitle(),
                request.getContent(),
                paste.getCreatedAt(),
                paste.getExpiresAt()
        );

    }
    /**
     * получение пасты по короткому хешу
     *
     * Пример запроса: GET /api/pastes/Ab9
     *
     * благодаря @Cachable в PasteService, повторные запросы
     * к популярным постам будут отдавать из Redis кэша,
     * не нагружая PostgreSQl и MinIO
     *
     * @param hash короткий хеш из url
     * @return json с метеданными и текстом пасты
     */
    @GetMapping("/{hash}")
    public PasteResponseDto getPaste(@PathVariable String hash) {
        return pasteService.getByHash(hash);
    }
}
