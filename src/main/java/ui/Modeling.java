package ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Modeling {
    enum States {S, I, R, Rc, Ra}

    enum Strategies {Seq, Random, Contr}

    public static States[] states;

    public static Map<Integer, Integer> model(Map<String, String> params) {
        Map<Integer, Integer> avgResult = new HashMap<>();
        ArrayList<ArrayList<Integer>> res2 = new ArrayList<>();
        int sample = 100;
        int maxT = 0;
        for (int i = 0; i < sample; i++) {
            ArrayList<Integer> res = new ArrayList<>();

            if (params.get("model").equals("SIR")) {
                res = modelSIR(params);
            } else if (params.get("model").equals("SI")) {
                res = modelSI(params);
            }
            res2.add(res);
            if (res.size() > maxT) {
                maxT = res.size();
            }

        }

        for (int i = 0; i < sample; i++) {
            ArrayList<Integer> current = res2.get(i);
            for (int j = 0; j < maxT; j++) {
                int value;
                if (j < current.size()) {
                    value = current.get(j);
                } else {
                    value = current.get(current.size() - 1);
                }
                if (avgResult.get(j) == null) {
                    avgResult.put(j, value);
                } else {
                    avgResult.put(j, avgResult.get(j) + value);
                }
            }
        }


        for (Map.Entry e : avgResult.entrySet()) {
            Integer value = avgResult.get(e.getKey()) / sample;
            avgResult.put((int) e.getKey(), value);
        }

        return avgResult;
    }

    public static ArrayList<Integer> modelSI(Map<String, String> params) {
        int N = Integer.valueOf(params.get("N"));
        int beta = Integer.valueOf(params.get("beta"));
        int I0 = Integer.valueOf(params.get("I0"));
        Strategies strategy = Strategies.valueOf(params.get("wormStr"));

        states = new States[N];
        int sum = 0;
        int t = 0;
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < N; i++) {
            states[i] = States.S;
        }

        Random r = new Random();
        for (int i = 0; i < I0; i++) {
            states[r.nextInt(N)] = States.I;
        }

        while (true) {
            sum = 0;
            for (int i = 0; i < N; i++) {
                if (states[i] == States.I) {
                    sum++;
                }
            }
            result.add(sum);
            System.out.println("T: " + t++ + ", sum: " + sum);
            if (sum == N) break;
            for (int i = 0; i < sum; i++) {
                for (int j = 0; j < beta; j++) {
                    if (strategy == Strategies.Seq) {
                        for (int k = 0; k < N; k++) {
                            if (states[k] == States.S) {
                                states[k] = States.I;
                                break;
                            }
                        }
                    } else if (strategy == Strategies.Random) {
                        states[r.nextInt(N)] = States.I;
                    }
                }
            }
        }
        return result;
    }


    public static ArrayList<Integer> modelSIR(Map<String, String> params) {
        int N = Integer.valueOf(params.get("N"));
        int beta = Integer.valueOf(params.get("beta"));
        int gamma = Integer.valueOf(params.get("gamma"));
        int I0 = Integer.valueOf(params.get("I0"));
        int R0 = Integer.valueOf(params.get("R0"));
        int Rc0 = Integer.valueOf(params.get("Rc0"));
        int tR = Integer.valueOf(params.get("tR"));
        Strategies wormStrategy = Strategies.valueOf(params.get("wormStr"));
        Strategies antivirusStrategy = Strategies.valueOf(params.get("antivirusStr"));
        Strategies contrwormStrategy = Strategies.valueOf(params.get("contrwormStr"));

        states = new States[N];
        int sumI = 0, sumR = 0;
        int t = 0;
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < N; i++) {
            states[i] = States.S;
        }

        Random r = new Random();
        for (int i = 0; i < I0; i++) {
            states[r.nextInt(N)] = States.I;
        }

        for (int i = 0; i < R0; i++) {
            states[r.nextInt(N)] = States.Ra;
        }
        for (int i = 0; i < Rc0; i++) {
            states[r.nextInt(N)] = States.Rc;
        }

        while (true) {
            if (t > 200) {
                break;
            }
            sumI = 0;
            sumR = 0;
            for (int i = 0; i < N; i++) {
                if (states[i] == States.I) sumI++;
            }
            result.add(sumI);
            for (int i = 0; i < N; i++) {
                if (states[i] == States.R || states[i] == States.Rc || states[i] == States.Ra) sumR++;
            }
            System.out.println("T: " + t++ + ", sumI: " + sumI + ", sumR: " + sumR);
            if (sumI == 0) break;
            for (int i = 0; i < N; i++) {
                if (states[i] == States.S) {
                    continue;
                }
                if (states[i] == States.Rc && t >= tR) {
                    for (int j = 0; j < gamma; j++) {
                        if (contrwormStrategy == Strategies.Seq) {
                            for (int k = 0; k < N; k++) {
                                if (states[k] == States.S || states[k] == States.I) {
                                    states[k] = States.R;
                                    break;
                                }
                            }
                        } else if (contrwormStrategy == Strategies.Random) {
                            int index = r.nextInt(N);
                            if (states[index] == States.I || states[index] == States.S) {
                                states[index] = States.R;
                            }
                        }

                    }
                } else if (states[i] == States.Ra && t >= tR) {
                    for (int j = 0; j < gamma; j++) {
                        if (antivirusStrategy == Strategies.Seq) {
                            for (int k = 0; k < N; k++) {
                                if (states[k] == States.I) {
                                    states[k] = States.R;
                                    break;
                                }
                            }
                        } else {
                            int index = r.nextInt(N);
                            if (states[index] == States.I) {
                                states[index] = States.R;
                            }
                        }

                    }
                } else if (states[i] == States.I) {
                    for (int j = 0; j < beta; j++) {
                        if (wormStrategy == Strategies.Seq) {
                            for (int k = 0; k < N; k++) {
                                if (states[k] == States.S) {
                                    states[k] = States.I;
                                    break;
                                } else if (states[k] == States.Rc && contrwormStrategy == Strategies.Contr && t >= tR) {
                                    states[i] = States.R;
                                    break;
                                }
                            }
                        } else if (wormStrategy == Strategies.Random) {
                            int index = r.nextInt(N);
                            if (states[index] == States.S) {
                                states[index] = States.I;
                            } else if (states[index] == States.Rc && contrwormStrategy == Strategies.Contr && t >= tR) {
                                states[i] = States.R;
                            }
                        }
                    }
                }
            }
        }
        return result;
    }
}
