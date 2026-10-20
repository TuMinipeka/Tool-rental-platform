package com.toolrental.application.tool.usecase;

import com.toolrental.application.category.exception.CategoryNotFoundApplicationException;
import com.toolrental.application.supplier.exception.SupplierNotFoundApplicationException;
import com.toolrental.application.tool.command.RegisterToolCommand;
import com.toolrental.application.tool.dto.ToolResponse;
import com.toolrental.domain.category.port.repository.CategoryRepository;
import com.toolrental.domain.common.port.ParticipantsPort;
import com.toolrental.domain.tool.model.aggregate.Tool;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import java.time.Clock;
import java.util.Objects;

public final class RegisterToolUseCase {
  private final Clock clock;
  private final ToolRepository tools;
  private final CategoryRepository categories;
  private final ParticipantsPort participants;

  public RegisterToolUseCase(
      ToolRepository h, CategoryRepository c, ParticipantsPort p, Clock clock) {
    tools = Objects.requireNonNull(h);
    categories = Objects.requireNonNull(c);
    participants = Objects.requireNonNull(p);
    this.clock = Objects.requireNonNull(clock);
  }

  public ToolResponse execute(RegisterToolCommand c) {
    categories
        .findById(c.categoryId())
        .orElseThrow(() -> new CategoryNotFoundApplicationException(c.categoryId()));
    if (!participants.isSupplierActive(c.supplierId()))
      throw new SupplierNotFoundApplicationException(c.supplierId());
    var h =
        Tool.register(
            c.supplierId(),
            c.categoryId(),
            c.name(),
            c.description(),
            c.serviceCity(),
            c.dailyRate(),
            c.imageUrls(),
            clock);
    tools.save(h);
    return ToolResponse.from(h);
  }
}
