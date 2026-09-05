package org.crosscheckj;

import java.io.File;
import java.nio.file.Path;

public final class ResourceFileReader {
  private ResourceFileReader() {}

  public static Path getResourcePath(String path) {
    File file = new File("src/test/resources");

    return file.toPath().resolve(path).toAbsolutePath();
  }
}
