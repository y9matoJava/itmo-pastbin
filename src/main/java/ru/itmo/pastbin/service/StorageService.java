package ru.itmo.pastbin.service;

import io.minio.*;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
/**
 * Сервис для работы с объектным хранилищем MinIO
 *
 * Отвечает за две операции:
 * 1) upload - загрузить текст пасты в MinIO как файл
 * 2) download - скачать текст пасты из MinIO по ключу (objectKey)
 *
 * Текст хранится как обычный .txt файл в бакете.
 * Это позволяет не нагружать PostgreSQL хранением больших текстов.
 */
@Service
public class StorageService {
    private final MinioClient minioClient;

    /** имя бакета из application.yaml */
    @Value("${minio.bucket}")
    private String bucket;

    /**
     * Constructor injection - Spring автоматически подставит
     * MinioClient, который создали в MinioConfig
     */
    public StorageService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    /**
     * Инициализация: проверяем, существует ли бакет.
     * Если нет - создаем его автоматически при старте приложения.
     *
     * @PostConstruct вызывается спрингом сразу после создания бина.
     */
    @PostConstruct
    public void init() {
        try {
            boolean exists = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucket).build()
            );
            if (!exists) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucket).build()
                );
            }
        } catch (Exception e) {
            throw new RuntimeException("Не удалось инициализировать MinIO бакет: " + bucket, e);
        }
    }

    /**
     * Загружает текст пасты в MinIO.
     *
     * @param objectKey путь к файлу внутри бакета
     * @param content текст пасты, который нужно сохранить
     */
    public void upload(String objectKey, String content) {
        try {
            // конвертируем строку в поток байтов
            byte[] bytes = content.getBytes(StandardCharsets.UTF_8);
            InputStream stream = new ByteArrayInputStream(bytes);

            minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)                            // в какой бакет
                        .object(objectKey)                         // под каким именем
                        .stream(stream, bytes.length, - 1) // что загружаем
                        .contentType("text/plain")                 // тип содержимого
                        .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Ошибка загрузки в MinIO: " + objectKey, e);
        }
    }

    /**
     * Скачивает файл пасты из MinIO.
     *
     * @param objectKey путь к файлу внутри бакета
     * @return содержимое файла как строка
     */
    public String download(String objectKey) {
        try (InputStream stream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(objectKey)
                        .build()
        )) {
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("Ошибка чтения из MinIO: " + objectKey, e);
        }
    }

    /**
     * Удаляет файл из MinIO.
     * Используется при физической очистке просроченных постов.
     *
     * @param objectKey путь к файлу внутри бакета
     */
    public void delete(String objectKey) {
        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(bucket)
                            .object(objectKey)
                            .build()
            );
        } catch (Exception e) {
            throw new RuntimeException("Ошибка удаления из MinIO: " + objectKey, e);
        }
    }
}

