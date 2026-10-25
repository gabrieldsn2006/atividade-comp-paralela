import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class Main {

    // ===== Configuração dos testes (altere aqui) =====
    static int[] TAMANHOS = {1_000, 10_000, 50_000, 100_000};
    static String[] TIPOS = {DataGenerator.ALEATORIO, DataGenerator.ORDENADO, DataGenerator.INVERSO};
    static int[] THREADS = {2, 4, 8};
    static int AMOSTRAS = 5;
    static String PASTA_SAIDA = "resultados";

    /** Um algoritmo com sua versão serial e paralela. */
    static class Algoritmo {
        final String nome;
        final Consumer<int[]> serial;
        final BiConsumer<int[], Integer> paralelo;

        Algoritmo(String nome, Consumer<int[]> serial, BiConsumer<int[], Integer> paralelo) {
            this.nome = nome; this.serial = serial; this.paralelo = paralelo;
        }
    }

    static List<Algoritmo> ALGORITMOS = List.of(
        new Algoritmo("MergeSort", MergeSort::serial, MergeSort::paralelo),
        new Algoritmo("QuickSort", QuickSort::serial, QuickSort::paralelo),
        new Algoritmo("InsertionSort", InsertionSort::serial, InsertionSort::paralelo),
        new Algoritmo("SelectionSort", SelectionSort::serial, SelectionSort::paralelo)
    );

    public static void main(String[] args) throws IOException {
        if (args.length > 0 && args[0].equals("teste")) {
            TAMANHOS = new int[]{1_000, 5_000};
            AMOSTRAS = 2;
        }

        int nucleos = Runtime.getRuntime().availableProcessors();
        System.out.println("Núcleos disponíveis: " + nucleos);
        new File(PASTA_SAIDA).mkdirs();

        try (PrintWriter brutos = new PrintWriter(PASTA_SAIDA + "/resultados.csv", "UTF-8");
             PrintWriter resumo = new PrintWriter(PASTA_SAIDA + "/resumo.csv", "UTF-8")) {

            brutos.println("algoritmo,modo,threads,tamanho,tipo_dados,amostra,tempo_ms,nucleos_disponiveis");
            resumo.println("algoritmo,modo,threads,tamanho,tipo_dados,media_ms,desvio_padrao_ms,speedup,eficiencia");

            for (Algoritmo alg : ALGORITMOS) {
                for (int n : TAMANHOS) {
                    for (String tipo : TIPOS) {
                        // threads = 1 representa a execução serial
                        double mediaSerial = 0;
                        List<Integer> configs = new ArrayList<>();
                        configs.add(1);
                        for (int t : THREADS) configs.add(t);

                        for (int t : configs) {
                            String modo = (t == 1) ? "serial" : "paralelo";
                            double[] tempos = new double[AMOSTRAS];

                            executar(alg, t, DataGenerator.gerar(n, tipo, 0)); // aquecimento da JVM (não é medido)

                            for (int s = 0; s < AMOSTRAS; s++) {
                                int[] dados = DataGenerator.gerar(n, tipo, s + 1);
                                tempos[s] = executar(alg, t, dados);
                                brutos.printf(Locale.US, "%s,%s,%d,%d,%s,%d,%.4f,%d%n",
                                        alg.nome, modo, t, n, tipo, s + 1, tempos[s], nucleos);
                            }

                            double media = media(tempos);
                            double desvio = desvioPadrao(tempos, media);
                            if (t == 1) mediaSerial = media;
                            double speedup = mediaSerial / media;   // S = T_serial / T_paralelo
                            double eficiencia = speedup / t;        // E = S / p

                            resumo.printf(Locale.US, "%s,%s,%d,%d,%s,%.4f,%.4f,%.4f,%.4f%n",
                                    alg.nome, modo, t, n, tipo, media, desvio, speedup, eficiencia);
                            System.out.printf(Locale.US, "%-14s %-8s threads=%d n=%-8d %-10s media=%10.3f ms  speedup=%.2f%n",
                                    alg.nome, modo, t, n, tipo, media, speedup);
                        }
                        brutos.flush();
                        resumo.flush();
                    }
                }
            }
        }
        System.out.println("Pronto! Resultados em " + PASTA_SAIDA + "/resultados.csv e " + PASTA_SAIDA + "/resumo.csv");
    }

    /** Executa o algoritmo, confere se ordenou e devolve o tempo em milissegundos. */
    static double executar(Algoritmo alg, int threads, int[] dados) {
        long inicio = System.nanoTime();
        if (threads == 1) alg.serial.accept(dados);
        else alg.paralelo.accept(dados, threads);
        long fim = System.nanoTime();

        if (!SortUtils.isSorted(dados)) {
            throw new IllegalStateException(alg.nome + " não ordenou corretamente (threads=" + threads + ")");
        }
        return (fim - inicio) / 1_000_000.0;
    }

    static double media(double[] v) {
        double soma = 0;
        for (double x : v) soma += x;
        return soma / v.length;
    }

    static double desvioPadrao(double[] v, double media) {
        if (v.length < 2) return 0;
        double soma = 0;
        for (double x : v) soma += (x - media) * (x - media);
        return Math.sqrt(soma / (v.length - 1));
    }
}
