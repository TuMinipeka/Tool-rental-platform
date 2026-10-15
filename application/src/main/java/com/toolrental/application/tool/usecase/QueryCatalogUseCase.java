package com.toolrental.application.tool.usecase;

import com.toolrental.application.category.dto.CategoryResponse;
import com.toolrental.application.equipmentunit.dto.EquipmentUnitResponse;
import com.toolrental.application.tool.dto.ToolResponse;
import com.toolrental.application.tool.exception.ToolNotFoundApplicationException;
import com.toolrental.domain.category.port.repository.CategoryRepository;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.tool.model.aggregate.Tool;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.tool.port.repository.ToolRepository;
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
