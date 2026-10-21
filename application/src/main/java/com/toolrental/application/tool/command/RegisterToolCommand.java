package com.toolrental.application.tool.command;

import com.toolrental.domain.category.model.valueobject.CategoryId;
import com.toolrental.domain.common.model.valueobject.Money;
import com.toolrental.domain.supplier.model.valueobject.SupplierId;
import java.net.URI;
import java.util.Set;

public record RegisterToolCommand(
    SupplierId supplierId,
    CategoryId categoryId,
    String name,
    String description,
    String serviceCity,
    Money dailyRate,
    Set<URI> imageUrls) {
  public RegisterToolCommand {
    imageUrls = Set.copyOf(imageUrls);
  }
}
