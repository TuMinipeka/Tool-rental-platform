package com.toolrental.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.toolrental.application.catalog.usecase.ConsultarCatalogoUseCase;
import com.toolrental.application.catalog.usecase.RegistrarCategoriaUseCase;
import com.toolrental.application.catalog.usecase.RegistrarHerramientaUseCase;
import com.toolrental.application.catalog.usecase.RegistrarUnidadUseCase;
import com.toolrental.application.reservation.usecase.CancelarReservaUseCase;
import com.toolrental.application.reservation.usecase.ConfirmarReservaUseCase;
import com.toolrental.application.reservation.usecase.RechazarReservaUseCase;
import com.toolrental.application.reservation.usecase.SolicitarReservaUseCase;
import com.toolrental.domain.catalog.port.repository.CategoriaRepository;
import com.toolrental.domain.catalog.port.repository.HerramientaRepository;
import com.toolrental.domain.catalog.port.repository.RegistroUnidadAtomico;
import com.toolrental.domain.catalog.port.repository.UnidadEquipoRepository;
import com.toolrental.domain.common.port.ParticipantesPort;
import com.toolrental.domain.reservation.port.repository.GestionReservaAtomica;
import com.toolrental.domain.reservation.port.repository.ReservaRepository;
import com.toolrental.infrastructure.catalog.config.CatalogBeanConfiguration;
import com.toolrental.infrastructure.reservation.config.ReservaBeanConfiguration;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class BeanConfigurationTest {
  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withUserConfiguration(CatalogBeanConfiguration.class, ReservaBeanConfiguration.class);

  @Test
  void sinAdaptadoresSoloConfiguraElReloj() {
    runner.run(
        context -> {
          assertThat(context).hasSingleBean(Clock.class);
          assertThat(context).doesNotHaveBean(ConfirmarReservaUseCase.class);
          assertThat(context).doesNotHaveBean(RegistrarCategoriaUseCase.class);
        });
  }

  @Test
  void ensamblaTodosLosCasosCuandoSeRegistranLosPuertos() {
    runner
        .withBean(CategoriaRepository.class, () -> mock(CategoriaRepository.class))
        .withBean(HerramientaRepository.class, () -> mock(HerramientaRepository.class))
        .withBean(UnidadEquipoRepository.class, () -> mock(UnidadEquipoRepository.class))
        .withBean(RegistroUnidadAtomico.class, () -> mock(RegistroUnidadAtomico.class))
        .withBean(ParticipantesPort.class, () -> mock(ParticipantesPort.class))
        .withBean(ReservaRepository.class, () -> mock(ReservaRepository.class))
        .withBean(GestionReservaAtomica.class, () -> mock(GestionReservaAtomica.class))
        .run(
            context -> {
              assertThat(context).hasSingleBean(RegistrarCategoriaUseCase.class);
              assertThat(context).hasSingleBean(RegistrarHerramientaUseCase.class);
              assertThat(context).hasSingleBean(RegistrarUnidadUseCase.class);
              assertThat(context).hasSingleBean(ConsultarCatalogoUseCase.class);
              assertThat(context).hasSingleBean(SolicitarReservaUseCase.class);
              assertThat(context).hasSingleBean(ConfirmarReservaUseCase.class);
              assertThat(context).hasSingleBean(RechazarReservaUseCase.class);
              assertThat(context).hasSingleBean(CancelarReservaUseCase.class);
            });
  }

  @Test
  void permiteInyectarUnRelojControlado() {
    Clock fijo = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneOffset.UTC);
    runner
        .withBean(Clock.class, () -> fijo)
        .run(context -> assertThat(context.getBean(Clock.class)).isSameAs(fijo));
  }
}
