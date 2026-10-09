package com.armaan.vectordb.core;

import java.util.Comparator;

/**
 * Strategy interface defining how distance or similarity is calculated between two vectors.
 *
 * <p>Vector comparisons generally fall into two categories:
 * <ul>
 *   <li><b>Distance metrics (e.g. Euclidean, Manhattan):</b> Lower values indicate closer proximity.
 *       Identical vectors have a distance of 0.0.</li>
 *   <li><b>Similarity metrics (e.g. Cosine Similarity):</b> Higher values indicate higher similarity.
 *       Identical direction vectors have a similarity of 1.0.</li>
 * </ul>
 *
 * <p>This interface encapsulates both the mathematical calculation and the ranking logic
 * (e.g. comparator, eviction predicate) so that search algorithms remain clean and metric-agnostic.
 */
public interface DistanceMetric {

    /**
     * Human-readable name of the metric.
     */
    String getName();

    /**
     * Calculates the metric score between vector {@code a} and vector {@code b}.
     *
     * @param a the query or reference vector
     * @param b the target vector
     * @return the calculated score (distance or similarity)
     * @throws IllegalArgumentException if vector lengths differ or arrays are null
     */
    double calculate(float[] a, float[] b);

    /**
     * Indicates whether higher scores represent better/closer matches.
     *
     * @return {@code true} for similarity metrics (e.g. Cosine),
     *         {@code false} for distance metrics (e.g. Euclidean, Manhattan).
     */
    boolean isHigherBetter();

    /**
     * Determines whether {@code candidateScore} is strictly better than {@code currentScore}.
     *
     * @param candidateScore the new candidate's score
     * @param currentScore   the existing score to compare against
     * @return {@code true} if candidate is superior, {@code false} otherwise
     */
    default boolean isBetter(double candidateScore, double currentScore) {
        if (isHigherBetter()) {
            return candidateScore > currentScore;
        } else {
            return candidateScore < currentScore;
        }
    }

    /**
     * Comparator that orders scores with the BEST match first.
     * Useful for final sorting of search results.
     */
    default Comparator<Double> bestFirstComparator() {
        return (s1, s2) -> isHigherBetter() ? Double.compare(s2, s1) : Double.compare(s1, s2);
    }

    /**
     * Comparator that orders scores with the WORST match at the head.
     * Useful for bounded PriorityQueue (heap) where the worst candidate in top-K must be evicted.
     */
    default Comparator<Double> worstAtHeadComparator() {
        return (s1, s2) -> isHigherBetter() ? Double.compare(s1, s2) : Double.compare(s2, s1);
    }
}
