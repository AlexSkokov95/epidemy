package generators;

import java.util.Random;

public class Scalefree extends Generator {
    public Scalefree(int size) {
        super(size);
    }

    // Количество соседей n-ой вершины
    public int countAdjacentVertices(int vertex) {
        int count = 0;
        for (int i = 0; i < verticesNumber; i++) {
            if ((i != vertex) && (adjacencyMatrix[vertex][i] == 1)) {
                count++;
            }
        }
        return count;
    }

    // Количество ребер во всем графе
    public int countEdges() {
        int count = 0;
        for (int i = 0; i < verticesNumber; i++) {
            count += countAdjacentVertices(i);
        }
        return count / 2;
    }

    //Поиск случайной изолированной вершины
    public int findIsolatedVertex() {
        Random r = new Random();
        int n = r.nextInt(verticesNumber);
        for (int i = 0; i < verticesNumber; i++) {
            if (countAdjacentVertices(n) == 0) {
                return n;
            }
            n = (n + 1) % verticesNumber;
        }
        return -1;
    }

    //Поиск случайной связанной вершины
    public int findRandomVertex() {
        Random r = new Random();
        int n = r.nextInt(verticesNumber);
        for (int i = 0; i < verticesNumber; i++) {
            if (countAdjacentVertices(n) != 0) {
                return n;
            }
            n = (n + 1) % verticesNumber;
        }
        return -1;
    }

    public byte[][] generate(double param) {
        System.out.println("START");
        int v1, v2, v3, v4;
        v1 = findIsolatedVertex();
        do {
            v2 = findIsolatedVertex();
        }
        while (v2 == v1);
        adjacencyMatrix[v1][v2] = 1;
        adjacencyMatrix[v2][v1] = 1;
        do {
            v3 = findIsolatedVertex();
        }
        while (v3 == v1 || v3 == v2);
        //v3 = FindIsolatedVertex();
        adjacencyMatrix[v1][v3] = 1;
        adjacencyMatrix[v3][v1] = 1;
        adjacencyMatrix[v2][v3] = 1;
        adjacencyMatrix[v3][v2] = 1;
        // v4 = FindIsolatedVertex();
        do {
            v4 = findIsolatedVertex();
        }
        while (v4 == v1);
        adjacencyMatrix[v1][v4] = 1;
        adjacencyMatrix[v4][v1] = 1;

        while (true) {
            v1 = findIsolatedVertex();
            v2 = findRandomVertex();
            if (v1 == -1) {
                break;
            }
            double p = (double) countAdjacentVertices(v2) / (double) countEdges();
            double r = (new Random()).nextDouble();
            if (r < p) {
                adjacencyMatrix[v1][v2] = 1;
                adjacencyMatrix[v2][v1] = 1;
            } else {
                adjacencyMatrix[v1][v2] = 0;
                adjacencyMatrix[v2][v1] = 0;
            }
        }
        System.out.println("FINISH");
        return adjacencyMatrix;
    }
}

