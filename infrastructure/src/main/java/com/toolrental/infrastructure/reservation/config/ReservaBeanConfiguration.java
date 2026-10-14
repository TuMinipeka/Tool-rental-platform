package com.toolrental.infrastructure.reservation.config;

import com.toolrental.application.reservation.usecase.CancelarReservaUseCase;
import com.toolrental.application.reservation.usecase.ConfirmarReservaUseCase;
import com.toolrental.application.reservation.usecase.RechazarReservaUseCase;
import com.toolrental.application.reservation.usecase.SolicitarReservaUseCase;
import com.toolrental.domain.catalog.port.repository.HerramientaRepository;
import com.toolrental.domain.catalog.port.repository.UnidadEquipoRepository;
import com.toolrental.domain.common.port.ParticipantesPort;
import com.toolrental.domain.reservation.port.repository.GestionReservaAtomica;
import com.toolrental.domain.reservation.port.repository.ReservaRepository;
import com.toolrental.infrastructure.common.config.ClockConfiguration;
import java.time.Clock;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration(proxyBeanMethods = false)
@Import(ClockConfiguration.class)
public class ReservaBeanConfiguration {
  @Bean
  @ConditionalOnBean({
    ReservaRepository.class,
    UnidadEquipoRepository.class,
    HerramientaRepository.class,
    ParticipantesPort.class
  })
  public SolicitarReservaUseCase solicitarReservaUseCase(
      ReservaRepository reservas,
      UnidadEquipoRepository unidades,
      HerramientaRepository herramientas,
      ParticipantesPort participantes,
      Clock clock) {
    return new SolicitarReservaUseCase(reservas, unidades, herramientas, participantes, clock);
  }

  @Bean
  @ConditionalOnBean(GestionReservaAtomica.class)
  public ConfirmarReservaUseCase confirmarReservaUseCase(
      GestionReservaAtomica gestion, Clock clock) {
    return new ConfirmarReservaUseCase(gestion, clock);
  }

  @Bean
  @ConditionalOnBean(GestionReservaAtomica.class)
  public RechazarReservaUseCase rechazarReservaUseCase(GestionReservaAtomica gestion, Clock clock) {
    return new RechazarReservaUseCase(gestion, clock);
  }

  @Bean
  @ConditionalOnBean(GestionReservaAtomica.class)
  public CancelarReservaUseCase cancelarReservaUseCase(GestionReservaAtomica gestion, Clock clock) {
    return new CancelarReservaUseCase(gestion, clock);
  }
}
