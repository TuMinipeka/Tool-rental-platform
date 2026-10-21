package com.toolrental.infrastructure.reservation.adapters.in.rest.dtos;

import com.toolrental.domain.reservation.model.valueobject.FulfillmentMethod;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ReservationRequest(
    @NotNull LocalDate startDate,
    @NotNull LocalDate returnDate,
    @NotEmpty List<@NotNull UUID> equipmentUnitIds,
    @NotNull Instant scheduledFulfillmentAt,
    @NotNull FulfillmentMethod fulfillmentMethod,
    String deliveryAddressLine,
    String deliveryCity,
    String deliveryRegion,
    String deliveryCountryCode) {}
