package com.toolrental.infrastructure.toolimage.adapters.out.persistence.repository;

import com.toolrental.infrastructure.toolimage.adapters.out.persistence.entity.ToolImageJpaEntity;
import com.toolrental.infrastructure.toolimage.adapters.out.persistence.entity.ToolImageJpaId;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ToolImageJpaRepository extends JpaRepository<ToolImageJpaEntity, ToolImageJpaId> {

  List<ToolImageJpaEntity> findByIdToolId(UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from ToolImageJpaEntity row where row.id = :id")
  Optional<ToolImageJpaEntity> findLocked(@Param("id") ToolImageJpaId id);
}
