package ui;

import com.byteowls.vaadin.chartjs.config.BarChartConfig;
import com.byteowls.vaadin.chartjs.data.BarDataset;
import com.byteowls.vaadin.chartjs.data.Dataset;
import com.byteowls.vaadin.chartjs.data.LineDataset;
import com.byteowls.vaadin.chartjs.options.Position;
import com.vaadin.annotations.Theme;
import com.vaadin.flow.component.UI;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.radiobutton.RadioButtonGroup;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.router.Route;
import ui.canvas.Canvas;
import ui.canvas.CanvasRenderingContext2D;

import java.util.ArrayList;
import java.util.List;

@Theme("valo")
@Route("")
public class MainUI extends HorizontalLayout {
    private static final int CANVAS_SIZE = 500;
    private static final int CHART_SIZE = 300;

    private VerticalLayout chartLayout;
    private VerticalLayout uploadLayout;

    private Canvas canvas;
    private Canvas chartCanvas;
    private CanvasRenderingContext2D ctx;
    private CanvasRenderingContext2D chartCtx;
    private Upload upload;

    public MainUI() {
        chartLayout = new VerticalLayout();
        uploadLayout= new VerticalLayout();

        canvas = new Canvas(CANVAS_SIZE, CANVAS_SIZE);
        canvas.setId("ui/canvas");
        ctx = canvas.getContext();
        add(canvas);
        add(chartLayout);
        add(uploadLayout);
        initChart();
        initResults();
        initUpload();
        initTopologyTypeGroup();
        UI.getCurrent().getPage().addJavaScript("https://cdnjs.cloudflare.com/ajax/libs/Chart.js/2.7.3/Chart.js");
    }

    private void initChart() {
        chartCanvas = new Canvas(CHART_SIZE, CHART_SIZE);
        chartCanvas.setId("chartCanvas");
        chartCtx = chartCanvas.getContext();
        chartCtx.chart(getBarChart());
        chartLayout.add(chartCanvas);
    }



    private void initTopologyTypeGroup() {
        Label topologyLabel = new Label("Способ определения топологии");
        RadioButtonGroup<String> group = new RadioButtonGroup<>();
        group.setItems("Автоматически", "Эрдеша-Реньи", "Безмасштабный", "Геометрический");
        group.getElement().getStyle().set("display", "flex");
        group.getElement().getStyle().set("flexDirection", "column");
        uploadLayout.add(topologyLabel, group);
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

    private String getBarChart() {
        BarChartConfig config = new BarChartConfig();
        config
                .data()
                .labels("January", "February", "March", "April", "May", "June", "July")
                .addDataset(new BarDataset().type().label("Dataset 1").backgroundColor("rgba(151,187,205,0.5)").borderColor("white").borderWidth(2))
                .addDataset(new LineDataset().type().label("Dataset 2").backgroundColor("rgba(151,187,205,0.5)").borderColor("white").borderWidth(2))
                .addDataset(new BarDataset().type().label("Dataset 3").backgroundColor("rgba(220,220,220,0.5)"))
                .and();

        config.
                options()
                .responsive(false)
                .title()
                .display(true)
                .position(Position.TOP)
                .text("Гистограмма распределения степеней вершин")
                .and()
                .done();

        List<String> labels = config.data().getLabels();
        for (Dataset<?, ?> ds : config.data().getDatasets()) {
            List<Double> data = new ArrayList<>();
            for (int i = 0; i < labels.size(); i++) {
                data.add((double) (Math.random() > 0.5 ? 1.0 : -1.0) * Math.round(Math.random() * 100));
            }

            if (ds instanceof BarDataset) {
                BarDataset bds = (BarDataset) ds;
                bds.dataAsList(data);
            }

            if (ds instanceof LineDataset) {
                LineDataset lds = (LineDataset) ds;
                lds.dataAsList(data);
            }
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
        uploadLayout.add(upload);
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
