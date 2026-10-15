package com.toolrental.domain.payment.port.repository;

import com.toolrental.domain.payment.model.aggregate.Payment;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import java.util.List;
import java.util.Optional;

public interface PaymentRepository {
  Optional<Payment> findById(PaymentId id);

  List<Payment> findAll();

  Payment save(Payment value);

  void delete(PaymentId id);
}
