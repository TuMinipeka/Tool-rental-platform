package com.toolrental.domain.customer.port.repository;

import com.toolrental.domain.customer.model.aggregate.Customer;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import java.util.List;
import java.util.Optional;

public interface CustomerRepository {
  Optional<Customer> findById(CustomerId id);

  List<Customer> findAll();

  Customer save(Customer value);

  void delete(CustomerId id);
}
