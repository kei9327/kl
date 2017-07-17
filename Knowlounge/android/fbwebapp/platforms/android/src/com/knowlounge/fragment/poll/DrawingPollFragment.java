package com.knowlounge.fragment.poll;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.model.PollCreateData;

/**
 * Created by Mansu on 2016-12-09.
 */

public class DrawingPollFragment extends PollDialogFragment {

    View rootView;

    private final String TAG = "DrawingPollFragment";

    private ImageView backBtn, icoTargetUserAll, icoTargetUserTeacher, icoFullScreen, icoSelection, icoUpload;
    private TextView confirmBtn, txtTargetUserAll, txtTargetUserTeacher, txtFullScreen, txtSelection, txtUpload;
    private LinearLayout optTargetUserAll, optTargetUserTeacher, optFullScreen, optSelection, optUpload;

    private String imgBinary = "";
    private int questionMethod;

    private String targetUser = "";

    private Context context;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    public static DrawingPollFragment _instance;

    ImageView capturePreview;


    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
        this.context = context;
        _instance = this;

        fragmentManager = getFragmentManager();
    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView");

        final Bundle param = getArguments();

        if (rootView == null)
            rootView = inflater.inflate(R.layout.poll_drawing, container, false);

        backBtn = (ImageView) rootView.findViewById(R.id.btn_back);
        confirmBtn = (TextView) rootView.findViewById(R.id.btn_confirm);
        capturePreview = (ImageView) rootView.findViewById(R.id.capture_preview);

        icoTargetUserAll = (ImageView) rootView.findViewById(R.id.ico_target_user_all);
        icoTargetUserTeacher = (ImageView) rootView.findViewById(R.id.ico_target_user_teacher);
        icoFullScreen = (ImageView) rootView.findViewById(R.id.opt_full_screen_ico);
        icoSelection = (ImageView) rootView.findViewById(R.id.opt_selection_ico);
        icoUpload = (ImageView) rootView.findViewById(R.id.opt_upload_ico);

        txtTargetUserAll = (TextView) rootView.findViewById(R.id.txt_target_user_all);
        txtTargetUserTeacher = (TextView) rootView.findViewById(R.id.txt_target_user_teacher);
        txtFullScreen = (TextView) rootView.findViewById(R.id.opt_full_screen_txt);
        txtSelection = (TextView) rootView.findViewById(R.id.opt_selection_txt);
        txtUpload = (TextView) rootView.findViewById(R.id.opt_upload_txt);

        optTargetUserAll = (LinearLayout) rootView.findViewById(R.id.target_user_all);
        optTargetUserTeacher = (LinearLayout) rootView.findViewById(R.id.target_user_teacher);
        optFullScreen = (LinearLayout) rootView.findViewById(R.id.opt_full_screen);
        optSelection = (LinearLayout) rootView.findViewById(R.id.opt_selection);
        optUpload = (LinearLayout) rootView.findViewById(R.id.opt_upload);

        if (RoomActivity.activity.getTeacherFlag() && RoomActivity.activity.getCreatorFlag()) { // 수업의 개설자일 경우..
            optTargetUserTeacher.setClickable(false);
            typeImgChecked(icoTargetUserAll, icoTargetUserTeacher, null, null);
            typeTextChecked(txtTargetUserAll, txtTargetUserTeacher, null, null);
            targetUser = PollCreateData.TARGET_USER_ALL;
        } else if (!RoomActivity.activity.getTeacherFlag() && RoomActivity.activity.getCreatorFlag()) {  // 학생방의 개설자일 경우..
            optTargetUserAll.setClickable(false);
            typeImgChecked(icoTargetUserTeacher, icoTargetUserAll, null, null);
            typeTextChecked(txtTargetUserTeacher, txtTargetUserAll, null, null);
            targetUser = PollCreateData.TARGET_USER_TEACHER;
        }

        // 전체 화면, 영역 선택, 직접 선택 여부 설정
        questionMethod = RoomActivity.activity.pollData.getDrawingMethod();
        switch (questionMethod) {
            case PollCreateData.FULL_SCREEN_CAPTURE :
                typeImgChecked(icoFullScreen, icoSelection, icoUpload, null);
                typeTextChecked(txtFullScreen, txtSelection, txtUpload, null);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        captureFullScreen();
                    }
                }, 200);
                break;
            case PollCreateData.SELECTION_CAPTURE :
                typeImgChecked(icoSelection, icoFullScreen, icoUpload, null);
                typeTextChecked(txtSelection, txtFullScreen, txtUpload, null);
                imgBinary = RoomActivity.activity.pollData.getCapturedImgBinary();
                if (!TextUtils.isEmpty(imgBinary)) {
                    setCapturePreview(imgBinary);
                }
                break;
            case PollCreateData.DIRECT_UPLOAD :
                typeImgChecked(icoUpload, icoFullScreen, icoSelection, null);
                typeTextChecked(txtUpload, txtFullScreen, txtSelection, null);
                imgBinary = RoomActivity.activity.pollData.getCapturedImgBinary();
                setCapturePreview(imgBinary);
                break;
            default :
                Log.d(TAG, "기본 전체 화면으로 세팅..");
                typeImgChecked(icoFullScreen, icoSelection, icoUpload, null);
                typeTextChecked(txtFullScreen, txtSelection, txtUpload, null);
                captureFullScreen();
                break;
        }

        if (param != null) {
            typeImgChecked(icoSelection, icoFullScreen, icoUpload, null);
            typeTextChecked(txtSelection, txtFullScreen, txtUpload, null);
        }


        optTargetUserAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomActivity.activity.pollData.setTargetUser(PollCreateData.TARGET_USER_ALL);
            }
        });

        optTargetUserTeacher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomActivity.activity.pollData.setTargetUser(PollCreateData.TARGET_USER_TEACHER);
            }
        });

        optFullScreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                typeImgChecked(icoFullScreen, icoSelection, icoUpload, null);
                typeTextChecked(txtFullScreen, txtSelection, txtUpload, null);
                RoomActivity.activity.pollData.setIsChange(true);
                RoomActivity.activity.pollData.setCheckedType(PollCreateData.POLL_TYPE_DRAWING);
                RoomActivity.activity.pollData.setDrawingMethod(PollCreateData.FULL_SCREEN_CAPTURE);
                captureFullScreen();
            }
        });
        optSelection.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomActivity.activity.resetZoom();   // 줌 값은 100으로 초기화 : 줌 상태에서 영역 캡쳐를 진행할 때 이슈가 발생함 - 2017.01.26
                getActivity().finish();
                if (RoomActivity.activity != null) {
                    RoomActivity.activity.invokeAreaSelector("question");
                }
            }
        });
        optUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UploadOptFragment fragment = new UploadOptFragment();
                fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.dialog_main_container, fragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //RoomActivity.activity.pollData.setCheckedType(PollCreateData.POLL_TYPE_DRAWING);
                RoomActivity.activity.pollData.removeDrawingPollInfo();
                if (param != null) {
                    pollCreateFragment createFragment = new pollCreateFragment();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.dialog_main_container, createFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } else {
                    getFragmentManager().popBackStack();
                }

            }
        });
        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomActivity.activity.pollData.setIsChange(true);
                RoomActivity.activity.pollData.setCheckedType(PollCreateData.POLL_TYPE_DRAWING);
                RoomActivity.activity.pollData.setCapturedImgBinary(imgBinary);
                RoomActivity.activity.pollData.setTargetUser(targetUser);
                Log.d(TAG, "pollType : " + PollCreateData.POLL_TYPE_DRAWING);
                Log.d(TAG, "target : " + targetUser);
                if (questionMethod == PollCreateData.SELECTION_CAPTURE) {
                    pollCreateFragment createFragment = new pollCreateFragment();
                    fragmentTransaction = fragmentManager.beginTransaction();
                    fragmentTransaction.replace(R.id.dialog_main_container, createFragment);
                    fragmentTransaction.addToBackStack(null);
                    fragmentTransaction.commit();
                } else {
                    getFragmentManager().popBackStack();
                }
            }
        });




        imgBinary = RoomActivity.activity.pollData.getCapturedImgBinary();

        return rootView;
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        Log.d(TAG, "onActivityCreated");
        super.onActivityCreated(savedInstanceState);

    }


    /**
     * 전체화면 캡쳐 시작
     */
    public void captureFullScreen() {
        Log.d(TAG, "captureFullScreen");
        RoomActivity.activity.mWebViewFragment.getCordovaWebView().sendJavascript("PollCtrl.UI.captureCanvas('question')");
    }


    /**
     * 전체화면 캡쳐 바이너리 처리
     * @param imgBinary
     */
    public void applyFullScreenCapture(String imgBinary) {
        Log.d(TAG, "applyFullScreenCapture");
        this.imgBinary = imgBinary;
        RoomActivity.activity.pollData.setCapturedImgBinary(imgBinary);
        if(context != null && this.isVisible())
            setCapturePreview(this.imgBinary);
    }


    /**
     * 캡쳐한 바이너리를 출력
     * @param base64Str
     */
    public void setCapturePreview(String base64Str) {
        this.imgBinary = base64Str;
        final byte[] decodedString = Base64.decode(base64Str, Base64.DEFAULT);


        if (context != null && !getActivity().isDestroyed()) {
            new Handler(context.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Glide.with(context)
                            .load(decodedString)
                            .asBitmap()
                            .error(context.getResources().getIdentifier("thumbnail_01", "drawable", context.getPackageName()))
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .into(capturePreview);
                }
            });
        }
    }
}
