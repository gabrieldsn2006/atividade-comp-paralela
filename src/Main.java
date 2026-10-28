import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Locale;

public class Main {

    static String[] algoritmos = {"MergeSort", "QuickSort", "InsertionSort", "SelectionSort"};
    static int[] tamanhos = {1000, 10000, 50000, 100000};
    static String[] tipos = {"aleatorio", "ordenado", "inverso"};
    static int[] threads = {1, 2, 4, 8}; // 1 = serial
    static int amostras = 5;

    public static void main(String[] args) throws IOException {
        // "java Main teste" roda uma versao rapida so pra ver se ta funcionando
        if (args.length > 0 && args[0].equals("teste")) {
            tamanhos = new int[]{1000, 5000};
            amostras = 2;
        }

        Locale.setDefault(Locale.US); // ponto como separador decimal no csv
        int nucleos = Runtime.getRuntime().availableProcessors();
        System.out.println("Nucleos disponiveis: " + nucleos);

        new File("resultados").mkdirs();
        PrintWriter csv = new PrintWriter(new FileWriter("resultados/resultados.csv"));
        PrintWriter resumo = new PrintWriter(new FileWriter("resultados/resumo.csv"));
        csv.println("algoritmo,modo,threads,tamanho,tipo_dados,amostra,tempo_ms,nucleos_disponiveis");
        resumo.println("algoritmo,modo,threads,tamanho,tipo_dados,media_ms,desvio_padrao_ms,speedup,eficiencia");

        for (String alg : algoritmos) {
            for (int n : tamanhos) {
                for (String tipo : tipos) {
                    double mediaSerial = 0;

                    for (int t : threads) {
                        String modo = (t == 1) ? "serial" : "paralelo";

                        // aquecimento da JVM, esse tempo nao entra no resultado
                        rodar(alg, DataGenerator.gerar(n, tipo, 0), t);

                        double[] tempos = new double[amostras];
                        for (int s = 0; s < amostras; s++) {
                            int[] v = DataGenerator.gerar(n, tipo, s + 1);
                            tempos[s] = rodar(alg, v, t);
                            csv.printf("%s,%s,%d,%d,%s,%d,%.4f,%d%n", alg, modo, t, n, tipo, s + 1, tempos[s], nucleos);
                        }

                        double media = media(tempos);
                        double desvio = desvioPadrao(tempos, media);
                        if (t == 1) mediaSerial = media;
                        double speedup = mediaSerial / media;
                        double eficiencia = speedup / t;

                        resumo.printf("%s,%s,%d,%d,%s,%.4f,%.4f,%.4f,%.4f%n", alg, modo, t, n, tipo, media, desvio, speedup, eficiencia);
                        System.out.printf("%s %s t=%d n=%d %s -> %.3f ms (speedup %.2f)%n", alg, modo, t, n, tipo, media, speedup);
                    }
                    csv.flush();
                    resumo.flush();
                }
            }
        }

        csv.close();
        resumo.close();
        System.out.println("Fim. Resultados na pasta resultados/");
    }

    // ordena e devolve o tempo em ms
    static double rodar(String alg, int[] v, int t) {
        long inicio = System.nanoTime();

        switch (alg) {
            case "MergeSort":
                if (t == 1) MergeSort.serial(v); else MergeSort.paralelo(v, t);
                break;
            case "QuickSort":
                if (t == 1) QuickSort.serial(v); else QuickSort.paralelo(v, t);
                break;
            case "InsertionSort":
                if (t == 1) InsertionSort.serial(v); else InsertionSort.paralelo(v, t);
                break;
            case "SelectionSort":
                if (t == 1) SelectionSort.serial(v); else SelectionSort.paralelo(v, t);
                break;
        }

        long fim = System.nanoTime();

        if (!SortUtils.estaOrdenado(v)) {
            throw new RuntimeException("Erro: " + alg + " nao ordenou (threads = " + t + ")");
        }
        return (fim - inicio) / 1_000_000.0;
    }

    static double media(double[] x) {
        double soma = 0;
        for (double d : x) soma += d;
        return soma / x.length;
    }

    static double desvioPadrao(double[] x, double media) {
        double soma = 0;
        for (double d : x) soma += (d - media) * (d - media);
        return Math.sqrt(soma / (x.length - 1));
    }
}
