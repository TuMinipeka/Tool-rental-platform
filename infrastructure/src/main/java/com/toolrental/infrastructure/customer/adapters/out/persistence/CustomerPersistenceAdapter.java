package com.toolrental.infrastructure.customer.adapters.out.persistence;

import com.toolrental.domain.customer.model.aggregate.Customer;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.customer.port.repository.CustomerRepository;
import com.toolrental.infrastructure.customer.adapters.out.persistence.mapper.CustomerPersistenceMapper;
import com.toolrental.infrastructure.customer.adapters.out.persistence.repository.CustomerJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class CustomerPersistenceAdapter implements CustomerRepository {
  private final CustomerJpaRepository rows;
  private final CustomerPersistenceMapper mapper;

  public CustomerPersistenceAdapter(CustomerJpaRepository rows, CustomerPersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<Customer> findById(CustomerId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public List<Customer> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public Customer save(Customer value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(CustomerId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
