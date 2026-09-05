package org.crosscheckj.comparators;

import java.nio.file.Path;
import java.util.List;

public sealed interface FolderEntryDifference
    permits FolderEntryDifference.DifferentChild,
        FolderEntryDifference.DifferentChildCount,
        FolderEntryDifference.DifferentChildType,
        FolderEntryDifference.FileCompareDiff {
  record DifferentChildCount(
      Path expectedPath, Path actualPath, int expectedChildren, int actualChildren)
      implements FolderEntryDifference {}

  record DifferentChild(Path expectedPath, Path actualPath) implements FolderEntryDifference {}

  record DifferentChildType(
      Path expectedPath, Path actualPath, boolean isExpectedFolder, boolean isActualFolder)
      implements FolderEntryDifference {}

  record FileCompareDiff(Path expectedPath, Path actualPath, List<FileEntryDifference> differences)
      implements FolderEntryDifference {}
}
