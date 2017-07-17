package com.knowlounge.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.knowlounge.KnowloungeApplication;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.customview.DragSelectableView;
import com.knowlounge.manager.WenotePreferenceManager;

import org.apache.cordova.ConfigXmlParser;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaInterfaceImpl;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaPreferences;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.CordovaWebViewImpl;
import org.apache.cordova.PluginEntry;
import org.apache.cordova.engine.SystemWebView;
import org.apache.cordova.engine.SystemWebViewClient;
import org.apache.cordova.engine.SystemWebViewEngine;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by Minsu on 2015-12-11.
 */
public class WebViewFragment extends Fragment implements CordovaInterface {

    private static String TAG = "WebViewFragment";

    // For CordovaInterface implements..
    protected CordovaPlugin activityResultCallback = null;
    protected boolean activityResultKeepRunning;
    protected boolean keepRunning = true;
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    // For read config.xml file..
    protected CordovaPreferences preferences;
    protected String launchUrl;
    protected ArrayList<PluginEntry> pluginEntries;
    protected CordovaInterfaceImpl cordovaInterface;

    protected ScaleGestureDetector scaleGestureDetector;

    private CordovaWebView mainWebView;

    private float scale = 1f;

    private String roomUrl;
    private float density;

    private WenotePreferenceManager prefManager;

    // Read config.xml file contents..
    protected void loadConfig() {
        ConfigXmlParser parser = new ConfigXmlParser();
        parser.parse(getActivity());
        preferences = parser.getPreferences();
        preferences.setPreferencesBundle(getActivity().getIntent().getExtras());
        launchUrl = parser.getLaunchUrl();
        pluginEntries = parser.getPluginEntries();
    }


    private boolean drawSoundEnable = false;

    // 판서폴 선택 영역 캡쳐하기 기능 대응 - 2016.12.12
    private DragSelectableView selectableDeemedView;
    private View dragSelector;

    private float initX = 0.f;
    private float initY = 0.f;

    int width = 0;
    int height = 0;

    boolean isNtEnable = true;    // 네이티브 터치 이벤트 활성화 여부

    private enum DragDirection {
        UP_RIGHT_TO_DOWN_LEFT, UP_LEFT_TO_DOWN_RIGHT, DOWN_LEFT_TO_UP_RIGHT, DOWN_RIGHT_TO_UP_LEFT
    };

    private DragDirection direction;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){

        prefManager = WenotePreferenceManager.getInstance(getActivity().getApplicationContext());

        if(drawSoundEnable) {
            initSound();
        }

        LayoutInflater localInflater = inflater.cloneInContext(new CordovaContext(getActivity(), this));
        View rootView = localInflater.inflate(R.layout.fragment_webview, container, false);

        dragSelector = rootView.findViewById(R.id.drag_selector);
//        selectableDeemedView = (DragSelectableView) rootView.findViewById(R.id.draggable_draw_container);
//
//        if(((RoomActivity)getActivity()).getIsSelectorMode()) {
//            selectableDeemedView.setVisibility(View.VISIBLE);
//        } else {
//            selectableDeemedView.setVisibility(View.GONE);
//        }
        //mainWebView = (CordovaWebView) rootView.findViewById(R.id.mainWebView);  // xml을 이용하여 웹뷰를 생성함..

        //Config.init(getActivity());
        density = getResources().getDisplayMetrics().density;

        Log.d(TAG, "onCreateView / density : " + density);

        Bundle extra = getArguments();
        String roomUrl = extra.getString("roomurl");
        String url = "file:///android_asset/www/" + roomUrl;
        Log.d(TAG, "roomUrl = " + roomUrl);
        Log.d(TAG, "url = " + url);


        // xml없이 웹뷰를 생성함..
        cordovaInterface = new CordovaInterfaceImpl(getActivity());
        loadConfig();

        mainWebView = new CordovaWebViewImpl(CordovaWebViewImpl.createEngine(getActivity(), preferences));

        // 웹뷰 내에서 새로운 URL이 로딩될 때의 액션을 정의하는 이벤트 리스너
        ((WebView)mainWebView.getView()).setWebViewClient(new SystemWebViewClient((SystemWebViewEngine)mainWebView.getEngine()){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                Log.d(TAG, "shouldOverrideUrlLoading / url : " + url);
                Uri uri = Uri.parse(url);

                if (uri.getScheme().equals("http") || uri.getScheme().equals("https")){
//                    Intent intent = new Intent(Intent.ACTION_VIEW);
//                    intent.setData(uri);
//                    startActivity(intent);
//                    return true;
                    return true;
                } else {
                    return super.shouldOverrideUrlLoading(view, url);
                }
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                Log.d(TAG, "onPageFinished");
                super.onPageFinished(view, url);
            }
        });

        mainWebView.getView().setId(100);

        //((WebView) mainWebView.getView()).getSettings().setLoadWithOverviewMode(true);
        //((WebView) mainWebView.getView()).getSettings().setUseWideViewPort(true);

        LinearLayout.LayoutParams wvlp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        mainWebView.getView().setLayoutParams(wvlp);

        if (!mainWebView.isInitialized()) {
            mainWebView.init(cordovaInterface, pluginEntries, preferences);
        }
        cordovaInterface.onCordovaInit(mainWebView.getPluginManager());
        mainWebView.loadUrl(url);


        // pinch zoom
        scaleGestureDetector = new ScaleGestureDetector(getActivity(), new ScaleGestureDetector.SimpleOnScaleGestureListener() {
                @Override
                public boolean onScale(ScaleGestureDetector detector){
                    if(!RoomActivity.activity.getIsHandMode())  // Pinch Zoom은 Hand mode일 때만 허용함
                        return false;
                    scale *= detector.getScaleFactor();
                    scale = Math.max(0.1f, Math.min(scale, 5.0f));
                    mainWebView.sendJavascript("CanvasApp.pinchZoomNt(" + scale + ")");

                    int val = (int) (scale * 100);
                    val = val < 100 ? 100 : val;

                    RoomActivity.activity.setZoomVal(val);
                    return true;
                }

                @Override
                public boolean onScaleBegin(ScaleGestureDetector detector){

                    return true;
                }

                @Override
                public void onScaleEnd(ScaleGestureDetector detector){

                }
        });

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            scaleGestureDetector.setQuickScaleEnabled(false);
        }

        if(prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
            ((SystemWebView) mainWebView.getView()).getSettings().setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        }


        mainWebView.getView().setOnTouchListener(new View.OnTouchListener() {
            private int touchPointerId = -1;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int pCnt = event.getPointerCount();
                // zoom일 경우와 투터치일 경우 좌표를 그대로 내려 준다.
                if(RoomActivity.activity.getIsHandMode() && RoomActivity.activity.getZoomVal() <= 100 && pCnt < 2) {

                    //mainWebView.sendJavascript("Ctrl.toggleRC(0, -1, false);");
                    RoomActivity.activity.pollONOFF();
                    if(prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE && getResources().getConfiguration().orientation == Configuration.ORIENTATION_PORTRAIT) {
                        Log.d(TAG, "block vertical scrolling..");
                    }
                    return false;
                }

                if(RoomActivity.activity.getIsTextMode()) {
                    return false;
                }

                // 판서 폴 드래그해서 선택하는 부분 UI 처리 - test
                if(RoomActivity.activity.getIsSelectorMode()) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {   // 드래그 진입점
                        width = 0;
                        height = 0;
//                        dragSelector.getLayoutParams().width = 0;
//                        dragSelector.getLayoutParams().height = 0;
//                        dragSelector.requestLayout();
                        ((RoomActivity) getActivity()).clearSelectionArea();
                        //((RoomActivity)getActivity()).clearSelectorButton();
                        float x = event.getX();
                        float y = event.getY();
                        initX = x;
                        initY = y;
                        Log.d(TAG, "[selectorMode] begin / x : " + x + ", y : " + y);

                        /*
                        FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(0, 0);
                        lp.leftMargin = (int) (x * density);
                        lp.topMargin = (int) (y * density) - 48;
                        dragSelector.setLayoutParams(lp);
                        dragSelector.requestLayout();

                        selectableDeemedView.updateSelectorPaint(initX, initY, initX, initY);
                        */
                    }

                    if (event.getAction() == MotionEvent.ACTION_MOVE) {   // 드래그 무브

                        float diffX = initX - event.getX();
                        float diffY = initY - event.getY();
                        Log.d(TAG, "[selectorMode] diffX : " + diffX + ", diffY : " + diffY);

                        if (diffX > 0 && diffY < 0) {
                            direction = DragDirection.UP_RIGHT_TO_DOWN_LEFT;  // 우상단에서 좌하단으로 드래그
                        }
                        if (diffX < 0 && diffY > 0) {
                            direction = DragDirection.DOWN_LEFT_TO_UP_RIGHT;  // 좌하단에서 우상단으로 드래그
                        }
                        if (diffX > 0 && diffY > 0) {
                            direction = DragDirection.DOWN_RIGHT_TO_UP_LEFT;  // 우하단에서 좌상단으로 드래그
                        }
                        if(diffX < 0 && diffY < 0) {
                            direction = DragDirection.UP_LEFT_TO_DOWN_RIGHT;  // 좌상단에서 우하단으로 드래그
                        }

                        width = Math.abs((int) diffX);
                        height = Math.abs((int) diffY);
                        Log.d(TAG, "===================================================");
                        Log.d(TAG, "[selectorMode] x : " + event.getX() + ", y : " + event.getY());
                        Log.d(TAG, "[selectorMode] width : " + width + ", height : " + height);
                        /*
                        dragSelector.getLayoutParams().width = (int) (width * density);
                        dragSelector.getLayoutParams().height = (int) (height * density);
                        dragSelector.requestLayout();
                        */

                        ((RoomActivity)getActivity()).adjustSelection(initX, initY, event.getX(), event.getY());
//                        selectableDeemedView.updateSelectorPaint(initX, initY, event.getRawX(), event.getRawY());
                    }

                    if (event.getAction() == MotionEvent.ACTION_UP) {   // 드래그 종료
                        Log.d(TAG, "[selectorMode] final coordinate / x : " + initX + ", y : " + initY + ", width : " + width + ", height : " + height);

                        float btnPosX = 0.f;
                        float btnPosY = 0.f;
                        float sx = 0.f, sy = 0.f;
                        if (direction == DragDirection.DOWN_LEFT_TO_UP_RIGHT) {
                            btnPosX = initX + width;
                            btnPosY = initY;
                            sx = initX;
                            sy = initY - height;
                        } else if (direction == DragDirection.UP_RIGHT_TO_DOWN_LEFT) {
                            btnPosX = initX;
                            btnPosY = initY + height;
                            sx = initX - width;
                            sy = initY;
                        } else if (direction == DragDirection.UP_LEFT_TO_DOWN_RIGHT) {
                            btnPosX = initX + width;
                            btnPosY = initY + height;
                            sx = initX;
                            sy = initY;
                        } else if (direction == DragDirection.DOWN_RIGHT_TO_UP_LEFT) {
                            btnPosX = initX;
                            btnPosY = initY;
                            sx = initX - width;
                            sy = initY - height;
                        } else {
                            btnPosX = initX + width;
                            btnPosY = initY + height;
                        }
                        direction = null;

                        if(width >= (110*KnowloungeApplication.density) && height >= (60*KnowloungeApplication.density)) {   // 최소 선택 영역 예외처리..
                            ((RoomActivity) getActivity()).adjustSelectorButtonPosition(btnPosX, btnPosY);  // 버튼 UI 포지션 지정..
                            ((RoomActivity) getActivity()).setSelectionCoord(sx, sy, width, height);        // 캡쳐할 영역의 좌표정보 전달
                        } else {
                            ((RoomActivity) getActivity()).clearSelectionArea();
                            Toast.makeText(getContext(), getString(R.string.poll_draw_sel_area_lag), Toast.LENGTH_SHORT).show();
                        }
                        initX = 0.f;
                        initY = 0.f;
                    }
                    return true;
                }




                if (scaleGestureDetector.isInProgress()) {
                    // drawing이 살아있으면 강제로 종료 시킨다.
                    if (touchPointerId > -1) {
                        int cnt = event.getPointerCount();
                        int idx = -1;
                        for (int i = 0; i < cnt; i++) {
                            int id = event.getPointerId(i);
                            if (touchPointerId == id) {
                                idx = i;
                            }
                        }
                        if (idx == -1) {
                            return false;
                        }
                        int x = (int) event.getX(idx);
                        int y = (int) event.getY(idx);

                        mainWebView.sendJavascript("CanvasApp.drawNt('ended'," + x + ", " + y + "," + density + "," + mainWebView.getView().getWidth() + "," + mainWebView.getView().getHeight() + ")");
//                        mainWebView.sendJavascript("UI.touchDetector('ended'," + x + ", " + y + "," + density + "," + mainWebView.getView().getWidth() + "," + mainWebView.getView().getHeight() + ")");
                    }

                    touchPointerId = -1;
                    scaleGestureDetector.onTouchEvent(event);
                    return true;
                }

                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    v.requestFocus();
                    if (drawSoundEnable) {
                        soundStreamId = mSoundPool.play(soundBeep, 1f, 1f, 0, -1, 1f);
                    }

                    if (RoomActivity.activity.mPopupWindow != null) {
                        if (RoomActivity.activity.mPopupWindow.isShowing()) {
                            RoomActivity.activity.mPopupWindow.setFocusable(false);
                            RoomActivity.activity.mPopupWindow.update();
                            RoomActivity.activity.mPopupWindow.dismiss();
                            return true;
                        }
                    }

                    // 첫번째로 터치한 id를 id로 한다.
                    touchPointerId = event.getPointerId(0);

                    float x = (float) event.getX(0);
                    float y = (float) event.getY(0);
                    Log.d(TAG, "begin / x : " + x + ", y : " + y);
                    if (isNtEnable) {
                        mainWebView.sendJavascript("CanvasApp.drawNt('began'," + x + ", " + y + "," + density + "," + mainWebView.getView().getWidth() + "," + mainWebView.getView().getHeight() + ")");
                    } else {
                        mainWebView.sendJavascript("UI.touchDetector('began'," + x + ", " + y + "," + density + "," + mainWebView.getView().getWidth() + "," + mainWebView.getView().getHeight() + ")");
                    }

                } else {
                    if (touchPointerId < 0) {
                        // scale 중일때 touch 이벤트가 들어오면 skip한다.
                        scaleGestureDetector.onTouchEvent(event);
                        return true;
                    }

                    int cnt = event.getPointerCount();
                    int idx = -1;
                    for (int i = 0; i < cnt; i++) {
                        int id = event.getPointerId(i);
                        if (touchPointerId == id) {
                            idx = i;
                        }
                    }

                    idx = 0;

                    if (event.getAction() == MotionEvent.ACTION_MOVE) {
                        float x = (float) event.getX(idx);
                        float y = (float) event.getY(idx);
                        Log.d(TAG, "move / x : " + x + ", y : " + y + " WebView height : " + mainWebView.getView().getHeight() / density);
                        mainWebView.sendJavascript("CanvasApp.drawNt('moved'," + x + ", " + y + "," + density + "," + mainWebView.getView().getWidth() + "," + mainWebView.getView().getHeight() + ")");

                    } else if (event.getAction() == MotionEvent.ACTION_UP) {
                        if (drawSoundEnable && soundStreamId == 0) {
                            mSoundPool.stop(soundStreamId);
                            soundStreamId = 0;
                        }

                        float x = (float) event.getX(idx);
                        float y = (float) event.getY(idx);
                        Log.d(TAG, "end / x : " + x + ", y : " + y);
                        mainWebView.sendJavascript("CanvasApp.drawNt('ended'," + x + ", " + y + "," + density + "," + mainWebView.getView().getWidth() + "," + mainWebView.getView().getHeight() + ")");

                        touchPointerId = -1;
                    }
                }

                scaleGestureDetector.onTouchEvent(event);
                /**  2016.03.22 중복 드로잉 관련
                 *  이곳을 true로 주지 않으면 event 중복 현상이 발생한다.
                 */
                return false;
            }
        });

        ((FrameLayout) rootView).addView(mainWebView.getView());
        ((FrameLayout) rootView).bringChildToFront(selectableDeemedView);
        ((FrameLayout) rootView).bringChildToFront(dragSelector);

        return rootView;
    }


    @Override
    public Object onMessage(String id, Object data) {
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy");
        super.onDestroy();
        /*
        if (mainWebView.getPluginManager() != null) {
            mainWebView.getPluginManager().onDestroy();
        }*/

        if(mainWebView != null) {
            mainWebView.handleDestroy();
            Log.d(this.getClass().getSimpleName(), "CordovaWebView destroy..");
        }
    }


    @Override
    public ExecutorService getThreadPool() {
        return threadPool;
    }


    @Override
    public void setActivityResultCallback(CordovaPlugin plugin) {
        this.activityResultCallback = plugin;
    }


    @Override
    public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
        this.activityResultCallback = command;
        this.activityResultKeepRunning = this.keepRunning;

        if(command != null) {
            this.keepRunning = false;
        }

        super.startActivityForResult(intent, requestCode);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        CordovaPlugin callback = this.activityResultCallback;
        if (callback != null) {
            callback.onActivityResult(requestCode, resultCode, intent);
        }
    }


    /**
     * A {@link ContextWrapper} that also implements {@link CordovaInterface} and acts as a proxy between the base
     * activity context and the fragment that contains a {@link CordovaWebView}.
     *
     */
    private class CordovaContext extends ContextWrapper implements CordovaInterface {
        CordovaInterface cordova;

        public CordovaContext(Context base, CordovaInterface cordova) {
            super(base);
            this.cordova = cordova;
        }

        public void startActivityForResult(CordovaPlugin command, Intent intent, int requestCode) {
            cordova.startActivityForResult(command, intent, requestCode);
        }

        public void setActivityResultCallback(CordovaPlugin plugin) {
            cordova.setActivityResultCallback(plugin);
        }

        public Activity getActivity() {
            return cordova.getActivity();
        }

        public Object onMessage(String id, Object data) {
            return cordova.onMessage(id, data);
        }

        public ExecutorService getThreadPool() {
            return cordova.getThreadPool();
        }

    }

    public CordovaWebView getCordovaWebView() {
        return this.mainWebView;
    }

    private SoundPool mSoundPool;
    private int soundBeep;
    private int soundStreamId = 0;
    private void initSound() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AudioAttributes attributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_GAME)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            mSoundPool = new SoundPool.Builder().setAudioAttributes(attributes).build();
        } else {
            mSoundPool = new SoundPool(5,AudioManager.STREAM_MUSIC,0);
        }
        soundBeep = mSoundPool.load(getContext(), R.raw.writing_on_blackboard_sound, 1);
    }



}
