package ru.itmo.pastbin.service;


import org.springframework.stereotype.Service;
import ru.itmo.pastbin.repository.PasteRepository;
import ru.itmo.pastbin.entity.Paste;

import java.time.LocalDateTime;

/**
 * сервис для работы с paste.
 *
 * бизнес-логика:
 * - создание paste (текст -> MinIO, метаданные -> PostgreSQL)
 * - генерация уникальных коротких ссылок (через HashGeneratorService)
 *
 * Архитектура:
 *  PasteService координирует работу между:
 *  - HashGeneratorService (Redis -> Base62 хеш)
 *  - StorageService (MinIO - хранение текста)
 *  - PasteRepository (PostgreSQL - хранение метаданных)
 *
 */
@Service
public class PasteService {
    private final PasteRepository pasteRepository;
    private final StorageService storageService;
    private final HashGeneratorService hashGeneratorService;

    public PasteService(PasteRepository pasteRepository,
                        StorageService storageService,
                        HashGeneratorService hashGeneratorService) {
        this.pasteRepository = pasteRepository;
        this.storageService = storageService;
        this.hashGeneratorService = hashGeneratorService;
    }

    /**
     * Создает новый текстовый блок.
     *
     * Алгоритм:
     * 1) Генерируем уникальый Base62 хеш через Redis счетчик
     * 2) Загружаем текст в MinIO
     * 3) Сохраняем метаданные в PostgreSQL
     *
     * @param title заголовой пасты
     * @param content текст пасты (будет сохранен в MinIO)
     * @param ttlMinutes время жизни ссылки в минутах
     * @return сохраненная сущность Paste с метаданными
     */
    public Paste createPaste(String title, String content, int ttlMinutes) {
        // шаг 1: получаем уникальный хеш (Redis INCR -> Base62)
        String hash = hashGeneratorService.generateHash();
        String objectKey = "pastes/" + hash + ".txt";


        // шаг 2: загружаем текст в MinIO
        storageService.upload(objectKey, content);

        // шаг 3: сохраняем метаданные в PostgreSQL
        Paste paste = new Paste();
        paste.setHash(hash);
        paste.setTitle(title);
        paste.setObjectKey(objectKey);
        paste.setCreatedAt(LocalDateTime.now());
        paste.setExpiresAt(LocalDateTime.now().plusMinutes(ttlMinutes));
        paste.setActive(true);
        paste.setViewsCount(0L);

        return pasteRepository.save(paste);
    }
}
