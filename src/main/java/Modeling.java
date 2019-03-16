import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Modeling {
    enum States {S, I, R, Rc, Ra};
    enum Strategies {Seq, Random, Contr};

    public static final int N = 100;
    public static States[] states;

    public void model() {
        Map<Integer, Integer> avgResult = new HashMap<>();
        ArrayList<ArrayList<Integer>> res2 = new ArrayList<>();
        int N = 100;
        int maxT = 0;
        for(int i = 0; i < N; i++) {
            ArrayList<Integer> res = modelSIR(1, 1, 1, 10, 0, 5, Strategies.Seq);
            //ArrayList<Integer> res = modelSI(2,1, Strategies.Random);
            res2.add(res);
            if(res.size() > maxT) {
                maxT = res.size();
            }

        }

        for(int i = 0; i < N; i++) {
            ArrayList<Integer> current = res2.get(i);
            for(int j = 0; j < maxT; j++) {
                int value;
                if(j < current.size()) {
                    value = current.get(j);
                } else {
                    value = current.get(current.size() - 1);
                }
                if(avgResult.get(j) == null) {
                    avgResult.put(j, value);
                } else {
                    avgResult.put(j, avgResult.get(j) + value);
                }
            }
        }



        for(Map.Entry e : avgResult.entrySet() ) {
            Integer value = avgResult.get(e.getKey()) / N;
            avgResult.put((int) e.getKey(), value);
        }
    }

    public static ArrayList<Integer> modelSI(int beta, int I0, Strategies strategy){
        states = new States[N];
        int sum = 0;
        int t = 0;
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < N; i++){
            states[i] = States.S;
        }

        Random r = new Random();
        for (int i = 0; i < I0; i++){
            states[r.nextInt(N)] = States.I;
        }

        while (true){
            sum = 0;
            for (int i = 0; i < N; i++) {
                if (states[i] == States.I) {
                    sum++;
                }
            }
            result.add(sum);
            System.out.println("T: " + t++ + ", sum: " + sum);
            if (sum == N) break;
            for (int i = 0; i < sum; i++){
                for (int j = 0; j < beta; j++){
                    if(strategy == Strategies.Seq) {
                        for (int k = 0; k < N; k++) {
                            if (states[k] == States.S) {
                                states[k] = States.I;
                                break;
                            }
                        }
                    } else if(strategy == Strategies.Random) {
                        states[r.nextInt(N)] = States.I;
                    }
                }
            }
        }
        return result;
    }


    public static ArrayList<Integer> modelSIR(int beta, double gamma, int I0, int R0, int Rc0, int tRc, Strategies strategy){
        states = new States[N];
        int sumI = 0, sumR = 0;
        int t = 0;
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < N; i++){
            states[i] = States.S;
        }

        Random r = new Random();
        for (int i = 0; i < I0; i++){
            states[r.nextInt(N)] = States.I;
        }

        for (int i = 0; i < R0; i++){
            states[r.nextInt(N)] = States.Ra;
        }
        for (int i = 0; i < Rc0; i++){
            states[r.nextInt(N)] = States.Rc;
        }

        while (true){
            sumI = 0;
            sumR = 0;
            for (int i = 0; i < N; i++) {
                if (states[i] == States.I) sumI++;
            }
            result.add(sumI);
            for (int i = 0; i < N; i++) {
                if (states[i] == States.R || states[i] == States.Rc || states[i] == States.Ra) sumR++;
            }
            System.out.println("T: " + t++ + ", sumI: " + sumI +  ", sumR: " + sumR);
            if (sumI == 0) break;
            for (int i = 0; i < N; i++) {
                if (states[i] == States.S) {
                    continue;
                }
                if (states[i] == States.Ra && t >= tRc) {
                    for (int j = 0; j < gamma; j++) {
                        /*for (int k = 0; k < N; k++) {
                            if (states[k] == States.I) {
                                states[k] = States.R;
                                break;
                            }
                        }*/
                        int index = r.nextInt(N);
                        if (states[index] == States.I) {
                            states[index] = States.R;
                        }

                    }
                } else if (states[i] == States.I){
                    for (int j = 0; j < beta; j++) {
                        if (strategy == Strategies.Seq) {
                            for (int k = 0; k < N; k++) {
                                if (states[k] == States.S) {
                                    states[k] = States.I;
                                    break;
                                }
                                if (states[k] == States.Rc) {
                                    states[i] = States.R;
                                }
                            }
                        } else if (strategy == Strategies.Random) {
                            int index = r.nextInt(N);
                            if (states[index] == States.S) {
                                states[index] = States.I;
                            } else if (states[index] == States.Rc) {
                                states[i] = States.Rc;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
