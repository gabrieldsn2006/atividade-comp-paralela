public class InsertionSort {

    public static void serial(int[] v) {
        ordenar(v, 0, v.length - 1);
    }

    public static void ordenar(int[] v, int ini, int fim) {
        for (int i = ini + 1; i <= fim; i++) {
            int chave = v[i];
            int j = i - 1;
            while (j >= ini && v[j] > chave) {
                v[j + 1] = v[j];
                j--;
            }
            v[j + 1] = chave;
        }
    }

    public static void paralelo(int[] v, int numThreads) {
        SortUtils.ordenarEmBlocos(v, numThreads, InsertionSort::ordenar);
    }
}
