package com.toolrental.application.legalacceptance.usecase;

import com.toolrental.application.legalacceptance.command.AcceptLegalDocumentCommand;
import com.toolrental.application.legalacceptance.dto.LegalAcceptanceResponse;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.legalacceptance.model.aggregate.LegalAcceptance;
import com.toolrental.domain.legalacceptance.model.valueobject.LegalAcceptanceId;
import com.toolrental.domain.legalacceptance.port.repository.LegalAcceptanceRepository;
import com.toolrental.domain.user.model.valueobject.UserId;
import java.time.Clock;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public final class ManageLegalAcceptanceUseCase {
  private final LegalAcceptanceRepository repository;
  private final AccessPolicy access;
  private final UnitOfWork work;
  private final Clock clock;

  public ManageLegalAcceptanceUseCase(
      LegalAcceptanceRepository repository, AccessPolicy access, UnitOfWork work, Clock clock) {
    this.repository = repository;
    this.access = access;
    this.work = work;
    this.clock = clock;
  }

  public LegalAcceptanceResponse accept(UUID actor, AcceptLegalDocumentCommand command) {
    return work.execute(
        () -> {
          access.requireActive(actor);
          var userId = new UserId(actor);
          var candidate =
              LegalAcceptance.accept(
                  LegalAcceptanceId.generate(),
                  userId,
                  command.documentType(),
                  command.documentVersion(),
                  command.purpose(),
                  clock);
          return repository
              .findExisting(
                  userId,
                  candidate.documentType(),
                  candidate.documentVersion(),
                  candidate.purpose())
              .map(LegalAcceptanceResponse::from)
              .orElseGet(() -> LegalAcceptanceResponse.from(repository.save(candidate)));
        });
  }

  public List<LegalAcceptanceResponse> mine(UUID actor) {
    return work.execute(
        () -> {
          access.requireActive(actor);
          return repository.findByUserId(new UserId(actor)).stream()
              .sorted(Comparator.comparing(LegalAcceptance::acceptedAt).reversed())
              .map(LegalAcceptanceResponse::from)
              .toList();
        });
  }
}
