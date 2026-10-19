package com.toolrental.infrastructure.rental.adapters.out.persistence;

import com.toolrental.domain.rental.model.aggregate.Rental;
import com.toolrental.domain.rental.model.valueobject.RentalId;
import com.toolrental.domain.rental.port.repository.RentalRepository;
import com.toolrental.infrastructure.rental.adapters.out.persistence.mapper.RentalPersistenceMapper;
import com.toolrental.infrastructure.rental.adapters.out.persistence.repository.RentalJpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
public class RentalPersistenceAdapter implements RentalRepository {
  private final RentalJpaRepository rows;
  private final RentalPersistenceMapper mapper;

  public RentalPersistenceAdapter(RentalJpaRepository rows, RentalPersistenceMapper mapper) {
    this.rows = rows;
    this.mapper = mapper;
  }

  public Optional<Rental> findById(RentalId id) {
    return rows.findLocked(mapper.key(id)).map(mapper::toDomain);
  }

  public List<Rental> findAll() {
    return rows.findAll().stream().map(mapper::toDomain).toList();
  }

  public List<Rental> findByParticipant(UUID participantId) {
    return rows.findByParticipantId(participantId).stream().map(mapper::toDomain).toList();
  }

  public Rental save(Rental value) {
    return mapper.toDomain(rows.saveAndFlush(mapper.toEntity(value)));
  }

  public void delete(RentalId id) {
    rows.deleteById(mapper.key(id));
    rows.flush();
  }
}
