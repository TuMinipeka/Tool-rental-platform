package com.toolrental.application.tool.usecase;

import com.toolrental.application.common.exception.ReferenceNotFoundApplicationException;
import com.toolrental.application.tool.dto.CatalogToolResponse;
import com.toolrental.domain.category.port.repository.CategoryRepository;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import java.util.Comparator;
import java.util.Locale;
import java.util.UUID;
import java.util.function.Predicate;

public final class BrowseCatalogUseCase {
  private final ToolRepository tools;
  private final CategoryRepository categories;
  private final EquipmentUnitRepository units;
  private final UnitOfWork work;

  public BrowseCatalogUseCase(
      ToolRepository tools,
      CategoryRepository categories,
      EquipmentUnitRepository units,
      UnitOfWork work) {
    this.tools = tools;
    this.categories = categories;
    this.units = units;
    this.work = work;
  }

  public java.util.List<CatalogToolResponse> list(
      UUID categoryId, String query, String serviceCity) {
    return work.execute(
        () -> {
          var categoryNames =
              categories.findAll().stream()
                  .collect(
                      java.util.stream.Collectors.toMap(
                          category -> category.id().value(), category -> category.name()));
          var operationalCounts =
              units.findAll().stream()
                  .filter(unit -> unit.isOperational())
                  .collect(
                      java.util.stream.Collectors.groupingBy(
                          unit -> unit.toolId().value(), java.util.stream.Collectors.counting()));
          var normalizedQuery = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
          var normalizedCity =
              serviceCity == null ? "" : serviceCity.trim().toLowerCase(Locale.ROOT);
          Predicate<com.toolrental.domain.tool.model.aggregate.Tool> categoryFilter =
              tool -> categoryId == null || categoryId.equals(tool.categoryId().value());
          Predicate<com.toolrental.domain.tool.model.aggregate.Tool> textFilter =
              tool ->
                  normalizedQuery.isBlank()
                      || tool.name().toLowerCase(Locale.ROOT).contains(normalizedQuery)
                      || tool.description().toLowerCase(Locale.ROOT).contains(normalizedQuery);
          Predicate<com.toolrental.domain.tool.model.aggregate.Tool> cityFilter =
              tool ->
                  normalizedCity.isBlank()
                      || tool.serviceCity().toLowerCase(Locale.ROOT).equals(normalizedCity);
          return tools.findAll().stream()
              .filter(
                  tool ->
                      tool.published()
                          && categoryFilter.test(tool)
                          && textFilter.test(tool)
                          && cityFilter.test(tool))
              .map(
                  tool ->
                      CatalogToolResponse.from(
                          tool,
                          categoryNames.getOrDefault(tool.categoryId().value(), ""),
                          operationalCounts.getOrDefault(tool.id().value(), 0L)))
              .sorted(Comparator.comparing(CatalogToolResponse::name))
              .toList();
        });
  }

  public java.util.List<CatalogToolResponse> list(UUID categoryId, String query) {
    return list(categoryId, query, null);
  }

  public CatalogToolResponse find(ToolId id) {
    return work.execute(
        () -> {
          var tool =
              tools
                  .findById(id)
                  .filter(value -> value.published())
                  .orElseThrow(() -> new ReferenceNotFoundApplicationException("Tool"));
          var categoryName =
              categories.findById(tool.categoryId()).map(category -> category.name()).orElse("");
          var operationalCount =
              units.findAll().stream()
                  .filter(unit -> unit.toolId().equals(id) && unit.isOperational())
                  .count();
          return CatalogToolResponse.from(tool, categoryName, operationalCount);
        });
  }
}
