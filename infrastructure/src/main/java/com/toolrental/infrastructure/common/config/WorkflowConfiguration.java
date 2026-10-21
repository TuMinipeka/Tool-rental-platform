package com.toolrental.infrastructure.common.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.reservation.port.repository.ReservationRepository;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class WorkflowConfiguration {
  @Bean
  WorkflowReferences workflowReferences(
      ReservationRepository reservations, ToolRepository tools, AccessPolicy access) {
    return new WorkflowReferences(reservations, tools, access);
  }
}
