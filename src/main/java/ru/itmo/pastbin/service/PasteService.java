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
    private static final int HASH_LENGTH = 6;
    private static final String HASH_CHASRS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";

    private final Random random = new Random();

    public PasteService(PasteRepository pasteRepository) {
        this.pasteRepository = pasteRepository;
    }

    /**
     * Создает новый текстовый блок.
     *
     * Пока сам текст временно не сохраняется в MinIO
     * На этом этапе мы сохраняем только метаданные в PostgreSQL.
     */
    public Paste createPaste(String title, int ttlMinutes) {
        String hash = generateUniqueHash();

        Paste paste = new Paste();
        paste.setHash(hash);
        paste.setTitle(title);

        // в будущем будет путь к файлу в MinIO.
        paste.setObjectKey("pastes/" + hash + ".txt");

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
            int index = random.nextInt(HASH_CHASRS.length());
            hash.append(HASH_CHASRS.charAt(index));
        }
        return hash.toString();
    }
}
