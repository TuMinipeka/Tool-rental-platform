package com.toolrental.infrastructure.payment.adapters.in.rest.controllers;

import com.toolrental.application.payment.dto.AvailabilityReportResponse;
import com.toolrental.application.payment.dto.FrequentCustomerReportResponse;
import com.toolrental.application.payment.dto.IncomeReportResponse;
import com.toolrental.application.payment.dto.ProfitabilityReportResponse;
import com.toolrental.application.payment.dto.ToolRentalReportResponse;
import com.toolrental.application.payment.usecase.OperationalReportUseCase;
import com.toolrental.infrastructure.common.web.Actor;
import java.security.Principal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api/v1/reports", "/api/reports"})
public class OperationalReportController {
  private final OperationalReportUseCase reports;

  public OperationalReportController(OperationalReportUseCase reports) {
    this.reports = reports;
  }

  @GetMapping("/income")
  public List<IncomeReportResponse> income(
      Principal principal,
      @RequestParam(required = false) UUID supplierId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return reports.income(Actor.id(principal), supplierId, from, to);
  }

  @GetMapping("/most-rented-tools")
  public List<ToolRentalReportResponse> mostRentedTools(
      Principal principal,
      @RequestParam(required = false) UUID supplierId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return reports.mostRentedTools(Actor.id(principal), supplierId, from, to);
  }

  @GetMapping("/frequent-customers")
  public List<FrequentCustomerReportResponse> frequentCustomers(
      Principal principal,
      @RequestParam(required = false) UUID supplierId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return reports.frequentCustomers(Actor.id(principal), supplierId, from, to);
  }

  @GetMapping("/availability")
  public List<AvailabilityReportResponse> availability(
      Principal principal,
      @RequestParam(required = false) UUID supplierId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return reports.availability(Actor.id(principal), supplierId, from, to);
  }

  @GetMapping("/profitability")
  public List<ProfitabilityReportResponse> profitability(
      Principal principal,
      @RequestParam(required = false) UUID supplierId,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
      @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
    return reports.profitability(Actor.id(principal), supplierId, from, to);
  }
}
