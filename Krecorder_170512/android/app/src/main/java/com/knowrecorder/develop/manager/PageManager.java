package com.knowrecorder.develop.manager;

import android.util.Log;

import com.knowrecorder.develop.ProcessStateModel;
import com.knowrecorder.develop.controller.RealmPacketPutter;
import com.knowrecorder.develop.event.ChangePage;
import com.knowrecorder.develop.model.PageStorage;
import com.knowrecorder.develop.model.realm.PacketObject;
import com.knowrecorder.develop.model.realm.Page;
import com.knowrecorder.develop.utils.PacketUtil;
import com.knowrecorder.rxjava.RxEventFactory;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by we160303 on 2017-02-23.
 */

public class PageManager {
    private final String TAG = "PaperManager";
    private static PageManager instance;

    private int currentPage;
    private long currentPageId;
    private float currentRunTime;

    private int sumOfPage;

    public long pageOverTime[][];

    private ArrayList<PageStorage> pageList;

    public static PageManager getInstance() {
        if(instance == null)
            instance = new PageManager();
        return instance;
    }

    private PageManager() {
    }

    public long getCurrentPageId(){ return currentPageId;}
    public int getCurrentPage() {
        return currentPage ;
    }
    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage ;
    }
    public long[][] getPageOverTime(){ return pageOverTime; };

    public int getSumOfPage() { return sumOfPage ; }
    public void setSumOfPage(int page){
        sumOfPage=page ;
    }
    public void addSumOfPage(int page){
        sumOfPage+=page ;
        pageList.add(new PageStorage(sumOfPage));
    }

    public void setCurrentRunTime(float runTime){ this.currentRunTime = runTime; }
    public float getCurrentRunTime(){
        if(ProcessStateModel.getInstanse().isRecording()){
            return ProcessStateModel.getInstanse().getElapsedTime();
        }else{
            return currentRunTime;
        }
    }

    public void changePage(int page){
        currentPage = page;
        currentPageId = getCurrentPageIdFromRealm(page);
    }

    public void changePageId(long pageId)
    {
        currentPageId = pageId;
        Realm realm = Realm.getDefaultInstance();
        int page = realm.where(Page.class).equalTo("id", pageId).findFirst().getPagenum();
        currentPage = page;
    }

    public void changePage(int page, final float runTime){
        currentPage = page;
        currentPageId = getCurrentPageIdFromRealm(page);
        //todo clearCanvas, loadPaper execute
        RealmPacketPutter.getInstance().allPacketSave(new RealmPacketPutter.SaveResult() {
            @Override
            public void saveResult() {
                RxEventFactory.get().post(new ChangePage(currentPageId, runTime));
            }
        });
    }

    public long getCurrentPageIdFromRealm(int page){
        Realm realm = Realm.getDefaultInstance();
        long pageid = realm.where(Page.class).equalTo("pagenum", page).findFirst().getId();
        realm.close();

        return pageid;
    }
    public long getTimeStampPageFromRealm(float timeStamp){
        Realm realm = Realm.getDefaultInstance();
        long pageid;

        RealmResults<PacketObject> results = realm.where(PacketObject.class).lessThanOrEqualTo("runtime", timeStamp).findAllSorted("runtime", Sort.DESCENDING);

        if(results.size() != 0)
            pageid = results.get(0).clone().getPageId() ;
        else
            pageid = realm.where(Page.class).findFirst().getId();

        realm.close();
        return pageid;
    }
    public int getTimeStampPageIdFromRealm(float timeStamp){
        Realm realm = Realm.getDefaultInstance();
        long pageid;
        RealmResults<PacketObject> results = realm.where(PacketObject.class).lessThanOrEqualTo("runtime", timeStamp).findAllSorted("runtime", Sort.DESCENDING);

        if(results.size() != 0)
            pageid = results.get(0).clone().getPageId() ;
        else
            pageid = realm.where(Page.class).findFirst().getId();

        int pageNum = realm.where(Page.class).equalTo("id", pageid).findFirst().getPagenum();
        realm.close();
        return pageNum;
    }
    public int getPageNumFromRealm(long pageid){
        Realm realm = Realm.getDefaultInstance();
        Log.d("WTFuck","pageid : " + pageid);
        Page page = realm.where(Page.class).equalTo("id", pageid).findFirst();
        int pageNum = 0;
        if(page !=null) {
            pageNum = page.getPagenum();
        }
        realm.close();
        return pageNum;
    }

    public void initPage(int sumPage){
        pageList = new ArrayList<>();
        for(int i = 1; i <= sumPage ; i++)
            pageList.add(new PageStorage(i));
    }

    public boolean getCurrentPageUndoListIsEmpty(){
        return pageList.get(this.currentPage-1).undoListIsEmpty();
    }
    public PageStorage getCurrentStorage(){
        if(pageList == null || pageList.size() == 0)
            return null;
        return pageList.get(this.currentPage-1);
    }
    public PageStorage getPageStorage(int page){
        return pageList.get(page);
    }



    public void currentPageClear(){
        if(getCurrentStorage() != null)
            getCurrentStorage().clearPage();
    }

    public void currentPageInaddDrawingPacket(long mid){
        getCurrentStorage().addDrawingPacket(mid);
    }
    public boolean currentPageContainPacket(long mid){
        return getCurrentStorage().isContainPacket(mid);
    }
    public boolean currentPageContainUndoPacket(long mid){
        return getCurrentStorage().isContainUndoPacket(mid);
    }
    public boolean exeUndo(boolean saved){
        return getCurrentStorage().undoPacket(saved);
    }
    public boolean exeRedo(boolean saved){
        return getCurrentStorage().redoPacket(saved);
    }
    public void clearCurrentPageUndoList(){
        if(!getCurrentPageUndoListIsEmpty())
            getCurrentStorage().clearUndoList();
    }

    public boolean isExistPage(int page){
        return 1 <= page && page <=sumOfPage;
    }

    public void makePageOverTime()
    {
        //시간에 따른 페이지 정보
        Realm realm = Realm.getDefaultInstance();
        RealmResults<PacketObject> packetObjects = realm.where(PacketObject.class).equalTo("type", PacketUtil.S_CHANGEPAGE).findAll();
        pageOverTime = null;
        pageOverTime = new long[packetObjects.size()][2];
        for(int i = 0 ; i < packetObjects.size() ; i++){
            pageOverTime[i][0] = (long)packetObjects.get(i).getRunTime();
            pageOverTime[i][1] = packetObjects.get(i).getPageId();
        }
    }

    public long getCurrentPageProgressPageId(float progressTime)
    {
        long page = -1;
        if(pageOverTime == null || pageOverTime.length ==0) {
            return page;
        }
        for(int cnt = 0 ; cnt < pageOverTime.length ; cnt++)
        {
            if(progressTime < pageOverTime[cnt][0]){
                page = (long)pageOverTime[cnt-1][1];
                return page;
            }else{
                page = (long)pageOverTime[cnt][1];
            }
        }
        return page;
    }


}
