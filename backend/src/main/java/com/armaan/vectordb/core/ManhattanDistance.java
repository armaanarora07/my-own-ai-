package com.armaan.vectordb.core;

/**
 * Calculates Manhattan distance (L1 norm / taxicab metric) between two vectors:
 * <pre>
 *   d(a, b) = sum( |a[i] - b[i]| )
 * </pre>
 *
 * <p>Properties:
 * <ul>
 *   <li>Range: [0, +infinity)</li>
 *   <li>Sum of absolute differences along each coordinate dimension</li>
 *   <li>Identical vectors yield distance 0.0</li>
 *   <li>Lower is better (distance metric)</li>
 * </ul>
 */
public final class ManhattanDistance implements DistanceMetric {

    public static final ManhattanDistance INSTANCE = new ManhattanDistance();

    @Override
    public String getName() {
        return "manhattan";
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
            sum += Math.abs((double) a[i] - b[i]);
        }
        return sum;
    }

    @Override
    public boolean isHigherBetter() {
        return false;
    }
}
