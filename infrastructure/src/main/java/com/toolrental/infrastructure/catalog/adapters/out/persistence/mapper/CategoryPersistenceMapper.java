package com.toolrental.infrastructure.catalog.adapters.out.persistence.mapper;

import com.toolrental.domain.catalog.model.aggregate.Category;
import com.toolrental.domain.catalog.model.valueobject.CategoryId;
import com.toolrental.infrastructure.catalog.adapters.out.persistence.entity.CategoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryPersistenceMapper {
  public Category toDomain(CategoryJpaEntity entity) {
    return Category.restore(new CategoryId(entity.getId()), entity.getName());
  }

  public CategoryJpaEntity toEntity(Category aggregate) {
    return new CategoryJpaEntity(aggregate.id().value(), aggregate.name());
  }
}
