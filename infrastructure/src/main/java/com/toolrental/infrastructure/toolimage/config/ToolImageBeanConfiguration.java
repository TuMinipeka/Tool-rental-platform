package com.toolrental.infrastructure.toolimage.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.toolimage.usecase.ManageToolImageUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.toolimage.port.repository.ToolImageRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ToolImageBeanConfiguration {
  @Bean
  ManageToolImageUseCase manageToolImageUseCase(
      ToolImageRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock) {
    return new ManageToolImageUseCase(repository, work, access, references, clock);
  }
}
