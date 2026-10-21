package com.toolrental.domain.refund.port.repository;

import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.domain.refund.model.aggregate.Refund;
import com.toolrental.domain.refund.model.valueobject.RefundId;
import com.toolrental.domain.reservationtermination.model.valueobject.ReservationTerminationId;
import java.util.List;
import java.util.Optional;

public interface RefundRepository {
  Optional<Refund> findById(RefundId id);

  Optional<Refund> findByIdempotencyKey(String idempotencyKey);

  List<Refund> findByPayment(PaymentId paymentId);

  List<Refund> findByTermination(ReservationTerminationId terminationId);

  Refund save(Refund refund);
}
