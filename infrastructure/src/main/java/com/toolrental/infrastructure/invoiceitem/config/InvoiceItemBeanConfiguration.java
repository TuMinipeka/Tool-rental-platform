package com.toolrental.infrastructure.invoiceitem.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.invoiceitem.usecase.ManageInvoiceItemUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.invoice.port.repository.InvoiceRepository;
import com.toolrental.domain.invoiceitem.port.repository.InvoiceItemRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class InvoiceItemBeanConfiguration {
  @Bean
  ManageInvoiceItemUseCase manageInvoiceItemUseCase(
      InvoiceItemRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      InvoiceRepository invoices) {
    return new ManageInvoiceItemUseCase(repository, work, access, references, clock, invoices);
  }
}
