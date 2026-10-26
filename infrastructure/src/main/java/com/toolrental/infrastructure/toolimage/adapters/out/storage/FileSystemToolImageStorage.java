package com.toolrental.infrastructure.toolimage.adapters.out.storage;

import com.toolrental.application.toolimage.exception.ToolImageNotFoundApplicationException;
import com.toolrental.domain.toolimage.model.valueobject.ToolImageFile;
import com.toolrental.domain.toolimage.model.valueobject.ToolImageReference;
import com.toolrental.domain.toolimage.port.ToolImageStorage;
import com.toolrental.infrastructure.toolimage.exception.ToolImageStorageInfrastructureException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public final class FileSystemToolImageStorage implements ToolImageStorage {
  private final Path root;

  public FileSystemToolImageStorage(
      @Value("${tool-images.storage-directory:./storage/tool-images}") String directory) {
    root = Path.of(directory).toAbsolutePath().normalize();
  }

  @Override
  public String store(byte[] content) {
    if (content == null || content.length == 0 || content.length > 5 * 1024 * 1024)
      throw new IllegalArgumentException("Image must contain at most 5 MiB");
    try (var input = ImageIO.createImageInputStream(new ByteArrayInputStream(content))) {
      var readers = ImageIO.getImageReaders(input);
      if (!readers.hasNext()) throw new IllegalArgumentException("Invalid image content");
      var reader = readers.next();
      try {
        reader.setInput(input);
        var format = reader.getFormatName();
        var extension = format.equalsIgnoreCase("PNG") ? "png" : "jpg";
        if (!format.equalsIgnoreCase("PNG") && !format.equalsIgnoreCase("JPEG"))
          throw new IllegalArgumentException("Only JPEG and PNG images are supported");
        if (reader.getWidth(0) > 4096 || reader.getHeight(0) > 4096)
          throw new IllegalArgumentException("Image dimensions exceed 4096 pixels");
        var image = reader.read(0);
        var filename = UUID.randomUUID() + "." + extension;
        Files.createDirectories(root);
        var target = root.resolve(filename);
        try {
          if (!ImageIO.write(image, extension, target.toFile()))
            throw new IOException("Image encoder is unavailable");
        } catch (IOException failure) {
          Files.deleteIfExists(target);
          throw failure;
        }
        return ToolImageReference.PUBLIC_PREFIX + filename;
      } finally {
        reader.dispose();
      }
    } catch (IOException failure) {
      throw new ToolImageStorageInfrastructureException(failure);
    }
  }

  @Override
  public ToolImageFile read(String reference) {
    var path = resolve(reference);
    try {
      return new ToolImageFile(
          Files.readAllBytes(path), reference.endsWith(".png") ? "image/png" : "image/jpeg");
    } catch (NoSuchFileException failure) {
      throw new ToolImageNotFoundApplicationException();
    } catch (IOException failure) {
      throw new ToolImageStorageInfrastructureException(failure);
    }
  }

  @Override
  public void delete(String reference) {
    var path = resolve(reference);
    try {
      Files.deleteIfExists(path);
    } catch (IOException failure) {
      throw new ToolImageStorageInfrastructureException(failure);
    }
  }

  private Path resolve(String reference) {
    var value = new ToolImageReference(reference);
    if (!value.managed()) throw new IllegalArgumentException("Managed image reference required");
    return root.resolve(reference.substring(ToolImageReference.PUBLIC_PREFIX.length()));
  }
}
