package com.armaan.vectordb.config;

import com.armaan.vectordb.core.VectorDB;
import com.armaan.vectordb.model.VectorRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * Initializes the VectorDB with a deterministic 16-dimensional clustered dataset upon startup.
 *
 * <p>Embedding structure:
 * <ul>
 *   <li>Dimensions 0-3: Computer Science concepts (algorithms, data structures)</li>
 *   <li>Dimensions 4-7: Mathematics concepts (calculus, algebra, probability)</li>
 *   <li>Dimensions 8-11: Food & Cuisine items (pizza, sushi, ramen)</li>
 *   <li>Dimensions 12-15: Sports & Athletics (basketball, soccer, tennis)</li>
 * </ul>
 */
@Component
public class DataLoader implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataLoader.class);
    private final VectorDB vectorDB;

    public DataLoader(VectorDB vectorDB) {
        this.vectorDB = vectorDB;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("Seeding deterministic 16D demo vectors into VectorDB...");

        // Category 1: Computer Science (dims 0-3 active)
        insert("cs-1", "Linked List: linear collection of data elements where order is given by pointers", "cs",
                new float[]{0.90f, 0.85f, 0.72f, 0.68f, 0.12f, 0.08f, 0.15f, 0.10f, 0.05f, 0.08f, 0.06f, 0.09f, 0.07f, 0.11f, 0.08f, 0.06f});

        insert("cs-2", "Binary Search Tree: hierarchical node structure with O(log n) search and insert", "cs",
                new float[]{0.88f, 0.82f, 0.78f, 0.74f, 0.15f, 0.10f, 0.08f, 0.12f, 0.06f, 0.07f, 0.08f, 0.05f, 0.09f, 0.06f, 0.07f, 0.10f});

        insert("cs-3", "Dynamic Programming: algorithm optimization via memoization and overlapping subproblems", "cs",
                new float[]{0.82f, 0.76f, 0.88f, 0.80f, 0.20f, 0.18f, 0.12f, 0.09f, 0.07f, 0.06f, 0.08f, 0.07f, 0.08f, 0.09f, 0.06f, 0.07f});

        insert("cs-4", "Graph Traversal BFS and DFS: systematically exploring vertices and edge topologies", "cs",
                new float[]{0.85f, 0.80f, 0.75f, 0.82f, 0.18f, 0.14f, 0.10f, 0.08f, 0.06f, 0.09f, 0.07f, 0.06f, 0.10f, 0.08f, 0.09f, 0.07f});

        insert("cs-5", "Hash Table: key-value storage providing average O(1) lookups via hashing functions", "cs",
                new float[]{0.87f, 0.78f, 0.70f, 0.76f, 0.13f, 0.11f, 0.09f, 0.14f, 0.08f, 0.07f, 0.06f, 0.08f, 0.07f, 0.10f, 0.08f, 0.09f});

        // Category 2: Mathematics (dims 4-7 active)
        insert("math-1", "Calculus: continuous change, derivatives, integrals, and limits", "math",
                new float[]{0.12f, 0.15f, 0.18f, 0.10f, 0.91f, 0.86f, 0.78f, 0.72f, 0.08f, 0.06f, 0.07f, 0.09f, 0.07f, 0.08f, 0.06f, 0.10f});

        insert("math-2", "Linear Algebra: vector spaces, matrices, eigenvalues, and coordinate transformations", "math",
                new float[]{0.20f, 0.18f, 0.15f, 0.12f, 0.88f, 0.90f, 0.82f, 0.76f, 0.09f, 0.07f, 0.08f, 0.06f, 0.10f, 0.07f, 0.08f, 0.09f});

        insert("math-3", "Probability and Statistics: probability distributions, random variables, and Bayes theorem", "math",
                new float[]{0.15f, 0.12f, 0.20f, 0.18f, 0.84f, 0.80f, 0.88f, 0.82f, 0.07f, 0.08f, 0.06f, 0.10f, 0.09f, 0.06f, 0.09f, 0.08f});

        insert("math-4", "Number Theory: prime numbers, modular arithmetic, and cryptographic algorithms", "math",
                new float[]{0.22f, 0.16f, 0.14f, 0.20f, 0.80f, 0.85f, 0.76f, 0.90f, 0.08f, 0.09f, 0.07f, 0.06f, 0.08f, 0.10f, 0.07f, 0.06f});

        insert("math-5", "Combinatorics: counting, permutations, combinations, and recurrence relations", "math",
                new float[]{0.18f, 0.20f, 0.16f, 0.14f, 0.86f, 0.78f, 0.84f, 0.80f, 0.06f, 0.07f, 0.09f, 0.08f, 0.06f, 0.09f, 0.10f, 0.07f});

        // Category 3: Food & Cuisine (dims 8-11 active)
        insert("food-1", "Neapolitan Pizza: wood-fired crust with San Marzano tomatoes and mozzarella", "food",
                new float[]{0.08f, 0.06f, 0.09f, 0.07f, 0.07f, 0.08f, 0.06f, 0.09f, 0.90f, 0.86f, 0.78f, 0.72f, 0.08f, 0.06f, 0.09f, 0.07f});

        insert("food-2", "Sushi Nigiri: seasoned vinegared rice topped with fresh raw sashimi", "food",
                new float[]{0.06f, 0.08f, 0.07f, 0.09f, 0.09f, 0.06f, 0.08f, 0.07f, 0.86f, 0.90f, 0.82f, 0.76f, 0.07f, 0.09f, 0.06f, 0.08f});

        insert("food-3", "Japanese Ramen: rich savory broth with wheat noodles, chashu pork, and egg", "food",
                new float[]{0.09f, 0.07f, 0.06f, 0.08f, 0.08f, 0.09f, 0.07f, 0.06f, 0.82f, 0.78f, 0.90f, 0.84f, 0.09f, 0.07f, 0.08f, 0.06f});

        insert("food-4", "Mexican Street Tacos: warm corn tortillas packed with spiced carnitas and salsa", "food",
                new float[]{0.07f, 0.09f, 0.08f, 0.06f, 0.06f, 0.07f, 0.09f, 0.08f, 0.78f, 0.82f, 0.86f, 0.90f, 0.06f, 0.08f, 0.07f, 0.09f});

        insert("food-5", "French Croissant: flaky laminated pastry dough baked with European butter", "food",
                new float[]{0.06f, 0.07f, 0.10f, 0.09f, 0.10f, 0.06f, 0.07f, 0.10f, 0.85f, 0.80f, 0.76f, 0.82f, 0.09f, 0.07f, 0.10f, 0.06f});

        // Category 4: Sports (dims 12-15 active)
        insert("sports-1", "Basketball: fast-paced court game with dribbling, shooting, and slam dunks", "sports",
                new float[]{0.09f, 0.07f, 0.08f, 0.10f, 0.08f, 0.09f, 0.07f, 0.06f, 0.08f, 0.07f, 0.09f, 0.06f, 0.91f, 0.85f, 0.78f, 0.72f});

        insert("sports-2", "Soccer / Football: pitch sport focusing on passing, ball control, and scoring goals", "sports",
                new float[]{0.07f, 0.09f, 0.06f, 0.08f, 0.09f, 0.07f, 0.10f, 0.08f, 0.07f, 0.09f, 0.08f, 0.07f, 0.87f, 0.89f, 0.82f, 0.76f});

        insert("sports-3", "Tennis Championship: singles racket duel with serves, volleys, and baseline rallies", "sports",
                new float[]{0.08f, 0.06f, 0.09f, 0.07f, 0.07f, 0.08f, 0.06f, 0.09f, 0.09f, 0.06f, 0.07f, 0.08f, 0.83f, 0.80f, 0.88f, 0.82f});

        insert("sports-4", "Chess Grandmaster: intellectual board strategy, openings, endgames, and tactics", "sports",
                new float[]{0.25f, 0.20f, 0.22f, 0.18f, 0.22f, 0.18f, 0.20f, 0.15f, 0.06f, 0.08f, 0.07f, 0.09f, 0.80f, 0.84f, 0.78f, 0.90f});

        insert("sports-5", "Olympic Swimming: competitive aquatic racing including freestyle, backstroke, and butterfly", "sports",
                new float[]{0.06f, 0.08f, 0.07f, 0.09f, 0.08f, 0.06f, 0.09f, 0.07f, 0.10f, 0.08f, 0.06f, 0.07f, 0.85f, 0.82f, 0.86f, 0.80f});

        log.info("Successfully loaded {} demo vectors into VectorDB.", vectorDB.size());
    }

    private void insert(String id, String metadata, String category, float[] values) {
        vectorDB.insert(new VectorRecord(id, values, category, metadata));
    }
}
