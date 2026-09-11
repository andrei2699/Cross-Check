package org.crosscheck.comparators;

public record FileEntryDifference(long lineNumber, String expectedValue, String actualValue) {}
