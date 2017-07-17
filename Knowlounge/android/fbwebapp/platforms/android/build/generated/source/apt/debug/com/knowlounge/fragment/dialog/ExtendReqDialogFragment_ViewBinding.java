// Generated code from Butter Knife. Do not modify!
package com.knowlounge.fragment.dialog;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.knowlounge.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ExtendReqDialogFragment_ViewBinding implements Unbinder {
  private ExtendReqDialogFragment target;

  @UiThread
  public ExtendReqDialogFragment_ViewBinding(ExtendReqDialogFragment target, View source) {
    this.target = target;

    target.btn_req_extend = Utils.findRequiredViewAsType(source, R.id.btn_req_extend, "field 'btn_req_extend'", Button.class);
    target.btn_extend_release = Utils.findRequiredViewAsType(source, R.id.btn_extend_release, "field 'btn_extend_release'", LinearLayout.class);
    target.extend_request_content = Utils.findRequiredViewAsType(source, R.id.extend_request_content, "field 'extend_request_content'", TextView.class);
    target.txt_my_star = Utils.findRequiredViewAsType(source, R.id.txt_my_star, "field 'txt_my_star'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    ExtendReqDialogFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.btn_req_extend = null;
    target.btn_extend_release = null;
    target.extend_request_content = null;
    target.txt_my_star = null;
  }
}
