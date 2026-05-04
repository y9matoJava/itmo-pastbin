package ru.itmo.pastbin.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.itmo.pastbin.entity.Paste;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * репозиторий для работы с paste
 */

public interface PasteRepository extends JpaRepository<Paste, Long>{
    /**
     * найти paste по hash
     */
    Optional<Paste> findByHash(String hash);

    /**
     * найти все активные пасты, у которых срок жизни истек.
     *
     * Spring Data JPA сам генерирует SQL по имени метода:
     * findByACtiveTrueAndExpiresAtBefore(now) ->
     *  SELECT * FROM pastes WHERE active = true AND expires_at < now
     *
     * @param now текущее время
     * @return список просроченных паст
     */
    List<Paste> findByActiveTrueAndExpiresAtBefore(LocalDateTime now);
}
