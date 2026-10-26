package com.toolrental.infrastructure;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.toolrental.application.common.exception.ApplicationException;
import com.toolrental.domain.common.event.DomainEvent;
import com.toolrental.domain.common.exception.DomainException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.infrastructure.common.exception.InfrastructureException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
  @Test
  void allTableContextsOwnTheirApplicationAndInfrastructurePackages() throws IOException {
    var domainRoot = Path.of("../domain/src/main/java/com/toolrental/domain");
    var applicationRoot = Path.of("../application/src/main/java/com/toolrental/application");
    var infrastructureRoot = Path.of("src/main/java/com/toolrental/infrastructure");
    Set<String> contexts;
    try (var children = Files.list(domainRoot)) {
      contexts =
          children
              .filter(Files::isDirectory)
              .map(path -> path.getFileName().toString())
              .filter(name -> !name.equals("common"))
              .collect(Collectors.toSet());
    }
    assertThat(contexts).hasSize(26);
    for (var context : contexts) {
      assertThat(Files.isDirectory(applicationRoot.resolve(context)))
          .as(context + " application")
          .isTrue();
      assertThat(Files.isDirectory(infrastructureRoot.resolve(context).resolve("config")))
          .as(context + " configuration")
          .isTrue();
      assertThat(
              Files.isDirectory(
                  infrastructureRoot.resolve(context).resolve("adapters/out/persistence")))
          .as(context + " persistence")
          .isTrue();
    }
  }

  @Test
  void javaPackageDeclarationsMatchTheirSourcePathsAndUseLf() throws IOException {
    for (var module : Set.of("domain", "application", "infrastructure")) {
      var root = Path.of("../" + module + "/src/main/java");
      if (module.equals("infrastructure")) root = Path.of("src/main/java");
      try (var files = Files.walk(root)) {
        for (var file : files.filter(path -> path.toString().endsWith(".java")).toList()) {
          var source = Files.readString(file);
          var packageName =
              file.getParent()
                  .toString()
                  .replace('\\', '/')
                  .substring(root.toString().replace('\\', '/').length() + 1)
                  .replace('/', '.');
          assertThat(source).as(file.toString()).startsWith("package " + packageName + ";");
          assertThat(source).as(file.toString()).doesNotContain("\r");
        }
      }
    }
  }

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
