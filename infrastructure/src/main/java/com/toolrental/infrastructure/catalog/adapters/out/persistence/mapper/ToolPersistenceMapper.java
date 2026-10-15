package com.toolrental.infrastructure.catalog.adapters.out.persistence.mapper;

import com.toolrental.domain.catalog.model.aggregate.Tool;
import com.toolrental.domain.catalog.model.valueobject.CategoryId;
import com.toolrental.domain.catalog.model.valueobject.ToolId;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.model.valueobject.SupplierId;
import com.toolrental.infrastructure.catalog.adapters.out.persistence.entity.ToolJpaEntity;
import java.net.URI;
import java.util.Currency;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class ToolPersistenceMapper {
  private static final String PUBLISHED = "PUBLISHED";
  private static final String UNPUBLISHED = "UNPUBLISHED";

  public Tool toDomain(ToolJpaEntity entity) {
    return Tool.restore(
        new ToolId(entity.getId()),
        new SupplierId(entity.getSupplierId()),
        new CategoryId(entity.getCategoryId()),
        entity.getName(),
        entity.getDescription(),
        new Money(entity.getCurrentDailyRate(), Currency.getInstance(entity.getRateCurrency())),
        entity.getImageUrls().stream().map(URI::create).collect(Collectors.toUnmodifiableSet()),
        PUBLISHED.equals(entity.getPublicationStatus()));
  }

  public ToolJpaEntity toEntity(Tool aggregate) {
    return new ToolJpaEntity(
        aggregate.id().value(),
        aggregate.supplierId().value(),
        aggregate.categoryId().value(),
        aggregate.name(),
        aggregate.description(),
        aggregate.dailyRate().amount(),
        aggregate.dailyRate().currency().getCurrencyCode(),
        aggregate.published() ? PUBLISHED : UNPUBLISHED,
        aggregate.imageUrls().stream().map(URI::toString).collect(Collectors.toUnmodifiableSet()));
  }
}
