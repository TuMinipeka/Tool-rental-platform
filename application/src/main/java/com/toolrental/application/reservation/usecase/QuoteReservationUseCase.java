package com.toolrental.application.reservation.usecase;

import com.toolrental.application.common.exception.ReferenceNotFoundApplicationException;
import com.toolrental.application.reservation.command.QuoteReservationCommand;
import com.toolrental.application.reservation.dto.ReservationQuoteResponse;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.reservation.model.valueobject.ReservationQuote;
import com.toolrental.domain.reservation.port.repository.AvailabilityRepository;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import java.util.HashSet;
import java.util.UUID;

public final class QuoteReservationUseCase {
  private final EquipmentUnitRepository units;
  private final ToolRepository tools;
  private final AvailabilityRepository availability;
  private final AccessPolicy access;
  private final UnitOfWork work;

  public QuoteReservationUseCase(
      EquipmentUnitRepository units,
      ToolRepository tools,
      AvailabilityRepository availability,
      AccessPolicy access,
      UnitOfWork work) {
    this.units = units;
    this.tools = tools;
    this.availability = availability;
    this.access = access;
    this.work = work;
  }

  public ReservationQuoteResponse execute(UUID actor, QuoteReservationCommand command) {
    return work.execute(
        () -> {
          access.requireRole(actor, "CUSTOMER");
          if (command.equipmentUnitIds().isEmpty())
            throw new BusinessRuleException("A quote requires at least one equipment unit");
          if (new HashSet<>(command.equipmentUnitIds()).size() != command.equipmentUnitIds().size())
            throw new BusinessRuleException("An equipment unit cannot be repeated");

          var selectedUnits =
              command.equipmentUnitIds().stream()
                  .map(
                      id ->
                          units
                              .findById(id)
                              .orElseThrow(
                                  () ->
                                      new ReferenceNotFoundApplicationException("Equipment unit")))
                  .toList();
          var selectedTools =
              selectedUnits.stream()
                  .map(
                      unit ->
                          tools
                              .findById(unit.toolId())
                              .filter(tool -> tool.published())
                              .orElseThrow(() -> new ReferenceNotFoundApplicationException("Tool")))
                  .toList();
          var supplier = selectedTools.getFirst().supplierId();
          if (selectedTools.stream().anyMatch(tool -> !supplier.equals(tool.supplierId())))
            throw new BusinessRuleException("All quoted units must belong to one supplier");
          for (var unit : selectedUnits) {
            if (!unit.isOperational()
                || availability.findAvailableUnits(unit.toolId(), command.period()).stream()
                    .noneMatch(available -> available.id().equals(unit.id()))) {
              throw new BusinessRuleException("Equipment unit is unavailable for the period");
            }
          }
          var quote =
              ReservationQuote.calculate(
                  command.period(), selectedTools.stream().map(tool -> tool.dailyRate()).toList());
          return ReservationQuoteResponse.from(quote);
        });
  }
}
