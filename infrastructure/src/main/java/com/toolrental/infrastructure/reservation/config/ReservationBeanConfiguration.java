package com.toolrental.infrastructure.reservation.config;

import com.toolrental.application.reservation.usecase.CancelReservationUseCase;
import com.toolrental.application.reservation.usecase.ConfirmReservationUseCase;
import com.toolrental.application.reservation.usecase.QuoteReservationUseCase;
import com.toolrental.application.reservation.usecase.RejectReservationUseCase;
import com.toolrental.application.reservation.usecase.RequestReservationUseCase;
import com.toolrental.domain.common.port.ParticipantsPort;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import com.toolrental.domain.reservation.port.repository.ReservationRepository;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import com.toolrental.infrastructure.common.config.ClockConfiguration;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import(ClockConfiguration.class)
public class ReservationBeanConfiguration {
  @Bean
  public QuoteReservationUseCase quoteReservationUseCase(
      EquipmentUnitRepository equipmentUnits,
      ToolRepository tools,
      com.toolrental.domain.reservation.port.repository.AvailabilityRepository availability,
      com.toolrental.domain.common.port.AccessPolicy access,
      com.toolrental.domain.common.port.UnitOfWork work,
      com.toolrental.domain.cancellationpolicy.port.repository.CancellationPolicyRepository
          policies,
      com.toolrental.domain.deliveryrate.port.repository.DeliveryRateRepository deliveryRates) {
    return new QuoteReservationUseCase(
        equipmentUnits, tools, availability, access, work, policies, deliveryRates);
  }

  @Bean
  public RequestReservationUseCase requestReservationUseCase(
      ReservationRepository reservations,
      EquipmentUnitRepository equipmentUnits,
      ToolRepository tools,
      ParticipantsPort participants,
      com.toolrental.domain.cancellationpolicy.port.repository.CancellationPolicyRepository
          policies,
      com.toolrental.domain.deliveryrate.port.repository.DeliveryRateRepository deliveryRates,
      Clock clock) {
    return new RequestReservationUseCase(
        reservations, equipmentUnits, tools, participants, policies, deliveryRates, clock);
  }

  @Bean
  public ConfirmReservationUseCase confirmReservationUseCase(
      AtomicReservationManagement management, Clock clock) {
    return new ConfirmReservationUseCase(management, clock);
  }

  @Bean
  public RejectReservationUseCase rejectReservationUseCase(
      AtomicReservationManagement management, Clock clock) {
    return new RejectReservationUseCase(management, clock);
  }

  @Bean
  public CancelReservationUseCase cancelReservationUseCase(
      AtomicReservationManagement management, Clock clock) {
    return new CancelReservationUseCase(management, clock);
  }

  @Bean
  public com.toolrental.application.reservation.usecase.QueryReservationUseCase
      queryReservationUseCase(
          com.toolrental.application.common.service.WorkflowReferences references,
          com.toolrental.domain.common.port.UnitOfWork work,
          ReservationRepository reservations,
          com.toolrental.domain.common.port.AccessPolicy access) {
    return new com.toolrental.application.reservation.usecase.QueryReservationUseCase(
        references, work, reservations, access);
  }
}
