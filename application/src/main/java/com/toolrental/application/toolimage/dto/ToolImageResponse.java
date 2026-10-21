package com.toolrental.application.toolimage.dto;

import com.toolrental.domain.toolimage.model.aggregate.ToolImage;
import java.util.UUID;

public record ToolImageResponse(UUID toolId, String imageUrl) {
  public static ToolImageResponse from(ToolImage value) {
    return new ToolImageResponse(value.id().toolId().value(), value.id().imageUrl());
  }
}
