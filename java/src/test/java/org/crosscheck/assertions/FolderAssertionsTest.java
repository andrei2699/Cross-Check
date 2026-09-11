package org.crosscheck.assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.crosscheck.ResourceFileReader;
import org.crosscheck.comparators.CsvFileComparator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FolderAssertionsTest {
  @TempDir Path tempDir;

  @Test
  void assertFolder() throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = tempDir.resolve("actual-folder");
    Files.createDirectories(expected);
    Files.createDirectories(actual);

    Path expectedResourcePath =
        ResourceFileReader.getResourcePath("test-data/comparators/csv/input.csv");
    Path actualResourcePath =
        ResourceFileReader.getResourcePath("test-data/comparators/csv/different-column.csv");

    Files.copy(expectedResourcePath, expected.resolve("sub-file.csv"));
    Files.copy(actualResourcePath, actual.resolve("sub-file.csv"));

    Assertions.assertThrows(
        CrossCheckException.class,
        () ->
            CrossCheck.folder()
                .withComparators(List.of(new CsvFileComparator()))
                .assertFolder(expected, actual));
  }
}
