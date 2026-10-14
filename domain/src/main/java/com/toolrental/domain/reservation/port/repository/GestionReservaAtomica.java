package com.toolrental.domain.reservation.port.repository;

import com.toolrental.domain.catalog.model.aggregate.Herramienta;
import com.toolrental.domain.catalog.model.aggregate.UnidadEquipo;
import com.toolrental.domain.catalog.model.valueobject.HerramientaId;
import com.toolrental.domain.catalog.model.valueobject.UnidadEquipoId;
import com.toolrental.domain.common.model.valueobject.ClienteId;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import com.toolrental.domain.reservation.model.aggregate.Reserva;
import com.toolrental.domain.reservation.model.valueobject.PeriodoReserva;
import com.toolrental.domain.reservation.model.valueobject.ReservaId;
import java.util.function.Function;

/**
 * Ejecuta la función y guarda su resultado en UNA transacción. El adaptador debe recargar y
 * bloquear reserva y unidades en orden estable antes de la función; todas las lecturas de Contexto
 * pertenecen a esa transacción. Si la función falla, no guarda cambios ni publica eventos. La
 * reserva entregada debe ser una copia aislada: modificar el agregado no modifica el almacenamiento
 * antes del commit. Confirmación, rechazo y cancelación usan el mismo bloqueo para evitar carreras.
 * Las reglas de negocio se ejecutan en aplicación/dominio, no en el adaptador.
 */
public interface GestionReservaAtomica {
  Reserva ejecutar(ReservaId id, Function<Contexto, Reserva> operacion);

  interface Contexto {
    Reserva reserva();

    UnidadEquipo unidad(UnidadEquipoId id);

    Herramienta herramienta(HerramientaId id);

    boolean clienteActivo(ClienteId id);

    boolean proveedorActivo(ProveedorId id);

    /** Consulta solo reservas CONFIRMADAS, con fechas inclusivas, excluyendo la reserva actual. */
    boolean ocupada(UnidadEquipoId id, PeriodoReserva periodo);
  }
}
