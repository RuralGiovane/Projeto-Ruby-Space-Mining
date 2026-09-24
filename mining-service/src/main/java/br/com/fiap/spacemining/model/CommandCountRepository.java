package br.com.fiap.spacemining.model;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CommandCountRepository extends JpaRepository<CommandCount, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT c FROM CommandCount c WHERE c.command = :command")
    Optional<CommandCount> findByCommandForUpdate(@Param("command") String command);
}
