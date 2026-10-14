package com.toolrental.application;

import com.toolrental.application.common.exception.RecursoNoEncontradoException;
import com.toolrental.domain.catalog.model.aggregate.Categoria;
import com.toolrental.domain.catalog.model.aggregate.Herramienta;
import com.toolrental.domain.catalog.model.aggregate.UnidadEquipo;
import com.toolrental.domain.catalog.model.valueobject.CategoriaId;
import com.toolrental.domain.catalog.model.valueobject.HerramientaId;
import com.toolrental.domain.catalog.model.valueobject.UnidadEquipoId;
import com.toolrental.domain.catalog.port.repository.CategoriaRepository;
import com.toolrental.domain.catalog.port.repository.HerramientaRepository;
import com.toolrental.domain.catalog.port.repository.RegistroUnidadAtomico;
import com.toolrental.domain.catalog.port.repository.UnidadEquipoRepository;
import com.toolrental.domain.common.exception.ReglaNegocioException;
import com.toolrental.domain.common.model.valueobject.ClienteId;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import com.toolrental.domain.common.port.ParticipantesPort;
import com.toolrental.domain.reservation.model.aggregate.Reserva;
import com.toolrental.domain.reservation.model.valueobject.EstadoReserva;
import com.toolrental.domain.reservation.model.valueobject.PeriodoReserva;
import com.toolrental.domain.reservation.model.valueobject.ReservaId;
import com.toolrental.domain.reservation.port.repository.GestionReservaAtomica;
import com.toolrental.domain.reservation.port.repository.ReservaRepository;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Adaptadores de prueba exclusivamente. No prueban transacciones ni bloqueos PostgreSQL. */
final class Fixture {
  final Map<CategoriaId, Categoria> categorias = new HashMap<>();
  final Map<HerramientaId, Herramienta> herramientas = new HashMap<>();
  final Map<UnidadEquipoId, UnidadEquipo> unidades = new HashMap<>();
  final Map<ReservaId, Reserva> reservas = new HashMap<>();
  final Set<ClienteId> clientes = new HashSet<>();
  final Set<ProveedorId> proveedores = new HashSet<>();

  final ParticipantesPort participantes =
      new ParticipantesPort() {
        public boolean clienteActivo(ClienteId id) {
          return clientes.contains(id);
        }

        public boolean proveedorActivo(ProveedorId id) {
          return proveedores.contains(id);
        }
      };
  final CategoriaRepository categoriaRepository =
      new CategoriaRepository() {
        public Optional<Categoria> buscar(CategoriaId id) {
          return Optional.ofNullable(categorias.get(id));
        }

        public List<Categoria> listar() {
          return List.copyOf(categorias.values());
        }

        public void guardar(Categoria c) {
          categorias.put(c.id(), c);
        }
      };
  final HerramientaRepository herramientaRepository =
      new HerramientaRepository() {
        public Optional<Herramienta> buscar(HerramientaId id) {
          return Optional.ofNullable(herramientas.get(id));
        }

        public List<Herramienta> listar() {
          return List.copyOf(herramientas.values());
        }

        public void guardar(Herramienta h) {
          herramientas.put(h.id(), h);
        }
      };
  final UnidadEquipoRepository unidadRepository =
      new UnidadEquipoRepository() {
        public Optional<UnidadEquipo> buscar(UnidadEquipoId id) {
          return Optional.ofNullable(unidades.get(id));
        }

        public List<UnidadEquipo> listar() {
          return List.copyOf(unidades.values());
        }

        public void guardar(UnidadEquipo u) {
          unidades.put(u.id(), u);
        }
      };
  final ReservaRepository reservaRepository =
      new ReservaRepository() {
        public Optional<Reserva> buscar(ReservaId id) {
          return Optional.ofNullable(reservas.get(id));
        }

        public void agregarSolicitud(Reserva r) {
          if (r.estado() != EstadoReserva.PENDIENTE || reservas.containsKey(r.id()))
            throw new ReglaNegocioException("Solicitud inválida");
          reservas.put(r.id(), r);
        }
      };
  final RegistroUnidadAtomico registroUnidad =
      (id, codigo) -> {
        synchronized (this) {
          var h = herramienta(id);
          var unidad = UnidadEquipo.registrar(id, codigo);
          boolean duplicado =
              unidades.values().stream()
                  .anyMatch(
                      u ->
                          u.codigoInventario().equals(unidad.codigoInventario())
                              && herramienta(u.herramientaId())
                                  .proveedorId()
                                  .equals(h.proveedorId()));
          if (duplicado) throw new ReglaNegocioException("Código duplicado para proveedor");
          unidades.put(unidad.id(), unidad);
          return unidad;
        }
      };
  final GestionReservaAtomica gestion =
      (id, operacion) -> {
        synchronized (this) {
          var aislada = reserva(id).copia();
          var contexto =
              new GestionReservaAtomica.Contexto() {
                public Reserva reserva() {
                  return aislada;
                }

                public UnidadEquipo unidad(UnidadEquipoId unidadId) {
                  return unidadRepository
                      .buscar(unidadId)
                      .orElseThrow(() -> new RecursoNoEncontradoException("Unidad"));
                }

                public Herramienta herramienta(HerramientaId herramientaId) {
                  return Fixture.this.herramienta(herramientaId);
                }

                public boolean clienteActivo(ClienteId cliente) {
                  return participantes.clienteActivo(cliente);
                }

                public boolean proveedorActivo(ProveedorId proveedor) {
                  return participantes.proveedorActivo(proveedor);
                }

                public boolean ocupada(UnidadEquipoId unidadId, PeriodoReserva periodo) {
                  return reservas.values().stream()
                      .filter(otra -> !otra.id().equals(id))
                      .filter(otra -> otra.estado() == EstadoReserva.CONFIRMADA)
                      .filter(otra -> otra.periodo().solapa(periodo))
                      .anyMatch(
                          otra ->
                              otra.detalles().stream()
                                  .anyMatch(d -> d.unidadId().equals(unidadId)));
                }
              };
          var resultado = operacion.apply(contexto);
          if (!id.equals(resultado.id())) throw new ReglaNegocioException("Identidad modificada");
          reservas.put(id, resultado);
          return resultado;
        }
      };

  private Herramienta herramienta(HerramientaId id) {
    return Optional.ofNullable(herramientas.get(id))
        .orElseThrow(() -> new RecursoNoEncontradoException("Herramienta"));
  }

  private Reserva reserva(ReservaId id) {
    return reservaRepository
        .buscar(id)
        .orElseThrow(() -> new RecursoNoEncontradoException("Reserva"));
  }
}
