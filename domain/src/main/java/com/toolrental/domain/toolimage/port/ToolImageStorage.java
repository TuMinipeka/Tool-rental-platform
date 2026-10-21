package com.toolrental.domain.toolimage.port;

import com.toolrental.domain.toolimage.model.valueobject.ToolImageFile;

public interface ToolImageStorage {
  String store(byte[] content);

  ToolImageFile read(String reference);

  void delete(String reference);
}
