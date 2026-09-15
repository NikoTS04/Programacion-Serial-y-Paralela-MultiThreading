# Especificación y Diseño: Generación y Manejo del Dataset
**Software Design Description (SDD) — Integrante 2**  
*Proyecto: Programación Serial y Paralela con MultiThreading (Java)*  
*Módulo: Generador y Validador de Datos Multidimensionales*

---

## 1. Responsabilidad del Módulo

El Integrante 2 es responsable de:
1. Diseñar e implementar el generador de datasets sintéticos (`DatasetGenerator.java`).
2. Diseñar e implementar el mecanismo de validación de integridad (`DatasetValidator.java`).
3. Producir datasets de prueba para calibración y benchmarking.
4. Garantizar el cumplimiento estricto de las restricciones impuestas por la cátedra:
   - Cero librerías externas.
   - Prohibido el uso de `ArrayList` o colecciones análogas.
   - Prohibido cargar el dataset completo en memoria (streaming directo a disco).
   - Escalabilidad para $N \in [5, 100\,000\,000]$ y $n \in [2, 1000]$ con consumo de memoria $O(1)$.

---

## 2. Formato Formal del Dataset

Basado en el diseño preliminar del Integrante 1 (`docs/sdd/01-analisis-y-diseno-general.md`), el dataset es un archivo de texto plano estructurado de la siguiente forma:

### 2.1 Especificación de la Estructura

- **Línea 1 (Encabezado):**
  ```text
  N n
  ```
  Donde:
  - `N`: Entero positivo que indica la cantidad total de puntos en el dataset ($N \ge 2$).
  - `n`: Entero positivo que indica la dimensionalidad o cantidad de atributos de cada punto ($n \ge 1$).
  - Separador: Espacio en blanco simple (`' '`).

- **Líneas 2 a $N+1$ (Puntos):**
  ```text
  v1 v2 v3 ... vn
  ```
  Donde cada $v_k$ es un número de punto flotante (`double`) expresado con punto decimal (formato estándar `Locale.US`), delimitado por un espacio en blanco.

- **Fin de línea:** Estándar de sistema (`\n` o `\r\n`), manejado transparentemente por `BufferedReader` / `BufferedWriter`.

### 2.2 Ejemplo (N=5, n=3)
```text
5 3
1.23 4.56 7.89
2.34 5.67 8.90
0.12 3.45 6.78
9.87 6.54 3.21
4.44 5.55 6.66
```

---

## 3. Arquitectura y Estrategia de I/O de Alto Rendimiento

### 3.1 Restricción de Memoria y Streaming
Si se cargaran $N = 10\,000\,000$ puntos con $n = 3$ dimensiones como objetos en memoria, se requerirían varios gigabytes de RAM, disparando excepciones `OutOfMemoryError` o pausas críticas del Garbage Collector.

**Solución adoptada:**
- **Streaming Point-by-Point:** La generación procesa un único vector `double[n]` a la vez.
- **Buffer de I/O optimizado:** Se utiliza `BufferedWriter` envolviendo un `FileWriter` con un buffer explícito de **64 KB** (`65536 bytes`). Esto reduce drásticamente los accesos físicos a disco (syscalls `write()`).
- **Reutilización de Buffers de Texto:** Se utiliza un `StringBuilder` predimensionado que se vacía con `.setLength(0)` en cada punto para evitar crear millones de objetos `String` temporales.

```text
[ Generador ]
     |
     v (genera 1 punto: double[n])
[ Formateador StringBuilder reutilizable ]
     | (escribe texto al buffer)
[ BufferedWriter (64 KB) ]
     | (cuando se llena el buffer)
     v
[ Disco: dataset.txt ]
```

---

## 4. Componente 1: `DatasetGenerator.java`

### 4.1 Interfaz de Línea de Comandos (CLI)
```bash
java -cp src DatasetGenerator <N> <n> <ruta_salida> [minVal] [maxVal] [seed]
```

#### Parámetros:
- `N` (Obligatorio): Cantidad de puntos a generar ($N \ge 2$).
- `n` (Obligatorio): Cantidad de dimensiones ($n \ge 1$).
- `ruta_salida` (Obligatorio): Ruta del archivo a generar (ej. `datasets/dataset_1000_3.txt`).
- `minVal` (Opcional, por defecto `0.0`): Valor mínimo para los componentes aleatorios.
- `maxVal` (Opcional, por defecto `100.0`): Valor máximo para los componentes aleatorios.
- `seed` (Opcional, por defecto pseudoaleatorio libre): Semilla entera (`long`) para inicializar `Random`. Permite generar datasets deterministas y reproducibles para benchmarks científicos.

### 4.2 Algoritmo de Generación
1. Validar parámetros de entrada.
2. Calcular estimación aproximada de tamaño en disco y alertar al usuario si el archivo superará 1 GB.
3. Abrir `BufferedWriter` con buffer de 64 KB.
4. Escribir encabezado: `N + " " + n + "\n"`.
5. Iterar de $i = 0$ hasta $N-1$:
   - Para cada dimensión $k = 0 \dots n-1$:
     - Calcular $v = minVal + random.nextDouble() \times (maxVal - minVal)$.
     - Redondear a 4 decimales o formatear directamente.
     - Anexar a `StringBuilder`.
   - Escribir línea en buffer.
6. Vaciar (`flush`) y cerrar recursos en bloque `try-with-resources`.
7. Medir tiempo transcurrido y reportar velocidad de escritura (puntos/segundo y MB/s).

---

## 5. Componente 2: `DatasetValidator.java`

### 5.1 Objetivo
Permite a cualquier integrante comprobar de manera automática y en segundos que un archivo de dataset cumple con la especificación antes de iniciar un procesamiento serial o paralelo.

### 5.2 Algoritmo de Validación en Streaming
1. Abrir `BufferedReader` con buffer de 64 KB.
2. Leer primera línea:
   - Tokenizar mediante split de espacios.
   - Validar que existan exactamente 2 tokens.
   - Parsear $N_{header}$ y $n_{header}$.
3. Bucle secuencial línea a línea:
   - Mantener contador de líneas de datos leídas `lineasLeidas`.
   - Para cada línea:
     - Si está vacía y es la última, reportar advertencia o error según fin de archivo.
     - Tokenizar por espacio.
     - Validar que la cantidad de tokens sea exactamente $n_{header}$.
     - Validar que cada token sea un `double` válido.
     - Incrementar `lineasLeidas`.
4. Al finalizar la lectura:
   - Validar que `lineasLeidas == N_{header}`.
5. Reporte de diagnóstico detallado:
   - Si es válido: mostrar $N$, $n$, tamaño del archivo y tiempo de validación.
   - Si es inválido: mostrar el número de línea exacto del fallo y la causa específica (ej. "Línea 452 tiene 4 dimensiones en vez de 3").

---

## 6. Datasets de Prueba Recomendados para el Grupo

| Dataset | $N$ | $n$ | Propósito | Integrante Clave |
|---|---|---|---|---|
| `dataset_test_5_3.txt` | 5 | 3 | Verificación manual de distancias euclidianas exactas | Integrante 1 y 3 |
| `dataset_1000_3.txt` | 1,000 | 3 | **Caso oficial del enunciado** (2 hilos vs serial) | Todos |
| `dataset_100k_3.txt` | 100,000 | 3 | Evaluación de escalabilidad en $N$ | Integrante 4 y 5 |
| `dataset_1000_10.txt` | 1,000 | 10 | Evaluación de escalabilidad en dimensiones $n$ | Integrante 4 y 5 |
| `dataset_1M_3.txt` | 1,000,000 | 3 | Prueba de estrés de MultiThreading | Integrante 5 |

---

## 7. Recomendaciones Técnicas para Integrantes 3 y 4 (Lectura sin Memoria)

Para que los Integrantes 3 (Serial) y 4 (Paralelo) cumplan la restricción de **no cargar el dataset en memoria**, deben leer el archivo utilizando la siguiente pauta:

1. **Lectura con BufferedReader:**
   ```java
   try (BufferedReader reader = new BufferedReader(new FileReader(ruta), 65536)) {
       String header = reader.readLine();
       String[] parts = header.trim().split("\\s+");
       int N = Integer.parseInt(parts[0]);
       int n = Integer.parseInt(parts[1]);
       
       String line;
       while ((line = reader.readLine()) != null) {
           // Parsear sólo el punto actual en un double[n]
           // Procesar y descartar inmediatamente
       }
   }
   ```
2. **Cálculo de pares $O(N^2)$ en disco:**
   - Si no se pueden almacenar todos los puntos en RAM, se puede mantener un punto en memoria ($P_i$) y hacer un segundo recorrido de streaming para comparar con los $P_j$ siguientes, o bien mantener bloques de tamaño controlado si el tamaño de $N$ lo requiere. Esta estrategia queda coordinada con Integrantes 3 y 4.
