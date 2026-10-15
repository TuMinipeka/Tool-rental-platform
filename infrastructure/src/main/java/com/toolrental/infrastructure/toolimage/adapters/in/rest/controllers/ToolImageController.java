package com.toolrental.infrastructure.toolimage.adapters.in.rest.controllers;

import com.toolrental.application.toolimage.command.RegisterToolImageCommand;
import com.toolrental.application.toolimage.dto.ToolImageResponse;
import com.toolrental.application.toolimage.usecase.ManageToolImageUseCase;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.toolimage.model.valueobject.ToolImageId;
import com.toolrental.infrastructure.common.web.Actor;
import com.toolrental.infrastructure.toolimage.adapters.in.rest.dtos.ToolImageRequest;
import jakarta.validation.Valid;
import java.security.Principal;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/tool-images")
public class ToolImageController {
  private final ManageToolImageUseCase useCase;

  public ToolImageController(ManageToolImageUseCase useCase) {
    this.useCase = useCase;
  }

  @GetMapping
  public List<ToolImageResponse> list(Principal principal) {
    return useCase.list(Actor.id(principal));
  }

  @GetMapping("/entry")
  public ToolImageResponse find(
      Principal principal, @RequestParam UUID toolId, @RequestParam String imageUrl) {
    return useCase.find(Actor.id(principal), new ToolImageId(new ToolId(toolId), imageUrl));
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ToolImageResponse register(
      Principal principal, @Valid @RequestBody ToolImageRequest request) {
    return useCase.register(
        Actor.id(principal),
        new RegisterToolImageCommand(
            new ToolImageId(new ToolId(request.toolId()), request.imageUrl())));
  }

  @DeleteMapping("/entry")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void remove(
      Principal principal, @RequestParam UUID toolId, @RequestParam String imageUrl) {
    useCase.remove(Actor.id(principal), new ToolImageId(new ToolId(toolId), imageUrl));
  }
}
