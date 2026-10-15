package com.toolrental.infrastructure.invoiceitem.adapters.in.rest.controllers;

import com.toolrental.application.invoiceitem.dto.InvoiceItemResponse;
import com.toolrental.application.invoiceitem.usecase.ManageInvoiceItemUseCase;
import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import com.toolrental.domain.invoiceitem.model.valueobject.InvoiceItemId;
import com.toolrental.infrastructure.common.web.Actor;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/invoice-items")
public class InvoiceItemController {
  private final ManageInvoiceItemUseCase useCase;

  public InvoiceItemController(ManageInvoiceItemUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  public List<InvoiceItemResponse> list(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @GetMapping("/entry")
  public InvoiceItemResponse find(
      Principal principal, @RequestParam UUID invoiceId, @RequestParam Integer lineNumber) {
    return useCase.find(
        Actor.id(principal), new InvoiceItemId(new InvoiceId(invoiceId), lineNumber));
  }
}
