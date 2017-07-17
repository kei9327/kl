package com.knowrecorder.RealmHolder;

public class TextHolder {
    private int id;
    private String event;
    /**
     *  - Init : 텍스트를 처음 추가했을 때
     *  - Movement : Text Layout을 움직였을 때
     *  - Typing : 텍스트 타이핑을 마치고 Focus를 벗어났을 때
     */
    private String text;
    private float touchX;
    private float touchY;

    private float textTop;
    private float btnTop;

    private float textLeft;
    private float btnLeft;

    private float width;
    private float height;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
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

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public float getTextTop() {
        return textTop;
    }

    public void setTextTop(float textTop) {
        this.textTop = textTop;
    }

    public float getBtnTop() {
        return btnTop;
    }

    public void setBtnTop(float btnTop) {
        this.btnTop = btnTop;
    }

    public float getTextLeft() {
        return textLeft;
    }

    public void setTextLeft(float textLeft) {
        this.textLeft = textLeft;
    }

    public float getBtnLeft() {
        return btnLeft;
    }

    public void setBtnLeft(float btnLeft) {
        this.btnLeft = btnLeft;
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = width;
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height) {
        this.height = height;
    }

    public TextHolder clone() {
        TextHolder textHolder = new TextHolder();
        textHolder.setId(id);
        textHolder.setEvent(event);
        textHolder.setText(text);
        textHolder.setTouchX(touchX);
        textHolder.setTouchY(touchY);
        textHolder.setTextTop(textTop);
        textHolder.setBtnTop(btnTop);
        textHolder.setTextLeft(textLeft);
        textHolder.setBtnLeft(btnLeft);
        textHolder.setWidth(width);
        textHolder.setHeight(height);

        return textHolder;
    }
}
