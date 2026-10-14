package com.toolrental.domain.catalog.port.repository;

import com.toolrental.domain.catalog.model.aggregate.Herramienta;
import com.toolrental.domain.catalog.model.valueobject.HerramientaId;
import java.util.List;
import java.util.Optional;

public interface HerramientaRepository {
  Optional<Herramienta> buscar(HerramientaId id);

  List<Herramienta> listar();

  void guardar(Herramienta agregado);
}
