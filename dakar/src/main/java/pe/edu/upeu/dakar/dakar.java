package pe.edu.upeu.dakar;

public class dakar {
    public static void imprimirMatriz(int[][] m) {
        for (int i = 0; i < m.length; i++) {
            for (int j = 0; j < m[0].length; j++) {
                if (m[i][j] != 0) {
                    System.out.print(m[i][j] + "\t");
                } else {
                    System.out.print(" \t");
                }
            }
            System.out.println("");
        }

    }

    public static int[][] matrizF5(int tamanho, int numI) {
        int[][] matriz = new int[tamanho][tamanho];
        for (int i = 0; i < matriz.length; i++) {
            for (int j = matriz[0].length - 1; j >= matriz[0].length - 1 - i; j--) {
                matriz[i][j] = numI;
                numI++;
            }
        }
        return matriz;
    }

    public static void main(String[] args) {

       
        // meses();
        // int [][] m={{1,2,6}, {3,4,2}, {5,6,7}};
        // imprimirMatriz(m);
        imprimirMatriz(matrizF5(6, 1));

    }
}