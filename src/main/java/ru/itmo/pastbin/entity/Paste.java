package ru.itmo.pastbin.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Entity, представляющая текстовый блок (paste)
 *
 * Важно:
 * - сам текст НЕ хранится в БД (он лежит в MinIO)
 * - в БД хранятся только метаданные
 */
@Entity
@Table(name = "pastes")
public class Paste {
    /**
     * Уникальный ID записи (генерируется автоматически
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Короткий уникальный хеш для ссылки (пример: Ab91xQ)
     */
    @Column(nullable = false, unique = true, length = 16)
    private String hash;

    /**
     *  путь к файлу MinIO (пример: pastes/Ab91xQ.txt)
     */
    @Column(nullable = false)
    private String objectKey;

    private String title;

    /**
     * Дата создания
     */
    @Column(nullable = false)
    private LocalDateTime createdAt;

    /**
     * Дата истечения (TTL)
     */
    @Column(nullable = false)
    private LocalDateTime expiresAt;

    /**
     * Активен ли пост (используется вместо удаления)
     */
    @Column(nullable = false)
    private Boolean active = true;

    /**
     * количество просмотров (для будущей оптимизации)
     */
    private Long viewsCount = 0L;

    // ===== Constructors =====
    public Paste() {}

    // ===== Getters & Setters =====

    public Long getId(){
        return id;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getObjectKey() {
        return objectKey;
    }

    public void setObjectKey(String objectKey) {
        this.objectKey = objectKey;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getExpiresAt()
    {
        return expiresAt;
    }

    public void setExpiresAt(LocalDateTime expiresAt) {
        this.expiresAt = expiresAt;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Long getViewsCount() {
        return viewsCount;
    }

    public void setViewsCount(Long viewsCount) {
        this.viewsCount = viewsCount;
    }
}
