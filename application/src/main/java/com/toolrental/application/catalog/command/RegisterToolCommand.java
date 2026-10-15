package com.toolrental.application.catalog.command;

import com.toolrental.domain.catalog.model.valueobject.CategoryId;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.common.model.valueobject.SupplierId;
import java.net.URI;
import java.util.Set;

public record RegisterToolCommand(
    SupplierId supplierId,
    CategoryId categoryId,
    String name,
    String description,
    Money dailyRate,
    Set<URI> imageUrls) {
  public RegisterToolCommand {
    imageUrls = Set.copyOf(imageUrls);
  }
}
