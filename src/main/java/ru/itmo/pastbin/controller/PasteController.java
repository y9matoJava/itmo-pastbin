package ru.itmo.pastbin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import ru.itmo.pastbin.dto.PasteResponseDto;
import ru.itmo.pastbin.service.PasteService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pastes")
public class PasteController {
    private final PasteService pasteService;

    /**
     * Получение пасты по короткому хешу
     *
     * пример запроса: GET /api/pastes/Ab9
     *
     * Благодаря @Cachable и PasteService, повторные запросы
     * к популярным постам будут отдаваться из redis кшэша
     * не нагружая Postgres и MinIO
     *
     * @param hash короткий хеш из url
     * @return json с метаданными и текстом пасты
     */
    @GetMapping("/{hash}")
    public PasteResponseDto getPaste(@PathVariable String hash) {
        return pasteService.getByHash(hash);
    }
}
