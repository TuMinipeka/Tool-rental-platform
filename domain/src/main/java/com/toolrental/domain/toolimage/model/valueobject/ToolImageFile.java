package com.toolrental.domain.toolimage.model.valueobject;

public record ToolImageFile(byte[] content, String contentType) {
  public ToolImageFile {
    content = content.clone();
  }

  @Override
  public byte[] content() {
    return content.clone();
  }
}
