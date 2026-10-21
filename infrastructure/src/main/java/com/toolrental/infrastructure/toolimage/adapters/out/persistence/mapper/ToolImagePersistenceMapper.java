package com.toolrental.infrastructure.toolimage.adapters.out.persistence.mapper;

import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.toolimage.model.aggregate.ToolImage;
import com.toolrental.domain.toolimage.model.valueobject.ToolImageId;
import com.toolrental.infrastructure.toolimage.adapters.out.persistence.entity.ToolImageJpaEntity;
import com.toolrental.infrastructure.toolimage.adapters.out.persistence.entity.ToolImageJpaId;
import org.springframework.stereotype.Component;

@Component
public class ToolImagePersistenceMapper {
  public ToolImageJpaId key(ToolImageId id) {
    return new ToolImageJpaId(id.toolId().value(), id.imageUrl());
  }

  public ToolImage toDomain(ToolImageJpaEntity row) {
    return ToolImage.restore(
        new ToolImageId(new ToolId(row.getId().getToolId()), row.getId().getImageUrl()));
  }

  public ToolImageJpaEntity toEntity(ToolImage value) {
    return new ToolImageJpaEntity(key(value.id()));
  }
}
