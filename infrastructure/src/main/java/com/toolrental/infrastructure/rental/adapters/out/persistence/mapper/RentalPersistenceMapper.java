package com.toolrental.infrastructure.rental.adapters.out.persistence.mapper;

import com.toolrental.domain.rental.model.aggregate.Rental;
import com.toolrental.domain.rental.model.valueobject.RentalId;
import com.toolrental.infrastructure.rental.adapters.out.persistence.entity.RentalJpaEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class RentalPersistenceMapper {
  public UUID key(RentalId id) {
    return id.value();
  }

  public Rental toDomain(RentalJpaEntity row) {
    return Rental.restore(new RentalId(row.getId()), row.getStartedAt(), row.getRentalStatus());
  }

  public RentalJpaEntity toEntity(Rental value) {
    return new RentalJpaEntity(key(value.id()), value.startedAt(), value.rentalStatus());
  }
}
