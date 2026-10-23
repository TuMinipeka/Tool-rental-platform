package com.toolrental.infrastructure.legalacceptance.adapters.out.persistence;

import com.toolrental.domain.legalacceptance.model.aggregate.LegalAcceptance;
import com.toolrental.domain.legalacceptance.port.repository.LegalAcceptanceRepository;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.infrastructure.legalacceptance.adapters.out.persistence.mapper.LegalAcceptancePersistenceMapper;
import com.toolrental.infrastructure.legalacceptance.adapters.out.persistence.repository.LegalAcceptanceJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class LegalAcceptancePersistenceAdapter implements LegalAcceptanceRepository {
  private final LegalAcceptanceJpaRepository rows;
  private final LegalAcceptancePersistenceMapper mapper;

  public LegalAcceptancePersistenceAdapter(
      LegalAcceptanceJpaRepository rows, LegalAcceptancePersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  @Override
  public Optional<LegalAcceptance> findExisting(
      UserId userId, String documentType, String documentVersion, String purpose) {
    return rows.findByUserIdAndDocumentTypeAndDocumentVersionAndPurpose(
            userId.value(), documentType, documentVersion, purpose)
        .map(mapper::toDomain);
  }

  @Override
  public List<LegalAcceptance> findByUserId(UserId userId) {
    return rows.findByUserIdOrderByAcceptedAtDesc(userId.value()).stream()
        .map(mapper::toDomain)
        .toList();
  }

  @Override
  public LegalAcceptance save(LegalAcceptance acceptance) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(acceptance)));
  }
}
