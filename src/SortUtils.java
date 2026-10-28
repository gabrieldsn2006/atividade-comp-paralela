import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SortUtils {

    interface Ordenador {
        void ordenar(int[] v, int ini, int fim);
    }

    public static void merge(int[] v, int[] aux, int ini, int meio, int fim) {
        for (int k = ini; k <= fim; k++) {
            aux[k] = v[k];
        }
        int i = ini, j = meio + 1, k = ini;
        while (i <= meio && j <= fim) {
            if (aux[i] <= aux[j]) v[k++] = aux[i++];
            else v[k++] = aux[j++];
        }
        while (i <= meio) v[k++] = aux[i++];
        while (j <= fim) v[k++] = aux[j++];
    }

    // usado no Insertion e no Selection: cada thread ordena um bloco do vetor
    // e no final a main junta os blocos com merge
    public static void ordenarEmBlocos(int[] v, int numThreads, Ordenador alg) {
        int tamBloco = v.length / numThreads;
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            int ini = i * tamBloco;
            int fim = (i == numThreads - 1) ? v.length - 1 : (i + 1) * tamBloco - 1;
            executor.execute(() -> alg.ordenar(v, ini, fim));
        }

        executor.shutdown();
        try {
            executor.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        int[] aux = new int[v.length];
        for (int i = 1; i < numThreads; i++) {
            int meio = i * tamBloco - 1;
            int fim = (i == numThreads - 1) ? v.length - 1 : (i + 1) * tamBloco - 1;
            merge(v, aux, 0, meio, fim);
        }
    }

    public static boolean estaOrdenado(int[] v) {
        for (int i = 1; i < v.length; i++) {
            if (v[i - 1] > v[i]) return false;
        }
        return true;
    }
}
