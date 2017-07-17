package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-23.
 */

public class ChangePageBody {
    private int b;
    private int e;
    private int pageno;

    public ChangePageBody(int b, int e, int pageno) {
        this.b = b;
        this.e = e;
        this.pageno = pageno;
    }

    public int getB() {
        return b;
    }

    public int getE() {
        return e;
    }

    public int getPageno() {
        return pageno;
    }

    public void setB(int b) {
        this.b = b;
    }

    public void setE(int e) {
        this.e = e;
    }

    public void setPageno(int pageno) {
        this.pageno = pageno;
    }
}
