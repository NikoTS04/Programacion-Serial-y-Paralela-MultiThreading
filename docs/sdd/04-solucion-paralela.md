# Especificación y Diseño: Solución Paralela
**Software Design Description (SDD) — Integrante 4**
*Proyecto: Programación Serial y Paralela con MultiThreading (Java)*
*Módulo: Procesador Paralelo con MultiThreading (2 hilos)*

---

## 1. Responsabilidad del Módulo

El Integrante 4 es responsable de:
1. Implementar `ThreadProcessor.java`, el hilo que procesa una franja de índices.
2. Implementar `ParallelProcessor.java`, el orquestador que crea los 2 hilos, reparte el trabajo y combina resultados.
3. Garantizar que el resultado paralelo sea **exactamente idéntico** al de `SerialProcessor` (ver `03-solucion-serial.md`).
4. Medir el tiempo total de ejecución paralela y compararlo contra la línea base serial.

---

## 2. Contrato del Módulo

Respeta la misma interfaz común que `SerialProcessor` (definida en `01-analisis-y-diseno-general.md`, sección 5.3):

```java
Resultado procesar(String rutaDataset, int N, int n);
```

`Resultado` (`minimo`, `maximo`, `tiempoMs`) es la clase de transporte compartida entre ambos procesadores, lo que permite a `Main.java` invocarlos de forma intercambiable.

---

## 3. División del Trabajo entre los 2 Hilos

Se adopta la estrategia definida en `01-analisis-y-diseno-general.md` (sección 6.1): **división por rango del índice externo `i`**.

```text
                    Dataset completo (N puntos)
                              |
                 índice i = 0 ... N-1
                              |
              +---------------+---------------+
              |                               |
              v                               v
   Hilo 1: i = 0 .. N/2-1          Hilo 2: i = N/2 .. N-1
   (compara cada i con              (compara cada i con
    todo j > i)                      todo j > i)
              |                               |
              v                               v
     min_local_1, max_local_1        min_local_2, max_local_2
              |                               |
              +---------------+---------------+
                              |
                              v
                 min_global = min(min_local_1, min_local_2)
                 max_global = max(max_local_1, max_local_2)
```

`ThreadProcessor` recibe `(rutaDataset, n, iInicio, iFin)` y calcula, para cada `i` en su franja, la comparación contra todo `j > i`, usando la misma estrategia de doble `BufferedReader` que `SerialProcessor` (lector externo secuencial + lector interno por cada `i`).

### 3.1 Sincronización

- Cada hilo mantiene `minLocal` y `maxLocal` como variables de instancia propias, sin estado compartido durante el cálculo. No se requiere `synchronized`.
- `ParallelProcessor` hace `hilo1.join()` y `hilo2.join()` antes de leer los resultados locales.
- La combinación final (`Math.min` / `Math.max`) se hace en el hilo principal, después de que ambos hilos terminaron, nunca de forma concurrente.
- Ambos hilos leen el archivo en modo solo lectura, sin conflicto de escritura simultánea.

---

## 4. Punto Crítico: Equivalencia con el Resultado Serial

El resultado paralelo debe ser exactamente igual al serial; la diferencia esperada está en el **tiempo de ejecución**, no en el valor de mínimo/máximo.

---

## 5. Resultados de Validación

| Dataset | N | n | Serial (min / max) | Paralelo (min / max) | ¿Idénticos? |
|---|---:|---:|---|---|:---:|
| `dataset_test_5_3.txt` | 5 | 3 | 1.8666279757894988 / 10.833074355878852 | 1.8666279757894988 / 10.833074355878852 | ✅ |
| `dataset_1000_3.txt` (caso de referencia) | 1,000 | 3 | 0.6114907767088541 / 160.0026562593884 | 0.6114907767088541 / 160.0026562593884 | ✅ |
| `dataset_5000_3.txt` | 5,000 | 3 | 0.3246476397573216 / 166.22381419366482 | 0.3246476397573216 / 166.22381419366482 | ✅ |
| `dataset_20000_3.txt` | 20,000 | 3 | 0.05594229169420802 / 168.53530443589557 | 0.05594229169420802 / 168.53530443589557 | ✅ |

En todos los casos probados, serial y paralelo produjeron resultados idénticos.

---

## 6. Comparativa de Tiempos y Análisis de Escalabilidad

| N | n | Tiempo serial | Tiempo paralelo | Mejora |
|---:|---:|---:|---:|---:|
| 1,000 | 3 | 186 ms | 98 ms | 47.31% |
| 5,000 | 3 | 2,741 ms | 1,858 ms | 32.21% |
| 20,000 | 3 | 44,642 ms | 30,991 ms | 30.58% |

### 6.1 Observación: la mejora cae y luego se estabiliza

La mejora de rendimiento no se mantiene constante al aumentar N: cae de ~47% (N=1,000) a ~30-32% (N≥5,000), y luego se estabiliza en ese rango en vez de seguir bajando.

**Causa:** la estrategia de lectura elegida por simplicidad (`03-solucion-serial.md`, sección 3.1; recomendación de `01-analisis-y-diseno-general.md`, sección 6.4) reabre el dataset por cada `i` procesado. Este overhead de I/O es de orden `O(N)` por punto, y crece junto con el trabajo real de cómputo de cada hilo. A medida que N aumenta, el I/O empieza a pesar proporcionalmente más, reduciendo el beneficio relativo del paralelismo. El hecho de que la mejora se estabilice en vez de seguir cayendo indica que, a partir de cierto N, cómputo e I/O crecen a un ritmo comparable.

**Impacto en escalabilidad:** para N=20,000 el tiempo serial ya es de ~44.6 segundos. Dado el crecimiento aproximadamente `O(N²)` de esta estrategia, escalar hasta valores grandes (N=100,000 o más, mencionados como objetivo conceptual en `00-especificacion-general.md`) sería impráctico sin cambiar el mecanismo de lectura.

**Decisión del equipo:** se mantiene la estrategia actual de doble `BufferedReader` (sin migrar a `RandomAccessFile`), y las pruebas de escalabilidad del Integrante 5 se acotan a un rango de N razonable (hasta el orden de los 20,000-50,000 probados en este documento). Esta limitación queda documentada explícitamente en el informe final como una decisión consciente de alcance, no como un defecto de la implementación: el contrato de `ParallelProcessor` es correcto y funcional para el rango decidido, y la equivalencia con el resultado serial se mantiene validada en todos los tamaños probados (sección 5).

---

## 7. Entregables

- `ThreadProcessor.java`
- `ParallelProcessor.java`
- División del trabajo por rango de índice `i`.
- Sincronización sin memoria compartida (variables locales por hilo + `join()`).
- Resultados de validación contra `SerialProcessor` en cuatro tamaños de dataset.
- Análisis de escalabilidad y limitación conocida de la estrategia de I/O.
