package org.crosscheckj.comparators;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

public class FolderComparatorTest {
  private FolderComparator comparator;
  @TempDir Path tempDir;

  @BeforeEach
  void setup() {
    comparator = new FolderComparator();
  }

  @Test
  void shouldReturnMissingExpected_whenExpectedFolderDoesNotExists() {
    Path expected = Path.of("non-existing-expected");
    Path actual = Path.of("non-existing-actual");

    RuntimeException exception =
        Assertions.assertThrows(RuntimeException.class, () -> comparator.compare(expected, actual));
    Assertions.assertInstanceOf(FileNotFoundException.class, exception.getCause());
  }

  @Test
  void shouldThrowException_whenExpectedFolderIsAFile() throws IOException {
    Path expected = tempDir.resolve("expected-file");
    Path actual = Path.of("non-existing-actual");
    Files.createFile(expected);

    RuntimeException exception =
        Assertions.assertThrows(RuntimeException.class, () -> comparator.compare(expected, actual));
    Assertions.assertInstanceOf(NotDirectoryException.class, exception.getCause());
  }

  @Test
  void shouldThrowException_whenActualFolderDoesNotExists() throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = Path.of("non-existing-actual");
    Files.createDirectories(expected);

    RuntimeException exception =
        Assertions.assertThrows(RuntimeException.class, () -> comparator.compare(expected, actual));
    Assertions.assertInstanceOf(FileNotFoundException.class, exception.getCause());
  }

  @Test
  void shouldThrowException_whenActualFolderIsAFile() throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = tempDir.resolve("actual-file");
    Files.createDirectories(expected);
    Files.createFile(actual);

    RuntimeException exception =
        Assertions.assertThrows(RuntimeException.class, () -> comparator.compare(expected, actual));
    Assertions.assertInstanceOf(NotDirectoryException.class, exception.getCause());
  }

  @Test
  void shouldReturnEmptyList_whenBothFoldersAreEmpty() throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = tempDir.resolve("actual-folder");
    Files.createDirectories(expected);
    Files.createDirectories(actual);

    List<FolderEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(0, differences.size());
  }

  @Test
  void shouldReturnDifferences_whenExpectedHasEntryAndActualDoesNot() throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = tempDir.resolve("actual-folder");
    Files.createDirectories(expected);
    Files.createDirectories(expected.resolve("sub-folder"));
    Files.createDirectories(actual);

    List<FolderEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(1, differences.size());
    Assertions.assertEquals(
        new FolderEntryDifference.DifferentChildCount(expected, actual, 1, 0),
        differences.getFirst());
  }

  @Test
  void shouldReturnDifferences_whenActualHasEntryAndExpectedDoesNot() throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = tempDir.resolve("actual-folder");
    Files.createDirectories(expected);
    Files.createDirectories(actual);
    Files.createDirectories(actual.resolve("sub-folder"));

    List<FolderEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(1, differences.size());
    Assertions.assertEquals(
        new FolderEntryDifference.DifferentChildCount(expected, actual, 0, 1),
        differences.getFirst());
  }

  @Test
  void shouldReturnDifferences_whenExpectedHasDifferentEntriesThanTheActualAtFirstLevel()
      throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = tempDir.resolve("actual-folder");
    Files.createDirectories(expected);
    Files.createDirectories(actual);
    Files.createFile(expected.resolve("sub-file-1"));
    Files.createFile(actual.resolve("sub-file-2"));

    List<FolderEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(1, differences.size());
    Assertions.assertEquals(
        new FolderEntryDifference.DifferentChild(
            expected.resolve("sub-file-1"), actual.resolve("sub-file-2")),
        differences.getFirst());
  }

  @Test
  void shouldReturnDifferences_whenExpectedHasDifferentEntryTypesThanTheActualAtFirstLevel()
      throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = tempDir.resolve("actual-folder");
    Files.createDirectories(expected);
    Files.createDirectories(actual);
    Files.createDirectories(expected.resolve("sub-folder"));
    Files.createFile(actual.resolve("sub-folder"));

    List<FolderEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(1, differences.size());
    Assertions.assertEquals(
        new FolderEntryDifference.DifferentChildType(
            expected.resolve("sub-folder"), actual.resolve("sub-folder"), true, false),
        differences.getFirst());
  }

  @Test
  void shouldReturnDifferences_whenActualHasDifferentEntryTypesThanTheExpectedAtFirstLevel()
      throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = tempDir.resolve("actual-folder");
    Files.createDirectories(expected);
    Files.createDirectories(actual);
    Files.createFile(expected.resolve("sub-file"));
    Files.createDirectories(actual.resolve("sub-file"));

    List<FolderEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(1, differences.size());
    Assertions.assertEquals(
        new FolderEntryDifference.DifferentChildType(
            expected.resolve("sub-file"), actual.resolve("sub-file"), false, true),
        differences.getFirst());
  }

  @Test
  void shouldReturnDifferences_whenExpectedHasDifferentEntriesThanTheActualAtSecondLevel()
      throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = tempDir.resolve("actual-folder");
    Files.createDirectories(expected.resolve("sub-folder"));
    Files.createDirectories(actual.resolve("sub-folder"));
    Files.createFile(expected.resolve("sub-folder").resolve("sub-file-1"));
    Files.createFile(actual.resolve("sub-folder").resolve("sub-file-2"));

    List<FolderEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(1, differences.size());
    Assertions.assertEquals(
        new FolderEntryDifference.DifferentChild(
            expected.resolve("sub-folder").resolve("sub-file-1"),
            actual.resolve("sub-folder").resolve("sub-file-2")),
        differences.getFirst());
  }

  @Test
  void shouldReturnDifferences_whenExpectedHasDifferentEntryTypesThanTheActualAtSecondLevel()
      throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = tempDir.resolve("actual-folder");
    Files.createDirectories(expected.resolve("sub-folder"));
    Files.createDirectories(actual.resolve("sub-folder"));
    Files.createDirectories(expected.resolve("sub-folder").resolve("sub-nested-folder"));
    Files.createFile(actual.resolve("sub-folder").resolve("sub-nested-folder"));

    List<FolderEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(1, differences.size());
    Assertions.assertEquals(
        new FolderEntryDifference.DifferentChildType(
            expected.resolve("sub-folder").resolve("sub-nested-folder"),
            actual.resolve("sub-folder").resolve("sub-nested-folder"),
            true,
            false),
        differences.getFirst());
  }

  @Test
  void shouldReturnDifferences_whenActualHasDifferentEntryTypesThanTheExpectedAtSecondLevel()
      throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = tempDir.resolve("actual-folder");
    Files.createDirectories(expected.resolve("sub-folder"));
    Files.createDirectories(actual.resolve("sub-folder"));
    Files.createFile(expected.resolve("sub-folder").resolve("sub-file"));
    Files.createDirectories(actual.resolve("sub-folder").resolve("sub-file"));

    List<FolderEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(1, differences.size());
    Assertions.assertEquals(
        new FolderEntryDifference.DifferentChildType(
            expected.resolve("sub-folder").resolve("sub-file"),
            actual.resolve("sub-folder").resolve("sub-file"),
            false,
            true),
        differences.getFirst());
  }

  @Test
  void shouldReturnEmptyList_whenNotApplicableComparatorIsUsed() throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = tempDir.resolve("actual-folder");
    Files.createDirectories(expected);
    Files.createDirectories(actual);
    Files.createFile(expected.resolve("sub-file"));
    Files.createFile(actual.resolve("sub-file"));

    comparator = new FolderComparator(List.of(new NotApplicableComparator()));
    List<FolderEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(0, differences.size());
  }

  @Test
  void shouldReturnEmptyList_whenAlwaysNoDiffComparatorIsUsed() throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = tempDir.resolve("actual-folder");
    Files.createDirectories(expected);
    Files.createDirectories(actual);
    Files.createFile(expected.resolve("sub-file"));
    Files.createFile(actual.resolve("sub-file"));

    comparator = new FolderComparator(List.of(new AlwaysNoDiffComparator()));
    List<FolderEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(0, differences.size());
  }

  @Test
  void shouldReturnDifferences_whenAlwaysDiffComparatorIsUsed() throws IOException {
    Path expected = tempDir.resolve("expected-folder");
    Path actual = tempDir.resolve("actual-folder");
    Files.createDirectories(expected);
    Files.createDirectories(actual);
    Files.createFile(expected.resolve("sub-file"));
    Files.createFile(actual.resolve("sub-file"));

    comparator = new FolderComparator(List.of(new AlwaysDiffComparator()));
    List<FolderEntryDifference> differences = comparator.compare(expected, actual);

    Assertions.assertEquals(1, differences.size());
    Assertions.assertEquals(
        new FolderEntryDifference.FileCompareDiff(
            expected.resolve("sub-file"),
            actual.resolve("sub-file"),
            List.of(new FileEntryDifference(0, "expected", "actual"))),
        differences.getFirst());
  }
}

class NotApplicableComparator implements FileComparator {
  @Override
  public boolean canApply(Path file) {
    return false;
  }

  @Override
  public List<FileEntryDifference> compare(Path expected, Path actual) {
    return List.of(new FileEntryDifference(0, "", ""));
  }
}

class AlwaysDiffComparator implements FileComparator {
  @Override
  public boolean canApply(Path file) {
    return true;
  }

  @Override
  public List<FileEntryDifference> compare(Path expected, Path actual) {
    return List.of(new FileEntryDifference(0, "expected", "actual"));
  }
}

class AlwaysNoDiffComparator implements FileComparator {
  @Override
  public boolean canApply(Path file) {
    return true;
  }

  @Override
  public List<FileEntryDifference> compare(Path expected, Path actual) {
    return List.of();
  }
}
