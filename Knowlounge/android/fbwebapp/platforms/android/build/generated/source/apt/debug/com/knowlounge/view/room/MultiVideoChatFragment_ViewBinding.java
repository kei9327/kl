// Generated code from Butter Knife. Do not modify!
package com.knowlounge.view.room;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.knowlounge.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class MultiVideoChatFragment_ViewBinding implements Unbinder {
  private MultiVideoChatFragment target;

  private View view2131755495;

  @UiThread
  public MultiVideoChatFragment_ViewBinding(final MultiVideoChatFragment target, View source) {
    this.target = target;

    View view;
    target.mVideoContainer = Utils.findRequiredViewAsType(source, R.id.fragment_multi_video_chat, "field 'mVideoContainer'", HorizontalScrollView.class);
    view = Utils.findRequiredView(source, R.id.video_wrapper, "field 'mVideoWrapper' and method 'onVideoWrapperClick'");
    target.mVideoWrapper = Utils.castView(view, R.id.video_wrapper, "field 'mVideoWrapper'", FrameLayout.class);
    view2131755495 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onVideoWrapperClick();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    MultiVideoChatFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mVideoContainer = null;
    target.mVideoWrapper = null;

    view2131755495.setOnClickListener(null);
    view2131755495 = null;
  }
}
