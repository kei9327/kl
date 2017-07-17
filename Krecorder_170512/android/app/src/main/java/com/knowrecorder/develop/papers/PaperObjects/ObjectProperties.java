package com.knowrecorder.develop.papers.PaperObjects;

/**
 * Created by we160303 on 2017-02-06.
 */

public interface ObjectProperties {
    void moveTo(float x, float y);
    boolean isMovable();

    void scale(float pivotX, float pivotY, float scaleFactor);
    boolean isScalable();
}
