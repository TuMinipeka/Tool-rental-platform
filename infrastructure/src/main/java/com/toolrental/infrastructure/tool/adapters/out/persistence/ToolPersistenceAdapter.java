package com.toolrental.infrastructure.tool.adapters.out.persistence;
import com.toolrental.infrastructure.tool.adapters.out.persistence.repository.ToolJpaRepository;


import com.toolrental.domain.tool.model.aggregate.Tool;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import com.toolrental.infrastructure.tool.adapters.out.persistence.mapper.ToolPersistenceMapper;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class ToolPersistenceAdapter implements ToolRepository {
  private final ToolJpaRepository repository;
  private final ToolPersistenceMapper mapper;

  public ToolPersistenceAdapter(ToolJpaRepository repository, ToolPersistenceMapper mapper) {
    this.repository = repository;
    this.mapper = mapper;
  }

  @Override
  public Optional<Tool> findById(ToolId id) {
    return repository.findById(id.value()).map(mapper::toDomain);
  }

  @Override
  public List<Tool> findAll() {
    return repository.findAll().stream().map(mapper::toDomain).toList();
  }

  @Override
  @Transactional
  public void save(Tool aggregate) {
    repository.save(mapper.toEntity(aggregate));
  }
}
