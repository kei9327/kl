package com.knowrecorder.develop.model.body;

/**
 * Created by we160303 on 2017-02-24.
 */

public class PdfBody {
    boolean locked;

    int b;
    int e;
    int width;
    int height;
    int order;
    int pdfpageno;
    long paperId;
    float originx;
    float originy;
    float endx;
    float endy;
    String filename;
    String pdfFileName;
    String storage;
    String cmd;

    public PdfBody(int b, int e, float originx, float originy, int pdfpageno, String filename) {
        this.b = b;
        this.e = e;
        this.originx = originx;
        this.originy = originy;
        this.pdfpageno = pdfpageno;
        this.filename = filename;
    }

    public PdfBody(Builder builder) {
        b = builder.b;
        e = builder.e;
        width =  builder.width;
        height =  builder.height;
        order =  builder.order;
        pdfpageno =  builder.pdfpageno;
        paperId =  builder.paperId;
        originx =  builder.originx;
        originy =  builder.originy;
        endx =  builder.endx;
        endy =  builder.endy;
        filename =  builder.filename;
        pdfFileName =  builder.pdfFileName;
        storage =  builder.storage;
        cmd =  builder.cmd;
    }

    public static class Builder{
        private final int b;
        private final int e;
        private final int pdfpageno;
        private final String filename;
        private final int originx;
        private final int originy;
        private final int endx;
        private final int endy;

        private int order = 0;
        private int width = 0;
        private int height = 0;
        private long paperId = 0;
        private String cmd = null;
        private String storage = null;
        private String pdfFileName;

        public Builder(int b, int e, int pdfpageno, String filename, int originx, int originy, int endx, int endy) {
            this.b = b;
            this.e = e;
            this.pdfpageno = pdfpageno;
            this.filename = filename;
            this.originx = originx;
            this.originy = originy;
            this.endx = endx;
            this.endy = endy;
        }


        public PdfBody build(){
            return new PdfBody(this);
        }
    }

    public int getB() {
        return b;
    }

    public int getE() {
        return e;
    }

    public float getOriginx() {
        return originx;
    }

    public float getOriginy() {
        return originy;
    }

    public int getPdfpageno() {
        return pdfpageno;
    }

    public String getFilename() {
        return filename;
    }

    public void setB(int b) {
        this.b = b;
    }

    public void setE(int e) {
        this.e = e;
    }

    public void setOriginx(float originx) {
        this.originx = originx;
    }

    public void setOriginy(float originy) {
        this.originy = originy;
    }

    public void setPdfpageno(int pdfpageno) {
        this.pdfpageno = pdfpageno;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public boolean isLocked() {
        return locked;
    }

    public void setLocked(boolean locked) {
        this.locked = locked;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeigth() {
        return height;
    }

    public void setHeigth(int heigth) {
        this.height = heigth;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public long getPaperId() {
        return paperId;
    }

    public void setPaperId(long paperId) {
        this.paperId = paperId;
    }

    public float getEndx() {
        return endx;
    }

    public void setEndx(float endx) {
        this.endx = endx;
    }

    public float getEndy() {
        return endy;
    }

    public void setEndy(float endy) {
        this.endy = endy;
    }

    public String getPdfFileName() {
        return pdfFileName;
    }

    public void setPdfFileName(String pdfFileName) {
        this.pdfFileName = pdfFileName;
    }

    public String getStorage() {
        return storage;
    }

    public void setStorage(String storage) {
        this.storage = storage;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }
}
