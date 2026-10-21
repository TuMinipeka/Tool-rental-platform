package com.toolrental.application.common.service;

import com.toolrental.application.common.exception.ReferenceNotFoundApplicationException;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.port.repository.ReservationRepository;
import com.toolrental.domain.tool.model.aggregate.Tool;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import java.util.UUID;

public final class WorkflowReferences {
  private final ReservationRepository reservations;
  private final ToolRepository tools;
  private final AccessPolicy access;

  public WorkflowReferences(
      ReservationRepository reservations, ToolRepository tools, AccessPolicy access) {
    this.reservations = reservations;
    this.tools = tools;
    this.access = access;
  }

  public Reservation reservation(UUID actor, ReservationId id, boolean supplierOnly) {
    var value =
        reservations
            .findById(id)
            .orElseThrow(() -> new ReferenceNotFoundApplicationException("Reservation"));
    if (supplierOnly) access.requireOwnerOrAdministrator(actor, value.supplierId().value());
    else {
      access.requireActive(actor);
      if (!actor.equals(value.customerId().value()) && !actor.equals(value.supplierId().value()))
        access.requireAdministrator(actor);
    }
    return value;
  }

  public Tool tool(UUID actor, ToolId id) {
    var value =
        tools.findById(id).orElseThrow(() -> new ReferenceNotFoundApplicationException("Tool"));
    access.requireOwnerOrAdministrator(actor, value.supplierId().value());
    return value;
  }

  public void item(Reservation reservation, EquipmentUnitId unit) {
    if (reservation.items().stream().noneMatch(i -> i.equipmentUnitId().equals(unit)))
      throw new ReferenceNotFoundApplicationException("Reservation item");
  }

  public static void require(boolean condition, String message) {
    if (!condition) throw new BusinessRuleException(message);
  }

  public static void reference(boolean condition, String resource) {
    if (!condition) throw new ReferenceNotFoundApplicationException(resource);
  }
}
