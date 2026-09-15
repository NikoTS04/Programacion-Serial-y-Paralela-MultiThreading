# Plan de Trabajo Grupal
## Programación Serial y Paralela – MultiThreading
### Aplicación: Aprendizaje No Supervisado

**Integrantes:** 5  
**Fecha de presentación:** 11/09/2026  
**Modalidad:** Presencial

---

## 1. Objetivo del trabajo

El trabajo consiste en implementar en **Java** una solución **serial** y una solución **paralela mediante MultiThreading** para procesar un dataset multidimensional.

El problema planteado por el documento parte de un conjunto de **N puntos**, donde cada punto posee **n atributos o dimensiones**. A medida que aumentan N y n, el procesamiento requiere mayor costo computacional, por lo que se busca aprovechar la programación paralela.

El ejemplo concreto indicado en el documento consiste en utilizar **N = 1000 puntos de 3 dimensiones y 2 hilos** para determinar las **distancias mínima y máxima**.

La solución debe respetar estas restricciones:

- Usar Java.
- Implementar una versión serial.
- Implementar una versión paralela.
- Utilizar MultiThreading en la versión paralela.
- No utilizar bibliotecas externas.
- No utilizar estructuras de datos complejas como `ArrayList`.
- Procesar los datos en disco, no cargar todo el dataset en memoria.
- Diseñar la solución para poder escalar con diferentes valores de N y n.
- Comparar el comportamiento de la solución serial frente a la paralela.

> **Importante:** El PDF no especifica explícitamente la fórmula de distancia ni todos los detalles del formato del dataset. Antes de cerrar la implementación, el grupo debe confirmar estos puntos con las indicaciones del docente.

---

# 2. Distribución del trabajo entre 5 integrantes

## Integrante 1 – Análisis del problema y diseño general

### Responsabilidad
Comprender formalmente el problema y definir cómo funcionará toda la solución.

### Tareas

1. Analizar el enunciado.
2. Definir:
   - Qué representa N.
   - Qué representa n.
   - Cómo se representan los puntos.
   - Qué significa distancia mínima.
   - Qué significa distancia máxima.
3. Confirmar con el docente la fórmula de distancia que se debe utilizar.
4. Diseñar el formato del archivo de entrada.
5. Diseñar la estructura general del programa.
6. Definir cómo se dividirá el procesamiento entre los 2 hilos.
7. Documentar las decisiones técnicas para que los otros integrantes trabajen sobre una misma arquitectura.

### Entregables

- Documento de especificación del problema.
- Diseño general del programa.
- Formato definido para el dataset.
- Diagrama del flujo general.
- Definición de cómo se dividirá el trabajo paralelo.

### Ejemplo de estructura

```text
Dataset
   |
   v
Lectura desde disco
   |
   +--------------------+
   |                    |
   v                    v
Hilo 1               Hilo 2
   |                    |
   v                    v
Procesamiento        Procesamiento
   |                    |
   +---------+----------+
             |
             v
       Resultado global
       mínimo / máximo
```

---

# 3. Integrante 2 – Generación y manejo del dataset

### Responsabilidad
Crear el mecanismo para generar y almacenar los datos que serán procesados.

### Tareas

1. Crear un generador de datasets en Java.
2. Permitir configurar:
   - Número de puntos `N`.
   - Número de dimensiones `n`.
3. Generar datos de prueba.
4. Guardar los datos en disco.
5. Evitar cargar todo el dataset en memoria.
6. Permitir generar diferentes tamaños para las pruebas.
7. Validar que el archivo generado tenga la cantidad correcta de puntos y dimensiones.

### Casos de prueba

Se deben considerar progresivamente casos como:

```text
N = 1,000
N = 100,000
N = 1,000,000
...
```

y dimensiones:

```text
n = 2
n = 3
n = 10
n = 100
...
```

El documento plantea como objetivo de escalabilidad llegar conceptualmente hasta valores grandes como `N = 100,000,000` y `n = 1000`.

### Restricción importante

No utilizar:

```java
ArrayList
```

ni cargar todos los puntos en una estructura de memoria.

La idea es trabajar con el archivo directamente desde disco.

### Entregables

- `DatasetGenerator.java`
- Archivos de prueba.
- Validación de N y n.
- Documentación del formato utilizado.

---

# 4. Integrante 3 – Implementación de la solución serial

### Responsabilidad
Implementar la versión que procesa el dataset sin utilizar paralelismo.

### Tareas

1. Leer el dataset desde disco.
2. Procesar los puntos.
3. Calcular las distancias correspondientes.
4. Encontrar la distancia mínima.
5. Encontrar la distancia máxima.
6. Guardar los resultados.
7. Medir el tiempo de ejecución.
8. Verificar los resultados con datasets pequeños.

### Objetivo

La solución serial será la **línea base** para saber cuánto tarda el algoritmo sin paralelismo.

Ejemplo conceptual:

```text
Inicio
  |
  v
Abrir dataset
  |
  v
Leer datos
  |
  v
Calcular distancias
  |
  v
Actualizar mínimo/máximo
  |
  v
Finalizar lectura
  |
  v
Mostrar resultados
  |
  v
Mostrar tiempo
```

### Entregables

- `SerialProcessor.java`
- Cálculo de distancia.
- Cálculo de mínimo y máximo.
- Medición de tiempo.
- Resultados de validación.

---

# 5. Integrante 4 – Implementación de la solución paralela

### Responsabilidad
Convertir el procesamiento serial en una solución utilizando **MultiThreading**.

### Tareas

1. Analizar cómo dividir el dataset en dos partes.
2. Crear los **2 hilos** indicados en el ejemplo del documento.
3. Asignar a cada hilo una parte del trabajo.
4. Hacer que cada hilo procese su parte de forma independiente.
5. Obtener un mínimo y máximo local por hilo.
6. Combinar los resultados al finalizar los hilos.
7. Evitar condiciones de carrera.
8. Medir el tiempo total de ejecución.

### Arquitectura conceptual

```text
                 Dataset
                    |
             División del trabajo
                    |
          +---------+---------+
          |                   |
          v                   v
      Thread 1            Thread 2
          |                   |
          v                   v
     Mínimo local        Mínimo local
     Máximo local        Máximo local
          |                   |
          +---------+---------+
                    |
                    v
             Combinar resultados
                    |
                    v
            Mínimo global
            Máximo global
```

### Punto crítico

El resultado paralelo debe ser **exactamente equivalente** al resultado serial.

Si el serial obtiene:

```text
Mínimo = X
Máximo = Y
```

el paralelo debe obtener:

```text
Mínimo = X
Máximo = Y
```

La diferencia esperada debe estar principalmente en el **tiempo de ejecución**, no en el resultado.

### Entregables

- `ParallelProcessor.java`
- Clase o mecanismo para manejar los hilos.
- División del trabajo.
- Sincronización necesaria.
- Resultados de validación.

---

# 6. Integrante 5 – Pruebas, benchmarking y documentación

### Responsabilidad
Comprobar que todo funciona y demostrar experimentalmente la diferencia entre serial y paralelo.

### Tareas

1. Ejecutar la versión serial.
2. Ejecutar la versión paralela.
3. Utilizar exactamente el mismo dataset para ambas.
4. Registrar:
   - N.
   - n.
   - Tiempo serial.
   - Tiempo paralelo.
   - Distancia mínima.
   - Distancia máxima.
5. Comparar resultados.
6. Calcular la mejora de rendimiento.
7. Crear tablas y gráficos.
8. Preparar las conclusiones.
9. Integrar la documentación final.

### Tabla sugerida

| N | n | Tiempo serial | Tiempo paralelo | Mejora |
|---:|---:|---:|---:|---:|
| 1,000 | 3 | ... | ... | ... |
| 100,000 | 3 | ... | ... | ... |
| 1,000 | 10 | ... | ... | ... |
| 100,000 | 10 | ... | ... | ... |

### Validación

Para cada prueba:

```text
Resultado serial
        =
Resultado paralelo
```

Si los resultados son diferentes, primero se debe corregir la implementación antes de comparar rendimiento.

### Entregables

- Tabla de resultados.
- Gráficos.
- Análisis de rendimiento.
- Conclusiones.
- Informe final.
- Material para la exposición.

---

# 7. Integración de los 5 trabajos

Aunque cada integrante tenga una responsabilidad principal, el proyecto debe funcionar como *un único sistema*.

La estructura final podría ser:

```text
Proyecto/
│
├── src/
│   ├── DatasetGenerator.java
│   ├── SerialProcessor.java
│   ├── ParallelProcessor.java
│   ├── DistanceCalculator.java
│   ├── ThreadProcessor.java
│   └── Main.java
│
├── datasets/
│   ├── dataset_1000_3.txt
│   └── ...
│
└── resultados/
    └── resultados.csv
```

La estructura exacta puede cambiar según las decisiones del grupo y las restricciones del docente.

---

# 8. Cronograma recomendado

## Fase 1 – Comprensión y diseño

**Responsables:** Integrante 1 + todo el grupo

- Analizar el PDF.
- Confirmar dudas con el docente.
- Definir fórmula de distancia.
- Definir formato del dataset.
- Diseñar la arquitectura.
- Definir cómo se dividirá el procesamiento.

**Resultado:** Diseño aprobado por todo el grupo.

---

## Fase 2 – Generación de datos

**Responsable principal:** Integrante 2

- Crear generador.
- Probar N pequeños.
- Probar diferentes dimensiones.
- Validar archivos.
- Comprobar que no se almacene innecesariamente todo en memoria.

**Resultado:** Dataset funcional.

---

## Fase 3 – Algoritmo serial

**Responsable principal:** Integrante 3

- Implementar lectura.
- Implementar cálculo de distancia.
- Obtener mínimo/máximo.
- Medir tiempo.
- Validar resultados.

**Resultado:** Solución serial funcionando.

---

## Fase 4 – Algoritmo paralelo

**Responsable principal:** Integrante 4

- Dividir el procesamiento.
- Implementar 2 hilos.
- Procesar cada parte.
- Combinar resultados.
- Validar contra la versión serial.

**Resultado:** Solución paralela funcionando.

---

## Fase 5 – Benchmarking

**Responsable principal:** Integrante 5

- Ejecutar pruebas.
- Registrar tiempos.
- Comparar serial/paralelo.
- Analizar escalabilidad.
- Crear gráficos.

**Resultado:** Resultados experimentales.

---

## Fase 6 – Integración final

**Responsables:** Los 5 integrantes

Todos deben revisar:

- Código.
- Resultados.
- Documentación.
- Restricciones del trabajo.
- Presentación.

No debería existir una parte del proyecto que solamente conozca un integrante.

---

# 9. Pruebas que debería realizar el grupo

Se recomienda realizar las pruebas de manera progresiva.

### Nivel 1 – Prueba pequeña

```text
N = 100
n = 3
```

Objetivo: comprobar que el algoritmo funciona.

### Nivel 2 – Prueba solicitada explícitamente

```text
N = 1,000
n = 3
Hilos = 2
```

Objetivo: reproducir el escenario indicado en el documento.

### Nivel 3 – Mayor cantidad de datos

```text
N = 100,000
n = 3
Hilos = 2
```

Objetivo: observar el efecto del aumento de N.

### Nivel 4 – Mayor dimensionalidad

```text
N = 1,000
n = 10 / 100
Hilos = 2
```

Objetivo: observar el efecto del aumento de n.

### Nivel 5 – Escalabilidad

Realizar pruebas incrementando progresivamente:

```text
N: 1,000 → 100,000 → 1,000,000 → ...
n: 3 → 10 → 100 → ...
```

Hasta donde el equipo pueda ejecutar las pruebas de forma segura.

---

# 10. Qué debe demostrar el trabajo

El proyecto no debería limitarse a decir:

> "La versión paralela utiliza dos hilos."

Debe demostrar experimentalmente:

1. Que el algoritmo serial funciona.
2. Que el algoritmo paralelo obtiene los mismos resultados.
3. Que el trabajo puede dividirse entre hilos.
4. Que el programa puede trabajar leyendo desde disco.
5. Que no depende de estructuras complejas como `ArrayList`.
6. Que puede trabajar con diferentes valores de N y n.
7. Cómo cambia el tiempo de ejecución al aumentar el tamaño del problema.
8. Qué beneficio aporta el procesamiento paralelo.

---

# 11. Estructura recomendada del informe

## 1. Introducción

Explicar el problema de procesamiento de datasets multidimensionales y la motivación para utilizar programación paralela.

## 2. Descripción del problema

Explicar:

- Dataset.
- N.
- n.
- Puntos multidimensionales.
- Distancias mínima y máxima.

## 3. Restricciones

Explicar las condiciones establecidas:

- Java.
- Sin bibliotecas.
- Sin `ArrayList` u otras estructuras complejas.
- Procesamiento en disco.
- Escalabilidad.

## 4. Solución serial

Explicar el algoritmo y mostrar su flujo.

## 5. Solución paralela

Explicar:

- MultiThreading.
- Dos hilos.
- División del trabajo.
- Resultados locales.
- Combinación de resultados.

## 6. Pruebas

Presentar los datasets utilizados.

## 7. Resultados

Comparar:

- Tiempo serial.
- Tiempo paralelo.
- N.
- n.
- Resultados obtenidos.

## 8. Análisis

Explicar cuándo la solución paralela mejora y cómo influye el tamaño del problema.

## 9. Conclusiones

Indicar qué se aprendió y qué ventajas/desventajas se observaron al utilizar procesamiento paralelo.

---

# 13. Repartición resumida

| Integrante | Área principal | Entregable |
|---|---|---|
| **1** | Análisis y arquitectura | Diseño del sistema |
| **2** | Dataset y archivos | Generador de datos |
| **3** | Algoritmo serial | Procesador serial |
| **4** | MultiThreading | Procesador paralelo |
| **5** | Pruebas y documentación | Benchmark + informe |

---

# 14. Recomendación importante

La división anterior **no significa que cada integrante solamente deba conocer su módulo**. Debido a que la presentación es grupal, todos deberían poder explicar:

- Qué problema se está resolviendo.
- Qué es N y qué es n.
- Cómo se calcula la distancia.
- Cómo funciona la versión serial.
- Cómo se divide el trabajo entre los hilos.
- Cómo se obtienen los resultados mínimo y máximo.
- Por qué se procesa desde disco.
- Por qué no se utiliza `ArrayList`.
- Cómo se midió el rendimiento.
- Qué resultados se obtuvieron.
- Qué conclusiones se pueden sacar.

Esto es especialmente importante porque el objetivo del trabajo es demostrar el uso de **programación serial y paralela**, no simplemente presentar cinco partes independientes

---

## 15. Meta final del proyecto

Al finalizar, el grupo debería tener un programa capaz de ejecutar conceptualmente:

```text
                 DATASET
                    |
                    v
          +-------------------+
          | Configurar N y n  |
          +-------------------+
                    |
          +---------+---------+
          |                   |
          v                   v
      SERIAL              PARALELO
                              |
                       +------+------+
                       |             |
                    HILO 1        HILO 2
                       |             |
                       +------+------+
                              |
                              v
                       RESULTADO FINAL
                              |
                +-------------+-------------+
                |                           |
          Distancia mínima             Distancia máxima
                |                           |
                +-------------+-------------+
                              |
                              v
                     COMPARACIÓN DE TIEMPOS
```

**Objetivo final:** demostrar que ambas implementaciones producen el mismo resultado y analizar experimentalmente el impacto del paralelismo cuando aumenta el tamaño y la dimensionalidad del dataset.

> **Fecha indicada en el PDF: 11/09/2026.** La modalidad indicada es grupal y presencial.
