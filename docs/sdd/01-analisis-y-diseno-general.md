**Análisis del Problema y Diseño General**

*Proyecto: Programación Serial y Paralela con MultiThreading (Java)*

*Aplicación de referencia: Aprendizaje No Supervisado — cálculo de distancias mínima y máxima*

# 1\. Análisis del enunciado

El problema plantea procesar un dataset multidimensional compuesto por N puntos, donde cada punto tiene n atributos (dimensiones). El objetivo del programa es recorrer todas las combinaciones necesarias de puntos para determinar la distancia mínima y la distancia máxima entre ellos, comparando el costo en tiempo de una implementación serial contra una implementación paralela con 2 hilos.

El caso de referencia explícito del documento es:

* N \= 1000 puntos

* n \= 3 dimensiones

* Hilos \= 2

**Restricciones clave que condicionan todo el diseño:**

| Restricción | Impacto en el diseño |
| :---- | :---- |
| Lenguaje Java | Sin librerías externas, solo API estándar |
| Sin ArrayList ni estructuras complejas | Se usan arrays primitivos (double\[\]) de tamaño fijo por punto, o lectura por streaming sin acumular en memoria |
| No cargar todo el dataset en memoria | Lectura secuencial desde disco (buffered), procesando un punto a la vez o por bloques |
| Debe escalar en N y n | El formato de archivo y el algoritmo no deben depender de tamaños fijos |
| Comparar serial vs. paralelo | Ambos deben leer el mismo archivo y producir resultados idénticos |

# 2\. Definiciones formales

## 2.1 ¿Qué representa N?

N es la cantidad total de puntos en el dataset. Cada punto es un registro independiente que se procesará para el cálculo de distancias.

## 2.2 ¿Qué representa n?

n es el número de dimensiones (atributos) que tiene cada punto. Por ejemplo, con n \= 3, cada punto es un vector (x1, x2, x3).

## 2.3 ¿Cómo se representa un punto?

Un punto se representa como un vector de n valores numéricos de punto flotante (double), en un orden fijo y consistente para todos los puntos del dataset:

Punto\_i \= (v1, v2, v3, ..., vn)

En memoria (dentro del hilo que procesa), un punto se maneja como double\[n\], leído y descartado inmediatamente después de usarse (no se acumulan todos los puntos en una estructura global).

## 2.4 ¿Qué significa "distancia mínima" y "distancia máxima"?

El enunciado no detalla explícitamente entre qué elementos se calcula la distancia, por lo que se evaluaron dos interpretaciones posibles y se adoptó una de ellas como definición oficial del proyecto:

### Interpretación A (par de puntos — par a par) — Adoptada:

Se calcula la distancia entre todos los pares posibles de puntos (Pi, Pj) con i \< j, y se busca:

* distancia\_mínima \= min( d(Pi, Pj) ) para todo par válido

* distancia\_máxima \= max( d(Pi, Pj) ) para todo par válido

Esto tiene complejidad O(N²) y es coherente con problemas típicos de clustering/aprendizaje no supervisado (p. ej. hallar los dos puntos más cercanos/lejanos).

### Interpretación B (respecto a un punto de referencia, p. ej. el origen o un centroide) — Descartada:

Se calcula la distancia de cada punto respecto a un punto de referencia fijo, y se busca el mínimo y máximo de esas distancias individuales. Complejidad O(N).

**Decisión de diseño:** se implementará la Interpretación A (par a par), por ser la que da sentido real a "aprendizaje no supervisado" (similar a construir la matriz de distancias usada en clustering jerárquico). Esta decisión es la base sobre la que trabajarán Integrante 3 y 4, ya que define la complejidad algorítmica y la forma de dividir el trabajo entre hilos.

# 3\. Fórmula de distancia adoptada

Se utilizará la distancia Euclidiana, por ser el estándar en problemas de este tipo:

d(P, Q) \= sqrt( Σ (Pk \- Qk)² )   para k \= 1 hasta n

Todo el diseño de abajo es paramétrico respecto a la fórmula de distancia: se aísla el cálculo en un único método (DistanceCalculator.calcular(double\[\] p, double\[\] q)) para que, si más adelante se necesitara ajustar la fórmula, el cambio sea de una sola línea y no afecte al resto del sistema.

# 4\. Formato del archivo de entrada (dataset)

Se diseña el formato para que:

* sea fácil de leer línea por línea sin cargar todo en memoria,

* permita cualquier N y n,

* sea fácil de generar (tarea de Integrante 2\) y de leer (Integrante 3 y 4).

## 4.1 Estructura del archivo (texto plano, .txt o .csv)

**Primera línea \= encabezado con metadatos:**

N n

**Líneas siguientes \= un punto por línea, valores separados por espacio:**

v1 v2 v3 ... vn

## 4.2 Ejemplo concreto (N=5, n=3)

5 3  
1.23 4.56 7.89  
2.34 5.67 8.90  
0.12 3.45 6.78  
9.87 6.54 3.21  
4.44 5.55 6.66

## 4.3 Justificación del formato

| Decisión | Razón |
| :---- | :---- |
| Separador \= espacio simple | Fácil de tokenizar con Scanner/BufferedReader.split(" ") sin librerías externas |
| Encabezado con N n | Permite validar que el archivo tiene la cantidad esperada de líneas/columnas antes de procesar (tarea de Integrante 2\) |
| Un punto por línea | Permite leer el archivo con BufferedReader.readLine() de forma secuencial, sin cargar todo el dataset en memoria |
| Extensión .txt | Evita ambigüedades de parsing de CSV con comas decimales |

*Este formato queda definido para que Integrante 2 lo implemente en DatasetGenerator.java.*

# 5\. Diseño de la estructura general del programa

## 5.1 Diagrama de flujo general

                 DATASET (archivo en disco)  
                        |  
                        v  
             \+----------------------+  
             | Leer encabezado N, n |  
             \+----------------------+  
                        |  
             \+----------+-----------+  
             |                      |  
             v                      v  
      MODO SERIAL             MODO PARALELO  
   (SerialProcessor)        (ParallelProcessor)  
             |                      |  
             v                      v  
    Recorre todo el          Divide el trabajo  
    dataset en un solo       en 2 partes  
    hilo (main thread)              |  
             |              \+-------+-------+  
             |              |               |  
             |              v               v  
             |          Hilo 1          Hilo 2  
             |          (mitad A)       (mitad B)  
             |              |               |  
             |              v               v  
             |         min/max local   min/max local  
             |              |               |  
             |              \+-------+-------+  
             |                      |  
             |                      v  
             |              Combinar resultados  
             |              (sincronización)  
             |                      |  
             v                      v  
     Distancia mínima        Distancia mínima  
     Distancia máxima        Distancia máxima  
             |                      |  
             \+----------+-----------+  
                        |  
                        v  
              Comparar resultados  
           (deben ser idénticos)  
                        |  
                        v  
           Comparar tiempos de ejecución  
                        |  
                        v  
                Guardar resultados  
              (resultados.csv)

## 5.2 Componentes del sistema y responsables

| Clase | Responsable | Función |
| :---- | :---- | :---- |
| DatasetGenerator.java | Integrante 2 | Genera el archivo de dataset según formato definido en sección 4 |
| DistanceCalculator.java | Integrante 1 (diseño) / 3 y 4 (uso) | Método único y compartido para calcular distancia entre dos puntos |
| SerialProcessor.java | Integrante 3 | Recorre el dataset en un solo hilo y calcula min/máx |
| ThreadProcessor.java | Integrante 4 | Lógica que ejecuta cada hilo (procesa su porción y guarda min/máx local) |
| ParallelProcessor.java | Integrante 4 | Crea los 2 hilos, reparte el trabajo, combina resultados |
| Main.java | Todo el grupo | Orquesta: lee argumentos (N, n, ruta dataset), ejecuta serial, ejecuta paralelo, mide tiempos, guarda resultados |

## 5.3 Contrato entre módulos (para que todos trabajen sobre la misma arquitectura)

Para que Integrante 3 y 4 puedan trabajar en paralelo sin bloquearse mutuamente, se define esta interfaz común (pseudocódigo Java):

// DistanceCalculator.java — clase compartida, sin estado  
public class DistanceCalculator {  
    public static double calcular(double\[\] p, double\[\] q) {  
        double suma \= 0;  
        for (int k \= 0; k \< p.length; k++) {  
            double diff \= p\[k\] \- q\[k\];  
            suma \+= diff \* diff;  
        }  
        return Math.sqrt(suma);  
    }  
}

// Resultado.java — objeto simple para transportar min/max  
public class Resultado {  
    public double minimo;  
    public double maximo;  
    public long tiempoMs;  
}

Tanto SerialProcessor como ParallelProcessor deben exponer la misma firma de método:

Resultado procesar(String rutaDataset, int N, int n);

Esto permite que Main.java los invoque de forma intercambiable y compare resultados/tiempos sin conocer detalles internos de cada uno.

# 6\. División del procesamiento entre los 2 hilos

## 6.1 Estrategia de división

Dado que se adopta la Interpretación A (distancias par a par, sección 2.4), el conjunto total de pares a evaluar es:

Total de pares \= N \* (N \- 1\) / 2

**Estrategia elegida: división por rango de índice del primer punto (i).**

Se reparte el índice externo i del bucle de pares entre los 2 hilos, en dos mitades:

* Hilo 1: procesa los pares (i, j) donde i va de 0 hasta N/2 \- 1

* Hilo 2: procesa los pares (i, j) donde i va de N/2 hasta N \- 1

Cada hilo recorre, para su rango de i, todos los j \> i dentro del dataset completo.

**Nota de balance de carga:** esta división no es perfectamente equitativa en número de pares (el hilo con los índices i más bajos procesa más pares que el de índices altos, porque j siempre es mayor que i). Para el caso de referencia (N=1000, 2 hilos) el desbalance es aceptable. Si el grupo quiere balancear exactamente el número de comparaciones, se puede usar una división por "franjas intercaladas" (hilo 1 toma i par, hilo 2 toma i impar) — queda como posible mejora a discutir con el equipo.

## 6.2 Diagrama de división del trabajo

                    Dataset completo (N puntos)  
                              |  
                 índice i \= 0 ... N-1  
                              |  
              \+---------------+---------------+  
              |                               |  
              v                               v  
   Hilo 1: i \= 0 .. N/2-1          Hilo 2: i \= N/2 .. N-1  
   (compara cada i con              (compara cada i con  
    todo j \> i)                      todo j \> i)  
              |                               |  
              v                               v  
     min\_local\_1, max\_local\_1        min\_local\_2, max\_local\_2  
              |                               |  
              \+---------------+---------------+  
                              |  
                              v  
                 min\_global \= min(min\_local\_1, min\_local\_2)  
                 max\_global \= max(max\_local\_1, max\_local\_2)

## 6.3 Sincronización necesaria

* Cada hilo no comparte estado mutable durante el procesamiento: cada uno mantiene su propio minLocal y maxLocal en variables locales (evita condiciones de carrera por diseño, sin necesidad de synchronized).

* El hilo principal (main) hace thread.join() sobre ambos hilos antes de leer sus resultados.

* La combinación final (Math.min / Math.max entre los resultados locales) se hace en el hilo principal, después de que ambos hilos terminaron — nunca de forma concurrente.

* Ambos hilos leen el archivo del dataset solo en modo lectura, por lo que no hay conflicto de escritura simultánea sobre el mismo recurso.

## 6.4 Consideración sobre lectura desde disco en paralelo

Como el dataset no se carga completo en memoria, cada hilo necesita poder acceder a los puntos que le corresponden sin cargar todo el archivo. Dos opciones a decidir con Integrante 4:

* Cada hilo abre su propio RandomAccessFile y salta directamente a la posición de los puntos que necesita (requiere que el formato tenga registros de longitud fija o un índice de offsets).

* Cada hilo abre su propio BufferedReader independiente sobre el mismo archivo y descarta (skip) las líneas que no le corresponden hasta llegar a su rango.

**Recomendación:** para el caso de referencia (N=1000) la opción 2 es más simple de implementar y suficiente. Para escalar a valores muy grandes (N=100,000,000), la opción 1 sería más eficiente. Se recomienda implementar primero la opción 2 y documentarla como limitación conocida de escalabilidad.

# 7\. Documento de especificación del problema (resumen ejecutivo)

| Aspecto | Definición |
| :---- | :---- |
| N | Número total de puntos en el dataset |
| n | Número de dimensiones (atributos) por punto |
| Punto | Vector de n valores double, leído desde disco |
| Distancia mínima | El menor valor de distancia entre todos los pares de puntos evaluados |
| Distancia máxima | El mayor valor de distancia entre todos los pares de puntos evaluados |
| Fórmula de distancia | Euclidiana |
| Formato de dataset | Texto plano, primera línea N n, luego un punto por línea, valores separados por espacio |
| División de hilos | Por rango de índice i (mitad inferior / mitad superior) |
| Sincronización | Sin memoria compartida durante el cálculo; combinación de resultados solo tras join() |
| Validación | Resultado serial \== resultado paralelo, para el mismo dataset |

1. 