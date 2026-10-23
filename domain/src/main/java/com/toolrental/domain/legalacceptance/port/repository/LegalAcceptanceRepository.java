package com.toolrental.domain.legalacceptance.port.repository;

import com.toolrental.domain.legalacceptance.model.aggregate.LegalAcceptance;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.util.List;
import java.util.Optional;

public interface LegalAcceptanceRepository {
  Optional<LegalAcceptance> findExisting(
      UserId userId, String documentType, String documentVersion, String purpose);

  List<LegalAcceptance> findByUserId(UserId userId);

  LegalAcceptance save(LegalAcceptance acceptance);
}
