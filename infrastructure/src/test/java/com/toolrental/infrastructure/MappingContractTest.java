package com.toolrental.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

class MappingContractTest {
  @Test
  void everySchemaColumnHasExactlyOneOwningEntity() throws Exception {
    var sql = new StringBuilder();
    try (var migrations = Files.list(Path.of("src/main/resources/db/migration"))) {
      for (var migration : migrations.filter(Files::isRegularFile).sorted().toList()) {
        sql.append(Files.readString(migration)).append('\n');
      }
    }
    var tables = new LinkedHashMap<String, Set<String>>();
    var matcher = Pattern.compile("CREATE TABLE (\\w+) \\(([\\s\\S]*?)\\n\\);").matcher(sql);
    while (matcher.find()) {
      var columns = new HashSet<String>();
      var rows =
          Pattern.compile(
                  "(?m)^    (\\w+) (?:uuid|smallint|integer|boolean|varchar|char|numeric|timestamptz|date)")
              .matcher(matcher.group(2));
      while (rows.find()) columns.add(rows.group(1));
      tables.put(matcher.group(1), columns);
    }
    var alterTableMatcher =
        Pattern.compile("ALTER TABLE (\\w+)([\\s\\S]*?);", Pattern.CASE_INSENSITIVE).matcher(sql);
    while (alterTableMatcher.find()) {
      var addedColumnMatcher =
          Pattern.compile("ADD COLUMN(?: IF NOT EXISTS)?\\s+(\\w+)", Pattern.CASE_INSENSITIVE)
              .matcher(alterTableMatcher.group(2));
      while (addedColumnMatcher.find()) {
        tables.get(alterTableMatcher.group(1)).add(addedColumnMatcher.group(1));
      }
    }
    var classes =
        new ClassFileImporter()
            .withImportOption(new ImportOption.DoNotIncludeTests())
            .importPackages("com.toolrental.infrastructure");
    var owners = new HashSet<String>();
    for (var candidate : classes) {
      Class<?> type = candidate.reflect();
      if (!type.isAnnotationPresent(Entity.class)) continue;
      String table = type.getAnnotation(Table.class).name();
      assertThat(owners.add(table)).isTrue();
      assertThat(type.getPackageName()).doesNotContain("catalog", "identity");
      var columns = new HashSet<String>();
      for (var field : type.getDeclaredFields()) {
        if (field.isAnnotationPresent(Transient.class)) continue;
        var column = field.getAnnotation(Column.class);
        if (column != null) columns.add(column.name());
        if (field.isAnnotationPresent(EmbeddedId.class))
          for (var part : field.getType().getDeclaredFields())
            if (part.isAnnotationPresent(Column.class))
              columns.add(part.getAnnotation(Column.class).name());
        assertThat(field.isAnnotationPresent(ElementCollection.class)).isFalse();
      }
      assertThat(columns).as(table).isEqualTo(tables.get(table));
    }
    assertThat(owners).hasSize(25).containsExactlyInAnyOrderElementsOf(tables.keySet());
  }
}
