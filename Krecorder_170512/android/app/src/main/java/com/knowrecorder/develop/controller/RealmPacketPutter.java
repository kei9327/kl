package com.knowrecorder.develop.controller;

import android.util.Log;

import com.google.gson.Gson;
import com.knowrecorder.develop.ProcessStateModel;
import com.knowrecorder.develop.event.EventType;
import com.knowrecorder.develop.manager.PageManager;
import com.knowrecorder.develop.model.body.ChangePageBody;
import com.knowrecorder.develop.model.realm.Note;
import com.knowrecorder.develop.model.realm.PacketObject;
import com.knowrecorder.develop.model.realm.Page;
import com.knowrecorder.develop.model.realm.TimeLine;
import com.knowrecorder.develop.model.realmHoler.PacketObjectHolder;
import com.knowrecorder.develop.papers.DrawingPanel;
import com.knowrecorder.develop.utils.PacketUtil;
import com.knowrecorder.rxjava.RxEventFactory;

import java.util.ArrayDeque;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * Created by we160303 on 2017-02-02.
 */

public class RealmPacketPutter {

    private final String TAG = "RealmPacketPutter";
    private static RealmPacketPutter instance = null;
    private ArrayDeque<PacketObject> packetBuffer = new ArrayDeque<>();

    private Timer timer;
    private TimerTask timerTask;

    public interface SaveResult{
        void saveResult();
    }

    public static RealmPacketPutter getInstance(){
        if(instance == null)
            instance = new RealmPacketPutter();
        return instance;
    }

    private RealmPacketPutter() {
    }

    /**
     * Realm 구조
     * NoteObject
     *   - noteId     : noteID 식별 별로 쓸데가 없어보인다.
     *   - CreateDate : Note가 생성된 날짜
     *   - title      : Note의 이름
     *   - totalTime  : Realm의 총 녹화된 시간
     *   - Info       : 부가적인 정보들 JSon Format으로 들어간다. 현재 Note가 만들어진 디바이스에 해상도가 들어간다. ex) width : 2400, height : 1200, dencity :1, platform : Android
     *
     * PacketObject
     *   - id         : AutoIncrement의 값으로 Primary Key이다. 순서 보장이 필요 하다.
     *   - mid        : Packet들의 MemberID 이다. 예를들어 pointerBegon -> pointerMove - > pointerEnd  이렇게 한 멤버이므로 같은 mid를 가지고 있다.
     *   - pageid     : PageObject의 ID 이다. 이 패킷이 어떤 페이지에서 생성되었는지 판별할 수 있다.
     *   - type       : String 값으로 이루어져 있으며 작업을 식별할 수 있다. 예를들어 펜, 지우개, 이미지삽입 등등...
     *   - body       : todo 설명 다시 필요
     *   - runtime    : Recording이 시작된 시간부터 경과된 시간이다.
     *   - isStatic   : Recording이 되면서 저장된 패킷인지 아닌지 판단 Recording이 되지 않는 상태는 True값을 저장한다.
     *   - isPdfPage  : PDF가 있는 페이지를 나타낸다.
     *   - isEditingMode  : 재녹화할때 필요한 정보이다.
     *
     * pageObject
     *   - id         : AutoIncrement의 값으로 Primary Key이다. 순서 보장이 필요하다.
     *   - pageNumber : 페이지의 수가 들어간다.
     *   - scale      : 이페이지가 얼마나 확대 되었는지의 값이 들어간다.
     *   - PivotX     : 확대의 기준점 X값이 들어간다.
     *   - PivotY     : 확대의 기준점 Y값이 들어간다.
     *
     * TimeLineObject
     *   - mid        : Packet들의 MemberID 이다. PacketObject의 mid값을 가져다 사용한다.
     *   - startRun   : Packet의 시작 RunTime의 값이 들어간다.
     *   - endRun     : Packet의 종료 RunTime의 값이 들어간다.
     *   - type       : int 값으로 이루어져 있으며 작업을 식별할 수 있다. PacketObject의 type값을 가져다 사용한다.
     *   - remarks    : 부가적인 정보가 들어간다. 예를들어 Color값..등등
     */

    public void packetPut(PacketObject packet){
        if(packetBuffer != null)
            packetBuffer.add(packet);
    }

    public void allPacketSave(final SaveResult result){

        PacketUtil.insertTimeLine();
        if(packetBuffer.isEmpty()) {
            if(result != null)
                result.saveResult();

            return;
        }
        Realm realm = Realm.getDefaultInstance();

        realm.executeTransactionAsync(new Realm.Transaction() {

            @Override
            public void execute(Realm realm) {
                while(!packetBuffer.isEmpty()){
                    realm.insert(packetBuffer.pop());
                }
            }
        }, new Realm.Transaction.OnSuccess() {

            @Override
            public void onSuccess() {
                if(result != null)
                    result.saveResult();
            }
        }, new Realm.Transaction.OnError(){

            @Override
            public void onError(Throwable error) {
                error.printStackTrace();
            }
        });
        realm.close();
    }

    public void InsertAddPage(final int pageNum){
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();

        Page page = new Page();
        page.setId(DrawingPanel.pageId.incrementAndGet());
        page.setPagenum(pageNum);
        realm.copyToRealm(page);

        realm.commitTransaction();

        realm.close();
    }

    public void InsertTimeLine(long mid, float startRun, float endRun, String type){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        TimeLine timeLine = new TimeLine();
        timeLine.setMid(mid);
        timeLine.setStartRun(startRun);
        timeLine.setEndRun(endRun);
        timeLine.setType(type);
        realm.copyToRealm(timeLine);

        realm.commitTransaction();
        realm.close();

    }

    public void InsertChangePage(){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        PacketObject packet = makeChangePagePacket();
        realm.copyToRealm(packet);

        realm.commitTransaction();
        realm.close();
    }

    public PacketObject makeChangePagePacket(){
        int id = DrawingPanel.id.incrementAndGet();
        int mid = DrawingPanel.mid.incrementAndGet();
        long pageId = PageManager.getInstance().getCurrentPageId();
        long runTime = ProcessStateModel.getInstanse().getElapsedTime();
        String type = "changepage";
        Gson gson = new Gson();
        ChangePageBody changePageBody = new ChangePageBody(1, 1, PageManager.getInstance().getCurrentPage());
        String body = gson.toJson(changePageBody);
        PacketUtil.insertTimeLine(mid, runTime, "changepage");

        PacketObjectHolder packet = new PacketObjectHolder.PacketObjectHolderBuilder(id, mid, pageId, type, body, runTime).setIsStaticEnabled(false).build();
        return packet.clone();
    }

    public void DeleteUndoPacket(long pageid){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        RealmResults<PacketObject> results = realm.where(PacketObject.class)
                .equalTo("pageid", pageid)
                .equalTo("type", "undo")
                .findAllSorted("id", Sort.DESCENDING);

        results.get(0).deleteFromRealm();

        realm.commitTransaction();
        realm.close();
    }

    public void UpdateNoteTitle(String title){
        Realm realm = Realm.getDefaultInstance();
        realm.beginTransaction();

        Note note = realm.where(Note.class).findFirst();
        note.setTitle(title);
        realm.copyToRealm(note);

        realm.commitTransaction();
        realm.close();
    }


    class PacketSaveTask extends TimerTask {

        @Override
        public void run() {
            if(!packetBuffer.isEmpty())
                RxEventFactory.get().post(new EventType(EventType.ALL_SAVE_PACKET));
        }
    }

    public void startTimer() {
        timer = new Timer();
        timerTask = new PacketSaveTask();
        timer.schedule(timerTask, 3000);
    }

    public void cancelTimer() {
        try {
            timerTask.cancel();
            timerTask = null;
        } catch (Exception e) {

        }

        try {
            timer.cancel();
            timer.purge();
            timer = null;
        } catch (Exception e) {

        }
    }

}
