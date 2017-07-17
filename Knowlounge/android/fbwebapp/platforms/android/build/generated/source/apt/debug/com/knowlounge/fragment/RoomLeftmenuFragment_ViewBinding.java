// Generated code from Butter Knife. Do not modify!
package com.knowlounge.fragment;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.knowlounge.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class RoomLeftmenuFragment_ViewBinding implements Unbinder {
  private RoomLeftmenuFragment target;

  @UiThread
  public RoomLeftmenuFragment_ViewBinding(RoomLeftmenuFragment target, View source) {
    this.target = target;

    target.btnLeftmenuBack = Utils.findRequiredViewAsType(source, R.id.btn_leftmenu_back, "field 'btnLeftmenuBack'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    RoomLeftmenuFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.btnLeftmenuBack = null;
  }
}
