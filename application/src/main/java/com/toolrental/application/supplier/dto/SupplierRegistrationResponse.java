package com.toolrental.application.supplier.dto;

import java.util.UUID;

public record SupplierRegistrationResponse(
    UUID userId, String accountStatus, String businessName) {}
