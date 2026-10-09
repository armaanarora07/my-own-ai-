package com.armaan.vectordb.core;

/**
 * Calculates Euclidean distance (L2 norm) between two vectors:
 * <pre>
 *   d(a, b) = sqrt( sum( (a[i] - b[i])^2 ) )
 * </pre>
 *
 * <p>Properties:
 * <ul>
 *   <li>Range: [0, +infinity)</li>
 *   <li>Identical vectors yield distance 0.0</li>
 *   <li>Lower is better (distance metric)</li>
 * </ul>
 */
public final class EuclideanDistance implements DistanceMetric {

    public static final EuclideanDistance INSTANCE = new EuclideanDistance();

    @Override
    public String getName() {
        return "euclidean";
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

        double sum = 0.0;
        for (int i = 0; i < a.length; i++) {
            double diff = a[i] - b[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
    }

    @Override
    public boolean isHigherBetter() {
        return false;
    }
}
