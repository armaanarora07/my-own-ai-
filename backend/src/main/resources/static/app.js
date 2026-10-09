/**
 * VectorDB Explorer — Vanilla JavaScript Frontend Client
 * Communicates with the Spring Boot VectorDB REST API
 */

// Determine base API URL (handles same-origin hosting and standalone frontend)
const API_BASE = window.location.origin.includes('8080')
    ? `${window.location.origin}/api/vectors`
    : 'http://localhost:8080/api/vectors';

// State
let currentDimensions = 16;
let cachedVectors = [];

// Cluster Presets (16-Dimensional)
const PRESETS = {
    cs: [0.90, 0.85, 0.80, 0.75, 0.12, 0.08, 0.10, 0.12, 0.05, 0.08, 0.06, 0.07, 0.08, 0.09, 0.07, 0.08],
    math: [0.12, 0.14, 0.18, 0.10, 0.91, 0.88, 0.84, 0.78, 0.08, 0.06, 0.07, 0.09, 0.08, 0.07, 0.06, 0.09],
    food: [0.08, 0.06, 0.09, 0.07, 0.08, 0.09, 0.07, 0.06, 0.90, 0.86, 0.88, 0.82, 0.08, 0.07, 0.09, 0.06],
    sports: [0.09, 0.07, 0.08, 0.10, 0.08, 0.09, 0.07, 0.08, 0.08, 0.07, 0.09, 0.08, 0.90, 0.88, 0.84, 0.80]
};

// DOM Elements
const statusDot = document.getElementById('status-dot');
const statusText = document.getElementById('status-text');

const statVectors = document.getElementById('stat-total-vectors');
const statDims = document.getElementById('stat-dimensions');
const statAlgo = document.getElementById('stat-algorithm');
const statMetrics = document.getElementById('stat-metrics');

const queryInput = document.getElementById('search-query-vector');
const queryDimCount = document.getElementById('query-dim-count');
const metricSelect = document.getElementById('search-metric');
const kSlider = document.getElementById('search-k-slider');
const kValuePill = document.getElementById('k-value-pill');
const btnSearch = document.getElementById('btn-search');

const metaLatency = document.getElementById('meta-latency');
const metaMetric = document.getElementById('meta-metric');
const metaAlgo = document.getElementById('meta-algo');
const resultsList = document.getElementById('search-results-list');

const formAddVector = document.getElementById('form-add-vector');
const addId = document.getElementById('add-id');
const addCategory = document.getElementById('add-category');
const addMetadata = document.getElementById('add-metadata');
const addValues = document.getElementById('add-values');
const btnGenRandom = document.getElementById('btn-generate-random-vector');

const corpusCount = document.getElementById('corpus-count');
const corpusFilter = document.getElementById('corpus-filter');
const corpusTableBody = document.getElementById('corpus-table-body');
const btnRefreshCorpus = document.getElementById('btn-refresh-corpus');

// =====================================================================
// Initialization
// =====================================================================
document.addEventListener('DOMContentLoaded', () => {
    setupEventListeners();
    applyPreset('cs'); // Default preset: Computer Science
    loadStats();
    loadCorpus();

    // Perform an initial search once connected
    setTimeout(() => {
        executeSearch();
    }, 400);
});

function setupEventListeners() {
    // Top-K Slider
    kSlider.addEventListener('input', (e) => {
        kValuePill.textContent = e.target.value;
    });

    // Preset buttons
    document.querySelectorAll('.btn-preset').forEach(btn => {
        btn.addEventListener('click', () => {
            const cluster = btn.dataset.cluster;
            if (cluster === 'random') {
                setQueryVector(generateRandomVector(currentDimensions));
                highlightPresetButton(btn);
            } else {
                applyPreset(cluster);
                highlightPresetButton(btn);
            }
        });
    });

    // Query dimension counter
    queryInput.addEventListener('input', updateQueryDimensionCounter);

    // Search button
    btnSearch.addEventListener('click', executeSearch);

    // Add Vector form
    formAddVector.addEventListener('submit', handleAddVector);
    btnGenRandom.addEventListener('click', () => {
        addValues.value = generateRandomVector(currentDimensions).join(', ');
    });

    // Corpus actions
    corpusFilter.addEventListener('input', renderCorpusTable);
    btnRefreshCorpus.addEventListener('click', () => {
        loadCorpus();
        loadStats();
    });
}

function highlightPresetButton(activeBtn) {
    document.querySelectorAll('.btn-preset').forEach(b => b.classList.remove('active'));
    activeBtn.classList.add('active');
}

function applyPreset(clusterName) {
    const values = PRESETS[clusterName];
    if (values) {
        setQueryVector(values);
        const btn = document.getElementById(`preset-${clusterName}`);
        if (btn) highlightPresetButton(btn);
    }
}

function setQueryVector(floats) {
    queryInput.value = floats.map(f => Number(f.toFixed(4))).join(', ');
    updateQueryDimensionCounter();
}

function updateQueryDimensionCounter() {
    const parsed = parseVectorString(queryInput.value);
    queryDimCount.textContent = `${parsed.length} / ${currentDimensions} dims`;
    if (parsed.length === currentDimensions) {
        queryDimCount.style.color = 'var(--success)';
    } else {
        queryDimCount.style.color = 'var(--danger)';
    }
}

function parseVectorString(raw) {
    if (!raw) return [];
    // Strip braces if present: [0.1, 0.2]
    const cleaned = raw.replace(/[\[\]]/g, ' ');
    return cleaned.split(/[,\s]+/)
        .map(s => s.trim())
        .filter(s => s.length > 0)
        .map(Number)
        .filter(n => !isNaN(n));
}

function generateRandomVector(dims) {
    const vec = [];
    for (let i = 0; i < dims; i++) {
        vec.push(Number((Math.random() * 0.9 + 0.05).toFixed(4)));
    }
    return vec;
}

// =====================================================================
// API Communication
// =====================================================================

async function loadStats() {
    try {
        const res = await fetch(`${API_BASE}/stats`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();

        statVectors.textContent = data.totalVectors;
        statDims.textContent = `${data.dimensions}D`;
        currentDimensions = data.dimensions;
        statAlgo.textContent = data.searchAlgorithm.toUpperCase();
        statMetrics.textContent = data.supportedMetrics.map(capitalize).join(' • ');

        setConnectionStatus(true, 'Connected to Java Backend');
    } catch (err) {
        console.error('Failed to load stats:', err);
        setConnectionStatus(false, 'Disconnected (Check Java Backend)');
    }
}

async function loadCorpus() {
    try {
        const res = await fetch(`${API_BASE}`);
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        cachedVectors = await res.json();
        corpusCount.textContent = cachedVectors.length;
        renderCorpusTable();
    } catch (err) {
        console.error('Failed to load corpus:', err);
        showToast('Failed to load vector corpus', 'error');
    }
}

async function executeSearch() {
    const query = parseVectorString(queryInput.value);
    if (query.length !== currentDimensions) {
        showToast(`Query must have exactly ${currentDimensions} dimensions (currently ${query.length})`, 'error');
        return;
    }

    const k = parseInt(kSlider.value, 10);
    const metric = metricSelect.value;

    btnSearch.disabled = true;
    btnSearch.innerHTML = '<span>Searching...</span>';

    try {
        const res = await fetch(`${API_BASE}/search`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ query, k, metric })
        });

        if (!res.ok) {
            const errData = await res.json();
            throw new Error(errData.message || `HTTP ${res.status}`);
        }

        const data = await res.json();
        renderSearchResults(data);
    } catch (err) {
        console.error('Search error:', err);
        showToast(`Search failed: ${err.message}`, 'error');
    } finally {
        btnSearch.disabled = false;
        btnSearch.innerHTML = '<span class="btn-icon">⚡</span><span>Execute Search</span>';
    }
}

async function handleAddVector(e) {
    e.preventDefault();
    const id = addId.value.trim();
    const category = addCategory.value.trim();
    const metadata = addMetadata.value.trim();
    const values = parseVectorString(addValues.value);

    if (!id) {
        showToast('Vector ID is required', 'error');
        return;
    }
    if (values.length !== currentDimensions) {
        showToast(`Vector must contain exactly ${currentDimensions} floats (received ${values.length})`, 'error');
        return;
    }

    try {
        const res = await fetch(`${API_BASE}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({ id, category, metadata, values })
        });

        if (!res.ok) {
            const errData = await res.json();
            throw new Error(errData.message || `HTTP ${res.status}`);
        }

        showToast(`Vector '${id}' added successfully!`, 'success');
        formAddVector.reset();
        loadStats();
        loadCorpus();
    } catch (err) {
        console.error('Add vector error:', err);
        showToast(`Error adding vector: ${err.message}`, 'error');
    }
}

async function deleteVector(id) {
    if (!confirm(`Are you sure you want to delete vector '${id}'?`)) {
        return;
    }

    try {
        const res = await fetch(`${API_BASE}/${encodeURIComponent(id)}`, {
            method: 'DELETE'
        });

        if (!res.ok) {
            const errData = await res.json();
            throw new Error(errData.message || `HTTP ${res.status}`);
        }

        showToast(`Vector '${id}' deleted`, 'success');
        loadStats();
        loadCorpus();
    } catch (err) {
        console.error('Delete error:', err);
        showToast(`Failed to delete vector: ${err.message}`, 'error');
    }
}

// =====================================================================
// Renderers
// =====================================================================

function renderSearchResults(data) {
    // Update metadata pills
    metaLatency.textContent = `⏱️ ${data.latencyMicros} µs (${data.latencyMs.toFixed(3)} ms)`;
    metaMetric.textContent = `Metric: ${capitalize(data.metric)}`;
    metaAlgo.textContent = `Algo: ${capitalize(data.algorithm)}`;

    if (!data.results || data.results.length === 0) {
        resultsList.innerHTML = `
            <div class="empty-state">
                <div class="empty-icon">🔍</div>
                <h3>No matches found</h3>
                <p>Try lowering K or adding vectors to the database.</p>
            </div>
        `;
        return;
    }

    const isSim = data.metric === 'cosine';

    resultsList.innerHTML = data.results.map(item => {
        const rankClass = item.rank <= 3 ? `top-${item.rank}` : '';
        const badgeClass = getCategoryBadgeClass(item.category);
        const scoreLabel = isSim ? 'Cosine Sim' : 'Distance';
        const formattedScore = item.score.toFixed(6);

        // Visual progress bar percentage
        let fillPct = 50;
        if (isSim) {
            fillPct = Math.max(0, Math.min(100, item.score * 100));
        } else {
            // Lower distance is better: inverted scale
            fillPct = Math.max(5, Math.min(100, 100 - (item.score * 20)));
        }

        const valuesPreview = item.values ? item.values.slice(0, 6).map(v => v.toFixed(2)).join(', ') + '...' : '';

        return `
            <div class="result-item">
                <div class="result-item-header">
                    <div class="result-id-group">
                        <span class="rank-badge ${rankClass}">#${item.rank}</span>
                        <span class="result-id">${escapeHtml(item.id)}</span>
                        ${item.category ? `<span class="badge ${badgeClass}">${escapeHtml(item.category)}</span>` : ''}
                    </div>
                    <div class="score-display">
                        <div class="score-value">${formattedScore}</div>
                        <div class="stat-sub">${scoreLabel}</div>
                    </div>
                </div>

                <div class="score-meter-bg">
                    <div class="score-meter-fill ${isSim ? 'sim' : 'dist'}" style="width: ${fillPct}%"></div>
                </div>

                ${item.metadata ? `<div class="result-desc">${escapeHtml(item.metadata)}</div>` : ''}

                <div class="result-values-preview">
                    <span>Embedding [16D]: [${valuesPreview}]</span>
                </div>
            </div>
        `;
    }).join('');
}

function renderCorpusTable() {
    const filter = corpusFilter.value.trim().toLowerCase();
    const filtered = cachedVectors.filter(v => {
        return v.id.toLowerCase().includes(filter) ||
               (v.category && v.category.toLowerCase().includes(filter)) ||
               (v.metadata && v.metadata.toLowerCase().includes(filter));
    });

    if (filtered.length === 0) {
        corpusTableBody.innerHTML = `
            <tr>
                <td colspan="4" class="text-center">No vectors match filter '${escapeHtml(filter)}'</td>
            </tr>
        `;
        return;
    }

    corpusTableBody.innerHTML = filtered.map(vec => {
        const badgeClass = getCategoryBadgeClass(vec.category);
        return `
            <tr>
                <td class="table-id">${escapeHtml(vec.id)}</td>
                <td><span class="badge ${badgeClass}">${escapeHtml(vec.category || 'none')}</span></td>
                <td>${escapeHtml(vec.metadata || '--')}</td>
                <td>
                    <div class="table-actions">
                        <button class="btn-action-small" onclick="useAsQuery('${vec.id}')" title="Set this vector as search query">Use as Query</button>
                        <button class="btn-action-small btn-action-danger" onclick="deleteVector('${vec.id}')" title="Delete vector">Delete</button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

function useAsQuery(id) {
    const vec = cachedVectors.find(v => v.id === id);
    if (vec && vec.values) {
        setQueryVector(vec.values);
        document.querySelectorAll('.btn-preset').forEach(b => b.classList.remove('active'));
        showToast(`Loaded '${id}' values into query input`, 'success');
        // Scroll to search card
        document.getElementById('search-card').scrollIntoView({ behavior: 'smooth' });
    }
}

// =====================================================================
// Helpers
// =====================================================================

function getCategoryBadgeClass(category) {
    if (!category) return 'badge-generic';
    const cat = category.toLowerCase();
    if (cat === 'cs') return 'badge-cs';
    if (cat === 'math') return 'badge-math';
    if (cat === 'food') return 'badge-food';
    if (cat === 'sports') return 'badge-sports';
    return 'badge-generic';
}

function setConnectionStatus(online, text) {
    statusText.textContent = text;
    statusDot.className = 'status-dot ' + (online ? 'online' : 'error');
}

function showToast(message, type = 'info') {
    const container = document.getElementById('toast-container');
    const toast = document.createElement('div');
    toast.className = `toast ${type}`;
    toast.textContent = message;
    container.appendChild(toast);

    setTimeout(() => {
        toast.style.opacity = '0';
        toast.style.transform = 'translateY(10px)';
        setTimeout(() => toast.remove(), 250);
    }, 3200);
}

function capitalize(str) {
    if (!str) return '';
    return str.charAt(0).toUpperCase() + str.slice(1);
}

function escapeHtml(text) {
    if (!text) return '';
    const map = {
        '&': '&amp;',
        '<': '&lt;',
        '>': '&gt;',
        '"': '&quot;',
        "'": '&#039;'
    };
    return text.toString().replace(/[&<>"']/g, m => map[m]);
}
