package ru.itmo.pastbin.dto;

/**
 * DTO для запроса на создание пасты
 *
 * Клиент отправляет json:
 * {
 *     "title": "Мой код",
 *     "content": "System.out.println(\"Hello\");",
 *     "ttlMinutes": 30
 * }
 * Spring автоматически десериализует json в этот объект
 */
public class PasteRequestDto {
    private String title;
    private String content;
    private int ttlMinutes = 60;

    public PasteRequestDto() {}

    public String getTitle() { return title; }
    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
    }

    public int getTtlMinutes() { return ttlMinutes; }
    public void setTtlMinutes(int ttlMinutes) {
        this.ttlMinutes = ttlMinutes;
    }
}
