package com.toolrental.infrastructure.refund.adapters.in.rest.controllers;

import com.toolrental.application.refund.dto.RefundResponse;
import com.toolrental.application.refund.usecase.QueryRefundUseCase;
import com.toolrental.domain.payment.model.valueobject.PaymentId;
import com.toolrental.infrastructure.common.web.Actor;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping({"/api/v1/refunds", "/api/refunds"})
public class RefundController {
  private final QueryRefundUseCase query;

  public RefundController(QueryRefundUseCase query) {
    this.query = query;
  }

  @GetMapping("/payments/{id}")
  public List<RefundResponse> byPayment(Principal p, @PathVariable UUID id) {
    return query.byPayment(Actor.id(p), new PaymentId(id));
  }
}
