package com.toolrental.infrastructure.cancellationpolicy.adapters.out.persistence;

import com.toolrental.domain.cancellationpolicy.model.aggregate.CancellationPolicy;
import com.toolrental.domain.cancellationpolicy.model.valueobject.CancellationPolicyId;
import com.toolrental.domain.cancellationpolicy.port.repository.CancellationPolicyRepository;
import com.toolrental.infrastructure.cancellationpolicy.adapters.out.persistence.entity.CancellationPolicyJpaEntity;
import com.toolrental.infrastructure.cancellationpolicy.adapters.out.persistence.repository.CancellationPolicyJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class CancellationPolicyPersistenceAdapter implements CancellationPolicyRepository {
  private final CancellationPolicyJpaRepository rows;

  public CancellationPolicyPersistenceAdapter(CancellationPolicyJpaRepository rows) {
    this.rows = rows;
  }

  private CancellationPolicy map(CancellationPolicyJpaEntity r) {
    return CancellationPolicy.restore(
        new CancellationPolicyId(r.getId()),
        r.getVersion(),
        r.getCutoffHours(),
        r.getLateRate(),
        r.getNoShowRate(),
        r.getGraceMinutes(),
        r.isActive(),
        r.getEffectiveAt());
  }

  private CancellationPolicyJpaEntity map(CancellationPolicy p) {
    return new CancellationPolicyJpaEntity(
        p.id().value(),
        p.version(),
        p.cutoffHours(),
        p.lateCancellationRate(),
        p.noShowRate(),
        p.noShowGraceMinutes(),
        p.active(),
        p.effectiveAt());
  }

  public Optional<CancellationPolicy> findById(CancellationPolicyId id) {
    return rows.findById(id.value()).map(this::map);
  }

  public Optional<CancellationPolicy> findActive() {
    return rows.findFirstByActiveTrueOrderByVersionDesc().map(this::map);
  }

  public List<CancellationPolicy> findAll() {
    return rows.findAll().stream().map(this::map).toList();
  }

  @Transactional
  public CancellationPolicy save(CancellationPolicy policy) {
    if (policy.active()) rows.deactivateAll();
    return map(rows.save(map(policy)));
  }
}
