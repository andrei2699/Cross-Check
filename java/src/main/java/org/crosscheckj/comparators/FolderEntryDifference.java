package org.crosscheckj.comparators;

import java.nio.file.Path;

public sealed interface FolderEntryDifference
    permits FolderEntryDifference.DifferentChild,
        FolderEntryDifference.DifferentChildCount,
        FolderEntryDifference.DifferentChildType {
  record DifferentChildCount(
      Path expectedPath, Path actualPath, int expectedChildren, int actualChildren)
      implements FolderEntryDifference {}

  record DifferentChild(Path expectedPath, Path actualPath) implements FolderEntryDifference {}

  record DifferentChildType(
      Path expectedPath, Path actualPath, boolean isExpectedFolder, boolean isActualFolder)
      implements FolderEntryDifference {}
}
