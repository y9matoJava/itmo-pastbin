package ru.itmo.pastbin.job;

import org.springframework.stereotype.Component;
import ru.itmo.pastbin.entity.Paste;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.cache.CacheManager;
import ru.itmo.pastbin.repository.PasteRepository;
import ru.itmo.pastbin.service.StorageService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Фоновая задача очистки просроченных паст
 *
 * @Scheduled(fixedRate = 60000) запуск каждые 60 секунд
 * fixedRate = интервал между началами вызовов
 *
 * Алгоритм:
 * 1) Запрашиваем из PostgresSQL все пасты где active = true И expires_at < NOW()
 * 2) Для каждой просроенной пасты:
 *  шаг1: удаляем файл из MinIO
 *  шаг2: удаляем запись из кэша Redis
 *  шаг3: удаляем запись из PostgreSQL
 * 3) Логируем количество удаленных паст
 */
@Component
public class PasteCleanupJob {
    private final PasteRepository pasteRepository;
    private final StorageService storageService;
    private final CacheManager cacheManager;

    public PasteCleanupJob(PasteRepository pasteRepository,
                           StorageService storageService,
                           CacheManager cacheManager) {
        this.pasteRepository = pasteRepository;
        this.storageService = storageService;
        this.cacheManager = cacheManager;
    }

    @Scheduled(fixedRate = 60000)
    public void cleanupExpiredpastes() {
        List<Paste> expired = pasteRepository.findByActiveTrueAndExpiresAtBefore(LocalDateTime.now());
        if (expired.isEmpty()) {
            return; // ничего не удаляем
        }
        for (Paste paste : expired) {
            storageService.delete(paste.getObjectKey());
            Objects.requireNonNull(cacheManager.getCache("pastes")).evict(paste.getHash());
            pasteRepository.delete(paste);
        }
        System.out.println("[CleanupJob] удалено просроченных паст: " + expired.size());
    }
}
