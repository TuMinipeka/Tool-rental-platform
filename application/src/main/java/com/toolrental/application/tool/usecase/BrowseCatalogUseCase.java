package com.toolrental.application.tool.usecase;

import com.toolrental.application.common.exception.ReferenceNotFoundApplicationException;
import com.toolrental.application.tool.dto.CatalogPageResponse;
import com.toolrental.application.tool.dto.CatalogToolResponse;
import com.toolrental.domain.category.port.repository.CategoryRepository;
import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.equipmentunit.port.repository.EquipmentUnitRepository;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.tool.port.repository.ToolRepository;
import java.math.BigDecimal;
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
    return filtered(categoryId, query, serviceCity);
  }

  public CatalogPageResponse page(
      UUID categoryId,
      String query,
      String serviceCity,
      BigDecimal minDailyRate,
      BigDecimal maxDailyRate,
      int page,
      int size,
      String sort) {
    if (page < 0 || size < 1 || size > 100)
      throw new BusinessRuleException(
          "Page must be non-negative and size must be between 1 and 100");
    if (minDailyRate != null && minDailyRate.signum() < 0
        || maxDailyRate != null && maxDailyRate.signum() < 0
        || minDailyRate != null && maxDailyRate != null && minDailyRate.compareTo(maxDailyRate) > 0)
      throw new BusinessRuleException("Daily rate range is invalid");

    var comparator = catalogComparator(sort);
    var matches =
        filtered(categoryId, query, serviceCity).stream()
            .filter(item -> minDailyRate == null || item.dailyRate().compareTo(minDailyRate) >= 0)
            .filter(item -> maxDailyRate == null || item.dailyRate().compareTo(maxDailyRate) <= 0)
            .sorted(comparator)
            .toList();
    var offset = (long) page * size;
    var content =
        offset >= matches.size()
            ? java.util.List.<CatalogToolResponse>of()
            : matches.subList((int) offset, Math.min((int) offset + size, matches.size()));
    var totalPages = matches.isEmpty() ? 0 : (matches.size() + size - 1) / size;
    return new CatalogPageResponse(content, page, size, matches.size(), totalPages);
  }

  private java.util.List<CatalogToolResponse> filtered(
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

  private static Comparator<CatalogToolResponse> catalogComparator(String sort) {
    var normalized =
        sort == null || sort.isBlank() ? "name,asc" : sort.trim().toLowerCase(Locale.ROOT);
    return switch (normalized) {
      case "name,asc" -> Comparator.comparing(CatalogToolResponse::name);
      case "name,desc" -> Comparator.comparing(CatalogToolResponse::name).reversed();
      case "dailyrate,asc" ->
          Comparator.comparing(CatalogToolResponse::dailyRate)
              .thenComparing(CatalogToolResponse::name);
      case "dailyrate,desc" ->
          Comparator.comparing(CatalogToolResponse::dailyRate)
              .reversed()
              .thenComparing(CatalogToolResponse::name);
      default -> throw new BusinessRuleException("Unsupported catalog sort");
    };
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
