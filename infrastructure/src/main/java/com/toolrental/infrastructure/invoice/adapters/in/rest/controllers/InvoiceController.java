package com.toolrental.infrastructure.invoice.adapters.in.rest.controllers;

import com.toolrental.application.invoice.command.RegisterInvoiceCommand;
import com.toolrental.application.invoice.dto.InvoiceResponse;
import com.toolrental.application.invoice.usecase.DownloadInvoiceUseCase;
import com.toolrental.application.invoice.usecase.ManageInvoiceUseCase;
import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.invoice.adapters.in.rest.documents.InvoicePdfRenderer;
import com.toolrental.infrastructure.invoice.adapters.in.rest.dtos.InvoiceRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/invoices", "/api/invoices"})
public class InvoiceController {
  private final ManageInvoiceUseCase useCase;
  private final DownloadInvoiceUseCase download;
  private final InvoicePdfRenderer pdf;

  public InvoiceController(
      ManageInvoiceUseCase useCase, DownloadInvoiceUseCase download, InvoicePdfRenderer pdf) {
    this.useCase = useCase;
    this.download = download;
    this.pdf = pdf;
  }

  @GetMapping(value = "/{id}/document", produces = MediaType.APPLICATION_PDF_VALUE)
  public ResponseEntity<byte[]> download(
      Principal principal,
      @PathVariable UUID id,
      @RequestParam(defaultValue = "inline") String disposition) {
    var document = download.execute(Actor.id(principal), new InvoiceId(id));
    var downloadRequested = "attachment".equalsIgnoreCase(disposition);
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            (downloadRequested ? "attachment" : "inline")
                + "; filename=academic-invoice-"
                + id
                + ".pdf")
        .contentType(MediaType.APPLICATION_PDF)
        .body(pdf.render(document));
  }

  @GetMapping
  public List<InvoiceResponse> list(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @GetMapping("/{id}")
  public InvoiceResponse find(Principal principal, @PathVariable UUID id) {
    return useCase.find(Actor.id(principal), new InvoiceId(id));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public InvoiceResponse register(Principal principal, @Valid @RequestBody InvoiceRequest request) {
    return useCase.register(
        Actor.id(principal),
        new RegisterInvoiceCommand(
            new ReservationId(request.reservationId()),
            request.number(),
            request.lines().stream()
                .map(
                    line ->
                        new com.toolrental.application.invoice.command.InvoiceLineCommand(
                            line.description(), line.quantity(), line.unitPrice()))
                .toList()));
  }

  @PatchMapping("/{id}/status")
  public InvoiceResponse transition(
      Principal principal,
      @PathVariable UUID id,
      @Valid @RequestBody com.toolrental.infrastructure.common.web.StatusRequest request) {
    return useCase.transition(Actor.id(principal), new InvoiceId(id), request.status());
  }
}
