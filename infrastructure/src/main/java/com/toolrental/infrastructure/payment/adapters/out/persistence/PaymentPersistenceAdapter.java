package com.toolrental.infrastructure.payment.adapters.out.persistence;

import com.toolrental.domain.payment.model.aggregate.Payment;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.infrastructure.payment.adapters.out.persistence.mapper.PaymentPersistenceMapper;
import com.toolrental.infrastructure.payment.adapters.out.persistence.repository.PaymentJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class PaymentPersistenceAdapter implements PaymentRepository {
  private final PaymentJpaRepository rows;
  private final PaymentPersistenceMapper mapper;

  public PaymentPersistenceAdapter(PaymentJpaRepository rows, PaymentPersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<Payment> findById(PaymentId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public List<Payment> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public Payment save(Payment value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(PaymentId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
