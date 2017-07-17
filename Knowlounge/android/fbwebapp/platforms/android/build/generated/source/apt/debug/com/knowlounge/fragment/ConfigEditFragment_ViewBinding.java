// Generated code from Butter Knife. Do not modify!
package com.knowlounge.fragment;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.knowlounge.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ConfigEditFragment_ViewBinding implements Unbinder {
  private ConfigEditFragment target;

  @UiThread
  public ConfigEditFragment_ViewBinding(ConfigEditFragment target, View source) {
    this.target = target;

    target.btnConfigEditBack = Utils.findRequiredViewAsType(source, R.id.btn_config_edit_back, "field 'btnConfigEditBack'", ImageView.class);
    target.ConfigEditTitle = Utils.findRequiredViewAsType(source, R.id.config_edit_title, "field 'ConfigEditTitle'", TextView.class);
    target.ConfigEditSubtext = Utils.findRequiredViewAsType(source, R.id.config_edit_subtext, "field 'ConfigEditSubtext'", TextView.class);
    target.ConfigEditCommitBtn = Utils.findRequiredViewAsType(source, R.id.config_edit_commit_btn, "field 'ConfigEditCommitBtn'", TextView.class);
    target.ConfigEditEdittext = Utils.findRequiredViewAsType(source, R.id.config_edit_edittext, "field 'ConfigEditEdittext'", EditText.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    ConfigEditFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.btnConfigEditBack = null;
    target.ConfigEditTitle = null;
    target.ConfigEditSubtext = null;
    target.ConfigEditCommitBtn = null;
    target.ConfigEditEdittext = null;
  }
}
