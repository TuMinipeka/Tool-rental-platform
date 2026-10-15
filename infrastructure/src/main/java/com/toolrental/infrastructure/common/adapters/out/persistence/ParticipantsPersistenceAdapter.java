package com.toolrental.infrastructure.common.adapters.out.persistence;

import com.toolrental.domain.common.port.ParticipantsPort;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.infrastructure.customer.adapters.out.persistence.repository.CustomerJpaRepository;
import com.toolrental.infrastructure.supplier.adapters.out.persistence.repository.SupplierJpaRepository;
import com.toolrental.infrastructure.user.adapters.out.persistence.repository.UserJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class ParticipantsPersistenceAdapter implements ParticipantsPort {
  private static final String CUSTOMER_ROLE = "CUSTOMER";
  private static final String SUPPLIER_ROLE = "SUPPLIER";

  private final UserJpaRepository accounts;
  private final CustomerJpaRepository customers;
  private final SupplierJpaRepository suppliers;

  public ParticipantsPersistenceAdapter(
      UserJpaRepository accounts,
      CustomerJpaRepository customers,
      SupplierJpaRepository suppliers) {
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
