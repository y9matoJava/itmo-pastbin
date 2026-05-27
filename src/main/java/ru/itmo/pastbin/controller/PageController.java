package ru.itmo.pastbin.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * Контроллер для отдачи HTML-страниц (фронтенд).
 *
 * Разделение ответственности:
 *  - /api/pastes/** → PasteController (REST JSON)
 *  - /              → index.html     (форма создания)
 *  - /paste/{hash}  → paste.html     (просмотр пасты)
 *
 * HTML-файлы лежат в src/main/resources/static/
 * Spring Boot отдаёт их автоматически.
 *
 * @Controller (не @RestController) — чтобы возвращать
 * forward на статические ресурсы, а не JSON.
 */
@Controller
public class PageController {

    /**
     * Главная страница — форма создания пасты.
     * Spring Boot сам отдаёт /index.html при запросе /,
     * но явно объявим для надёжности.
     */
    @GetMapping("/")
    public String index() {
        return "forward:/index.html";
    }

    /**
     * Страница просмотра пасты.
     *
     * Браузер переходит на /paste/Ab9xQ —
     * мы отдаём paste.html, а JS внутри него
     * сам вызывает GET /api/pastes/Ab9xQ и рендерит данные.
     *
     * @param hash короткий хеш пасты из URL
     */
    @GetMapping("/paste/{hash}")
    public String viewPaste(@PathVariable String hash) {
        return "forward:/paste.html";
    }
}
