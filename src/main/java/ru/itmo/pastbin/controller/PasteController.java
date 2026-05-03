package ru.itmo.pastbin.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.itmo.pastbin.service.PasteService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/pastes")
public class PasteController {
    private final PasteService pasteService;

    @PostMapping
    public String createPaste(@RequestBody String text) {
        pasteService.createPaste("тест", text, 5);
        return "бубаби";
    }
}
