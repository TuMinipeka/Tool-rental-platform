package com.toolrental.domain.unitdelivery.port.repository;

import com.toolrental.domain.unitdelivery.model.aggregate.UnitDelivery;
import com.toolrental.domain.unitdelivery.model.valueobject.UnitDeliveryId;
import java.util.List;
import java.util.Optional;

public interface UnitDeliveryRepository {
  Optional<UnitDelivery> findById(UnitDeliveryId id);

  List<UnitDelivery> findAll();

  UnitDelivery save(UnitDelivery value);

  void delete(UnitDeliveryId id);
}
