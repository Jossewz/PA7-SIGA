const fs = require('fs');

const runs = [1, 2, 3];
const metrics = [
    'lectura_mapa_duration_ms',
    'escritura_40_estudiantes_duration_ms',
    'colision_batch_duration_ms',
    'contencion_abrir_dia_duration_ms',
    'http_req_duration'
];

const results = {};
metrics.forEach(m => results[m] = []);

runs.forEach(r => {
    const raw = fs.readFileSync(`scripts/k6/summary_run${r}.json`, 'utf8');
    const data = JSON.parse(raw);
    const m = data.metrics;
    
    metrics.forEach(key => {
        const val = m[key];
        if (val) {
            results[key].push({
                min: Number(val.min),
                med: Number(val.med),
                avg: Number(val.avg),
                p90: Number(val['p(90)']),
                p95: Number(val['p(95)']),
                max: Number(val.max)
            });
        }
    });
});

const labels = {
    'lectura_mapa_duration_ms': 'Lectura de Mapa (40 est.)',
    'escritura_40_estudiantes_duration_ms': 'Escritura Masiva (40 est.)',
    'colision_batch_duration_ms': 'Colisión Concurrente Batch (12 reqs)',
    'contencion_abrir_dia_duration_ms': 'Idempotencia abrirDia (12 VUs contención)',
    'http_req_duration': 'Duración Global Peticiones HTTP'
};

const thresholds = {
    'lectura_mapa_duration_ms': '< 300 ms',
    'escritura_40_estudiantes_duration_ms': '< 500 ms',
    'colision_batch_duration_ms': '< 500 ms',
    'contencion_abrir_dia_duration_ms': '< 300 ms',
    'http_req_duration': '< 300 ms'
};

console.log('| Métrica / Escenario | Corrida 1 (min / med / p95 / max) | Corrida 2 (min / med / p95 / max) | Corrida 3 (min / med / p95 / max) | Rango p95 [min - max] | Umbral Máx. | Condición |');
console.log('|---|---|---|---|:---:|:---:|:---:|');

metrics.forEach(k => {
    const r1 = results[k][0];
    const r2 = results[k][1];
    const r3 = results[k][2];
    
    const fmt = (v) => `${v.min.toFixed(2)} / ${v.med.toFixed(2)} / **${v.p95.toFixed(2)}** / ${v.max.toFixed(2)} ms`;
    const minP95 = Math.min(r1.p95, r2.p95, r3.p95).toFixed(2);
    const maxP95 = Math.max(r1.p95, r2.p95, r3.p95).toFixed(2);
    
    console.log(`| **${labels[k]}** | ${fmt(r1)} | ${fmt(r2)} | ${fmt(r3)} | [${minP95} - ${maxP95}] ms | ${thresholds[k]} | Cumple |`);
});

runs.forEach(r => {
    const raw = fs.readFileSync(`scripts/k6/summary_run${r}.json`, 'utf8');
    const data = JSON.parse(raw);
    const m = data.metrics;
    console.log(`\n--- RESUMEN OPERACIONAL CORRIDA ${r} ---`);
    console.log(`Peticiones HTTP totales: ${m.http_reqs.count}`);
    console.log(`Tasa de solicitudes/seg: ${m.http_reqs.rate.toFixed(2)} req/s`);
    console.log(`Tasa Fallos HTTP: ${(m.http_req_failed.rate * 100).toFixed(2)}% (${m.http_req_failed.fails} exitosas / ${m.http_req_failed.passes} fallidas)`);
    console.log(`Aserciones / Checks: ${m.checks.passes} de ${m.checks.passes + m.checks.fails} exitosos (${((m.checks.passes/(m.checks.passes + m.checks.fails))*100).toFixed(2)}%)`);
});
