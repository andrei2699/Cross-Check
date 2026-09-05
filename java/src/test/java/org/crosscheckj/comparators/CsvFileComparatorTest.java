package org.crosscheckj.comparators;

import java.nio.file.Path;
import java.util.List;
import org.crosscheckj.ResourceFileReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvReadFeature;

class CsvFileComparatorTest {
  private CsvFileComparator comparator;

  @BeforeEach
  void setup() {
    comparator = new CsvFileComparator();
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "file",
        "folder/file",
        "f.abc",
        "folder/file.abc",
        "file.csv.other",
        "folder/file.csv.other"
      })
  void canApply_shouldReturnFalse_whenExtensionIsNotSupported(String path) {
    boolean canApply = comparator.canApply(Path.of(path));

    Assertions.assertFalse(canApply);
  }

  @ParameterizedTest
  @ValueSource(strings = {"file.csv", "folder/file.csv"})
  void canApply_shouldReturnTrue_whenExtensionIsSupported(String path) {
    boolean canApply = comparator.canApply(Path.of(path));

    Assertions.assertTrue(canApply);
  }

  @ParameterizedTest
  @ValueSource(
      strings = {
        "file",
        "folder/file",
        "f.abc",
        "folder/file.abc",
        "file.csv.other",
        "folder/file.csv.other",
        "f.abc.csv",
        "folder/file.csv",
      })
  void canApply_withOverriddenExtensions__shouldReturnFalse_whenExtensionIsNotSupported(
      String path) {
    comparator = new CsvFileComparator(List.of(".txt", ".tsv"));

    boolean canApply = comparator.canApply(Path.of(path));

    Assertions.assertFalse(canApply);
  }

  @ParameterizedTest
  @ValueSource(strings = {"file.txt", "folder/file.txt", "file.tsv", "folder/file.tsv"})
  void canApply_withOverriddenExtensions_shouldReturnTrue_whenExtensionIsSupported(String path) {
    comparator = new CsvFileComparator(List.of(".txt", ".tsv"));

    boolean canApply = comparator.canApply(Path.of(path));

    Assertions.assertTrue(canApply);
  }

  @Test
  void compare_shouldReturnEmptyList_whenFilesAreIdentical() {
    Path expected = ResourceFileReader.getResourcePath("test-data/comparators/csv/input.csv");
    Path actual = ResourceFileReader.getResourcePath("test-data/comparators/csv/same-content.csv");

    List<FileEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(0, differences.size());
  }

  @Test
  void compare_shouldReturnTheChangedValues_whenActualFileHasSameColumnsButDifferentValues() {
    Path expected = ResourceFileReader.getResourcePath("test-data/comparators/csv/input.csv");
    Path actual =
        ResourceFileReader.getResourcePath("test-data/comparators/csv/different-value.csv");

    List<FileEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(3, differences.size());
    Assertions.assertEquals(new FileEntryDifference(1, "a", "other"), differences.getFirst());
    Assertions.assertEquals(new FileEntryDifference(1, "2", "1"), differences.get(1));
    Assertions.assertEquals(new FileEntryDifference(1, "true", "false"), differences.get(2));
  }

  @Test
  void compare_shouldReturnTheChangedValues_whenActualFileHasDifferentColumnsButSameContent() {
    Path expected = ResourceFileReader.getResourcePath("test-data/comparators/csv/input.csv");
    Path actual =
        ResourceFileReader.getResourcePath("test-data/comparators/csv/different-column.csv");

    List<FileEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(3, differences.size());
    Assertions.assertEquals(
        new FileEntryDifference(0, "string", "string1"), differences.getFirst());
    Assertions.assertEquals(new FileEntryDifference(0, "number", "number1"), differences.get(1));
    Assertions.assertEquals(new FileEntryDifference(0, "boolean", "boolean1"), differences.get(2));
  }

  @Test
  void compare_shouldReturnTheChangedValues_whenActualFileHasLessColumns() {
    Path expected = ResourceFileReader.getResourcePath("test-data/comparators/csv/input.csv");
    Path actual =
        ResourceFileReader.getResourcePath("test-data/comparators/csv/input-less-values.csv");

    List<FileEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(2, differences.size());
    Assertions.assertEquals(new FileEntryDifference(0, "boolean", null), differences.getFirst());
    Assertions.assertEquals(new FileEntryDifference(1, "true", null), differences.get(1));
  }

  @Test
  void compare_shouldReturnTheChangedValues_whenActualFileHasMoreColumns() {
    Path expected = ResourceFileReader.getResourcePath("test-data/comparators/csv/input.csv");
    Path actual =
        ResourceFileReader.getResourcePath("test-data/comparators/csv/input-more-values.csv");

    List<FileEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(2, differences.size());
    Assertions.assertEquals(new FileEntryDifference(0, null, "string2"), differences.getFirst());
    Assertions.assertEquals(new FileEntryDifference(1, null, "b"), differences.get(1));
  }

  @Test
  void compare_withCustomCsvMapper_shouldReturnEmptyList_whenFilesDifferentHeaderCasing() {
    Path expected = ResourceFileReader.getResourcePath("test-data/comparators/csv/input.csv");
    Path actual =
        ResourceFileReader.getResourcePath("test-data/comparators/csv/input-with-comments.csv");
    comparator =
        new CsvFileComparator(
            List.of("csv"), CsvMapper.builder().enable(CsvReadFeature.ALLOW_COMMENTS).build());

    List<FileEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(0, differences.size());
  }
}
