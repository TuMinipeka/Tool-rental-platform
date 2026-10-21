package com.toolrental.infrastructure.invoice.config;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.invoice.usecase.DownloadInvoiceUseCase;
import com.toolrental.application.invoice.usecase.IssueAcademicInvoiceUseCase;
import com.toolrental.application.invoice.usecase.ManageInvoiceUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.customer.port.repository.CustomerRepository;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.invoice.port.repository.InvoiceRepository;
import com.toolrental.domain.invoiceitem.port.repository.InvoiceItemRepository;
import com.toolrental.domain.rental.port.repository.RentalRepository;
import com.toolrental.domain.supplier.port.repository.SupplierRepository;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class InvoiceBeanConfiguration {
  @Bean
  ManageInvoiceUseCase manageInvoiceUseCase(
      InvoiceRepository repository,
      UnitOfWork work,
      AccessPolicy access,
      WorkflowReferences references,
      Clock clock,
      RentalRepository rentals,
      CustomerRepository customers,
      SupplierRepository suppliers,
      InvoiceItemRepository lines) {
    return new ManageInvoiceUseCase(
        repository, work, access, references, clock, rentals, customers, suppliers, lines);
  }

  @Bean
  IssueAcademicInvoiceUseCase issueAcademicInvoiceUseCase(
      InvoiceRepository invoices,
      InvoiceItemRepository lines,
      CustomerRepository customers,
      SupplierRepository suppliers,
      EquipmentUnitRepository equipmentUnits,
      ToolRepository tools,
      Clock clock) {
    return new IssueAcademicInvoiceUseCase(
        invoices, lines, customers, suppliers, equipmentUnits, tools, clock);
  }

  @Bean
  DownloadInvoiceUseCase downloadInvoiceUseCase(
      InvoiceRepository invoices,
      InvoiceItemRepository lines,
      WorkflowReferences references,
      UnitOfWork work) {
    return new DownloadInvoiceUseCase(invoices, lines, references, work);
  }
}
