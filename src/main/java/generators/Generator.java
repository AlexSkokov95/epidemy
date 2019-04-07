package generators;

public class Generator {
    protected byte[][] adjacencyMatrix;
    protected int verticesNumber;

    public Generator(int size) {
        adjacencyMatrix = new byte[size][];
        for (int i = 0; i < size; i++) {
            adjacencyMatrix[i] = new byte[size];
        }
        verticesNumber = size;
        for (int i = 0; i < verticesNumber; i++) {
            for (int j = 0; j < verticesNumber; j++) {
                adjacencyMatrix[i][j] = 0;
            }
        }
    }
}
