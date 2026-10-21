package com.toolrental.domain.supplier.port.repository;

import com.toolrental.domain.supplier.model.aggregate.Supplier;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import java.util.List;
import java.util.Optional;

public interface SupplierRepository {
  Optional<Supplier> findById(SupplierId id);

  List<Supplier> findAll();

  Supplier save(Supplier value);

  void delete(SupplierId id);
}
