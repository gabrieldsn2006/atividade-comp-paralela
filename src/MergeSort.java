import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MergeSort {

    /** Abaixo desse tamanho a tarefa paralela ordena sozinha (evita criar tarefas minúsculas). */
    private static final int LIMITE = 2048;

    public static void serial(int[] a) {
        int[] tmp = new int[a.length];
        sort(a, tmp, 0, a.length - 1);
    }

    private static void sort(int[] a, int[] tmp, int lo, int hi) {
        if (lo >= hi) return;
        int mid = lo + (hi - lo) / 2;
        sort(a, tmp, lo, mid);
        sort(a, tmp, mid + 1, hi);
        SortUtils.merge(a, tmp, lo, mid, hi);
    }

    /** Paralelo com Fork-Join: as duas metades são ordenadas ao mesmo tempo. */
    public static void paralelo(int[] a, int threads) {
        int[] tmp = new int[a.length];
        ForkJoinPool pool = new ForkJoinPool(threads);
        try {
            pool.invoke(new Tarefa(a, tmp, 0, a.length - 1));
        } finally {
            pool.shutdown();
        }
    }

    private static class Tarefa extends RecursiveAction {
        private final int[] a, tmp;
        private final int lo, hi;

        Tarefa(int[] a, int[] tmp, int lo, int hi) {
            this.a = a; this.tmp = tmp; this.lo = lo; this.hi = hi;
        }

        @Override
        protected void compute() {
            if (hi - lo < LIMITE) {
                sort(a, tmp, lo, hi);
                return;
            }
            int mid = lo + (hi - lo) / 2;
            invokeAll(new Tarefa(a, tmp, lo, mid),        // fork
                      new Tarefa(a, tmp, mid + 1, hi));
            SortUtils.merge(a, tmp, lo, mid, hi);          // join
        }
    }
}
