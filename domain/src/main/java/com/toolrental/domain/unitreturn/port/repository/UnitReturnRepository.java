package com.toolrental.domain.unitreturn.port.repository;

import com.toolrental.domain.unitreturn.model.aggregate.UnitReturn;
import com.toolrental.domain.unitreturn.model.valueobject.UnitReturnId;
import java.util.List;
import java.util.Optional;

public interface UnitReturnRepository {
  Optional<UnitReturn> findById(UnitReturnId id);

  List<UnitReturn> findAll();

  UnitReturn save(UnitReturn value);

  void delete(UnitReturnId id);
}
