package ui;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.textfield.NumberField;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.router.Route;
import com.vaadin.flow.router.RouterLink;
import com.vaadin.flow.server.StreamResource;
import com.vaadin.flow.theme.Theme;
import com.vaadin.flow.theme.lumo.Lumo;
import ui.canvas.Canvas;
import ui.canvas.CanvasRenderingContext2D;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


@Theme(value = Lumo.class)
@Route("draw")
public class MapWindow extends HorizontalLayout {
    private static final int WIDTH = 600;
    private static final int HEIGHT = 600;
    private static final int OFFSET_X = 832;
    private static final int OFFSET_Y = 16;

    private Button modelBtn;
    private Button writeBtn;
    private Canvas canvas;
    private Upload upload;
    private int gridSize = 20;
    private int density = 1;
    CanvasRenderingContext2D ctx;
    private VerticalLayout layout;

    NumberField dots1Field;
    NumberField dots2Field;
    NumberField dots3Field;

    NumberField radiusField;

    private Segment[][] segments;
    private List<Dot> dots;
    private int[][] matrix;

    public MapWindow() {

        layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setMargin(true);

        initCanvas();
        VerticalLayout canvasLayout = new VerticalLayout();
        canvasLayout.add(canvas);
        add(layout);
        add(canvasLayout);
        add(new VerticalLayout());

        Div toModel = new Div();
        toModel.add(new RouterLink("Назад", ModelingWindow.class));
        layout.add(toModel);

        initUpload();
        layout.add(upload);

        initGridSize();
        initDensityField();
        initDotsFields();

        initSegments();

        HorizontalLayout buttonsLayout = new HorizontalLayout();

        modelBtn = new Button("Моделировать");
        modelBtn.addThemeVariants(ButtonVariant.LUMO_PRIMARY);
        writeBtn = new Button("Сохранить");

        buttonsLayout.add(modelBtn, writeBtn);
        layout.add(buttonsLayout);

        writeBtn.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
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
        });

        modelBtn.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                dots = new ArrayList<>();
                int quantityDots;
                int varRandom;
                int varSize = 0;
                Random r = new Random();
                int dots1 = dots1Field.getValue().intValue();
                int dots2 = dots2Field.getValue().intValue();
                int dots3 = dots3Field.getValue().intValue();

                for (int i = 0; i < gridSize; i++) {
                    for (int j = 0; j < gridSize; j++) {
                        if (segments[i][j].getDensity() == 0) {
                            quantityDots = 0;
                        } else if (segments[i][j].getDensity() == 1) {
                            quantityDots = dots1;
                        } else if (segments[i][j].getDensity() == 2) {
                            quantityDots = dots2;
                        } else quantityDots = dots3;

                        if (gridSize == 10) {
                            varRandom = 60;
                            varSize = 5;
                        } else if (gridSize == 20) {
                            varRandom = 30;
                            varSize = 4;
                        } else {
                            varRandom = 20;
                            varSize = 3;
                        }
                        for (int k = 0; k < quantityDots; k++) {
                            int X = segments[i][j].getX() + OFFSET_X + r.nextInt(varRandom);
                            int Y = segments[i][j].getY() + OFFSET_Y + r.nextInt(varRandom);
                            Dot d = new Dot(X, Y);
                            dots.add(d);
                        }
                    }
                }
                matrix = fillMatrix();
                drawDots(varSize);
            }

        });
    }

    private void drawDots(int varSize) {
        for (Dot d : dots) {
            drawCircle(d.getX(), d.getY(), varSize, "rgb(255, 0, 0)");
        }
    }

    private int[][] fillMatrix() {
        double R = Double.valueOf(radiusField.getValue());
        int[][] matrix;

        matrix = new int[dots.size()][];
        for (int i = 0; i < dots.size(); i++) {
            matrix[i] = new int[dots.size()];
            for (int j = 0; j < dots.size(); j++) {
                matrix[i][j] = 0;
            }
        }
        for (int i = 0; i < dots.size(); i++) {
            for (int j = 0; j < dots.size(); j++) {
                double dist = Math.sqrt((dots.get(i).getX() - dots.get(j).getX()) *
                        (dots.get(i).getX() - dots.get(j).getX()) +
                        (dots.get(i).getY() - dots.get(j).getY()) *
                                (dots.get(i).getY() - dots.get(j).getY()));

                if (i != j && dist < R) {
                    matrix[i][j] = 1;
                    matrix[j][i] = 1;
                    drawLine(dots.get(i), dots.get(j), "rgb(150, 150, 150)");
                }
            }
        }
        return matrix;
    }

    private void initCanvas() {
        canvas = new Canvas(WIDTH, HEIGHT);
        canvas.getElement().setAttribute("id", "canvas");
        ctx = canvas.getContext();

        canvas.addClickListener(new ComponentEventListener<ClickEvent<org.vaadin.pekkam.Canvas>>() {
            @Override
            public void onComponentEvent(ClickEvent clickEvent) {
                double x = clickEvent.getClientX() - OFFSET_X;
                double y = clickEvent.getClientY() - OFFSET_Y;

                System.out.println("x: " + x + ", y: " + y);

                int width = WIDTH / gridSize;
                int height = HEIGHT / gridSize;

                int newDensity = density;

                for (int j = 1; j <= gridSize; j++) {
                    if (x <= (width * j)) {
                        j--;
                        for (int i = 1; i <= gridSize; i++) {
                            if (y <= (height * i)) {
                                i--;
                                Segment seg = segments[i][j];

                                setDensity(seg, newDensity);
                                newDensity--;
                                if (newDensity > 0) {
                                    if (i > 0) setDensity(segments[i - 1][j], newDensity);
                                    if (i < gridSize - 1) setDensity(segments[i + 1][j], newDensity);
                                    if (j > 0) setDensity(segments[i][j - 1], newDensity);
                                    if (j < gridSize - 1) setDensity(segments[i][j + 1], newDensity);
                                    if (i > 0 && j > 0) setDensity(segments[i - 1][j - 1], newDensity);
                                    if (i < gridSize - 1 && j < gridSize - 1)
                                        setDensity(segments[i + 1][j + 1], newDensity);
                                    if (i > 0 && j < gridSize - 1) setDensity(segments[i - 1][j + 1], newDensity);
                                    if (i < gridSize - 1 && j > 0) setDensity(segments[i + 1][j - 1], newDensity);
                                }
                                newDensity--;
                                if (newDensity > 0) {
                                    if (i > 1) setDensity(segments[i - 2][j], newDensity);
                                    if (i < gridSize - 2) setDensity(segments[i + 2][j], newDensity);
                                    if (i > 1 && j < gridSize - 1) setDensity(segments[i - 2][j + 1], newDensity);
                                    if (i < gridSize - 2 && j < gridSize - 1)
                                        setDensity(segments[i + 2][j + 1], newDensity);
                                    if (i > 1 && j > 0) setDensity(segments[i - 2][j - 1], newDensity);
                                    if (i < gridSize - 2 && j > 0) setDensity(segments[i + 2][j - 1], newDensity);
                                    if (j > 1) setDensity(segments[i][j - 2], newDensity);
                                    if (j < gridSize - 2) setDensity(segments[i][j + 2], newDensity);
                                    if (j > 1 && i < gridSize - 1) setDensity(segments[i + 1][j - 2], newDensity);
                                    if (j < gridSize - 2 && i < gridSize - 1)
                                        setDensity(segments[i + 1][j + 2], newDensity);
                                    if (j > 1 && i > 0) setDensity(segments[i - 1][j - 2], newDensity);
                                    if (j < gridSize - 2 && i > 0) setDensity(segments[i - 1][j + 2], newDensity);
                                    if (i > 1 && j > 1) setDensity(segments[i - 2][j - 2], newDensity);
                                    if (i < gridSize - 2 && j < gridSize - 2)
                                        setDensity(segments[i + 2][j + 2], newDensity);
                                    if (i > 1 && j < gridSize - 2) setDensity(segments[i - 2][j + 2], newDensity);
                                    if (i < gridSize - 2 && j > 1) setDensity(segments[i + 2][j - 2], newDensity);
                                }

                                break;
                            }
                        }
                        break;
                    }
                }
            }
        });
    }

    private void initUpload() {
        MemoryBuffer buffer = new MemoryBuffer();
        upload = new Upload(buffer);
        upload.setDropLabel(new Label("Перетащите файл"));
        upload.setUploadButton(new Button("Загрузить"));

        upload.addSucceededListener(event -> {
            Element image = new Element("object");
            image.setAttribute("type", "image/png");

            StreamResource resource = new StreamResource("image.png",
                    () -> buffer.getInputStream());

            image.setAttribute("data", resource);
            image.setAttribute("width", "0");
            image.setAttribute("height", "0");

            UI.getCurrent().getElement().appendChild(image);

            ctx.drawImage(image.getAttribute("data"), 0, 0);

        });

        upload.getElement().addEventListener("file-remove", new DomEventListener() {
            @Override
            public void handleEvent(DomEvent arg0) {
                ctx.clearRect(0, 0, WIDTH, HEIGHT);
            }
        });
    }

    private void initDotsFields() {
        HorizontalLayout horizontalLayout = new HorizontalLayout();
        Label label = new Label("Число точек для каждого значения плотности");
        layout.add(label);
        layout.add(horizontalLayout);

        dots1Field = new NumberField();
        dots1Field.setMin(1);
        dots1Field.setMax(3);
        dots1Field.setStep(1);
        dots1Field.setValue(1.0);
        dots1Field.setHasControls(true);
        horizontalLayout.add(dots1Field);

        dots2Field = new NumberField();
        dots2Field.setMin(1);
        dots2Field.setMax(6);
        dots2Field.setStep(1);
        dots2Field.setValue(2.0);
        dots2Field.setHasControls(true);
        horizontalLayout.add(dots2Field);

        dots3Field = new NumberField();
        dots3Field.setMin(1);
        dots3Field.setMax(9);
        dots3Field.setStep(1);
        dots3Field.setValue(3.0);
        dots3Field.setHasControls(true);
        horizontalLayout.add(dots3Field);

        radiusField = new NumberField("Радиус");
        radiusField.setValue(40.0);
        radiusField.setMin(1);
        radiusField.setStep(5);
        radiusField.setHasControls(true);
        layout.add(radiusField);
    }

    private void initDensityField() {
        NumberField densityField = new NumberField("Плотность");
        densityField.setMin(1);
        densityField.setMax(3);
        densityField.setStep(1);
        densityField.setValue(1.0);
        densityField.setHasControls(true);
        layout.add(densityField);
        densityField.addValueChangeListener(
                new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<NumberField, Double>>() {
                    @Override
                    public void valueChanged(AbstractField.ComponentValueChangeEvent<NumberField, Double> numberFieldDoubleComponentValueChangeEvent) {
                        density = densityField.getValue().intValue();
                    }
                }
        );
    }

    private void initSegments() {
        segments = new Segment[gridSize][];
        for (int i = 0; i < gridSize; i++) {
            segments[i] = new Segment[gridSize];
            for (int j = 0; j < gridSize; j++) {
                segments[i][j] = new Segment(i * gridSize + j,
                        WIDTH / gridSize * j,
                        HEIGHT / gridSize * i);
            }
        }
    }

    private void densityChanged(Segment s) {
        int k = (245 - 255 / 4 * s.getDensity());
        drawRect(s.getX(), s.getY(), WIDTH / gridSize, HEIGHT / gridSize,
                String.format("rgb(%s, %s, %s)", k, k, k));
    }

    private void setDensity(Segment s, int density) {
        int prevDensity = s.getDensity();
        if (s.getDensity() < 2)
            s.setDensity(s.getDensity() + density);
        else s.setDensity(3);
        if (prevDensity != s.getDensity()) {
            densityChanged(s);
        }
    }

    //Code for ComboBox size
    private void initGridSize() {
        ComboBox<String> comboBox = new ComboBox<>("Размер сетки");
        comboBox.setItems("10", "20", "30");
        layout.add(comboBox);
        comboBox.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<String>, String>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<ComboBox<String>, String> comboBoxStringComponentValueChangeEvent) {
                gridSize = Integer.valueOf(comboBoxStringComponentValueChangeEvent.getValue());
                initSegments();
                for (int i = 1; i < gridSize; i++) {
                    int x1 = WIDTH / gridSize * i;
                    int x2 = WIDTH / gridSize * i;
                    int y1 = 0;
                    int y2 = HEIGHT;
                    System.out.println("Vert: (" + x1 + ", " + y1 + "), (" + x2 + ", " + y2 + ")");
                    drawLine(x1, y1, x2, y2, "black");

                    x1 = 0;
                    x2 = WIDTH;
                    y1 = HEIGHT / gridSize * i;
                    y2 = HEIGHT / gridSize * i;
                    System.out.println("Hor: (" + x1 + ", " + y1 + "), (" + x2 + ", " + y2 + ")");
                    drawLine(x1, y1, x2, y2, "black");
                }
            }
        });
    }

    private void drawCircle(double x, double y, double r, String color) {
        ctx.save();
        ctx.setFillStyle(color);
        ctx.beginPath();
        ctx.arc(x, y, r, 0, 2 * Math.PI, false);
        ctx.closePath();
        ctx.fill();
        ctx.restore();
    }

    private void drawRect(double x, double y, double w, double h, String color) {
        ctx.save();
        ctx.setFillStyle(color);
        ctx.setStrokeStyle("white");
        ctx.strokeRect(x, y, w, h);
        ctx.fillRect(x, y, w, h);
        ctx.restore();
    }

    private void drawLine(double x0, double y0, double x1, double y1, String color) {
        ctx.setStrokeStyle(color);
        ctx.beginPath();
        ctx.moveTo(x0, y0);
        ctx.lineTo(x1, y1);
        ctx.closePath();
        ctx.stroke();
    }

    private void drawLine(Dot d1, Dot d2, String color) {
        drawLine(d1.getX() - OFFSET_X, d1.getY() - OFFSET_Y, d2.getX() - OFFSET_X, d2.getY() - OFFSET_Y, color);
    }

    private void drawRandomCircle() {
        ctx.save();
        ctx.setLineWidth(2);
        ctx.setFillStyle(getRandomColor());
        ctx.beginPath();
        ctx.arc(Math.random() * 500, Math.random() * 500,
                10 + Math.random() * 90, 0, 2 * Math.PI, false);
        ctx.closePath();
        ctx.stroke();
        ctx.fill();
        ctx.restore();
    }

    private String getRandomColor() {
        return String.format("rgb(%s, %s, %s)", (int) (Math.random() * 256),
                (int) (Math.random() * 256), (int) (Math.random() * 256));
    }
}
