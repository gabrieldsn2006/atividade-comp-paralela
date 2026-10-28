public class SelectionSort {

    public static void serial(int[] v) {
        ordenar(v, 0, v.length - 1);
    }

    public static void ordenar(int[] v, int ini, int fim) {
        for (int i = ini; i < fim; i++) {
            int menor = i;
            for (int j = i + 1; j <= fim; j++) {
                if (v[j] < v[menor]) menor = j;
            }
            int temp = v[i];
            v[i] = v[menor];
            v[menor] = temp;
        }
    }

    public static void paralelo(int[] v, int numThreads) {
        SortUtils.ordenarEmBlocos(v, numThreads, SelectionSort::ordenar);
    }
}
