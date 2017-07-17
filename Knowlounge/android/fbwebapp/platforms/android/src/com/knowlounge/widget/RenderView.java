package com.knowlounge.widget;

import android.os.Handler;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.knowlounge.R;

import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Copyright 2017 Klounge. All Rights Reserved.
 * <p>
 * Alo
 * <p>
 * author: Jun-hyoung Lee
 * date: 2017-03-14.
 */

@SuppressWarnings("WeakerAccess")
public class RenderView {
    private View mLayout;

    @BindView(R.id.video_view_renderer)
    SurfaceViewRenderer surface;

    @BindView(R.id.local_video_user_nm)
    TextView userNameView;

    @BindView(R.id.layout_no_video)
    LinearLayout mNoVideoLayout;

    @BindView(R.id.video_loading)
    ImageView mVideoLoader;

    @BindView(R.id.no_video_img)
    ImageView mNoVideoView;

    @BindView(R.id.ico_video_authority)
    ImageView mIcoVideoAuthrity;

    @BindView(R.id.layout_video_controller)
    LinearLayout mVideoControllerLayout;

    @BindView(R.id.btn_screen_max)
    ImageView mBtnScreenMax;

    @BindView(R.id.btn_video_reconnect)
    ImageView mBtnVideoReconnect;

    @BindView(R.id.btn_volume_control)
    ImageView mBtnVolumeControl;

    @BindView(R.id.btn_video_setting)
    ImageView mBtnVideoSetting;

    private boolean isInit = false;

    private int position;

    public RenderView(View layout, int position) {
        ButterKnife.bind(this, layout);
        this.mLayout = layout;
        this.position = position;
    }

    public RenderView init(EglBase.Context eglContext, RendererCommon.RendererEvents events) {
        if (!isInit) {
            surface.init(eglContext, events);
            isInit = true;
        }
        return this;
    }

    public void release() {
        if (surface != null && isInit) {
            surface.release();
            surface = null;
            isInit = false;
        }
    }

    public SurfaceViewRenderer getRenderer() {
        return surface;
    }

    public void visibleRenderer() {
        mNoVideoLayout.setVisibility(View.GONE);
//        mNoVideoView.setVisibility(View.GONE);
        surface.setVisibility(View.VISIBLE);
    }

    public void goneRenderer() {
        surface.setVisibility(View.GONE);
        mNoVideoLayout.setVisibility(View.VISIBLE);
    }

    public void showScreenMaxBtn() {
        mBtnScreenMax.setVisibility(View.VISIBLE);
    }

    public void hideScreenMaxBtn() {
        mBtnScreenMax.setVisibility(View.GONE);
    }

    public void showReconnectBtn() {
        mBtnVideoReconnect.setVisibility(View.VISIBLE);
    }

    public void hideReconnectBtn() {
        mBtnVideoReconnect.setVisibility(View.GONE);
    }

    public void showVolumeControlBtn() {
        mBtnVolumeControl.setVisibility(View.VISIBLE);
    }

    public void hideVolumeControlBtn() {
        mBtnVolumeControl.setVisibility(View.GONE);
    }

    public void showVideoSettingBtn() {
        mBtnVideoSetting.setVisibility(View.VISIBLE);
    }

    public void hideVideoSettingBtn() {
        mBtnVideoSetting.setVisibility(View.GONE);
    }

    public void showHideControllMenu() {
        if (!mVideoControllerLayout.isShown() || mVideoControllerLayout.getAlpha() == 0.0f) {
//            mIcoVideoAuthrity.setVisibility(View.INVISIBLE);
            mVideoControllerLayout.setVisibility(View.VISIBLE);
            mVideoControllerLayout.setAlpha(0.0f);
            mVideoControllerLayout.animate().alpha(1.0f);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mVideoControllerLayout.animate().alpha(0.0f);
                }
            }, 5000);
        } else {
//            mIcoVideoAuthrity.setVisibility(View.VISIBLE);
            mVideoControllerLayout.animate().alpha(0.0f);
        }
    }

    public void setControllMenuView() {
        if (mVideoControllerLayout.isShown()) {
            mVideoControllerLayout.setVisibility(View.INVISIBLE);
        } else {
            mVideoControllerLayout.setVisibility(View.VISIBLE);
        }
    }

    public void hideControllMenuView() {
        if (mVideoControllerLayout.isShown()) {
            mVideoControllerLayout.setVisibility(View.INVISIBLE);
        }
    }


    public void visible() {
        if (!mLayout.isShown())
            mLayout.setVisibility(View.VISIBLE);
    }

    public void gone() {
        if (mLayout.isShown())
            mLayout.setVisibility(View.GONE);
    }


    public void visibleThumbnail() {
        if (!mNoVideoView.isShown())
            mNoVideoView.setVisibility(View.VISIBLE);
    }

    public void goneThumbnail() {
        if (mNoVideoView.isShown())
            mNoVideoView.setVisibility(View.GONE);
    }

    public void visibleLoader() {
        if (!mVideoLoader.isShown())
            mVideoLoader.setVisibility(View.VISIBLE);
    }

    public void goneLoader() {
        if (mVideoLoader.isShown())
            mVideoLoader.setVisibility(View.GONE);
    }


    public View getView() {
        return mLayout;
    }

    public ImageView getNoVideoView() {
        return mNoVideoView;
    }

    public ImageView getScreenMaxBtn() {
        return mBtnScreenMax;
    }

    public ImageView getIcoVideoAuthrity() {
        return mIcoVideoAuthrity;
    }

    public ImageView getVideoSettingBtn() {
        return mBtnVideoSetting;
    }

    public ImageView getReconnectBtn() {
        return mBtnVideoReconnect;
    }

    public ImageView getVolumeControlBtn() {
        return mBtnVolumeControl;
    }

    public ImageView getLoaderView() {
        return mVideoLoader;
    }

    public boolean isInit() {
        return isInit;
    }

    public int getPosition() {
        return position;
    }

    public void setMirror(boolean mirror) {
        surface.setMirror(mirror);
    }

    public void setScalingType(RendererCommon.ScalingType scalingType) {
        surface.setScalingType(scalingType);
    }

    public void setUserNm(String userNm) {
        userNameView.setText(userNm);
    }

    public void setUserNo(String userNo) {

    }

    public void setIsInit(boolean isInit) {
        this.isInit = isInit;
    }

    public void setPosition(int position) {
        this.position = position;
    }
}
