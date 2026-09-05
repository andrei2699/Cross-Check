package org.crosscheckj.comparators;

import java.nio.file.Path;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
}
