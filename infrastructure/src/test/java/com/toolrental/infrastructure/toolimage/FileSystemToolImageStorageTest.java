package com.toolrental.infrastructure.toolimage;

import static org.assertj.core.api.Assertions.*;

import com.toolrental.infrastructure.toolimage.adapters.out.storage.FileSystemToolImageStorage;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class FileSystemToolImageStorageTest {
  @TempDir Path root;

  @Test
  void normalizesAndReadsFilesAfterRecreatingTheAdapter() throws Exception {
    var output = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(12, 12, BufferedImage.TYPE_INT_RGB), "png", output);
    var first = new FileSystemToolImageStorage(root.toString());
    var reference = first.store(output.toByteArray());
    var restored = new FileSystemToolImageStorage(root.toString());
    assertThat(restored.read(reference).contentType()).isEqualTo("image/png");
    assertThat(
            ImageIO.read(new java.io.ByteArrayInputStream(restored.read(reference).content()))
                .getWidth())
        .isEqualTo(12);
    assertThatThrownBy(() -> first.read("/api/v1/public/tool-images/files/../../secret.png"))
        .isInstanceOf(IllegalArgumentException.class);
    restored.delete(reference);
    assertThatThrownBy(() -> first.read(reference))
        .isInstanceOf(
            com.toolrental.application.toolimage.exception.ToolImageNotFoundApplicationException
                .class);
  }

  @Test
  void storesJpegWithoutResizingAndServesTheCorrectMediaType() throws Exception {
    var output = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(40, 30, BufferedImage.TYPE_INT_RGB), "jpeg", output);
    var store = new FileSystemToolImageStorage(root.toString());
    var file = store.read(store.store(output.toByteArray()));
    assertThat(file.contentType()).isEqualTo("image/jpeg");
    var image = ImageIO.read(new java.io.ByteArrayInputStream(file.content()));
    assertThat(image.getWidth()).isEqualTo(40);
    assertThat(image.getHeight()).isEqualTo(30);
  }

  @Test
  void rejectsOtherFormatsOversizedDimensionsAndInvalidContent() throws Exception {
    var store = new FileSystemToolImageStorage(root.toString());
    assertThatThrownBy(() -> store.store(new byte[] {1, 2, 3}))
        .isInstanceOf(IllegalArgumentException.class);
    assertThatThrownBy(() -> store.store(new byte[5 * 1024 * 1024 + 1]))
        .isInstanceOf(IllegalArgumentException.class);
    var output = new ByteArrayOutputStream();
    ImageIO.write(new BufferedImage(4097, 1, BufferedImage.TYPE_INT_RGB), "png", output);
    assertThatThrownBy(() -> store.store(output.toByteArray()))
        .isInstanceOf(IllegalArgumentException.class);
    output.reset();
    ImageIO.write(new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB), "gif", output);
    assertThatThrownBy(() -> store.store(output.toByteArray()))
        .isInstanceOf(IllegalArgumentException.class);
  }
}
