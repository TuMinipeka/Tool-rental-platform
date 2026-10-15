package com.toolrental.infrastructure.reservationitem.adapters.out.persistence.repository;

import com.toolrental.infrastructure.reservationitem.adapters.out.persistence.entity.ReservationItemJpaEntity;
import com.toolrental.infrastructure.reservationitem.adapters.out.persistence.entity.ReservationItemJpaId;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationItemJpaRepository
    extends JpaRepository<ReservationItemJpaEntity, ReservationItemJpaId> {

  List<ReservationItemJpaEntity> findByIdReservationId(UUID id);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select row from ReservationItemJpaEntity row where row.id = :id")
  Optional<ReservationItemJpaEntity> findLocked(@Param("id") ReservationItemJpaId id);
}
