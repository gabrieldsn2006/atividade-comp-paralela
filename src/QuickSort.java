import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class QuickSort {

    static final int LIMITE = 2048;

    public static void serial(int[] v) {
        quickSort(v, 0, v.length - 1);
    }

    static void quickSort(int[] v, int ini, int fim) {
        if (ini >= fim) return;
        int p = particionar(v, ini, fim);
        quickSort(v, ini, p);
        quickSort(v, p + 1, fim);
    }

    // particao de Hoare com pivo no meio (com pivo na ponta o vetor ordenado vira O(n^2))
    static int particionar(int[] v, int ini, int fim) {
        int pivo = v[(ini + fim) / 2];
        int i = ini - 1;
        int j = fim + 1;
        while (true) {
            do { i++; } while (v[i] < pivo);
            do { j--; } while (v[j] > pivo);
            if (i >= j) return j;
            int temp = v[i];
            v[i] = v[j];
            v[j] = temp;
        }
    }

    public static void paralelo(int[] v, int numThreads) {
        ForkJoinPool pool = new ForkJoinPool(numThreads);
        pool.invoke(new QuickSortTask(v, 0, v.length - 1));
        pool.shutdown();
    }

    static class QuickSortTask extends RecursiveAction {
        int[] v;
        int ini, fim;

        QuickSortTask(int[] v, int ini, int fim) {
            this.v = v;
            this.ini = ini;
            this.fim = fim;
        }

        @Override
        protected void compute() {
            if (fim - ini < LIMITE) {
                quickSort(v, ini, fim);
                return;
            }
            int p = particionar(v, ini, fim);
            invokeAll(new QuickSortTask(v, ini, p), new QuickSortTask(v, p + 1, fim));
        }
    }
}
