import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

public class MergeSort {

    static final int LIMITE = 2048;

    public static void serial(int[] v) {
        int[] aux = new int[v.length];
        mergeSort(v, aux, 0, v.length - 1);
    }

    static void mergeSort(int[] v, int[] aux, int ini, int fim) {
        if (ini >= fim) return;
        int meio = (ini + fim) / 2;
        mergeSort(v, aux, ini, meio);
        mergeSort(v, aux, meio + 1, fim);
        SortUtils.merge(v, aux, ini, meio, fim);
    }

    public static void paralelo(int[] v, int numThreads) {
        int[] aux = new int[v.length];
        ForkJoinPool pool = new ForkJoinPool(numThreads);
        pool.invoke(new MergeSortTask(v, aux, 0, v.length - 1));
        pool.shutdown();
    }

    static class MergeSortTask extends RecursiveAction {
        int[] v, aux;
        int ini, fim;

        MergeSortTask(int[] v, int[] aux, int ini, int fim) {
            this.v = v;
            this.aux = aux;
            this.ini = ini;
            this.fim = fim;
        }

        @Override
        protected void compute() {
            // pedaços pequenos ordena direto, senão o custo de criar tarefas fica maior que o ganho
            if (fim - ini < LIMITE) {
                mergeSort(v, aux, ini, fim);
                return;
            }
            int meio = (ini + fim) / 2;
            invokeAll(new MergeSortTask(v, aux, ini, meio),
                      new MergeSortTask(v, aux, meio + 1, fim));
            SortUtils.merge(v, aux, ini, meio, fim);
        }
    }
}
