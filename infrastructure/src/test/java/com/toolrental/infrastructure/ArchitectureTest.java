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
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
  @Test
  void hierarchiesAndPackagesFollowReferenceContract() {
    var importedClasses = importedProductionClasses();
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
    var importedClasses = importedProductionClasses();
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

  private com.tngtech.archunit.core.domain.JavaClasses importedProductionClasses() {
    return new ClassFileImporter()
        .withImportOption(new ImportOption.DoNotIncludeTests())
        .importPaths(
            Path.of("../domain/target/classes"),
            Path.of("../application/target/classes"),
            Path.of("target/classes"));
  }
}
