package com.toolrental.application.unitreturn.command;

import com.toolrental.domain.unitreturn.model.valueobject.UnitReturnId;

public record RegisterUnitReturnCommand(UnitReturnId id, String inboundCondition, String notes) {}
