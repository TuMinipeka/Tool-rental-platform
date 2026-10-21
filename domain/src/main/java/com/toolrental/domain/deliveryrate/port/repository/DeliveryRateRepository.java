package com.toolrental.domain.deliveryrate.port.repository;

import com.toolrental.domain.deliveryrate.model.aggregate.DeliveryRate;
import com.toolrental.domain.deliveryrate.model.valueobject.DeliveryRateId;
import java.util.List;
import java.util.Optional;

public interface DeliveryRateRepository {
  Optional<DeliveryRate> findById(DeliveryRateId id);

  Optional<DeliveryRate> findActive();

  List<DeliveryRate> findAll();

  DeliveryRate save(DeliveryRate rate);
}
