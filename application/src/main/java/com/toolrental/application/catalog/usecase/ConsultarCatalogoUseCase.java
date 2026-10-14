package com.toolrental.application.catalog.usecase;

import com.toolrental.application.catalog.dto.CategoriaResponse;
import com.toolrental.application.catalog.dto.HerramientaResponse;
import com.toolrental.application.catalog.dto.UnidadEquipoResponse;
import com.toolrental.application.common.exception.RecursoNoEncontradoException;
import com.toolrental.domain.catalog.model.aggregate.Herramienta;
import com.toolrental.domain.catalog.model.valueobject.HerramientaId;
import com.toolrental.domain.catalog.port.repository.CategoriaRepository;
import com.toolrental.domain.catalog.port.repository.HerramientaRepository;
import com.toolrental.domain.catalog.port.repository.UnidadEquipoRepository;
import java.util.List;
import java.util.Objects;

public final class ConsultarCatalogoUseCase {
  private final CategoriaRepository categorias;
  private final HerramientaRepository herramientas;
  private final UnidadEquipoRepository unidades;

  public ConsultarCatalogoUseCase(
      CategoriaRepository c, HerramientaRepository h, UnidadEquipoRepository u) {
    categorias = Objects.requireNonNull(c);
    herramientas = Objects.requireNonNull(h);
    unidades = Objects.requireNonNull(u);
  }

  public List<CategoriaResponse> categorias() {
    return categorias.listar().stream().map(CategoriaResponse::desde).toList();
  }

  public List<HerramientaResponse> herramientas() {
    return herramientas.listar().stream()
        .filter(Herramienta::publicada)
        .map(HerramientaResponse::desde)
        .toList();
  }

  public HerramientaResponse herramienta(HerramientaId id) {
    return HerramientaResponse.desde(
        herramientas.buscar(id).orElseThrow(() -> new RecursoNoEncontradoException("Herramienta")));
  }

  public List<UnidadEquipoResponse> unidades(HerramientaId id) {
    herramienta(id);
    return unidades.listar().stream()
        .filter(u -> u.herramientaId().equals(id))
        .map(UnidadEquipoResponse::desde)
        .toList();
  }
}
