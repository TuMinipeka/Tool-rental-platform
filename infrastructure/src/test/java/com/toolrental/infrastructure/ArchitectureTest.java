package com.toolrental.infrastructure;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

class ArchitectureTest {
  @Test
  void dependenciasApuntanHaciaElDominio() {
    var clases =
        new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.toolrental");
    noClasses()
        .that()
        .resideInAPackage("com.toolrental.domain..")
        .should()
        .dependOnClassesThat()
        .resideOutsideOfPackages("com.toolrental.domain..", "java..")
        .check(clases);
    noClasses()
        .that()
        .resideInAPackage("com.toolrental.application..")
        .should()
        .dependOnClassesThat()
        .resideOutsideOfPackages(
            "com.toolrental.application..", "com.toolrental.domain..", "java..")
        .check(clases);
  }
}
