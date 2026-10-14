package com.toolrental.domain.reservation.event;

import com.toolrental.domain.reservation.model.valueobject.ReservaId;
import java.time.Instant;

public record ReservaConfirmada(ReservaId reservaId, Instant ocurridaEn) {}
