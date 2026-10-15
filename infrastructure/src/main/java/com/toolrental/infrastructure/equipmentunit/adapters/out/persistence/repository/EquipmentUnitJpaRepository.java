package com.toolrental.infrastructure.equipmentunit.adapters.out.persistence.repository;

import com.toolrental.infrastructure.equipmentunit.adapters.out.persistence.entity.EquipmentUnitJpaEntity;
import jakarta.persistence.LockModeType;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EquipmentUnitJpaRepository extends JpaRepository<EquipmentUnitJpaEntity, UUID> {
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
}
