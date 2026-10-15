package com.toolrental.infrastructure.unitdelivery.adapters.out.persistence;

import com.toolrental.domain.unitdelivery.model.aggregate.UnitDelivery;
import com.toolrental.domain.unitdelivery.model.valueobject.UnitDeliveryId;
import com.toolrental.domain.unitdelivery.port.repository.UnitDeliveryRepository;
import com.toolrental.infrastructure.unitdelivery.adapters.out.persistence.mapper.UnitDeliveryPersistenceMapper;
import com.toolrental.infrastructure.unitdelivery.adapters.out.persistence.repository.UnitDeliveryJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class UnitDeliveryPersistenceAdapter implements UnitDeliveryRepository {
  private final UnitDeliveryJpaRepository rows;
  private final UnitDeliveryPersistenceMapper mapper;

  public UnitDeliveryPersistenceAdapter(
      UnitDeliveryJpaRepository rows, UnitDeliveryPersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<UnitDelivery> findById(UnitDeliveryId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public List<UnitDelivery> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public UnitDelivery save(UnitDelivery value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(UnitDeliveryId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
