// Generated code from Butter Knife. Do not modify!
package com.knowlounge.fragment;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.knowlounge.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ProfileMultiSelectItemFragment_ViewBinding implements Unbinder {
  private ProfileMultiSelectItemFragment target;

  @UiThread
  public ProfileMultiSelectItemFragment_ViewBinding(ProfileMultiSelectItemFragment target, View source) {
    this.target = target;

    target.profileMultiSelectListRootView = Utils.findRequiredViewAsType(source, R.id.profile_multi_select_list_root_view, "field 'profileMultiSelectListRootView'", RelativeLayout.class);
    target.multiSelectTitle = Utils.findRequiredViewAsType(source, R.id.multi_select_title, "field 'multiSelectTitle'", TextView.class);
    target.multiSelectListView = Utils.findRequiredViewAsType(source, R.id.multi_select_list_view, "field 'multiSelectListView'", ListView.class);
    target.multiSelectOk = Utils.findRequiredViewAsType(source, R.id.multi_select_ok, "field 'multiSelectOk'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    ProfileMultiSelectItemFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.profileMultiSelectListRootView = null;
    target.multiSelectTitle = null;
    target.multiSelectListView = null;
    target.multiSelectOk = null;
  }
}
