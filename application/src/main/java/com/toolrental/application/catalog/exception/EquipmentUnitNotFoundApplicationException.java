package com.toolrental.application.catalog.exception;

import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.catalog.model.valueobject.EquipmentUnitId;
import java.util.Objects;

public final class EquipmentUnitNotFoundApplicationException extends ApplicationException {
  public EquipmentUnitNotFoundApplicationException(EquipmentUnitId id) {
    super("Equipment unit not found: " + Objects.requireNonNull(id).value());
  }

  public EquipmentUnitNotFoundApplicationException(EquipmentUnitId id, Throwable cause) {
    super("Equipment unit not found: " + Objects.requireNonNull(id).value(), cause);
  }
}
