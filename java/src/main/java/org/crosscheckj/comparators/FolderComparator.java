package org.crosscheckj.comparators;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.NotDirectoryException;
import java.nio.file.Path;
import java.util.*;

public class FolderComparator {
  private final List<FileComparator> comparators;

  public FolderComparator() {
    comparators = new ArrayList<>();
  }

  public FolderComparator(List<FileComparator> comparators) {
    this.comparators = comparators;
  }

  public List<FolderEntryDifference> compare(Path expected, Path actual) {
    File expectedFolder = expected.toFile();
    File actualFolder = actual.toFile();

    ensureFolderExists(expectedFolder);
    ensureFolderExists(actualFolder);

    List<File> sortedExpectedFiles = getSortedEntries(expectedFolder);
    List<File> sortedActualFiles = getSortedEntries(actualFolder);

    if (sortedExpectedFiles.size() != sortedActualFiles.size()) {
      return List.of(
          new FolderEntryDifference.DifferentChildCount(
              expectedFolder.toPath(),
              actualFolder.toPath(),
              sortedExpectedFiles.size(),
              sortedActualFiles.size()));
    }

    return compare(sortedExpectedFiles, sortedActualFiles);
  }

  private List<File> getSortedEntries(File folder) {
    File[] files = Objects.requireNonNullElse(folder.listFiles(), new File[] {});

    return Arrays.stream(files).sorted().toList();
  }

  private List<FolderEntryDifference> compare(List<File> expectedFiles, List<File> actualFiles) {
    List<FolderEntryDifference> result = new ArrayList<>();

    int index = 0;
    while (index < expectedFiles.size() && index < actualFiles.size()) {
      File expected = expectedFiles.get(index);
      File actual = actualFiles.get(index);

      getPossibleDifferenceName(expected, actual)
          .or(() -> getPossibleDifferenceType(expected, actual))
          .ifPresent(result::add);

      if (expected.isDirectory() && actual.isDirectory()) {
        List<File> expectedSubFiles = getSortedEntries(expected);
        List<File> actualSubFiles = getSortedEntries(actual);

        result.addAll(compare(expectedSubFiles, actualSubFiles));
      }

      index++;
    }

    return result;
  }

  private void ensureFolderExists(File folder) {
    if (!folder.exists()) {
      throw new RuntimeException(new FileNotFoundException());
    }

    if (!folder.isDirectory()) {
      throw new RuntimeException(new NotDirectoryException(folder.getPath()));
    }
  }

  private Optional<FolderEntryDifference> getPossibleDifferenceType(File expected, File actual) {
    boolean expectedIsDirectory = expected.isDirectory();
    boolean actualIsDirectory = actual.isDirectory();

    if (expectedIsDirectory && actualIsDirectory) {
      return Optional.empty();
    }

    return Optional.of(
        new FolderEntryDifference.DifferentChildType(
            expected.toPath(), actual.toPath(), expectedIsDirectory, actualIsDirectory));
  }

  private Optional<FolderEntryDifference> getPossibleDifferenceName(File expected, File actual) {
    if (expected.getName().equals(actual.getName())) {
      return Optional.empty();
    }

    return Optional.of(
        new FolderEntryDifference.DifferentChild(expected.toPath(), actual.toPath()));
  }
}
