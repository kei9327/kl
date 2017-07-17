//package com.knowrecorder.OpenCourse.Manager;
//
//import android.content.Context;
//import android.widget.FrameLayout;
//import android.widget.TextView;
//
//import com.knowrecorder.Managers.PaperLoader;
//import com.knowrecorder.Managers.PaperManagerV2;
//import com.knowrecorder.OpenCourse.Papers.OCDrawingPanel;
//import com.knowrecorder.OpenCourse.Papers.OCDrawingPaper;
//import com.knowrecorder.OpenCourse.Papers.OCObjectPaper;
//import com.knowrecorder.Papers.ObjectPaper;
//import com.knowrecorder.PlayerActivity;
//
///**
// * Created by we160303 on 2017-01-13.
// */
//
//public class OCPaperManagerV2 {
//
//    private final String TAG = PaperManagerV2.class.getSimpleName();
//
//    private int noteId;
//    private int currentPage = 1;
//    private int sumOfPage;
//
//    private OCObjectPaper mOCObjectPaper;
//    private OCDrawingPaper mOCDrawingPaper;
//    private OCDrawingPanel mOCDrawingPanel;
//
//    private PlayerActivity player;
//
//    public void setDrawingPanel(OCDrawingPanel mOCDrawingPanel) {this.mOCDrawingPanel = mOCDrawingPanel;}
//    public OCDrawingPanel getDrawingPanel() {
//        return mOCDrawingPanel;
//    }
//
//    public void setObjectPaper(OCObjectPaper mOCObjectPaper){this.mOCObjectPaper = mOCObjectPaper;}
//    public OCObjectPaper getObjectPaper() {
//        return mOCObjectPaper;
//    }
//
//    public void setDrawingPaper(OCDrawingPaper mOCDrawingPaper) {this.mOCDrawingPaper = mOCDrawingPaper;}
//    public OCDrawingPaper getDrawingPaper() {
//        return mOCDrawingPaper;
//    }
//
//
//    public int getCurrentPage() { return currentPage ; }
//    public void setCurrentPage(int currentPage) {
//        this.currentPage = currentPage ;
//    }
//
//    public int getSumOfPage() { return sumOfPage ; }
//    public void setSumOfPage(int page){
//        sumOfPage=page ;
//    }
//    public void addSumOfPage(int page){
//        sumOfPage+=page ;
//    }
//
//    public void changePage(int page, long timestamp){
//        currentPage = page;
//
//        mOCObjectPaper.clearCanvas();
//        mOCDrawingPaper.clearCanvas();
//
//        player.drawPage_lab(currentPage, timestamp, false);
//    }
//
//    public void setPlayer(PlayerActivity player){
//        this.player = player;
//    }
//
//    public boolean isExistPage(int page){
//        return 1 <= page && page <=sumOfPage;
//    }
//
//}
