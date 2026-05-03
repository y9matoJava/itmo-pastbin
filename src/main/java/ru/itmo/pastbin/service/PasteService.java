package ru.itmo.pastbin.service;

import org.springframework.cache.annotation.Cacheable;
import ru.itmo.pastbin.dto.PasteResponseDto;
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

    /**
     * Получает пасту по короткому хешу.
     *
     * @Cacheable - спринг автоматически кэширует результат в Redis
     *
     * Как он работает:
     * 1) Первый запрос с hash="Ab9" -> метод выполняется полностью
     *  (БД + MinIO), результат сохраняется в Redis под ключом "pastes::Ab9"
     * 2) Повторный запрос с hash="Ab9" -> метод НЕ вызывается,
     *  результат берется напрямую из redis (время работы 0.1мс вместо 50мс)
     *
     * value = "pastes" имя кэша
     * key = "#hash" по какому параметру кэшировать
     *
     * @param hash короткий хеш из url
     * @return DTO с метаданными из текстов пасты
     * @throws RuntimeException если паста не найдена или истекла
     */
    @Cacheable(value = "pastes", key = "#hash")
    public PasteResponseDto getByHash(String hash) {
        // шаг 1: ищем метаданные в PostgreSQL
        Paste paste = pasteRepository.findByHash(hash)
                .orElseThrow(() -> new RuntimeException("паста не найдена: " + hash));

        // шаг 2: проверяем, не истекла ли паста
        if (!paste.getActive() || paste.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("паста истекла: " + hash);
        }

        // шаг 3: загружаем текст из MinIO
        String content = storageService.download(paste.getObjectKey());

        // шаг 4: увеличиваем счетчик просмотров
        paste.setViewsCount(paste.getViewsCount() + 1);
        pasteRepository.save(paste);

        // шаг 5: собираем DTO
        return new PasteResponseDto(
                paste.getHash(),
                paste.getTitle(),
                content,
                paste.getCreatedAt(),
                paste.getExpiresAt()
        );
    }
}
