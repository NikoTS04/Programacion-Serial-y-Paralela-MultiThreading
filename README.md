# Programación Serial y Paralela – MultiThreading

Proyecto grupal para la asignatura de Computación Paralela y Distribuida / Programación Concurrente.
Implementación en **Java** de procesamiento de datasets multidimensionales (cálculo de distancias mínima y máxima par a par) comparando una solución serial contra una solución paralela con **MultiThreading (2 hilos)**.

---

## 📁 Estructura del Repositorio (SDD)

El proyecto sigue una metodología **SDD (Software Design Description / Spec-Driven Development)**:

```text
.
├── README.md                       # Documentación general y guía de uso
├── .gitignore                      # Exclusión de binarios y datasets pesados
│
├── docs/                           # Documentación técnica y especificaciones SDD
│   └── sdd/
│       ├── 00-especificacion-general.md    # Requisitos y lineamientos generales (5 integrantes)
│       ├── 01-analisis-y-diseno-general.md # SDD Integrante 1: Análisis, fórmulas y arquitectura
│       ├── 02-generador-dataset.md         # SDD Integrante 2: Especificación y diseño del dataset
│       ├── 03-solucion-serial.md           # SDD Integrante 3: Especificación serial (pendiente)
│       ├── 04-solucion-paralela.md         # SDD Integrante 4: Especificación paralela (pendiente)
│       └── 05-benchmarking-y-pruebas.md    # SDD Integrante 5: Plan de pruebas y métricas (pendiente)
│
├── src/                            # Código fuente en Java (sin librerías externas)
│   ├── DatasetGenerator.java       # [Integrante 2] Generador de datasets en streaming
│   ├── DatasetValidator.java       # [Integrante 2] Validador de formato e integridad en disco
│   ├── DistanceCalculator.java     # [Integrante 1 / Compartido] Cálculo de distancia euclidiana
│   ├── SerialProcessor.java        # [Integrante 3] Procesador serial de distancias
│   ├── ThreadProcessor.java        # [Integrante 4] Hilo de procesamiento por franja
│   ├── ParallelProcessor.java      # [Integrante 4] Orquestador paralelo (2 hilos)
│   └── Main.java                   # [Todo el equipo] Orquestador principal y benchmarking
│
├── datasets/                       # Datasets de prueba
│   ├── .gitkeep
│   └── dataset_test_5_3.txt        # Caso de prueba mínimo (N=5, n=3)
│
└── resultados/                     # Salidas de benchmarks y comparaciones de tiempo
    └── .gitkeep
```

---

## 🚀 Compilación y Ejecución (Integrante 2: Generador y Validador)

### 1. Compilar clases
```bash
javac src/*.java
```

### 2. Generar un dataset
```bash
# Sintaxis:
# java -cp src DatasetGenerator <N> <n> <archivo_salida> [minVal] [maxVal] [seed]

# Ejemplo de referencia (N=1000 puntos, n=3 dimensiones, valores entre 0.0 y 100.0):
java -cp src DatasetGenerator 1000 3 datasets/dataset_1000_3.txt 0.0 100.0 42
```

### 3. Validar la integridad del dataset
```bash
# Sintaxis:
# java -cp src DatasetValidator <archivo_dataset>

# Ejemplo:
java -cp src DatasetValidator datasets/dataset_1000_3.txt
```

---

## 📋 Distribución de Integrantes

| Integrante | Área | Entregables Principales |
|---|---|---|
| **Integrante 1** | Análisis y Arquitectura | Especificación del problema, fórmulas, contratos (`docs/sdd/01-...`) |
| **Integrante 2** | Generación y Manejo de Dataset | `DatasetGenerator.java`, `DatasetValidator.java`, datasets de prueba, `docs/sdd/02-...` |
| **Integrante 3** | Solución Serial | `SerialProcessor.java`, cálculo base y toma de tiempos |
| **Integrante 4** | Solución Paralela | `ThreadProcessor.java`, `ParallelProcessor.java` (2 hilos, sincronización) |
| **Integrante 5** | Benchmarking y Reporte | Pruebas de escalabilidad, gráficas, informe final y comparativa |
