package ui.canvas;


import com.vaadin.flow.component.*;

@Tag("ui/canvas")
public class Canvas extends Component implements HasStyle, HasSize, ClickNotifier {
    private CanvasRenderingContext2D context = new CanvasRenderingContext2D(this);

    public Canvas(int width, int height) {
        this.getElement().setAttribute("width", String.valueOf(width));
        this.getElement().setAttribute("height", String.valueOf(height));
    }

    public CanvasRenderingContext2D getContext() {
        return this.context;
    }
}
