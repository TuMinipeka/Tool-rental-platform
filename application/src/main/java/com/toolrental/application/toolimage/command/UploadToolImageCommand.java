package com.toolrental.application.toolimage.command;

import com.toolrental.domain.tool.model.valueobject.ToolId;

public record UploadToolImageCommand(ToolId toolId, byte[] content) {
  public UploadToolImageCommand {
    content = content.clone();
  }

  @Override
  public byte[] content() {
    return content.clone();
  }
}
