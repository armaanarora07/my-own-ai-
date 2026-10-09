package com.armaan.vectordb.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.Map;

/**
 * Request payload for creating or inserting a vector record.
 */
public record AddVectorRequest(
        @NotBlank(message = "Vector ID is required and cannot be blank")
        String id,

        @NotEmpty(message = "Vector values array cannot be empty")
        float[] values,

        String category,

        String metadata,

        Map<String, Object> attributes
) {}
