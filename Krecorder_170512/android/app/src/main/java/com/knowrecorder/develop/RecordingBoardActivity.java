package com.knowrecorder.develop;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.Gson;
import com.knowrecorder.Constants.ServerInfo;
import com.knowrecorder.KnowRecorderApplication;
import com.knowrecorder.OpenCourse.API.Models.UploadVideo;
import com.knowrecorder.OpenCourse.API.Models.UserInfo;
import com.knowrecorder.OpenCourse.ApiEndPointInterface;
import com.knowrecorder.R;
import com.knowrecorder.Toolbox.Toolbox;
import com.knowrecorder.Utils.E;
import com.knowrecorder.Utils.PermissionChecker;
import com.knowrecorder.Utils.PixelUtil;
import com.knowrecorder.Utils.TimeConverter;
import com.knowrecorder.Widgets.StrokePreview;
import com.knowrecorder.develop.audio.AudioPlayer;
import com.knowrecorder.develop.audio.AudioRecorder;
import com.knowrecorder.develop.controller.RealmPacketPutter;
import com.knowrecorder.develop.controller.RecordingBoardController;
import com.knowrecorder.develop.event.BringToFrontView;
import com.knowrecorder.develop.event.ChangePage;
import com.knowrecorder.develop.event.EventBus;
import com.knowrecorder.develop.event.EventOpenCourseExport;
import com.knowrecorder.develop.event.EventType;
import com.knowrecorder.develop.event.ExportSelectedDialogEvent;
import com.knowrecorder.develop.event.ObjectDeleteEvent;
import com.knowrecorder.develop.file.FilePath;
import com.knowrecorder.develop.fragment.Dialog.ExportDialog;
import com.knowrecorder.develop.fragment.Dialog.HelpDialog;
import com.knowrecorder.develop.fragment.Dialog.InformationDialog;
import com.knowrecorder.develop.fragment.Dialog.OpenCourseDialog;
import com.knowrecorder.develop.fragment.Dialog.ServiceGuideDialog;
import com.knowrecorder.develop.fragment.Dialog.SettingDialog;
import com.knowrecorder.develop.fragment.LeftMenu.LeftMenuFragment;
import com.knowrecorder.develop.fragment.TimeLine.TImeLineFragment;
import com.knowrecorder.develop.manager.NoteManager;
import com.knowrecorder.develop.manager.PageManager;
import com.knowrecorder.develop.manager.SharedPreferencesManager;
import com.knowrecorder.develop.model.NoteInfo;
import com.knowrecorder.develop.model.body.VideoPauseBody;
import com.knowrecorder.develop.model.body.VideoStartBody;
import com.knowrecorder.develop.model.packetHolder.ObjectControllPacket;
import com.knowrecorder.develop.model.realm.Note;
import com.knowrecorder.develop.model.realm.PacketObject;
import com.knowrecorder.develop.model.realm.Page;
import com.knowrecorder.develop.opencourse.ocexport.OCExportTask;
import com.knowrecorder.develop.papers.DrawingPanel;
import com.knowrecorder.develop.papers.DrawingPaper;
import com.knowrecorder.develop.papers.ObjectPaperV2;
import com.knowrecorder.develop.player.PaperPlayer;
import com.knowrecorder.develop.utils.PacketUtil;
import com.knowrecorder.rxjava.RxEventFactory;
import com.soundcloud.android.crop.Crop;
import com.yalantis.ucrop.UCrop;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;
import io.realm.Sort;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.functions.Action1;
import rx.subscriptions.CompositeSubscription;

/**
 * Created by we160303 on 2017-01-31.
 */

public class RecordingBoardActivity extends AppCompatActivity implements TImeLineFragment.TimeLineListener,E{

    private final String TAG = "RBActivity";

    public static final int DRAWING_PAPER = 999;


    public static final int OBJECT_PAPER = DRAWING_PAPER + 1;
    public static final int DRAWING_OBJECT_PAPER = OBJECT_PAPER + 1;
    private static final int READ_PDF_CODE = 42; // PDF 파일

    private static final int READ_IMAGE_CODE = 43; // 이미지 파일
    private static final int READ_VIDEO_FILE = 44;

    private static final String SAMPLE_CROPPED_IMAGE_NAME = "SampleCropImage";

    private CallbackManager callbackManager;

    private FrameLayout mainPanel;
    private DrawerLayout drawerLayout;
    private FrameLayout timeLineContainer;

    private DrawingPanel drawingPanel;
    private DrawingPaper drawingPaper;
    private ObjectPaperV2 objectPaper;

    //Drawing Tools
    LinearLayout rightToolWrapper;
    CheckBox btnImage;
    CheckBox btnShape;
    CheckBox btnPalm;
    CheckBox btnFinger;
    CheckBox btnPointer;
    CheckBox btnPen;
    CheckBox btnText;
    CheckBox btnEraser;
    CheckBox btnPdf;
    CheckBox btnRemove;
    CheckBox btnVideo;

    ImageButton btnUndo;
    ImageButton btnRedo;

    //Control Tools
    android.widget.ImageView btnLeftMenu;
    android.widget.ImageView btnRecord;
    android.widget.ImageView btnPlayPause;
    android.widget.ImageView btnAddPage;
    android.widget.ImageView btnPrevPage;
    android.widget.ImageView btnNextPage;
    android.widget.ImageView btnRewind;
    android.widget.ImageView btnForward;
    android.widget.ImageView btnSetting;
    android.widget.ImageView btnExportToVideo;
    android.widget.ImageView btnTimeline;

    TextView txtPageStatus;
    TextView txtTimer;


    private RecordingBoardController controller;

    private Toolbox.Tooltype currentTool;

    private View mPopup = null;

    private PaperPlayer player;

    private CompositeSubscription mSubscription = new CompositeSubscription();

    private EventHandler eventHandler = new EventHandler();

    private Timer recordTimer;
    private TimerTask recordTimerTask;

    public static int statusBarSize;

    private TImeLineFragment tImeLineFragment = null;
    private LeftMenuFragment leftMenuFragment;

    private Retrofit retrofit;
    private ApiEndPointInterface apiService;

    private AlertDialog loginDialog;
    private SettingDialog settingDialog;
    private boolean twiceBackpressedToExit = false;
    private boolean isRestartRecord = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); // 절전모드 화면 꺼짐 방지
        FilePath.isOpencouse = false;
        setContentView(R.layout.recording_board_activity);

        retrofit = new Retrofit.Builder()
                .baseUrl(ServerInfo.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiEndPointInterface.class);

        facebookLogin();

        controller = new RecordingBoardController(this);
        controller.currentInitNote();

        statusBarSize = getStatusBarHeight();

        Toolbox.getInstance().setContext(this);
        PixelUtil.getInstance().setContext(this);

        PermissionChecker pChecker = new PermissionChecker();
        pChecker.check(this, 0);
        setInitView();
        setSubscription();
        setNoteInit();
        checkUndoRedo();


        drawingPanel.post(new Runnable() {
            @Override
            public void run() {
                //todo 초기 drawing작업
                playerForward();
            }
        });
    }

    private void checkUndoRedo() {

        btnRedo.setEnabled(false);
        btnRedo.setAlpha(ALPHA.INACTIVE);
        btnUndo.setEnabled(false);
        btnUndo.setAlpha(ALPHA.INACTIVE);

        /*undo disable
        Realm realm = Realm.getDefaultInstance();
        realm.addChangeListener(new RealmChangeListener<Realm>() {
            @Override
            public void onChange(Realm element) {
                if(PageManager.getInstance().exeUndo(false)){
                    btnUndo.setEnabled(true);
                    btnUndo.setAlpha(ALPHA.ACTIVE);
                }else
                {
                    btnUndo.setEnabled(false);
                    btnUndo.setAlpha(E.ALPHA.INACTIVE);
                }

                if(PageManager.getInstance().exeRedo(false)){
                    btnRedo.setEnabled(true);
                    btnRedo.setAlpha(ALPHA.ACTIVE);
                }else
                {
                    btnRedo.setEnabled(false);
                    btnRedo.setAlpha(ALPHA.INACTIVE);
                }
            }
        });
        */
    }

    private void resetUndoRedo()
    {
        btnRedo.setEnabled(false);
        btnRedo.setAlpha(ALPHA.INACTIVE);
        btnUndo.setEnabled(false);
        btnUndo.setAlpha(ALPHA.INACTIVE);
    }

    private void facebookLogin() {
        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        LoginManager.getInstance().registerCallback(callbackManager,
                new FacebookCallback<LoginResult>() {
                    @Override
                    public void onSuccess(final LoginResult loginResult) {
                        Log.d("MainActivity", "Facebook login success");
                        try {
                            loginDialog.dismiss();
                        } catch (Exception e) {

                        }

                        GraphRequest request = GraphRequest.newMeRequest(loginResult.getAccessToken(),
                                new GraphRequest.GraphJSONObjectCallback() {
                                    @Override
                                    public void onCompleted(JSONObject object, GraphResponse response) {
                                        Log.d("GraphRequest", response.toString());

                                        try {
                                            final String id = object.getString("id");
                                            String name = object.getString("name");
                                            String locale = object.getString("locale");
                                            String gender = object.getString("gender");
                                            String picture = object.getString("picture");
                                            String timezone = object.getString("timezone");
                                            String email = "";
                                            try {
                                                email = object.getString("email");
                                            } catch (JSONException e) {
                                                e.printStackTrace();
                                            }
                                            String snsType = "fb";
                                            String accessToken = loginResult.getAccessToken().getToken();

                                            try {
                                                    settingDialog.fbSetText(name);
                                            } catch (Exception e) {

                                            }

                                            JSONObject jsonObject = new JSONObject(picture);
                                            jsonObject = jsonObject.optJSONObject("data");
                                            picture = jsonObject.optString("url");

                                            final UserInfo userInfo = new UserInfo();
                                            userInfo.setFbUserId(id);
                                            userInfo.setName(name);
                                            userInfo.setLocale(locale);
                                            userInfo.setGender(gender);
                                            userInfo.setPicture(picture);
                                            userInfo.setTimezone(timezone);
                                            userInfo.setEmail(email);
                                            userInfo.setSnsType(snsType);
                                            userInfo.setAccessToken(accessToken);

                                            Call<ResponseBody> createUser = apiService.createUser(ServerInfo.API_KEY, userInfo);
                                            createUser.enqueue(new Callback<ResponseBody>() {
                                                @Override
                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                    if (response.code() != 201) {
                                                        Call<ResponseBody> updateUser = apiService.updateUser("fb", id, ServerInfo.API_KEY, userInfo);
                                                        updateUser.enqueue(new Callback<ResponseBody>() {
                                                            @Override
                                                            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                                Log.d("updateUser()", "fb_user_id: " + id);
                                                            }

                                                            @Override
                                                            public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                                Log.d("updateUser()", "fail to update user");
                                                            }
                                                        });
                                                    } else {
                                                        Log.d("createUser()", "create user success");
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                                    Log.d("createUser()", "fail to create user");
                                                }
                                            });

                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                });

                        Bundle parameters = new Bundle();
                        parameters.putString("fields", "id,name,email,locale,gender,picture,timezone");
                        request.setParameters(parameters);
                        request.executeAsync();
                    }

                    @Override
                    public void onCancel() {
                        Log.d("MainActivity", "Facebook login cancel");
                    }

                    @Override
                    public void onError(FacebookException error) {
                        Log.d("MainActivity", "Facebook login error : " + error.getMessage());
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        callbackManager.onActivityResult(requestCode, resultCode, resultData);

        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case READ_IMAGE_CODE:
                startCropActivity(resultData.getData());
                break;
            case UCrop.REQUEST_CROP:
                try {
                    chkRestartRecord();
                    handleCrop(resultCode, resultData);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            case READ_VIDEO_FILE:
                chkRestartRecord();
                new FileCopyTask().execute(new CUri(resultData.getData(), READ_VIDEO_FILE));
                break;

            case READ_PDF_CODE :
                chkRestartRecord();
                new FileCopyTask().execute(new CUri(resultData.getData(), READ_PDF_CODE));
                break;
        }
    }

    private void chkRestartRecord(){
        if(isRestartRecord) {
            startRecord();
            isRestartRecord = false;
        }
    }

    private void startCropActivity(@NonNull Uri uri) {
        String destinationFileName = SAMPLE_CROPPED_IMAGE_NAME + ".png";

        UCrop uCrop = UCrop.of(uri, Uri.fromFile(new File(getCacheDir(), destinationFileName)));
        uCrop.start(RecordingBoardActivity.this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "LCH , onReusme");
        FilePath.isOpencouse = false;
        controller.setPrevRealmAndAudioFile();
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "LCH , onPause");
        if(ProcessStateModel.getInstanse().isRecording()) {
            stopRecord();
        }

        if(ProcessStateModel.getInstanse().isPlaying())
            playerStop();

        RealmPacketPutter.getInstance().allPacketSave(null);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    public void onBackPressed() {
        objectPaper.currentFocusedTextMid = 0;
        Log.d(TAG, "onBackPressed");
        if (twiceBackpressedToExit) {

            if(ProcessStateModel.getInstanse().isRecording())
                stopRecord();
            mSubscription.unsubscribe();

            System.exit(0); // 애플리케이션 종료
        }

        twiceBackpressedToExit = true;
        Toast.makeText(this, R.string.twice_backpress_to_exit, Toast.LENGTH_SHORT).show();
        RealmPacketPutter.getInstance().allPacketSave(null);

        new Handler().postDelayed(new Runnable() {

            @Override
            public void run() {
                twiceBackpressedToExit = false;
            }
        }, 2000);
    }

    private void setSubscription() {
        mSubscription.add(RxEventFactory.get().subscribe(EventType.class,
                new Action1<EventType>() {
                    @Override
                    public void call(EventType eventType) {
                        if(FilePath.isOpencouse)
                            return;

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
                        if(FilePath.isOpencouse)
                            return;

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
                        if(FilePath.isOpencouse)
                            return;

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
                        if(FilePath.isOpencouse)
                            return;

                        Message msg = eventHandler.obtainMessage();
                        msg.what = EventBus.CHANGE_PAGE;
                        msg.obj = changePage;
                        eventHandler.sendMessage(msg);
                    }
                }));

        mSubscription.add(RxEventFactory.get().subscribe(ExportSelectedDialogEvent.class,
                new Action1<ExportSelectedDialogEvent>() {
                    @Override
                    public void call(ExportSelectedDialogEvent exportSelectedDialogEvent) {
                        if(FilePath.isOpencouse)
                            return;

                        Message msg = eventHandler.obtainMessage();
                        msg.what = EventBus.EXPORT_SELECTED;
                        msg.obj = exportSelectedDialogEvent;
                        eventHandler.sendMessage(msg);
                    }
                }));

        mSubscription.add(RxEventFactory.get().subscribe(EventOpenCourseExport.class,
                new Action1<EventOpenCourseExport>() {
                    @Override
                    public void call(EventOpenCourseExport eventOpenCourseExport) {
                        if(FilePath.isOpencouse)
                            return;

                        Message msg = eventHandler.obtainMessage();
                        msg.what = EventBus.OPEN_COURSE_EXPORT;
                        msg.obj = eventOpenCourseExport;
                        eventHandler.sendMessage(msg);
                    }
                }));
    }


    private void setNoteInit() {

        Realm realm = Realm.getDefaultInstance();

        Note note = realm.where(Note.class).findFirst();
        NoteInfo noteInfo = (new Gson()).fromJson(note.getInfo(), NoteInfo.class);

        int sumOfPage = getSumOfPage();
        //페이지 상태 초기화
        PageManager.getInstance().setCurrentPage(getRuntimeZeroPage());
        PageManager.getInstance().setSumOfPage(sumOfPage);
        PageManager.getInstance().initPage(sumOfPage);

        //플레이어 해상도 초기화
        player.setVideoResolution(noteInfo.getWidth(), noteInfo.getHeight(), noteInfo.getDensity());

        //Audio초기화
        long lastRunTIme = 0;
        try {
            lastRunTIme = realm.where(PacketObject.class).max("runtime").longValue();
        }catch(NullPointerException ne){
            lastRunTIme = 0;
        }finally {
            ProcessStateModel.getInstanse().setLastRecordingTime(lastRunTIme);
        }

        realm.close();
    }

    public int getSumOfPage() {
        Realm realm = Realm.getDefaultInstance();
        int sumOfPage = realm.where(Page.class).max("pagenum").intValue();
        realm.close();

        return sumOfPage;
    }

    private int getRuntimeZeroPage() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<PacketObject> packetObjects = realm.where(PacketObject.class).equalTo("runtime", 0.f).findAllSorted("runtime", Sort.DESCENDING);
        if (packetObjects.isEmpty())
            return 1;
        else {
            Page page = realm.where(Page.class).equalTo("id", packetObjects.get(0).getPageId()).findFirst();
            if(page == null){
                return getLastPacketPage();
            }else{
                Crashlytics.log(1, "[RuntimeZero page id] = ", Integer.toString((int) packetObjects.get(0).getPageId()) );
                Crashlytics.log(1, "[getLastPacketPage()] = ", Integer.toString(getLastPacketPage()) );
                return page.getPagenum();
            }
        }
    }

    private int getLastPacketPage() {
        Realm realm = Realm.getDefaultInstance();
        RealmResults<PacketObject> packetObjects = realm.where(PacketObject.class).findAllSorted("runtime", Sort.DESCENDING);
        if (packetObjects.isEmpty())
            return 1;
        else {
            Page page = realm.where(Page.class).equalTo("id", packetObjects.get(0).getPageId()).findFirst();
            if(page != null) {
                return page.getPagenum();
            }
            return  1;
        }
    }


    private void setInitView() {

        player = new PaperPlayer(this);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawerlayout);
        mainPanel = (FrameLayout) findViewById(R.id.fl_activity_main_container);
        timeLineContainer = (FrameLayout) findViewById(R.id.timelist_container);

        leftMenuFragment = new LeftMenuFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.left_menu_container, leftMenuFragment, "leftMenu").commitAllowingStateLoss();

        setNewTImeLine();
        timeLineContainer.setVisibility(View.GONE);

        settingDialog = new SettingDialog();

        drawerLayout.setScrimColor(Color.parseColor("#B3FFFFFF"));

        drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

            }

            @Override
            public void onDrawerOpened(View drawerView) {
                currentTool = Toolbox.getInstance().getToolType();
                Toolbox.getInstance().setToolType(Toolbox.Tooltype.NONE);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                Toolbox.getInstance().setToolType(currentTool);
            }

            @Override
            public void onDrawerStateChanged(int newState) {

            }
        });

        addDrawingPanel();

        setPrimaryKey();
        setBindView();
    }

    private void addDrawingPanel() {

        drawingPanel = new DrawingPanel(this);
        drawingPaper = new DrawingPaper(this);
        objectPaper = new ObjectPaperV2(this);

        drawingPaper.setClickable(true);
        objectPaper.setClickable(true);

        drawingPanel.addView(drawingPaper);
        drawingPanel.addView(objectPaper);

        mainPanel.addView(drawingPanel, 0);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);
        params.setMargins(0,0,0,(int)PixelUtil.getInstance().convertDpToPixel(45));
        drawingPanel.setLayoutParams(params);

        drawingPanel.setBackgroundColor(Color.parseColor("#FFFFFF"));

        player.setDrawingPanel(drawingPanel);
        player.setDrawingPaper(drawingPaper);
        player.setObjectPaper(objectPaper);


    }

    private void setPrimaryKey() {
        Realm realm = Realm.getDefaultInstance();
        try {
            DrawingPanel.id.set(realm.where(PacketObject.class).max("id").intValue());
            DrawingPanel.id.set(realm.where(PacketObject.class).max("id").intValue());
        } catch (NullPointerException n) {
            DrawingPanel.id.set(0);
        }

        try {
            DrawingPanel.mid.set(realm.where(PacketObject.class).max("mid").intValue());
        } catch (NullPointerException n) {
            DrawingPanel.mid.set(0);
        }

        try {
            DrawingPanel.pageId.set(realm.where(Page.class).max("id").intValue());
        } catch (NullPointerException n) {
            DrawingPanel.pageId.set(0);
        }


    }

    private void setBindView() {
        //Drawing Tools
        rightToolWrapper = (LinearLayout) findViewById(R.id.right_tool_wrapper);
        btnImage = (CheckBox) findViewById(R.id.btn_image);
        btnShape = (CheckBox) findViewById(R.id.btn_shape);
        btnPalm = (CheckBox) findViewById(R.id.btn_palm);
        btnFinger = (CheckBox) findViewById(R.id.btn_finger);
        btnPointer = (CheckBox) findViewById(R.id.btn_pointer);
        btnPen = (CheckBox) findViewById(R.id.btn_pen);
        btnText = (CheckBox) findViewById(R.id.btn_text);
        btnEraser = (CheckBox) findViewById(R.id.btn_eraser);
        btnPdf = (CheckBox) findViewById(R.id.btn_add_pdf);
        btnRemove = (CheckBox) findViewById(R.id.btn_remove);
        btnVideo = (CheckBox) findViewById(R.id.btn_add_video);
        btnUndo = (ImageButton) findViewById(R.id.btn_undo);
        btnRedo = (ImageButton) findViewById(R.id.btn_redo);

        btnImage.setOnClickListener(onRightToolClick);
        btnShape.setOnClickListener(onRightToolClick);
        btnPalm.setOnClickListener(onRightToolClick);
        btnFinger.setOnClickListener(onRightToolClick);
        btnPointer.setOnClickListener(onRightToolClick);
        btnPen.setOnClickListener(onRightToolClick);
        btnText.setOnClickListener(onRightToolClick);
        btnEraser.setOnClickListener(onRightToolClick);
        btnPdf.setOnClickListener(onRightToolClick);
        btnRemove.setOnClickListener(onRightToolClick);
        btnVideo.setOnClickListener(onRightToolClick);
        btnUndo.setOnClickListener(onRightToolClick);
        btnRedo.setOnClickListener(onRightToolClick);

        //Control Tools
        btnLeftMenu = (android.widget.ImageView) findViewById(R.id.btn_left_menu);
        btnRecord = (android.widget.ImageView) findViewById(R.id.btn_record);
        btnPlayPause = (android.widget.ImageView) findViewById(R.id.btn_play_pause);
        btnAddPage = (android.widget.ImageView) findViewById(R.id.btn_add_page);
        btnPrevPage = (android.widget.ImageView) findViewById(R.id.btn_prev_page);
        btnNextPage = (android.widget.ImageView) findViewById(R.id.btn_next_page);

        btnRewind = (android.widget.ImageView) findViewById(R.id.btn_rewind);
        btnForward = (android.widget.ImageView) findViewById(R.id.btn_forward);
        btnSetting = (android.widget.ImageView) findViewById(R.id.btn_setting);
        btnExportToVideo = (android.widget.ImageView) findViewById(R.id.btn_export_to_video);
        btnTimeline = (android.widget.ImageView) findViewById(R.id.btn_timeline);

        txtPageStatus = (TextView) findViewById(R.id.txt_page_status);
        txtTimer = (TextView) findViewById(R.id.txt_timer);

        btnLeftMenu.setOnClickListener(onBottomToolClick);
        btnRecord.setOnClickListener(onBottomToolClick);
        btnPlayPause.setOnClickListener(onBottomToolClick);
        btnAddPage.setOnClickListener(onBottomToolClick);
        btnPrevPage.setOnClickListener(onBottomToolClick);
        btnNextPage.setOnClickListener(onBottomToolClick);

        btnRewind.setOnClickListener(onBottomToolClick);
        btnForward.setOnClickListener(onBottomToolClick);
        btnSetting.setOnClickListener(onBottomToolClick);
        btnExportToVideo.setOnClickListener(onBottomToolClick);
        btnTimeline.setOnClickListener(onBottomToolClick);


        txtPageStatus.setOnClickListener(onBottomToolClick);



    }

    private View.OnClickListener onRightToolClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent intent;
            if (ProcessStateModel.getInstanse().isPlaying())
                return;

            closePopup();

            switch (v.getId()) {
                //Recorder Right Menu
                case R.id.btn_image:
                    isRestartRecord = ProcessStateModel.getInstanse().isRecording();
                    Toolbox.getInstance().setToolType(Toolbox.Tooltype.IMAGE);
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("image/*");
                    startActivityForResult(intent, READ_IMAGE_CODE);
                    break;

                case R.id.btn_add_video:

                    if(objectPaper.isThisPageVideoInsert()) {
                        Toast.makeText(RecordingBoardActivity.this, getResources().getString(R.string.onepage_onevideo), Toast.LENGTH_SHORT).show();
                        break;
                    }
                    isRestartRecord = ProcessStateModel.getInstanse().isRecording();
                    Toolbox.getInstance().setToolType(Toolbox.Tooltype.VIDEO);
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("video/*");
                    startActivityForResult(intent, READ_VIDEO_FILE);
                    break;

                case R.id.btn_add_pdf:
                    isRestartRecord = ProcessStateModel.getInstanse().isRecording();
                    Toolbox.getInstance().setToolType(Toolbox.Tooltype.PDF);
                    intent = new Intent(Intent.ACTION_GET_CONTENT);
                    intent.addCategory(Intent.CATEGORY_OPENABLE);
                    intent.setType("application/pdf");
                    startActivityForResult(intent, READ_PDF_CODE);
                    break;

                case R.id.btn_finger:
                    Toolbox.getInstance().setToolType(Toolbox.Tooltype.FINGER);
                    break;

                case R.id.btn_pointer:
                    Toolbox.getInstance().setToolType(Toolbox.Tooltype.POINTER);
                    mPopup = showPopup(btnPointer, R.layout.view_pointer_selection);
                    break;

                case R.id.btn_pen:
                    Toolbox.getInstance().setToolType(Toolbox.Tooltype.PEN);
                    mPopup = showPopup(btnPen, R.layout.view_pen_selection);
                    break;

                case R.id.btn_text:
                    Toolbox.getInstance().setToolType(Toolbox.Tooltype.TEXT);
                    break;

                case R.id.btn_shape:
                    Toolbox.getInstance().setToolType(Toolbox.Tooltype.SHAPE);
                    mPopup = showPopup(btnShape, R.layout.view_shape_selection);
                    break;

                case R.id.btn_eraser:
                    Toolbox.getInstance().setToolType(Toolbox.Tooltype.ERASER);
                    mPopup = showPopup(btnEraser, R.layout.view_eraser_selection);
                    break;

                case R.id.btn_remove:
                    Toolbox.getInstance().setToolType(Toolbox.Tooltype.REMOVE);
                    break;

                case R.id.btn_undo:
                    PageManager.getInstance().exeUndo(true);
                    RealmPacketPutter.getInstance().allPacketSave(new RealmPacketPutter.SaveResult() {
                        @Override
                        public void saveResult() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    player.undoPageLoader();
                                }
                            });
                        }
                    });
                    break;
                case R.id.btn_redo:
                    PageManager.getInstance().exeRedo(true);
                    RealmPacketPutter.getInstance().allPacketSave(new RealmPacketPutter.SaveResult() {
                        @Override
                        public void saveResult() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    player.undoPageLoader();
                                }
                            });
                        }
                    });
                    break;
            }
        }
    };

    private View.OnClickListener onBottomToolClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            final PageManager manager;

            switch (v.getId()) {
                case R.id.btn_left_menu:
                    if(ProcessStateModel.getInstanse().isRecording() || ProcessStateModel.getInstanse().isPlaying())
                        return;

                    //ThumbNail 뜨기
//                    Bitmap drawingCache = ScreenCapturer.getInstance(drawingPanel).getBitmap();
                    //todo 캐쉬 작업 다시 하자!! !
                    Log.d("DrawingPanel", "LeftMenu_Click");
                    drawingPanel.invalidate();
                    drawingPanel.post(new Runnable() {
                        @Override
                        public void run() {
                            Log.d("DrawingPanel", "Bitmap to File");
                            Bitmap drawingCache = getThumbNail();
                            saveBitmapToFile(drawingCache);
                            drawingCache.recycle();

                            if (!drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                                drawerLayout.openDrawer(Gravity.LEFT);
                                timeLineContainer.setVisibility(View.GONE);
                            }
                            leftMenuFragment.getNoteList();
                        }
                    });
                    break;

                case R.id.btn_record:
                    resetUndoRedo();

                    RealmPacketPutter.getInstance().allPacketSave(new RealmPacketPutter.SaveResult() {
                        @Override
                        public void saveResult() {
                            ProcessStateModel process = ProcessStateModel.getInstanse();

                            if(process.isPlaying())
                                return;

                            if (process.isRecording()) {
                                stopRecord();
                                PageManager.getInstance().setCurrentRunTime(ProcessStateModel.getInstanse().getElapsedTime());
                            } else {
                                startRecord();
                            }
                        }
                    });
                    break;

                case R.id.btn_play_pause:
                    if(AudioPlayer.getInstance(RecordingBoardActivity.this).prevGetDuration() == 0)
                        return;

                    if(ProcessStateModel.getInstanse().isRecording())
                        return;

                    //팝업창 닫기
                    closePopup();

                    RealmPacketPutter.getInstance().allPacketSave(new RealmPacketPutter.SaveResult() {
                        @Override
                        public void saveResult() {
                            if (ProcessStateModel.getInstanse().isPlaying()) {
                                playerStop();
                            } else {
                                playerPlay();
                            }
                        }
                    });
                    break;
                case R.id.txt_page_status :

                    RealmPacketPutter.getInstance().allPacketSave(null);
                    if(ProcessStateModel.getInstanse().isPlaying())
                        return;

                    showPagePicker();

                    break;
                case R.id.btn_add_page: {
                    resetUndoRedo();

                    if (ProcessStateModel.getInstanse().isPlaying())
                        return;

                    manager = PageManager.getInstance();
                    manager.addSumOfPage(1);
                    RealmPacketPutter.getInstance().InsertAddPage(manager.getSumOfPage());
                    manager.changePage(manager.getSumOfPage());
                    changePage(manager.getCurrentPageId());

                    if (ProcessStateModel.getInstanse().isRecording()) {
                        ObjectControllPacket controllPacket = new ObjectControllPacket.ObjectControllPacketBuilder()
                                .setType("changepage")
                                .setAction(999)
                                .setPageNo(manager.getSumOfPage())
                                .build();
                        PacketUtil.makePacket(DrawingPanel.mid.incrementAndGet(), controllPacket);
                    }

                    RealmPacketPutter.getInstance().allPacketSave(null);

                    break;
                }
                case R.id.btn_prev_page: {
                    resetUndoRedo();

                    if (ProcessStateModel.getInstanse().isPlaying())
                        return;

                    manager = PageManager.getInstance();
                    if (manager.getCurrentPage() > 1) {
                        PageManager.getInstance().changePage( PageManager.getInstance().getCurrentPage() - 1);
                        changePage(PageManager.getInstance().getCurrentPageId());

                        if (ProcessStateModel.getInstanse().isRecording()) {
                            ObjectControllPacket controllPacket = new ObjectControllPacket.ObjectControllPacketBuilder()
                                    .setType("changepage")
                                    .setAction(999)
                                    .setPageNo(manager.getCurrentPage())
                                    .build();
                            PacketUtil.makePacket(DrawingPanel.mid.incrementAndGet(), controllPacket);
                        }

                        RealmPacketPutter.getInstance().allPacketSave(null);
                    }
                    break;
                }
                case R.id.btn_next_page:
                    resetUndoRedo();


                    if (ProcessStateModel.getInstanse().isPlaying())
                        return;

                    manager = PageManager.getInstance();
                    if (manager.getCurrentPage() < manager.getSumOfPage()) {
                        PageManager.getInstance().changePage( PageManager.getInstance().getCurrentPage() + 1);
                        changePage(PageManager.getInstance().getCurrentPageId());

                        if (ProcessStateModel.getInstanse().isRecording()) {
                            ObjectControllPacket controllPacket = new ObjectControllPacket.ObjectControllPacketBuilder()
                                    .setType("changepage")
                                    .setAction(999)
                                    .setPageNo(manager.getCurrentPage())
                                    .build();
                            PacketUtil.makePacket(DrawingPanel.mid.incrementAndGet(), controllPacket);
                        }

                        RealmPacketPutter.getInstance().allPacketSave(null);
                    }

                    break;

                case R.id.btn_rewind  :
                    RealmPacketPutter.getInstance().allPacketSave(new RealmPacketPutter.SaveResult() {
                        @Override
                        public void saveResult() {

                            if(ProcessStateModel.getInstanse().isRecording() || ProcessStateModel.getInstanse().isPlaying())
                                return;

                            playerRewind();
                        }
                    });
                    break;

                case R.id.btn_forward :

                    RealmPacketPutter.getInstance().allPacketSave(new RealmPacketPutter.SaveResult() {
                        @Override
                        public void saveResult() {

                            if(ProcessStateModel.getInstanse().isRecording() || ProcessStateModel.getInstanse().isPlaying())
                                return;

                            playerForward();
                        }
                    });
                    break;

                case R.id.btn_setting :

                    if(ProcessStateModel.getInstanse().isRecording() || ProcessStateModel.getInstanse().isPlaying())
                        return;

                    openSettingDialog();
                    break;

                case R.id.btn_export_to_video :

                    if(ProcessStateModel.getInstanse().isRecording() || ProcessStateModel.getInstanse().isPlaying())
                        return;

                    openExportDialog();
                    break;

                case R.id.btn_timeline :

                    if(ProcessStateModel.getInstanse().isRecording() || ProcessStateModel.getInstanse().isPlaying() || drawerLayout.isDrawerVisible(Gravity.LEFT))
                        return;

                    if(timeLineContainer.isShown()){
                        timeLineContainer.setVisibility(View.GONE);
                    }else{
                        tImeLineFragment.refreshData();
                        timeLineContainer.setVisibility(View.VISIBLE);

                    }
                    break;
            }
        }
    };
    private void stopRecord(){

        //todo video pause 처리

        //AudioRecording Stop
        AudioRecorder.getInstance().stopRecording();

        //Audio와 player에 playTIme을 recording된 시간으로 셋팅
//        int duration = getLastPacketRunTime();
        int duration = AudioPlayer.getInstance(this).prevGetDuration();
        AudioPlayer.getInstance(this).setPlayTime(duration);
        player.setPlayTime(duration);

        stopTimer();
        setRecordingState(false);

        if(objectPaper.isThisPageVideoInsert()){
            objectPaper.videoPause(objectPaper.getVideoMid());
            objectPaper.makeVideoPausePacket(objectPaper.getVideoMid());
        }

        NoteManager.getInstance().updateTotalTimeInRealm(controller.getmNote().getNoteName(), duration);

        ProcessStateModel.getInstanse().setIsRecording(false);
        btnRecord.setImageResource(R.drawable.btn_bott_rec);
        ProcessStateModel.getInstanse().endRecording(duration);

    }

    private void startRecord(){
        //녹화 전에 녹화된 duration을 가져와 시간 표시
        int duration = AudioPlayer.getInstance(this).prevGetDuration();
        timeLineContainer.setVisibility(View.GONE);
        closePopup();

        if(player.getPlayTime() < duration){
            //todo show dialog
            Toast.makeText(getApplicationContext(),"You can not edit in the middle.\n The recording point moves to the end", Toast.LENGTH_SHORT).show();
            playerForward();
            return;
        }
        setRecordingState(true);
        inputChangePagePacket();

        AudioRecorder.getInstance().startRecording( !ProcessStateModel.getInstanse().isRemodeling() );
        txtTimer.setText(TimeConverter.convertSecondsToHMmSs(duration));
        startTimer(duration);

        ProcessStateModel.getInstanse().setIsRecording(true);
        btnRecord.setImageResource(R.drawable.btn_bott_rec_s);
        ProcessStateModel.getInstanse().startRecording();
    }

    private void inputChangePagePacket() {
        try{
            Realm realm = Realm.getDefaultInstance();
            PacketObject packet = realm.where(PacketObject.class).lessThan("runtime", PageManager.getInstance().getCurrentRunTime()).findAllSorted("runtime",Sort.DESCENDING).get(0);
            if(PageManager.getInstance().getCurrentPageId() != packet.getPageId()){
                RealmPacketPutter.getInstance().InsertChangePage();
            }
        }catch (ArrayIndexOutOfBoundsException np){
            RealmPacketPutter.getInstance().InsertChangePage();
        }
    }

    private void playerStop() {
        ProcessStateModel.getInstanse().setIsPlaying(false);
        btnPlayPause.setImageResource(R.drawable.btn_bott_player_play_s);
        player.stopPacket();
        stopTimer();

        if (objectPaper.isThisPageVideoInsert()) {
            objectPaper.videoPause(objectPaper.getVideoMid());
        }

        PageManager.getInstance().setCurrentRunTime(AudioPlayer.getInstance(this).getPlayTime());
        setPlayState(false);

        Toolbox.getInstance().setToolType(Toolbox.Tooltype.NONE);
    }

    private void playerPlay() {
        //한프레임 정도 오차를 생각해서 40ms를 더 해줌
        if (AudioPlayer.getInstance(this).getPlayTime() + 40 >= AudioPlayer.getInstance(this).prevGetDuration()) {
            player.setPlayTime(0);
            AudioPlayer.getInstance(this).setPlayTime(0);
            PageManager.getInstance().setCurrentRunTime(0);

            clearAllCanvas();
        }

        ProcessStateModel.getInstanse().setIsPlaying(true);
        btnPlayPause.setImageResource(R.drawable.btn_bott_player_pause_s);
        player.playPacket();
        startTimer((int) player.getPlayTime());

        if (objectPaper.isThisPageVideoInsert()) {
            if (isCurrentVideoStatePlay()){
                objectPaper.videoStart(objectPaper.getVideoMid());
            }
        }

        //Timeline 닫기
        timeLineContainer.setVisibility(View.GONE);

        setPlayState(true);
    }

    private void playerRewind(){
        player.setPlayTime(0);
        txtTimer.setText(TimeConverter.convertSecondsToHMmSs(0));
        PageManager.getInstance().changePage(getRuntimeZeroPage(), 0);
        PageManager.getInstance().setCurrentRunTime(0);
    }

    private void playerForward(){
        int duration = AudioPlayer.getInstance(this).prevGetDuration();
        player.setPlayTime(duration);
        txtTimer.setText(TimeConverter.convertSecondsToHMmSs(duration));
        PageManager.getInstance().changePage(getLastPacketPage(), duration);
        PageManager.getInstance().setCurrentRunTime(duration);
    }

    private void setPlayState(boolean isPlaying){
        btnAddPage.setAlpha(isPlaying ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        btnSetting.setAlpha(isPlaying ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        btnExportToVideo.setAlpha(isPlaying ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        btnPrevPage.setAlpha(isPlaying ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        btnTimeline.setAlpha(isPlaying ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        btnNextPage.setAlpha(isPlaying ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        btnLeftMenu.setAlpha(isPlaying ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        btnRewind.setAlpha(isPlaying ? ALPHA.INACTIVE : E.ALPHA.ACTIVE);
        btnForward.setAlpha(isPlaying ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        btnRecord.setAlpha(isPlaying ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        rightToolWrapper.setVisibility(isPlaying ? View.GONE : View.VISIBLE);
    }

    private void setRecordingState(boolean isRecording){
        btnLeftMenu.setAlpha(isRecording ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        btnRewind.setAlpha(isRecording ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        btnForward.setAlpha(isRecording ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        btnPlayPause.setAlpha(isRecording ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        btnTimeline.setAlpha(isRecording ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        btnExportToVideo.setAlpha(isRecording ? ALPHA.INACTIVE : ALPHA.ACTIVE);
        btnSetting.setAlpha(isRecording ? ALPHA.INACTIVE : ALPHA.ACTIVE);

    }


    private void startTimer(int time){
        recordTimer = new Timer();
        recordTimerTask = new recordPlayTImerTask(time);
        recordTimer.schedule(recordTimerTask,0,100);
    }
    private void stopTimer(){
        try{
            recordTimer.purge();
            recordTimer.cancel();
            recordTimer = null;
        }catch (NullPointerException ne){
            ne.printStackTrace();
        }

        try{
            recordTimerTask.cancel();
            recordTimerTask = null;
        }catch (NullPointerException ne){
            ne.printStackTrace();
        }

    }

    private void releaseOtherButtons() {

        if(ObjectPaperV2.currentFocusedTextMid != -1)
            clearAllTextFocus();

        btnPalm.setChecked(false);
        btnFinger.setChecked(false);
        btnPointer.setChecked(false);
        btnPen.setChecked(false);
        btnText.setChecked(false);
        btnVideo.setChecked(false);
        btnEraser.setChecked(false);
        btnImage.setChecked(false);
        btnShape.setChecked(false);
        btnPdf.setChecked(false);
        btnRemove.setChecked(false);

        switch (Toolbox.getInstance().getToolType()) {
            case PALM:
                btnPalm.setChecked(true);
                break;

            case FINGER:
                btnFinger.setChecked(true);
                break;

            case POINTER:
                btnPointer.setChecked(true);
                break;

            case PEN:
                btnPen.setChecked(true);
                break;

            case ERASER:
                btnEraser.setChecked(true);
                break;

            case TEXT:
                btnText.setChecked(true);
                break;

            case VIDEO:
                btnVideo.setChecked(true);
                break;

            case IMAGE:
                btnImage.setChecked(true);
                break;

            case SHAPE:
                btnShape.setChecked(true);
                break;

            case PDF:
                btnPdf.setChecked(true);
                break;

            case REMOVE:
                btnRemove.setChecked(true);
                break;
        }
    }

    private View showPopup(View button, int popupToolBodyLayout) {
        closePopup();
        View popupTool = View.inflate(getApplicationContext(), R.layout.popup_tool, null);
        popupTool.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mainPanel.addView(popupTool, FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);

        int[] location = new int[2];
        button.getLocationOnScreen(location);

        Rect rectgle = new Rect();
        Window window = getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(rectgle);
        int StatusBarHeight = rectgle.top;

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) popupTool.getLayoutParams();
        params.gravity = Gravity.RIGHT;
        params.rightMargin = (int) PixelUtil.getInstance().convertDpToPixel(65);
        params.topMargin = location[1] - StatusBarHeight;

        popupTool.setLayoutParams(params);


        LinearLayout popupToolBody = (LinearLayout) popupTool.findViewById(R.id.popup_body);
        View toolView = View.inflate(getApplicationContext(), popupToolBodyLayout, popupToolBody);
        popupToolBody.forceLayout();

        setPopup(popupTool, button.getId());

        ProcessStateModel.getInstanse().setToolPopup(true);

        return popupTool;
    }

    private void closePopup() {
        Log.i(DrawingPanel.class.getCanonicalName(), "closePopup is called");
        if (mPopup != null) {
            mainPanel.removeView(mPopup);
            ProcessStateModel.getInstanse().setToolPopup(false);
        }
    }

    private void setPopup(View popup, int id) {
        StrokePreview preview;
        SeekBar sbWidth;
        SeekBar sbAlpha;
        SeekBar sbEraser;
        int[] ids;
        RadioButton button;
        RadioGroup group;

        switch (id) {
            case R.id.btn_pointer:
                button = (RadioButton) popup.findViewById(R.id.btnCircle);
                button.setOnClickListener(Toolbox.getInstance().pointerTypeListener);

                button = (RadioButton) popup.findViewById(R.id.btnArrow);
                button.setOnClickListener(Toolbox.getInstance().pointerTypeListener);

                button = (RadioButton) popup.findViewById(R.id.btnHand);
                button.setOnClickListener(Toolbox.getInstance().pointerTypeListener);

                //현재 사용하는 포이터 모양을 선택된 상태로 유지
                int pointerType = Toolbox.getInstance().getCurrentPointer();
                group = (RadioGroup) popup.findViewById(R.id.groupButtons);
                group.check(Toolbox.getInstance().getLaserPointerType().getButtonId(pointerType));

                ids = new int[]{
                        R.id.btnColor1,
                        R.id.btnColor2,
                        R.id.btnColor3,
                        R.id.btnColor4,
                        R.id.btnColor5,
                        R.id.btnColor6
                };

                for (int i = 0; i < ids.length; ++i) {
                    button = (RadioButton) popup.findViewById(ids[i]);
                    button.setOnClickListener(Toolbox.getInstance().colorChanged);

                    String tag = (String) button.getTag();
                    int color = Color.parseColor(tag);
                    if (Toolbox.getInstance().getCurrentPointerColor() == color) {
                        button.setChecked(true);
                        Toolbox.getInstance().setPrevPointerColorButton(button);
                    }
                }
                break;

            case R.id.btn_pen:
                preview = (StrokePreview) popup.findViewById(R.id.vStrokePreview);

                Toolbox.getInstance().setStrokePreview(preview);
                preview.setStrokeWidth(Toolbox.getInstance().currentStrokeWidth);
                preview.setStrokeColor(Toolbox.getInstance().currentStrokeColor);
                preview.setStrokeOpacity(Toolbox.getInstance().currentStrokeOpacity);

                sbWidth = (SeekBar) popup.findViewById(R.id.sbStrokeWidth);
                sbWidth.setMax(Toolbox.STROKE_MAX);
                sbWidth.setProgress(Toolbox.getInstance().currentStrokeWidth - Toolbox.STROKE_BASE);
                sbWidth.setOnSeekBarChangeListener(Toolbox.getInstance().strokeWidthChanged);

                sbAlpha = (SeekBar) popup.findViewById(R.id.sbOpacity);
                sbAlpha.setMax(Toolbox.ALPHA_MAX);
                sbAlpha.setProgress(Toolbox.getInstance().currentStrokeOpacity - Toolbox.ALPHA_BASE);
                sbAlpha.setOnSeekBarChangeListener(Toolbox.getInstance().opacityChanged);

                ids = new int[]{
                        R.id.btnColor1,
                        R.id.btnColor2,
                        R.id.btnColor3,
                        R.id.btnColor4,
                        R.id.btnColor5,
                        R.id.btnColor6,
                        R.id.btnColor7,
                        R.id.btnColor8
                };

                for (int i = 0; i < ids.length; ++i) {
                    button = (RadioButton) popup.findViewById(ids[i]);
                    button.setOnClickListener(Toolbox.getInstance().colorChanged);

                    String tag = (String) button.getTag();
                    int color = Color.parseColor(tag);
                    if (Toolbox.getInstance().currentStrokeColor == color) {
                        button.setChecked(true);
                        Toolbox.getInstance().prevPenColorButton = button;
                    }
                }

                break;

            case R.id.btn_shape:

                int[] buttons = new int[]{
                        R.id.btnCircle,
                        R.id.btnTriangle,
                        R.id.btnRectangle,
                        R.id.btnStraight,
                };
                for (int i = 0; i < buttons.length; ++i) {
                    button = (RadioButton) popup.findViewById(buttons[i]);
                    button.setOnClickListener(Toolbox.getInstance().shapeChanged);
                }

                // 현재 사용되고 있는 도형 모양을 선택된 상태로
                group = (RadioGroup) popup.findViewById(R.id.groupButtons);
                group.check(buttons[Toolbox.getInstance().currentShape.ordinal()]);

                ids = new int[]{
                        R.id.btnColor1,
                        R.id.btnColor2,
                        R.id.btnColor3,
                        R.id.btnColor4,
                        R.id.btnColor5,
                        R.id.btnColor6,
                        R.id.btnColor7,
                        R.id.btnColor8
                };

                for (int i = 0; i < ids.length; ++i) {
                    button = (RadioButton) popup.findViewById(ids[i]);
                    button.setOnClickListener(Toolbox.getInstance().colorChanged);

                    // 현재 사용하는 색 버튼을 선택된 상태로
                    String tag = (String) button.getTag();
                    int color = Color.parseColor(tag);
                    if (Toolbox.getInstance().currentShapeColor == color) {
                        button.setChecked(true);
                        Toolbox.getInstance().prevShapeColorButton = button;
                    }
                }
                break;

            case R.id.btn_eraser:
                preview = (StrokePreview) popup.findViewById(R.id.vStrokePreview);
                Toolbox.getInstance().setStrokePreview(preview);

                preview.setStrokeWidth(Toolbox.getInstance().currentEraserWidth / 2);
                ImageView btnPenClear = (ImageView)popup.findViewById(R.id.btn_pen_clear);
                btnPenClear.setVisibility(View.GONE);
                btnPenClear.setOnClickListener(new ImageView.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(RecordingBoardActivity.this);
                        builder.setMessage(R.string.alldelete_caution);
                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(ProcessStateModel.getInstanse().isRecording()){
                                    ObjectControllPacket controllPacket = new ObjectControllPacket.ObjectControllPacketBuilder()
                                                                                  .setAction(999)
                                                                                  .setType(PacketUtil.S_ALLDELETE)
                                                                                  .build();
                                    PacketUtil.makePacket(DrawingPanel.mid.incrementAndGet(), controllPacket);
                                }
                                drawingPaper.clearCanvas();
                                drawingPaper.invalidate();
                            }
                        });
                        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });

                ImageView btnAllDelete = (ImageView)popup.findViewById(R.id.btn_all_clear);
                btnAllDelete.setOnClickListener(new ImageView.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        AlertDialog.Builder builder = new AlertDialog.Builder(RecordingBoardActivity.this);
                        builder.setMessage(R.string.alldelete_pen_caution);
                        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if(ProcessStateModel.getInstanse().isRecording()){
                                    ObjectControllPacket controllPacket = new ObjectControllPacket.ObjectControllPacketBuilder()
                                                                                  .setAction(999)
                                                                                  .setType(PacketUtil.S_ALLDELETE)
                                                                                  .build();
                                    PacketUtil.makePacket(DrawingPanel.mid.incrementAndGet(), controllPacket);
                                }
                                clearAllCanvas();
                            }
                        });
                        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();
                    }
                });
                sbEraser = (SeekBar) popup.findViewById(R.id.sbEraserWidth);
                sbEraser.setProgress((Toolbox.getInstance().currentEraserWidth / 2) - Toolbox.STROKE_BASE);
                sbEraser.setOnSeekBarChangeListener(Toolbox.getInstance().eraserWidthChanged);
                break;
        }

    }


    private void BringToFront(int viewType) {
        if (viewType == DRAWING_PAPER) {
            drawingPanel.bringChildToFront(drawingPaper);
        } else if (viewType == OBJECT_PAPER) {
            drawingPanel.bringChildToFront(objectPaper);
        } else {
            drawingPanel.bringChildToFront(objectPaper);
            drawingPanel.bringChildToFront(drawingPaper);
        }

    }

    private void beginCrop(Uri source) {
        Uri destination = Uri.fromFile(new File(getCacheDir(), "cropped"));
        Crop.of(source, destination).withAspect(4, 3).start(this);
    }

    private void handleCrop(int resultCode, Intent result) throws IOException {
        if (resultCode == RESULT_OK) {
            new FileCopyTask().execute(new CUri(UCrop.getOutput(result), READ_IMAGE_CODE));
        } else if (resultCode == Crop.RESULT_ERROR) {
            Toast.makeText(this, Crop.getError(result).getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private String copyFileFromUri(Uri uri, String basePath, String filename) throws IOException {
        File basePathFile = new File(basePath);
        if (!basePathFile.exists())
            basePathFile.mkdirs();
        File outputFile;
            outputFile = new File(basePath, filename);

        InputStream inputStream = getContentResolver().openInputStream(uri);
        OutputStream outputStream = new FileOutputStream(outputFile);

        byte[] buf = new byte[1024];
        int len;
        while ((len = inputStream.read(buf)) > 0) {
            outputStream.write(buf, 0, len);
        }
        inputStream.close();
        outputStream.close();

        return outputFile.getAbsolutePath();
    }


    private void deleteObject(long mid) {
        objectPaper.deleteObject(mid);
    }

    private void clearAllTextFocus() {
        objectPaper.clearAllTextFocus();
    }


    private void setCurrentPageState() {
        txtPageStatus.setText(PageManager.getInstance().getCurrentPage() + "/" + PageManager.getInstance().getSumOfPage());
    }

    private void clearAllCanvas() {
        drawingPaper.clearCanvas();
        objectPaper.clearCanvas();
        drawingPaper.invalidate();
        objectPaper.invalidate();
    }

    private void loadPaper(long pageid) {
        int currentPage = PageManager.getInstance().getPageNumFromRealm(pageid);
        if(currentPage == 0){
            return;
        }
        PageManager.getInstance().setCurrentPage(currentPage);
        player.pageLoader(pageid);
    }

    private void loadPaper(float timeStamp) {
        long pageid = PageManager.getInstance().getTimeStampPageFromRealm(timeStamp);
        loadPaper(pageid, timeStamp);
    }

    private void loadPaper(long pageid, float timeStamp) {

        ProcessStateModel.getInstanse().setLastRecordingTime((long)timeStamp);
        PageManager.getInstance().setCurrentPage(PageManager.getInstance().getPageNumFromRealm(pageid));
        player.pageLoader(pageid, timeStamp);
    }

    public boolean isCurrentVideoStatePlay() {
        boolean isPlayState;
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

    @Override
    public void onTimeLineChanged(float pottition) {
        //타임라인 프래그먼트의 수평리스트뷰 스크롤 변경되었을때 상위의 액티비티에서 관리하기 위함.

    }

    private class FileCopyTask extends AsyncTask<CUri, Void, CFilePath> {

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected CFilePath doInBackground(CUri... params) {
            int fileType = params[0].getFileType();
            Uri uri = params[0].getUri();
            String filePath = null;
            String filename = null;
            String basePath = null;
            try {
                switch (fileType){
                    case READ_IMAGE_CODE :
                        basePath = FilePath.IMAGES_DIRECTORY;
                        filename = DrawingPanel.mid.incrementAndGet()+".png";
                        break;
                    case READ_VIDEO_FILE :
                        basePath = FilePath.IMAGES_DIRECTORY;
                        filename = DrawingPanel.mid.incrementAndGet()+".mp4";
                        break;
                    case READ_PDF_CODE :
                        basePath = FilePath.FILES_DIRECTORY;
                        filename = DrawingPanel.mid.incrementAndGet()+".pdf";
                        break;
                }

                filePath = copyFileFromUri(uri, basePath, filename);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return new CFilePath(filePath, fileType, filename);
        }

        @Override
        protected void onPostExecute(CFilePath result) {
            final String filePath = result.getFilePath();
            final String fileName = result.getFileName();
            switch (result.getFileType()) {
                case READ_IMAGE_CODE:
                    try {
                        ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(new File(filePath), ParcelFileDescriptor.MODE_READ_ONLY);
                        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
                        Bitmap bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor);
                        parcelFileDescriptor.close();

                        if (bitmap != null) {
                            float beginX = drawingPanel.getWidth() / 2 - bitmap.getWidth() / 2;
                            float beginY = drawingPanel.getHeight() / 2 - bitmap.getHeight() / 2;
                            float endX = beginX + bitmap.getWidth();
                            float endY = beginY + bitmap.getHeight();
                            objectPaper.onImageDown(DrawingPanel.mid.get(), beginX, beginY, endX, endY, filePath, true);
                            PageManager.getInstance().currentPageInaddDrawingPacket(DrawingPanel.mid.get());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;

                case READ_VIDEO_FILE:
                    objectPaper.onVideoDown(DrawingPanel.mid.get(), FilePath.IMAGES_DIRECTORY + fileName, true);
                    PageManager.getInstance().currentPageInaddDrawingPacket(DrawingPanel.mid.get());
                    break;

                case READ_PDF_CODE :
                    objectPaper.spreadPdfFile(FilePath.FILES_DIRECTORY + fileName, fileName);
                    break;
            }
        }
    }

    private class CUri {
        private Uri uri;
        private int fileType;

        public CUri(Uri uri, int fileType) {
            this.uri = uri;
            this.fileType = fileType;
        }

        public Uri getUri() {
            return uri;
        }

        public void setUri(Uri uri) {
            this.uri = uri;
        }

        public int getFileType() {
            return fileType;
        }

        public void setFileType(int fileType) {
            this.fileType = fileType;
        }
    }

    private class CFilePath {
        private String filePath;
        private int fileType;
        private String fileName;

        public CFilePath(String filePath, int fileType, String fileName) {
            this.filePath = filePath;
            this.fileType = fileType;
            this.fileName = fileName;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public int getFileType() {
            return fileType;
        }

        public void setFileType(int fileType) {
            this.fileType = fileType;
        }

        public String getFileName() { return fileName ; }

    }

    //ServiceGuide Dialog Open
    private void openServiceGuide() {

        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }

        ServiceGuideDialog serviceGuideDialog = new ServiceGuideDialog();
        serviceGuideDialog.show(getSupportFragmentManager(), "guide");
    }
    // Information Dialog Open
    private void openInformation() {

        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }

        InformationDialog informationDialog = new InformationDialog();
        informationDialog.show(getSupportFragmentManager(), "information");
    }
    // Help Dialog Open
    private void openHelp() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT);
        }

        HelpDialog helpDialog = new HelpDialog();
        helpDialog.show(getSupportFragmentManager(), "help");
    }

    // Export Dialog Open
    private void openExportDialog(){
        ExportDialog exportDialog = new ExportDialog();
        exportDialog.show(getSupportFragmentManager(), "export");
    }

    private void openSettingDialog() {

        settingDialog.show(getSupportFragmentManager(), "setting");
    }

    private void openOpenCourseDialog(){
        OpenCourseDialog openCourseDialog = new OpenCourseDialog();
        openCourseDialog.setCancelable(false);
        openCourseDialog.show(getSupportFragmentManager(), "opencourse");
    }

    /**
     * 레코딩 및 플레이 시간을 표시하는 TImerTask
     */
    class recordPlayTImerTask extends TimerTask{
        private long initTime;
        private long count;
        public recordPlayTImerTask(long initTime) {
            this.initTime = initTime;
            this.count = 0;
        }

        @Override
        public void run() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    txtTimer.setText(TimeConverter.convertSecondsToHMmSs(initTime + count));
                    count+=100;
                }
            });
        }
    }


    private class EventHandler extends Handler{

        @Override
        public void handleMessage(final Message msg) {
            super.handleMessage(msg);

            switch(msg.what){
                case EventBus.EVENT_TYPE :
                    switch (((EventType)msg.obj).getEvent()) {
                        case EventType.CLOSE_POPUP:
                            closePopup();
                            break;
                        case EventType.CLEAR_TEXT_FOCUS:
                            objectPaper.currentFocusedTextMid = 0;
                            clearAllTextFocus();
                            break;
                        case EventType.PLAYER_END:
                            playerStop();
                            break;
                        case EventType.OPEN_SERVICE_GUIDE:
                            openServiceGuide();
                            break;
                        case EventType.OPEN_INFOMATION :
                            openInformation();
                            break;
                        case EventType.OPEN_HELP:
                            openHelp();
                            break;
                        case EventType.CHANGE_TOOLTYPE :
                            releaseOtherButtons();
                            break;
                        case EventType.ALL_SAVE_PACKET:
                            RealmPacketPutter.getInstance().allPacketSave(null);
                            break;
                        case EventType.REFASH_PAGE_STATE :
                            setCurrentPageState();
                            break;
                        case EventType.NEW_NOTE :
                            resetUndoRedo();
                            RealmPacketPutter.getInstance().allPacketSave(new RealmPacketPutter.SaveResult() {
                                @Override
                                public void saveResult() {
                                    newNoteInitialize();
                                }
                            });
                            break;
                        case EventType.SET_NOTE :
                            resetUndoRedo();
                            RealmPacketPutter.getInstance().allPacketSave(new RealmPacketPutter.SaveResult() {
                                @Override
                                public void saveResult() {
                                    setNoteInitialize(((EventType)msg.obj).getArgument());
                                }
                            });
                            break;
                        case EventType.DEL_NOTE :
                            showDeleteNotePopup(((EventType)msg.obj).getArgument());
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

                    //쓰지 않는 logic 정리
                    if (changePage.getPageid() != -1 && changePage.getTimeStamp() != -1) {
                        loadPaper(changePage.getPageid(), changePage.getTimeStamp());
                    } else if (changePage.getPageid() != -1 && changePage.getTimeStamp() == -1) {
                        loadPaper(changePage.getPageid());
                    } else if (changePage.getPageid() == -1 && changePage.getTimeStamp() != -1) {
                        loadPaper(changePage.getTimeStamp());
                    }

                    //loginc 자세히 정리
                    AudioPlayer.getInstance(RecordingBoardActivity.this).setPlayTime((int)PageManager.getInstance().getCurrentRunTime());
                    player.setPlayTime(PageManager.getInstance().getCurrentRunTime());
                    txtTimer.setText(TimeConverter.convertSecondsToHMmSs((long)PageManager.getInstance().getCurrentRunTime()));

                    setCurrentPageState();
                    break;

                case EventBus.OBJECT_DELETE :
                    deleteObject(((ObjectDeleteEvent)msg.obj).getMid());
                    break;

                case EventBus.EXPORT_SELECTED:
                    showExportDialog(((ExportSelectedDialogEvent)msg.obj).getType());
                    break;

                case EventBus.OPEN_COURSE_EXPORT:
                    EventOpenCourseExport export = (EventOpenCourseExport) msg.obj;
                    exportToOpencourse(export.getTitle(), export.getCategoryId());
                    break;
            }
        }
    }


    private void showExportDialog(int type) {
        //todo 오픈코스, 갤러리, 유튜브 export dialog 각자 띄우기
        switch (type){
            case ExportSelectedDialogEvent.EXPORT_OPENCOURSE :
                if(chkLogin()) {
                    openOpenCourseDialog();
                }else{
                    showLoginDialog();
                }
                break;
            case ExportSelectedDialogEvent.EXPORT_GALLARY :
                break;
            case ExportSelectedDialogEvent.EXPORT_YOUTUBE :
                break;
        }
    }

    private void showLoginDialog() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View layout = inflater.inflate(R.layout.rb_dialog_sign_in, (ViewGroup) findViewById(R.id.layout_root));

        TextView loginWithFacebook = (TextView) layout.findViewById(R.id.login_with_facebook);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(layout);

        loginDialog = builder.create();
        loginWithFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoginManager.getInstance().logInWithReadPermissions(RecordingBoardActivity.this, Arrays.asList("public_profile", "email"));
            }
        });

        loginDialog.show();
        loginDialog.getWindow().setLayout((int) PixelUtil.getInstance().convertDpToPixel(360), WindowManager.LayoutParams.WRAP_CONTENT);
    }

    private boolean chkLogin() {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if(accessToken == null)
            return false;
        else
            return true;
    }

    //Todo 리펙토 필요
    private void exportToOpencourse(String title, int categoryId) {

        //todo construct Video information
        UploadVideo uploadVideo = new UploadVideo();
        uploadVideo.setTitle(title);
        uploadVideo.setDescription("");
        uploadVideo.setCategoryId(categoryId);
        uploadVideo.setPlaytime(AudioPlayer.getInstance(this).prevGetDuration()/1000);
        uploadVideo.setUserId(Profile.getCurrentProfile().getId());
        uploadVideo.setPlatform("Android");
        uploadVideo.setVisible(1);
        uploadVideo.setSnsType("fb");
        uploadVideo.setLang(KnowRecorderApplication.getLanguage());
        uploadVideo.setCountry(KnowRecorderApplication.getCountry());

        //todo dialog show

        final ProgressDialog progressDialog = new ProgressDialog(RecordingBoardActivity.this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.setMax(4);
        progressDialog.setMessage(getString(R.string.now_in_compress));
        progressDialog.setTitle(getString(R.string.opencourse));
        progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        progressDialog.setProgressDrawable(getResources().getDrawable(R.drawable.progress_drawable));
        progressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        progressDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialog) {
                applyProgressDialogTheme(progressDialog);
            }
        });
        progressDialog.show();
        //todo opencourse upload task;

        String folderName = controller.getmNote().getNoteName();

        Bitmap snapshot = BitmapFactory.decodeFile(FilePath.NOTE_FOLDER + folderName + ".png");
        if (snapshot == null) {
            Bitmap drawingCache = getThumbNail();
            saveBitmapToFile(drawingCache);
            drawingCache.recycle();

            snapshot = BitmapFactory.decodeFile(FilePath.NOTE_FOLDER + folderName + ".png");
        }

        new OCExportTask(RecordingBoardActivity.this,
                uploadVideo, folderName,
                FilePath.TEMP_ZIP_DIRECTORY + folderName + ".zip",
                snapshot,
                progressDialog).execute();
    }

    private void applyProgressDialogTheme(ProgressDialog dialog) {
        Button negativeButton = dialog.getButton(ProgressDialog.BUTTON_NEGATIVE);

        negativeButton.setTextColor(Color.parseColor("#a0c81e"));
        negativeButton.setTypeface(null, Typeface.BOLD);
        dialog.getWindow().setBackgroundDrawableResource(R.drawable.dialog_bg);

        negativeButton.invalidate();
    }

    public void changePage(long pageid){
        if(!ProcessStateModel.getInstanse().isRecording())
            ProcessStateModel.getInstanse().setLastRecordingTime(getPageLastrunTime(pageid));

        setCurrentPageState();
        clearAllCanvas();

        loadPaper(pageid);
    }

    public long getPageLastrunTime(long pageid){
        Realm realm = Realm.getDefaultInstance();
        long runtime;
        try {
            runtime = realm.where(PacketObject.class).equalTo("pageid", pageid).max("runtime").longValue();
        }catch (NullPointerException ne){
            ne.printStackTrace();
            if(realm.where(PacketObject.class).findAll().size() > 0)
                runtime = realm.where(PacketObject.class).max("runtime").longValue();
            else
                runtime = 0;
        }
        return runtime;
    }

    //status bar height 구하기;
    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    private Bitmap getThumbNail(){

        drawingPanel.setDrawingCacheEnabled(true);
        drawingPanel.buildDrawingCache(true);
        Bitmap drawingCache = drawingPanel.getDrawingCache(true).copy(Bitmap.Config.ARGB_8888, false);
        drawingPanel.setDrawingCacheEnabled(false);
        drawingPanel.destroyDrawingCache();

        return drawingCache;
    }

    //File로 현재 Thumbnail 저장
    private void saveBitmapToFile(Bitmap bitmap) {
        FileOutputStream out = null;
        bitmap = Bitmap.createScaledBitmap(bitmap, 1024, 768, true);
        try {
            out = new FileOutputStream(new File(FilePath.NOTE_FOLDER + controller.getmNote().getNoteName() + ".png"));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e) {

        }
    }

    private void newNoteInitialize(){
        drawerLayout.closeDrawer(Gravity.LEFT);
        leftMenuFragment.closeList();

        //새 노트 생성
        controller.createNote();
        Toolbox.getInstance().initToolbox();
        //노트 초기화
        setNoteInit();

        mainPanel.removeView(drawingPanel);
        addDrawingPanel();
        setPrimaryKey();

        drawingPanel.post(new Runnable() {
            @Override
            public void run() {
                //화면 초기화
                playerForward();
            }
        });
        //Time LIne 초기화
        setNewTImeLine();

    }

    private void setNoteInitialize(String noteName){

        if(TextUtils.equals(SharedPreferencesManager.getInstance(this).getLastNote(), noteName)){
            playerForward();
            return;
        }

        drawerLayout.closeDrawer(Gravity.LEFT);
        leftMenuFragment.closeList();

        controller.setNote(noteName);
        Toolbox.getInstance().initToolbox();

        setNoteInit();

        mainPanel.removeView(drawingPanel);
        addDrawingPanel();
        setPrimaryKey();

        drawingPanel.post(new Runnable() {
            @Override
            public void run() {
                playerForward();
            }
        });

        //Time LIne 초기화
        setNewTImeLine();
    }
    private void setNewTImeLine(){
        if (tImeLineFragment != null)
            getSupportFragmentManager().beginTransaction().remove(tImeLineFragment).commitAllowingStateLoss();

        tImeLineFragment = new TImeLineFragment();
        getSupportFragmentManager().beginTransaction().replace(R.id.timelist_container, tImeLineFragment, "timeLine").commitAllowingStateLoss();
    }

    private void showDeleteNotePopup(final String noteName) {

        //todo 적어도 하나의 노트 메세지와 현재 노트의 메세지가 같게 되어있다. 수정 필요
        if(FilePath.getNoteCount() == 1)
        {
            Toast.makeText(this, "At least one note is required here.", Toast.LENGTH_SHORT).show();
            return;
        }

        if(TextUtils.equals(controller.getmNote().getNoteName(), noteName))
        {
            Toast.makeText(this, getResources().getString(R.string.unavailable_to_delete_note), Toast.LENGTH_SHORT).show();
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.note_delete_confirm_msg);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                noteDelete(noteName);
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void noteDelete(String noteName){
        FilePath.deleteNote(noteName);
        leftMenuFragment.getNoteList();
    }
    //Fragment로 커스텀한 UI처리
    private void showPagePicker() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(RecordingBoardActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_page_picker, null);
        final NumberPicker picker = (NumberPicker) dialogView.findViewById(R.id.picker);

        try {
            //todo PaperManagerV2수정
            int maxPage = PageManager.getInstance().getSumOfPage();
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    int pickedPage = picker.getValue();
                    if(PageManager.getInstance().isExistPage(pickedPage)) {

                        PageManager.getInstance().changePage( pickedPage);
                        changePage(PageManager.getInstance().getCurrentPageId());

                        if (ProcessStateModel.getInstanse().isRecording()) {
                            ObjectControllPacket controllPacket = new ObjectControllPacket.ObjectControllPacketBuilder()
                                    .setType(PacketUtil.S_CHANGEPAGE)
                                    .setAction(999)
                                    .setPageNo(pickedPage)
                                    .build();
                            PacketUtil.makePacket(DrawingPanel.mid.incrementAndGet(), controllPacket);
                        }

                        RealmPacketPutter.getInstance().allPacketSave(null);
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, null);
            picker.setMaxValue(maxPage);
            picker.setMinValue(1);
            picker.setValue(PageManager.getInstance().getCurrentPage());
            picker.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
            builder.setView(dialogView);

            final AlertDialog dialog = builder.create();
            dialog.show();
        } catch (NullPointerException e) {

        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return super.onTouchEvent(event);
    }
}