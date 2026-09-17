# Especificación y Diseño: Solución Serial
**Software Design Description (SDD) — Integrante 3**
*Proyecto: Programación Serial y Paralela con MultiThreading (Java)*
*Módulo: Procesador Serial de Distancias*

---

## 1. Responsabilidad del Módulo

El Integrante 3 es responsable de:
1. Implementar `SerialProcessor.java`, la línea base sin paralelismo.
2. Leer el dataset desde disco, calcular la distancia mínima y máxima entre todos los pares de puntos (Interpretación A, ver `01-analisis-y-diseno-general.md`, sección 2.4).
3. Medir el tiempo total de ejecución.
4. Garantizar que el resultado sea la referencia contra la cual se valida `ParallelProcessor.java`.

---

## 2. Contrato del Módulo

Respeta la interfaz común definida en `01-analisis-y-diseno-general.md` (sección 5.3):

```java
Resultado procesar(String rutaDataset, int N, int n);
```

Esto permite que `Main.java` invoque `SerialProcessor` y `ParallelProcessor` de forma intercambiable, sin conocer detalles internos de cada uno.

---

## 3. Algoritmo

`SerialProcessor` recorre, en el hilo principal, todos los pares `(i, j)` con `i < j`, sin dividir el trabajo:

```text
Inicio
  |
  v
Abrir dataset (readerExterno)
  |
  v
Para i = 0 hasta N-1:
  |   Leer punto i
  |   Abrir dataset de nuevo (readerInterno)
  |   Saltar hasta el punto i+1
  |   Para cada punto j > i:
  |       Calcular distancia(i, j)
  |       Actualizar mínimo / máximo global
  v
Fin
  |
  v
Mostrar resultados y tiempo
```

### 3.1 Estrategia de lectura desde disco

Siguiendo la restricción de no cargar el dataset completo en memoria, `SerialProcessor` usa **doble `BufferedReader`**:

- Un lector externo avanza secuencialmente por cada punto `i`.
- Por cada `i`, se abre un lector interno independiente que salta hasta `i+1` y recorre el resto del archivo comparando contra `puntoI`.

Es la misma estrategia ("opción 2") documentada en `01-analisis-y-diseno-general.md` (sección 6.4) para el caso de referencia, elegida por simplicidad de implementación por sobre el uso de `RandomAccessFile` con offsets.

### 3.2 Complejidad

- Comparaciones: `N * (N - 1) / 2` (todas las combinaciones de pares).
- Lecturas de disco: del orden de `O(N²)` líneas totales, por reabrir el archivo en cada `i`.

---

## 4. Resultados de Validación

Ejecutado sobre `datasets/dataset_test_5_3.txt` (N=5, n=3):

```text
Distancia mínima: 1.8666279757894988
Distancia máxima: 10.833074355878852
```

Ejecutado sobre el caso de referencia del enunciado (N=1000, n=3):

```text
Distancia mínima: 0.6114907767088541
Distancia máxima: 160.0026562593884
Tiempo: 186 ms
```

Ambos resultados fueron confirmados como línea base contra la cual `ParallelProcessor` produjo valores idénticos (ver `04-solucion-paralela.md`, sección 5).

---

## 5. Entregables

- `SerialProcessor.java`
- Cálculo de distancia (reutiliza `DistanceCalculator.calcular`).
- Cálculo de mínimo y máximo global.
- Medición de tiempo (`System.currentTimeMillis()`).
- Resultados de validación con datasets pequeños y con el caso de referencia.
