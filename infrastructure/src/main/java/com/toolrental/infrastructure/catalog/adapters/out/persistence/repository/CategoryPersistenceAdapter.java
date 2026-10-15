package com.toolrental.infrastructure.catalog.adapters.out.persistence.repository;

import com.toolrental.domain.catalog.model.aggregate.Category;
import com.toolrental.domain.catalog.model.valueobject.CategoryId;
import com.toolrental.domain.catalog.port.repository.CategoryRepository;
import com.toolrental.infrastructure.catalog.adapters.out.persistence.mapper.CategoryPersistenceMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class CategoryPersistenceAdapter implements CategoryRepository {
  private final CategoryJpaRepository repository;
  private final CategoryPersistenceMapper mapper;

  public CategoryPersistenceAdapter(
      CategoryJpaRepository repository, CategoryPersistenceMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Optional<Category> findById(CategoryId id) {
    return repository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  public List<Category> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Override
  @Transactional
  public void save(Category aggregate) {
    repository.save(mapper.toEntity(aggregate));
  }
}
