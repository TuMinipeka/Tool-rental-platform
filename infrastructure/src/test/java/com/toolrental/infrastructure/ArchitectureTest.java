package com.toolrental.infrastructure;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.common.exception.DomainException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.infrastructure.common.exception.InfrastructureException;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
  @Test
  void hierarchiesAndPackagesFollowReferenceContract() {
    var importedClasses =
        new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.toolrental");
    classes()
        .that()
        .resideInAPackage("com.toolrental.application..exception")
        .should()
        .beAssignableTo(ApplicationException.class)
        .check(importedClasses);
    classes()
        .that()
        .resideInAPackage("com.toolrental.domain..exception")
        .should()
        .beAssignableTo(DomainException.class)
        .check(importedClasses);
    classes()
        .that()
        .resideInAPackage("com.toolrental.infrastructure..exception")
        .should()
        .beAssignableTo(InfrastructureException.class)
        .check(importedClasses);
    classes()
        .that()
        .resideInAPackage("com.toolrental.domain..model.aggregate")
        .should()
        .beAssignableTo(AggregateRoot.class)
        .check(importedClasses);
    classes()
        .that()
        .resideInAPackage("com.toolrental.domain..event")
        .should()
        .beAssignableTo(DomainEvent.class)
        .check(importedClasses);
  }

  @Test
  void dependenciesPointTowardDomain() {
    var importedClasses =
        new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.toolrental");
    noClasses()
        .that()
        .resideInAPackage("com.toolrental.domain..")
        .should()
        .dependOnClassesThat()
        .resideOutsideOfPackages("com.toolrental.domain..", "java..")
        .check(importedClasses);
    noClasses()
        .that()
        .resideInAPackage("com.toolrental.application..")
        .should()
        .dependOnClassesThat()
        .resideOutsideOfPackages(
            "com.toolrental.application..", "com.toolrental.domain..", "java..")
        .check(importedClasses);
  }
}
