package com.armaan.vectordb.core;

/**
 * Calculates Cosine Similarity between two vectors:
 * <pre>
 *   similarity(a, b) = (a . b) / ( ||a|| * ||b|| )
 * </pre>
 *
 * <p>Properties:
 * <ul>
 *   <li>Range: [-1.0, 1.0] (or [0.0, 1.0] for non-negative embeddings)</li>
 *   <li>Measures the cosine of the angle between two vectors (direction, not magnitude)</li>
 *   <li>Vectors pointing in the exact same direction have similarity 1.0</li>
 *   <li>Orthogonal vectors have similarity 0.0</li>
 *   <li>Opposite vectors have similarity -1.0</li>
 *   <li>Higher is better (similarity metric)</li>
 * </ul>
 */
public final class CosineSimilarity implements DistanceMetric {

    public static final CosineSimilarity INSTANCE = new CosineSimilarity();
    private static final double EPSILON = 1e-9;

    @Override
    public String getName() {
        return "cosine";
    }

    @Override
    public double calculate(float[] a, float[] b) {
        if (a == null || b == null) {
            throw new IllegalArgumentException("Vectors cannot be null");
        }
        if (a.length != b.length) {
            throw new IllegalArgumentException(
                    "Dimension mismatch: vector a has " + a.length + " dims, but vector b has " + b.length + " dims");
        }

        double dotProduct = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dotProduct += (double) a[i] * b[i];
            normA += (double) a[i] * a[i];
            normB += (double) b[i] * b[i];
        }

        if (normA < EPSILON || normB < EPSILON) {
            return 0.0; // Avoid division by zero when magnitude is negligible
        }

        double similarity = dotProduct / (Math.sqrt(normA) * Math.sqrt(normB));

        // Clamp floating-point rounding errors into [-1.0, 1.0]
        return Math.max(-1.0, Math.min(1.0, similarity));
    }

    @Override
    public boolean isHigherBetter() {
        return true;
    }
}
