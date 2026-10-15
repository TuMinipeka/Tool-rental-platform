package com.toolrental.infrastructure.rental.adapters.out.persistence.repository;

import com.toolrental.infrastructure.rental.adapters.out.persistence.entity.RentalJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RentalJpaRepository extends JpaRepository<RentalJpaEntity, UUID> {

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from RentalJpaEntity row where row.id = :id")
  Optional<RentalJpaEntity> findLocked(@Param("id") UUID id);
}
