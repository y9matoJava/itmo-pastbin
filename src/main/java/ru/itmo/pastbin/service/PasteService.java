package ru.itmo.pastbin.service;


import org.springframework.stereotype.Service;
import ru.itmo.pastbin.repository.PasteRepository;
import ru.itmo.pastbin.entity.Paste;
/**
 * сервис для работы с paste.
 *
 * здесь находится вся бизнес-логика:
 * - генерация hash
 * - создание paste
 * - работа с БД
 */
@Service
public class PasteService {
    private final PasteRepository pasteRepository;

    public PasteService(PasteRepository pasteRepository) {
        this.pasteRepository = pasteRepository;
    }
}
