package ui;

import com.byteowls.vaadin.chartjs.config.LineChartConfig;
import com.byteowls.vaadin.chartjs.data.LineDataset;
import com.vaadin.annotations.Theme;
import com.vaadin.flow.component.ClickEvent;
import com.vaadin.flow.component.ComponentEventListener;
import com.vaadin.flow.component.UI;
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
import ui.canvas.Canvas;
import ui.canvas.CanvasRenderingContext2D;

import java.util.*;

@Theme("valo")
@Route("")
public class MainUI extends HorizontalLayout {
    private static final int CANVAS_SIZE = 500;
    private static final int CHART_SIZE = 600;

    private VerticalLayout chartLayout;
    private VerticalLayout fieldsLayout;

    private Canvas canvas;
    private Canvas chartCanvas;
    private CanvasRenderingContext2D ctx;
    private CanvasRenderingContext2D chartCtx;
    private Upload upload;
    private Map<String, Map<Integer, Integer>> allResults = new HashMap<>();
    private Map<String, String> strategies = new HashMap<>();
    private Map<Integer, String> colors = new HashMap<>();

    public MainUI() {
        strategies.put("Линейный", "Seq");
        strategies.put("Случайный", "Random");
        strategies.put("Контратака", "Contr");

        chartLayout = new VerticalLayout();
        fieldsLayout = new VerticalLayout();

      /*  canvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        canvas.setId("canvas");
        ctx = canvas.getContext();
        add(canvas);*/
        add(chartLayout);
        add(fieldsLayout);
        initChart();
        initFields();
        // initResults();
        // initUpload();
        //initTopologyTypeGroup();
        UI.getCurrent().getPage().addJavaScript("https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.3/Chart.js");
    }

    private void initChart() {
        chartCanvas = new Canvas(1000, CHART_SIZE);
        chartCanvas.setId("chartCanvas");
        chartCtx = chartCanvas.getContext();
        chartLayout.add(chartCanvas);
        colors.put(0, "rgba(30,30,30,0.5)");
        colors.put(1, "rgba(248,0,18,0.5)");
        colors.put(2, "rgba(254,172,0,0.5)");
        colors.put(3, "rgba(0,190,50,0.5)");
    }


    private void initTopologyTypeGroup() {
        Label topologyLabel = new Label("Способ определения топологии");
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("Автоматически", "Эрдеша-Реньи", "Безмасштабный", "Геометрический");
        group.getElement().getStyle().set("display", "flex");
        group.getElement().getStyle().set("flexDirection", "column");
        //uploadLayout.add(topologyLabel, group);
    }

    private void initResults() {
        FormLayout formLayout = new FormLayout();
        Label label1 = new Label("Средний коэффициент кластеризации");
        Label value = new Label("1");
        Label label2 = new Label("Средний коэффициент ");
        Label value2 = new Label("2.3");

        formLayout.addFormItem(value, label1);
        formLayout.addFormItem(value2, label2);
        chartLayout.add(formLayout);
    }

    private void initFields() {
        FormLayout form = new FormLayout();

        TextField dataset = new TextField();
        dataset.setValue("Dataset 1");

        TextField vertices = new TextField();
        vertices.setValue("100");
        vertices.setLabel("Число узлов");

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
        contrwormStrategy.setItems("Линейный", "Случайный", "Контратака");
        contrwormStrategy.setValue("Линейный");

        TextField gamma = new TextField();
        gamma.setLabel("gamma");
        gamma.setValue("1");

        TextField tR = new TextField();
        tR.setLabel("Время начала работы антивируса");
        tR.setValue("5");

        Button siModel = new Button("SI");
        siModel.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                Map<String, String> params = new HashMap<>();
                params.put("model", "SI");
                params.put("N", vertices.getValue());
                params.put("I0", infected.getValue());
                params.put("beta", beta.getValue());
                params.put("wormStr", strategies.get(wormStrategy.getValue()));
                allResults.put(dataset.getValue(), Modeling.model(params));
                chartCtx.chart(getChart());
            }
        });

        Button sirModel = new Button("SIR");
        sirModel.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                Map<String, String> params = new HashMap<>();
                params.put("model", "SIR");
                params.put("N", vertices.getValue());
                params.put("I0", infected.getValue());
                params.put("beta", beta.getValue());
                params.put("wormStr", strategies.get(wormStrategy.getValue()));
                params.put("R0", antivirus.getValue());
                params.put("antivirusStr", strategies.get(antivirusStrategy.getValue()));
                params.put("Rc0", contrworm.getValue());
                params.put("contrwormStr", strategies.get(contrwormStrategy.getValue()));
                params.put("gamma", gamma.getValue());
                params.put("tR", tR.getValue());
                allResults.put(dataset.getValue(), Modeling.model(params));
                chartCtx.chart(getChart());
            }
        });

        form.add(dataset, vertices, infected, wormStrategy,
                beta, antivirus, antivirusStrategy,
                contrworm, contrwormStrategy, gamma, tR,
                siModel, sirModel);
        fieldsLayout.add(form);
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
        for(Map<Integer, Integer> map : allResults.values()) {
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

        upload.addSucceededListener(event -> {

        });

        upload.getElement().addEventListener("file-remove", new DomEventListener() {
            @Override
            public void handleEvent(DomEvent arg0) {

            }
        });
        // uploadLayout.add(upload);
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
