// Generated code from Butter Knife. Do not modify!
package com.knowlounge.fragment;

import android.support.annotation.CallSuper;
import android.support.annotation.UiThread;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import butterknife.Unbinder;
import butterknife.internal.Utils;
import com.knowlounge.R;
import java.lang.IllegalStateException;
import java.lang.Override;

public class ChattingFragment_ViewBinding implements Unbinder {
  private ChattingFragment target;

  @UiThread
  public ChattingFragment_ViewBinding(ChattingFragment target, View source) {
    this.target = target;

    target.chatListView = Utils.findRequiredViewAsType(source, android.R.id.list, "field 'chatListView'", ListView.class);
    target.mRecyclerView = Utils.findRequiredViewAsType(source, R.id.chat_user_list, "field 'mRecyclerView'", RecyclerView.class);
    target.btnSendChat = Utils.findRequiredViewAsType(source, R.id.btn_send_chat, "field 'btnSendChat'", TextView.class);
    target.chatEditText = Utils.findRequiredViewAsType(source, R.id.input_chat, "field 'chatEditText'", EditText.class);
    target.chatUserSelector = Utils.findRequiredViewAsType(source, R.id.chat_user_selector, "field 'chatUserSelector'", ImageView.class);
    target.chatUserWhisper = Utils.findRequiredViewAsType(source, R.id.chat_user_whisper, "field 'chatUserWhisper'", ImageView.class);
  }

  @Override
  @CallSuper
  public void unbind() {
    ChattingFragment target = this.target;
    if (target == null) throw new IllegalStateException("Bindings already cleared.");
    this.target = null;

    target.chatListView = null;
    target.mRecyclerView = null;
    target.btnSendChat = null;
    target.chatEditText = null;
    target.chatUserSelector = null;
    target.chatUserWhisper = null;
  }
}
