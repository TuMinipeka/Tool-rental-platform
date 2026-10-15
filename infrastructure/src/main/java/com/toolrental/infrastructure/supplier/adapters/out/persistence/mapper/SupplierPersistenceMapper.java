package com.toolrental.infrastructure.supplier.adapters.out.persistence.mapper;

import com.toolrental.domain.supplier.model.aggregate.Supplier;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.infrastructure.supplier.adapters.out.persistence.entity.SupplierJpaEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class SupplierPersistenceMapper {
  public UUID key(SupplierId id) {
    return id.value();
  }

  public Supplier toDomain(SupplierJpaEntity row) {
    return Supplier.restore(
        new SupplierId(row.getId()),
        row.getDocumentType(),
        row.getDocumentNumber(),
        row.getBusinessName());
  }

  public SupplierJpaEntity toEntity(Supplier value) {
    return new SupplierJpaEntity(
        key(value.id()), value.documentType(), value.documentNumber(), value.businessName());
  }
}
