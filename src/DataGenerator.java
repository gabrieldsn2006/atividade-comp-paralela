import java.util.Random;

public class DataGenerator {

    public static int[] gerar(int n, String tipo, long semente) {
        int[] v = new int[n];
        Random rand = new Random(semente);
        for (int i = 0; i < n; i++) {
            if (tipo.equals("aleatorio")) v[i] = rand.nextInt(n * 10);
            else if (tipo.equals("ordenado")) v[i] = i;
            else v[i] = n - i; // inverso
        }
        return v;
    }
}
