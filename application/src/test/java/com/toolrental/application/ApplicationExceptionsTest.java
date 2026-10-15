package com.toolrental.application;

import static org.junit.jupiter.api.Assertions.*;

import com.toolrental.application.category.exception.CategoryNotFoundApplicationException;
import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.application.equipmentunit.exception.EquipmentUnitNotFoundApplicationException;
import com.toolrental.application.reservation.exception.CustomerNotFoundApplicationException;
import com.toolrental.application.reservation.exception.ReservationNotFoundApplicationException;
import com.toolrental.application.supplier.exception.SupplierNotFoundApplicationException;
import com.toolrental.application.tool.exception.ToolNotFoundApplicationException;
import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationExceptionsTest {
  @Test
  void abstractBaseHasProtectedConstructors() {
    assertTrue(Modifier.isAbstract(ApplicationException.class.getModifiers()));
    for (var constructor : ApplicationException.class.getDeclaredConstructors()) {
      assertTrue(Modifier.isProtected(constructor.getModifiers()));
    }
  }

  @Test
  void concreteExceptionsPreserveCauseAndIdentity() {
    var cause = new IllegalStateException("original failure");
    var category = CategoryId.generate();
    var e = new CategoryNotFoundApplicationException(category, cause);
    assertSame(cause, e.getCause());
    assertTrue(e.getMessage().contains(category.value().toString()));
    List<ApplicationException> excepciones =
        List.of(
            new ToolNotFoundApplicationException(ToolId.generate(), cause),
            new EquipmentUnitNotFoundApplicationException(EquipmentUnitId.generate(), cause),
            new SupplierNotFoundApplicationException(SupplierId.generate(), cause),
            new CustomerNotFoundApplicationException(CustomerId.generate(), cause),
            new ReservationNotFoundApplicationException(ReservationId.generate(), cause));
    excepciones.forEach(error -> assertSame(cause, error.getCause()));
  }
}
