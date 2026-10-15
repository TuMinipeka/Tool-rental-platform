package com.toolrental.domain.catalog.port.repository;

import com.toolrental.domain.catalog.model.aggregate.Tool;
import com.toolrental.domain.catalog.model.valueobject.ToolId;
import java.util.List;
import java.util.Optional;

public interface ToolRepository {
  Optional<Tool> findById(ToolId id);

  List<Tool> findAll();

  void save(Tool aggregate);
}
