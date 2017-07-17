package com.knowrecorder.RealmHolder;

/**
 * Created by ssyou on 2016-02-03.
 */
public class DrawingHolder {
    private String drawingType;
    private String event;
    private float coordX;
    private float coordY;
    private int strokeWidth;
    private int strokeColor;
    private int strokeOpacity;
    private int eraserWidth;
    private int pointer;
    private int pointerColor;

    public String getDrawingType() {
        return drawingType;
    }

    public void setDrawingType(String drawingType) {
        this.drawingType = drawingType;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public float getCoordX() {
        return coordX;
    }

    public void setCoordX(float coordX) {
        this.coordX = coordX;
    }

    public float getCoordY() {
        return coordY;
    }

    public void setCoordY(float coordY) {
        this.coordY = coordY;
    }

    public int getStrokeWidth() {
        return strokeWidth;
    }

    public void setStrokeWidth(int strokeWidth) {
        this.strokeWidth = strokeWidth;
    }

    public int getStrokeColor() {
        return strokeColor;
    }

    public void setStrokeColor(int strokeColor) {
        this.strokeColor = strokeColor;
    }

    public int getStrokeOpacity() {
        return strokeOpacity;
    }

    public void setStrokeOpacity(int strokeOpacity) {
        this.strokeOpacity = strokeOpacity;
    }

    public int getEraserWidth() {
        return eraserWidth;
    }

    public void setEraserWidth(int eraserWidth) {
        this.eraserWidth = eraserWidth;
    }

    public int getPointer() {
        return pointer;
    }

    public void setPointer(int pointer) {
        this.pointer = pointer;
    }

    public int getPointerColor() {
        return pointerColor;
    }

    public void setPointerColor(int pointerColor) {
        this.pointerColor = pointerColor;
    }

    public DrawingHolder clone() {
        DrawingHolder drawingHolder = new DrawingHolder();
        drawingHolder.setDrawingType(drawingType);
        drawingHolder.setEvent(event);
        drawingHolder.setCoordX(coordX);
        drawingHolder.setCoordY(coordY);
        drawingHolder.setStrokeWidth(strokeWidth);
        drawingHolder.setStrokeColor(strokeColor);
        drawingHolder.setStrokeOpacity(strokeOpacity);
        drawingHolder.setStrokeWidth(strokeWidth);
        drawingHolder.setEraserWidth(eraserWidth);
        drawingHolder.setPointer(pointer);
        drawingHolder.setPointerColor(pointerColor);

        return drawingHolder;
    }
}
