package com.toolrental.infrastructure.reservationextension.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.reservationextension.usecase.ManageReservationExtensionUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.payment.port.repository.PaymentRepository;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import com.toolrental.domain.reservationextension.port.repository.ReservationExtensionRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ReservationExtensionBeanConfiguration {
  @Bean
  ManageReservationExtensionUseCase extensionUseCase(
      ReservationExtensionRepository extensions,
      AtomicReservationManagement reservations,
      PaymentRepository payments,
      WorkflowReferences references,
      AccessPolicy access,
      UnitOfWork work,
      Clock clock) {
    return new ManageReservationExtensionUseCase(
        extensions, reservations, payments, references, access, work, clock);
  }
}
