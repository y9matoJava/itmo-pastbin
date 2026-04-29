package ru.itmo.pastbin.service;


import org.springframework.stereotype.Service;
import ru.itmo.pastbin.repository.PasteRepository;
import ru.itmo.pastbin.entity.Paste;

import java.time.LocalDateTime;
import java.util.Random;

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
    private final StorageService storageService;

    private static final int HASH_LENGTH = 6;
    private static final String HASH_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final Random random = new Random();

    public PasteService(PasteRepository pasteRepository, StorageService storageService) {
        this.pasteRepository = pasteRepository;
        this.storageService = storageService;
    }

    /**
     * Создает новый текстовый блок.
     *
     * Алгоритм:
     * 1) Генерируем уникальый hash для короткой ссылки
     * 2) Загружаем текст в MinIO
     * 3) Сохраняем метаданные (hash, objectKey, TTL) в PostgreSQL
     *
     * @param title заголовой пасты
     * @param content текст пасты (будет сохранен в MinIO)
     * @param ttlMinutes время жизни ссылки в минутах
     * @return сохраненная сущность Paste с метаданными
     */
    public Paste createPaste(String title, String content, int ttlMinutes) {
        String hash = generateUniqueHash();
        String objectKey = "pastes/" + hash + ".txt";


        // шаг 1: загружаем текст в MinIO
        storageService.upload(objectKey, content);

        // шаг 2: сохраняем метаданные в PostgreSQL
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


    /**
     * Генерирует уникальный короткий hash для ссылки.
     *
     * Проверяем hash в БД, чтобы не было двух paste с одинаковой ссылкой.
     */
    private String generateUniqueHash() {
        String hash;

        do {
            hash = generateHash();
        } while (pasteRepository.findByHash(hash).isPresent());
        return hash;
    }

    /**
     * Генерирует случайную строку из букв и цифр.
     */
    private String generateHash() {
        StringBuilder hash = new StringBuilder();

        for (int i = 0; i < HASH_LENGTH; i++) {
            int index = random.nextInt(HASH_CHARS.length());
            hash.append(HASH_CHARS.charAt(index));
        }
        return hash.toString();
    }
}
