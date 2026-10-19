package com.toolrental.infrastructure.invoice.adapters.out.persistence;

import com.toolrental.domain.invoice.model.aggregate.Invoice;
import com.toolrental.domain.invoice.model.valueobject.InvoiceId;
import com.toolrental.domain.invoice.port.repository.InvoiceRepository;
import com.toolrental.infrastructure.invoice.adapters.out.persistence.mapper.InvoicePersistenceMapper;
import com.toolrental.infrastructure.invoice.adapters.out.persistence.repository.InvoiceJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class InvoicePersistenceAdapter implements InvoiceRepository {
  private final InvoiceJpaRepository rows;
  private final InvoicePersistenceMapper mapper;

  public InvoicePersistenceAdapter(InvoiceJpaRepository rows, InvoicePersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<Invoice> findById(InvoiceId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public List<Invoice> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public List<Invoice> findByParticipant(UUID participantId) {
    return rows.findByParticipantId(participantId).stream().map(mapper::toDomain).toList();
  }

  public Invoice save(Invoice value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(InvoiceId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
