package com.armaan.vectordb.core;

/**
 * Thrown when an input vector does not match the database's configured dimensionality.
 */
public class DimensionMismatchException extends RuntimeException {

    private final int expected;
    private final int actual;

    public DimensionMismatchException(int expected, int actual) {
        super(String.format("Dimension mismatch: expected %d dimensions, but received %d", expected, actual));
        this.expected = expected;
        this.actual = actual;
    }

    public DimensionMismatchException(String message) {
        super(message);
        this.expected = -1;
        this.actual = -1;
    }

    public int getExpected() {
        return expected;
    }

    public int getActual() {
        return actual;
    }
}
