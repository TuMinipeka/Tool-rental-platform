package com.toolrental.domain.toolimage.model.valueobject;

import java.net.URI;

public record ToolImageReference(String value) {
  public static final String PUBLIC_PREFIX = "/api/v1/public/tool-images/files/";

  public ToolImageReference {
    if (value == null || value.isBlank() || value.length() > 2048)
      throw new IllegalArgumentException("Invalid tool image reference");
    var uri = URI.create(value);
    boolean managed = value.matches("/api/v1/public/tool-images/files/[0-9a-f-]{36}\\.(png|jpg)");
    boolean external =
        ("https".equalsIgnoreCase(uri.getScheme()) || "http".equalsIgnoreCase(uri.getScheme()))
            && uri.getHost() != null
            && uri.getUserInfo() == null;
    if (!managed && !external)
      throw new IllegalArgumentException(
          "Images require an HTTP(S) URL or managed image reference");
  }

  public boolean managed() {
    return value.startsWith(PUBLIC_PREFIX);
  }
}
