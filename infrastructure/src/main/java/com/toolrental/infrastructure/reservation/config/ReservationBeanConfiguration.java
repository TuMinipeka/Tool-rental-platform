package com.toolrental.infrastructure.reservation.config;

import com.toolrental.application.reservation.usecase.CancelReservationUseCase;
import com.toolrental.application.reservation.usecase.ConfirmReservationUseCase;
import com.toolrental.application.reservation.usecase.RejectReservationUseCase;
import com.toolrental.application.reservation.usecase.RequestReservationUseCase;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import com.toolrental.domain.common.port.ParticipantsPort;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import com.toolrental.domain.reservation.port.repository.ReservationRepository;
import com.toolrental.infrastructure.common.config.ClockConfiguration;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import(ClockConfiguration.class)
public class ReservationBeanConfiguration {
  @Bean
  @ConditionalOnBean({
    ReservationRepository.class,
    EquipmentUnitRepository.class,
    ToolRepository.class,
    ParticipantsPort.class
  })
  public RequestReservationUseCase requestReservationUseCase(
      ReservationRepository reservations,
      EquipmentUnitRepository equipmentUnits,
      ToolRepository tools,
      ParticipantsPort participants,
      Clock clock) {
    return new RequestReservationUseCase(reservations, equipmentUnits, tools, participants, clock);
  }

  @Bean
  @ConditionalOnBean(AtomicReservationManagement.class)
  public ConfirmReservationUseCase confirmReservationUseCase(
      AtomicReservationManagement management, Clock clock) {
    return new ConfirmReservationUseCase(management, clock);
  }

  @Bean
  @ConditionalOnBean(AtomicReservationManagement.class)
  public RejectReservationUseCase rejectReservationUseCase(
      AtomicReservationManagement management, Clock clock) {
    return new RejectReservationUseCase(management, clock);
  }

  @Bean
  @ConditionalOnBean(AtomicReservationManagement.class)
  public CancelReservationUseCase cancelReservationUseCase(
      AtomicReservationManagement management, Clock clock) {
    return new CancelReservationUseCase(management, clock);
  }
}
