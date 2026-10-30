"""
Gera os gráficos de desempenho (serial x paralelo) a partir de resultados/resumo.csv.

Uso (na raiz do projeto, depois de rodar o Main.java):
    pip install pandas matplotlib
    python graficos.py             # só salva os PNGs em graficos/
    python graficos.py --mostrar   # salva e também abre as janelas dos gráficos

O Main.java chama este script com --mostrar no final da execução.
"""
import math
import sys
from pathlib import Path

import matplotlib

MOSTRAR = "--mostrar" in sys.argv
if not MOSTRAR:
    matplotlib.use("Agg")  # sem janela (útil em servidor / sem interface gráfica)
import matplotlib.pyplot as plt
import matplotlib.ticker as mticker
import pandas as pd

RAIZ = Path(__file__).resolve().parent
CSV = RAIZ / "resultados" / "resumo.csv"
SAIDA = RAIZ / "graficos"
SAIDA.mkdir(exist_ok=True)

ALGORITMOS = ["MergeSort", "QuickSort", "InsertionSort", "SelectionSort"]
TIPOS = ["aleatorio", "ordenado", "inverso"]
NOME_TIPO = {"aleatorio": "Aleatório", "ordenado": "Ordenado", "inverso": "Inverso"}
THREADS = [1, 2, 4, 8]
COR = {1: "#444444", 2: "#1f77b4", 4: "#ff7f0e", 8: "#2ca02c"}
MARC = {1: "s", 2: "o", 4: "^", 8: "D"}


def rotulo(t):
    return "Serial" if t == 1 else f"Paralelo ({t} threads)"


def fmt_n(x, _=None):
    return f"{int(x):,}".replace(",", ".")


def fmt_k(x, _=None):
    """Rótulo compacto para o eixo x: 1k, 10k, 50k, 100k."""
    return f"{int(x) // 1000}k"


df = pd.read_csv(CSV)
nucleos = None
try:
    nucleos = int(pd.read_csv(RAIZ / "resultados" / "resultados.csv", nrows=1)["nucleos_disponiveis"][0])
except Exception:
    pass
sub_nucleos = f" — máquina com {nucleos} núcleos" if nucleos else ""

plt.rcParams.update({"figure.dpi": 110, "axes.grid": True, "grid.alpha": 0.3, "font.size": 10})


def salvar(fig, nome):
    fig.savefig(SAIDA / nome, bbox_inches="tight")
    if not MOSTRAR:
        plt.close(fig)  # com --mostrar as figuras ficam abertas até o plt.show() final
    print("gerado:", SAIDA / nome)


# 1) Tempo serial x paralelo por algoritmo (uma figura por algoritmo, um painel por tipo de dado)
for alg in ALGORITMOS:
    fig, eixos = plt.subplots(1, 3, figsize=(15, 4.4), sharey=True, layout="constrained")
    for ax, tipo in zip(eixos, TIPOS):
        for t in THREADS:
            d = df[(df.algoritmo == alg) & (df.tipo_dados == tipo) & (df.threads == t)].sort_values("tamanho")
            ax.errorbar(d.tamanho, d.media_ms, yerr=d.desvio_padrao_ms, label=rotulo(t),
                        color=COR[t], marker=MARC[t], capsize=3, linewidth=1.8,
                        linestyle="--" if t == 1 else "-")
        ax.set_xscale("log")
        ax.set_yscale("log")
        ax.set_xticks(sorted(df.tamanho.unique()))
        ax.xaxis.set_major_formatter(mticker.FuncFormatter(fmt_k))
        ax.minorticks_off()
        ax.set_title(f"Dados {NOME_TIPO[tipo].lower()}")
        ax.set_xlabel("Tamanho da entrada (n)")
    eixos[0].set_ylabel("Tempo médio (ms) — escala log")
    eixos[0].legend(loc="upper left", fontsize=9)
    fig.suptitle(f"{alg}: tempo de execução serial x paralelo{sub_nucleos}", fontsize=13)
    salvar(fig, f"1_tempo_{alg}.png")

# limites do eixo y do speedup, calculados a partir dos dados (potencias de 2)
_sp = df[df.tamanho == df.tamanho.max()].speedup  # o grafico usa so o maior n
_exp_min = math.floor(math.log2(min(_sp.min(), 1.0)))
_exp_max = math.ceil(math.log2(max(_sp.max(), THREADS[-1])))
TICKS_SPEEDUP = [2.0 ** e for e in range(_exp_min, _exp_max + 1)]
YMIN_SPEEDUP = 2.0 ** _exp_min * 0.8
YMAX_SPEEDUP = 2.0 ** _exp_max * 1.25

# 2) Speedup por algoritmo (n = maior tamanho), um painel por tipo de dado
n_max = df.tamanho.max()
fig, eixos = plt.subplots(1, 3, figsize=(15, 4.4), layout="constrained")
for ax, tipo in zip(eixos, TIPOS):
    for alg in ALGORITMOS:
        d = df[(df.algoritmo == alg) & (df.tipo_dados == tipo) & (df.tamanho == n_max)].sort_values("threads")
        ax.plot(d.threads, d.speedup, marker="o", linewidth=1.8, label=alg)
    ax.plot(THREADS, THREADS, "k--", linewidth=1, label="Speedup ideal (linear)")
    ax.axhline(1, color="gray", linewidth=0.8)
    ax.set_xscale("log", base=2)
    ax.set_yscale("log", base=2)
    ax.set_xticks(THREADS)
    ax.set_yticks(TICKS_SPEEDUP)
    ax.set_ylim(YMIN_SPEEDUP, YMAX_SPEEDUP)
    ax.xaxis.set_major_formatter(mticker.ScalarFormatter())
    ax.yaxis.set_major_formatter(mticker.FuncFormatter(lambda v, _: f"{v:g}"))
    ax.minorticks_off()
    ax.set_title(f"Dados {NOME_TIPO[tipo].lower()}")
    ax.set_xlabel("Número de threads")
eixos[0].set_ylabel("Speedup (T serial / T paralelo) — escala log")
eixos[0].legend(fontsize=8, loc="upper left")
fig.suptitle(f"Speedup com n = {fmt_n(n_max)}{sub_nucleos}", fontsize=13)
salvar(fig, "2_speedup.png")

# 3) Eficiência (speedup / threads) com n máximo, dados aleatórios
fig, ax = plt.subplots(figsize=(7.5, 4.6), layout="constrained")
largura = 0.2
par = [2, 4, 8]
for i, alg in enumerate(ALGORITMOS):
    d = df[(df.algoritmo == alg) & (df.tipo_dados == "aleatorio") & (df.tamanho == n_max) & (df.threads > 1)].sort_values("threads")
    pos = [j + (i - 1.5) * largura for j in range(len(par))]
    ax.bar(pos, d.eficiencia, width=largura, label=alg)
ax.axhline(1, color="k", linestyle="--", linewidth=1, label="Eficiência ideal (1,0)")
ax.set_xticks(range(len(par)))
ax.set_xticklabels([f"{t} threads" for t in par])
ax.set_ylabel("Eficiência (speedup / threads)")
ax.set_title(f"Eficiência — dados aleatórios, n = {fmt_n(n_max)}")
ax.legend(fontsize=8)
salvar(fig, "3_eficiencia.png")

# 4) Comparação geral: serial x paralelo (maior nº de threads), n máximo, dados aleatórios
t_max = df.threads.max()
fig, ax = plt.subplots(figsize=(8, 4.8), layout="constrained")
largura = 0.38
x = range(len(ALGORITMOS))
ser = [df[(df.algoritmo == a) & (df.tipo_dados == "aleatorio") & (df.tamanho == n_max) & (df.threads == 1)].media_ms.iloc[0] for a in ALGORITMOS]
par_ = [df[(df.algoritmo == a) & (df.tipo_dados == "aleatorio") & (df.tamanho == n_max) & (df.threads == t_max)].media_ms.iloc[0] for a in ALGORITMOS]
b1 = ax.bar([i - largura / 2 for i in x], ser, largura, label="Serial", color=COR[1])
b2 = ax.bar([i + largura / 2 for i in x], par_, largura, label=f"Paralelo ({t_max} threads)", color=COR[8])
ax.bar_label(b1, fmt="%.1f", fontsize=8, padding=2)
ax.bar_label(b2, fmt="%.1f", fontsize=8, padding=2)
ax.set_yscale("log")
ax.set_xticks(list(x))
ax.set_xticklabels(ALGORITMOS)
ax.set_ylabel("Tempo médio (ms) — escala log")
ax.set_title(f"Serial x paralelo — dados aleatórios, n = {fmt_n(n_max)}")
ax.legend()
salvar(fig, "4_comparacao_geral.png")

# 5) Ponto de equilíbrio: speedup x tamanho (Merge e Quick, dados aleatórios)
fig, eixos = plt.subplots(1, 2, figsize=(11, 4.4), sharey=True, layout="constrained")
for ax, alg in zip(eixos, ["MergeSort", "QuickSort"]):
    for t in [2, 4, 8]:
        d = df[(df.algoritmo == alg) & (df.tipo_dados == "aleatorio") & (df.threads == t)].sort_values("tamanho")
        ax.plot(d.tamanho, d.speedup, marker=MARC[t], color=COR[t], linewidth=1.8, label=f"{t} threads")
    ax.axhline(1, color="k", linestyle="--", linewidth=1)
    ax.set_xscale("log")
    ax.set_xticks(sorted(df.tamanho.unique()))
    ax.xaxis.set_major_formatter(mticker.FuncFormatter(fmt_k))
    ax.minorticks_off()
    ax.set_title(alg)
    ax.set_xlabel("Tamanho da entrada (n)")
eixos[0].set_ylabel("Speedup (abaixo de 1 = paralelo mais lento)")
eixos[0].legend()
fig.suptitle("Quando o paralelismo compensa? (dados aleatórios)", fontsize=13)
salvar(fig, "5_ponto_de_equilibrio.png")

if MOSTRAR:
    print("Abrindo os gráficos... feche todas as janelas para encerrar.")
    plt.show()