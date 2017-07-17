// Generated code from Butter Knife. Do not modify!
package com.knowlounge.fragment;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.knowlounge.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class RoomRightSlideFragment_ViewBinding implements Unbinder {
  private RoomRightSlideFragment target;

  private View view2131755648;

  private View view2131755650;

  private View view2131755651;

  private View view2131755653;

  private View view2131755655;

  @UiThread
  public RoomRightSlideFragment_ViewBinding(final RoomRightSlideFragment target, View source) {
    this.target = target;

    View view;
    view = Utils.findRequiredView(source, R.id.class_right_slide_menu_userlist, "field 'user' and method 'onClickUserList'");
    target.user = Utils.castView(view, R.id.class_right_slide_menu_userlist, "field 'user'", ImageView.class);
    view2131755648 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickUserList();
      }
    });
    view = Utils.findRequiredView(source, R.id.class_right_slide_menu_invite, "field 'invite' and method 'onClickInvite'");
    target.invite = Utils.castView(view, R.id.class_right_slide_menu_invite, "field 'invite'", ImageView.class);
    view2131755650 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickInvite();
      }
    });
    view = Utils.findRequiredView(source, R.id.class_right_slide_menu_community, "field 'community' and method 'onClickCommunity'");
    target.community = Utils.castView(view, R.id.class_right_slide_menu_community, "field 'community'", ImageView.class);
    view2131755651 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickCommunity();
      }
    });
    view = Utils.findRequiredView(source, R.id.class_right_slide_menu_comment, "field 'comment' and method 'onClickComment'");
    target.comment = Utils.castView(view, R.id.class_right_slide_menu_comment, "field 'comment'", ImageView.class);
    view2131755653 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickComment();
      }
    });
    view = Utils.findRequiredView(source, R.id.class_right_slide_menu_back, "field 'back' and method 'onClickBack'");
    target.back = Utils.castView(view, R.id.class_right_slide_menu_back, "field 'back'", ImageView.class);
    view2131755655 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickBack();
      }
    });
    target.userBadge = Utils.findRequiredViewAsType(source, R.id.class_right_slide_menu_userlist_badge, "field 'userBadge'", TextView.class);
    target.communityBadge = Utils.findRequiredViewAsType(source, R.id.class_right_slide_menu_community_badge, "field 'communityBadge'", TextView.class);
    target.commentBadge = Utils.findRequiredViewAsType(source, R.id.class_right_slide_menu_comment_badge, "field 'commentBadge'", TextView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    RoomRightSlideFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.user = null;
    target.invite = null;
    target.community = null;
    target.comment = null;
    target.back = null;
    target.userBadge = null;
    target.communityBadge = null;
    target.commentBadge = null;

    view2131755648.setOnClickListener(null);
    view2131755648 = null;
    view2131755650.setOnClickListener(null);
    view2131755650 = null;
    view2131755651.setOnClickListener(null);
    view2131755651 = null;
    view2131755653.setOnClickListener(null);
    view2131755653 = null;
    view2131755655.setOnClickListener(null);
    view2131755655 = null;
  }
}
