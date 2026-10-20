package com.toolrental.infrastructure.payment.adapters.out.persistence;

import com.toolrental.domain.payment.port.OperationalReportingPort;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class OperationalReportingPersistenceAdapter implements OperationalReportingPort {
  private static final String OWNERSHIP =
      """
      SELECT DISTINCT item.reservation_id, tool.supplier_id
      FROM reservation_items item
      JOIN equipment_units unit ON unit.equipment_unit_id = item.equipment_unit_id
      JOIN tools tool ON tool.tool_id = unit.tool_id
      """;

  private final JdbcTemplate jdbc;

  public OperationalReportingPersistenceAdapter(JdbcTemplate jdbc) {
    this.jdbc = jdbc;
  }

  @Override
  public List<IncomeView> income(UUID supplierId, LocalDate from, LocalDate to) {
    var sql =
        """
        WITH ownership AS (%s), collections AS (
          SELECT payment.payment_id,
                 reservation.currency,
                 payment.amount,
                 COALESCE((SELECT SUM(refund.amount) FROM refunds refund
                           WHERE refund.payment_id = payment.payment_id
                             AND refund.refund_status = 'CONFIRMED'), 0) AS refunded
          FROM payments payment
          JOIN reservations reservation ON reservation.reservation_id = payment.reservation_id
          JOIN ownership ON ownership.reservation_id = reservation.reservation_id
          WHERE payment.payment_status = 'CONFIRMED'
            AND payment.confirmed_at::date BETWEEN ? AND ?
            AND (?::uuid IS NULL OR ownership.supplier_id = ?::uuid)
        )
        SELECT currency,
               SUM(amount) AS gross_income,
               SUM(refunded) AS refunds,
               SUM(amount - refunded) AS net_income,
               COUNT(*) AS confirmed_payments
        FROM collections
        GROUP BY currency
        ORDER BY currency
        """
            .formatted(OWNERSHIP);
    return jdbc.query(
        sql,
        (row, index) ->
            new IncomeView(
                row.getString("currency"),
                row.getBigDecimal("gross_income"),
                row.getBigDecimal("refunds"),
                row.getBigDecimal("net_income"),
                row.getLong("confirmed_payments")),
        from,
        to,
        supplierId,
        supplierId);
  }

  @Override
  public List<ToolRentalView> mostRentedTools(UUID supplierId, LocalDate from, LocalDate to) {
    var sql =
        """
        SELECT tool.tool_id,
               tool.name AS tool_name,
               reservation.currency,
               COUNT(DISTINCT reservation.reservation_id) AS reservation_count,
               SUM(reservation.effective_return_date - reservation.start_date + 1) AS rented_days,
               SUM(item.agreed_daily_rate
                   * (reservation.effective_return_date - reservation.start_date + 1)) AS booked_revenue
        FROM reservations reservation
        JOIN reservation_items item ON item.reservation_id = reservation.reservation_id
        JOIN equipment_units unit ON unit.equipment_unit_id = item.equipment_unit_id
        JOIN tools tool ON tool.tool_id = unit.tool_id
        WHERE reservation.confirmed_at::date BETWEEN ? AND ?
          AND reservation.reservation_status IN ('CONFIRMED', 'CANCELLATION_REVIEW', 'NO_SHOW')
          AND (?::uuid IS NULL OR tool.supplier_id = ?::uuid)
        GROUP BY tool.tool_id, tool.name, reservation.currency
        ORDER BY reservation_count DESC, rented_days DESC, tool.name
        """;
    return jdbc.query(
        sql,
        (row, index) ->
            new ToolRentalView(
                row.getObject("tool_id", UUID.class),
                row.getString("tool_name"),
                row.getString("currency"),
                row.getLong("reservation_count"),
                row.getLong("rented_days"),
                row.getBigDecimal("booked_revenue")),
        from,
        to,
        supplierId,
        supplierId);
  }

  @Override
  public List<FrequentCustomerView> frequentCustomers(
      UUID supplierId, LocalDate from, LocalDate to) {
    var sql =
        """
        WITH ownership AS (%s), customer_collections AS (
          SELECT reservation.customer_id,
                 reservation.reservation_id,
                 reservation.currency,
                 payment.amount
                   - COALESCE((SELECT SUM(refund.amount) FROM refunds refund
                               WHERE refund.payment_id = payment.payment_id
                                 AND refund.refund_status = 'CONFIRMED'), 0) AS net_amount
          FROM payments payment
          JOIN reservations reservation ON reservation.reservation_id = payment.reservation_id
          JOIN ownership ON ownership.reservation_id = reservation.reservation_id
          WHERE payment.payment_status = 'CONFIRMED'
            AND payment.confirmed_at::date BETWEEN ? AND ?
            AND (?::uuid IS NULL OR ownership.supplier_id = ?::uuid)
        )
        SELECT customer.user_id AS customer_id,
               customer.billing_name AS customer_name,
               collection.currency,
               COUNT(DISTINCT collection.reservation_id) AS reservation_count,
               SUM(collection.net_amount) AS net_income
        FROM customer_collections collection
        JOIN customers customer ON customer.user_id = collection.customer_id
        GROUP BY customer.user_id, customer.billing_name, collection.currency
        ORDER BY reservation_count DESC, net_income DESC, customer.billing_name
        """
            .formatted(OWNERSHIP);
    return jdbc.query(
        sql,
        (row, index) ->
            new FrequentCustomerView(
                row.getObject("customer_id", UUID.class),
                row.getString("customer_name"),
                row.getString("currency"),
                row.getLong("reservation_count"),
                row.getBigDecimal("net_income")),
        from,
        to,
        supplierId,
        supplierId);
  }

  @Override
  public List<AvailabilityView> availability(
      UUID supplierId, LocalDate from, LocalDate to, Instant currentTime) {
    var sql =
        """
        SELECT tool.tool_id,
               tool.name AS tool_name,
               COUNT(unit.equipment_unit_id) AS total_units,
               COUNT(unit.equipment_unit_id) FILTER (
                 WHERE unit.operational_status = 'OPERATIONAL'
                   AND NOT EXISTS (
                     SELECT 1
                     FROM reservation_items item
                     JOIN reservations reservation ON reservation.reservation_id = item.reservation_id
                     WHERE item.equipment_unit_id = unit.equipment_unit_id
                       AND reservation.start_date <= ?
                       AND reservation.effective_return_date >= ?
                       AND (reservation.reservation_status IN ('CONFIRMED', 'CANCELLATION_REVIEW')
                            OR (reservation.reservation_status = 'PENDING_PAYMENT'
                                AND reservation.expires_at > ?)))) AS available_units,
               COUNT(unit.equipment_unit_id) FILTER (
                 WHERE unit.operational_status = 'OPERATIONAL'
                   AND EXISTS (
                     SELECT 1
                     FROM reservation_items item
                     JOIN reservations reservation ON reservation.reservation_id = item.reservation_id
                     WHERE item.equipment_unit_id = unit.equipment_unit_id
                       AND reservation.start_date <= ?
                       AND reservation.effective_return_date >= ?
                       AND (reservation.reservation_status IN ('CONFIRMED', 'CANCELLATION_REVIEW')
                            OR (reservation.reservation_status = 'PENDING_PAYMENT'
                                AND reservation.expires_at > ?)))) AS reserved_units,
               COUNT(unit.equipment_unit_id) FILTER (
                 WHERE unit.operational_status = 'MAINTENANCE') AS maintenance_units,
               COUNT(unit.equipment_unit_id) FILTER (
                 WHERE unit.operational_status = 'RETIRED') AS retired_units
        FROM tools tool
        LEFT JOIN equipment_units unit ON unit.tool_id = tool.tool_id
        WHERE (?::uuid IS NULL OR tool.supplier_id = ?::uuid)
        GROUP BY tool.tool_id, tool.name
        ORDER BY tool.name
        """;
    return jdbc.query(
        sql,
        (row, index) ->
            new AvailabilityView(
                row.getObject("tool_id", UUID.class),
                row.getString("tool_name"),
                row.getLong("total_units"),
                row.getLong("available_units"),
                row.getLong("reserved_units"),
                row.getLong("maintenance_units"),
                row.getLong("retired_units")),
        to,
        from,
        Timestamp.from(currentTime),
        to,
        from,
        Timestamp.from(currentTime),
        supplierId,
        supplierId);
  }

  @Override
  public List<ProfitabilityView> profitability(UUID supplierId, LocalDate from, LocalDate to) {
    var sql =
        """
        SELECT tool.tool_id,
               tool.name AS tool_name,
               reservation.currency,
               COUNT(DISTINCT reservation.reservation_id) AS reservation_count,
               SUM(reservation.effective_return_date - reservation.start_date + 1) AS rented_days,
               SUM(item.agreed_daily_rate
                   * (reservation.effective_return_date - reservation.start_date + 1)) AS booked_revenue,
               SUM(item.agreed_daily_rate
                   * (reservation.effective_return_date - reservation.start_date + 1))
                   / COUNT(DISTINCT reservation.reservation_id) AS average_revenue
        FROM tools tool
        JOIN equipment_units unit ON unit.tool_id = tool.tool_id
        JOIN reservation_items item ON item.equipment_unit_id = unit.equipment_unit_id
        JOIN reservations reservation ON reservation.reservation_id = item.reservation_id
        WHERE reservation.confirmed_at::date BETWEEN ? AND ?
          AND reservation.reservation_status IN ('CONFIRMED', 'CANCELLATION_REVIEW', 'NO_SHOW')
          AND (?::uuid IS NULL OR tool.supplier_id = ?::uuid)
        GROUP BY tool.tool_id, tool.name, reservation.currency
        ORDER BY booked_revenue DESC, tool.name
        """;
    return jdbc.query(
        sql,
        (row, index) ->
            new ProfitabilityView(
                row.getObject("tool_id", UUID.class),
                row.getString("tool_name"),
                row.getString("currency"),
                row.getLong("reservation_count"),
                row.getLong("rented_days"),
                row.getBigDecimal("booked_revenue"),
                row.getBigDecimal("average_revenue")),
        from,
        to,
        supplierId,
        supplierId);
  }
}
