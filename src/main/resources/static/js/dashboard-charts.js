/**
 * dashboard-charts.js
 * Inicializa los gráficos Chart.js para el dashboard principal.
 * Requiere Chart.js cargado previamente.
 */

// Inicializar iconos de Lucide
lucide.createIcons();

// Configuración de Gráficos con Chart.js
document.addEventListener("DOMContentLoaded", function () {
    // --- 1. GRÁFICO HISTÓRICO DE ASISTENCIA ---
    const ctxAttendance = document.getElementById('attendanceChart').getContext('2d');
    
    // Crear degradado verde para el fondo del área
    const gradient = ctxAttendance.createLinearGradient(0, 0, 0, 200);
    gradient.addColorStop(0, 'rgba(20, 95, 34, 0.25)');
    gradient.addColorStop(1, 'rgba(20, 95, 34, 0)');

    new Chart(ctxAttendance, {
        type: 'line',
        data: {
            labels: Array.from({length: 15}, (_, i) => `Día ${i + 1}`),
            datasets: [{
                label: 'Asistencia (%)',
                data: [93.1, 94.0, 92.5, 93.8, 94.2, 93.0, 92.8, 93.9, 94.5, 93.4, 94.0, 93.7, 94.1, 93.5, 94.2],
                borderColor: '#145f22',
                borderWidth: 3,
                backgroundColor: gradient,
                fill: true,
                tension: 0.4,
                pointRadius: 0,
                pointHoverRadius: 6,
                pointHoverBackgroundColor: '#145f22',
                pointHoverBorderColor: '#ffffff',
                pointHoverBorderWidth: 2
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: '#173518',
                    titleColor: '#ffffff',
                    bodyColor: '#ecffe4',
                    titleFont: { weight: 'bold' },
                    displayColors: false,
                    callbacks: {
                        label: function(context) {
                            return `Asistencia: ${context.parsed.y}%`;
                        }
                    }
                }
            },
            scales: {
                x: {
                    grid: {
                        display: false
                    },
                    ticks: {
                        color: '#6f826f',
                        font: {
                            size: 10,
                            weight: 'bold'
                        }
                    }
                },
                y: {
                    min: 85,
                    max: 100,
                    grid: {
                        color: 'rgba(196, 238, 192, 0.25)',
                        lineWidth: 1
                    },
                    ticks: {
                        color: '#6f826f',
                        font: {
                            size: 10,
                            weight: 'bold'
                        },
                        callback: function(value) {
                            return value + '%';
                        }
                    }
                }
            }
        }
    });

    // --- 2. GRÁFICO DE DISTRIBUCIÓN DE ALUMNOS ---
    const ctxDistribution = document.getElementById('distributionChart').getContext('2d');
    new Chart(ctxDistribution, {
        type: 'doughnut',
        data: {
            labels: ['Primaria', 'Secundaria', 'Media'],
            datasets: [{
                data: [45, 40, 15],
                backgroundColor: ['#145f22', '#34a853', '#aacf7f'],
                borderColor: '#ffffff',
                borderWidth: 2.5,
                hoverOffset: 4
            }]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            cutout: '75%',
            plugins: {
                legend: {
                    display: false
                },
                tooltip: {
                    backgroundColor: '#173518',
                    titleColor: '#ffffff',
                    bodyColor: '#ecffe4',
                    displayColors: true,
                    callbacks: {
                        label: function(context) {
                            return ` ${context.label}: ${context.parsed}%`;
                        }
                    }
                }
            }
        }
    });
});
