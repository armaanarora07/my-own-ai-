package com.armaan.vectordb.search;

import com.armaan.vectordb.core.DistanceMetric;
import com.armaan.vectordb.model.VectorRecord;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Exact Brute-Force k-Nearest Neighbors (k-NN) search implementation.
 *
 * <h3>Algorithmic Strategy:</h3>
 * <p>Examines every stored vector in the corpus sequentially, evaluates its distance/similarity
 * to the query vector, and maintains the current top-K best candidates using a <b>bounded PriorityQueue (heap)</b>.
 *
 * <h3>Why Bounded PriorityQueue?</h3>
 * <ul>
 *   <li>The queue is configured with {@link DistanceMetric#worstAtHeadComparator()}, placing
 *       the <b>worst</b> current candidate at the root (head) of the heap.</li>
 *   <li>For distance metrics (lower is better), this acts as a <b>Max-Heap</b> where the maximum
 *       distance is at the top.</li>
 *   <li>For similarity metrics (higher is better), this acts as a <b>Min-Heap</b> where the minimum
 *       similarity is at the top.</li>
 *   <li>When a new candidate is evaluated:
 *     <ul>
 *       <li>If the queue has fewer than {@code k} items, we simply offer the candidate.</li>
 *       <li>If the queue is full and the candidate is strictly better than the head
 *           ({@link DistanceMetric#isBetter(double, double)}), we poll (evict) the worst candidate
 *           and offer the new candidate.</li>
 *     </ul>
 *   </li>
 * </ul>
 *
 * <h3>Complexity:</h3>
 * <ul>
 *   <li><b>Time Complexity:</b> {@code O(N * (D + log K))}, where:
 *     <ul>
 *       <li>{@code N} is the total number of vectors in the corpus.</li>
 *       <li>{@code D} is the vector dimensionality.</li>
 *       <li>{@code log K} is the heap rebalancing cost.</li>
 *     </ul>
 *     When {@code K << N}, this is significantly more efficient than a full sort {@code O(N * (D + log N))}.
 *   </li>
 *   <li><b>Space Complexity:</b> {@code O(K)} auxiliary memory for the bounded heap,
 *       compared to {@code O(N)} required by a full sort.</li>
 * </ul>
 */
public final class BruteForceSearch implements SearchAlgorithm {

    public static final BruteForceSearch INSTANCE = new BruteForceSearch();

    private record ScoredCandidate(VectorRecord vector, double score) {}

    @Override
    public String getName() {
        return "bruteforce";
    }

    @Override
    public List<SearchResult> search(float[] queryVector, int k, DistanceMetric metric, Collection<VectorRecord> corpus) {
        if (queryVector == null || queryVector.length == 0) {
            throw new IllegalArgumentException("Query vector cannot be null or empty");
        }
        if (k <= 0) {
            throw new IllegalArgumentException("Parameter k must be greater than zero, received: " + k);
        }
        if (metric == null) {
            throw new IllegalArgumentException("Distance metric cannot be null");
        }
        if (corpus == null || corpus.isEmpty()) {
            return Collections.emptyList();
        }

        int targetDimensions = queryVector.length;
        int boundedK = Math.min(k, corpus.size());

        // Bounded heap holding top-K candidates:
        // Configured so the WORST candidate in the current top-K is at the head (peek).
        PriorityQueue<ScoredCandidate> heap = new PriorityQueue<>(
                boundedK,
                (c1, c2) -> metric.worstAtHeadComparator().compare(c1.score(), c2.score())
        );

        for (VectorRecord record : corpus) {
            if (record.getDimensions() != targetDimensions) {
                throw new IllegalArgumentException(String.format(
                        "Dimension mismatch for vector id '%s': expected %d dimensions, but vector has %d",
                        record.getId(), targetDimensions, record.getDimensions()));
            }

            double score = metric.calculate(queryVector, record.getValues());

            if (heap.size() < boundedK) {
                heap.offer(new ScoredCandidate(record, score));
            } else {
                ScoredCandidate worstCandidate = heap.peek();
                if (worstCandidate != null && metric.isBetter(score, worstCandidate.score())) {
                    heap.poll(); // Evict the worst
                    heap.offer(new ScoredCandidate(record, score));
                }
            }
        }

        // Drain the heap and sort results from best to worst
        List<ScoredCandidate> extracted = new ArrayList<>(heap);
        extracted.sort((c1, c2) -> metric.bestFirstComparator().compare(c1.score(), c2.score()));

        // Assign 1-based ranks
        List<SearchResult> results = new ArrayList<>(extracted.size());
        for (int i = 0; i < extracted.size(); i++) {
            ScoredCandidate candidate = extracted.get(i);
            results.add(new SearchResult(candidate.vector(), candidate.score(), i + 1));
        }

        return results;
    }
}
