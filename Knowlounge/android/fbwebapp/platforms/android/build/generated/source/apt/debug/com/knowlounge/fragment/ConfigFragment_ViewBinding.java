// Generated code from Butter Knife. Do not modify!
package com.knowlounge.fragment;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.knowlounge.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ConfigFragment_ViewBinding implements Unbinder {
  private ConfigFragment target;

  @UiThread
  public ConfigFragment_ViewBinding(ConfigFragment target, View source) {
    this.target = target;

    target.ck_authtype = Utils.findRequiredViewAsType(source, R.id.ck_authtype, "field 'ck_authtype'", SwitchCompat.class);
    target.ck_passwd = Utils.findRequiredViewAsType(source, R.id.ck_passwd, "field 'ck_passwd'", SwitchCompat.class);
    target.passwdInput = Utils.findRequiredViewAsType(source, R.id.passwd_txt, "field 'passwdInput'", EditText.class);
    target.ck_chat = Utils.findRequiredViewAsType(source, R.id.ck_chat, "field 'ck_chat'", SwitchCompat.class);
    target.ck_cmt = Utils.findRequiredViewAsType(source, R.id.ck_cmt, "field 'ck_cmt'", SwitchCompat.class);
    target.ck_exp = Utils.findRequiredViewAsType(source, R.id.ck_exp, "field 'ck_exp'", SwitchCompat.class);
    target.ck_openflag = Utils.findRequiredViewAsType(source, R.id.ck_openflag, "field 'ck_openflag'", SwitchCompat.class);
    target.ck_only_teacher_cam = Utils.findRequiredViewAsType(source, R.id.ck_only_teacher_cam, "field 'ck_only_teacher_cam'", SwitchCompat.class);
    target.ck_only_teacher_video = Utils.findRequiredViewAsType(source, R.id.ck_only_teacher_video, "field 'ck_only_teacher_video'", SwitchCompat.class);
    target.classSettingClassTitleLayout = Utils.findRequiredViewAsType(source, R.id.class_setting_class_title_layout, "field 'classSettingClassTitleLayout'", LinearLayout.class);
    target.classSettingParticipantLimitLayout = Utils.findRequiredViewAsType(source, R.id.class_setting_participant_limit_layout, "field 'classSettingParticipantLimitLayout'", LinearLayout.class);
    target.classSettingPublicLimitLayout = Utils.findRequiredViewAsType(source, R.id.class_setting_open_flag_layout, "field 'classSettingPublicLimitLayout'", LinearLayout.class);
    target.classSettingClassDescLayout = Utils.findRequiredViewAsType(source, R.id.class_setting_class_desc_layout, "field 'classSettingClassDescLayout'", LinearLayout.class);
    target.classSettingPasswdOpenflagContainer = Utils.findRequiredViewAsType(source, R.id.class_setting_passwd_openflag_container, "field 'classSettingPasswdOpenflagContainer'", LinearLayout.class);
    target.layoutOptPasswd = Utils.findRequiredViewAsType(source, R.id.layout_opt_passwd, "field 'layoutOptPasswd'", LinearLayout.class);
    target.textViewRoomTitle = Utils.findRequiredViewAsType(source, R.id.class_setting_class_title, "field 'textViewRoomTitle'", TextView.class);
    target.textViewUserLimit = Utils.findRequiredViewAsType(source, R.id.txt_class_setting_participant_limit, "field 'textViewUserLimit'", TextView.class);
    target.textViewOpenFlag = Utils.findRequiredViewAsType(source, R.id.class_setting_open_flag, "field 'textViewOpenFlag'", TextView.class);
    target.textViewRoomDesc = Utils.findRequiredViewAsType(source, R.id.class_setting_class_desc, "field 'textViewRoomDesc'", TextView.class);
    target.btnRoomSettingBack = Utils.findRequiredViewAsType(source, R.id.btn_room_setting_back, "field 'btnRoomSettingBack'", ImageView.class);
    target.classSettingClassTitleNextArrow = Utils.findRequiredViewAsType(source, R.id.class_setting_class_title_next_arrow, "field 'classSettingClassTitleNextArrow'", ImageView.class);
    target.classSettingPublicLimitNextArrow = Utils.findRequiredViewAsType(source, R.id.class_setting_open_flag_next_arrow, "field 'classSettingPublicLimitNextArrow'", ImageView.class);
    target.classSettingClassDescNextArrow = Utils.findRequiredViewAsType(source, R.id.class_setting_class_desc_next_arrow, "field 'classSettingClassDescNextArrow'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    ConfigFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.ck_authtype = null;
    target.ck_passwd = null;
    target.passwdInput = null;
    target.ck_chat = null;
    target.ck_cmt = null;
    target.ck_exp = null;
    target.ck_openflag = null;
    target.ck_only_teacher_cam = null;
    target.ck_only_teacher_video = null;
    target.classSettingClassTitleLayout = null;
    target.classSettingParticipantLimitLayout = null;
    target.classSettingPublicLimitLayout = null;
    target.classSettingClassDescLayout = null;
    target.classSettingPasswdOpenflagContainer = null;
    target.layoutOptPasswd = null;
    target.textViewRoomTitle = null;
    target.textViewUserLimit = null;
    target.textViewOpenFlag = null;
    target.textViewRoomDesc = null;
    target.btnRoomSettingBack = null;
    target.classSettingClassTitleNextArrow = null;
    target.classSettingPublicLimitNextArrow = null;
    target.classSettingClassDescNextArrow = null;
  }
}
