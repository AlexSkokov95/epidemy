package ui.canvas;

import java.io.Serializable;

public class CanvasRenderingContext2D {
    private Canvas canvas;

    protected CanvasRenderingContext2D(Canvas canvas) {
        this.canvas = canvas;
    }

    public void setFillStyle(String fillStyle) {
        this.setProperty("fillStyle", fillStyle);
    }

    public void setStrokeStyle(String strokeStyle) {
        this.setProperty("strokeStyle", strokeStyle);
    }

    public void setLineWidth(double lineWidth) {
        this.setProperty("lineWidth", lineWidth);
    }

    public void setFont(String font) {
        this.setProperty("font", font);
    }

    public void arc(double x, double y, double radius, double startAngle, double endAngle, boolean antiClockwise) {
        this.runScript(String.format("var canvas=document.getElementById('canvas');$0.getContext('2d').arc(%s-canvas.offsetLeft, %s-canvas.offsetTop, %s, %s,%s,%s);",
                x, y, radius, startAngle, endAngle, antiClockwise));
    }

    public void chart(String json) {
        this.runScript(String.format("var ctx=document.getElementById('chartCanvas').getContext('2d');var myChart = new Chart(ctx, %s);", json));
    }

    public void beginPath() {
        this.callJsMethod("beginPath");
    }

    public void clearRect(double x, double y, double width, double height) {
        this.callJsMethod("clearRect", x, y, width, height);
    }

    public void closePath() {
        this.callJsMethod("closePath");
    }

    public void drawImage(String src, double x, double y) {
        this.runScript(String.format("var img = new Image();img.onload = function () {$0.getContext('2d').drawImage(img, %s, %s);};img.src='%s';", x, y, src));
    }

    public void drawImage(String src, double x, double y, double width, double height) {
        this.runScript(String.format("var img = new Image();img.onload = function () {$0.getContext('2d').drawImage(img, %s, %s, %s, %s);};img.src='%s';", x, y, width, height, src));
    }

    public void fill() {
        this.callJsMethod("fill");
    }

    public void fillRect(double x, double y, double width, double height) {
        this.runScript(String.format("var canvas=document.getElementById(\"canvas\");$0.getContext('2d').fillRect(%s, %s, %s, %s);", x, y, width, height));
    }

    public void fillText(String text, double x, double y) {
        this.callJsMethod("fillText", text, x, y);
    }

    public void lineTo(double x, double y) {
        this.runScript(String.format("var canvas=document.getElementById(\"canvas\");$0.getContext('2d').lineTo(%s, %s);", x, y));
    }

    public void moveTo(double x, double y) {
        this.runScript(String.format("var canvas=document.getElementById(\"canvas\");$0.getContext('2d').moveTo(%s, %s);", x, y));
    }

    public void rect(double x, double y, double width, double height) {
        this.callJsMethod("rect", x, y, width, height);
    }

    public void restore() {
        this.callJsMethod("restore");
    }

    public void rotate(double angle) {
        this.callJsMethod("rotate", angle);
    }

    public void save() {
        this.callJsMethod("save");
    }

    public void scale(double x, double y) {
        this.callJsMethod("scale", x, y);
    }

    public void stroke() {
        this.callJsMethod("stroke");
    }

    public void strokeRect(double x, double y, double width, double height) {
        this.callJsMethod("strokeRect", x, y, width, height);
    }

    public void strokeText(String text, double x, double y) {
        this.callJsMethod("strokeText", text, x, y);
    }

    public void translate(double x, double y) {
        this.callJsMethod("translate", x, y);
    }

    protected void setProperty(String propertyName, Serializable value) {
        this.runScript(String.format("$0.getContext('2d').%s='%s'", propertyName, value));
    }

    private void runScript(String script) {
        this.canvas.getElement().getNode().runWhenAttached((ui) -> {
            ui.getInternals().getStateTree().beforeClientResponse(this.canvas.getElement().getNode(), (context) -> {
                ui.getPage().executeJavaScript(script, new Serializable[]{this.canvas.getElement()});
            });
        });
    }

    protected void callJsMethod(String methodName, Serializable... parameters) {
        this.canvas.getElement().callFunction("getContext('2d')." + methodName, parameters);
    }

    private void runScriptWithParams(String methodName, Serializable... parameters) {

    }
}

