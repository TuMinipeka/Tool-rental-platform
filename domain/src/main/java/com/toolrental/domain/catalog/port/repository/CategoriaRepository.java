package com.toolrental.domain.catalog.port.repository;

import com.toolrental.domain.catalog.model.aggregate.Categoria;
import com.toolrental.domain.catalog.model.valueobject.CategoriaId;
import java.util.List;
import java.util.Optional;

public interface CategoriaRepository {
  Optional<Categoria> buscar(CategoriaId id);

  List<Categoria> listar();

  void guardar(Categoria agregado);
}
