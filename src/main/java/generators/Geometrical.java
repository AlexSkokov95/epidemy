package generators;

import java.util.Random;

public class Geometrical extends Generator {
    private double[][] coordinates;

    public Geometrical(int size) {
        super(size);

        Random r = new Random();
        coordinates = new double[verticesNumber][];
        for (int i = 0; i < verticesNumber; i++) {
            coordinates[i] = new double[2];
            coordinates[i][0] = r.nextDouble();
            coordinates[i][1] = r.nextDouble();
        }
    }

    public byte[][] generate() {
        double r = 0;
        double p = 0.3;
        for (int i = 0; i < verticesNumber; i++) {
            for (int j = 0; j < verticesNumber; j++) {
                if (i != j) {
                    r = Math.sqrt((coordinates[i][0] - coordinates[j][0]) * (coordinates[i][0] - coordinates[j][0]) +
                            (coordinates[i][1] - coordinates[j][1]) * (coordinates[i][1] - coordinates[j][1]));
                }
                if (r <= p) {
                    adjacencyMatrix[i][j] = 1;
                    adjacencyMatrix[j][i] = 1;
                } else {
                    adjacencyMatrix[i][j] = 0;
                    adjacencyMatrix[j][i] = 0;
                }
            }
        }
        return adjacencyMatrix;
    }
}

