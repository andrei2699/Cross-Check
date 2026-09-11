package org.crosscheck.assertions;

import java.nio.file.Path;
import java.util.List;
import org.crosscheck.comparators.FileComparator;
import org.crosscheck.comparators.FileEntryDifference;
import org.crosscheck.comparators.FolderComparator;
import org.crosscheck.comparators.FolderEntryDifference;

public final class CrossCheckFolderAssertions {
  private List<FileComparator> comparators = List.of();

  public CrossCheckFolderAssertions withComparators(List<FileComparator> comparators) {
    this.comparators = comparators;
    return this;
  }

  public void assertFolder(Path expected, Path actual) {
    FolderComparator folderComparator = new FolderComparator(comparators);

    List<FolderEntryDifference> differences = folderComparator.compare(expected, actual);

    if (differences.isEmpty()) {
      return;
    }

    String errorMessage = computeMessage(differences);
    System.err.println(errorMessage);
    throw new CrossCheckException(errorMessage);
  }

  private String computeMessage(List<FolderEntryDifference> differences) {
    StringBuilder stringBuilder = new StringBuilder();

    for (FolderEntryDifference difference : differences) {

      computeMessage(stringBuilder, difference);
      stringBuilder.append(System.lineSeparator());
    }

    return stringBuilder.toString();
  }

  private void computeMessage(StringBuilder stringBuilder, FolderEntryDifference difference) {
    switch (difference) {
      case FolderEntryDifference.DifferentChild differentChild ->
          computeMessage(stringBuilder, differentChild);
      case FolderEntryDifference.DifferentChildCount differentChildCount ->
          computeMessage(stringBuilder, differentChildCount);
      case FolderEntryDifference.DifferentChildType differentChildType ->
          computeMessage(stringBuilder, differentChildType);
      case FolderEntryDifference.FileCompareDiff fileCompareDiff ->
          computeMessage(stringBuilder, fileCompareDiff);
    }
  }

  private void computeMessage(
      StringBuilder stringBuilder, FolderEntryDifference.DifferentChild differentChild) {
    stringBuilder.append(
        "Different paths. Expected: %s, but found %s"
            .formatted(differentChild.expectedPath(), differentChild.actualPath()));
  }

  private void computeMessage(
      StringBuilder stringBuilder, FolderEntryDifference.DifferentChildCount differentChildCount) {
    stringBuilder.append(
        "Different folder child count. Expected: %d (%s), but found %d (%s)"
            .formatted(
                differentChildCount.expectedChildren(),
                differentChildCount.expectedPath(),
                differentChildCount.actualChildren(),
                differentChildCount.actualPath()));
  }

  private void computeMessage(
      StringBuilder stringBuilder, FolderEntryDifference.DifferentChildType differentChildType) {
    if (differentChildType.isExpectedFolder()) {
      stringBuilder.append(
          "Different file type. Expected: %s to be a folder, but found %s was a file"
              .formatted(differentChildType.expectedPath(), differentChildType.actualPath()));
    } else {
      stringBuilder.append(
          "Different file type. Expected: %s to be a file, but found %s was a folder"
              .formatted(differentChildType.expectedPath(), differentChildType.actualPath()));
    }
  }

  private void computeMessage(
      StringBuilder stringBuilder, FolderEntryDifference.FileCompareDiff fileCompareDiff) {
    stringBuilder.append(
        "Different file content for %s. Found the following differences:"
            .formatted(fileCompareDiff.expectedPath()));
    stringBuilder.append(System.lineSeparator());

    for (FileEntryDifference difference : fileCompareDiff.differences()) {
      stringBuilder.append(
          "Expected: %s, Actual: %s at line %d"
              .formatted(
                  difference.expectedValue(), difference.actualValue(), difference.lineNumber()));

      stringBuilder.append(System.lineSeparator());
    }
  }
}
