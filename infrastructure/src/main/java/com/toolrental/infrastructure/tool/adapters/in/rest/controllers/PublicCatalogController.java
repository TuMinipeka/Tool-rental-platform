package com.toolrental.infrastructure.tool.adapters.in.rest.controllers;

import com.toolrental.application.tool.dto.CatalogPageResponse;
import com.toolrental.application.tool.dto.CatalogToolResponse;
import com.toolrental.application.tool.dto.ToolAvailabilityResponse;
import com.toolrental.application.tool.usecase.BrowseCatalogUseCase;
import com.toolrental.application.tool.usecase.CheckToolAvailabilityUseCase;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/public/catalog", "/api/public/catalog"})
@SecurityRequirements
public final class PublicCatalogController {
  private final BrowseCatalogUseCase catalog;
  private final CheckToolAvailabilityUseCase availability;

  public PublicCatalogController(
      BrowseCatalogUseCase catalog, CheckToolAvailabilityUseCase availability) {
    this.catalog = catalog;
    this.availability = availability;
  }

  @GetMapping
  public List<CatalogToolResponse> list(
      @RequestParam(required = false) UUID categoryId,
      @RequestParam(required = false) String query,
      @RequestParam(required = false) String serviceCity) {
    return catalog.list(categoryId, query, serviceCity);
  }

  @GetMapping("/page")
  public CatalogPageResponse page(
      @RequestParam(required = false) UUID categoryId,
      @RequestParam(required = false) String query,
      @RequestParam(required = false) String serviceCity,
      @RequestParam(required = false) BigDecimal minDailyRate,
      @RequestParam(required = false) BigDecimal maxDailyRate,
      @RequestParam(defaultValue = "0") int page,
      @RequestParam(defaultValue = "12") int size,
      @RequestParam(defaultValue = "name,asc") String sort) {
    return catalog.page(
        categoryId, query, serviceCity, minDailyRate, maxDailyRate, page, size, sort);
  }

  @GetMapping("/{id}")
  public CatalogToolResponse find(@PathVariable UUID id) {
    return catalog.find(new ToolId(id));
  }

  @GetMapping("/{id}/availability")
  public ToolAvailabilityResponse availability(
      @PathVariable UUID id,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate returnDate) {
    return availability.execute(new ToolId(id), new ReservationPeriod(startDate, returnDate));
  }
}
