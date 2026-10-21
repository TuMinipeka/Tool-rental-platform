package com.toolrental.domain.category.port.repository;

import com.toolrental.domain.category.model.aggregate.Category;
import com.toolrental.domain.category.model.valueobject.CategoryId;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
  Optional<Category> findById(CategoryId id);

  List<Category> findAll();

  void delete(CategoryId id);

  void save(Category aggregate);
}
