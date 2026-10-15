package com.toolrental.domain.toolimage.model.valueobject;

import com.toolrental.domain.tool.model.valueobject.ToolId;
import java.util.Objects;

public record ToolImageId(ToolId toolId, String imageUrl) {
  public ToolImageId {
    Objects.requireNonNull(toolId);
    Objects.requireNonNull(imageUrl);
  }
}
