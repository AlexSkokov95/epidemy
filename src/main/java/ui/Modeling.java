package ui;

import java.util.*;

public class Modeling {
    enum States {S, I, R, Rc, Ra}

    enum Strategies {Seq, Random, Contr}

    public static States[] states;

    public static Map<Integer, Integer> model(Map<String, String> params, byte[][] adjacencyMatrix) {
        Map<Integer, List<Integer>> hitList = new HashMap<>();
        for(int i = 0; i < adjacencyMatrix[0].length; i++) {
            List<Integer> adjacentVertices = new ArrayList<>();
            for(int j = 0; j < adjacencyMatrix[0].length; j++) {
                if(adjacencyMatrix[i][j] == 1) {
                    adjacentVertices.add(j);
                }
            }
            hitList.put(i, adjacentVertices);
        }
        Map<Integer, Integer> avgResult = new HashMap<>();
        ArrayList<ArrayList<Integer>> res2 = new ArrayList<>();
        int sample = 100;
        int maxT = 0;
        for (int i = 0; i < sample; i++) {
            ArrayList<Integer> res = new ArrayList<>();

            if (params.get("model").equals("SIR")) {
                res = modelSIR(params, hitList);
            } else if (params.get("model").equals("SI")) {
                res = modelSI(params, hitList);
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

    public static ArrayList<Integer> modelSI(Map<String, String> params, Map<Integer, List<Integer>> hitList) {
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
            if(t == 200) break;
            if (sum == N) break;
            for (int i = 0; i < N; i++) {
                if(states[i] == States.I) {
                    for (int j = 0; j < beta; j++) {
                        List<Integer> hit = hitList.get(i);
                        System.out.println(i);
                        if (strategy == Strategies.Seq) {
                            for (int k = 0; k < hit.size(); k++) {
                                int adjacentVertex = hit.get(k);
                                System.out.print(adjacentVertex + ", ");
                                if (states[adjacentVertex] == States.S) {
                                    states[adjacentVertex] = States.I;
                                    break;
                                }
                            }
                        } else if (strategy == Strategies.Random) {
                            int adjacentVertex = r.nextInt(hit.size());
                            states[hit.get(adjacentVertex)] = States.I;
                        }
                    }
                }
            }
        }
        return result;
    }


    public static ArrayList<Integer> modelSIR(Map<String, String> params, Map<Integer, List<Integer>> hitList) {
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

                List<Integer> hit = hitList.get(i);
                if (states[i] == States.Rc && t >= tR) {
                    for (int j = 0; j < gamma; j++) {
                        if (contrwormStrategy == Strategies.Seq) {
                            for (int k = 0; k < hit.size(); k++) {
                                int adjacentVertex = hit.get(k);
                                if (states[adjacentVertex] == States.S || states[k] == States.I) {
                                    states[adjacentVertex] = States.R;
                                    break;
                                }
                            }
                        } else if (contrwormStrategy == Strategies.Random) {
                            int index = r.nextInt(hit.size());
                            if (states[hit.get(index)] == States.I || states[index] == States.S) {
                                states[hit.get(index)] = States.R;
                            }
                        }

                    }
                } else if (states[i] == States.Ra && t >= tR) {
                    for (int j = 0; j < gamma; j++) {
                        if (antivirusStrategy == Strategies.Seq) {
                            for (int k = 0; k < hit.size(); k++) {
                                int adjacentVertex = hit.get(k);
                                if (states[adjacentVertex] == States.I) {
                                    states[adjacentVertex] = States.R;
                                    break;
                                }
                            }
                        } else {
                            int index = r.nextInt(hit.size());
                            if (states[hit.get(index)] == States.I) {
                                states[hit.get(index)] = States.R;
                            }
                        }

                    }
                } else if (states[i] == States.I) {
                    for (int j = 0; j < beta; j++) {
                        if (wormStrategy == Strategies.Seq) {
                            for (int k = 0; k < hit.size(); k++) {
                                int adjacentVertex = hit.get(k);
                                if (states[adjacentVertex] == States.S) {
                                    states[adjacentVertex] = States.I;
                                    break;
                                } else if (states[adjacentVertex] == States.Rc && contrwormStrategy == Strategies.Contr && t >= tR) {
                                    states[i] = States.R;
                                    break;
                                }
                            }
                        } else if (wormStrategy == Strategies.Random) {
                            int index = r.nextInt(hit.size());
                            if (states[hit.get(index)] == States.S) {
                                states[hit.get(index)] = States.I;
                            } else if (states[hit.get(index)] == States.Rc && contrwormStrategy == Strategies.Contr && t >= tR) {
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
