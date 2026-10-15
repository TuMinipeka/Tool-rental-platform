package com.toolrental.infrastructure.toolimage.adapters.out.persistence;

import com.toolrental.domain.toolimage.model.aggregate.ToolImage;
import com.toolrental.domain.toolimage.model.valueobject.ToolImageId;
import com.toolrental.domain.toolimage.port.repository.ToolImageRepository;
import com.toolrental.infrastructure.toolimage.adapters.out.persistence.mapper.ToolImagePersistenceMapper;
import com.toolrental.infrastructure.toolimage.adapters.out.persistence.repository.ToolImageJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class ToolImagePersistenceAdapter implements ToolImageRepository {
  private final ToolImageJpaRepository rows;
  private final ToolImagePersistenceMapper mapper;

  public ToolImagePersistenceAdapter(
      ToolImageJpaRepository rows, ToolImagePersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<ToolImage> findById(ToolImageId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public List<ToolImage> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public ToolImage save(ToolImage value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(ToolImageId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
