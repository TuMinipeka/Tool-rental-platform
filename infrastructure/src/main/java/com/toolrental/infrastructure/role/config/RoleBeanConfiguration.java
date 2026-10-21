package com.toolrental.infrastructure.role.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.role.usecase.ManageRoleUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.role.port.repository.RoleRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class RoleBeanConfiguration {
  @Bean
  ManageRoleUseCase manageRoleUseCase(
      RoleRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock) {
    return new ManageRoleUseCase(repository, work, access, references, clock);
  }
}
