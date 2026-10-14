package com.toolrental.domain.common.port;

import com.toolrental.domain.common.model.valueobject.ClienteId;
import com.toolrental.domain.common.model.valueobject.ProveedorId;

/** Perfiles activos y con rol coherente; implementación de identidad en una fase posterior. */
public interface ParticipantesPort {
  boolean clienteActivo(ClienteId id);

  boolean proveedorActivo(ProveedorId id);
}
