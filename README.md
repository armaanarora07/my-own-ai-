# VectorDB Explorer — Java Vector Search Platform

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.3.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Build & Tests](https://img.shields.io/badge/Tests-19%20Passing-success.svg)](#automated-testing)
[![Phase](https://img.shields.io/badge/Status-Phase%201%20Complete-blue.svg)](#project-status)

**VectorDB Explorer** is an in-memory vector database and similarity search platform built in Java. It provides a modular vector search core, exact nearest-neighbor search, a Spring Boot REST API, and an interactive glassmorphic web dashboard for executing vector queries, exploring distance metrics, and visualizing ranked results.

---

## Table of Contents

- [Project Overview](#project-overview)
- [Current Features — Phase 1](#current-features--phase-1)
- [Architecture & Request Flow](#architecture--request-flow)
- [Technology Stack](#technology-stack)
- [Getting Started](#getting-started)
- [API Reference](#api-reference)
- [Seeded Dataset](#seeded-dataset)
- [Automated Testing](#automated-testing)
- [Current Limitations](#current-limitations)
- [Roadmap — Phase 2](#roadmap--phase-2)
- [Project Status](#project-status)

---

## Project Overview

Modern AI and machine learning systems rely on high-dimensional vector representations (embeddings) to model text, images, and semantic relationships. VectorDB Explorer is designed as a modular vector search platform implemented in idiomatic Java.

In this **Phase 1** release, the system implements:
1. A **pure, framework-independent vector engine** providing in-memory vector indexing and CRUD operations.
2. A **strategy-based metric layer** supporting Cosine Similarity, Euclidean Distance, and Manhattan Distance with unified ranking semantics.
3. An **exact top-K nearest-neighbor search** engine using a bounded `PriorityQueue` (heap).
4. A **production-oriented Spring Boot REST API** with Jakarta validation and centralized error handling.
5. A **responsive, dark-themed HTML/CSS/JavaScript dashboard** featuring 1-click cluster query presets, vector creation, real-time latency profiling, and corpus management.

---

## Current Features — Phase 1

All features listed below are implemented and verified in the current codebase:

- **Idiomatic Java 21 & Modular Architecture**: Clean separation between the pure domain engine (`core`, `model`, `search`) and the web layer (`api`, `service`, `config`). The vector core has zero dependencies on Spring Boot.
- **Thread-Safe In-Memory Vector Store**: Backed by `ConcurrentHashMap` for lock-free reads and partitioned concurrent writes, enforcing uniform vector dimensionality across all records.
- **Defensive Immutability**: Primitive `float[]` arrays are defensively cloned during construction and retrieval, eliminating alien mutation while maintaining CPU cache line efficiency.
- **Distance & Similarity Metrics (Strategy Pattern)**:
  - **Cosine Similarity**: Angular comparison ($\frac{u \cdot v}{\|u\| \|v\|}$), range $[-1.0, 1.0]$. Higher values rank first.
  - **Euclidean Distance ($L_2$ Norm)**: Geometric straight-line distance, range $[0.0, +\infty)$. Lower values rank first.
  - **Manhattan Distance ($L_1$ Norm)**: Taxicab grid distance, range $[0.0, +\infty)$. Lower values rank first.
- **PriorityQueue-Based Top-K Brute Force Search**:
  - Employs a bounded heap of size $K$ using `worstAtHeadComparator()`.
  - Distances use a **Max-Heap** (worst/largest distance at head for immediate eviction).
  - Similarities use a **Min-Heap** (worst/smallest similarity at head for immediate eviction).
  - Achieves $O(N \cdot (D + \log K))$ time complexity and $O(K)$ auxiliary space.
- **Comprehensive REST API**:
  - Vector CRUD operations (add, retrieve by ID, delete by ID, list all).
  - Top-K similarity search with custom query vectors and metric selection.
  - Database metadata and runtime statistics reporting.
- **Input Validation & Structured Error Responses**:
  - Jakarta Bean Validation (`@Valid`, `@NotBlank`, `@NotEmpty`, `@Min`, `@Max`).
  - `@RestControllerAdvice` mapping domain exceptions (`DimensionMismatchException`, `VectorNotFoundException`) to standard HTTP status codes (`400 Bad Request`, `404 Not Found`).
- **Interactive Web Dashboard**:
  - Built with Vanilla HTML5, CSS3, and JavaScript (no heavy node build steps required).
  - 1-click semantic cluster presets (Computer Science, Mathematics, Food, Sports).
  - Dynamic dimension validation counter and top-K slider.
  - Microsecond-precision search latency display and visual score meter bars.
- **Deterministic Seed Data**:
  - Automatically loads 20 clustered 16-dimensional vectors on application startup.
- **Comprehensive Automated Test Suite**:
  - 19 automated unit and integration tests verifying metric mathematics, bounded heap ranking, CRUD operations, and HTTP endpoints.

---

## Architecture & Request Flow

The project follows a strict layered architecture:

```
[ Web Browser / API Client ]
             │
             │ HTTP / JSON (fetch)
             ▼
[ REST Controller: VectorController ]
  • Validates input payloads (@Valid)
  • Returns standard HTTP status codes (200, 201, 400, 404)
             │
             │ DTOs (SearchRequest, AddVectorRequest)
             ▼
[ Service Layer: VectorService ]
  • Resolves metric strategies from user inputs
  • Profiles search latency using System.nanoTime()
             │
             │ Domain Entities (VectorRecord, MetricType)
             ▼
[ Database Core: VectorDB (Pure Java) ]
  • Enforces dimensionality (16D)
  • Thread-safe storage via ConcurrentHashMap
             │
             │ Delegates query, k, metric, corpus
             ▼
[ Search Algorithm: SearchAlgorithm -> BruteForceSearch ]
  • Bounded PriorityQueue (Heap) of capacity K
  • Evaluates candidates sequentially
             │
             │ calculate(a, b), isBetter(), worstAtHeadComparator()
             ▼
[ Metric Layer: DistanceMetric ]
  ├── CosineSimilarity (Higher is better)
  ├── EuclideanDistance (Lower is better)
  └── ManhattanDistance (Lower is better)
```

### Mermaid Architecture Diagram

```mermaid
flowchart TD
    subgraph Frontend["Frontend Layer (Vanilla HTML5 / CSS3 / ES6 JS)"]
        UI["Dashboard (index.html)"]
        JS["Client Logic (app.js)"]
        UI <--> JS
    end

    subgraph Backend["Spring Boot Web & Application Layer"]
        VC["VectorController\n(@RestController)"]
        GEH["GlobalExceptionHandler\n(@RestControllerAdvice)"]
        VS["VectorService\n(@Service)"]
        DTO["DTO Records\n(SearchRequest, AddVectorRequest, etc.)"]
        VC --> VS
        VC -.-> GEH
        VC <--> DTO
    end

    subgraph CoreEngine["Pure Java Vector Core (No Framework Dependencies)"]
        VDB["VectorDB\n(ConcurrentHashMap Storage)"]
        VR["VectorRecord\n(Immutable float[] array)"]
        SA["<<interface>>\nSearchAlgorithm"]
        BFS["BruteForceSearch\n(Bounded PriorityQueue Heap)"]
        DM["<<interface>>\nDistanceMetric"]
        COS["CosineSimilarity"]
        EUC["EuclideanDistance"]
        MAN["ManhattanDistance"]

        VS --> VDB
        VDB --> VR
        VDB --> SA
        SA <|.. BFS
        BFS --> DM
        DM <|.. COS
        DM <|.. EUC
        DM <|.. MAN
    end

    JS -- "HTTP / REST (JSON)" --> VC
```

### Package Responsibilities

| Package | Responsibility |
|---|---|
| `com.armaan.vectordb.model` | Domain representation (`VectorRecord`) with defensive cloning of numerical arrays. |
| `com.armaan.vectordb.core` | Core database engine (`VectorDB`), strategy interface (`DistanceMetric`), metric implementations, and domain exceptions. |
| `com.armaan.vectordb.search` | Search abstraction (`SearchAlgorithm`), ranked hit record (`SearchResult`), and exact heap-based `BruteForceSearch`. |
| `com.armaan.vectordb.service` | Business logic, DTO mapping, and microsecond latency profiling (`VectorService`). |
| `com.armaan.vectordb.api` | REST Controller (`VectorController`), global exception mapping (`GlobalExceptionHandler`), and request/response DTOs. |
| `com.armaan.vectordb.config` | Spring configuration (`VectorDBConfig`), CORS / static resource mappings (`WebConfig`), and startup demo seeder (`DataLoader`). |

---

## Technology Stack

The Phase 1 implementation uses only the following confirmed technologies:

- **Java 21**: Modern LTS release leveraging records, pattern matching, and enhanced stream APIs.
- **Spring Boot 3.3.4**:
  - `spring-boot-starter-web`: RESTful endpoints, embedded Tomcat web server.
  - `spring-boot-starter-validation`: Jakarta Bean Validation specification.
  - `spring-boot-starter-test`: Testing framework.
- **Apache Maven 3.9.9**: Build automation and dependency management (included Maven Wrapper `mvnw.cmd` / `mvnw`).
- **HTML5 & Vanilla CSS3**: Glassmorphic dark design system with custom CSS variables, flexbox, and CSS grid.
- **Vanilla JavaScript (ES6+)**: Native asynchronous `fetch()` API, DOM rendering, and client-side input parsing.
- **Testing Frameworks**:
  - JUnit 5 (Jupiter)
  - AssertJ
  - Spring Boot Test & MockMvc

*(Note: Advanced indexing structures such as KD-Tree and HNSW, local LLM integrations like Ollama, and RAG pipelines are future roadmap items and are not included in the current Phase 1 codebase.)*

---

## Getting Started

### Prerequisites

- **Java Development Kit (JDK) 21 or later** installed.
  Verify with Windows PowerShell:
  ```powershell
  java -version
  ```
- No global Maven installation is required; the repository includes the Maven Wrapper.

### 1. Run Automated Tests

Execute the unit and integration test suite:

```powershell
cd backend
.\mvnw.cmd test
```

### 2. Start the Application

Launch the Spring Boot backend server:

```powershell
cd backend
.\mvnw.cmd spring-boot:run
```

By default, the server starts on port `8080` with Tomcat and seeds the 20 demo vectors.

### 3. Open the Web Dashboard

Open your web browser and navigate to:

```
http://localhost:8080/
```

*(Alternatively, open `frontend/index.html` directly in your browser. The client will automatically communicate with `http://localhost:8080/api/vectors` via CORS).*

### 4. Build the Standalone JAR

To compile and package the application into an executable JAR:

```powershell
cd backend
.\mvnw.cmd clean package
```

The resulting artifact will be located at:
```
backend/target/vectordb-1.0.0-SNAPSHOT.jar
```

Run the packaged JAR directly:
```powershell
java -jar backend/target/vectordb-1.0.0-SNAPSHOT.jar
```

---

## API Reference

All endpoints communicate using JSON payloads under the base path `/api/vectors`.

### 1. Get Database Statistics
Returns database configuration, current vector count, active algorithm, and supported metrics.

- **URL:** `GET /api/vectors/stats`
- **Response:** `200 OK`
```json
{
  "totalVectors": 20,
  "dimensions": 16,
  "searchAlgorithm": "bruteforce",
  "supportedMetrics": [
    "cosine",
    "euclidean",
    "manhattan"
  ]
}
```

---

### 2. Execute Similarity Search
Searches the corpus for top-K nearest neighbors matching the query vector.

- **URL:** `POST /api/vectors/search`
- **Headers:** `Content-Type: application/json`
- **Request Body:**
```json
{
  "query": [0.90, 0.85, 0.80, 0.75, 0.12, 0.08, 0.10, 0.12, 0.05, 0.08, 0.06, 0.07, 0.08, 0.09, 0.07, 0.08],
  "k": 3,
  "metric": "cosine"
}
```
- **Response:** `200 OK`
```json
{
  "results": [
    {
      "rank": 1,
      "id": "cs-2",
      "score": 0.999181,
      "category": "cs",
      "metadata": "Binary Search Tree: hierarchical node structure with O(log n) search and insert",
      "values": [0.88, 0.82, 0.78, 0.74, 0.15, 0.1, 0.08, 0.12, 0.06, 0.07, 0.08, 0.05, 0.09, 0.06, 0.07, 0.1],
      "attributes": {}
    },
    {
      "rank": 2,
      "id": "cs-1",
      "score": 0.997870,
      "category": "cs",
      "metadata": "Linked List: linear collection of data elements where order is given by pointers",
      "values": [0.9, 0.85, 0.72, 0.68, 0.12, 0.08, 0.15, 0.1, 0.05, 0.08, 0.06, 0.09, 0.07, 0.11, 0.08, 0.06],
      "attributes": {}
    }
  ],
  "latencyMicros": 320,
  "latencyMs": 0.320,
  "metric": "cosine",
  "algorithm": "bruteforce",
  "totalCorpusSize": 20
}
```

---

### 3. List All Stored Vectors
Retrieves all stored vectors sorted by ID.

- **URL:** `GET /api/vectors`
- **Response:** `200 OK`
```json
[
  {
    "id": "cs-1",
    "values": [0.90, 0.85, 0.72, 0.68, 0.12, 0.08, 0.15, 0.10, 0.05, 0.08, 0.06, 0.09, 0.07, 0.11, 0.08, 0.06],
    "category": "cs",
    "metadata": "Linked List: linear collection of data elements where order is given by pointers",
    "dimensions": 16,
    "attributes": {}
  }
]
```

---

### 4. Get Vector by ID
Retrieves a specific vector by its unique identifier.

- **URL:** `GET /api/vectors/{id}`
- **Response:** `200 OK` or `404 Not Found`

---

### 5. Add / Upsert Vector
Inserts a new vector into the database.

- **URL:** `POST /api/vectors`
- **Headers:** `Content-Type: application/json`
- **Request Body:**
```json
{
  "id": "custom-item-1",
  "values": [0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 1.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6],
  "category": "demo",
  "metadata": "Custom demonstration vector"
}
```
- **Response:** `201 Created`

---

### 6. Delete Vector
Removes a vector by ID.

- **URL:** `DELETE /api/vectors/{id}`
- **Response:** `200 OK`
```json
{
  "success": true,
  "message": "Vector 'custom-item-1' deleted successfully"
}
```

---

### Error Responses
All validation errors and domain exceptions return a consistent structure:

```json
{
  "status": 400,
  "error": "Dimension Mismatch",
  "message": "Dimension mismatch: expected 16 dimensions, but received 3",
  "details": [],
  "path": "/api/vectors",
  "timestamp": "2026-10-09T17:34:00Z"
}
```

---

## Seeded Dataset

The application initializes with 20 clustered 16-dimensional vectors designed for deterministic demonstration:

| Cluster | Dimensions Active | Vectors | Description |
|---|---|---|---|
| **Computer Science** | Dims 0–3 | `cs-1` to `cs-5` | Data structures and algorithms (Linked List, BST, DP, BFS/DFS, Hash Table) |
| **Mathematics** | Dims 4–7 | `math-1` to `math-5` | Mathematical topics (Calculus, Linear Algebra, Probability, Number Theory, Combinatorics) |
| **Food & Cuisine** | Dims 8–11 | `food-1` to `food-5` | Culinary items (Pizza, Sushi, Ramen, Tacos, Croissant) |
| **Sports** | Dims 12–15 | `sports-1` to `sports-5` | Athletic disciplines (Basketball, Soccer, Tennis, Chess, Swimming) |

Clicking any of the cluster presets in the web dashboard automatically inputs a query vector targeted at that specific subspace.

---

## Automated Testing

The project includes 19 automated tests across four test classes:

1. **`DistanceMetricTest` (4 tests)**: Verifies numerical correctness for Euclidean distance ($L_2$), Manhattan distance ($L_1$), Cosine similarity (including orthogonal, opposite, and zero-vector edge cases), and case-insensitive metric type resolution.
2. **`BruteForceSearchTest` (5 tests)**: Verifies top-K ranking with ascending distance and descending similarity, handles $K > N$ gracefully, rejects invalid $K \le 0$, and validates empty corpus behavior.
3. **`VectorDBTest` (3 tests)**: Verifies thread-safe CRUD operations, dimensional integrity enforcement (`DimensionMismatchException`), and search delegation.
4. **`VectorControllerTest` (7 tests)**: Full Spring Boot integration tests verifying `/api/vectors/stats`, vector retrieval, insertion (`201 Created`), deletion (`200 OK` / `404 Not Found`), dimension mismatch rejection (`400 Bad Request`), blank ID validation, and similarity search.

Run the tests via Maven:
```powershell
cd backend
.\mvnw.cmd test
```

---

## Current Limitations

- **Volatile In-Memory Storage**: Vectors reside strictly in JVM heap memory. All additions and modifications are lost when the application restarts.
- **Linear Scan Latency ($O(N)$)**: Search evaluates every candidate vector in the corpus sequentially. While optimal for datasets with thousands of vectors, latency increases linearly as corpus size expands to millions of vectors.
- **Manual Vector Inputs**: The platform operates on numerical vectors (`float[]`). It does not currently contain local neural embedding models to convert arbitrary raw text into embeddings on the fly.
- **Index-Free Execution**: The current version does not yet incorporate tree-based partitioning (KD-Tree) or graph-based approximate nearest neighbor indexing (HNSW).

---

## Roadmap — Phase 2

The following capabilities are planned for upcoming iterations:

### Phase 2A — Advanced Indexing & Search
- *KD-Tree Search*: Exact k-NN spatial partitioning for low-to-medium dimensional spaces.
- *HNSW (Hierarchical Navigable Small World)*: Multi-layer graph index for sub-millisecond approximate nearest neighbor (ANN) search on large datasets.
- *Dynamic Algorithm Selection*: Ability to toggle between Brute Force, KD-Tree, and HNSW via API parameters.

### Phase 2B — Benchmarking & Metrics
- *Latency Profiling*: Automated comparison of query execution time across search algorithms.
- *Recall@K Evaluation*: Quantitative evaluation of approximate search quality (HNSW) against exact Brute Force ground truth.
- *Visual Performance Dashboards*: Interactive response-time distribution and throughput charts.

### Phase 2C — Semantic Search & Local RAG
- *Ollama Integration*: Connection to local Ollama instances running open embedding models (e.g., `nomic-embed-text`) and LLMs (e.g., `llama3.2`).
- *Document Ingestion & Chunking*: Parsing raw text files into discrete chunks with automatic vector embedding generation.
- *Retrieval-Augmented Generation (RAG)*: Vector-based semantic document retrieval combined with prompt synthesis for local LLM question answering.

### Phase 2D — Reliability & Operational Enhancements
- *Persistence Engine*: Write-Ahead Log (WAL) or snapshot serialization to disk.
- *Batch Operations*: Bulk vector insertion and batch search endpoints.
- *Configurable Namespaces*: Multi-tenant vector space isolation.

---

## Project Status

- **Phase 1 Status:** **Complete & Verified**
- **Automated Test Results:** **19 Tests Run, 0 Failures, 0 Errors, 0 Skipped**
- **Current Release:** `1.0.0-SNAPSHOT`
