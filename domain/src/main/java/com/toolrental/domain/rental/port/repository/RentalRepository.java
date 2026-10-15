package com.toolrental.domain.rental.port.repository;

import com.toolrental.domain.rental.model.aggregate.Rental;
import com.toolrental.domain.rental.model.valueobject.RentalId;
import java.util.List;
import java.util.Optional;

public interface RentalRepository {
  Optional<Rental> findById(RentalId id);

  List<Rental> findAll();

  Rental save(Rental value);

  void delete(RentalId id);
}
