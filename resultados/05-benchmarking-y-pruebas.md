# Especificación y Diseño: Benchmarking y Pruebas
**Software Design Description (SDD) — Integrante 5**
*Proyecto: Programación Serial y Paralela con MultiThreading (Java)*
*Módulo: Plan de pruebas, benchmarking y reporte final*

---

## 1. Responsabilidad del Módulo

El Integrante 5 es responsable de:
1. Ejecutar `SerialProcessor` y `ParallelProcessor` (vía `Main.java`) sobre el mismo dataset, en distintos tamaños de `N` y `n`.
2. Registrar `N`, `n`, tiempo serial, tiempo paralelo, mínimo, máximo y mejora porcentual de cada prueba en `resultados/resultados.csv`.
3. Verificar en cada corrida que el resultado paralelo sea idéntico al serial (`Main.java` ya aborta automáticamente si no coinciden — ver `04-solucion-paralela.md`, sección 4).
4. Generar tablas y gráficos comparativos a partir de `resultados.csv`.
5. Analizar cómo cambia la mejora de rendimiento al escalar `N` y `n`.
6. Documentar conclusiones para el informe final del grupo.

---

## 2. Metodología

Todas las pruebas se ejecutan con el mismo flujo, ya provisto por `Main.java`:

```text
java -cp out Main <ruta_dataset> resultados/resultados.csv
```

Cada ejecución corre **primero** `SerialProcessor` y **luego** `ParallelProcessor` sobre el **mismo archivo**, compara mínimo/máximo y calcula:

```text
mejora (%) = (tiempoSerialMs - tiempoParaleloMs) * 100 / tiempoSerialMs
```

El script `resultados/run_benchmarks.sh` automatiza la generación de los datasets (vía `DatasetGenerator`, reutilizando la semilla `42` para reproducibilidad) y la ejecución de `Main` para cada combinación de `N` y `n` definida en este documento, acumulando todo en un único `resultados.csv`. El script `resultados/generar_graficos.py` (requiere `pandas` y `matplotlib`) lee ese CSV y produce los tres gráficos de la sección 5.

### 2.1 Casos de prueba seleccionados

Siguiendo los niveles de prueba definidos en `00-especificacion-general.md` (sección 9) y el acotamiento de escalabilidad decidido en `04-solucion-paralela.md` (sección 6.1):

| Caso | N | n | Objetivo |
|---|---:|---:|---|
| Validación mínima | 5 | 3 | Confirmar equivalencia serial/paralelo en un caso trivial |
| Nivel 2 (documento) | 1,000 | 3 | Reproducir el escenario indicado en el enunciado |
| Escalabilidad en N | 2,000 / 5,000 / 10,000 | 3 | Observar el efecto de aumentar N con n fijo |
| Escalabilidad en n | 1,000 con n=10 / n=50 | — | Observar el efecto de aumentar n con N fijo |
| Cruce N×n | 2,000 | 10 | Punto adicional con ambos factores aumentados |

No se prueban valores de N por encima de 10,000 en esta ronda: `04-solucion-paralela.md` ya documentó que la estrategia de I/O (doble `BufferedReader`, sin `RandomAccessFile`) hace que el costo crezca de forma aproximadamente `O(N²)`, por lo que valores mayores dejan de ser prácticos de ejecutar de forma repetida sin cambiar esa estrategia. Esta es una decisión de alcance del equipo, no una limitación del benchmarking en sí.

---

## 3. Validación de Equivalencia Serial/Paralelo

Para **todas** las pruebas ejecutadas, `Main.java` confirmó `Resultados idénticos: SÍ` antes de registrar el tiempo — es decir, ninguna corrida llegó a compararse en rendimiento sin antes pasar la validación de correctitud.

| N | n | Mínimo | Máximo | ¿Idénticos? |
|---:|---:|---|---|:---:|
| 5 | 3 | 1.8666279757894988 | 10.833074355878852 | ✅ |
| 1,000 | 3 | 0.6114907767088541 | 160.0026562593884 | ✅ |
| 2,000 | 3 | 0.45046110819914004 | 160.0026562593884 | ✅ |
| 5,000 | 3 | 0.3246476397573216 | 166.22381419366482 | ✅ |
| 10,000 | 3 | 0.2631802614179083 | 167.92492930547863 | ✅ |
| 1,000 | 10 | 24.695259453384974 | 226.1010746719042 | ✅ |
| 1,000 | 50 | 169.03940443792982 | 390.7720857101618 | ✅ |
| 2,000 | 10 | 22.695533848755357 | 232.38225223902106 | ✅ |

---

## 4. Resultados de Tiempos y Mejora

### 4.1 Escalabilidad en N (n = 3 fijo)

| N | n | Tiempo serial | Tiempo paralelo | Mejora |
|---:|---:|---:|---:|---:|
| 1,000 | 3 | 923 ms | 449 ms | 51.35% |
| 2,000 | 3 | 1,592 ms | 1,048 ms | 34.17% |
| 5,000 | 3 | 5,761 ms | 5,379 ms | 6.63% |
| 10,000 | 3 | 20,628 ms | 20,610 ms | 0.09% |

### 4.2 Escalabilidad en n (N = 1,000 fijo)

| N | n | Tiempo serial | Tiempo paralelo | Mejora |
|---:|---:|---:|---:|---:|
| 1,000 | 3 | 923 ms | 449 ms | 51.35% |
| 1,000 | 10 | 1,278 ms | 652 ms | 48.98% |
| 1,000 | 50 | 3,117 ms | 2,470 ms | 20.76% |

### 4.3 Punto adicional cruzado

| N | n | Tiempo serial | Tiempo paralelo | Mejora |
|---:|---:|---:|---:|---:|
| 2,000 | 10 | 2,966 ms | 2,302 ms | 22.39% |

> **Nota sobre el entorno de ejecución:** estos tiempos se midieron en un entorno de ejecución concreto (contenedor Linux compartido) y son útiles para comparar la *tendencia* serial vs paralelo, no como cifras absolutas de referencia. Al reproducir estas pruebas en otra máquina (por ejemplo, la usada en `04-solucion-paralela.md`, que reportó ~30% de mejora en N=20,000), los tiempos absolutos y el punto exacto donde la mejora decae van a variar según la cantidad de núcleos disponibles y la carga del sistema en el momento de la prueba.

---

## 5. Gráficos

![Tiempo Serial vs Paralelo según N](../../resultados/grafico_tiempos_vs_N.png)

*Ambas curvas crecen de forma consistente con el comportamiento `O(N²)` ya documentado en `04-solucion-paralela.md`; la curva paralela se mantiene por debajo de la serial, pero la separación entre ambas se reduce a medida que N crece.*

![Mejora de rendimiento según N](../../resultados/grafico_mejora_vs_N.png)

*La mejora porcentual cae de forma pronunciada con N: de ~51% en N=1,000 a prácticamente 0% en N=10,000.*

![Tiempo Serial vs Paralelo según n](../../resultados/grafico_tiempos_vs_n.png)

*Con N fijo en 1,000, aumentar n incrementa el tiempo de ambas versiones de forma aproximadamente lineal, ya que cada cálculo de distancia recorre las n dimensiones del punto.*

(Los archivos `.png` están en `resultados/` y se regeneran con `python3 resultados/generar_graficos.py`.)

---

## 6. Análisis de Escalabilidad

### 6.1 Efecto de aumentar N

La mejora de rendimiento del paralelo sobre el serial **no es constante**: es más alta en datasets pequeños (51.35% en N=1,000) y se acerca a cero en N=10,000 (0.09%). Esto es consistente con lo ya documentado en `04-solucion-paralela.md` (sección 6.1): el overhead de I/O de la estrategia de doble `BufferedReader` (reabrir el archivo por cada punto `i`) crece en el mismo orden que el cómputo, así que a medida que N aumenta ese overhead —que no se beneficia del paralelismo tanto como el cálculo puro de distancias— empieza a dominar el tiempo total, y el margen que el paralelismo puede recortar se reduce.

En este entorno de ejecución la caída fue incluso más marcada que en `04-solucion-paralela.md` (que reportaba una estabilización cerca de 30% en vez de una caída a 0%). La tendencia (mejora decreciente con N) es la misma en ambos casos; la diferencia en el punto exacto de estabilización es esperable entre máquinas distintas y confirma que **la comparación relevante es la tendencia, no un porcentaje fijo de mejora**.

### 6.2 Efecto de aumentar n

Con N fijo en 1,000, aumentar n también reduce la mejora relativa (51.35% en n=3, 48.98% en n=10, 20.76% en n=50), aunque de forma más suave que al aumentar N. Esto es esperable: n solo agrega trabajo lineal dentro de cada comparación de distancia (`DistanceCalculator`, ver `01-analisis-y-diseno-general.md`), mientras que N afecta la cantidad de comparaciones de forma cuadrática y, sobre todo, la cantidad de veces que se reabre el archivo en disco.

### 6.3 Conclusión de escalabilidad

El paralelismo con 2 hilos **sí ofrece una mejora real y medible**, pero esa mejora es más visible en datasets pequeños/medianos. Para que el beneficio se sostuviera en datasets grandes, el cuello de botella a resolver no sería el número de hilos sino la estrategia de I/O (evitar reabrir el archivo por cada punto). Esto queda fuera del alcance decidido por el grupo (`04-solucion-paralela.md`, sección 6.1) y se documenta aquí como una limitación conocida, no como un error de la implementación.

---

## 7. Conclusiones del Benchmarking

1. En **todos** los casos probados (8 combinaciones de N y n), el resultado paralelo fue idéntico al serial — la implementación de `ParallelProcessor` es correcta.
2. El paralelismo con 2 hilos reduce el tiempo de ejecución en todos los casos con N ≥ 1,000, con mejoras de hasta ~51% en los tamaños más pequeños probados.
3. La mejora relativa **decrece** a medida que N crece, por el overhead de I/O ya identificado por el Integrante 4.
4. Aumentar n (dimensiones) incrementa el tiempo de ambas versiones de forma aproximadamente lineal y también reduce ligeramente la mejora relativa.
5. El caso N=5 (validación mínima) no es representativo de rendimiento: con datasets tan pequeños, el overhead de crear y sincronizar hilos supera cualquier beneficio del paralelismo (mejora de -66.67%, es decir, el paralelo fue más lento). Esto es esperado y **no invalida** la solución paralela; simplemente confirma que el paralelismo aporta a partir de cierto tamaño de dataset.

---

## 8. Entregables

- `resultados/resultados.csv` — tabla completa de resultados (8 corridas).
- `resultados/run_benchmarks.sh` — script reproducible que genera los datasets y ejecuta todas las pruebas.
- `resultados/generar_graficos.py` — script en Python que genera los 3 gráficos a partir del CSV.
- `resultados/grafico_tiempos_vs_N.png`, `grafico_mejora_vs_N.png`, `grafico_tiempos_vs_n.png`.
- Este documento (`05-benchmarking-y-pruebas.md`), con validación de equivalencia, tablas, gráficos, análisis de escalabilidad y conclusiones.
