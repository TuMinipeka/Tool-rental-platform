package com.toolrental.application.catalog.usecase;

import com.toolrental.application.catalog.dto.CategoryResponse;
import com.toolrental.application.catalog.dto.EquipmentUnitResponse;
import com.toolrental.application.catalog.dto.ToolResponse;
import com.toolrental.application.catalog.exception.ToolNotFoundApplicationException;
import com.toolrental.domain.catalog.model.aggregate.Tool;
import com.toolrental.domain.catalog.model.valueobject.ToolId;
import com.toolrental.domain.catalog.port.repository.CategoryRepository;
import com.toolrental.domain.catalog.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.catalog.port.repository.ToolRepository;
import java.util.List;
import java.util.Objects;

public final class QueryCatalogUseCase {
  private final CategoryRepository categories;
  private final ToolRepository tools;
  private final EquipmentUnitRepository equipmentUnits;

  public QueryCatalogUseCase(CategoryRepository c, ToolRepository h, EquipmentUnitRepository u) {
    categories = Objects.requireNonNull(c);
    tools = Objects.requireNonNull(h);
    equipmentUnits = Objects.requireNonNull(u);
  }

  public List<CategoryResponse> categories() {
    return categories.findAll().stream().map(CategoryResponse::from).toList();
  }

  public List<ToolResponse> tools() {
    return tools.findAll().stream().filter(Tool::published).map(ToolResponse::from).toList();
  }

  public ToolResponse tool(ToolId id) {
    return ToolResponse.from(
        tools.findById(id).orElseThrow(() -> new ToolNotFoundApplicationException(id)));
  }

  public List<EquipmentUnitResponse> equipmentUnits(ToolId id) {
    tool(id);
    return equipmentUnits.findAll().stream()
        .filter(u -> u.toolId().equals(id))
        .map(EquipmentUnitResponse::from)
        .toList();
  }
}
