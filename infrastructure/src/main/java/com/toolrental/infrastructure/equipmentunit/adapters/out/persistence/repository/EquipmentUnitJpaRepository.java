package com.toolrental.infrastructure.equipmentunit.adapters.out.persistence.repository;

import com.toolrental.infrastructure.equipmentunit.adapters.out.persistence.entity.EquipmentUnitJpaEntity;
import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EquipmentUnitJpaRepository extends JpaRepository<EquipmentUnitJpaEntity, UUID> {
  @Query(
      value =
          """
          SELECT unit.*
            FROM equipment_units unit
           WHERE unit.tool_id = :toolId
             AND unit.operational_status = 'OPERATIONAL'
             AND NOT EXISTS (
                 SELECT 1
                   FROM reservation_items item
                   JOIN reservations reservation
                     ON reservation.reservation_id = item.reservation_id
                  WHERE item.equipment_unit_id = unit.equipment_unit_id
                    AND (
                        reservation.reservation_status IN ('CONFIRMED', 'CANCELLATION_REVIEW')
                        OR (
                            reservation.reservation_status = 'PENDING_PAYMENT'
                            AND reservation.expires_at > CURRENT_TIMESTAMP
                        )
                    )
                    AND reservation.start_date <= :returnDate
                    AND reservation.effective_return_date >= :startDate
             )
           ORDER BY unit.inventory_code
          """,
      nativeQuery = true)
  List<EquipmentUnitJpaEntity> findAvailableByToolAndPeriod(
      @Param("toolId") UUID toolId,
      @Param("startDate") LocalDate startDate,
      @Param("returnDate") LocalDate returnDate);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query(
      "select equipmentUnit from EquipmentUnitJpaEntity equipmentUnit "
          + "where equipmentUnit.id in :ids order by equipmentUnit.id")
  List<EquipmentUnitJpaEntity> findAllByIdForUpdate(@Param("ids") Collection<UUID> ids);

  @Query(
      "select equipmentUnit.id as equipmentUnitId, tool.supplierId as supplierId "
          + "from EquipmentUnitJpaEntity equipmentUnit, ToolJpaEntity tool "
          + "where equipmentUnit.toolId = tool.id and equipmentUnit.id in :ids")
  List<EquipmentUnitSupplierView> findSupplierIdsByEquipmentUnitIds(
      @Param("ids") Collection<UUID> ids);

  @Query(
      "select count(equipmentUnit) from EquipmentUnitJpaEntity equipmentUnit, "
          + "ToolJpaEntity tool where equipmentUnit.toolId = tool.id "
          + "and tool.supplierId = :supplierId "
          + "and lower(equipmentUnit.inventoryCode) = lower(:inventoryCode)")
  long countBySupplierAndInventoryCode(
      @Param("supplierId") UUID supplierId, @Param("inventoryCode") String inventoryCode);

  @Lock(LockModeType.PESSIMISTIC_WRITE)
  @Query("select unit from EquipmentUnitJpaEntity unit where unit.id=:id")
  java.util.Optional<EquipmentUnitJpaEntity> findByIdForUpdate(@Param("id") UUID id);
}
