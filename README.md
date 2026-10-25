# Análise de Desempenho de Algoritmos de Ordenação Seriais e Paralelos em Java

Atividade extra de Computação Paralela e Concorrente (UNIFOR) — Gabriel e Lorenzo.

Compara as versões **serial** e **paralela** de quatro algoritmos de ordenação, variando o tamanho dos dados, o tipo dos dados e o número de threads. Os tempos são gravados em CSV.

## Como rodar

Precisa de Java 11 ou mais recente. Na pasta do projeto:

```bash
javac -d out src/*.java
java -cp out Main          # execução completa (~2–5 min, depende da máquina)
java -cp out Main teste    # execução rápida, só para conferir se tudo funciona
```

No Windows (PowerShell ou cmd), troque `src/*.java` por `src\*.java`.

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

## Observações para o relatório

- **Insertion e Selection paralelos mostram speedup acima do número de threads** (ex.: ~16× com 8 threads). Isso não é ganho "mágico" de paralelismo. Como esses algoritmos são O(n²), ordenar p blocos de n/p elementos custa p·(n/p)² = n²/p, ou seja, a própria divisão em blocos já reduz o trabalho. Vale explicar isso na discussão.
- **Merge e Quick com até 100.000 elementos terminam em poucos milissegundos**, e o custo de criar as tarefas e o pool de threads pode ser maior que o ganho. Para ver o efeito do paralelismo neles, aumente `TAMANHOS` (ex.: 1.000.000 ou mais), de preferência rodando só esses dois algoritmos, porque Insertion e Selection ficariam lentos demais.
- O número de núcleos da máquina limita o ganho: usar mais threads que núcleos não acelera e pode até piorar. A coluna `nucleos_disponiveis` registra quantos núcleos a máquina tinha.
