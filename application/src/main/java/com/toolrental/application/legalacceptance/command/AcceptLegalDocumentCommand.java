package com.toolrental.application.legalacceptance.command;

public record AcceptLegalDocumentCommand(
    String documentType, String documentVersion, String purpose) {}
