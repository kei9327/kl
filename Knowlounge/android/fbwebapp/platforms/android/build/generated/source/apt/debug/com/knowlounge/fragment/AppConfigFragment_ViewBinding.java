// Generated code from Butter Knife. Do not modify!
package com.knowlounge.fragment;

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
import java.lang.IllegalStateException;
import java.lang.Override;

public class AppConfigFragment_ViewBinding implements Unbinder {
  private AppConfigFragment target;

  private View view2131755526;

  private View view2131755527;

  private View view2131755529;

  private View view2131755531;

  private View view2131755532;

  private View view2131755530;

  @UiThread
  public AppConfigFragment_ViewBinding(final AppConfigFragment target, View source) {
    this.target = target;

    View view;
    target.btnMainleftSettingBack = Utils.findRequiredViewAsType(source, R.id.btn_mainleft_setting_back, "field 'btnMainleftSettingBack'", ImageView.class);
    target.establishClosed = Utils.findRequiredViewAsType(source, R.id.establish_closed, "field 'establishClosed'", LinearLayout.class);
    view = Utils.findRequiredView(source, R.id.mainleft_setting_profile, "field 'mainleftSettingProfile' and method 'OnClickProfileItem'");
    target.mainleftSettingProfile = Utils.castView(view, R.id.mainleft_setting_profile, "field 'mainleftSettingProfile'", LinearLayout.class);
    view2131755526 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickProfileItem();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_mainleft_dropbox, "field 'btnMainleftDropbox' and method 'OnClickDropBoxItem'");
    target.btnMainleftDropbox = Utils.castView(view, R.id.btn_mainleft_dropbox, "field 'btnMainleftDropbox'", LinearLayout.class);
    view2131755527 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickDropBoxItem();
      }
    });
    view = Utils.findRequiredView(source, R.id.mainleft_beeper, "field 'mainleftBeeper' and method 'OnClickBeeperItem'");
    target.mainleftBeeper = Utils.castView(view, R.id.mainleft_beeper, "field 'mainleftBeeper'", LinearLayout.class);
    view2131755529 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBeeperItem();
      }
    });
    view = Utils.findRequiredView(source, R.id.service_access_terms, "field 'service_access_terms' and method 'OnClickTermsItem'");
    target.service_access_terms = Utils.castView(view, R.id.service_access_terms, "field 'service_access_terms'", LinearLayout.class);
    view2131755531 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickTermsItem();
      }
    });
    view = Utils.findRequiredView(source, R.id.user_info_treaty, "field 'user_info_treaty' and method 'OnClickPrivacyItem'");
    target.user_info_treaty = Utils.castView(view, R.id.user_info_treaty, "field 'user_info_treaty'", LinearLayout.class);
    view2131755532 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickPrivacyItem();
      }
    });
    target.mainleftDropboxId = Utils.findRequiredViewAsType(source, R.id.mainleft_dropbox_id, "field 'mainleftDropboxId'", TextView.class);
    target.mainleftSettingVer = Utils.findRequiredViewAsType(source, R.id.mainleft_setting_ver, "field 'mainleftSettingVer'", TextView.class);
    view = Utils.findRequiredView(source, R.id.mainleft_push_toggle, "field 'mainleftPushTogle' and method 'OnClickPushSwitch'");
    target.mainleftPushTogle = Utils.castView(view, R.id.mainleft_push_toggle, "field 'mainleftPushTogle'", SwitchCompat.class);
    view2131755530 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickPushSwitch();
      }
    });
    target.classOpenFlagSwith = Utils.findRequiredViewAsType(source, R.id.switch_class_openflag, "field 'classOpenFlagSwith'", SwitchCompat.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    AppConfigFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.btnMainleftSettingBack = null;
    target.establishClosed = null;
    target.mainleftSettingProfile = null;
    target.btnMainleftDropbox = null;
    target.mainleftBeeper = null;
    target.service_access_terms = null;
    target.user_info_treaty = null;
    target.mainleftDropboxId = null;
    target.mainleftSettingVer = null;
    target.mainleftPushTogle = null;
    target.classOpenFlagSwith = null;

    view2131755526.setOnClickListener(null);
    view2131755526 = null;
    view2131755527.setOnClickListener(null);
    view2131755527 = null;
    view2131755529.setOnClickListener(null);
    view2131755529 = null;
    view2131755531.setOnClickListener(null);
    view2131755531 = null;
    view2131755532.setOnClickListener(null);
    view2131755532 = null;
    view2131755530.setOnClickListener(null);
    view2131755530 = null;
  }
}
