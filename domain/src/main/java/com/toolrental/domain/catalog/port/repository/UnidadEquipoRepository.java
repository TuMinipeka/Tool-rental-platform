package com.toolrental.domain.catalog.port.repository;

import com.toolrental.domain.catalog.model.aggregate.UnidadEquipo;
import com.toolrental.domain.catalog.model.valueobject.UnidadEquipoId;
import java.util.List;
import java.util.Optional;

public interface UnidadEquipoRepository {
  Optional<UnidadEquipo> buscar(UnidadEquipoId id);

  List<UnidadEquipo> listar();

  void guardar(UnidadEquipo agregado);
}
