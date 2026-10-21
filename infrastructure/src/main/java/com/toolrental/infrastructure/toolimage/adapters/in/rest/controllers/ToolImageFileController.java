package com.toolrental.infrastructure.toolimage.adapters.in.rest.controllers;

import com.toolrental.application.toolimage.command.UploadToolImageCommand;
import com.toolrental.application.toolimage.dto.ToolImageResponse;
import com.toolrental.application.toolimage.usecase.ManageToolImageUseCase;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.toolimage.model.valueobject.ToolImageReference;
import com.toolrental.domain.toolimage.port.ToolImageStorage;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.toolimage.exception.ToolImageStorageInfrastructureException;
import java.io.IOException;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public final class ToolImageFileController {
  private final ManageToolImageUseCase images;
  private final ToolImageStorage storage;

  public ToolImageFileController(ManageToolImageUseCase images, ToolImageStorage storage) {
    this.images = images;
    this.storage = storage;
  }

  @GetMapping({"/api/v1/tools/{id}/images", "/api/tools/{id}/images"})
  public List<ToolImageResponse> list(Principal principal, @PathVariable UUID id) {
    return images.listForTool(Actor.id(principal), new ToolId(id));
  }

  @PostMapping(
      value = {"/api/v1/tools/{id}/images", "/api/tools/{id}/images"},
      consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  @ResponseStatus(HttpStatus.CREATED)
  public ToolImageResponse upload(
      Principal principal, @PathVariable UUID id, @RequestPart("file") MultipartFile file) {
    var actor = Actor.id(principal);
    if (file.getSize() > 5 * 1024 * 1024)
      throw new org.springframework.web.multipart.MaxUploadSizeExceededException(5 * 1024 * 1024);
    try {
      return images.upload(actor, new UploadToolImageCommand(new ToolId(id), file.getBytes()));
    } catch (IOException error) {
      throw new ToolImageStorageInfrastructureException(error);
    }
  }

  @GetMapping("/api/v1/public/tool-images/files/{filename}")
  public ResponseEntity<byte[]> read(@PathVariable String filename) {
    var image = storage.read(ToolImageReference.PUBLIC_PREFIX + filename);
    return ResponseEntity.ok()
        .contentType(MediaType.parseMediaType(image.contentType()))
        .header("X-Content-Type-Options", "nosniff")
        .header("Content-Disposition", "inline")
        .cacheControl(CacheControl.maxAge(java.time.Duration.ofHours(1)))
        .body(image.content());
  }
}
