package com.toolrental.domain.catalog.port.repository;

import com.toolrental.domain.catalog.model.aggregate.Category;
import com.toolrental.domain.catalog.model.valueobject.CategoryId;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
  Optional<Category> findById(CategoryId id);

  List<Category> findAll();

  void save(Category aggregate);
}
