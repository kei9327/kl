package com.knowrecorder.RealmHolder;

/**
 * Created by ssyou on 2016-02-03.
 */
public class ObjectHolder {
    private String objectType;
    private String event;
    private float scaleFactor;
    private float startX;
    private float startY;
    private float touchX;
    private float touchY;
    private int shapeType;
    private int color;
    private int pdfPageNumber = -1;
    private String pdfFile = null;
    private String imageFile = null;

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public float getScaleFactor() {
        return scaleFactor;
    }

    public void setScaleFactor(float scaleFactor) {
        this.scaleFactor = scaleFactor;
    }

    public float getStartX() {
        return startX;
    }

    public void setStartX(float startX) {
        this.startX = startX;
    }

    public float getStartY() {
        return startY;
    }

    public void setStartY(float startY) {
        this.startY = startY;
    }

    public float getTouchX() {
        return touchX;
    }

    public void setTouchX(float touchX) {
        this.touchX = touchX;
    }

    public float getTouchY() {
        return touchY;
    }

    public void setTouchY(float touchY) {
        this.touchY = touchY;
    }

    public String getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(String pdfFile) {
        this.pdfFile = pdfFile;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public int getPdfPageNumber() {
        return pdfPageNumber;
    }

    public void setPdfPageNumber(int pdfPageNumber) {
        this.pdfPageNumber = pdfPageNumber;
    }

    public int getShapeType() {
        return shapeType;
    }

    public void setShapeType(int shapeType) {
        this.shapeType = shapeType;
    }

    public ObjectHolder clone() {
        ObjectHolder objectHolder = new ObjectHolder();
        objectHolder.setObjectType(objectType);
        objectHolder.setEvent(event);
        objectHolder.setScaleFactor(scaleFactor);
        objectHolder.setStartX(startX);
        objectHolder.setStartY(startY);
        objectHolder.setTouchX(touchX);
        objectHolder.setTouchY(touchY);
        objectHolder.setShapeType(shapeType);
        objectHolder.setColor(color);
        objectHolder.setPdfPageNumber(pdfPageNumber);
        objectHolder.setPdfFile(pdfFile);
        objectHolder.setImageFile(imageFile);

        return objectHolder;
    }
}
