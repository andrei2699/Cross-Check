package org.crosscheckj.comparators;

import java.nio.file.Path;
import java.util.List;

public interface FileComparator {
  boolean canApply(Path file);

  List<FileEntryDifference> compare(Path expected, Path actual);
}
