package ui;

import com.byteowls.vaadin.chartjs.config.LineChartConfig;
import com.byteowls.vaadin.chartjs.data.LineDataset;
import com.vaadin.annotations.Theme;
import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.radiobutton.RadioGroupVariant;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.Router;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.startup.RouteRegistry;
import generators.Generator;
import generators.Geometrical;
import generators.Rado;
import generators.Scalefree;
import sun.reflect.generics.tree.Tree;
import ui.canvas.Canvas;
import ui.canvas.CanvasRenderingContext2D;

import java.io.*;
import java.util.*;

@Theme("valo")
@Route("")
public class ModelingWindow extends HorizontalLayout {
    private static final int CANVAS_SIZE = 500;
    private static final int CHART_HEIGHT = 650;
    private static final int CHART_WIDTH = 1050;

    private VerticalLayout chartLayout;
    private VerticalLayout fieldsLayout1;
    private VerticalLayout fieldsLayout2;

    private Canvas canvas;
    private Canvas chartCanvas;
    private CanvasRenderingContext2D ctx;
    private CanvasRenderingContext2D chartCtx;
    private Upload upload;

    private Map<String, Map<Integer, Integer>> allResults = new HashMap<>();
    private Map<String, String> strategies = new HashMap<>();
    private Map<Integer, String> colors = new HashMap<>();

    private byte[][] matrix;

    public ModelingWindow() {
        strategies.put("Линейный", "Seq");
        strategies.put("Случайный", "Random");
        strategies.put("Контратака", "Contr");

        chartLayout = new VerticalLayout();

        HorizontalLayout fieldsLayout = new HorizontalLayout();
        fieldsLayout1 = new VerticalLayout();
        fieldsLayout2 = new VerticalLayout();
        fieldsLayout.add(fieldsLayout1, fieldsLayout2);

        VerticalLayout l = new VerticalLayout();
        l.add(fieldsLayout);

        add(l);
        add(chartLayout);
        add(new VerticalLayout());
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

        Div toDraw = new Div();
        toDraw.add(new RouterLink("Построить геометрический граф", MapWindow.class));

        ComboBox<String> topology = new ComboBox<>();
        topology.setItems("Геометрический", "Безмасштабный", "Эрдеша-Реньи", "Полный");
        topology.setValue("Геометрический");
        topology.setLabel("Топология графа");

        NumberField generatorParam = new NumberField("Радиус");
        generatorParam.setValue(0.5);
        generatorParam.setMin(0);
        generatorParam.setStep(0.1);
        generatorParam.setHasControls(true);

        NumberField vertices = new NumberField();
        vertices.setValue(100.0);
        vertices.setLabel("Число узлов");
        vertices.setMin(0);
        vertices.setStep(10);
        vertices.setHasControls(true);

        RadioButtonGroup<String> modelGroup = new RadioButtonGroup<>();
        modelGroup.setItems("SI", "SIR", "SIR с вакцинацией и без", "SIS");
        modelGroup.getElement().getStyle().set("display", "flex");
        modelGroup.getElement().getStyle().set("flexDirection", "column");
        modelGroup.setValue("SIR");
        modelGroup.setLabel("Модель");

        NumberField infected = new NumberField();
        infected.setValue(1.0);
        infected.setLabel("Число червей");
        infected.setMin(1);
        infected.setMax(20);
        infected.setStep(1);
        infected.setHasControls(true);

        ComboBox<String> wormStrategy = new ComboBox<>();
        wormStrategy.setItems("Линейный", "Случайный");
        wormStrategy.setValue("Линейный");
        wormStrategy.setLabel("Стратегия поиска червя");

        NumberField beta = new NumberField();
        beta.setValue(1.0);
        beta.setLabel("beta");
        beta.setMin(1);
        beta.setStep(1);
        beta.setHasControls(true);

        NumberField antivirus = new NumberField();
        antivirus.setValue(5.0);
        antivirus.setLabel("Число антивирусов");
        antivirus.setMin(0);
        antivirus.setMax(20);
        antivirus.setStep(1);
        antivirus.setHasControls(true);

        ComboBox<String> antivirusStrategy = new ComboBox<>();
        antivirusStrategy.setItems("Линейный", "Случайный");
        antivirusStrategy.setValue("Линейный");
        antivirusStrategy.setLabel("Стратегия поиска антивируса");

        NumberField contrworm = new NumberField();
        contrworm.setValue(0.0);
        contrworm.setLabel("Число контрчервей");
        contrworm.setMin(0);
        contrworm.setMax(20);
        contrworm.setStep(1);
        contrworm.setHasControls(true);

        ComboBox<String> contrwormStrategy = new ComboBox<>();
        contrwormStrategy.setLabel("Стратегия поиска контрчервя");
        contrwormStrategy.setItems("Линейный", "Случайный", "Контратака");
        contrwormStrategy.setValue("Линейный");

        NumberField gamma = new NumberField("gamma");
        gamma.setValue(1.0);
        gamma.setMin(1);
        gamma.setStep(1);
        gamma.setHasControls(true);

        NumberField tR = new NumberField("Время начала работы");
        tR.setValue(5.0);
        tR.setMin(0);
        tR.setStep(1);
        tR.setHasControls(true);

        group.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<RadioButtonGroup<String>, String>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<RadioButtonGroup<String>, String> radioButtonGroupStringComponentValueChangeEvent) {
                if (group.getValue().equals("Генерировать")) {
                    if(topology.getValue().equals("Геометрический") || topology.getValue().equals("Эрдеша-Реньи")) {
                        generatorParam.setVisible(true);
                    }
                    topology.setVisible(true);
                    upload.setVisible(false);
                } else {
                    topology.setVisible(false);
                    upload.setVisible(true);
                    generatorParam.setVisible(false);
                }
            }
        });

        topology.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<String>, String>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<ComboBox<String>, String> comboBoxStringComponentValueChangeEvent) {
                if(topology.getValue().equals("Геометрический")) {
                    generatorParam.setVisible(true);
                    generatorParam.setLabel("Радиус");
                } else if(topology.getValue().equals("Эрдеша-Реньи")) {
                    generatorParam.setVisible(true);
                    generatorParam.setLabel("Вероятность соединения");
                } else {
                    generatorParam.setVisible(false);
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
        modelBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        modelBtn.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                chartLayout.remove(chartCanvas);
                initChart();
                if(group.getValue().equals("Генерировать")) {
                    matrix = generateGraph(topology.getValue(),
                            vertices.getValue().intValue(), generatorParam.getValue());
                    writeToFile(matrix);
                } else if(((MemoryBuffer)upload.getReceiver()).getFileData() == null) {
                    Notification.show("Загрузите файл с матрицей смежности графа",5000, Notification.Position.MIDDLE);
                    return;
                }
                vertices.setValue((double)matrix.length);
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
                params.put("I0", String.valueOf(infected.getValue().intValue()));
                params.put("N", String.valueOf(matrix.length));
                params.put("beta", String.valueOf(beta.getValue().intValue()));
                params.put("wormStr", strategies.get(wormStrategy.getValue()));
                params.put("R0", String.valueOf(antivirus.getValue().intValue()));
                params.put("antivirusStr", strategies.get(antivirusStrategy.getValue()));
                params.put("Rc0", String.valueOf(contrworm.getValue().intValue()));
                params.put("contrwormStr", strategies.get(contrwormStrategy.getValue()));
                params.put("gamma", String.valueOf(gamma.getValue().intValue()));
                params.put("tR", String.valueOf(tR.getValue().intValue()));

                TreeMap<Integer, Integer> avgResult = Modeling.model(params, matrix);
                System.out.println(String.format("%s: T = %s, sumI = %s", dataset.getValue(),
                        avgResult.lastEntry().getKey(), avgResult.lastEntry().getValue()));
                allResults.put(dataset.getValue(), avgResult);
                chartCtx.chart(getChart());
            }
        });


        fieldsLayout1.add(dataset, group, toDraw, upload, topology, generatorParam, vertices,
                modelGroup, modelBtn);
        fieldsLayout2.add(infected, wormStrategy,
                beta, antivirus, antivirusStrategy,
                contrworm, contrwormStrategy, gamma, tR);
    }

    private void writeToFile(byte[][] matrix) {
        try {
            PrintWriter dos = new PrintWriter(new FileWriter(new File("C:\\Users\\User\\matrix.txt")));
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

    private byte[][] generateGraph(String topology, int size, double param) {
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
        return g.generate(param);
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
        upload.setDropLabel(new Label("Перетащите файл"));
        upload.setUploadButton(new Button("Загрузить"));
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
}
