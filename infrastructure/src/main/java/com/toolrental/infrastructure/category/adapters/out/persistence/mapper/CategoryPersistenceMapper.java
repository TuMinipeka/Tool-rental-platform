package com.toolrental.infrastructure.category.adapters.out.persistence.mapper;

import com.toolrental.domain.category.model.aggregate.Category;
import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.infrastructure.category.adapters.out.persistence.entity.CategoryJpaEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryPersistenceMapper {
  public Category toDomain(CategoryJpaEntity entity) {
    return Category.restore(new CategoryId(entity.getId()), entity.getName(), entity.getDescription());
  }

  public CategoryJpaEntity toEntity(Category aggregate) {
    var entity = new CategoryJpaEntity(aggregate.id().value(), aggregate.name()); entity.preserveDescription(aggregate.description()); return entity;
  }
}
