//package com.knowrecorder.OpenCourse.Manager;
//
//import android.widget.FrameLayout;
//import android.widget.TextView;
//
//import com.knowrecorder.OpenCourse.Papers.OCDrawingPanel;
//import com.knowrecorder.OpenCourse.Papers.OCDrawingPaper;
//import com.knowrecorder.OpenCourse.Papers.OCObjectPaper;
//import com.knowrecorder.PlayerActivity;
//
///**
// * Created by ssyou on 2016-02-11.
// */
//public class OCPaperManager {
//    private int currentPage = 1;
//    private int sumOfPage;
//    private OCObjectPaper mOCObjectPaper;
//    private OCDrawingPaper mOCDrawingPaper;
//    private OCDrawingPanel mOCDrawingPanel;
//    private FrameLayout layoutMain;
//    private int noteId;
//    private boolean doPageJob = false;
//    private PlayerActivity player;
//
//    public OCObjectPaper getObjectPaper() {
//        return mOCObjectPaper;
//    }
//
//    public void setObjectPaper(OCObjectPaper mOCObjectPaper) {
//        this.mOCObjectPaper = mOCObjectPaper;
//    }
//
//    public int getCurrentPage() {
//        return currentPage;
//    }
//
//    public void setCurrentPage(int currentPage) {
//        this.currentPage = currentPage;
//    }
//
//    public boolean jumpPage(int page) {
//        if (page > sumOfPage || page < 1) {
//            return false;
//        }
//
//        mOCObjectPaper.clearCanvas();
//        mOCDrawingPaper.clearCanvas();
//
//        currentPage = page;
//        player.drawPage_lab(currentPage, 0, false);
//
//        return true;
//    }
//
//    public void addPage() {
//        mOCObjectPaper.clearCanvas();
//        mOCDrawingPaper.clearCanvas();
//
//        sumOfPage++;
//        currentPage = sumOfPage;
//    }
//
//    public void addPageNotClear() {
//        sumOfPage++;
//        currentPage = sumOfPage;
//    }
//
//    public void nextPage() {
//        mOCObjectPaper.clearCanvas();
//        mOCDrawingPaper.clearCanvas();
//
//        currentPage++;
//
//        player.drawPage_lab(currentPage, 0, false);
//    }
//
//    public void nextPageNotClear() {
//        currentPage++;
//    }
//
//    public void prevPage() {
//        mOCObjectPaper.clearCanvas();
//        mOCDrawingPaper.clearCanvas();
//
//        currentPage--;
//
//        player.drawPage_lab(currentPage, 0, false);
//    }
//
//    public void prevPageNotClear() {
//        currentPage--;
//    }
//
//    public void jupmPageNotClear(int page) {
//        currentPage = page;
//    }
//
//    public int getSumOfPage() {
//        return this.sumOfPage;
//    }
//
//    public void setSumOfPage(int sumOfPage) {
//        this.sumOfPage = sumOfPage;
//    }
//
//    public void setDrawingPaper(OCDrawingPaper mOCDrawingPaper) {
//        this.mOCDrawingPaper = mOCDrawingPaper;
//    }
//
//    public boolean isDoPageJob() {
//        return doPageJob;
//    }
//
//    public void setDoPageJob(boolean isWorking) {
//        this.doPageJob = isWorking;
//    }
//
//    public int getNoteId() {
//        return noteId;
//    }
//
//    public void setNoteId(int noteId) {
//        this.noteId = noteId;
//    }
//
//    public OCDrawingPanel getDrawingPanel() {
//        return mOCDrawingPanel;
//    }
//
//    public void setDrawingPanel(OCDrawingPanel mOCDrawingPanel) {
//        this.mOCDrawingPanel = mOCDrawingPanel;
//    }
//
//    public FrameLayout getLayoutMain() {
//        return layoutMain;
//    }
//
//    public void setLayoutMain(FrameLayout layoutMain) {
//        this.layoutMain = layoutMain;
//    }
//
//    public void setPlayer(PlayerActivity player) {
//        this.player = player;
//    }
//
//    public OCDrawingPaper getDrawingPaper() {
//        return mOCDrawingPaper;
//    }
//}
