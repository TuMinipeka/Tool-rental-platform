package com.toolrental.application.tool.usecase;

import com.toolrental.application.common.exception.ReferenceNotFoundApplicationException;
import com.toolrental.application.tool.dto.ToolAvailabilityResponse;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.reservation.port.repository.AvailabilityRepository;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.tool.port.repository.ToolRepository;

public final class CheckToolAvailabilityUseCase {
  private final ToolRepository tools;
  private final AvailabilityRepository availability;
  private final UnitOfWork work;

  public CheckToolAvailabilityUseCase(
      ToolRepository tools, AvailabilityRepository availability, UnitOfWork work) {
    this.tools = tools;
    this.availability = availability;
    this.work = work;
  }

  public ToolAvailabilityResponse execute(ToolId toolId, ReservationPeriod period) {
    return work.execute(
        () -> {
          tools
              .findById(toolId)
              .filter(tool -> tool.published())
              .orElseThrow(() -> new ReferenceNotFoundApplicationException("Tool"));
          return ToolAvailabilityResponse.from(
              toolId.value(), period, availability.findAvailableUnits(toolId, period));
        });
  }
}
