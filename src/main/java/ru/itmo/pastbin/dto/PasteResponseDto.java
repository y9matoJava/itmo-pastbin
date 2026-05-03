package ru.itmo.pastbin.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO для ответа клиенту при запросе пасты.\
 *
 * Этот объект:
 * 1) Возвращается как json через rest API
 * 2) Кэшируется в Redis для быстрого повторного доступа
 * implements Serializable - для хранения в Redis
 */
public class PasteResponseDto implements Serializable {
    /** короткий хеш */
    private String hash;

    /** заголовок пасты */
    private String title;

    /** текст пасты (подгруженный из MinIO)*/
    private String content;

    /** когда создала */
    private LocalDateTime createdAt;

    /** когда истекает */
    private LocalDateTime expireAt;

    public PasteResponseDto() {}

    public PasteResponseDto(String hash, String title, String content,
                            LocalDateTime createdAt, LocalDateTime expireAt) {
        this.hash = hash;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.expireAt = expireAt;
    }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getExpireAt() { return expireAt; }
    public void setExpireAt(LocalDateTime expireAt) { this.expireAt = expireAt; }
}
