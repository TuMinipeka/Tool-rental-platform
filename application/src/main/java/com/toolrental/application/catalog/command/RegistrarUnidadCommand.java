package com.toolrental.application.catalog.command;

import com.toolrental.domain.catalog.model.valueobject.HerramientaId;

public record RegistrarUnidadCommand(HerramientaId herramientaId, String codigoInventario) {}
