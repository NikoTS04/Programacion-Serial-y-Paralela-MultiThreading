"""
Genera los gráficos de benchmarking (Integrante 5) a partir de resultados/resultados.csv.

Uso:
    python3 generar_graficos.py

Requiere: pandas, matplotlib (pip install pandas matplotlib)
Genera 3 imágenes PNG en la carpeta resultados/:
    - grafico_tiempos_vs_N.png
    - grafico_mejora_vs_N.png
    - grafico_tiempos_vs_n.png
"""
import pandas as pd
import matplotlib.pyplot as plt
import os

CARPETA = os.path.dirname(os.path.abspath(__file__))
CSV = os.path.join(CARPETA, "resultados.csv")

df = pd.read_csv(CSV)

# --- Filtramos el caso de validación mínimo (N=5) para los gráficos de escalabilidad ---
df_n3 = df[(df["n"] == 3) & (df["N"] > 5)].sort_values("N")
df_N1000 = df[df["N"] == 1000].sort_values("n")

# 1) Tiempo Serial vs Paralelo en función de N (n=3 fijo)
plt.figure(figsize=(7, 5))
plt.plot(df_n3["N"], df_n3["tiempoSerialMs"], marker="o", label="Serial")
plt.plot(df_n3["N"], df_n3["tiempoParaleloMs"], marker="o", label="Paralelo (2 hilos)")
plt.xlabel("N (número de puntos)")
plt.ylabel("Tiempo de ejecución (ms)")
plt.title("Tiempo Serial vs Paralelo según N (n=3)")
plt.legend()
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig(os.path.join(CARPETA, "grafico_tiempos_vs_N.png"), dpi=150)
plt.close()

# 2) Mejora porcentual vs N
plt.figure(figsize=(7, 5))
plt.plot(df_n3["N"], df_n3["mejoraPorcentaje"], marker="o", color="green")
plt.axhline(0, color="gray", linewidth=0.8)
plt.xlabel("N (número de puntos)")
plt.ylabel("Mejora de rendimiento (%)")
plt.title("Mejora de rendimiento (paralelo vs serial) según N (n=3)")
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig(os.path.join(CARPETA, "grafico_mejora_vs_N.png"), dpi=150)
plt.close()

# 3) Tiempo Serial vs Paralelo en función de n (N=1000 fijo)
plt.figure(figsize=(7, 5))
plt.plot(df_N1000["n"], df_N1000["tiempoSerialMs"], marker="s", label="Serial")
plt.plot(df_N1000["n"], df_N1000["tiempoParaleloMs"], marker="s", label="Paralelo (2 hilos)")
plt.xlabel("n (dimensiones por punto)")
plt.ylabel("Tiempo de ejecución (ms)")
plt.title("Tiempo Serial vs Paralelo según n (N=1,000)")
plt.legend()
plt.grid(True, alpha=0.3)
plt.tight_layout()
plt.savefig(os.path.join(CARPETA, "grafico_tiempos_vs_n.png"), dpi=150)
plt.close()

print("Gráficos generados en:", CARPETA)
