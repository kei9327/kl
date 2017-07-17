// Generated code from Butter Knife. Do not modify!
package com.knowlounge.view.room;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.knowlounge.R;
import com.knowlounge.customview.CircleSurfaceViewRenderer;
import java.lang.IllegalStateException;
import java.lang.Override;

public class RtcStatusDialogFragment_ViewBinding implements Unbinder {
  private RtcStatusDialogFragment target;

  private View view2131755730;

  private View view2131755731;

  private View view2131755732;

  private View view2131755700;

  private View view2131755725;

  @UiThread
  public RtcStatusDialogFragment_ViewBinding(final RtcStatusDialogFragment target, View source) {
    this.target = target;

    View view;
    target.mDialogContainer = Utils.findRequiredViewAsType(source, R.id.dialog_container, "field 'mDialogContainer'", LinearLayout.class);
    target.mDialogBody = Utils.findRequiredViewAsType(source, R.id.dialog_body, "field 'mDialogBody'", LinearLayout.class);
    target.mVideoSettingTitle = Utils.findRequiredViewAsType(source, R.id.video_setting_title, "field 'mVideoSettingTitle'", TextView.class);
    view = Utils.findRequiredView(source, R.id.switch_video, "field 'mSwitchVideo' and method 'onVideoSwitchClick'");
    target.mSwitchVideo = Utils.castView(view, R.id.switch_video, "field 'mSwitchVideo'", ImageView.class);
    view2131755730 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onVideoSwitchClick();
      }
    });
    view = Utils.findRequiredView(source, R.id.switch_mic, "field 'mSwitchMic' and method 'onMicrophoneSwitchClick'");
    target.mSwitchMic = Utils.castView(view, R.id.switch_mic, "field 'mSwitchMic'", ImageView.class);
    view2131755731 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onMicrophoneSwitchClick();
      }
    });
    view = Utils.findRequiredView(source, R.id.switch_audio, "field 'mSwitchVolume' and method 'onVolumeSwitchClick'");
    target.mSwitchVolume = Utils.castView(view, R.id.switch_audio, "field 'mSwitchVolume'", ImageView.class);
    view2131755732 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onVolumeSwitchClick();
      }
    });
    target.mSwitchNormalClass = Utils.findRequiredViewAsType(source, R.id.switch_normal, "field 'mSwitchNormalClass'", SwitchCompat.class);
    target.mSwitchWhiteboard = Utils.findRequiredViewAsType(source, R.id.switch_whiteboard, "field 'mSwitchWhiteboard'", SwitchCompat.class);
    target.mSwitchClassVideo = Utils.findRequiredViewAsType(source, R.id.switch_video_control, "field 'mSwitchClassVideo'", SwitchCompat.class);
    view = Utils.findRequiredView(source, R.id.dialog_btn_confirm, "field 'mBtnConfirm' and method 'onClickConfirm'");
    target.mBtnConfirm = Utils.castView(view, R.id.dialog_btn_confirm, "field 'mBtnConfirm'", TextView.class);
    view2131755700 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickConfirm();
      }
    });
    target.mUserThumbImg = Utils.findRequiredViewAsType(source, R.id.video_setting_user_thumb, "field 'mUserThumbImg'", ImageView.class);
    target.mLocalPreviewRenderer = Utils.findRequiredViewAsType(source, R.id.preview_local_video, "field 'mLocalPreviewRenderer'", CircleSurfaceViewRenderer.class);
    view = Utils.findRequiredView(source, R.id.btn_video_setting_close, "field 'mBtnClose' and method 'onClickClose'");
    target.mBtnClose = Utils.castView(view, R.id.btn_video_setting_close, "field 'mBtnClose'", ImageView.class);
    view2131755725 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickClose();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    RtcStatusDialogFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mDialogContainer = null;
    target.mDialogBody = null;
    target.mVideoSettingTitle = null;
    target.mSwitchVideo = null;
    target.mSwitchMic = null;
    target.mSwitchVolume = null;
    target.mSwitchNormalClass = null;
    target.mSwitchWhiteboard = null;
    target.mSwitchClassVideo = null;
    target.mBtnConfirm = null;
    target.mUserThumbImg = null;
    target.mLocalPreviewRenderer = null;
    target.mBtnClose = null;

    view2131755730.setOnClickListener(null);
    view2131755730 = null;
    view2131755731.setOnClickListener(null);
    view2131755731 = null;
    view2131755732.setOnClickListener(null);
    view2131755732 = null;
    view2131755700.setOnClickListener(null);
    view2131755700 = null;
    view2131755725.setOnClickListener(null);
    view2131755725 = null;
  }
}
