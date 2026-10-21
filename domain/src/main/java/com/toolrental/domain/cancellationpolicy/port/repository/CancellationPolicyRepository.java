package com.toolrental.domain.cancellationpolicy.port.repository;

import com.toolrental.domain.cancellationpolicy.model.aggregate.CancellationPolicy;
import com.toolrental.domain.cancellationpolicy.model.valueobject.CancellationPolicyId;
import java.util.List;
import java.util.Optional;

public interface CancellationPolicyRepository {
  Optional<CancellationPolicy> findById(CancellationPolicyId id);

  Optional<CancellationPolicy> findActive();

  List<CancellationPolicy> findAll();

  CancellationPolicy save(CancellationPolicy policy);
}
