package com.toolrental.application.payment.dto;

import java.time.LocalDate;
import java.util.UUID;

public record AvailabilityReportResponse(
    LocalDate from,
    LocalDate to,
    UUID toolId,
    String toolName,
    long totalUnits,
    long availableUnits,
    long reservedUnits,
    long maintenanceUnits,
    long retiredUnits) {}
