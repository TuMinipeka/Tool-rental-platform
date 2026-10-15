package com.toolrental.infrastructure.toolimage.adapters.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class ToolImageJpaId implements Serializable {
  @Column(name = "tool_id")
  private UUID toolId;

  @Column(name = "image_url", length = 2048)
  private String imageUrl;

  protected ToolImageJpaId() {}

  public ToolImageJpaId(UUID toolId, String imageUrl) {
    this.toolId = toolId;
    this.imageUrl = imageUrl;
  }

  public UUID getToolId() {
    return toolId;
  }

  public String getImageUrl() {
    return imageUrl;
  }

  public boolean equals(Object other) {
    return other instanceof ToolImageJpaId key
        && Objects.equals(toolId, key.toolId)
        && Objects.equals(imageUrl, key.imageUrl);
  }

  public int hashCode() {
    return Objects.hash(toolId, imageUrl);
  }
}
