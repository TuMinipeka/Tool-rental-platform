package com.toolrental.domain.catalog.model.aggregate;

import com.toolrental.domain.catalog.event.HerramientaRegistrada;
import com.toolrental.domain.catalog.model.valueobject.CategoriaId;
import com.toolrental.domain.catalog.model.valueobject.HerramientaId;
import com.toolrental.domain.common.exception.ReglaNegocioException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.common.model.valueobject.Dinero;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import com.toolrental.domain.common.model.valueobject.Validacion;
import java.net.URI;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;
import java.util.Set;

public final class Herramienta extends AggregateRoot {
  private final HerramientaId id;
  private final ProveedorId proveedorId;
  private final CategoriaId categoriaId;
  private final String nombre;
  private final String descripcion;
  private final Set<URI> imagenes;
  private Dinero tarifaDiaria;
  private boolean publicada;

  private Herramienta(
      HerramientaId id,
      ProveedorId proveedor,
      CategoriaId categoria,
      String nombre,
      String descripcion,
      Dinero tarifa,
      Set<URI> imagenes,
      boolean publicada) {
    this.id = Objects.requireNonNull(id);
    this.publicada = publicada;
    this.proveedorId = Objects.requireNonNull(proveedor);
    this.categoriaId = Objects.requireNonNull(categoria);
    this.nombre = Validacion.texto(nombre, "nombre");
    this.descripcion = Validacion.texto(descripcion, "descripcion");
    this.tarifaDiaria = Objects.requireNonNull(tarifa);
    this.imagenes = Set.copyOf(imagenes);
    for (URI imagen : this.imagenes) {
      if (!("https".equalsIgnoreCase(imagen.getScheme())
              || "http".equalsIgnoreCase(imagen.getScheme()))
          || imagen.getHost() == null)
        throw new ReglaNegocioException("Imagen debe tener URL HTTP(S) absoluta");
    }
  }

  public static Herramienta registrar(
      ProveedorId proveedor,
      CategoriaId categoria,
      String nombre,
      String descripcion,
      Dinero tarifa,
      Set<URI> imagenes,
      Clock clock) {
    var herramienta =
        new Herramienta(
            HerramientaId.generar(),
            proveedor,
            categoria,
            nombre,
            descripcion,
            tarifa,
            imagenes,
            true);
    herramienta.recordEvent(new HerramientaRegistrada(herramienta.id, Instant.now(clock)));
    return herramienta;
  }

  public static Herramienta restaurar(
      HerramientaId id,
      ProveedorId proveedor,
      CategoriaId categoria,
      String nombre,
      String descripcion,
      Dinero tarifa,
      Set<URI> imagenes,
      boolean publicada) {
    return new Herramienta(
        id, proveedor, categoria, nombre, descripcion, tarifa, imagenes, publicada);
  }

  public void cambiarTarifa(Dinero tarifa) {
    this.tarifaDiaria = Objects.requireNonNull(tarifa);
  }

  public void retirarPublicacion() {
    publicada = false;
  }

  public HerramientaId id() {
    return id;
  }

  public ProveedorId proveedorId() {
    return proveedorId;
  }

  public CategoriaId categoriaId() {
    return categoriaId;
  }

  public String nombre() {
    return nombre;
  }

  public String descripcion() {
    return descripcion;
  }

  public Set<URI> imagenes() {
    return imagenes;
  }

  public Dinero tarifaDiaria() {
    return tarifaDiaria;
  }

  public boolean publicada() {
    return publicada;
  }
}
