package com.toolrental.infrastructure.category.adapters.out.persistence.repository;

import com.toolrental.infrastructure.category.adapters.out.persistence.entity.CategoryJpaEntity;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryJpaRepository extends JpaRepository<CategoryJpaEntity, UUID> {
  @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
  @org.springframework.data.jpa.repository.Query(
      "select row from CategoryJpaEntity row where row.id=:id")
  java.util.Optional<CategoryJpaEntity> findByIdForUpdate(
      @org.springframework.data.repository.query.Param("id") UUID id);
}
