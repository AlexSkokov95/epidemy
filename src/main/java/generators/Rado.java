package generators;

import java.util.Random;

public class Rado extends Generator {
    public Rado(int size) {
        super(size);
    }

    public byte[][] generate(double param) {
        Random r = new Random();
        for (int i = 0; i < verticesNumber; i++) {
            for (int j = 0; j < verticesNumber; j++) {
                if (i != j) {
                    double p1 = r.nextDouble();
                    if (p1 <= param) {
                        adjacencyMatrix[i][j] = 1;
                        adjacencyMatrix[j][i] = 1;
                    } else {
                        adjacencyMatrix[i][j] = 0;
                        adjacencyMatrix[j][i] = 0;
                    }
                }
            }
        }
        return adjacencyMatrix;
    }
}
