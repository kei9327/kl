package com.knowrecorder.develop.player;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import com.google.gson.Gson;
import com.knowrecorder.R;
import com.knowrecorder.Toolbox.Toolbox;
import com.knowrecorder.develop.OpenCoursePlayerActivity;
import com.knowrecorder.develop.ProcessStateModel;
import com.knowrecorder.develop.audio.AudioPlayer;
import com.knowrecorder.develop.event.EventType;
import com.knowrecorder.develop.file.FilePath;
import com.knowrecorder.develop.manager.PageManager;
import com.knowrecorder.develop.model.body.ChangePageBody;
import com.knowrecorder.develop.model.body.DelObjBody;
import com.knowrecorder.develop.model.body.ImageBody;
import com.knowrecorder.develop.model.body.LaserBody;
import com.knowrecorder.develop.model.body.PdfBody;
import com.knowrecorder.develop.model.body.PenBody;
import com.knowrecorder.develop.model.body.PointEndBody;
import com.knowrecorder.develop.model.body.PointMoveBody;
import com.knowrecorder.develop.model.body.RedoBody;
import com.knowrecorder.develop.model.body.ResizeObjBody;
import com.knowrecorder.develop.model.body.ShapeBody;
import com.knowrecorder.develop.model.body.TransObjBody;
import com.knowrecorder.develop.model.body.TxtBeginBody;
import com.knowrecorder.develop.model.body.TxtEditBody;
import com.knowrecorder.develop.model.body.TxtEndBody;
import com.knowrecorder.develop.model.body.UndoBody;
import com.knowrecorder.develop.model.body.VideoBody;
import com.knowrecorder.develop.model.body.VideoPauseBody;
import com.knowrecorder.develop.model.body.VideoProgressingBody;
import com.knowrecorder.develop.model.body.VideoStartBody;
import com.knowrecorder.develop.model.body.VolumeProgressingBody;
import com.knowrecorder.develop.model.realm.PacketObject;
import com.knowrecorder.develop.model.realm.Page;
import com.knowrecorder.develop.model.realmHoler.PacketObjectHolder;
import com.knowrecorder.develop.papers.DrawingPanel;
import com.knowrecorder.develop.papers.DrawingPaper;
import com.knowrecorder.develop.papers.ObjectPaperV2;
import com.knowrecorder.develop.papers.PaperObjects.pdf.PDFView;
import com.knowrecorder.develop.papers.PaperObjects.shape.Rectangle;
import com.knowrecorder.develop.utils.ColorUtil;
import com.knowrecorder.develop.utils.PacketUtil;
import com.knowrecorder.rxjava.RxEventFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Changha on 2017-02-10.
 */

public class PaperPlayer {

    private final int DRAW_PACKET = 0;
    private final int UNDO_INVALIDATE = 1;
    private final int LOAD_COMPLETED = 2;
    
    private Context mContext;

    private float deviceWidth;
    private float deviceHeight;
    private float deviceDensity;

    public float videoWidth;
    public float videoHeight;
    public float videoDensity;

    private float resolutionRate;

    private DrawingPanel drawingPanel;
    private DrawingPaper drawingPaper;
    private ObjectPaperV2 objectPaper;

    private Timer playTimer;
    private PlayTimerTask playTimerTask;

    public PacketHandler packetHandler = new PacketHandler();

    private HashMap<String, Integer> packetMap;

    private float objectBeginX, objectBeginY;

    float playTime,prePlayTime;

    private boolean loadCompleted = true;
    private boolean isUndoLoader = false;
    private boolean isPlayed = false;
    private boolean widthIsLarger;
    private boolean firstRun = true;

    public PaperPlayer(Context context) {
        mContext = context;
        packetMap = new HashMap<>();

        packetMap.put(PacketUtil.S_PEN, PacketUtil.PEN);
        packetMap.put(PacketUtil.S_POINTMOVE, PacketUtil.POINTMOVE);
        packetMap.put(PacketUtil.S_POINTEND, PacketUtil.POINTEND);
        packetMap.put(PacketUtil.S_LASER, PacketUtil.LASER);
        packetMap.put(PacketUtil.S_SHAPE, PacketUtil.SHAPE);
        packetMap.put(PacketUtil.S_GRABOBJ, PacketUtil.GRABOBJ);
        packetMap.put(PacketUtil.S_TRANSOBJ, PacketUtil.TRANSOBJ);
        packetMap.put(PacketUtil.S_RESIZEOBJ, PacketUtil.RESIZEOBJ);
        packetMap.put(PacketUtil.S_IMAGE, PacketUtil.IMAGE);
        packetMap.put(PacketUtil.S_VIDEO, PacketUtil.VIDEO);

        packetMap.put(PacketUtil.S_VIDEOSTART, PacketUtil.VIDEOSTART);
        packetMap.put(PacketUtil.S_VIDEOPAUSE, PacketUtil.VIDEOPAUSE);
        packetMap.put(PacketUtil.S_VIDEOPROGRESSING, PacketUtil.VIDEOPROGRESSING);
        packetMap.put(PacketUtil.S_VOLUMEPROGRESSING, PacketUtil.VOLUMEPROGRESSING);
        packetMap.put(PacketUtil.S_TXTBEGIN, PacketUtil.TXTBEGIN);
        packetMap.put(PacketUtil.S_TXTEDIT, PacketUtil.TXTEDIT);
        packetMap.put(PacketUtil.S_TXTEND, PacketUtil.TXTEND);
        packetMap.put(PacketUtil.S_ZOOMBEGIN, PacketUtil.ZOOMBEGIN);
        packetMap.put(PacketUtil.S_ZOOM, PacketUtil.ZOOM);
        packetMap.put(PacketUtil.S_ZOOMEND, PacketUtil.ZOOMEND);

        packetMap.put(PacketUtil.S_DELOBJ, PacketUtil.DELOBJ);
        packetMap.put(PacketUtil.S_PDF, PacketUtil.PDF);
        packetMap.put(PacketUtil.S_UNDO, PacketUtil.UNDO);
        packetMap.put(PacketUtil.S_REDO, PacketUtil.REDO);
        packetMap.put(PacketUtil.S_CHANGEPAGE, PacketUtil.CHANGEPAGE);
        packetMap.put(PacketUtil.S_ALLDELETE, PacketUtil.ALLDELETE);
        packetMap.put(PacketUtil.S_TXT, PacketUtil.TXT);
        packetMap.put(PacketUtil.S_TXTMOVE, PacketUtil.TXTMOVE);
	    packetMap.put(PacketUtil.S_ADDPAGE, PacketUtil.ADDPAGE);
	    packetMap.put(PacketUtil.S_ROTATEOBJ, PacketUtil.ROTATEOBJ);

	    packetMap.put(PacketUtil.S_PDFCHANGEPAGE, PacketUtil.PDFCHANGEPAGE);
	    packetMap.put(PacketUtil.S_TXTRESIZE, PacketUtil.TXTRESIZE);



    }

    public void setVideoResolution(float videoWidth, float videoHeight, float videoDensity){
        this.videoWidth = videoWidth;
        this.videoHeight = videoHeight;
        this.videoDensity = videoDensity;

        setDeviceResolution();
    }

    private void setDeviceResolution() {
        DisplayMetrics metrics = new DisplayMetrics();
        ((AppCompatActivity)mContext).getWindowManager().getDefaultDisplay().getMetrics(metrics);
        deviceWidth = metrics.widthPixels;
        deviceHeight = (metrics.heightPixels-(45 * metrics.scaledDensity));
        deviceDensity = metrics.scaledDensity;

        if((videoWidth/videoHeight) > (deviceWidth/ deviceHeight)) {
            widthIsLarger = true;
            resolutionRate = deviceWidth /videoWidth;
        }else{
            widthIsLarger = false;
            resolutionRate = deviceHeight/videoHeight;
        }
    }

    public void setDrawingPanel(DrawingPanel drawingPanel){
        this.drawingPanel = drawingPanel;
    }

    public void setDrawingPaper(DrawingPaper drawingPaper){
        this.drawingPaper = drawingPaper;
    }

    public void setObjectPaper(ObjectPaperV2 objectPaper){
        this.objectPaper = objectPaper;
    }

    public void playPacket(){

        isPlayed = true;

        playTimer = new Timer();
        firstRun = true;
        playTimerTask = new PlayTimerTask();

        playTimer.scheduleAtFixedRate(playTimerTask, 0, 1);

        try {
            int curAudioPlayTime = AudioPlayer.getInstance(mContext).getPlayTime();
            AudioPlayer.getInstance(mContext).setInitTime(curAudioPlayTime);
            AudioPlayer.getInstance(mContext).startPlay();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void stopPacket(){
        isPlayed = false;
        try {
            playTimer.purge();
            playTimer.cancel();
            playTimer = null;
            playTimerTask = null;
            AudioPlayer.getInstance(mContext).stopPlay();
        }catch (NullPointerException ne){
            ne.printStackTrace();
        }
    }

    public void undoPageLoader(){

        ArrayList<Long> undoPacketList = getUndoPacketList(PageManager.getInstance().getCurrentPageId(), PageManager.getInstance().getCurrentRunTime());

        setUndoLoading(true);
        drawingPaper.clearCanvas();
        objectPaper.clearCanvasForUndo();

        Realm realm = Realm.getDefaultInstance();
        Message msg;
        long pageid = realm.where(Page.class).equalTo("pagenum", PageManager.getInstance().getCurrentPage()).findFirst().getId();
        RealmResults<PacketObject> results =
                realm.where(PacketObject.class)
                        .equalTo("pageid", pageid)
                        .notEqualTo("type", PacketUtil.S_CHANGEPAGE)
                        .notEqualTo("type", PacketUtil.S_UNDO)
                        .notEqualTo("type", PacketUtil.S_REDO)
                        .lessThanOrEqualTo("runtime", PageManager.getInstance().getCurrentRunTime())
                        .findAllSorted("runtime",Sort.ASCENDING,"id",Sort.ASCENDING);

        for(PacketObject data : results){
            PacketObjectHolder dataHolder = data.clone();
            if(undoPacketList.contains(dataHolder.getMid()) || PageManager.getInstance().currentPageContainUndoPacket(dataHolder.getMid()))
                continue;

            msg = packetHandler.obtainMessage();
            msg.what = DRAW_PACKET;
            msg.obj = dataHolder;
            packetHandler.sendMessage(msg);
        }

        packetHandler.sendEmptyMessage(UNDO_INVALIDATE);
    }

    public void pageLoader(long pageid){
        float runTime = 0;
        try {
            Realm realm = Realm.getDefaultInstance();
            runTime = realm.where(PacketObject.class).equalTo("pageid", pageid).max("runtime").floatValue();
            realm.close();
        }catch (NullPointerException ne){
            runTime = ProcessStateModel.getInstanse().getElapsedTime();
        }finally {
            pageLoader(pageid, runTime);
        }

    }
    public void pageLoader(long pageid, float runTime){

        ArrayList<Long> undoPacketList = getUndoPacketList(pageid, runTime);

        setLoadCompleted(false);

        Realm realm = Realm.getDefaultInstance();
        Message msg;
        RealmResults<PacketObject> results =
                    realm.where(PacketObject.class)
                            .equalTo("pageid", pageid)
                            .notEqualTo("type", PacketUtil.S_CHANGEPAGE)
                            .notEqualTo("type", PacketUtil.S_UNDO)
                            .notEqualTo("type", PacketUtil.S_REDO)
                            .lessThanOrEqualTo("runtime", runTime)
                            .findAllSorted("runtime",Sort.ASCENDING,"id",Sort.ASCENDING);

        for(PacketObject data : results){
            PacketObjectHolder dataHolder = data.clone();

            if(undoPacketList.contains(dataHolder.getMid()))
                continue;

            msg = packetHandler.obtainMessage();
            msg.what = DRAW_PACKET;
            msg.obj = dataHolder;
            packetHandler.sendMessage(msg);
        }
        packetHandler.sendEmptyMessage(LOAD_COMPLETED);
        realm.close();

        PageManager.getInstance().currentPageClear();
        PageManager.getInstance().setCurrentRunTime(runTime);
    }

    private ArrayList<Long> getUndoPacketList(long pageid, float runTime) {
        ArrayList<Long> undoPacketList = new ArrayList<>();
        Realm realm = Realm.getDefaultInstance();
        Gson gson = new Gson();
        RealmResults<PacketObject> results =
                realm.where(PacketObject.class)
                        .equalTo("pageid", pageid)
                        .beginGroup()
                            .equalTo("type", "undo")
                            .or()
                            .equalTo("type", "redo")
                        .endGroup()
                        .lessThanOrEqualTo("runtime", runTime)
                        .findAllSorted("runtime",Sort.ASCENDING,"id",Sort.ASCENDING);

        for(PacketObject data : results){
            PacketObjectHolder dataHolder = data.clone() ;
            if(TextUtils.equals(dataHolder.getType(), PacketUtil.S_UNDO)) {
                UndoBody undoBody = gson.fromJson(dataHolder.getBody(), UndoBody.class);
                undoPacketList.add(undoBody.getTarget());
            }else{
                RedoBody redoBody = gson.fromJson(dataHolder.getBody(), RedoBody.class);
                undoPacketList.remove(redoBody.getTarget());
            }
        }
        realm.close();
        return undoPacketList;
    }

    private void initPageState(long pageid, float runtime) {

        PageManager.getInstance().currentPageClear();

        Realm realm = Realm.getDefaultInstance();

        RealmResults<PacketObject> results =
                realm.where(PacketObject.class)
                        .equalTo("pageid", pageid)
                        .notEqualTo("type", "changepage")
                        .lessThanOrEqualTo("runtime", runtime)
                        .findAllSorted("id", Sort.ASCENDING);

        for(PacketObject data : results){
            PacketObjectHolder dataHolder = data.clone() ;
            int type = packetMap.get(dataHolder.getType());

            if(type == PacketUtil.UNDO)
                PageManager.getInstance().exeUndo(false);
            else if(inspectUndoPacket(type))
                PageManager.getInstance().currentPageInaddDrawingPacket(dataHolder.getMid());
        }
        realm.close();
    }

    private boolean inspectUndoPacket(int  type) {
        if(type == PacketUtil.CHANGEPAGE || type == PacketUtil.LASER || type == PacketUtil.VIDEOPAUSE || type == PacketUtil.VIDEOSTART || type == PacketUtil.VIDEOPROGRESSING || type == PacketUtil.VOLUMEPROGRESSING)
            return false;
        return true;
    }

    public void setPlayTime( float playTime) { this.playTime = playTime; }
    public float getPlayTime() { return playTime; }


    class PlayTimerTask extends TimerTask{
        @Override
        public void run() {
            Realm realm = Realm.getDefaultInstance();
            Message msg;

           if(android.os.Debug.isDebuggerConnected())
                android.os.Debug.waitForDebugger();

            if(firstRun) {
                playTime = AudioPlayer.getInstance(mContext).getPlayTime();
                firstRun = false;
            }

            RealmResults<PacketObject> results =
                    realm.where(PacketObject.class)
                            .greaterThan("runtime", playTime-1)
                            .lessThanOrEqualTo("runtime", playTime++)
                            .findAllSorted("runtime", Sort.ASCENDING, "id", Sort.ASCENDING);

            PageManager.getInstance().setCurrentRunTime(playTime);

            for (int i = 0; i < results.size(); i++) {

                PacketObjectHolder data = results.get(i).clone();

                Log.i("PaperPlayer2", " runTime : " + playTime + " index : " + data.getId());


                if (!TextUtils.equals(data.getType(), PacketUtil.S_CHANGEPAGE) && data.getPageId() != PageManager.getInstance().getCurrentPageId()){
                    //중간부터 시작했을때 페이지가 안 맞다면 타임라인에 맞는 페이지로 이동한 후에 메세지를 날린다.
                    long[][] pageOverTime = PageManager.getInstance().getPageOverTime();
                    if(pageOverTime != null && pageOverTime.length > 1){
                        for(int cnt = 0 ; cnt < pageOverTime.length ; cnt++)
                        {
                            if(playTime < pageOverTime[cnt][0]){
                                int page = (int)pageOverTime[cnt-1][1];
                                if(data.getPageId() == page){
                                    //패킷을 다시 보낼경우 페이지 세팅을 다시 하므로 페이지만 맞춰준다.
                                    PageManager.getInstance().changePage(page);
                                }
                            }
                        }
                    }
                    if(data.getPageId() != PageManager.getInstance().getCurrentPageId())
                        continue;
                }

                msg = packetHandler.obtainMessage();
                msg.what = DRAW_PACKET;
                msg.obj = data;
                packetHandler.sendMessage(msg);
            }
        }
    }

    public class PacketHandler extends Handler {
        private Gson gson = new Gson();

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            boolean drawingPaperPacket = true;
            switch (msg.what) {
                case DRAW_PACKET: {
                    final PacketObjectHolder packet;
                    packet = (PacketObjectHolder) msg.obj;
                    printLog(packet);

                    if(packetMap.get(packet.getType()) == null){
                        Log.w("[PacketHandler]","add packetName:"+ packet.getType());
                        break;
                    }
                    switch (packetMap.get(packet.getType())) {
                        case PacketUtil.PEN :
                            PenBody penBody = gson.fromJson(packet.getBody(), PenBody.class);
//                            float[][] xy = gson.fromJson(penBody.getPoints(), float[][].class);
                            float[][] xy = convertToDencity(penBody.getPoints());

                            if(penBody.getIsEraser())
                                Toolbox.getInstance().currentEraserWidth = (int)penBody.getStkwidth();

                            else
                                Toolbox.getInstance().currentStrokeWidth = (int) penBody.getStkwidth();

                            Toolbox.getInstance().currentStrokeColor = ColorUtil.colorFromRGBA(penBody.getColor());
                            Toolbox.getInstance().currentStrokeOpacity = (int) (penBody.getOpacity() * 255);


                            if (packet.isStatic()){
                                for(int i = 0; i < xy.length ; i++){
                                    if(i == 0){
                                        drawingPaper.onActionDown(xy[i][0], xy[i][1], penBody.getIsEraser(), packet.getMid());
                                    }else if(i == xy.length-1){
                                        drawingPaper.onActionUp(penBody.getIsEraser());
                                    }else{
                                        drawingPaper.onActionMove(xy[i][0], xy[i][1]);
                                    }
                                }
                            }else {
                                drawingPaper.onActionDown(xy[0][0], xy[0][1], penBody.getIsEraser(), packet.getMid());
                            }
                            drawingPaperPacket = true;
                            break;

                        case PacketUtil.POINTMOVE :
                            PointMoveBody pointMoveBody = gson.fromJson(packet.getBody(), PointMoveBody.class);
                            if(Toolbox.getInstance().getToolType() == Toolbox.Tooltype.PEN || Toolbox.getInstance().getToolType() == Toolbox.Tooltype.ERASER) {
                                drawingPaper.onActionMove(convertToDencity(pointMoveBody.getX()), convertToDencity(pointMoveBody.getY()));
                                drawingPaperPacket = true;
                            }else if(Toolbox.getInstance().getToolType() == Toolbox.Tooltype.SHAPE) {
                                objectPaper.onShapeMove(objectBeginX, objectBeginY, convertToDencity(pointMoveBody.getX()), convertToDencity(pointMoveBody.getY()));
                                drawingPaperPacket = false;
                            }
                            break;

                        case PacketUtil.POINTEND :
                            PointEndBody pointEndBody = gson.fromJson(packet.getBody(), PointEndBody.class);

                            if(Toolbox.getInstance().getToolType() == Toolbox.Tooltype.PEN || Toolbox.getInstance().getToolType() == Toolbox.Tooltype.ERASER) {
                                drawingPaper.onActionUp(pointEndBody.getIsEraser());
                                drawingPaperPacket = true;


                            }else if(Toolbox.getInstance().getToolType() == Toolbox.Tooltype.SHAPE) {
                                objectPaper.onShapUp(packet.getMid(), objectBeginX, objectBeginY, convertToDencity(pointEndBody.getX()), convertToDencity(pointEndBody.getY()));
                                drawingPaperPacket = false;
                            }

                            break;

                        case PacketUtil.LASER :
                            drawingPaperPacket = true;
                            LaserBody laserBody = gson.fromJson(packet.getBody(), LaserBody.class);

                            //예외처리 - IOS에서 레이저포인터를 끝낼때 pointerTag를 0으로 패킷을 넣기 떄문에 안드로이드에
                            //
                            // 서는 NullPoint Exception이 튄다.
                            if(laserBody.getPointertag() != 0)
                                Toolbox.getInstance().switchPointer(laserBody.getIco(), convertPointerTagToColor(laserBody.getPointertag()));

                            if(laserBody.getB() == 1 && laserBody.getE() == 0){
                                drawingPaper.onPointerDown(convertToDencity(laserBody.getX()), convertToDencity(laserBody.getY()));
                            }else if(laserBody.getB() == 0 && laserBody.getE() == 0){
                                drawingPaper.onPointerMove(convertToDencity(laserBody.getX()), convertToDencity(laserBody.getY()));
                            }else if(laserBody.getB() == 0 && laserBody.getE() == 1){
                                drawingPaper.onPointerUp();
                            }
                            break;

                        case PacketUtil.SHAPE :
                            ShapeBody shapeBody = gson.fromJson(packet.getBody(), ShapeBody.class);
                            objectBeginX = convertToDencity(shapeBody.getBeginx());
                            objectBeginY = convertToDencity(shapeBody.getBeginy());
                            Toolbox.getInstance().currentShape = Toolbox.ShapeType.values()[convertShapeType(shapeBody.getShapetype())];
                            Toolbox.getInstance().currentShapeColor = ColorUtil.colorFromRGBA(shapeBody.getColor());
                            if(packet.isStatic())
                                objectPaper.onShapeDownStatic(packet.getMid(), shapeBody.getBeginx(), shapeBody.getBeginy(), shapeBody.getEndx(), shapeBody.getEndy());
                            else
                                objectPaper.onShapeDown(packet.getMid());
                            drawingPaperPacket = false;
                            break;

                        case PacketUtil.GRABOBJ :
//                            GrabObjBody grabObjBody = gson.fromJson(packet.getBody(), GrabObjBody.class);
                            drawingPaperPacket = false;
                            break;
                        case PacketUtil.TRANSOBJ :
                            TransObjBody transObjBody = gson.fromJson(packet.getBody(), TransObjBody.class);

                            objectPaper.objectMove(transObjBody.getTarget(), convertToDencity(transObjBody.getDx()), convertToDencity(transObjBody.getDy()));
                            drawingPaperPacket = false;
                            break;
                        case PacketUtil.RESIZEOBJ :
                            ResizeObjBody resizeObjBody = gson.fromJson(packet.getBody(), ResizeObjBody.class);
                            if(packet.isStatic()) {
                                objectPaper.objectScaleStatic(resizeObjBody.getTarget(), resizeObjBody.getScale());
                            }else{
                                objectPaper.objectScale(resizeObjBody.getTarget(), resizeObjBody.getScale());
                            }
                            drawingPaperPacket = false;
                            break;

                        case PacketUtil.IMAGE :
                            ImageBody imageBody = gson.fromJson(packet.getBody(), ImageBody.class);
                            objectPaper.onImageDown(packet.getMid(), convertToDencity(imageBody.getOriginx()), convertToDencity(imageBody.getOriginy()), convertToDencity(imageBody.getEndx()), convertToDencity(imageBody.getEndy()), FilePath.getImagesDirectory()+packet.getMid()+".png", false);
                            drawingPaperPacket = false;
                            break;
                        case PacketUtil.VIDEO :
                            VideoBody videoBody = gson.fromJson(packet.getBody(), VideoBody.class);
                            if(packet.isStatic())
                                // IOS 때문에 생긴 메소드  static Packet을 Update하기때문에 있어야 함
                                objectPaper.onVideoStaticDown(packet.getMid(), FilePath.getImagesDirectory()+packet.getMid()+".mp4",
                                        convertToDencity(videoBody.getW()), convertToDencity(videoBody.getH()), convertToDencity(videoBody.getOriginx()), convertToDencity(videoBody.getOriginy()),
                                        1.f, videoBody.getVideoprogress());
                            else
                                objectPaper.onVideoDown(packet.getMid(), FilePath.getImagesDirectory()+packet.getMid()+".mp4", false);
                            drawingPaperPacket = false;
                            break;

                        case PacketUtil.VIDEOSTART :
                            VideoStartBody videoStartBody = gson.fromJson(packet.getBody(), VideoStartBody.class);
                            objectPaper.videoStart(videoStartBody.getMid());
                            drawingPaperPacket = false;
                            break;
                        case PacketUtil.VIDEOPAUSE :
                            VideoPauseBody videoPauseBody = gson.fromJson(packet.getBody(), VideoPauseBody.class);
                            objectPaper.videoPause(videoPauseBody.getMid());
                            drawingPaperPacket = false;
                            break;

                        case PacketUtil.VIDEOPROGRESSING :
                            VideoProgressingBody videoProgressingBody = gson.fromJson(packet.getBody(), VideoProgressingBody.class);
                            objectPaper.videoSeekTo(videoProgressingBody.getMid(), videoProgressingBody.getVideoprogress()); //todo videoProgress 자료형 float 으로 바꿈
                            drawingPaperPacket = false;
                            break;

                        case PacketUtil.VOLUMEPROGRESSING :
                            VolumeProgressingBody volumeProgressingBody = gson.fromJson(packet.getBody(), VolumeProgressingBody.class);
                            objectPaper.videoVolume(volumeProgressingBody.getMid(), (int)(volumeProgressingBody.getVolumeprogress()*8)); //todo volume 자료형 float 으로 바꿈
                            drawingPaperPacket = false;
                            break;

                        case PacketUtil.TXTBEGIN :
                            TxtBeginBody txtBeginBody = gson.fromJson(packet.getBody(), TxtBeginBody.class);
                            objectPaper.onTextDown(packet.getMid(), convertToDencity(txtBeginBody.getBeginx()), convertToDencity(txtBeginBody.getBeginy()), false, true);
                            drawingPaperPacket = false;
                            break;
                        case PacketUtil.TXTEDIT :
                            TxtEditBody txtEditBody = gson.fromJson(packet.getBody(), TxtEditBody.class);
                            objectPaper.setText(txtEditBody.getId(), txtEditBody.getContent());//todo id를 target으로 이름 바꿔야함...
                            drawingPaperPacket = false;
                            break;
                        case PacketUtil.ZOOMBEGIN:
                            break;
                        case PacketUtil.TXTEND :
                            TxtEndBody txtEndBody = gson.fromJson(packet.getBody(), TxtEndBody.class);
                            objectPaper.setText(txtEndBody.getId(), txtEndBody.getContent());//todo id를 target으로 이름 바꿔야함...
                            drawingPaperPacket = false;
                            break;

                        case PacketUtil.PDF :
                            PdfBody pdfBody = gson.fromJson(packet.getBody(), PdfBody.class);
                            objectPaper.openPdfPage(packet.getMid(), packet.getPageId(), pdfBody, resolutionRate);
                            drawingPaperPacket = false;
                            break;
                        case PacketUtil.DELOBJ :
                            DelObjBody delObjBody = gson.fromJson(packet.getBody(), DelObjBody.class);
                            objectPaper.deleteObject(delObjBody.getTarget());
                            drawingPaperPacket = false;
                            break;
                        case PacketUtil.UNDO :  // UNDO 정책을 정확히 다시 잡아야 함.
                            UndoBody undoBody = gson.fromJson(packet.getBody(), UndoBody.class);
                            PageManager.getInstance().exeUndo(false);
                            undoPageLoader();
                            break;
                        case PacketUtil.REDO :
                            undoPageLoader();
                            break;

                        case PacketUtil.CHANGEPAGE :
                            ChangePageBody changePageBody = gson.fromJson(packet.getBody(), ChangePageBody.class);
                            changePage(changePageBody.getPageno(), packet.getRunTime());
                            break;

                        case PacketUtil.TXT:    //IOS에서 기존 컨텐츠의  Text 떄문에 추가된 것
                            TxtBeginBody txtBody = gson.fromJson(packet.getBody(), TxtBeginBody.class);
                            objectPaper.onTextDown(packet.getMid(), convertToDencity(txtBody.getBeginx()), convertToDencity(txtBody.getBeginy()), false, true);
                            objectPaper.setText(packet.getMid(), txtBody.getContent());
                            drawingPaperPacket = false;
                            break;

                        case PacketUtil.TXTMOVE :    //IOS에서 기본 컨텐츠의 Text가 절대 좌표로 이동했기 때문에 추가 됨
                            TransObjBody txtMove = gson.fromJson(packet.getBody(), TransObjBody.class);
                            objectPaper.objectMoveStatic(txtMove.getTarget(), convertToDencity(txtMove.getDx()), convertToDencity(txtMove.getDy()));
                            drawingPaperPacket = false;
                            break;

                        case PacketUtil.ALLDELETE :     //IOS에서 AllDelete기능 현재 Android에는 없다.
                            clearAllCanvas();
                            break;

	                    case PacketUtil.ROTATEOBJ:
	                    case PacketUtil.ADDPAGE:
	                    case PacketUtil.PDFCHANGEPAGE:
	                    case PacketUtil.TXTRESIZE:
		                    Log.e("[PacketHandler]", "need to add function:"+ packet.getType());
		                    break;


                    }
                    if(!isUndoLoader) {
                        if (drawingPaperPacket)
                            drawingPaper.invalidate();
                        else
                            objectPaper.invalidate();
                    }
                    break;
                }

                case UNDO_INVALIDATE : {
                    setUndoLoading(false);
                    drawingPaper.invalidate();
                    objectPaper.invalidate();
                    break;
                }

                case LOAD_COMPLETED :{
                    ProcessStateModel.getInstanse().setIsPageLoading(false);
                    setLoadCompleted(true);
                    RxEventFactory.get().post(new EventType(EventType.LOAD_COMPLETED));
                }
            }
        }
    }

    private int convertPointerTagToColor(int pointerTag){
        //todo 내가 바꿔야 하는 부분 현재 color로 해쉬맵을 이루고 있다. tpye으로 바꿔야한다. (1,2,3,4...)
        switch (pointerTag){
            case 1 :
                return mContext.getResources().getColor(R.color.color1);
            case 2 :
                return mContext.getResources().getColor(R.color.color2);
            case 3 :
                return mContext.getResources().getColor(R.color.color3);
            case 4 :
                return mContext.getResources().getColor(R.color.color4);
            case 5 :
                return mContext.getResources().getColor(R.color.color5);
            case 6 :
                return mContext.getResources().getColor(R.color.color6);
        }
        return 0;
    }

    private int convertShapeType(String shapetype) {    //todo IOS 떄문에 쓸데없이 만든 메소드

        if(TextUtils.equals(shapetype, "circle"))
            return 0;
        if(TextUtils.equals(shapetype, "tri"))
            return 1;
        if(TextUtils.equals(shapetype, "box"))
            return 2;
        //todo 패킷이 깨졌을 상황
        return -1;
    }

    private float[][] convertToDencity(float[][] points) {
        int x = points.length;
        int y = points[0].length;
        float[][] convertPoints = new float[x][y];
        for(int i=0; i < x; i++){
            for(int j=0; j < y;j++){
                convertPoints[i][j] = points[i][j] * resolutionRate;
            }
        }
        return convertPoints;
    }

    private float convertToDencity(float x) {
        return x * resolutionRate;
    }

    public boolean isPlaying() { return isPlayed ; }

    public int videoWidth(){
        if(widthIsLarger)
            return (int)deviceWidth;
        else
            return (int)(deviceHeight * videoWidth / videoHeight);
    }

    public int videoHeight(){
        if(widthIsLarger)
            return (int)(deviceWidth * videoHeight / videoHeight);
        else
            return (int)deviceHeight;
    }

    private void changePage(int page, float runtime){
        clearAllCanvas();
        drawingPaper.invalidate();
        PageManager.getInstance().changePage(page);
        RxEventFactory.get().post(new EventType(EventType.REFASH_PAGE_STATE));
        pageLoader(PageManager.getInstance().getCurrentPageId(), runtime);
    }

    private void clearAllCanvas(){
        drawingPaper.clearCanvas();
        objectPaper.clearCanvas();
    }

    private void printLog(PacketObjectHolder packet){
        try {
            Log.d("drawingPacket", "id : "+packet.getId()+" mid : "+packet.getMid()+" pageId : "+packet.getPageId()+" type : "+packet.getType()+" runtime : "+packet.getRunTime()+"\n" +
                    "body : "+packet.getBody());
        } catch (NullPointerException e){
            e.printStackTrace();
        }

    }

    public void resetPlayer(){
        playTime = 0;
    }

    private void setUndoLoading(boolean doing)
    {
        isUndoLoader = doing;
        drawingPaper.isUndoLoading = doing;
        objectPaper.isUndoLoading = doing;
    }

    public void setLoadCompleted(boolean loadCompleted) {
        this.loadCompleted = loadCompleted;
    }

    public boolean isLoadCompleted(){ return loadCompleted ; }

}