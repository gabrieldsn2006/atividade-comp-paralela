public class InsertionSort {

    public static void serial(int[] a) {
        sortRange(a, 0, a.length - 1);
    }

    /** Ordena a[lo..hi] (hi inclusivo). */
    public static void sortRange(int[] a, int lo, int hi) {
        for (int i = lo + 1; i <= hi; i++) {
            int chave = a[i];
            int j = i - 1;
            while (j >= lo && a[j] > chave) {
                a[j + 1] = a[j];
                j--;
            }
            a[j + 1] = chave;
        }
    }

    /** Cada thread ordena um bloco; depois os blocos são intercalados. */
    public static void paralelo(int[] a, int threads) {
        SortUtils.sortChunksInParallel(a, threads, InsertionSort::sortRange);
    }
}
