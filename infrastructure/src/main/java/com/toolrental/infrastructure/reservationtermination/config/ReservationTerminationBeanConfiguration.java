package com.toolrental.infrastructure.reservationtermination.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.reservationtermination.usecase.ManageReservationTerminationUseCase;
import com.toolrental.domain.cancellationpolicy.port.repository.CancellationPolicyRepository;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.refund.port.repository.RefundRepository;
import com.toolrental.domain.rental.port.RentalLifecycle;
import com.toolrental.domain.rental.port.repository.RentalRepository;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import com.toolrental.domain.reservationtermination.port.repository.ReservationTerminationRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ReservationTerminationBeanConfiguration {
  @Bean
  ManageReservationTerminationUseCase terminationUseCase(
      ReservationTerminationRepository t,
      CancellationPolicyRepository c,
      PaymentRepository p,
      RefundRepository f,
      RentalRepository r,
      RentalLifecycle lifecycle,
      AtomicReservationManagement a,
      WorkflowReferences w,
      AccessPolicy access,
      UnitOfWork work,
      Clock clock) {
    return new ManageReservationTerminationUseCase(
        t, c, p, f, r, lifecycle, a, w, access, work, clock);
  }
}
