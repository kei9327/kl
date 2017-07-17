//package com.knowrecorder.PaperObjects;
//
//import android.content.Context;
//import android.util.AttributeSet;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.widget.Button;
//import android.widget.RelativeLayout;
//
//import com.knowrecorder.Managers.Recorder;
//
///**
// * Created by ssyou on 2016-03-08.
// */
//@Deprecated
//public class PDFLayout extends RelativeLayout {
//
//    LayoutInflater mInflater;
//    public PDFView pdfView;
//    Context context;
//    Button leftBtn;
//    Button rightBtn;
//    private int index;
//
//    public PDFLayout(Context context) {
//        super(context);
//        this.context = context;
//        mInflater = LayoutInflater.from(context);
//    }
//
//    public PDFLayout(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        this.context = context;
//        mInflater = LayoutInflater.from(context);
//    }
//
//    public PDFLayout(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        this.context = context;
//        mInflater = LayoutInflater.from(context);
//    }
//
//    public void init(float startX, float startY, float touchX, float touchY, float ratio, String pdfFile) {
//        pdfView = new PDFView(this.context);
//        leftBtn = new Button(this.context);
//        rightBtn = new Button(this.context);
//
//        leftBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                prevPdfPage();
//            }
//        });
//
//        rightBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                nextPdfPage();
//            }
//        });
//
//        pdfView.setPdfFile(pdfFile);
//
//        float tmp;
//        if (touchX < startX) {
//            tmp = touchX;
//            touchX = startX;
//            startX = tmp;
//        }
//
//        if (touchY < startY) {
//            tmp = touchY;
//            touchY = startY;
//            startY = tmp;
//        }
//
//        float width = touchX - startX;
//        float height = touchY - startY;
//        touchY = touchY - (height - (width / ratio));
//
//        pdfView.setCoordinates(startX, startY, touchX, touchY);
//        pdfView.isPdf = true;
//        addView(pdfView);
//
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
//                LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        layoutParams.leftMargin = (int) startX - 100;
//        layoutParams.topMargin = (int) touchY;
//
//        leftBtn.setText("<<");
//        leftBtn.setLayoutParams(layoutParams);
//        addView(leftBtn);
//
//        layoutParams = new RelativeLayout.LayoutParams(
//                LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        layoutParams.leftMargin = (int) touchX - 80;
//        layoutParams.topMargin = (int) touchY;
//
//        rightBtn.setText(">>");
//        rightBtn.setLayoutParams(layoutParams);
//        addView(rightBtn);
//    }
//
//    public void invalidateView(float startX, float startY, float touchX, float touchY) {
//        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(
//                LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        layoutParams.leftMargin = (int) startX - 100;
//        layoutParams.topMargin = (int) startY;
//
//        leftBtn.setLayoutParams(layoutParams);
//
//        layoutParams = new RelativeLayout.LayoutParams(
//                LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT);
//        layoutParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
//        layoutParams.leftMargin = (int) touchX - 80;
//        layoutParams.topMargin = (int) startY;
//
//        rightBtn.setLayoutParams(layoutParams);
//        this.invalidate();
//    }
//
//    public void setIndex(int index) {
//        this.index = index;
//    }
//
//    public void prevPdfPage() {
//        if (!Recorder.getInstance().isPlaying()) {
//            if (pdfView.getPageNumber() > 0) {
//                pdfView.setPageNumber(pdfView.getPageNumber() - 1);
//                pdfView.invalidate();
//            }
//        }
//    }
//
//    public void nextPdfPage() {
//        if (!Recorder.getInstance().isPlaying()) {
//            if (pdfView.getPageNumber() < pdfView.getMaxPage() - 1) {
//                pdfView.setPageNumber(pdfView.getPageNumber() + 1);
//                pdfView.invalidate();
//            }
//        }
//    }
//}
