package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-13.
 */

public class ShapeBody {
    String shapetype ;
    float beginx ;
    float beginy ;
    float endx ;
    float endy ;
    float angle ;
    float scale ;
    long color;
    int b;
    int e;

    public ShapeBody(String shapetype, float beginx, float beginy, float endx, float endy, float angle, float scale, long color, int b, int e) {
        this.shapetype = shapetype;
        this.beginx = beginx;
        this.beginy = beginy;
        this.endx = endx;
        this.endy = endy;
        this.angle = angle;
        this.scale = scale;
        this.color = color;
        this.b = b;
        this.e = e;
    }

    public String getShapetype() {
        return shapetype;
    }

    public float getBeginx() {
        return beginx;
    }

    public float getBeginy() {
        return beginy;
    }

    public float getEndx() {
        return endx;
    }

    public float getEndy() {
        return endy;
    }

    public float getAngle() {
        return angle;
    }

    public float getScale() {
        return scale;
    }

    public long getColor() {
        return color;
    }

    public int getB() {
        return b;
    }

    public int getE() {
        return e;
    }

    public void setShapetype(String shapetype) {
        this.shapetype = shapetype;
    }

    public void setBeginx(float beginx) {
        this.beginx = beginx;
    }

    public void setBeginy(float beginy) {
        this.beginy = beginy;
    }

    public void setEndx(float endx) {
        this.endx = endx;
    }

    public void setEndy(float endy) {
        this.endy = endy;
    }

    public void setAngle(float angle) {
        this.angle = angle;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public void setColor(long color) {
        this.color = color;
    }

    public void setB(int b) {
        this.b = b;
    }

    public void setE(int e) {
        this.e = e;
    }

    @Override
    public String toString() {
        return "[ shapetype : "+shapetype+" beginX : "+beginx+" beginY : "+beginy+" endX : "+endx+"endY : "+endy+" angle : "+angle+" scale : "+scale+" color : "+color+" b : "+b+" e : "+e+" ]";
    }
}
