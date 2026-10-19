package com.toolrental.infrastructure.deliveryrate.adapters.out.persistence;

import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.deliveryrate.model.aggregate.DeliveryRate;
import com.toolrental.domain.deliveryrate.model.valueobject.DeliveryRateId;
import com.toolrental.domain.deliveryrate.port.repository.DeliveryRateRepository;
import com.toolrental.infrastructure.deliveryrate.adapters.out.persistence.entity.DeliveryRateJpaEntity;
import com.toolrental.infrastructure.deliveryrate.adapters.out.persistence.repository.DeliveryRateJpaRepository;
import java.util.Currency;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class DeliveryRatePersistenceAdapter implements DeliveryRateRepository {
  private final DeliveryRateJpaRepository rows;

  public DeliveryRatePersistenceAdapter(DeliveryRateJpaRepository rows) {
    this.rows = rows;
  }

  private DeliveryRate map(DeliveryRateJpaEntity r) {
    return DeliveryRate.restore(
        new DeliveryRateId(r.getId()),
        r.getVersion(),
        new Money(r.getPrice(), Currency.getInstance(r.getCurrency())),
        r.isActive(),
        r.getEffectiveAt());
  }

  private DeliveryRateJpaEntity map(DeliveryRate r) {
    return new DeliveryRateJpaEntity(
        r.id().value(),
        r.version(),
        r.price().amount(),
        r.price().currency().getCurrencyCode(),
        r.active(),
        r.effectiveAt());
  }

  public Optional<DeliveryRate> findById(DeliveryRateId id) {
    return rows.findById(id.value()).map(this::map);
  }

  public Optional<DeliveryRate> findActive() {
    return rows.findFirstByActiveTrueOrderByVersionDesc().map(this::map);
  }

  public List<DeliveryRate> findAll() {
    return rows.findAll().stream().map(this::map).toList();
  }

  @Transactional
  public DeliveryRate save(DeliveryRate rate) {
    if (rate.active()) rows.deactivateAll();
    return map(rows.save(map(rate)));
  }
}
