public class SelectionSort {

    public static void serial(int[] a) {
        sortRange(a, 0, a.length - 1);
    }

    /** Ordena a[lo..hi] (hi inclusivo). */
    public static void sortRange(int[] a, int lo, int hi) {
        for (int i = lo; i < hi; i++) {
            int menor = i;
            for (int j = i + 1; j <= hi; j++) {
                if (a[j] < a[menor]) menor = j;
            }
            int t = a[i]; a[i] = a[menor]; a[menor] = t;
        }
    }

    /** Cada thread ordena um bloco; depois os blocos são intercalados. */
    public static void paralelo(int[] a, int threads) {
        SortUtils.sortChunksInParallel(a, threads, SelectionSort::sortRange);
    }
}
