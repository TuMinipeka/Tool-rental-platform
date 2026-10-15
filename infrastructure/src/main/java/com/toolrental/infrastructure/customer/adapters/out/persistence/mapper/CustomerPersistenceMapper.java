package com.toolrental.infrastructure.customer.adapters.out.persistence.mapper;

import com.toolrental.domain.customer.model.aggregate.Customer;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.infrastructure.customer.adapters.out.persistence.entity.CustomerJpaEntity;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class CustomerPersistenceMapper {
  public UUID key(CustomerId id) {
    return id.value();
  }

  public Customer toDomain(CustomerJpaEntity row) {
    return Customer.restore(
        new CustomerId(row.getId()),
        row.getPersonType(),
        row.getDocumentType(),
        row.getDocumentNumber(),
        row.getBillingName());
  }

  public CustomerJpaEntity toEntity(Customer value) {
    return new CustomerJpaEntity(
        key(value.id()),
        value.personType(),
        value.documentType(),
        value.documentNumber(),
        value.billingName());
  }
}
