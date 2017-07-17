// Generated code from Butter Knife. Do not modify!
package com.knowlounge.fragment.poll;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.knowlounge.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class pollCreateType1Fragment_ViewBinding implements Unbinder {
  private pollCreateType1Fragment target;

  @UiThread
  public pollCreateType1Fragment_ViewBinding(pollCreateType1Fragment target, View source) {
    this.target = target;

    target.addQuestion = Utils.findRequiredViewAsType(source, R.id.add_question, "field 'addQuestion'", LinearLayout.class);
    target.delQuestion = Utils.findRequiredViewAsType(source, R.id.del_question, "field 'delQuestion'", LinearLayout.class);
    target.pollQuestionAddLayout = Utils.findRequiredViewAsType(source, R.id.poll_question_add_layout, "field 'pollQuestionAddLayout'", LinearLayout.class);
    target.btnPollAddquestion = Utils.findRequiredViewAsType(source, R.id.btn_poll_addquestion, "field 'btnPollAddquestion'", ImageView.class);
    target.btnPollDelquestion = Utils.findRequiredViewAsType(source, R.id.btn_poll_delquestion, "field 'btnPollDelquestion'", ImageView.class);
    target.backBtn = Utils.findRequiredViewAsType(source, R.id.type1_poll_backbtn, "field 'backBtn'", ImageView.class);
    target.switchAllowMultiChoice = Utils.findRequiredViewAsType(source, R.id.allow_multichoice_switch, "field 'switchAllowMultiChoice'", SwitchCompat.class);
    target.pollAddquestionText = Utils.findRequiredViewAsType(source, R.id.poll_addquestion_text, "field 'pollAddquestionText'", TextView.class);
    target.pollDelquestionText = Utils.findRequiredViewAsType(source, R.id.poll_delquestion_text, "field 'pollDelquestionText'", TextView.class);
    target.confirmBtn = Utils.findRequiredViewAsType(source, R.id.type1_ok_btn, "field 'confirmBtn'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    pollCreateType1Fragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.addQuestion = null;
    target.delQuestion = null;
    target.pollQuestionAddLayout = null;
    target.btnPollAddquestion = null;
    target.btnPollDelquestion = null;
    target.backBtn = null;
    target.switchAllowMultiChoice = null;
    target.pollAddquestionText = null;
    target.pollDelquestionText = null;
    target.confirmBtn = null;
  }
}
