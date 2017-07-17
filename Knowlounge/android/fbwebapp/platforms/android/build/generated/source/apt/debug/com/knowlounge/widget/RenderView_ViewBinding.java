// Generated code from Butter Knife. Do not modify!
package com.knowlounge.widget;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.knowlounge.R;
import java.lang.IllegalStateException;
import java.lang.Override;
import org.webrtc.SurfaceViewRenderer;

public class RenderView_ViewBinding implements Unbinder {
  private RenderView target;

  @UiThread
  public RenderView_ViewBinding(RenderView target, View source) {
    this.target = target;

    target.surface = Utils.findRequiredViewAsType(source, R.id.video_view_renderer, "field 'surface'", SurfaceViewRenderer.class);
    target.userNameView = Utils.findRequiredViewAsType(source, R.id.local_video_user_nm, "field 'userNameView'", TextView.class);
    target.mNoVideoLayout = Utils.findRequiredViewAsType(source, R.id.layout_no_video, "field 'mNoVideoLayout'", LinearLayout.class);
    target.mVideoLoader = Utils.findRequiredViewAsType(source, R.id.video_loading, "field 'mVideoLoader'", ImageView.class);
    target.mNoVideoView = Utils.findRequiredViewAsType(source, R.id.no_video_img, "field 'mNoVideoView'", ImageView.class);
    target.mIcoVideoAuthrity = Utils.findRequiredViewAsType(source, R.id.ico_video_authority, "field 'mIcoVideoAuthrity'", ImageView.class);
    target.mVideoControllerLayout = Utils.findRequiredViewAsType(source, R.id.layout_video_controller, "field 'mVideoControllerLayout'", LinearLayout.class);
    target.mBtnScreenMax = Utils.findRequiredViewAsType(source, R.id.btn_screen_max, "field 'mBtnScreenMax'", ImageView.class);
    target.mBtnVideoReconnect = Utils.findRequiredViewAsType(source, R.id.btn_video_reconnect, "field 'mBtnVideoReconnect'", ImageView.class);
    target.mBtnVolumeControl = Utils.findRequiredViewAsType(source, R.id.btn_volume_control, "field 'mBtnVolumeControl'", ImageView.class);
    target.mBtnVideoSetting = Utils.findRequiredViewAsType(source, R.id.btn_video_setting, "field 'mBtnVideoSetting'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    RenderView target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.surface = null;
    target.userNameView = null;
    target.mNoVideoLayout = null;
    target.mVideoLoader = null;
    target.mNoVideoView = null;
    target.mIcoVideoAuthrity = null;
    target.mVideoControllerLayout = null;
    target.mBtnScreenMax = null;
    target.mBtnVideoReconnect = null;
    target.mBtnVolumeControl = null;
    target.mBtnVideoSetting = null;
  }
}
