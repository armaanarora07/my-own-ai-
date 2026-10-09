package com.armaan.vectordb.core;

/**
 * Thrown when an operation targets a vector ID that does not exist in the database.
 */
public class VectorNotFoundException extends RuntimeException {

    private final String vectorId;

    public VectorNotFoundException(String vectorId) {
        super("Vector not found with ID: " + vectorId);
        this.vectorId = vectorId;
    }

    public String getVectorId() {
        return vectorId;
    }
}
