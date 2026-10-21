package com.toolrental.infrastructure.supplier.adapters.out.persistence;

import com.toolrental.domain.supplier.model.aggregate.Supplier;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.domain.supplier.port.repository.SupplierRepository;
import com.toolrental.infrastructure.supplier.adapters.out.persistence.mapper.SupplierPersistenceMapper;
import com.toolrental.infrastructure.supplier.adapters.out.persistence.repository.SupplierJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class SupplierPersistenceAdapter implements SupplierRepository {
  private final SupplierJpaRepository rows;
  private final SupplierPersistenceMapper mapper;

  public SupplierPersistenceAdapter(SupplierJpaRepository rows, SupplierPersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<Supplier> findById(SupplierId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public List<Supplier> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public Supplier save(Supplier value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(SupplierId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
