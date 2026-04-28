package ru.itmo.pastbin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.pastbin.entity.Paste;

import java.util.Optional;

/**
 * репозиторий для работы с paste
 */

public interface PasteRepository extends JpaRepository<Paste, Long>{
    /**
     * найти paste по hash
     */
    Optional<Paste> findByHash(String hash);
}
