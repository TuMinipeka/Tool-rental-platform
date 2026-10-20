package com.toolrental.infrastructure.invoice.adapters.in.rest.controllers;

import com.toolrental.application.invoice.command.RegisterInvoiceCommand;
import com.toolrental.application.invoice.dto.InvoiceResponse;
import com.toolrental.application.invoice.usecase.DownloadInvoiceUseCase;
import com.toolrental.application.invoice.usecase.ManageInvoiceUseCase;
import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.infrastructure.common.web.Actor;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/invoices", "/api/invoices"})
public class InvoiceController {
  private final ManageInvoiceUseCase useCase;
  private final DownloadInvoiceUseCase download;

  public InvoiceController(ManageInvoiceUseCase useCase, DownloadInvoiceUseCase download) {
    this.useCase = useCase;
    this.download = download;
  }

  @GetMapping(value = "/{id}/document", produces = MediaType.TEXT_HTML_VALUE)
  public ResponseEntity<String> download(Principal principal, @PathVariable UUID id) {
    var document = download.execute(Actor.id(principal), new InvoiceId(id));
    var html =
        new StringBuilder(
            "<!doctype html><html lang=\"es\"><meta charset=\"utf-8\"><title>Comprobante academico</title><body>");
    html.append("<h1>Comprobante academico ")
        .append(escape(document.invoice().number()))
        .append("</h1><p>")
        .append(escape(document.legalNotice()))
        .append("</p><p>Proveedor: ")
        .append(escape(document.invoice().issuedSupplierName()))
        .append(" - ")
        .append(escape(document.invoice().issuedSupplierDocument()))
        .append("</p><p>Cliente: ")
        .append(escape(document.invoice().issuedCustomerName()))
        .append(" - ")
        .append(escape(document.invoice().issuedCustomerDocument()))
        .append(
            "</p><table><thead><tr><th>Concepto</th><th>Cantidad</th><th>Valor unitario</th></tr></thead><tbody>");
    for (var line : document.lines()) {
      html.append("<tr><td>")
          .append(escape(line.issuedDescription()))
          .append("</td><td>")
          .append(line.quantity().toPlainString())
          .append("</td><td>")
          .append(line.issuedUnitPrice().toPlainString())
          .append("</td></tr>");
    }
    html.append("</tbody></table><p>Total: ")
        .append(document.total().toPlainString())
        .append(" ")
        .append(document.currency())
        .append(
            "</p><p>El tratamiento tributario definitivo esta pendiente de validacion profesional.</p></body></html>");
    return ResponseEntity.ok()
        .header(
            HttpHeaders.CONTENT_DISPOSITION,
            "attachment; filename=academic-invoice-" + id + ".html")
        .contentType(MediaType.TEXT_HTML)
        .body(html.toString());
  }

  private String escape(String value) {
    return org.springframework.web.util.HtmlUtils.htmlEscape(value);
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
