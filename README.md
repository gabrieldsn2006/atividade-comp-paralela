# Análise de Desempenho de Algoritmos de Ordenação Seriais e Paralelos em Java

Atividade extra de Computação Paralela e Concorrente (UNIFOR) — Gabriel e Lorenzo.

Compara as versões **serial** e **paralela** de quatro algoritmos de ordenação, variando o tamanho dos dados, o tipo dos dados e o número de threads. Os tempos são gravados em CSV.

## Como rodar

```bash
pip install pandas matplotlib # Terminal python
```
Precisa de Java 11 ou mais recente. Na pasta do projeto:

```bash
javac -d out src/*.java
java -cp out Main          # execução completa (~2–5 min, depende da máquina)
java -cp out Main teste    # execução rápida, só para conferir se tudo funciona
```

No Windows (PowerShell ou cmd), troque `src/*.java` por `src\*.java`.

Ao terminar os testes, o `Main` chama automaticamente o `graficos.py`, que salva os PNGs em `graficos/` e abre as janelas com os gráficos. Para isso é preciso ter Python com `pip install pandas matplotlib`. Para pular essa etapa: `java -cp out Main semgraficos`. Para gerar os gráficos de novo sem refazer os testes: `python graficos.py --mostrar`.

## Estrutura

| Arquivo | O que faz |
| --- | --- |
| `src/Main.java` | Roda todos os testes e grava os CSVs. A configuração fica no topo do arquivo. |
| `src/MergeSort.java` | Serial recursivo; paralelo com **Fork-Join** (as duas metades em paralelo). |
| `src/QuickSort.java` | Serial com partição de Hoare (pivô no meio); paralelo com **Fork-Join**. |
| `src/InsertionSort.java` | Serial clássico; paralelo divide o array em blocos, cada thread ordena um bloco e depois os blocos são intercalados. |
| `src/SelectionSort.java` | Mesma estratégia paralela do Insertion. |
| `src/SortUtils.java` | Intercalação (merge), divisão em blocos com `ExecutorService` e verificação de ordenação. |
| `src/DataGenerator.java` | Gera dados `aleatorio`, `ordenado` e `inverso`. |
| `graficos.py` | Lê `resultados/resumo.csv` e gera os gráficos (Python + pandas + matplotlib). Chamado automaticamente pelo `Main`. |

## Configuração dos testes

No topo de `Main.java`:

- `TAMANHOS`: 1.000, 10.000, 50.000 e 100.000 elementos
- `TIPOS`: aleatório, ordenado e inverso
- `THREADS`: 2, 4 e 8 (a execução serial é sempre feita e aparece como `threads = 1`)
- `AMOSTRAS`: 5 por configuração

Antes das amostras de cada configuração roda uma execução de aquecimento (não medida), para o JIT da JVM não distorcer a primeira medição. Depois de cada execução o programa confere se o array ficou ordenado.

## Saída

Os arquivos ficam na pasta `resultados/`:

**`resultados.csv`**: uma linha por execução.
`algoritmo, modo, threads, tamanho, tipo_dados, amostra, tempo_ms, nucleos_disponiveis`

**`resumo.csv`**: uma linha por configuração, já com a estatística.
`algoritmo, modo, threads, tamanho, tipo_dados, media_ms, desvio_padrao_ms, speedup, eficiencia`

- speedup = tempo médio serial / tempo médio paralelo
- eficiência = speedup / threads

Os CSVs usam vírgula como separador e ponto como decimal (padrão que pandas e Grafana leem direto). No Excel em português, use *Dados → De Texto/CSV* e escolha vírgula como delimitador.

## Gráficos (`graficos.py`)

O `graficos.py` transforma o `resultados/resumo.csv` em gráficos de desempenho. Ele é chamado pelo `Main` no fim da execução, mas também pode ser rodado sozinho, quantas vezes quiser, sem refazer os testes.

### Dependências

```bash
pip install pandas matplotlib
```

- **pandas** lê o CSV e filtra os dados (por algoritmo, tipo de dado, tamanho e número de threads).
- **matplotlib** desenha os gráficos. É a biblioteca padrão de gráficos do Python: cada gráfico é uma *figura* (`fig`) com um ou mais *eixos* (`ax`) onde as linhas e barras são desenhadas, e `savefig` grava a figura em PNG.

### Como rodar

```bash
python graficos.py             # só salva os PNGs em graficos/
python graficos.py --mostrar   # salva e abre as janelas dos gráficos
```

Com `--mostrar`, o script usa a janela interativa do matplotlib (dá para dar zoom, mover e salvar pela barra de ferramentas) e só termina quando todas as janelas forem fechadas. Sem essa opção, usa o modo `Agg`, que não abre janela e serve para rodar sem interface gráfica.

### O que cada gráfico mostra

| Arquivo | Conteúdo |
| --- | --- |
| `1_tempo_<Algoritmo>.png` (4 arquivos) | Tempo médio serial x paralelo (2, 4 e 8 threads) em função do tamanho da entrada. Um painel para cada tipo de dado; as barras de erro são o desvio padrão. |
| `2_speedup.png` | Speedup de cada algoritmo com o maior n, por número de threads, com a linha do speedup ideal (linear). |
| `3_eficiencia.png` | Eficiência (speedup / threads) com o maior n e dados aleatórios. Ideal = 1,0. |
| `4_comparacao_geral.png` | Serial x paralelo com o maior número de threads, lado a lado, para os 4 algoritmos. |
| `5_ponto_de_equilibrio.png` | Speedup em função de n para MergeSort e QuickSort: mostra a partir de qual tamanho o paralelo passa a compensar (speedup acima de 1). |

### Dados dos gráficos

- **Escala logarítmica:** os eixos de tempo e de speedup usam escala log, porque os valores variam muito (de décimos de ms a vários segundos). Em escala log, a mesma distância no eixo significa a mesma *proporção*, e não a mesma diferença.
- **Speedup abaixo de 1** significa que o paralelo ficou *mais lento* que o serial. A linha horizontal em 1 marca esse limite.
- **Linha tracejada preta** nos gráficos de speedup é o speedup ideal: com *p* threads, seria *p*. Ficar abaixo dela é o normal; ficar acima (Insertion e Selection) tem a explicação nas observações abaixo.
- **Barras de erro** são o desvio padrão das 5 amostras. Barras grandes indicam medições instáveis.

### Como o script funciona

1. Lê o `resumo.csv` com o pandas e descobre o número de núcleos da máquina no `resultados.csv` (para o subtítulo dos gráficos).
2. Para cada gráfico, filtra o `DataFrame` e desenha uma linha ou barra por série (algoritmo ou número de threads).
3. Salva o PNG em `graficos/` e, com `--mostrar`, mantém a figura aberta até o `plt.show()` final.

As configurações ficam no topo do arquivo: `ALGORITMOS`, `TIPOS`, `THREADS` e as cores (`COR`) e marcadores (`MARC`) de cada número de threads. Se mudarem os algoritmos, `TIPOS` ou `THREADS` no `Main.java`, atualizem essas listas também. Os tamanhos de entrada são lidos do CSV, e o eixo y do gráfico de speedup se ajusta sozinho aos dados.


## Observações para o relatório

- **Insertion e Selection paralelos mostram speedup acima do número de threads** (ex.: ~16× com 8 threads). Isso não é ganho "mágico" de paralelismo. Como esses algoritmos são O(n²), ordenar p blocos de n/p elementos custa p·(n/p)² = n²/p, ou seja, a própria divisão em blocos já reduz o trabalho. Vale explicar isso na discussão.
- **Merge e Quick com até 100.000 elementos terminam em poucos milissegundos**, e o custo de criar as tarefas e o pool de threads pode ser maior que o ganho. Para ver o efeito do paralelismo neles, aumente `TAMANHOS` (ex.: 1.000.000 ou mais), de preferência rodando só esses dois algoritmos, porque Insertion e Selection ficariam lentos demais.
- O número de núcleos da máquina limita o ganho: usar mais threads que núcleos não acelera e pode até piorar. A coluna `nucleos_disponiveis` registra quantos núcleos a máquina tinha.