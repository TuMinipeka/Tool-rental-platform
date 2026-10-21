package com.toolrental.infrastructure.userrole.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.userrole.usecase.ManageUserRoleUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.role.port.repository.RoleRepository;
import com.toolrental.domain.user.port.repository.UserRepository;
import com.toolrental.domain.userrole.port.repository.UserRoleRepository;
import com.toolrental.domain.usersession.port.repository.UserSessionRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class UserRoleBeanConfiguration {
  @Bean
  ManageUserRoleUseCase manageUserRoleUseCase(
      UserRoleRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      UserRepository users,
      RoleRepository roles,
      UserSessionRepository sessions) {
    return new ManageUserRoleUseCase(
        repository, work, access, references, clock, users, roles, sessions);
  }
}
