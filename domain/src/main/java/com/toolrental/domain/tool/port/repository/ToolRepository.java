package com.toolrental.domain.tool.port.repository;

import com.toolrental.domain.tool.model.aggregate.Tool;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import java.util.List;
import java.util.Optional;

public interface ToolRepository {
  Optional<Tool> findById(ToolId id);

  List<Tool> findAll();

  void save(Tool aggregate);
}
