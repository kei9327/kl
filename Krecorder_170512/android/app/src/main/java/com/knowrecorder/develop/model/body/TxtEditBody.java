package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-17.
 */

public class TxtEditBody {
    int b;
    int e;
    long id;
    String content;

    public TxtEditBody(int b, int e, long id, String content) {
        this.b = b;
        this.e = e;
        this.id = id;
        this.content = content;
    }

    public int getB() {
        return b;
    }

    public int getE() {
        return e;
    }

    public long getId() {
        return id;
    }

    public String getContent() {
        return content;
    }

    public void setB(int b) {
        this.b = b;
    }

    public void setE(int e) {
        this.e = e;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
