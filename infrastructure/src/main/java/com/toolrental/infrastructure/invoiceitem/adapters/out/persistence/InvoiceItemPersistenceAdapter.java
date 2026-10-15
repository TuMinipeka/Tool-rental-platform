package com.toolrental.infrastructure.invoiceitem.adapters.out.persistence;

import com.toolrental.domain.invoiceitem.model.aggregate.InvoiceItem;
import com.toolrental.domain.invoiceitem.model.valueobject.InvoiceItemId;
import com.toolrental.domain.invoiceitem.port.repository.InvoiceItemRepository;
import com.toolrental.infrastructure.invoiceitem.adapters.out.persistence.mapper.InvoiceItemPersistenceMapper;
import com.toolrental.infrastructure.invoiceitem.adapters.out.persistence.repository.InvoiceItemJpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class InvoiceItemPersistenceAdapter implements InvoiceItemRepository {
  private final InvoiceItemJpaRepository rows;
  private final InvoiceItemPersistenceMapper mapper;

  public InvoiceItemPersistenceAdapter(
      InvoiceItemJpaRepository rows, InvoiceItemPersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<InvoiceItem> findById(InvoiceItemId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public List<InvoiceItem> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public InvoiceItem save(InvoiceItem value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(InvoiceItemId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
