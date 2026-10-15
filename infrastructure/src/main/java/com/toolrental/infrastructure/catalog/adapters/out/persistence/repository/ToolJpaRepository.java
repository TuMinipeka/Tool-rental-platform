package com.toolrental.infrastructure.catalog.adapters.out.persistence.repository;

import com.toolrental.infrastructure.catalog.adapters.out.persistence.entity.ToolJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ToolJpaRepository extends JpaRepository<ToolJpaEntity, UUID> {
  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select tool from ToolJpaEntity tool where tool.id = :id")
  Optional<ToolJpaEntity> findByIdForUpdate(@Param("id") UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select tool from ToolJpaEntity tool where tool.id in :ids order by tool.id")
  List<ToolJpaEntity> findAllByIdForUpdate(@Param("ids") Collection<UUID> ids);
}
