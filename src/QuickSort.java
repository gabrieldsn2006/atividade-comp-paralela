import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class QuickSort {

    /** Abaixo desse tamanho a tarefa paralela ordena sozinha (evita criar tarefas minúsculas). */
    private static final int LIMITE = 2048;

    public static void serial(int[] a) {
        sort(a, 0, a.length - 1);
    }

    private static void sort(int[] a, int lo, int hi) {
        if (lo >= hi) return;
        int p = particiona(a, lo, hi);
        sort(a, lo, p);
        sort(a, p + 1, hi);
    }

    /**
     * Partição de Hoare com pivô no meio. O pivô no meio evita o pior caso O(n²)
     * quando o array já está ordenado ou invertido.
     */
    private static int particiona(int[] a, int lo, int hi) {
        int pivo = a[lo + (hi - lo) / 2];
        int i = lo - 1, j = hi + 1;
        while (true) {
            do { i++; } while (a[i] < pivo);
            do { j--; } while (a[j] > pivo);
            if (i >= j) return j;
            int t = a[i]; a[i] = a[j]; a[j] = t;
        }
    }

    /** Paralelo com Fork-Join: depois de particionar, os dois lados são ordenados ao mesmo tempo. */
    public static void paralelo(int[] a, int threads) {
        ForkJoinPool pool = new ForkJoinPool(threads);
        try {
            pool.invoke(new Tarefa(a, 0, a.length - 1));
        } finally {
            pool.shutdown();
        }
    }

    private static class Tarefa extends RecursiveAction {
        private final int[] a;
        private final int lo, hi;

        Tarefa(int[] a, int lo, int hi) {
            this.a = a; this.lo = lo; this.hi = hi;
        }

        @Override
        protected void compute() {
            if (hi - lo < LIMITE) {
                sort(a, lo, hi);
                return;
            }
            int p = particiona(a, lo, hi);
            invokeAll(new Tarefa(a, lo, p), new Tarefa(a, p + 1, hi));
        }
    }
}
