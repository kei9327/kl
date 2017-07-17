package com.knowlounge.view.room;

import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.widget.SwitchCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.customview.CircleSurfaceViewRenderer;
import com.knowlounge.dagger.scopes.PerActivity;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.RoomSpec;
import com.knowlounge.util.AndroidUtils;
import com.wescan.alo.rtc.RtcChatClient;
import com.wescan.alo.rtc.RtcChatContext;
import com.wescan.alo.rtc.RtcMediaChannel;
import org.webrtc.VideoRenderer;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static android.view.KeyEvent.KEYCODE_BACK;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Mansu
 * date: 2017-03-13.
 */

public class RtcStatusDialogFragment extends DialogFragment {

    private static final String TAG = RtcStatusDialogFragment.class.getSimpleName();

    private WenotePreferenceManager prefManager;



    interface OnStatusChangeListener {
        void onStatusInit(RoomSpec roomSpec, boolean isChange);
        void onStatusChange(RoomSpec roomSpec, boolean isChange);
        void onStatusCancel();
    }

    // DATA
    private RoomSpec mRoomSpec;
    private boolean mVideoEnable;
    private boolean mAudioEnable;
    private boolean mVolumeEnable;

    private boolean normalClassEnable;
    private boolean videoControlEnable;
    private boolean soundOnlyEnable;

    // View
    @BindView(R.id.dialog_container) LinearLayout mDialogContainer;
    @BindView(R.id.dialog_body) LinearLayout mDialogBody;

    @BindView(R.id.video_setting_title) TextView mVideoSettingTitle;
    @BindView(R.id.switch_video) ImageView mSwitchVideo;
    @BindView(R.id.switch_mic) ImageView mSwitchMic;
    @BindView(R.id.switch_audio) ImageView mSwitchVolume;
    @BindView(R.id.switch_normal) SwitchCompat mSwitchNormalClass;
    @BindView(R.id.switch_whiteboard) SwitchCompat mSwitchWhiteboard;
    @BindView(R.id.switch_video_control) SwitchCompat mSwitchClassVideo;
    @BindView(R.id.dialog_btn_confirm) TextView mBtnConfirm;
//    @BindView(R.id.dialog_btn_cancel) TextView mBtnCancel;
    @BindView(R.id.video_setting_user_thumb) ImageView mUserThumbImg;
    @BindView(R.id.preview_local_video) CircleSurfaceViewRenderer mLocalPreviewRenderer;
    @BindView(R.id.btn_video_setting_close) ImageView mBtnClose;

//    @BindView(R.id.preview_local_video) SurfaceViewRenderer mLocalPreviewRenderer;

    private boolean isInit = true;

    private RtcChatContext mContext;

    private OnStatusChangeListener mOnStatusChangeListener;

    private View.OnTouchListener listener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            if ("deemed".equals(v.getTag())) {
                onClickConfirm();
                return true;
            } else {
                return false;
            }

        }
    };

    public static RtcStatusDialogFragment newInstance(RoomSpec roomSpec) {
        RtcStatusDialogFragment fragment = new RtcStatusDialogFragment();
        Bundle arguments = new Bundle();
        arguments.putParcelable("arguments", roomSpec);
        fragment.setArguments(arguments);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "<onAttach>");
        super.onAttach(context);
        if (context instanceof RtcChatContext)
            mContext = (RtcChatContext) context;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setUpArguments();
        prefManager = WenotePreferenceManager.getInstance(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.d(TAG, "<onCreateView / ZICO>");

        View view = inflater.inflate(R.layout.fragment_rtc_status_dialog, container, false);

        ButterKnife.bind(this, view);
        setUpView();

        final Drawable bgDrawable = new ColorDrawable(Color.BLACK);
        bgDrawable.setAlpha(0);
        getDialog().getWindow().setBackgroundDrawable(bgDrawable);

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, KeyEvent event) {
                if (keyCode == KEYCODE_BACK) {
                    mOnStatusChangeListener.onStatusCancel();
                    dismiss();
                    if (isInit)
                        getActivity().finish();
                } else {

                }
                return false;
            }
        });

//        if (!isInit) {
//            mBtnCancel.setVisibility(View.GONE);
//        } else {
//            mBtnCancel.setVisibility(View.VISIBLE);
//        }

        return view;
    }




    @Override
    public void onCancel(DialogInterface dialog) {
        super.onCancel(dialog);
        onClickConfirm();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "<onActivityCreated>");
        super.onActivityCreated(savedInstanceState);
        getDialog().getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    }

    @Override
    public void onStart() {
        Log.d(TAG, "<onStart>");
        super.onStart();

        RtcChatClient.instance().startVideo();
        RtcMediaChannel localMediaChannel = RtcChatClient.instance().getLocalMediaChannel();
        if (localMediaChannel != null) {
            localMediaChannel.rebindVideoRenderer(new VideoRenderer(mLocalPreviewRenderer));
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mLocalPreviewRenderer != null) {
            mLocalPreviewRenderer.release();
        }
    }

    /**
     * 수업 모드
     *  - 영상 수업
     *  - 화이트보드 수업
     *  - 참여자 영상 제어 수업
     */
    private void setUpArguments() {
        Bundle arguments = getArguments();
        mRoomSpec = arguments.getParcelable("arguments");
        if (mRoomSpec != null) {
            if (TextUtils.isEmpty(mRoomSpec.getHost())) {
                isInit = true;
            } else {
                isInit = false;
            }
            mVideoEnable = mRoomSpec.isVideoEnabled();
            mAudioEnable = mRoomSpec.isAudioEnabled();
            mVolumeEnable = mRoomSpec.isVolumeEnabled();
            videoControlEnable = mRoomSpec.isVideoControlEnable();
            soundOnlyEnable = mRoomSpec.isWhiteboardMode();

            normalClassEnable = videoControlEnable != true && soundOnlyEnable != true ? true : false;

            if (!mRoomSpec.isParentMaster()) {
                if (videoControlEnable) {
                    if (!mRoomSpec.isAllowCaller()) {
                        mVideoEnable = false;
                        mAudioEnable = false;
                        mVolumeEnable = false;
                    } else {
                        mVideoEnable = true;
                        mAudioEnable = true;
                        mVolumeEnable = true;
                    }
                }
            }
            if (soundOnlyEnable) {
                mVideoEnable = false;
                mAudioEnable = true;
                mVolumeEnable = true;
            }

            Log.d(TAG, "<setUpArguments / ZICO> mVolumeEnable : " + mVolumeEnable + ", videoControlEnable : " + videoControlEnable + ", soundOnlyEnable : " + soundOnlyEnable);
        }
    }

    private void setUpView() {
        mDialogContainer.setOnTouchListener(listener);
        mDialogBody.setOnTouchListener(listener);

        if (mRoomSpec.isVideoControlEnable()) {
            mVideoSettingTitle.setText(getResources().getString(R.string.cam_video_setting_text_3));
        } else if (mRoomSpec.isWhiteboardMode()) {
            mVideoSettingTitle.setText(getResources().getString(R.string.cam_video_setting_text_2));
        } else {
            mVideoSettingTitle.setText(getResources().getString(R.string.cam_video_setting_text_1));
        }

        mLocalPreviewRenderer.init(mContext.getEglContext(), null);

//        RoundRectShape rect = new RoundRectShape(
//                new float[] {90,90, 90,90, 90,90, 90,90},
//                null,
//                null);
//        ShapeDrawable bg = new ShapeDrawable(rect);
//        mLocalPreviewRenderer.setBackgroundDrawable(bg);

//        mLocalPreviewRenderer.setBackground(getResources().getDrawable(R.drawable.mask_circle));  // 방법 2

        mSwitchVideo.setImageResource(mVideoEnable ? R.drawable.btn_video_setting_cam_on : R.drawable.btn_video_setting_cam);
        mSwitchMic.setImageResource(mAudioEnable ? R.drawable.btn_video_setting_mic_on : R.drawable.btn_video_setting_mic);
        mSwitchVolume.setImageResource(mVolumeEnable ? R.drawable.btn_video_setting_volume_on : R.drawable.btn_video_setting_volume);

        mSwitchNormalClass.setChecked(normalClassEnable);
        mSwitchClassVideo.setChecked(videoControlEnable);
        mSwitchWhiteboard.setChecked(soundOnlyEnable);

        Glide.with(getActivity())
                .load(AndroidUtils.changeSizeThumbnail(prefManager.getUserThumbnail(), 200))
                .error(getResources().getDrawable(R.drawable.img_userlist_default01))
                .transform(new CircleTransformTemp(getActivity())).into((mUserThumbImg));

        if (mRoomSpec.isParentMaster()) {   // 선생님이라면..
            if (!mRoomSpec.isSeparate()) {
                // 영상 수업 스위치 이벤트
                mSwitchNormalClass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        normalClassEnable = isChecked;
                        if (isChecked) {
                            mVideoEnable = true;
                            mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam_on);
                            mAudioEnable = true;
                            mSwitchMic.setImageResource(R.drawable.btn_video_setting_mic_on);

                            mSwitchWhiteboard.setChecked(!isChecked);
                            mSwitchClassVideo.setChecked(!isChecked);
                        }
                    }
                });
                // 화이트보드 모드 스위치 이벤트
                mSwitchWhiteboard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        soundOnlyEnable = isChecked;
                        if (isChecked) {
                            mVideoEnable = false;
                            mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam);
                            mAudioEnable = true;
                            mSwitchMic.setImageResource(R.drawable.btn_video_setting_mic_on);

                            mSwitchNormalClass.setChecked(!isChecked);
                            mSwitchClassVideo.setChecked(!isChecked);
                        }
                    }
                });
                // 영상 제어 모드 스위치 이벤트
                mSwitchClassVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        videoControlEnable = isChecked;
                        if (isChecked) {
                            mSwitchNormalClass.setChecked(!isChecked);
                            mSwitchWhiteboard.setChecked(!isChecked);
                            if (mRoomSpec.isParentMaster()) {
                                mVideoEnable = true;
                                mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam_on);
                            } else {
                                mVideoEnable = false;
                                mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam);
                            }
                        }
                    }
                });
            } else {
                // 일반 참여자는 수업 모드 스위치를 비활성화 처리..
                mSwitchNormalClass.setEnabled(false);
                mSwitchWhiteboard.setEnabled(false);
                mSwitchClassVideo.setEnabled(false);
                if (videoControlEnable) {
                    if (!mRoomSpec.isParentMaster() && !mRoomSpec.isAllowCaller()) {
                        mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam);
                        mSwitchMic.setImageResource(R.drawable.btn_video_setting_mic);
                        mSwitchVolume.setImageResource(R.drawable.btn_video_setting_volume);
                        mSwitchVideo.setClickable(false);
                        mSwitchMic.setClickable(false);
                        mSwitchVolume.setClickable(false);
                    }
                } else if (soundOnlyEnable) {
                    mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam);
                    mSwitchMic.setImageResource(R.drawable.btn_video_setting_mic_on);
                    mSwitchVolume.setImageResource(R.drawable.btn_video_setting_volume_on);
                    mSwitchVideo.setClickable(false);
                    mSwitchMic.setClickable(false);
                    mSwitchVolume.setClickable(false);
                }
            }
        } else {
            if (mRoomSpec.isSeparate()) {
                if (mRoomSpec.isCreator()) {
                    Log.d(TAG, "<setUpView / ZICO> 분리된 서브룸의 개설자입니다.");
                    // 영상 수업 스위치 이벤트
                    mSwitchNormalClass.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            normalClassEnable = isChecked;
                            if (isChecked) {
                                mVideoEnable = true;
                                mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam_on);
                                mAudioEnable = true;
                                mSwitchMic.setImageResource(R.drawable.btn_video_setting_mic_on);

                                mSwitchWhiteboard.setChecked(!isChecked);
                                mSwitchClassVideo.setChecked(!isChecked);
                            }
                        }
                    });
                    // 화이트보드 모드 스위치 이벤트
                    mSwitchWhiteboard.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            soundOnlyEnable = isChecked;
                            if (isChecked) {
                                mVideoEnable = false;
                                mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam);
                                mAudioEnable = true;
                                mSwitchMic.setImageResource(R.drawable.btn_video_setting_mic_on);

                                mSwitchNormalClass.setChecked(!isChecked);
                                mSwitchClassVideo.setChecked(!isChecked);
                            }
                        }
                    });
                    // 영상 제어 모드 스위치 이벤트
                    mSwitchClassVideo.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                            videoControlEnable = isChecked;
                            if (isChecked) {
                                mSwitchNormalClass.setChecked(!isChecked);
                                mSwitchWhiteboard.setChecked(!isChecked);
                                if (mRoomSpec.isParentMaster()) {
                                    mVideoEnable = true;
                                    mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam_on);
                                } else {
                                    if(mRoomSpec.isSeparate()) {
                                        if (mRoomSpec.isCreator()) {
                                            mVideoEnable = true;
                                            mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam_on);
                                        } else {
                                            mVideoEnable = false;
                                            mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam);
                                        }
                                    } else {
                                        mVideoEnable = false;
                                        mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam);
                                    }

                                }
                            }
                        }
                    });
                } else {
                    Log.d(TAG, "<setUpView> 분리된 서브룸의 참여자입니다.");
                    // 일반 참여자는 수업 모드 스위치를 비활성화 처리..
                    mSwitchNormalClass.setEnabled(false);
                    mSwitchWhiteboard.setEnabled(false);
                    mSwitchClassVideo.setEnabled(false);
                    if (videoControlEnable) {
                        if (!mRoomSpec.isParentMaster() && !mRoomSpec.isAllowCaller()) {
                            mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam);
                            mSwitchMic.setImageResource(R.drawable.btn_video_setting_mic);
                            mSwitchVolume.setImageResource(R.drawable.btn_video_setting_volume);
                            mSwitchVideo.setClickable(false);
                            mSwitchMic.setClickable(false);
                            mSwitchVolume.setClickable(false);
                        }
                    } else if (soundOnlyEnable) {
                        mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam);
                        mSwitchMic.setImageResource(R.drawable.btn_video_setting_mic_on);
                        mSwitchVolume.setImageResource(R.drawable.btn_video_setting_volume_on);
                        mSwitchVideo.setClickable(false);
                        mSwitchMic.setClickable(false);
                        mSwitchVolume.setClickable(false);
                    }
                }
            } else {

                // 일반 참여자는 수업 모드 스위치를 비활성화 처리..
                mSwitchNormalClass.setEnabled(false);
                mSwitchWhiteboard.setEnabled(false);
                mSwitchClassVideo.setEnabled(false);
                if (videoControlEnable) {
                    if (!mRoomSpec.isParentMaster() && !mRoomSpec.isAllowCaller()) {
                        mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam);
                        mSwitchMic.setImageResource(R.drawable.btn_video_setting_mic);
                        mSwitchVolume.setImageResource(R.drawable.btn_video_setting_volume);
                        mSwitchVideo.setClickable(false);
                        mSwitchMic.setClickable(false);
                        mSwitchVolume.setClickable(false);
                    }
                } else if (soundOnlyEnable) {
                    mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam);
                    mSwitchMic.setImageResource(R.drawable.btn_video_setting_mic_on);
                    mSwitchVolume.setImageResource(R.drawable.btn_video_setting_volume_on);
                    mSwitchVideo.setClickable(false);
                    mSwitchMic.setClickable(false);
                    mSwitchVolume.setClickable(false);
                }
            }
        }
    }

    void setOnStatusChangeListener(OnStatusChangeListener listener) {
        mOnStatusChangeListener = listener;
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.switch_video)
    void onVideoSwitchClick() {
        if (mVideoEnable) {
            mVideoEnable = false;
            mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam);
        } else {
            if (soundOnlyEnable) {
                Toast.makeText(getContext(), "화이트보드 모드에서는 영상을 켤 수 없습니다_다국어 필요", Toast.LENGTH_SHORT).show();
                return;
            }
            mVideoEnable = true;
            mSwitchVideo.setImageResource(R.drawable.btn_video_setting_cam_on);
        }
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.switch_mic)
    void onMicrophoneSwitchClick() {
        if (mAudioEnable) {
            if (soundOnlyEnable) {
                Toast.makeText(getContext(), "화이트보드 모드에서는 마이크를 끌 수 없습니다_다국어 필요", Toast.LENGTH_SHORT).show();
                return;
            }
            mAudioEnable = false;
            mSwitchMic.setImageResource(R.drawable.btn_video_setting_mic);
        } else {
            mAudioEnable = true;
            mSwitchMic.setImageResource(R.drawable.btn_video_setting_mic_on);
        }
    }


    @SuppressWarnings("unused")
    @OnClick(R.id.switch_audio)
    void onVolumeSwitchClick() {
        if (mVolumeEnable) {
            mVolumeEnable = false;
            mSwitchVolume.setImageResource(R.drawable.btn_video_setting_volume);
        } else {
            mVolumeEnable = true;
            mSwitchVolume.setImageResource(R.drawable.btn_video_setting_volume_on);
        }
    }


//    @SuppressWarnings("unused")
//    @OnClick(R.id.dialog_btn_cancel)
//    void onCancelClick() {
//        mOnStatusChangeListener.onStatusCancel();
//        dismiss();
//        if (isInit)
//            getActivity().finish();
//    }


    @SuppressWarnings("unused")
    @OnClick(R.id.btn_video_setting_close)
    void onClickClose() {
        mOnStatusChangeListener.onStatusCancel();
        dismiss();
        if (isInit)
            getActivity().finish();
    }


    boolean isOptionChange = false;

    @SuppressWarnings("unused")
    @OnClick(R.id.dialog_btn_confirm)
    void onClickConfirm() {
        if (mOnStatusChangeListener != null) {
            if (videoControlEnable != mRoomSpec.isVideoControlEnable()) {
                isOptionChange = true;
            }

            if (soundOnlyEnable != mRoomSpec.isWhiteboardMode()) {
                isOptionChange = true;
            }

            mRoomSpec.setIsVideoEnable(mVideoEnable);               // 로컬 비디오 on/off
            mRoomSpec.setIsAudioEnable(mAudioEnable);               // 로컬 오디오 on/off
            mRoomSpec.setIsVolumeEnable(mVolumeEnable);             // 원격 오디오 on/off
            mRoomSpec.setIsVideoControlEnable(videoControlEnable);  // 클래스룸 비디오 모드 설정
            mRoomSpec.setIsWhiteboardMode(soundOnlyEnable);         // 화이트보드 수업 설정

            if (isInit)
                mOnStatusChangeListener.onStatusInit(mRoomSpec, isOptionChange);
            else
                mOnStatusChangeListener.onStatusChange(mRoomSpec, isOptionChange);
            dismiss();
        }
    }
}