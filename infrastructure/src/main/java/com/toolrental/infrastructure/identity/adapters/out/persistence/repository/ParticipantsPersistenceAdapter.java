package com.toolrental.infrastructure.identity.adapters.out.persistence.repository;

import com.toolrental.domain.common.model.valueobject.CustomerId;
import com.toolrental.domain.common.model.valueobject.SupplierId;
import com.toolrental.domain.common.port.ParticipantsPort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class ParticipantsPersistenceAdapter implements ParticipantsPort {
  private static final String CUSTOMER_ROLE = "CUSTOMER";
  private static final String SUPPLIER_ROLE = "SUPPLIER";

  private final UserAccountJpaRepository accounts;
  private final CustomerProfileJpaRepository customers;
  private final SupplierProfileJpaRepository suppliers;

  public ParticipantsPersistenceAdapter(
      UserAccountJpaRepository accounts,
      CustomerProfileJpaRepository customers,
      SupplierProfileJpaRepository suppliers) {
    this.accounts = accounts;
    this.customers = customers;
    this.suppliers = suppliers;
  }

  @Override
  public boolean isCustomerActive(CustomerId id) {
    return customers.existsById(id.value()) && accounts.hasActiveRole(id.value(), CUSTOMER_ROLE);
  }

  @Override
  public boolean isSupplierActive(SupplierId id) {
    return suppliers.existsById(id.value()) && accounts.hasActiveRole(id.value(), SUPPLIER_ROLE);
  }
}
