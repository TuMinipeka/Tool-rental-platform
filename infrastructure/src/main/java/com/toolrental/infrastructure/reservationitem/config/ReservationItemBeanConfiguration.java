package com.toolrental.infrastructure.reservationitem.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.reservationitem.usecase.ManageReservationItemUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.reservationitem.port.repository.ReservationItemRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ReservationItemBeanConfiguration {
  @Bean
  ManageReservationItemUseCase manageReservationItemUseCase(
      ReservationItemRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock) {
    return new ManageReservationItemUseCase(repository, work, access, references, clock);
  }
}
