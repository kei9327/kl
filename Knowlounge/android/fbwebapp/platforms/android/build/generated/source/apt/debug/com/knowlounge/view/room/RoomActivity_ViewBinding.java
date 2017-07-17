// Generated code from Butter Knife. Do not modify!
package com.knowlounge.view.room;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.DebouncingOnClickListener;
import butterknife.internal.Utils;
import com.knowlounge.R;
import com.knowlounge.customview.DragSelectableView;
import java.lang.IllegalStateException;
import java.lang.Override;

public class RoomActivity_ViewBinding implements Unbinder {
  private RoomActivity target;

  private View view2131755317;

  private View view2131755318;

  private View view2131755268;

  private View view2131755286;

  private View view2131755270;

  private View view2131755288;

  private View view2131755271;

  private View view2131755289;

  private View view2131755299;

  private View view2131755272;

  private View view2131755290;

  private View view2131755300;

  private View view2131755291;

  private View view2131755275;

  private View view2131755292;

  private View view2131755277;

  private View view2131755293;

  private View view2131755301;

  private View view2131755276;

  private View view2131755295;

  private View view2131755302;

  private View view2131755294;

  private View view2131755303;

  private View view2131755273;

  private View view2131755274;

  private View view2131755278;

  private View view2131755279;

  private View view2131755280;

  private View view2131755296;

  private View view2131755297;

  private View view2131755281;

  private View view2131755283;

  private View view2131755348;

  private View view2131755349;

  @UiThread
  public RoomActivity_ViewBinding(RoomActivity target) {
    this(target, target.getWindow().getDecorView());
  }

  @UiThread
  public RoomActivity_ViewBinding(final RoomActivity target, View source) {
    this.target = target;

    View view;
    target.mToolbar = Utils.findRequiredViewAsType(source, R.id.room_toolbar, "field 'mToolbar'", Toolbar.class);
    target.txtMoveRoom = Utils.findRequiredViewAsType(source, R.id.txt_move_room, "field 'txtMoveRoom'", TextView.class);
    target.icoMoveRoom = Utils.findRequiredViewAsType(source, R.id.ico_move_room, "field 'icoMoveRoom'", ImageView.class);
    target.tooltipAnswerPollGuide = Utils.findRequiredViewAsType(source, R.id.txt_answer_poll_guide, "field 'tooltipAnswerPollGuide'", TextView.class);
    target.txtAnswerPoll = Utils.findRequiredViewAsType(source, R.id.txt_poll, "field 'txtAnswerPoll'", TextView.class);
    target.mDocumentSubMenu = Utils.findRequiredViewAsType(source, R.id.layer_document_submenu, "field 'mDocumentSubMenu'", ScrollView.class);
    target.mPollSubMenu = Utils.findRequiredViewAsType(source, R.id.layer_poll_submenu, "field 'mPollSubMenu'", ScrollView.class);
    target.mTextSubMenu = Utils.findRequiredViewAsType(source, R.id.layer_text_submenu, "field 'mTextSubMenu'", ScrollView.class);
    target.selectableView = Utils.findRequiredViewAsType(source, R.id.draggable_draw_container, "field 'selectableView'", DragSelectableView.class);
    target.rightMenu = Utils.findRequiredViewAsType(source, R.id.right_menu_area_scroll, "field 'rightMenu'", ScrollView.class);
    view = Utils.findRequiredView(source, R.id.video_user_invite_btn, "field 'mVideoInviteBtn' and method 'OnClickVideoInviteBtn'");
    target.mVideoInviteBtn = Utils.castView(view, R.id.video_user_invite_btn, "field 'mVideoInviteBtn'", ImageView.class);
    view2131755317 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickVideoInviteBtn();
      }
    });
    view = Utils.findRequiredView(source, R.id.video_separate_btn, "field 'mVideoSeperateBtn' and method 'OnClickVideoSeparateBtn'");
    target.mVideoSeperateBtn = Utils.castView(view, R.id.video_separate_btn, "field 'mVideoSeperateBtn'", ImageView.class);
    view2131755318 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickVideoSeparateBtn();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_fold, "field 'mBtnMenuFold' and method 'OnClickBtnRightMenuFold'");
    target.mBtnMenuFold = Utils.castView(view, R.id.btn_rightmenu_fold, "field 'mBtnMenuFold'", ImageView.class);
    view2131755268 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuFold();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_fold_phone, "field 'mBtnMenuFoldPhone' and method 'OnClickBtnRightMenuFoldPhone'");
    target.mBtnMenuFoldPhone = Utils.castView(view, R.id.btn_rightmenu_fold_phone, "field 'mBtnMenuFoldPhone'", ImageView.class);
    view2131755286 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuFoldPhone();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_hand, "field 'mBtnMenuHand' and method 'OnClickBtnHand'");
    target.mBtnMenuHand = Utils.castView(view, R.id.btn_rightmenu_hand, "field 'mBtnMenuHand'", ImageView.class);
    view2131755270 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnHand();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_hand_phone, "field 'mBtnMenuHandPhone' and method 'OnClickBtnRightMenuHandPhone'");
    target.mBtnMenuHandPhone = Utils.castView(view, R.id.btn_rightmenu_hand_phone, "field 'mBtnMenuHandPhone'", ImageView.class);
    view2131755288 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuHandPhone();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_pen, "field 'mBtnMenuPen' and method 'OnClickBtnRightMenuPen'");
    target.mBtnMenuPen = Utils.castView(view, R.id.btn_rightmenu_pen, "field 'mBtnMenuPen'", ImageView.class);
    view2131755271 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuPen();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_pen_phone, "field 'mBtnMenuPenPhone' and method 'OnClickBtnRightMenuPenPhone'");
    target.mBtnMenuPenPhone = Utils.castView(view, R.id.btn_rightmenu_pen_phone, "field 'mBtnMenuPenPhone'", ImageView.class);
    view2131755289 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuPenPhone();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_pen_landscape, "field 'mBtnMenuPenLand' and method 'OnClickBtnRightMenuPenLand'");
    target.mBtnMenuPenLand = Utils.castView(view, R.id.btn_rightmenu_pen_landscape, "field 'mBtnMenuPenLand'", ImageView.class);
    view2131755299 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuPenLand();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_eraser, "field 'mBtnMenuEraser' and method 'OnClickBtnRightMenuEraser'");
    target.mBtnMenuEraser = Utils.castView(view, R.id.btn_rightmenu_eraser, "field 'mBtnMenuEraser'", ImageView.class);
    view2131755272 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuEraser();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_eraser_phone, "field 'mBtnMenuEraserPhone' and method 'OnClickBtnRightMenuEraserPhone'");
    target.mBtnMenuEraserPhone = Utils.castView(view, R.id.btn_rightmenu_eraser_phone, "field 'mBtnMenuEraserPhone'", ImageView.class);
    view2131755290 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuEraserPhone();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_eraser_landscape, "field 'mBtnMenuEraserLand' and method 'OnClickBtnRightMenuEraserLand'");
    target.mBtnMenuEraserLand = Utils.castView(view, R.id.btn_rightmenu_eraser_landscape, "field 'mBtnMenuEraserLand'", ImageView.class);
    view2131755300 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuEraserLand();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_laser_phone, "field 'mBtnMenuLaserPhone' and method 'OnClickBtnRightMenuLaserPhone'");
    target.mBtnMenuLaserPhone = Utils.castView(view, R.id.btn_rightmenu_laser_phone, "field 'mBtnMenuLaserPhone'", ImageView.class);
    view2131755291 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuLaserPhone();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_text, "field 'mBtnMenuText' and method 'OnClickBtnText'");
    target.mBtnMenuText = Utils.castView(view, R.id.btn_rightmenu_text, "field 'mBtnMenuText'", ImageView.class);
    view2131755275 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnText();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_text_phone, "field 'mBtnMenuTextPhone' and method 'OnClickBtnRightMenuTextPhone'");
    target.mBtnMenuTextPhone = Utils.castView(view, R.id.btn_rightmenu_text_phone, "field 'mBtnMenuTextPhone'", ImageView.class);
    view2131755292 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuTextPhone();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_document, "field 'mBtnMenuDocument' and method 'OnClickBtnDocument'");
    target.mBtnMenuDocument = Utils.castView(view, R.id.btn_rightmenu_document, "field 'mBtnMenuDocument'", ImageView.class);
    view2131755277 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnDocument();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_document_phone, "field 'mBtnMenuDocumentPhone' and method 'OnClickBtnDocumentPhone'");
    target.mBtnMenuDocumentPhone = Utils.castView(view, R.id.btn_rightmenu_document_phone, "field 'mBtnMenuDocumentPhone'", ImageView.class);
    view2131755293 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnDocumentPhone();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_document_landscape, "field 'mBtnMenuDocumentLand' and method 'OnClickBtnDocumentLand'");
    target.mBtnMenuDocumentLand = Utils.castView(view, R.id.btn_rightmenu_document_landscape, "field 'mBtnMenuDocumentLand'", ImageView.class);
    view2131755301 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnDocumentLand();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_memo, "field 'mBtnMemo' and method 'onClickBtnMemo'");
    target.mBtnMemo = Utils.castView(view, R.id.btn_rightmenu_memo, "field 'mBtnMemo'", ImageView.class);
    view2131755276 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickBtnMemo();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_poll_phone, "field 'mBtnMenuPollPhone' and method 'OnClickBtnRightMenuPollPhone'");
    target.mBtnMenuPollPhone = Utils.castView(view, R.id.btn_rightmenu_poll_phone, "field 'mBtnMenuPollPhone'", ImageView.class);
    view2131755295 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuPollPhone();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_poll_landscape, "field 'mBtnMenuPollLand' and method 'OnClickBtnRightMenuPollLand'");
    target.mBtnMenuPollLand = Utils.castView(view, R.id.btn_rightmenu_poll_landscape, "field 'mBtnMenuPollLand'", ImageView.class);
    view2131755302 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuPollLand();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_multipage_phone, "field 'mBtnMultipagePhone' and method 'OnClickBtnRightMenuMultipagePhone'");
    target.mBtnMultipagePhone = Utils.castView(view, R.id.btn_rightmenu_multipage_phone, "field 'mBtnMultipagePhone'", ImageView.class);
    view2131755294 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuMultipagePhone();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_multipage_landscape, "field 'mBtnMultipageLand' and method 'OnClickBtnRightMenuMultipageLand'");
    target.mBtnMultipageLand = Utils.castView(view, R.id.btn_rightmenu_multipage_landscape, "field 'mBtnMultipageLand'", ImageView.class);
    view2131755303 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuMultipageLand();
      }
    });
    target.mDrawerLayout = Utils.findRequiredViewAsType(source, R.id.right_drawer_room, "field 'mDrawerLayout'", DrawerLayout.class);
    target.mRoomContent = Utils.findRequiredViewAsType(source, R.id.main_layout, "field 'mRoomContent'", FrameLayout.class);
    target.mDrawerRightPanel = Utils.findRequiredViewAsType(source, R.id.right_drawer_panel_room, "field 'mDrawerRightPanel'", FrameLayout.class);
    target.mDrawerContent = Utils.findRequiredViewAsType(source, R.id.right_drawer_layout, "field 'mDrawerContent'", LinearLayout.class);
    target.mLeftDrawerContent = Utils.findRequiredViewAsType(source, R.id.left_drawer_layout, "field 'mLeftDrawerContent'", LinearLayout.class);
    target.mRightMenuArea = Utils.findRequiredViewAsType(source, R.id.rightmenu_area, "field 'mRightMenuArea'", LinearLayout.class);
    target.mPhoneRightMenuArea = Utils.findRequiredViewAsType(source, R.id.phone_rightmenu_area, "field 'mPhoneRightMenuArea'", LinearLayout.class);
    target.layoutBottom = Utils.findRequiredViewAsType(source, R.id.layout_move_room, "field 'layoutBottom'", LinearLayout.class);
    target.containerMoveRoom = Utils.findRequiredViewAsType(source, R.id.container_move_room, "field 'containerMoveRoom'", LinearLayout.class);
    target.containerAnswerPoll = Utils.findRequiredViewAsType(source, R.id.container_poll, "field 'containerAnswerPoll'", LinearLayout.class);
    target.serviceGuideLayout = Utils.findRequiredViewAsType(source, R.id.service_guide_layout, "field 'serviceGuideLayout'", FrameLayout.class);
    target.mRightMenuTablet = Utils.findRequiredViewAsType(source, R.id.right_menu_tablet, "field 'mRightMenuTablet'", LinearLayout.class);
    target.mRightMenuPhone = Utils.findRequiredViewAsType(source, R.id.right_menu_phone, "field 'mRightMenuPhone'", LinearLayout.class);
    target.mRightMenuLand = Utils.findRequiredViewAsType(source, R.id.right_menu_landscape, "field 'mRightMenuLand'", LinearLayout.class);
    target.mMultiPageLayoutPhone = Utils.findRequiredViewAsType(source, R.id.multi_page_sliding_layout_phone, "field 'mMultiPageLayoutPhone'", LinearLayout.class);
    target.mMultiPageLayout = Utils.findRequiredViewAsType(source, R.id.multi_page_sliding_layout, "field 'mMultiPageLayout'", LinearLayout.class);
    target.notificationLayout = Utils.findRequiredViewAsType(source, R.id.notification_container, "field 'notificationLayout'", FrameLayout.class);
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_shape, "method 'OnClickBtnShape'");
    view2131755273 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnShape();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_pointer, "method 'OnClickBtnRightMenuLaser'");
    view2131755274 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuLaser();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_redo, "method 'onClickBtnRedo'");
    view2131755278 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickBtnRedo();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_undo, "method 'onClickBtnUndo'");
    view2131755279 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickBtnUndo();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_trash, "method 'onClickBtnTrash'");
    view2131755280 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.onClickBtnTrash();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_undo_phone, "method 'OnClickBtnRightMenuUndoPhone'");
    view2131755296 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuUndoPhone();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_trash_phone, "method 'OnClickBtnRightMenuTrashPhone'");
    view2131755297 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnRightMenuTrashPhone();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_chat, "method 'OnClickBtnChat'");
    view2131755281 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnChat();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_rightmenu_comment, "method 'OnClickBtnComment'");
    view2131755283 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickBtnComment();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_cancel_capture, "method 'OnClickCaptureCancel'");
    view2131755348 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickCaptureCancel();
      }
    });
    view = Utils.findRequiredView(source, R.id.btn_confirm_capture, "method 'OnClickCaptureConfirm'");
    view2131755349 = view;
    view.setOnClickListener(new DebouncingOnClickListener() {
      @Override
      public void doClick(View p0) {
        target.OnClickCaptureConfirm();
      }
    });
  }

  @Override
  @CallSuper
  public void unbind() {
    RoomActivity target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.mToolbar = null;
    target.txtMoveRoom = null;
    target.icoMoveRoom = null;
    target.tooltipAnswerPollGuide = null;
    target.txtAnswerPoll = null;
    target.mDocumentSubMenu = null;
    target.mPollSubMenu = null;
    target.mTextSubMenu = null;
    target.selectableView = null;
    target.rightMenu = null;
    target.mVideoInviteBtn = null;
    target.mVideoSeperateBtn = null;
    target.mBtnMenuFold = null;
    target.mBtnMenuFoldPhone = null;
    target.mBtnMenuHand = null;
    target.mBtnMenuHandPhone = null;
    target.mBtnMenuPen = null;
    target.mBtnMenuPenPhone = null;
    target.mBtnMenuPenLand = null;
    target.mBtnMenuEraser = null;
    target.mBtnMenuEraserPhone = null;
    target.mBtnMenuEraserLand = null;
    target.mBtnMenuLaserPhone = null;
    target.mBtnMenuText = null;
    target.mBtnMenuTextPhone = null;
    target.mBtnMenuDocument = null;
    target.mBtnMenuDocumentPhone = null;
    target.mBtnMenuDocumentLand = null;
    target.mBtnMemo = null;
    target.mBtnMenuPollPhone = null;
    target.mBtnMenuPollLand = null;
    target.mBtnMultipagePhone = null;
    target.mBtnMultipageLand = null;
    target.mDrawerLayout = null;
    target.mRoomContent = null;
    target.mDrawerRightPanel = null;
    target.mDrawerContent = null;
    target.mLeftDrawerContent = null;
    target.mRightMenuArea = null;
    target.mPhoneRightMenuArea = null;
    target.layoutBottom = null;
    target.containerMoveRoom = null;
    target.containerAnswerPoll = null;
    target.serviceGuideLayout = null;
    target.mRightMenuTablet = null;
    target.mRightMenuPhone = null;
    target.mRightMenuLand = null;
    target.mMultiPageLayoutPhone = null;
    target.mMultiPageLayout = null;
    target.notificationLayout = null;

    view2131755317.setOnClickListener(null);
    view2131755317 = null;
    view2131755318.setOnClickListener(null);
    view2131755318 = null;
    view2131755268.setOnClickListener(null);
    view2131755268 = null;
    view2131755286.setOnClickListener(null);
    view2131755286 = null;
    view2131755270.setOnClickListener(null);
    view2131755270 = null;
    view2131755288.setOnClickListener(null);
    view2131755288 = null;
    view2131755271.setOnClickListener(null);
    view2131755271 = null;
    view2131755289.setOnClickListener(null);
    view2131755289 = null;
    view2131755299.setOnClickListener(null);
    view2131755299 = null;
    view2131755272.setOnClickListener(null);
    view2131755272 = null;
    view2131755290.setOnClickListener(null);
    view2131755290 = null;
    view2131755300.setOnClickListener(null);
    view2131755300 = null;
    view2131755291.setOnClickListener(null);
    view2131755291 = null;
    view2131755275.setOnClickListener(null);
    view2131755275 = null;
    view2131755292.setOnClickListener(null);
    view2131755292 = null;
    view2131755277.setOnClickListener(null);
    view2131755277 = null;
    view2131755293.setOnClickListener(null);
    view2131755293 = null;
    view2131755301.setOnClickListener(null);
    view2131755301 = null;
    view2131755276.setOnClickListener(null);
    view2131755276 = null;
    view2131755295.setOnClickListener(null);
    view2131755295 = null;
    view2131755302.setOnClickListener(null);
    view2131755302 = null;
    view2131755294.setOnClickListener(null);
    view2131755294 = null;
    view2131755303.setOnClickListener(null);
    view2131755303 = null;
    view2131755273.setOnClickListener(null);
    view2131755273 = null;
    view2131755274.setOnClickListener(null);
    view2131755274 = null;
    view2131755278.setOnClickListener(null);
    view2131755278 = null;
    view2131755279.setOnClickListener(null);
    view2131755279 = null;
    view2131755280.setOnClickListener(null);
    view2131755280 = null;
    view2131755296.setOnClickListener(null);
    view2131755296 = null;
    view2131755297.setOnClickListener(null);
    view2131755297 = null;
    view2131755281.setOnClickListener(null);
    view2131755281 = null;
    view2131755283.setOnClickListener(null);
    view2131755283 = null;
    view2131755348.setOnClickListener(null);
    view2131755348 = null;
    view2131755349.setOnClickListener(null);
    view2131755349 = null;
  }
}
