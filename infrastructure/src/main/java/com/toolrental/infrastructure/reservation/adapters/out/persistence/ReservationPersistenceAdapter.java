package com.toolrental.infrastructure.reservation.adapters.out.persistence;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.port.ParticipantsPort;
import com.toolrental.domain.customer.model.valueobject.CustomerId;
import com.toolrental.domain.equipmentunit.model.aggregate.EquipmentUnit;
import com.toolrental.domain.equipmentunit.model.valueobject.EquipmentUnitId;
import com.toolrental.domain.reservation.model.aggregate.Reservation;
import com.toolrental.domain.reservation.model.valueobject.ReservationId;
import com.toolrental.domain.reservation.model.valueobject.ReservationPeriod;
import com.toolrental.domain.reservation.port.repository.AtomicReservationManagement;
import com.toolrental.domain.reservation.port.repository.ReservationRepository;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import com.toolrental.domain.tool.model.aggregate.Tool;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.infrastructure.equipmentunit.adapters.out.persistence.entity.EquipmentUnitJpaEntity;
import com.toolrental.infrastructure.equipmentunit.adapters.out.persistence.mapper.EquipmentUnitPersistenceMapper;
import com.toolrental.infrastructure.equipmentunit.adapters.out.persistence.repository.EquipmentUnitJpaRepository;
import com.toolrental.infrastructure.reservation.adapters.out.persistence.entity.ReservationJpaEntity;
import com.toolrental.infrastructure.reservation.adapters.out.persistence.mapper.ReservationPersistenceMapper;
import com.toolrental.infrastructure.reservation.adapters.out.persistence.repository.ReservationJpaRepository;
import com.toolrental.infrastructure.tool.adapters.out.persistence.entity.ToolJpaEntity;
import com.toolrental.infrastructure.tool.adapters.out.persistence.mapper.ToolPersistenceMapper;
import com.toolrental.infrastructure.tool.adapters.out.persistence.repository.ToolJpaRepository;
import com.toolrental.infrastructure.user.adapters.out.persistence.repository.UserJpaRepository;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional(readOnly = true)
public class ReservationPersistenceAdapter
    implements ReservationRepository, AtomicReservationManagement {
  @jakarta.persistence.PersistenceContext private jakarta.persistence.EntityManager entityManager;
  private final ReservationJpaRepository reservations;
  private final com.toolrental.infrastructure.reservationitem.adapters.out.persistence.repository
          .ReservationItemJpaRepository
      items;
  private final EquipmentUnitJpaRepository equipmentUnits;
  private final ToolJpaRepository tools;
  private final UserJpaRepository accounts;
  private final ParticipantsPort participants;
  private final ReservationPersistenceMapper reservationMapper;
  private final EquipmentUnitPersistenceMapper equipmentUnitMapper;
  private final ToolPersistenceMapper toolMapper;

  public ReservationPersistenceAdapter(
      com.toolrental.infrastructure.reservationitem.adapters.out.persistence.repository
              .ReservationItemJpaRepository
          items,
      ReservationJpaRepository reservations,
      EquipmentUnitJpaRepository equipmentUnits,
      ToolJpaRepository tools,
      UserJpaRepository accounts,
      ParticipantsPort participants,
      ReservationPersistenceMapper reservationMapper,
      EquipmentUnitPersistenceMapper equipmentUnitMapper,
      ToolPersistenceMapper toolMapper) {
    this.reservations = reservations;
    this.items = items;
    this.equipmentUnits = equipmentUnits;
    this.tools = tools;
    this.accounts = accounts;
    this.participants = participants;
    this.reservationMapper = reservationMapper;
    this.equipmentUnitMapper = equipmentUnitMapper;
    this.toolMapper = toolMapper;
  }

  @Override
  @Transactional
  public void addRequest(Reservation reservation) {
    saveWithItems(reservation);
  }

  @Override
  @Transactional
  public Optional<Reservation> findById(ReservationId id) {
    return reservations
        .findByIdForUpdate(id.value())
        .map(this::hydrate)
        .map(entity -> reservationMapper.toDomain(entity, loadSupplierIds(entity)));
  }

  @Override
  public java.util.List<Reservation> findByCustomer(CustomerId customerId) {
    return reservations.findByCustomerIdOrderByCreatedAtDesc(customerId.value()).stream()
        .map(this::hydrate)
        .map(entity -> reservationMapper.toDomain(entity, loadSupplierIds(entity)))
        .toList();
  }

  @Override
  public java.util.List<Reservation> findBySupplier(SupplierId supplierId) {
    return reservations.findBySupplierId(supplierId.value()).stream()
        .map(this::hydrate)
        .map(entity -> reservationMapper.toDomain(entity, loadSupplierIds(entity)))
        .toList();
  }

  @Override
  @Transactional
  public Reservation execute(ReservationId id, Function<Context, Reservation> operation) {
    var reservationEntity =
        reservations
            .findByIdForUpdate(id.value())
            .orElseThrow(
                () ->
                    new com.toolrental.application.reservation.exception
                        .ReservationNotFoundApplicationException(id));

    hydrate(reservationEntity);
    var equipmentUnitIds =
        reservationEntity.getItems().stream()
            .map(item -> item.getEquipmentUnitId())
            .sorted()
            .toList();
    var lockedEquipmentUnits = equipmentUnits.findAllByIdForUpdate(equipmentUnitIds);
    requireCompleteLoad("equipment units", equipmentUnitIds, lockedEquipmentUnits);
    var equipmentUnitEntities = indexEquipmentUnits(lockedEquipmentUnits);

    var toolIds =
        lockedEquipmentUnits.stream()
            .map(EquipmentUnitJpaEntity::getToolId)
            .distinct()
            .sorted()
            .toList();
    var lockedTools = tools.findAllByIdForUpdate(toolIds);
    requireCompleteLoad("tools", toolIds, lockedTools);
    var toolEntities = indexTools(lockedTools);
    var supplierByEquipmentUnit = supplierIdsByEquipmentUnit(lockedEquipmentUnits, toolEntities);

    var participantIds =
        Stream.concat(
                Stream.of(reservationEntity.getCustomerId()),
                supplierByEquipmentUnit.values().stream())
            .distinct()
            .sorted()
            .toList();
    accounts.findAllByIdForUpdate(participantIds);

    var reservation = reservationMapper.toDomain(reservationEntity, supplierByEquipmentUnit);
    var context =
        new PersistenceContext(
            reservation, equipmentUnitEntities, toolEntities, participants, reservations);
    var result = operation.apply(context);
    if (result.status()
            == com.toolrental.domain.reservation.model.valueobject.ReservationStatus.CANCELLED
        && entityManager
                .createNativeQuery("select count(*) from rentals where reservation_id=:id")
                .setParameter("id", id.value())
                .getSingleResult()
            instanceof Number count
        && count.longValue() > 0)
      throw new BusinessRuleException("A reservation with a rental cannot be cancelled");
    saveWithItems(result);
    return result;
  }

  private ReservationJpaEntity hydrate(ReservationJpaEntity entity) {
    entity.attachItems(new java.util.LinkedHashSet<>(items.findByIdReservationId(entity.getId())));
    return entity;
  }

  private void saveWithItems(Reservation reservation) {
    var entity = reservationMapper.toEntity(reservation);
    reservations.saveAndFlush(entity);
    items.saveAll(entity.getItems());
    items.flush();
  }

  private Map<UUID, UUID> loadSupplierIds(ReservationJpaEntity reservation) {
    var ids = reservation.getItems().stream().map(item -> item.getEquipmentUnitId()).toList();
    return equipmentUnits.findSupplierIdsByEquipmentUnitIds(ids).stream()
        .collect(
            Collectors.toMap(
                projection -> projection.getEquipmentUnitId(),
                projection -> projection.getSupplierId()));
  }

  private Map<UUID, EquipmentUnitJpaEntity> indexEquipmentUnits(
      Collection<EquipmentUnitJpaEntity> entities) {
    return entities.stream()
        .collect(
            Collectors.toMap(
                EquipmentUnitJpaEntity::getId,
                Function.identity(),
                (left, right) -> left,
                LinkedHashMap::new));
  }

  private Map<UUID, ToolJpaEntity> indexTools(Collection<ToolJpaEntity> entities) {
    return entities.stream()
        .collect(
            Collectors.toMap(
                ToolJpaEntity::getId,
                Function.identity(),
                (left, right) -> left,
                LinkedHashMap::new));
  }

  private Map<UUID, UUID> supplierIdsByEquipmentUnit(
      Collection<EquipmentUnitJpaEntity> units, Map<UUID, ToolJpaEntity> toolEntities) {
    return units.stream()
        .collect(
            Collectors.toMap(
                EquipmentUnitJpaEntity::getId,
                unit -> requireTool(toolEntities, unit.getToolId()).getSupplierId()));
  }

  private ToolJpaEntity requireTool(Map<UUID, ToolJpaEntity> toolsById, UUID toolId) {
    var tool = toolsById.get(toolId);
    if (tool == null) throw new BusinessRuleException("Tool does not exist");
    return tool;
  }

  private void requireCompleteLoad(
      String resource, Collection<UUID> expectedIds, Collection<?> loadedEntities) {
    if (expectedIds.size() != loadedEntities.size()) {
      throw new BusinessRuleException("Reservation references missing " + resource);
    }
  }

  private final class PersistenceContext implements Context {
    private final Reservation reservation;
    private final Map<UUID, EquipmentUnitJpaEntity> equipmentUnitEntities;
    private final Map<UUID, ToolJpaEntity> toolEntities;
    private final ParticipantsPort participants;
    private final ReservationJpaRepository reservations;

    private PersistenceContext(
        Reservation reservation,
        Map<UUID, EquipmentUnitJpaEntity> equipmentUnitEntities,
        Map<UUID, ToolJpaEntity> toolEntities,
        ParticipantsPort participants,
        ReservationJpaRepository reservations) {
      this.reservation = reservation;
      this.equipmentUnitEntities = equipmentUnitEntities;
      this.toolEntities = toolEntities;
      this.participants = participants;
      this.reservations = reservations;
    }

    @Override
    public Reservation reservation() {
      return reservation;
    }

    @Override
    public EquipmentUnit equipmentUnit(EquipmentUnitId id) {
      var entity = equipmentUnitEntities.get(id.value());
      if (entity == null) throw new BusinessRuleException("Equipment unit does not exist");
      return equipmentUnitMapper.toDomain(entity);
    }

    @Override
    public Tool tool(ToolId id) {
      var entity = toolEntities.get(id.value());
      if (entity == null) throw new BusinessRuleException("Tool does not exist");
      return toolMapper.toDomain(entity);
    }

    @Override
    public boolean isCustomerActive(CustomerId id) {
      return participants.isCustomerActive(id);
    }

    @Override
    public boolean isSupplierActive(SupplierId id) {
      return participants.isSupplierActive(id);
    }

    @Override
    public boolean isOccupied(EquipmentUnitId id, ReservationPeriod period) {
      return reservations.existsConfirmedOverlap(
          id.value(), reservation.id().value(), period.startDate(), period.returnDate());
    }
  }
}
