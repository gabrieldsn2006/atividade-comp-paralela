import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Funções auxiliares usadas pelos algoritmos. */
public class SortUtils {

    /** Ordena o trecho a[lo..hi] (hi inclusivo). */
    public interface RangeSorter {
        void sort(int[] a, int lo, int hi);
    }

    /** Intercala os trechos já ordenados a[lo..mid] e a[mid+1..hi]. */
    public static void merge(int[] a, int[] tmp, int lo, int mid, int hi) {
        System.arraycopy(a, lo, tmp, lo, hi - lo + 1);
        int i = lo, j = mid + 1, k = lo;
        while (i <= mid && j <= hi) {
            a[k++] = (tmp[i] <= tmp[j]) ? tmp[i++] : tmp[j++];
        }
        while (i <= mid) a[k++] = tmp[i++];
        while (j <= hi) a[k++] = tmp[j++];
    }

    /**
     * Estratégia paralela para algoritmos que não são "dividir para conquistar"
     * (Insertion e Selection):
     *   1. divide o array em 'threads' blocos;
     *   2. cada thread ordena o seu bloco com o algoritmo serial;
     *   3. a thread principal intercala os blocos ordenados.
     */
    public static void sortChunksInParallel(int[] a, int threads, RangeSorter sorter) {
        int n = a.length;
        int[] inicio = new int[threads + 1];
        for (int t = 0; t <= threads; t++) {
            inicio[t] = (int) ((long) n * t / threads);
        }

        ExecutorService pool = Executors.newFixedThreadPool(threads);
        try {
            List<Callable<Void>> tarefas = new ArrayList<>();
            for (int t = 0; t < threads; t++) {
                final int lo = inicio[t];
                final int hi = inicio[t + 1] - 1;
                tarefas.add(() -> {
                    if (lo < hi) sorter.sort(a, lo, hi);
                    return null;
                });
            }
            pool.invokeAll(tarefas); // espera todas as threads terminarem
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } finally {
            pool.shutdown();
        }

        // Intercala os blocos: [0] com [1], o resultado com [2], e assim por diante.
        int[] tmp = new int[n];
        for (int t = 1; t < threads; t++) {
            int mid = inicio[t] - 1;
            int hi = inicio[t + 1] - 1;
            if (mid >= 0 && hi > mid) merge(a, tmp, 0, mid, hi);
        }
    }

    public static boolean isSorted(int[] a) {
        for (int i = 1; i < a.length; i++) {
            if (a[i - 1] > a[i]) return false;
        }
        return true;
    }
}
