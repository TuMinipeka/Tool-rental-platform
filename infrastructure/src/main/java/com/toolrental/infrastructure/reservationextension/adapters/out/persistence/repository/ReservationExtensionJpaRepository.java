package com.toolrental.infrastructure.reservationextension.adapters.out.persistence.repository;

import com.toolrental.infrastructure.reservationextension.adapters.out.persistence.entity.ReservationExtensionJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationExtensionJpaRepository
    extends JpaRepository<ReservationExtensionJpaEntity, UUID> {
  Optional<ReservationExtensionJpaEntity> findByKey(String key);

  Optional<ReservationExtensionJpaEntity> findByReservationIdAndStatus(UUID id, String status);

  List<ReservationExtensionJpaEntity> findByReservationIdOrderByRequestedAt(UUID id);
}
