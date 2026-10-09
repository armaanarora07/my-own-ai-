package com.armaan.vectordb;

import com.armaan.vectordb.core.CosineSimilarity;
import com.armaan.vectordb.core.EuclideanDistance;
import com.armaan.vectordb.core.ManhattanDistance;
import com.armaan.vectordb.core.MetricType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.within;

class DistanceMetricTest {

    @Test
    @DisplayName("Euclidean distance computes correct L2 norm")
    void testEuclideanDistance() {
        EuclideanDistance metric = EuclideanDistance.INSTANCE;

        // Identical vectors -> 0 distance
        float[] v1 = {1.0f, 2.0f, 3.0f};
        float[] v2 = {1.0f, 2.0f, 3.0f};
        assertThat(metric.calculate(v1, v2)).isEqualTo(0.0);

        // 3-4-5 right triangle: (3, 4) to (0, 0) is 5.0
        float[] a = {3.0f, 4.0f};
        float[] b = {0.0f, 0.0f};
        assertThat(metric.calculate(a, b)).isCloseTo(5.0, within(1e-6));

        // Dimension mismatch throws
        assertThatThrownBy(() -> metric.calculate(new float[]{1.0f}, new float[]{1.0f, 2.0f}))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Dimension mismatch");
    }

    @Test
    @DisplayName("Manhattan distance computes correct L1 norm")
    void testManhattanDistance() {
        ManhattanDistance metric = ManhattanDistance.INSTANCE;

        // Identical vectors -> 0 distance
        float[] v1 = {1.0f, 2.0f, 3.0f};
        assertThat(metric.calculate(v1, v1)).isEqualTo(0.0);

        // |3 - 0| + |4 - 0| = 7.0
        float[] a = {3.0f, 4.0f};
        float[] b = {0.0f, 0.0f};
        assertThat(metric.calculate(a, b)).isCloseTo(7.0, within(1e-6));

        // Mixed signs: |-2 - 3| + |5 - (-1)| = 5 + 6 = 11.0
        float[] c = {-2.0f, 5.0f};
        float[] d = {3.0f, -1.0f};
        assertThat(metric.calculate(c, d)).isCloseTo(11.0, within(1e-6));
    }

    @Test
    @DisplayName("Cosine similarity computes correct angular similarity")
    void testCosineSimilarity() {
        CosineSimilarity metric = CosineSimilarity.INSTANCE;

        // Parallel vectors -> similarity 1.0
        float[] v1 = {1.0f, 2.0f, 3.0f};
        float[] v2 = {2.0f, 4.0f, 6.0f}; // same direction, double magnitude
        assertThat(metric.calculate(v1, v2)).isCloseTo(1.0, within(1e-6));

        // Orthogonal vectors -> similarity 0.0
        float[] o1 = {1.0f, 0.0f};
        float[] o2 = {0.0f, 1.0f};
        assertThat(metric.calculate(o1, o2)).isCloseTo(0.0, within(1e-6));

        // Opposite vectors -> similarity -1.0
        float[] opp1 = {1.0f, 0.0f};
        float[] opp2 = {-1.0f, 0.0f};
        assertThat(metric.calculate(opp1, opp2)).isCloseTo(-1.0, within(1e-6));

        // Zero vector safely returns 0.0 without NaN/Infinity
        float[] zero = {0.0f, 0.0f};
        assertThat(metric.calculate(zero, o1)).isEqualTo(0.0);
    }

    @Test
    @DisplayName("MetricType enum resolves strings case-insensitively")
    void testMetricTypeResolution() {
        assertThat(MetricType.fromString("cosine")).isEqualTo(MetricType.COSINE);
        assertThat(MetricType.fromString("EUCLIDEAN")).isEqualTo(MetricType.EUCLIDEAN);
        assertThat(MetricType.fromString("Manhattan")).isEqualTo(MetricType.MANHATTAN);

        assertThatThrownBy(() -> MetricType.fromString("unknown_metric"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Unknown metric");
    }
}
