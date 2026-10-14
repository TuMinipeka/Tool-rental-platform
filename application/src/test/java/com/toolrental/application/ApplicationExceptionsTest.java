package com.toolrental.application;

import static org.junit.jupiter.api.Assertions.*;

import com.toolrental.application.catalog.exception.CategoriaNotFoundApplicationException;
import com.toolrental.application.catalog.exception.HerramientaNotFoundApplicationException;
import com.toolrental.application.catalog.exception.ProveedorNotFoundApplicationException;
import com.toolrental.application.catalog.exception.UnidadEquipoNotFoundApplicationException;
import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.application.reservation.exception.ClienteNotFoundApplicationException;
import com.toolrental.application.reservation.exception.ReservaNotFoundApplicationException;
import com.toolrental.domain.catalog.model.valueobject.CategoriaId;
import com.toolrental.domain.catalog.model.valueobject.HerramientaId;
import com.toolrental.domain.catalog.model.valueobject.UnidadEquipoId;
import com.toolrental.domain.common.model.valueobject.ClienteId;
import com.toolrental.domain.common.model.valueobject.ProveedorId;
import com.toolrental.domain.reservation.model.valueobject.ReservaId;
import java.lang.reflect.Modifier;
import java.util.List;
import org.junit.jupiter.api.Test;

class ApplicationExceptionsTest {
  @Test
  void baseAbstractaConConstructoresProtegidos() {
    assertTrue(Modifier.isAbstract(ApplicationException.class.getModifiers()));
    for (var constructor : ApplicationException.class.getDeclaredConstructors()) {
      assertTrue(Modifier.isProtected(constructor.getModifiers()));
    }
  }

  @Test
  void excepcionesConcretasConservanCausaEIdentidad() {
    var cause = new IllegalStateException("fallo original");
    var categoria = CategoriaId.generar();
    var e = new CategoriaNotFoundApplicationException(categoria, cause);
    assertSame(cause, e.getCause());
    assertTrue(e.getMessage().contains(categoria.value().toString()));
    List<ApplicationException> excepciones =
        List.of(
            new HerramientaNotFoundApplicationException(HerramientaId.generar(), cause),
            new UnidadEquipoNotFoundApplicationException(UnidadEquipoId.generar(), cause),
            new ProveedorNotFoundApplicationException(ProveedorId.generar(), cause),
            new ClienteNotFoundApplicationException(ClienteId.generar(), cause),
            new ReservaNotFoundApplicationException(ReservaId.generar(), cause));
    excepciones.forEach(error -> assertSame(cause, error.getCause()));
  }
}
