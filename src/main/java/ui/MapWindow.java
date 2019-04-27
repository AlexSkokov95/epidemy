package ui;

import com.byteowls.vaadin.chartjs.config.LineChartConfig;
import com.byteowls.vaadin.chartjs.data.LineDataset;
import com.vaadin.annotations.Theme;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.router.Route;
import generators.Generator;
import generators.Geometrical;
import generators.Rado;
import generators.Scalefree;
import ui.canvas.Canvas;
import ui.canvas.CanvasRenderingContext2D;

import java.io.*;
import java.util.*;

@Theme("valo")
@Route("")
public class MapWindow extends HorizontalLayout {
    private static final int CANVAS_SIZE = 500;
    private static final int CHART_HEIGHT = 650;
    private static final int CHART_WIDTH = 1050;

    private VerticalLayout chartLayout;
    private FormLayout fieldsLayout1;
    private FormLayout fieldsLayout2;

    private Canvas canvas;
    private Canvas chartCanvas;
    private CanvasRenderingContext2D ctx;
    private CanvasRenderingContext2D chartCtx;
    private Upload upload;

    private Map<String, Map<Integer, Integer>> allResults = new HashMap<>();
    private Map<String, String> strategies = new HashMap<>();
    private Map<Integer, String> colors = new HashMap<>();

    private byte[][] matrix;

    public MapWindow() {
        strategies.put("Линейный", "Seq");
        strategies.put("Случайный", "Random");
        strategies.put("Контратака", "Contr");

        chartLayout = new VerticalLayout();
        fieldsLayout1 = new FormLayout();
        fieldsLayout2 = new FormLayout();


        add(fieldsLayout1);
        add(fieldsLayout2);
        add(chartLayout);
        initUpload();
        initChart();
        initFields();
        UI.getCurrent().getPage().addJavaScript("https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.3/Chart.js");
    }

    private void initChart() {
        chartCanvas = new Canvas(CHART_WIDTH, CHART_HEIGHT);
        chartCanvas.setId("chartCanvas");
        chartCtx = chartCanvas.getContext();
        chartLayout.add(chartCanvas);
        colors.put(0, "rgba(30,30,30,0.5)");
        colors.put(1, "rgba(248,0,18,0.5)");
        colors.put(2, "rgba(254,172,0,0.5)");
        colors.put(3, "rgba(0,190,50,0.5)");
    }

    private void initFields() {

        TextField dataset = new TextField("Название");
        dataset.setValue("Dataset 1");

        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("Генерировать", "Загрузить из файла");
        group.getElement().getStyle().set("display", "flex");
        group.getElement().getStyle().set("flexDirection", "column");
        group.setValue("Генерировать");

        ComboBox<String> topology = new ComboBox<>();
        topology.setItems("Геометрический", "Безмасштабный", "Эрдеша-Реньи", "Полный");
        topology.setValue("Геометрический");
        topology.setLabel("Топология графа");

        TextField vertices = new TextField();
        vertices.setValue("100");
        vertices.setLabel("Число узлов");

        Label modelGroupName = new Label("Модель");
        RadioButtonGroup<String> modelGroup = new RadioButtonGroup<>();
        modelGroup.setItems("SI", "SIR", "SIR с вакцинацией и без", "SIS");
        modelGroup.getElement().getStyle().set("display", "flex");
        modelGroup.getElement().getStyle().set("flexDirection", "column");
        modelGroup.setValue("SIR");

        TextField infected = new TextField();
        infected.setValue("1");
        infected.setLabel("Число червей");

        ComboBox<String> wormStrategy = new ComboBox<>();
        wormStrategy.setItems("Линейный", "Случайный");
        wormStrategy.setValue("Линейный");
        wormStrategy.setLabel("Стратегия поиска червя");

        TextField beta = new TextField();
        beta.setValue("1");
        beta.setLabel("beta");

        TextField antivirus = new TextField();
        antivirus.setValue("5");
        antivirus.setLabel("Число антивирусов");

        ComboBox<String> antivirusStrategy = new ComboBox<>();
        antivirusStrategy.setItems("Линейный", "Случайный");
        antivirusStrategy.setValue("Линейный");
        antivirusStrategy.setLabel("Стратегия поиска антивируса");

        TextField contrworm = new TextField();
        contrworm.setValue("0");
        contrworm.setLabel("Число контрчервей");

        ComboBox<String> contrwormStrategy = new ComboBox<>();
        contrwormStrategy.setLabel("Стратегия поиска контрчервя");
        contrwormStrategy.setItems("Линейный", "Случайный", "Контратака");
        contrwormStrategy.setValue("Линейный");

        TextField gamma = new TextField("gamma");
        gamma.setValue("1");

        TextField tR = new TextField("Время начала работы");
        tR.setValue("5");

        group.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<RadioButtonGroup<String>, String>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<String>, String> radioButtonGroupStringComponentValueChangeEvent) {
                if (group.getValue().equals("Генерировать")) {
                    topology.setVisible(true);
                    upload.setVisible(false);
                } else {
                    topology.setVisible(false);
                    vertices.setValue("");
                    upload.setVisible(true);
                }
            }
        });

        modelGroup.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<RadioButtonGroup<String>, String>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<String>, String> radioButtonGroupStringComponentValueChangeEvent) {
                if (modelGroup.getValue().equals("SI")) {
                    antivirus.setVisible(false);
                    antivirusStrategy.setVisible(false);
                    contrworm.setVisible(false);
                    contrwormStrategy.setVisible(false);
                    gamma.setVisible(false);
                    tR.setVisible(false);
                } else if (modelGroup.getValue().equals("SIS")) {
                    antivirus.setVisible(false);
                    antivirusStrategy.setVisible(false);
                    contrworm.setVisible(true);
                    contrwormStrategy.setVisible(true);
                    gamma.setVisible(true);
                    tR.setVisible(true);
                } else {
                    antivirus.setVisible(true);
                    antivirusStrategy.setVisible(true);
                    contrworm.setVisible(true);
                    contrwormStrategy.setVisible(true);
                    gamma.setVisible(true);
                    tR.setVisible(true);
                }
            }
        });


        Button modelBtn = new Button("Моделировать");
        modelBtn.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                if(group.getValue().equals("Генерировать")) {
                    matrix = generateGraph(topology.getValue(), Integer.valueOf(vertices.getValue()));
                    writeToFile(matrix);
                }
                vertices.setValue(String.valueOf(matrix.length));
                Map<String, String> params = new HashMap<>();
                switch (modelGroup.getValue()) {
                    case "SI":
                        params.put("model", "SI");
                        break;
                    case "SIR":
                        params.put("model", "SIR");
                        break;
                    case "SIS":
                        params.put("model", "SIS");
                        break;
                    case "SIR с вакцинацией и без":
                        params.put("model", "SIR2");
                        break;
                }
                params.put("I0", infected.getValue());
                params.put("N", String.valueOf(matrix.length));
                params.put("beta", beta.getValue());
                params.put("wormStr", strategies.get(wormStrategy.getValue()));
                params.put("R0", antivirus.getValue());
                params.put("antivirusStr", strategies.get(antivirusStrategy.getValue()));
                params.put("Rc0", contrworm.getValue());
                params.put("contrwormStr", strategies.get(contrwormStrategy.getValue()));
                params.put("gamma", gamma.getValue());
                params.put("tR", tR.getValue());

                allResults.put(dataset.getValue(), Modeling.model(params, matrix));
                chartCtx.chart(getChart());
            }
        });


        fieldsLayout1.add(dataset, group, upload, topology, vertices,
                modelGroupName, modelGroup, modelBtn);
        fieldsLayout2.add(infected, wormStrategy,
                beta, antivirus, antivirusStrategy,
                contrworm, contrwormStrategy, gamma, tR);
    }

    private void writeToFile(byte[][] matrix) {
        try {
            PrintWriter dos = new PrintWriter(new FileWriter(new File("C:\\Users\\tomak\\matrix.txt")));
            for (int i = 0; i < matrix[0].length; i++) {
                StringBuilder line = new StringBuilder();
                for (int j = 0; j < matrix[i].length; j++) {
                    line.append(matrix[i][j]);
                }
                dos.println(line.toString());
            }
            dos.close();
        } catch (FileNotFoundException e) {
            System.out.println("File not found");
        } catch (IOException e) {
            System.out.println("IOException");
        }
    }

    private byte[][] generateGraph(String topology, int size) {
        Generator g;
        if (topology.equals("Геометрический")) {
            g = new Geometrical(size);
        } else if (topology.equals("Безмасштабный")) {
            g = new Scalefree(size);
        } else if (topology.equals("Эрдеша-Реньи")){
            g = new Rado(size);
        } else {
            g = new Generator(size);
        }
        return g.generate();
    }

    private String getChart() {
        List<String> labels = new ArrayList<>();

        int maxLabels = 0;
        for (Map map : allResults.values()) {
            if (map.keySet().size() > maxLabels) {
                maxLabels = map.keySet().size();
            }
        }
        for (int i = 0; i < maxLabels; i++) {
            if (maxLabels < 100 || i % 4 == 0) {
                labels.add(String.valueOf(i));
            } else {
                labels.add("");
            }

        }

        LineChartConfig config = new LineChartConfig();
        config
                .data()
                .labelsAsList(labels)
                .and();

        int color = 0;
        for (Map.Entry<String, Map<Integer, Integer>> entry : allResults.entrySet()) {
            config.data().addDataset(new LineDataset().type().label(entry.getKey()).borderColor(colors.get(color)).borderWidth(3))
                    .and();
            System.out.println(colors.get(color++));
        }

        config.
                options()
                .responsive(false)
                .done();

        int datasetIndex = 0;
        for (Map<Integer, Integer> map : allResults.values()) {
            List<Double> data = new ArrayList<>();
            for (Integer value : map.values()) {
                data.add((double) value);
            }

            LineDataset dataset = (LineDataset) config.data().getDatasetAtIndex(datasetIndex);
            dataset.fill(false);
            dataset.pointRadius(1);
            dataset.dataAsList(data);

            datasetIndex++;
        }

        return config.buildJson().toJson();
    }

    private void initUpload() {
        MemoryBuffer buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.setDropLabel(new Label("Drop"));
        upload.setUploadButton(new Button("Upload"));
        upload.setVisible(false);

        upload.addSucceededListener(event -> {
            Object[] result = null;
            try (BufferedReader br = new BufferedReader(new InputStreamReader(buffer.getInputStream()))) {
                result = br.lines().toArray();
            } catch (IOException e) {
                System.out.println("IO exception");
            }

            matrix = new byte[result.length][];
            for (int i = 0; i < result.length; i++) {
                String line = (String) result[i];
                matrix[i] = new byte[line.length()];
                for (int j = 0; j < line.length(); j++) {
                    byte value = Byte.valueOf(String.valueOf(line.charAt(j)));
                    matrix[i][j] = value;
                }
            }
        });

        upload.getElement().addEventListener("file-remove", new DomEventListener() {
            @Override
            public void handleEvent(DomEvent arg0) {

            }
        });

    }

    protected class Result {
        private String name;
        private double value;

        public Result(String name, double value) {
            this.name = name;
            this.value = value;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }
}