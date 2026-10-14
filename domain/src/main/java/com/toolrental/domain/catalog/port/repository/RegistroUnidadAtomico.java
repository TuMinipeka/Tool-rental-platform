package com.toolrental.domain.catalog.port.repository;

import com.toolrental.domain.catalog.model.aggregate.UnidadEquipo;
import com.toolrental.domain.catalog.model.valueobject.HerramientaId;

/** Debe validar herramienta y unicidad del código por proveedor y guardar en una transacción. */
public interface RegistroUnidadAtomico {
  UnidadEquipo registrar(HerramientaId herramientaId, String codigoInventario);
}
