package org.crosscheckj.comparators;

public record FileEntryDifference(long lineNumber, String expectedValue, String actualValue) {}
