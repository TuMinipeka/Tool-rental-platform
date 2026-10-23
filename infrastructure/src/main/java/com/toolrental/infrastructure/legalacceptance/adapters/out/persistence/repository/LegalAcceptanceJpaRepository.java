package com.toolrental.infrastructure.legalacceptance.adapters.out.persistence.repository;

import com.toolrental.infrastructure.legalacceptance.adapters.out.persistence.entity.LegalAcceptanceJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LegalAcceptanceJpaRepository
    extends JpaRepository<LegalAcceptanceJpaEntity, UUID> {
  Optional<LegalAcceptanceJpaEntity> findByUserIdAndDocumentTypeAndDocumentVersionAndPurpose(
      UUID userId, String documentType, String documentVersion, String purpose);

  List<LegalAcceptanceJpaEntity> findByUserIdOrderByAcceptedAtDesc(UUID userId);
}
