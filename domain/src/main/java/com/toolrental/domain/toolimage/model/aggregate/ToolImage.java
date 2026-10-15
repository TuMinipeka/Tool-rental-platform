package com.toolrental.domain.toolimage.model.aggregate;

import com.toolrental.domain.common.exception.BusinessRuleException;
import com.toolrental.domain.common.model.AggregateRoot;
import com.toolrental.domain.toolimage.event.ToolImageChanged;
import com.toolrental.domain.toolimage.model.valueobject.ToolImageId;
import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

public final class ToolImage extends AggregateRoot {
  private final ToolImageId id;

  private ToolImage(ToolImageId id) {
    Objects.requireNonNull(id);
    var uri = java.net.URI.create(id.imageUrl());
    if (uri.getHost() == null
        || !("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())))
      throw new BusinessRuleException("Image requires an absolute HTTP(S) URL");
    this.id = id;
  }

  public static ToolImage restore(ToolImageId id) {
    return new ToolImage(id);
  }

  public static ToolImage register(ToolImageId id, Clock clock) {
    var result = new ToolImage(id);
    result.recordEvent(new ToolImageChanged(id, Instant.now(clock)));
    return result;
  }

  public ToolImageId id() {
    return id;
  }
}
