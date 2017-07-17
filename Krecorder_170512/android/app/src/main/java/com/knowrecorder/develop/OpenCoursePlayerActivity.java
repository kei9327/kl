package com.knowrecorder.develop;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.google.gson.Gson;
import com.knowrecorder.Encrypt.SHA1;
import com.knowrecorder.KnowRecorderApplication;
import com.knowrecorder.R;
import com.knowrecorder.RealmMigration.MigrationIOS;
import com.knowrecorder.Toolbox.Toolbox;
import com.knowrecorder.Utils.PermissionChecker;
import com.knowrecorder.Utils.PixelUtil;
import com.knowrecorder.Utils.TimeConverter;
import com.knowrecorder.develop.audio.AudioPlayer;
import com.knowrecorder.develop.event.BringToFrontView;
import com.knowrecorder.develop.event.ChangePage;
import com.knowrecorder.develop.event.EventBus;
import com.knowrecorder.develop.event.EventType;
import com.knowrecorder.develop.event.ObjectDeleteEvent;
import com.knowrecorder.develop.file.FilePath;
import com.knowrecorder.develop.manager.PageManager;
import com.knowrecorder.develop.manager.SharedPreferencesManager;
import com.knowrecorder.develop.model.NoteInfo;
import com.knowrecorder.develop.model.body.VideoPauseBody;
import com.knowrecorder.develop.model.body.VideoStartBody;
import com.knowrecorder.develop.model.realm.Note;
import com.knowrecorder.develop.model.realm.PacketObject;
import com.knowrecorder.develop.model.realm.Page;
import com.knowrecorder.develop.opencourse.PanelWrapper;
import com.knowrecorder.develop.papers.DrawingPanel;
import com.knowrecorder.develop.papers.DrawingPaper;
import com.knowrecorder.develop.papers.ObjectPaperV2;
import com.knowrecorder.develop.papers.PaperObjects.video.VideoView;
import com.knowrecorder.develop.player.PaperPlayer;
import com.knowrecorder.develop.utils.PacketUtil;
import com.knowrecorder.rxjava.RxEventFactory;
import com.yalantis.ucrop.util.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import io.realm.Sort;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by we160303 on 2017-03-03.
 */

public class OpenCoursePlayerActivity extends AppCompatActivity implements View.OnClickListener {
    private final String TAG = "OCPlayerActivity";

    private float totalDuration;
    private boolean isControllPanelHided = false;
    private int videoId;
    private String title;
    private String dbName;

    private Tracker mTracker;
    private String resourcePath;
    private PanelWrapper panelWrapper;
    private Button btnPlaySmall;
    private ImageButton btnPlay;
    private RelativeLayout controllPanel;
    private TextView tvTitle;
    private TextView playTime, totalTime;
    private DrawingPanel drawingPanel;
    private DrawingPaper drawingPaper;
    private ObjectPaperV2 objectPaper;
    private PaperPlayer player;
    private Timer hidingControllPanelTimer;
    private TimerTask hidingControllPanelTimerTask;
    private Timer playerTimer;
    private TimerTask playerTimerTask;

    private CompositeSubscription mSubscription = new CompositeSubscription();
    private EventHandler eventHandler = new EventHandler();

    public static SeekBar seekBar;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(FilePath.isRealmFolderNull()){
            Toast.makeText(this, getResources().getString(R.string.opencourse_play_unavailable), Toast.LENGTH_SHORT).show();
            finish();
        }

        FilePath.isOpencouse = true;
        ProcessStateModel.getInstanse().setIsPlaying(true);
        Toolbox.getInstance().setContext(this);

        KnowRecorderApplication application = (KnowRecorderApplication) getApplication();
        mTracker = application.getDefaultTracker();

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 절전모드 화면 꺼짐 방지
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.rb_player_activity);   //view 추가

        if (!KnowRecorderApplication.isPhone) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        } else {
            if (SharedPreferencesManager.getInstance(this).getOnlyLandscape()) {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
            }
        }

        // IOS에서의 AudioFile 확장자는 m4a, Android에서는 wav 이기떄문에 분기 함... 결국 m4a로 맞춰야함
        if(isExistFile(FilePath.getFilesDirectory() + FilePath.VIEWER_FOLDER_NAME + "_audio1.m4a"))
            AudioPlayer.getInstance(this).setAudioPath(FilePath.getFilesDirectory() + FilePath.VIEWER_FOLDER_NAME + "_audio1.m4a");
        else
            AudioPlayer.getInstance(this).setAudioPath(FilePath.getFilesDirectory() + FilePath.VIEWER_FOLDER_NAME + "_audio1.wav");


        AudioPlayer.getInstance(this).setInitTime(0);
        PermissionChecker pChecker = new PermissionChecker();
        pChecker.check(this, 0);

        setBindViewId();
        tvTitle.setText(getIntent().getStringExtra("VIDEO_TITLE"));
        //신고 하기를 위해 가지고있어야 함

        if(isExistFile(FilePath.VIEWERS_REALM_FOLDER + "default.realm")) {
            Toast.makeText(this, getResources().getString(R.string.old_data), Toast.LENGTH_SHORT).show();
            finish();

            /* 오래된 파일도 재생할지 물어봄. 예전 파일에 대한 realm 필드를 맞춰줘야 플레이 가능.
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.old_data_alert);
            builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    File file = new File(FilePath.VIEWERS_REALM_FOLDER + "default.realm");
                    File file2 = new File(FilePath.VIEWERS_REALM_FOLDER + "default2.realm");
                    File lock1 = new File(FilePath.VIEWERS_REALM_FOLDER + "default.realm.lock");
                    File lock2 = new File(FilePath.VIEWERS_REALM_FOLDER + "default2.realm.lock");
                    try {
                        FilePath.copyFile(file, file2);
                        FilePath.copyFile(lock1, lock2);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    playOpenCourse();
                }
            });
            builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }
            });
            AlertDialog dialog = builder.create();
            dialog.show();
            */
        }else{
            playOpenCourse();
        }
    }

    private void playOpenCourse()
    {

        initDefaultRealm();
        setSubscription();
        PageManager.getInstance().makePageOverTime();

        videoId = getIntent().getIntExtra("VIDEO_ID",-1);

        //여기까지 와야 재생된 데이터 목록
        SharedPreferencesManager sp = SharedPreferencesManager.getInstance(this);
        sp.addRecentData(Integer.toString(videoId));

        drawingPanel.post(new Runnable() {
            @Override
            public void run() {
                playerRewind();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) drawingPanel.getLayoutParams();
        params.gravity = Gravity.CENTER;

        params.width = player.videoWidth();
        params.height = player.videoHeight();

        drawingPanel.setLayoutParams(params);
        drawingPanel.setBackgroundColor(Color.parseColor("#FFFFFFFF"));

        mTracker.setScreenName("PlayerActivity");
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    private void setBindViewId() {
        player = new PaperPlayer(this);

        btnPlay = (ImageButton) findViewById(R.id.btn_play);
        btnPlaySmall = (Button) findViewById(R.id.btn_play_small);
        tvTitle = (TextView) findViewById(R.id.tv_video_title);
        controllPanel = (RelativeLayout) findViewById(R.id.player_control_panel);
        seekBar = (SeekBar) findViewById(R.id.seek_bar);
        playTime = (TextView) findViewById(R.id.tv_time_play);
        totalTime = (TextView) findViewById(R.id.tv_time_total);

        panelWrapper = (PanelWrapper) findViewById(R.id.panel_wrapper);
        drawingPanel = new DrawingPanel(this);
        panelWrapper.addView(drawingPanel);

        drawingPaper = new DrawingPaper(this);
        objectPaper = new ObjectPaperV2(this);
        drawingPanel.addView(objectPaper);
        drawingPanel.addView(drawingPaper);

        player.setDrawingPanel(drawingPanel);
        player.setDrawingPaper(drawingPaper);
        player.setObjectPaper(objectPaper);

        btnPlaySmall.setOnClickListener(this);
        btnPlay.setOnClickListener(this);

        int totalDuration =AudioPlayer.getInstance(this).prevGetDuration();
        totalTime.setText(TimeConverter.convertMillisToStringFormat(totalDuration));
        seekBar.setMax(totalDuration);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            seekBar.setProgressTintList(ColorStateList.valueOf(Color.parseColor("#a0c81e")));
        }

        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                playTime.setText(TimeConverter.convertMillisToStringFormat(progress));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                if(!player.isLoadCompleted())
                    return;

                pause();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(!player.isLoadCompleted())
                    return;

                int progress = seekBar.getProgress();

                if(progress == seekBar.getMax()){
                    controllPanel.setVisibility(View.VISIBLE);
                    btnPlay.setImageResource(R.drawable.btn_viewer_rewind_big);
                    btnPlaySmall.setBackgroundResource(R.drawable.btn_viewer_rewind_s);
                }

                clearAllCanvas();

                loadPaper((float)progress);

                AudioPlayer.getInstance(OpenCoursePlayerActivity.this).setPlayTime(progress);
                player.setPlayTime((float)progress);

                drawingPanel.bringChildToFront(drawingPaper);
            }
        });

        panelWrapper.setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (controllPanel.isShown()) {
                        hideControllPanel();
                    } else {
                        clearHidingControllPanel();
                    }
                }
                return true;
            }
        });

        VideoView videoView = new VideoView(this);
        Matrix m = videoView.getMatrix();
    }

    @Override
    protected void onPause() {
        super.onPause();
        pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //초기화
        FilePath.clearVIewers();
        ProcessStateModel.getInstanse().setIsPlaying(false);

        if(player != null)
            player.resetPlayer();
    }

    private void playerRewind(){
        player.setPlayTime(0);
        AudioPlayer.getInstance(this).setPlayTime(0);
        PageManager.getInstance().changePage(getRuntimeZeroPage(), 0);
        PageManager.getInstance().setCurrentRunTime(0);
    }

    private int getRuntimeZeroPage() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<PacketObject> packetObjects = realm.where(PacketObject.class).equalTo("runtime", 0.f).findAllSorted("runtime", Sort.DESCENDING);
        if (packetObjects.isEmpty())
            return 1;
        else {
            Page page = realm.where(Page.class).equalTo("id", packetObjects.get(0).getPageId()).findFirst();
            return page.getPagenum();
        }

    }

    private boolean isExistFile(String filePath){
        File file = new File(filePath);
        return file.exists();
    }

    private void copyBundledRealmFile(String filePath, String outFileName) {
        try {
            File src = new File(filePath);
            File dst = new File(FilePath.VIEWERS_REALM_FOLDER, outFileName);
            FileInputStream inputStream = new FileInputStream(src);
            FileOutputStream outputStream = new FileOutputStream(dst);
            byte[] buf = new byte[1024];
            int bytesRead;
            while ((bytesRead = inputStream.read(buf)) > 0) {
                outputStream.write(buf, 0, bytesRead);
            }
            outputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void initDefaultRealm() {

        dbName = SHA1.hash(Long.toString(System.currentTimeMillis()));
        copyBundledRealmFile(FilePath.VIEWERS_REALM_FOLDER + "default2.realm", dbName);

        RealmConfiguration config = new RealmConfiguration.Builder()
                .assetFile(FilePath.VIEWERS_REALM_FOLDER + dbName)
                .directory(new File(FilePath.VIEWERS_REALM_FOLDER))
                .name(dbName)
                .schemaVersion(3)
                .migration(new MigrationIOS())
                .build();
        Realm.setDefaultConfiguration(config);

        Log.d("Realmfile",config.toString());


         Realm realm = Realm.getDefaultInstance();
        Note note = realm.where(Note.class).findFirst();

        Log.d(TAG, "this note id is " + note.getNoteId());
        Log.d(TAG, "this note name is " + note.getTitle());
        Log.d(TAG, "this note createDate is " + note.getCreateDate());

        Toolbox.getInstance().setContext(this);
        PixelUtil.getInstance().setContext(this);
        totalDuration = note.getTotalTime();
        NoteInfo noteInfo = (new Gson()).fromJson(note.getInfo(), NoteInfo.class);
        realm.close();

        setNoteInit(noteInfo);

    }
    private void setNoteInit(NoteInfo info) {

        int sumOfPage = getSumOfPage();

        PageManager.getInstance().setCurrentPage(1);
        PageManager.getInstance().setSumOfPage(sumOfPage);
        PageManager.getInstance().initPage(sumOfPage);
        try {
            player.setVideoResolution(info.getWidth(), info.getHeight(), info.getDensity());
        }catch (NullPointerException np){
            player.setVideoResolution(1024,768,1);
        }
    }

    private void setSubscription() {
        mSubscription.add(RxEventFactory.get().subscribe(EventType.class,
                new Action1<EventType>() {
                    @Override
                    public void call(EventType eventType) {
                        Message msg = eventHandler.obtainMessage();
                        msg.what = EventBus.EVENT_TYPE;
                        msg.obj = eventType;
                        eventHandler.sendMessage(msg);
                    }
                }));

        mSubscription.add(RxEventFactory.get().subscribe(BringToFrontView.class,
                new Action1<BringToFrontView>() {
                    @Override
                    public void call(BringToFrontView bringToFrontView) {
                        Message msg = eventHandler.obtainMessage();
                        msg.what = EventBus.BRING_TO_FRONT_VIEW;
                        msg.obj = bringToFrontView;
                        eventHandler.sendMessage(msg);
                    }
                }));

        mSubscription.add(RxEventFactory.get().subscribe(ObjectDeleteEvent.class,
                new Action1<ObjectDeleteEvent>() {
                    @Override
                    public void call(ObjectDeleteEvent objectDeleteEvent) {
                        Message msg = eventHandler.obtainMessage();

                        msg.what = EventBus.OBJECT_DELETE;
                        msg.obj = objectDeleteEvent;
                        eventHandler.sendMessage(msg);
                    }
                }));

        mSubscription.add(RxEventFactory.get().subscribe(ChangePage.class,
                new Action1<ChangePage>() {
                    @Override
                    public void call(ChangePage changePage) {
                        Message msg = eventHandler.obtainMessage();
                        msg.what = EventBus.CHANGE_PAGE;
                        msg.obj = changePage;
                        eventHandler.sendMessage(msg);
                    }
                }));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_play_small :
            case R.id.btn_play :
                if(player.isPlaying())
                    pause();
                else
                    play();
                break;
        }
    }

    private void play(){
        checkPlayerEnd();

        player.playPacket();
        playTimer();

        if (objectPaper.isThisPageVideoInsert()) {
            if (isCurrentVideoStatePlay()){
                objectPaper.videoStart(objectPaper.getVideoMid());
            }
        }

        btnPlay.setImageResource(R.drawable.btn_viewer_pause_big);
        btnPlaySmall.setBackgroundResource(R.drawable.btn_viewer_pause);
        clearHidingControllPanel();
    }
    private void pause(){
        player.stopPacket();
        stopTImer();

        if (objectPaper.isThisPageVideoInsert()) {
            objectPaper.videoPause(objectPaper.getVideoMid());
        }

        btnPlay.setImageResource(R.drawable.btn_viewer_play_big);
        btnPlaySmall.setBackgroundResource(R.drawable.btn_viewer_play);
        clearHidingControllPanel();
    }

    private void playTimer(){
        playerTimer = new Timer();
        playerTimerTask = new PlayerTimerTask();

        playerTimer.schedule(playerTimerTask, 0, 100);
    }
    private void stopTImer(){
        try{
            playerTimer.purge();
            playerTimer.cancel();
            playerTimer = null;
        }catch (NullPointerException ne){
            ne.printStackTrace();
        }

        try{
            playerTimerTask.cancel();
            playerTimerTask = null;
        }catch (NullPointerException ne){
            ne.printStackTrace();
        }
    }


    private void checkPlayerEnd(){

        if(AudioPlayer.getInstance(this).getPlayTime() + 40 >= AudioPlayer.getInstance(this).prevGetDuration()){
            player.setPlayTime(0);
            AudioPlayer.getInstance(this).setPlayTime(0);
            PageManager.getInstance().setCurrentRunTime(0);
            clearAllCanvas();
        }
    }



    private void clearHidingControllPanel(){
        controllPanel.setVisibility(View.VISIBLE);
        isControllPanelHided = true;

        try{
            hidingControllPanelTimer.cancel();
            hidingControllPanelTimer.purge();
            hidingControllPanelTimer = null;

        }catch (NullPointerException ne){

        }

        try{
            hidingControllPanelTimerTask.cancel();
            hidingControllPanelTimerTask = null;
        }catch (NullPointerException ne){

        }

        hidingControllPanelTimer = new Timer();
        hidingControllPanelTimerTask = new HidingPanelTask();
        hidingControllPanelTimer.schedule(hidingControllPanelTimerTask, 3000);
    }
    private void hideControllPanel(){
        isControllPanelHided = false;
        controllPanel.setVisibility(View.GONE);
    }

    class HidingPanelTask extends TimerTask{
        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    hideControllPanel();
                }
            });
        }
    }
    class PlayerTimerTask extends TimerTask{

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    int currentPosition =  AudioPlayer.getInstance(OpenCoursePlayerActivity.this).getCurrentPosition();
                    if(currentPosition  == -1)
                        return;

                    playTime.setText(TimeConverter.convertMillisToStringFormat(currentPosition));
                    seekBar.setProgress(currentPosition);
                }
            });
        }
    }

    public int getSumOfPage() {
        Realm realm = Realm.getDefaultInstance();
        int sumOfPage = realm.where(Page.class).max("pagenum").intValue();
        realm.close();

        return sumOfPage;
    }

    public boolean isCurrentVideoStatePlay() {
        boolean isPlayState;
        Realm realm = Realm.getDefaultInstance();
        RealmResults<PacketObject> packets = realm.where(PacketObject.class)
                .equalTo("pageid", PageManager.getInstance().getCurrentPageId())
                .beginGroup()
                .equalTo("type", PacketUtil.S_VIDEOSTART)
                .or()
                .equalTo("type",PacketUtil.S_VIDEOPAUSE)
                .endGroup()
                .lessThanOrEqualTo("runtime", PageManager.getInstance().getCurrentRunTime())
                .findAllSorted("runtime", Sort.DESCENDING);
        if(packets.size() != 0)
        {
            isPlayState = TextUtils.equals(PacketUtil.S_VIDEOSTART, packets.get(0).getType());
        }else{
            isPlayState = false ;
        }
        realm.close();

        return isPlayState;
    }

    private void setVideoSeek() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<PacketObject> packets = realm.where(PacketObject.class)
                .equalTo("pageid", PageManager.getInstance().getCurrentPageId())
                .beginGroup()
                .equalTo("type",PacketUtil.S_VIDEOSTART)
                .or()
                .equalTo("type",PacketUtil.S_VIDEOPAUSE)
                .endGroup()
                .lessThanOrEqualTo("runtime", PageManager.getInstance().getCurrentRunTime())
                .findAllSorted("runtime", Sort.DESCENDING);
        if(packets.size() == 0){
            realm.close();
            return;
        }

        Gson gson = new Gson();
        float seekTo;
        if(TextUtils.equals(packets.get(0).getType(), PacketUtil.S_VIDEOSTART)) {
            VideoStartBody body = gson.fromJson(packets.get(0).getBody(), VideoStartBody.class);
            float videoGap = PageManager.getInstance().getCurrentRunTime() - packets.get(0).getRunTime();
            float totalVideoTime = objectPaper.getVideoTotalTime();
            seekTo = body.getStartvalue() + (videoGap / totalVideoTime);
        }else{
            VideoPauseBody body = gson.fromJson(packets.get(0).getBody(), VideoPauseBody.class);
            seekTo = body.getEndvalue();
        }
        objectPaper.videoSeekTo(objectPaper.getVideoMid(), seekTo);
        realm.close();
    }



    private class EventHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch(msg.what){
                case EventBus.EVENT_TYPE :
                    switch (((EventType)msg.obj).getEvent()) {
                        case EventType.PLAYER_END:
                            playerEnd();
                            break;
                        case EventType.LOAD_COMPLETED :
                            if(objectPaper.isThisPageVideoInsert()){
                                setVideoSeek();
                            }
                            break;
                    }
                    break;
                case EventBus.BRING_TO_FRONT_VIEW :
                    Log.d(RecordingBoardActivity.class.getSimpleName(), "this event is bring to front");
                    BringToFront(((BringToFrontView)msg.obj).getViewType());
                    break;

                case EventBus.CHANGE_PAGE :

                    ChangePage changePage = (ChangePage)msg.obj;
                    clearAllCanvas();

                    if (changePage.getPageid() != -1 && changePage.getTimeStamp() != -1) {
                        loadPaper(changePage.getPageid(), changePage.getTimeStamp());
                    } else if (changePage.getPageid() != -1 && changePage.getTimeStamp() == -1) {
                        loadPaper(changePage.getPageid());
                    } else if (changePage.getPageid() == -1 && changePage.getTimeStamp() != -1) {
                        loadPaper(changePage.getTimeStamp());
                    }
                    break;

                case EventBus.OBJECT_DELETE :
                    deleteObject(((ObjectDeleteEvent)msg.obj).getMid());
                    break;
            }
        }
    }

    private void playerEnd(){
        player.stopPacket();
        stopTImer();

        controllPanel.setVisibility(View.VISIBLE);
        btnPlay.setImageResource(R.drawable.btn_viewer_rewind_big);
        btnPlaySmall.setBackgroundResource(R.drawable.btn_viewer_rewind_s);
    }

    private void BringToFront(int viewType) {
        if (viewType == RecordingBoardActivity.DRAWING_PAPER) {
            drawingPanel.bringChildToFront(drawingPaper);
        } else if (viewType == RecordingBoardActivity.OBJECT_PAPER) {
            drawingPanel.bringChildToFront(objectPaper);
        } else {
            drawingPanel.bringChildToFront(objectPaper);
            drawingPanel.bringChildToFront(drawingPaper);
        }
    }

    private void clearAllCanvas() {
        drawingPaper.clearCanvas();
        objectPaper.clearCanvas();
        drawingPanel.invalidate();
    }

    private void loadPaper(long pageid) {
        player.pageLoader(pageid);
    }

    private void loadPaper(float timeStamp) {
        long pageId = PageManager.getInstance().getCurrentPageProgressPageId(timeStamp);
        if( ( pageId> 0 ) && PageManager.getInstance().getCurrentPageId() != pageId) {
            PageManager.getInstance().changePageId(pageId);
        }
        loadPaper(pageId, timeStamp);

    }

    private void loadPaper(long pageid, float timeStamp) {
        player.pageLoader(pageid, timeStamp);
    }
    private void deleteObject(long mid) {
        objectPaper.deleteObject(mid);
    }


}
