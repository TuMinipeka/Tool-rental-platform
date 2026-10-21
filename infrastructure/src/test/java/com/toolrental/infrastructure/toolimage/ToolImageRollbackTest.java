package com.toolrental.infrastructure.toolimage;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import com.toolrental.application.common.service.WorkflowReferences;
import com.toolrental.application.toolimage.command.UploadToolImageCommand;
import com.toolrental.application.toolimage.usecase.ManageToolImageUseCase;
import com.toolrental.domain.common.port.AccessPolicy;
import com.toolrental.domain.common.port.UnitOfWork;
import com.toolrental.domain.tool.model.valueobject.ToolId;
import com.toolrental.domain.toolimage.port.ToolImageStorage;
import com.toolrental.domain.toolimage.port.repository.ToolImageRepository;
import java.time.Clock;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class ToolImageRollbackTest {
  @Test
  void removesNewFileWhenTheDatabaseCommitFails() {
    var images = mock(ToolImageRepository.class);
    var storage = mock(ToolImageStorage.class);
    var references = mock(WorkflowReferences.class);
    var id = new ToolId(UUID.randomUUID());
    var uri = "/api/v1/public/tool-images/files/" + UUID.randomUUID() + ".png";
    when(images.findByToolId(id)).thenReturn(List.of());
    when(images.save(any())).thenAnswer(call -> call.getArgument(0));
    when(storage.store(any())).thenReturn(uri);
    UnitOfWork failingCommit =
        new UnitOfWork() {
          public <T> T execute(Supplier<T> operation) {
            operation.get();
            throw new IllegalStateException("Commit failed");
          }
        };
    var service =
        new ManageToolImageUseCase(
            images,
            failingCommit,
            mock(AccessPolicy.class),
            references,
            Clock.systemUTC(),
            storage);
    assertThatThrownBy(
            () -> service.upload(UUID.randomUUID(), new UploadToolImageCommand(id, new byte[] {1})))
        .isInstanceOf(IllegalStateException.class);
    verify(storage).delete(uri);
  }
}
