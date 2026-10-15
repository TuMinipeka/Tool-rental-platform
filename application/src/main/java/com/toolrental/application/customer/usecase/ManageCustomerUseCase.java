package com.toolrental.application.customer.usecase;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.customer.command.RegisterCustomerCommand;
import com.toolrental.application.customer.dto.CustomerResponse;
import com.toolrental.application.customer.exception.CustomerNotFoundApplicationException;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.customer.model.aggregate.Customer;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.customer.port.repository.CustomerRepository;
import com.toolrental.domain.user.port.repository.UserRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;

public final class ManageCustomerUseCase {
  private final CustomerRepository repository;
  private final UnitOfWork work;
  private final AccessPolicy access;
  private final WorkflowReferences references;
  private final Clock clock;
  private final UserRepository users;

  public ManageCustomerUseCase(
      CustomerRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      UserRepository users) {
    this.repository = repository;
    this.work = work;
    this.access = access;
    this.references = references;
    this.clock = clock;
    this.users = users;
  }

  private Customer load(CustomerId id) {
    return repository.findById(id).orElseThrow(CustomerNotFoundApplicationException::new);
  }

  public CustomerResponse find(UUID actor, CustomerId id) {
    return work.execute(
        () -> {
          var value = load(id);
          access.requireOwnerOrAdministrator(actor, value.id().value());
          return CustomerResponse.from(value);
        });
  }

  public List<CustomerResponse> list(UUID actor) {
    return work.execute(
        () -> {
          access.requireAdministrator(actor);
          return repository.findAll().stream().map(CustomerResponse::from).toList();
        });
  }

  public CustomerResponse register(UUID actor, RegisterCustomerCommand command) {
    return work.execute(
        () -> {
          access.requireOwnerOrAdministrator(actor, command.userId().value());
          WorkflowReferences.reference(users.findById(command.userId()).isPresent(), "User");
          access.requireRole(command.userId().value(), "CUSTOMER");
          var id = new CustomerId(command.userId().value());
          if (repository.findById(id).isPresent())
            throw new BusinessRuleException("Customer already exists");
          var value =
              Customer.register(
                  id,
                  command.personType(),
                  command.documentType(),
                  command.documentNumber(),
                  command.billingName(),
                  clock);
          var saved = repository.save(value);
          return CustomerResponse.from(saved);
        });
  }

  public CustomerResponse update(UUID actor, CustomerId id, RegisterCustomerCommand command) {
    return work.execute(
        () -> {
          var value = load(id);
          access.requireOwnerOrAdministrator(actor, value.id().value());
          WorkflowReferences.require(
              id.value().equals(command.userId().value()), "Profile identity cannot change");
          return CustomerResponse.from(
              repository.save(
                  value.update(
                      command.personType(),
                      command.documentType(),
                      command.documentNumber(),
                      command.billingName(),
                      clock)));
        });
  }
}
