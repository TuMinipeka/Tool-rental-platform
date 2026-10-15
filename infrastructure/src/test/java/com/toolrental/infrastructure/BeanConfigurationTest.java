package com.toolrental.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.toolrental.application.tool.usecase.QueryCatalogUseCase;
import com.toolrental.application.category.usecase.RegisterCategoryUseCase;
import com.toolrental.application.equipmentunit.usecase.RegisterEquipmentUnitUseCase;
import com.toolrental.application.tool.usecase.RegisterToolUseCase;
import com.toolrental.application.reservation.usecase.CancelReservationUseCase;
import com.toolrental.application.reservation.usecase.ConfirmReservationUseCase;
import com.toolrental.application.reservation.usecase.RejectReservationUseCase;
import com.toolrental.application.reservation.usecase.RequestReservationUseCase;
import com.toolrental.domain.equipmentunit.port.repository.AtomicEquipmentUnitRegistration;
import com.toolrental.domain.category.port.repository.CategoryRepository;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import com.toolrental.domain.common.port.ParticipantsPort;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import com.toolrental.domain.reservation.port.repository.ReservationRepository;
import com.toolrental.infrastructure.category.config.CategoryBeanConfiguration;
import com.toolrental.infrastructure.tool.config.ToolBeanConfiguration;
import com.toolrental.infrastructure.equipmentunit.config.EquipmentUnitBeanConfiguration;
import com.toolrental.infrastructure.reservation.config.ReservationBeanConfiguration;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class BeanConfigurationTest {
  private final ApplicationContextRunner runner =
      new ApplicationContextRunner()
          .withUserConfiguration(
              CategoryBeanConfiguration.class, ToolBeanConfiguration.class, EquipmentUnitBeanConfiguration.class, ReservationBeanConfiguration.class);

  @Test
  void withoutAdaptersOnlyConfiguresClock() {
    runner.run(
        context -> {
          assertThat(context).hasSingleBean(Clock.class);
          assertThat(context).doesNotHaveBean(ConfirmReservationUseCase.class);
          assertThat(context).doesNotHaveBean(RegisterCategoryUseCase.class);
        });
  }

  @Test
  void assemblesAllUseCasesWhenPortsAreRegistered() {
    runner
        .withBean(CategoryRepository.class, () -> mock(CategoryRepository.class))
        .withBean(ToolRepository.class, () -> mock(ToolRepository.class))
        .withBean(EquipmentUnitRepository.class, () -> mock(EquipmentUnitRepository.class))
        .withBean(
            AtomicEquipmentUnitRegistration.class,
            () -> mock(AtomicEquipmentUnitRegistration.class))
        .withBean(ParticipantsPort.class, () -> mock(ParticipantsPort.class))
        .withBean(ReservationRepository.class, () -> mock(ReservationRepository.class))
        .withBean(AtomicReservationManagement.class, () -> mock(AtomicReservationManagement.class))
        .run(
            context -> {
              assertThat(context).hasSingleBean(RegisterCategoryUseCase.class);
              assertThat(context).hasSingleBean(RegisterToolUseCase.class);
              assertThat(context).hasSingleBean(RegisterEquipmentUnitUseCase.class);
              assertThat(context).hasSingleBean(QueryCatalogUseCase.class);
              assertThat(context).hasSingleBean(RequestReservationUseCase.class);
              assertThat(context).hasSingleBean(ConfirmReservationUseCase.class);
              assertThat(context).hasSingleBean(RejectReservationUseCase.class);
              assertThat(context).hasSingleBean(CancelReservationUseCase.class);
            });
  }

  @Test
  void allowsInjectingControlledClock() {
    Clock fixedClock = Clock.fixed(Instant.parse("2026-10-14T12:00:00Z"), ZoneOffset.UTC);
    runner
        .withBean(Clock.class, () -> fixedClock)
        .run(context -> assertThat(context.getBean(Clock.class)).isSameAs(fixedClock));
  }
}
