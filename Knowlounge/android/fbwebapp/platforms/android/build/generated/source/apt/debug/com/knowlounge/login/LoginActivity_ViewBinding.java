// Generated code from Butter Knife. Do not modify!
package com.knowlounge.login;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ViewFlipper;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.knowlounge.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class LoginActivity_ViewBinding implements Unbinder {
  private LoginActivity target;

  private View view2131755476;

  private View view2131755477;

  private View view2131755478;

  private View view2131755254;

  private View view2131755474;

  @UiThread
  public LoginActivity_ViewBinding(LoginActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public LoginActivity_ViewBinding(final LoginActivity target, View source) {
    this.target = target;

    View view;
    target.mainImgFlipper = Utils.findRequiredViewAsType(source, R.id.oauth_img_flipper, "field 'mainImgFlipper'", ViewFlipper.class);
    view = Utils.findRequiredView(source, R.id.direct_enter_room, "field 'directEnterRoom' and method 'OnClickEnterRoom'");
    target.directEnterRoom = Utils.castView(view, R.id.direct_enter_room, "field 'directEnterRoom'", TextView.class);
    view2131755476 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickEnterRoom();
      }
    });
    view = Utils.findRequiredView(source, R.id.oauthfacebook, "field 'facebookBtn' and method 'OnClickFacebookSignIn'");
    target.facebookBtn = Utils.castView(view, R.id.oauthfacebook, "field 'facebookBtn'", ImageView.class);
    view2131755477 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickFacebookSignIn();
      }
    });
    view = Utils.findRequiredView(source, R.id.oauthgoogle, "field 'googleBtn' and method 'OnClickGoogleSignIn'");
    target.googleBtn = Utils.castView(view, R.id.oauthgoogle, "field 'googleBtn'", ImageView.class);
    view2131755478 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickGoogleSignIn();
      }
    });
    view = Utils.findRequiredView(source, R.id.root_layout, "field 'rootLayout' and method 'OnClickRootLayout'");
    target.rootLayout = Utils.castView(view, R.id.root_layout, "field 'rootLayout'", RelativeLayout.class);
    view2131755254 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickRootLayout();
      }
    });
    target.oauthImgLayout1 = Utils.findRequiredViewAsType(source, R.id.oauth_img_layout1, "field 'oauthImgLayout1'", LinearLayout.class);
    target.oauthImgLayout2 = Utils.findRequiredViewAsType(source, R.id.oauth_img_layout2, "field 'oauthImgLayout2'", LinearLayout.class);
    target.oauthImgLayout3 = Utils.findRequiredViewAsType(source, R.id.oauth_img_layout3, "field 'oauthImgLayout3'", LinearLayout.class);
    target.page1 = Utils.findRequiredViewAsType(source, R.id.page1, "field 'page1'", ImageView.class);
    target.page2 = Utils.findRequiredViewAsType(source, R.id.page2, "field 'page2'", ImageView.class);
    target.page3 = Utils.findRequiredViewAsType(source, R.id.page3, "field 'page3'", ImageView.class);
    target.oauthGuestErrorMessage = Utils.findRequiredViewAsType(source, R.id.oauth_guest_error_message, "field 'oauthGuestErrorMessage'", TextView.class);
    view = Utils.findRequiredView(source, R.id.oauth_guest_room_code_clear, "field 'oauthGuestRoomCodeClear' and method 'OnClickRoomCodeClear'");
    target.oauthGuestRoomCodeClear = Utils.castView(view, R.id.oauth_guest_room_code_clear, "field 'oauthGuestRoomCodeClear'", ImageView.class);
    view2131755474 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickRoomCodeClear();
      }
    });
    target.inputGuestRoomCode = Utils.findRequiredViewAsType(source, R.id.input_guest_room_code, "field 'inputGuestRoomCode'", EditText.class);
    target.inputGuestName = Utils.findRequiredViewAsType(source, R.id.input_guest_name, "field 'inputGuestName'", EditText.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    LoginActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mainImgFlipper = null;
    target.directEnterRoom = null;
    target.facebookBtn = null;
    target.googleBtn = null;
    target.rootLayout = null;
    target.oauthImgLayout1 = null;
    target.oauthImgLayout2 = null;
    target.oauthImgLayout3 = null;
    target.page1 = null;
    target.page2 = null;
    target.page3 = null;
    target.oauthGuestErrorMessage = null;
    target.oauthGuestRoomCodeClear = null;
    target.inputGuestRoomCode = null;
    target.inputGuestName = null;

    view2131755476.setOnClickListener(null);
    view2131755476 = null;
    view2131755477.setOnClickListener(null);
    view2131755477 = null;
    view2131755478.setOnClickListener(null);
    view2131755478 = null;
    view2131755254.setOnClickListener(null);
    view2131755254 = null;
    view2131755474.setOnClickListener(null);
    view2131755474 = null;
  }
}
