import java.util.Random;

/** Gera os conjuntos de dados de entrada. */
public class DataGenerator {

    public static final String ALEATORIO = "aleatorio";
    public static final String ORDENADO = "ordenado";
    public static final String INVERSO = "inverso";

    public static int[] gerar(int n, String tipo, long semente) {
        int[] a = new int[n];
        switch (tipo) {
            case ALEATORIO:
                Random r = new Random(semente);
                for (int i = 0; i < n; i++) a[i] = r.nextInt(n * 10);
                break;
            case ORDENADO:
                for (int i = 0; i < n; i++) a[i] = i;
                break;
            case INVERSO:
                for (int i = 0; i < n; i++) a[i] = n - i;
                break;
            default:
                throw new IllegalArgumentException("Tipo de dado desconhecido: " + tipo);
        }
        return a;
    }
}
