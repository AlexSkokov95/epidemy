package ui;

import java.io.*;
import java.util.*;

public class Modeling {
    enum States {S, I, R, Rc, Ra}

    enum Strategies {Seq, Random, Contr}

    public static States[] states;

    public static Map<Integer, Integer> model(Map<String, String> params, byte[][] adjacencyMatrix) {
        Map<Integer, List<Integer>> hitList = new HashMap<>();
        for (int i = 0; i < adjacencyMatrix[0].length; i++) {
            List<Integer> adjacentVertices = new ArrayList<>();
            for (int j = 0; j < adjacencyMatrix[0].length; j++) {
                if (adjacencyMatrix[i][j] == 1) {
                    adjacentVertices.add(j);
                }
            }
            hitList.put(i, adjacentVertices);
        }

        Map<Integer, Integer> avgResult = new HashMap<>();
        ArrayList<ArrayList<Integer>> res2 = new ArrayList<>();
        int sample = 50;
        int maxT = 0;
        for (int i = 0; i < sample; i++) {
            ArrayList<Integer> res = new ArrayList<>();

            if (params.get("model").equals("SIR")) {
                res = modelSIR(params, hitList);
            } else if (params.get("model").equals("SI")) {
                res = modelSI(params, hitList);
            } else if (params.get("model").equals("SIR2")) {
                res = modelSIR2(params, hitList);
            }  else if (params.get("model").equals("SIS")) {
                res = modelSIS(params, hitList);
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
        Integer[] stateCodes = new Integer[N];

        states = new States[N];
        int sum = 0;
        int t = 0;
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < N; i++) {
            states[i] = States.S;
        }

        Random r = new Random();
        for (int i = 0; i < I0; i++) {
            int index = r.nextInt(N);
            states[index] = States.I;
            stateCodes[index] = getInitialCode(States.I);
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
            if (t == 200) break;
            if (sum == N) break;
            for (int i = 0; i < N; i++) {
                if (states[i] == States.I) {
                    for (int j = 0; j < beta; j++) {
                        List<Integer> hit = hitList.get(i);
                        if (strategy == Strategies.Seq) {
                            for (int k = 0; k < hit.size(); k++) {
                                int adjacentVertex = hit.get(k);
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
        writeStatesToFile(stateCodes);
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
        Integer[] stateCodes = new Integer[N];

        states = new States[N];
        int sumI = 0, sumR = 0;
        int t = 0;
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < N; i++) {
            states[i] = States.S;
        }

        Random r = new Random();
        for (int i = 0; i < I0; i++) {
            int index = r.nextInt(N);
            states[index] = States.I;
            stateCodes[index] = getInitialCode(States.I);
        }

        for (int i = 0; i < R0; i++) {
            int index = r.nextInt(N);
            states[r.nextInt(N)] = States.Ra;
            stateCodes[index] = getInitialCode(States.Ra);
        }
        for (int i = 0; i < Rc0; i++) {
            int index = r.nextInt(N);
            states[r.nextInt(N)] = States.Rc;
            stateCodes[index] = getInitialCode(States.Rc);
        }

        while (true) {

            if(t % tR == 0 ) {
                states[r.nextInt(N)] = States.Ra;
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
                if (hit.size() == 0)
                    continue;
                if (states[i] == States.Rc && t >= tR) {
                    for (int j = 0; j < gamma; j++) {
                        if (contrwormStrategy == Strategies.Seq) {
                            for (int k = 0; k < hit.size(); k++) {
                                int adjacentVertex = hit.get(k);
                                if (states[adjacentVertex] == States.S
                                        || states[adjacentVertex] == States.I) {
                                    states[adjacentVertex] = States.Rc;
                                    break;
                                }
                            }
                        } else if (contrwormStrategy == Strategies.Random) {
                            int index = hit.get(r.nextInt(hit.size()));
                            if (states[index] == States.I
                                    || states[index] == States.S) {
                                states[index] = States.Rc;
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
                                    states[i] = States.Rc;
                                    break;
                                }
                            }
                        } else if (wormStrategy == Strategies.Random) {
                            int index = hit.get(r.nextInt(hit.size()));
                            if (states[index] == States.S) {
                                states[index] = States.I;
                            } else if (states[index] == States.Rc && contrwormStrategy == Strategies.Contr && t >= tR) {
                                states[i] = States.Rc;
                            }
                        }
                    }
                }
            }
        }
        writeStatesToFile(stateCodes);
        return result;
    }

    public static ArrayList<Integer> modelSIR2(Map<String, String> params, Map<Integer, List<Integer>> hitList) {
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
        Integer[] stateCodes = new Integer[N];

        states = new States[N];
        int sumI = 0, sumR = 0;
        int t = 0;
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < N; i++) {
            states[i] = States.S;
        }

        Random r = new Random();
        for (int i = 0; i < I0; i++) {
            int index = r.nextInt(N);
            states[index] = States.I;
            stateCodes[index] = getInitialCode(States.I);
        }

        for (int i = 0; i < R0; i++) {
            int index = r.nextInt(N);
            states[r.nextInt(N)] = States.Ra;
            stateCodes[index] = getInitialCode(States.Ra);
        }
        for (int i = 0; i < Rc0; i++) {
            int index = r.nextInt(N);
            states[r.nextInt(N)] = States.Rc;
            stateCodes[index] = getInitialCode(States.Rc);
        }

        while (true) {

            if(t % tR == 0 ) {
                states[r.nextInt(N)] = States.Ra;
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
                if (hit.size() == 0)
                    continue;
                if (states[i] == States.Rc && t >= tR) {
                    for (int j = 0; j < gamma; j++) {
                        if (contrwormStrategy == Strategies.Seq) {
                            for (int k = 0; k < hit.size(); k++) {
                                int adjacentVertex = hit.get(k);
                                if (states[adjacentVertex] == States.S
                                        || states[adjacentVertex] == States.I) {
                                    states[adjacentVertex] = States.Rc;
                                    break;
                                }
                            }
                        } else if (contrwormStrategy == Strategies.Random) {
                            int index = hit.get(r.nextInt(hit.size()));
                            if (states[index] == States.I
                                    || states[index] == States.S) {
                                states[index] = States.Rc;
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
                                if (states[adjacentVertex] == States.S ||
                                        states[adjacentVertex] == States.Rc) {
                                    states[adjacentVertex] = States.I;
                                    break;
                                }
                            }
                        } else if (wormStrategy == Strategies.Random) {
                            int index = hit.get(r.nextInt(hit.size()));
                            if (states[index] == States.S ||
                                    states[index] == States.Rc) {
                                states[index] = States.I;
                            }
                        }
                    }
                }
            }
        }
        writeStatesToFile(stateCodes);
        return result;
    }

    public static ArrayList<Integer> modelSIS(Map<String, String> params, Map<Integer, List<Integer>> hitList) {
        int N = Integer.valueOf(params.get("N"));
        int beta = Integer.valueOf(params.get("beta"));
        int gamma = Integer.valueOf(params.get("gamma"));
        int I0 = Integer.valueOf(params.get("I0"));
        int Rc0 = Integer.valueOf(params.get("Rc0"));
        int tR = Integer.valueOf(params.get("tR"));
        Strategies wormStrategy = Strategies.valueOf(params.get("wormStr"));
        Strategies contrwormStrategy = Strategies.valueOf(params.get("contrwormStr"));
        Integer[] stateCodes = new Integer[N];

        states = new States[N];
        int sumI = 0, sumR = 0;
        int t = 0;
        ArrayList<Integer> result = new ArrayList<Integer>();
        for (int i = 0; i < N; i++) {
            states[i] = States.S;
        }

        Random r = new Random();
        for (int i = 0; i < I0; i++) {
            int index = r.nextInt(N);
            states[index] = States.I;
            stateCodes[index] = getInitialCode(States.I);
        }

        for (int i = 0; i < Rc0; i++) {
            int index = r.nextInt(N);
            states[r.nextInt(N)] = States.Rc;
            stateCodes[index] = getInitialCode(States.Rc);
        }

        while (true) {
            if (t > 150) {
                break;
            }

            sumI = 0;
            for (int i = 0; i < N; i++) {
                if (states[i] == States.I) sumI++;
            }
            result.add(sumI);

            System.out.println("T: " + t++ + ", sumI: " + sumI);
            if (sumI == 0) break;
            for (int i = 0; i < N; i++) {
                if (states[i] == States.S) {
                    continue;
                }

                List<Integer> hit = hitList.get(i);
                if (hit.size() == 0)
                    continue;
                if (states[i] == States.Rc && t >= tR) {
                    for (int j = 0; j < gamma; j++) {
                        if (contrwormStrategy == Strategies.Seq) {
                            for (int k = 0; k < hit.size(); k++) {
                                int adjacentVertex = hit.get(k);
                                if (states[adjacentVertex] == States.S
                                        || states[adjacentVertex] == States.I) {
                                    states[adjacentVertex] = States.Rc;
                                    break;
                                }
                            }
                        } else if (contrwormStrategy == Strategies.Random) {
                            int index = hit.get(r.nextInt(hit.size()));
                            if (states[index] == States.I
                                    || states[index] == States.S) {
                                states[index] = States.Rc;
                            }
                        }

                    }
                } else if (states[i] == States.I) {
                    for (int j = 0; j < beta; j++) {
                        if (wormStrategy == Strategies.Seq) {
                            for (int k = 0; k < hit.size(); k++) {
                                int adjacentVertex = hit.get(k);
                                if (states[adjacentVertex] == States.S ||
                                        states[adjacentVertex] == States.Rc) {
                                    states[adjacentVertex] = States.I;
                                    break;
                                }
                            }
                        } else if (wormStrategy == Strategies.Random) {
                            int index = hit.get(r.nextInt(hit.size()));
                            if (states[index] == States.S ||
                                    states[index] == States.Rc) {
                                states[index] = States.I;
                            }
                        }
                    }
                }
            }
        }
        writeStatesToFile(stateCodes);
        return result;
    }

    private static int getInitialCode(States s) {
        switch (s) {
            case I:
                return 0;
            case Ra:
            case Rc:
            default:
                return 1;
        }
    }

    private static int getCode(States s) {
        switch (s) {
            case I:
                return 2;
            case S:
                return 3;
            case Ra:
            case Rc:
            case R:
            default:
                return 4;
        }
    }

    private static void writeStatesToFile(Integer[] stateCodes) {
        try {
            FileWriter dos = new FileWriter(new File("C:\\Users\\tomak\\matrix.txt"), true);
            for (int i = 0; i < states.length; i++) {
               if(stateCodes[i] == null) {
                   stateCodes[i] = getCode(states[i]);
               }
            }

            StringBuilder s = new StringBuilder();
            for (int i = 0; i < stateCodes.length; i++) {
                s.append(stateCodes[i]);
            }
            dos.write(s.toString());
            dos.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }
}
