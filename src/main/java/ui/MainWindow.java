package ui;

import com.vaadin.flow.component.*;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.upload.Upload;
import com.vaadin.flow.component.upload.receivers.MemoryBuffer;
import com.vaadin.flow.dom.DomEvent;
import com.vaadin.flow.dom.DomEventListener;
import com.vaadin.flow.dom.Element;
import com.vaadin.flow.server.StreamResource;
import ui.canvas.Canvas;
import ui.canvas.CanvasRenderingContext2D;


/*@Theme("valo")
@Route("")*/
public class MainWindow extends VerticalLayout {
    private Button button;
    private Canvas canvas;
    private Upload upload;
    private int sizeGrid;
    CanvasRenderingContext2D ctx;
    private VerticalLayout layout;

    public MainWindow() {
        layout = new VerticalLayout();
        layout.setSpacing(true);
        layout.setMargin(true);
        add(layout);

        button = new Button("Add");
        canvas = new Canvas(500, 500);
        canvas.getElement().setAttribute("id", "ui/canvas");
        ctx = canvas.getContext();

        canvas.addClickListener(new ComponentEventListener<ClickEvent<org.vaadin.pekkam.Canvas>>() {
            @Override
            public void onComponentEvent(ClickEvent clickEvent) {
                double x = clickEvent.getClientX();
                double y = clickEvent.getClientY();
                drawCircle(x, y, 20, "red");
            }
        });

        initUpload();
        initGridSize();

        button.addClickListener(new ComponentEventListener<ClickEvent<Button>>() {
            @Override
            public void onComponentEvent(ClickEvent<Button> buttonClickEvent) {
                for (int i = 0; i < 200; i++) {
                    drawRandomCircle();
                }
            }
        });



        layout.add(canvas, upload);
    }

    private void initUpload() {
        MemoryBuffer buffer = new MemoryBuffer();
        upload = new Upload(buffer);

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
                ctx.clearRect(0, 0, 500, 500);
            }
        });
    }

    //Code for ComboBox size
    private void initGridSize(){
        ComboBox<String> comboBox = new ComboBox<>("Размер сетки");
        comboBox.setItems("10", "20", "30");
        layout.add(comboBox);
        comboBox.addValueChangeListener(new HasValue.ValueChangeListener<AbstractField.ComponentValueChangeEvent<ComboBox<String>, String>>() {
            @Override
            public void valueChanged(AbstractField.ComponentValueChangeEvent<ComboBox<String>, String> comboBoxStringComponentValueChangeEvent) {
                sizeGrid = Integer.valueOf(comboBoxStringComponentValueChangeEvent.getValue());
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

    private void drawLine(double x0, double y0, double x1, double y1, String color) {
        ctx.setStrokeStyle(color);
        ctx.beginPath();
        ctx.moveTo(x0, y0);
        ctx.lineTo(x1, y1);
        ctx.closePath();
        ctx.stroke();
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
