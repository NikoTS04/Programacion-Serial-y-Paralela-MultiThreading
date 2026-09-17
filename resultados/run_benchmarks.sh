#!/bin/bash
# ---------------------------------------------------------------------------
# run_benchmarks.sh — Integrante 5 (Benchmarking y Reporte)
#
# Genera los datasets de prueba y ejecuta Main.java (Serial + Paralelo) para
# cada combinación de N y n, acumulando los resultados en resultados/resultados.csv
#
# Uso (desde la raíz del repositorio, después de compilar):
#   javac -d out src/*.java
#   bash resultados/run_benchmarks.sh
# ---------------------------------------------------------------------------
set -e

CP="out"
DATASETS_DIR="datasets"
RESULTADOS_CSV="resultados/resultados.csv"

mkdir -p "$DATASETS_DIR" resultados

echo "Limpiando resultados previos..."
rm -f "$RESULTADOS_CSV"

generar_y_correr() {
    local N=$1
    local n=$2
    local archivo="$DATASETS_DIR/dataset_${N}_${n}.txt"

    if [ ! -f "$archivo" ]; then
        echo ">> Generando dataset N=$N n=$n..."
        java -cp "$CP" DatasetGenerator "$N" "$n" "$archivo" 0.0 100.0 42 > /dev/null
    fi

    echo ">> Ejecutando benchmark N=$N n=$n..."
    java -cp "$CP" Main "$archivo" "$RESULTADOS_CSV"
    echo
}

# --- Caso de validación mínimo (proporcionado por Integrante 2) ---
generar_y_correr 5 3

# --- Escalabilidad en N (n=3 fijo, Nivel 2/3/5 de 00-especificacion-general.md) ---
generar_y_correr 1000 3
generar_y_correr 2000 3
generar_y_correr 5000 3
generar_y_correr 10000 3

# --- Escalabilidad en n (N=1000 fijo, Nivel 4 de 00-especificacion-general.md) ---
generar_y_correr 1000 10
generar_y_correr 1000 50

# --- Punto adicional cruzando N y n ---
generar_y_correr 2000 10

echo "Listo. Resultados en $RESULTADOS_CSV"
echo "Para generar los gráficos: python3 resultados/generar_graficos.py"
