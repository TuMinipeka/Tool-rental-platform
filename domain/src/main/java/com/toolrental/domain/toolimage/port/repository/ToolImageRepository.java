package com.toolrental.domain.toolimage.port.repository;

import com.toolrental.domain.toolimage.model.aggregate.ToolImage;
import com.toolrental.domain.toolimage.model.valueobject.ToolImageId;
import java.util.List;
import java.util.Optional;

public interface ToolImageRepository {
  Optional<ToolImage> findById(ToolImageId id);

  List<ToolImage> findAll();

  default List<ToolImage> findByToolId(com.toolrental.domain.tool.model.valueobject.ToolId id) {
    return findAll().stream().filter(image -> image.id().toolId().equals(id)).toList();
  }

  ToolImage save(ToolImage value);

  void delete(ToolImageId id);
}
