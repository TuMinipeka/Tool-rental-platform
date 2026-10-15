package com.toolrental.infrastructure.category.adapters.out.persistence;

import com.toolrental.domain.category.model.aggregate.Category;
import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.domain.category.port.repository.CategoryRepository;
import com.toolrental.infrastructure.category.adapters.out.persistence.mapper.CategoryPersistenceMapper;
import com.toolrental.infrastructure.category.adapters.out.persistence.repository.CategoryJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
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
    return repository.findByIdForUpdate(id.value()).map(mapper::toDomain);
  }

  @Override
  public List<Category> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  public void delete(CategoryId id) {
    repository.deleteById(id.value());
    repository.flush();
  }

  @Override
  @Transactional
  public void save(Category aggregate) {
    repository.save(mapper.toEntity(aggregate));
  }
}
