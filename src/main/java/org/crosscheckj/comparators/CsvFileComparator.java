package org.crosscheckj.comparators;

import java.nio.file.Path;
import java.util.List;

public final class CsvFileComparator implements FileComparator {
    private final List<String> allowedFileExtensions;

    public CsvFileComparator() {
        allowedFileExtensions = List.of(".csv");
    }

    public CsvFileComparator(List<String> allowedFileExtensions) {
        this.allowedFileExtensions = allowedFileExtensions;
    }

    @Override
    public boolean canApply(Path file) {
        String filePath = file.toString();
        return allowedFileExtensions.stream().anyMatch(filePath::endsWith);
    }

    @Override
    public List<FileEntryDifference> compare(Path source, Path destination) {
        return List.of();
    }
}
