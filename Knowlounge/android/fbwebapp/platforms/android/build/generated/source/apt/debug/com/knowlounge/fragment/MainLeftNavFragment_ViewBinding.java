// Generated code from Butter Knife. Do not modify!
package com.knowlounge.fragment;

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

public class MainLeftNavFragment_ViewBinding implements Unbinder {
  private MainLeftNavFragment target;

  @UiThread
  public MainLeftNavFragment_ViewBinding(MainLeftNavFragment target, View source) {
    this.target = target;

    target.btnMainleftHome = Utils.findRequiredViewAsType(source, R.id.btn_mainleft_home, "field 'btnMainleftHome'", LinearLayout.class);
    target.btnMainleftMyClass = Utils.findRequiredViewAsType(source, R.id.btn_mainleft_myclass, "field 'btnMainleftMyClass'", LinearLayout.class);
    target.btnMainleftFriendClass = Utils.findRequiredViewAsType(source, R.id.btn_mainleft_friendclass, "field 'btnMainleftFriendClass'", LinearLayout.class);
    target.btnMainleftSchoolClass = Utils.findRequiredViewAsType(source, R.id.btn_mainleft_schoolclass, "field 'btnMainleftSchoolClass'", LinearLayout.class);
    target.btnMainleftPublicClass = Utils.findRequiredViewAsType(source, R.id.btn_mainleft_publicclass, "field 'btnMainleftPublicClass'", LinearLayout.class);
    target.btnMainleftSetting = Utils.findRequiredViewAsType(source, R.id.btn_mainleft_setting, "field 'btnMainleftSetting'", LinearLayout.class);
    target.btnMainleftHelp = Utils.findRequiredViewAsType(source, R.id.btn_mainleft_help, "field 'btnMainleftHelp'", LinearLayout.class);
    target.btnMainleftStarshop = Utils.findRequiredViewAsType(source, R.id.btn_mainleft_starshop, "field 'btnMainleftStarshop'", LinearLayout.class);
    target.btnMainleftMyinfo = Utils.findRequiredViewAsType(source, R.id.btn_mainleft_myinfo, "field 'btnMainleftMyinfo'", LinearLayout.class);
    target.btnMainleftLogout = Utils.findRequiredViewAsType(source, R.id.btn_mainleft_logout, "field 'btnMainleftLogout'", LinearLayout.class);
    target.btnMainleftPremium = Utils.findRequiredViewAsType(source, R.id.btn_mainleft_premium, "field 'btnMainleftPremium'", LinearLayout.class);
    target.btnMainleftSetup = Utils.findRequiredViewAsType(source, R.id.btn_mainleft_setup, "field 'btnMainleftSetup'", ImageView.class);
    target.mainleftImg = Utils.findRequiredViewAsType(source, R.id.mainleft_img, "field 'mainleftImg'", ImageView.class);
    target.mainleftLoginType = Utils.findRequiredViewAsType(source, R.id.mainleft_login_type, "field 'mainleftLoginType'", ImageView.class);
    target.mainleftMyStarIco = Utils.findRequiredViewAsType(source, R.id.mainleft_my_star_ico, "field 'mainleftMyStarIco'", ImageView.class);
    target.btnLeftmenuBack = Utils.findRequiredViewAsType(source, R.id.btn_leftmenu_back, "field 'btnLeftmenuBack'", ImageView.class);
    target.mainleftMyName = Utils.findRequiredViewAsType(source, R.id.mainleft_my_name, "field 'mainleftMyName'", TextView.class);
    target.mainleftMyStar = Utils.findRequiredViewAsType(source, R.id.mainleft_my_star, "field 'mainleftMyStar'", TextView.class);
    target.mainleftMyEmail = Utils.findRequiredViewAsType(source, R.id.mainleft_my_email, "field 'mainleftMyEmail'", TextView.class);
    target.mainleftMyStarCharge = Utils.findRequiredViewAsType(source, R.id.mainleft_my_star_charge, "field 'mainleftMyStarCharge'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    MainLeftNavFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.btnMainleftHome = null;
    target.btnMainleftMyClass = null;
    target.btnMainleftFriendClass = null;
    target.btnMainleftSchoolClass = null;
    target.btnMainleftPublicClass = null;
    target.btnMainleftSetting = null;
    target.btnMainleftHelp = null;
    target.btnMainleftStarshop = null;
    target.btnMainleftMyinfo = null;
    target.btnMainleftLogout = null;
    target.btnMainleftPremium = null;
    target.btnMainleftSetup = null;
    target.mainleftImg = null;
    target.mainleftLoginType = null;
    target.mainleftMyStarIco = null;
    target.btnLeftmenuBack = null;
    target.mainleftMyName = null;
    target.mainleftMyStar = null;
    target.mainleftMyEmail = null;
    target.mainleftMyStarCharge = null;
  }
}
