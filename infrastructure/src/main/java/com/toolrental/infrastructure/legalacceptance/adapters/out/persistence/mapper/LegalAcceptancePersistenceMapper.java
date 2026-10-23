package com.toolrental.infrastructure.legalacceptance.adapters.out.persistence.mapper;

import com.toolrental.domain.legalacceptance.model.aggregate.LegalAcceptance;
import com.toolrental.domain.legalacceptance.model.valueobject.LegalAcceptanceId;
import com.toolrental.domain.user.model.valueobject.UserId;
import com.toolrental.infrastructure.legalacceptance.adapters.out.persistence.entity.LegalAcceptanceJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class LegalAcceptancePersistenceMapper {
  public LegalAcceptance toDomain(LegalAcceptanceJpaEntity entity) {
    return LegalAcceptance.restore(
        new LegalAcceptanceId(entity.getId()),
        new UserId(entity.getUserId()),
        entity.getDocumentType(),
        entity.getDocumentVersion(),
        entity.getPurpose(),
        entity.getAcceptedAt());
  }

  public LegalAcceptanceJpaEntity toEntity(LegalAcceptance value) {
    return new LegalAcceptanceJpaEntity(
        value.id().value(),
        value.userId().value(),
        value.documentType(),
        value.documentVersion(),
        value.purpose(),
        value.acceptedAt());
  }
}
