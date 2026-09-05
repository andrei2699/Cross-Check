package org.crosscheckj.comparators;

import tools.jackson.databind.MappingIterator;
import tools.jackson.dataformat.csv.CsvMapper;
import tools.jackson.dataformat.csv.CsvReadFeature;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class CsvFileComparator implements FileComparator {
    private static final CsvMapper DEFAULT_CSV_MAPPER = CsvMapper.builder()
            .enable(CsvReadFeature.WRAP_AS_ARRAY)
            .build();

    private final List<String> allowedFileExtensions;
    private final CsvMapper mapper;

    public CsvFileComparator() {
        allowedFileExtensions = List.of(".csv");
        mapper = DEFAULT_CSV_MAPPER;
    }

    public CsvFileComparator(List<String> allowedFileExtensions) {
        this.allowedFileExtensions = allowedFileExtensions;
        mapper = DEFAULT_CSV_MAPPER;
    }

    public CsvFileComparator(List<String> allowedFileExtensions, CsvMapper mapper) {
        this.allowedFileExtensions = allowedFileExtensions;
        this.mapper = mapper;
    }

    @Override
    public boolean canApply(Path file) {
        String filePath = file.toString();
        return allowedFileExtensions.stream().anyMatch(filePath::endsWith);
    }

    @Override
    public List<FileEntryDifference> compare(Path expected, Path actual) {
        List<FileEntryDifference> result = new ArrayList<>();

        File expectedFile = expected.toFile();
        File actualFile = actual.toFile();

        try (MappingIterator<String[]> expectedIterator = mapper.readerFor(String[].class).readValues(expectedFile);
             MappingIterator<String[]> actualIterator = mapper.readerFor(String[].class).readValues(actualFile)) {
            long lineCount = 0;

            while (expectedIterator.hasNext() && actualIterator.hasNext()) {
                String[] expectedRow = expectedIterator.next();
                String[] actualRow = actualIterator.next();

                result.addAll(addEntriesForLine(lineCount, expectedRow, actualRow));

                lineCount++;
            }

            result.addAll(addRemainingExpectedLines(lineCount, expectedIterator));
            result.addAll(addRemainingActualLines(lineCount, actualIterator));
        }
        return result;
    }

    private List<FileEntryDifference> addRemainingExpectedLines(long lineNumber, MappingIterator<String[]> expectedIterator) {
        List<FileEntryDifference> result = new ArrayList<>();

        while (expectedIterator.hasNext()) {
            String[] expectedRow = expectedIterator.next();

            result.addAll(addOnlyExpectedValues(lineNumber, expectedRow, 0));

            lineNumber++;
        }

        return result;
    }

    private List<FileEntryDifference> addRemainingActualLines(long lineNumber, MappingIterator<String[]> actualIterator) {
        List<FileEntryDifference> result = new ArrayList<>();

        while (actualIterator.hasNext()) {
            String[] expectedRow = actualIterator.next();

            result.addAll(addOnlyActualValues(lineNumber, expectedRow, 0));

            lineNumber++;
        }

        return result;
    }

    private List<FileEntryDifference> addEntriesForLine(long lineNumber, String[] expectedRow, String[] actualRow) {
        List<FileEntryDifference> result = new ArrayList<>();

        int currentIndex = 0;

        while (currentIndex < expectedRow.length && currentIndex < actualRow.length) {
            String expectedValue = expectedRow[currentIndex];
            String actualValue = actualRow[currentIndex];

            if (!expectedValue.equals(actualValue)) {
                result.add(new FileEntryDifference(lineNumber, expectedValue, actualValue));
            }

            currentIndex++;
        }

        result.addAll(addOnlyExpectedValues(lineNumber, expectedRow, currentIndex));
        result.addAll(addOnlyActualValues(lineNumber, actualRow, currentIndex));

        return result;
    }

    private List<FileEntryDifference> addOnlyExpectedValues(long lineNumber, String[] expectedRow, int index) {
        List<FileEntryDifference> result = new ArrayList<>();

        while (index < expectedRow.length) {
            result.add(new FileEntryDifference(lineNumber, expectedRow[index], null));

            index++;
        }

        return result;
    }

    private List<FileEntryDifference> addOnlyActualValues(long lineNumber, String[] actualRow, int index) {
        List<FileEntryDifference> result = new ArrayList<>();

        while (index < actualRow.length) {
            result.add(new FileEntryDifference(lineNumber, null, actualRow[index]));

            index++;
        }

        return result;
    }
}
