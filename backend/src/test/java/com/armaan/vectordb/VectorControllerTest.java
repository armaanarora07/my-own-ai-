package com.armaan.vectordb;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class VectorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/vectors/stats returns 200 and valid database stats")
    void testGetStats() throws Exception {
        mockMvc.perform(get("/api/vectors/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dimensions", is(16)))
                .andExpect(jsonPath("$.totalVectors", greaterThanOrEqualTo(20)))
                .andExpect(jsonPath("$.searchAlgorithm", is("bruteforce")))
                .andExpect(jsonPath("$.supportedMetrics", hasSize(3)));
    }

    @Test
    @DisplayName("GET /api/vectors returns seeded vectors")
    void testGetAllVectors() throws Exception {
        mockMvc.perform(get("/api/vectors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(20))));
    }

    @Test
    @DisplayName("POST /api/vectors creates a new 16D vector and DELETE removes it")
    void testAddAndRemoveVector() throws Exception {
        float[] valid16D = new float[16];
        valid16D[0] = 0.95f;

        Map<String, Object> newVec = Map.of(
                "id", "custom-vec-1",
                "values", valid16D,
                "category", "test",
                "metadata", "integration test custom vector"
        );

        // 1. Insert vector -> HTTP 201 Created
        mockMvc.perform(post("/api/vectors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newVec)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is("custom-vec-1")))
                .andExpect(jsonPath("$.category", is("test")));

        // 2. Fetch by ID -> HTTP 200 OK
        mockMvc.perform(get("/api/vectors/custom-vec-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is("custom-vec-1")));

        // 3. Delete vector -> HTTP 200 OK
        mockMvc.perform(delete("/api/vectors/custom-vec-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success", is(true)));

        // 4. Fetch again -> HTTP 404 Not Found
        mockMvc.perform(get("/api/vectors/custom-vec-1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error", is("Vector Not Found")));
    }

    @Test
    @DisplayName("POST /api/vectors rejects vector with incorrect dimensions")
    void testDimensionMismatchValidation() throws Exception {
        // Only 3 dimensions instead of 16
        Map<String, Object> badVec = Map.of(
                "id", "bad-dim-vec",
                "values", new float[]{1.0f, 2.0f, 3.0f},
                "category", "test"
        );

        mockMvc.perform(post("/api/vectors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badVec)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Dimension Mismatch")))
                .andExpect(jsonPath("$.message", containsString("expected 16 dimensions, but received 3")));
    }

    @Test
    @DisplayName("POST /api/vectors rejects blank ID")
    void testBlankIdValidation() throws Exception {
        Map<String, Object> badVec = Map.of(
                "id", "   ",
                "values", new float[16]
        );

        mockMvc.perform(post("/api/vectors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(badVec)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")));
    }

    @Test
    @DisplayName("POST /api/vectors/search executes similarity search and returns ranked hits")
    void testSearchEndpoint() throws Exception {
        // Query matching Computer Science profile (high in dims 0-3)
        float[] csQuery = new float[16];
        csQuery[0] = 0.9f;
        csQuery[1] = 0.8f;
        csQuery[2] = 0.7f;
        csQuery[3] = 0.6f;

        Map<String, Object> searchPayload = Map.of(
                "query", csQuery,
                "k", 3,
                "metric", "cosine"
        );

        mockMvc.perform(post("/api/vectors/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(searchPayload)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.algorithm", is("bruteforce")))
                .andExpect(jsonPath("$.metric", is("cosine")))
                .andExpect(jsonPath("$.results", hasSize(3)))
                .andExpect(jsonPath("$.results[0].rank", is(1)))
                .andExpect(jsonPath("$.results[0].category", is("cs"))); // Top result should be in "cs" cluster!
    }

    @Test
    @DisplayName("POST /api/vectors/search rejects invalid K (k=0)")
    void testSearchInvalidK() throws Exception {
        Map<String, Object> invalidSearch = Map.of(
                "query", new float[16],
                "k", 0,
                "metric", "cosine"
        );

        mockMvc.perform(post("/api/vectors/search")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidSearch)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", is("Validation Error")));
    }
}
