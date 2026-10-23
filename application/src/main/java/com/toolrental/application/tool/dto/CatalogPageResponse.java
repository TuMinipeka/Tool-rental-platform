package com.toolrental.application.tool.dto;

import java.util.List;

public record CatalogPageResponse(
    List<CatalogToolResponse> content, int page, int size, long totalElements, int totalPages) {
  public CatalogPageResponse {
    content = List.copyOf(content);
  }
}
