package com.knowlounge.fragment.dialog;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;


/**
 * Created by Minsu on 2016-04-01.
 */
public class RoomInfoDialogFragment extends DialogFragment {

    public interface OnRoomInfoListener {
        void setIsBookmark(boolean isEnable);
        boolean getIsBookmark();
        String getRoomTitle();
        void setRoomTitle(final String roomTitleParam);
        void shareRoom();
        void openInviteView();
        void importCanvas();
    }

    OnRoomInfoListener mCallback;
    WenotePreferenceManager prefManager;
    private boolean isGuest;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mCallback = (OnRoomInfoListener)context;
            prefManager = WenotePreferenceManager.getInstance(context);
            isGuest = RoomActivity.activity.getGuestFlag();
        } catch(ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.fragment_room_info_dialog, container);

        // remove dialog title
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // remove dialog background
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        final boolean isBookmark = mCallback.getIsBookmark();

        ((EditText)rootView.findViewById(R.id.dialog_room_title)).setText(mCallback.getRoomTitle());

        if(isBookmark) {
            ((ImageView)rootView.findViewById(R.id.dialog_bookmark_img)).setImageResource(R.drawable.btn_roomtitlemenu_bookmark_on);
        } else {
            ((ImageView)rootView.findViewById(R.id.dialog_bookmark_img)).setImageResource(R.drawable.btn_roomtitlemenu_bookmark);
        }

        final ImageView bookmarkImgView = (ImageView)rootView.findViewById(R.id.dialog_bookmark_img);
        rootView.findViewById(R.id.dialog_bookmark_layer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isGuest) {
                    Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.permission_deny_guest), Toast.LENGTH_SHORT).show();
                    return;
                }
                boolean bookmarkFlag = mCallback.getIsBookmark();
                mCallback.setIsBookmark(bookmarkFlag);
                if (bookmarkFlag) {
                    bookmarkImgView.setImageResource(R.drawable.btn_roomtitlemenu_bookmark);
                } else {
                    bookmarkImgView.setImageResource(R.drawable.btn_roomtitlemenu_bookmark_on);
                }
            }
        });

        rootView.findViewById(R.id.dialog_import_layer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isGuest) {
                    Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.permission_deny_guest), Toast.LENGTH_SHORT).show();
                    return;
                }
                mCallback.importCanvas();
            }
        });

        rootView.findViewById(R.id.dialog_share_layer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isGuest) {
                    Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.permission_deny_guest), Toast.LENGTH_SHORT).show();
                    return;
                }
                mCallback.shareRoom();
            }
        });

        rootView.findViewById(R.id.dialog_invite_layer).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isGuest) {
                    Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.permission_deny_guest), Toast.LENGTH_SHORT).show();
                    return;
                }
                mCallback.openInviteView();
                getDialog().dismiss();
            }
        });


        rootView.findViewById(R.id.dialog_btn_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newRoomTitle = ((EditText)rootView.findViewById(R.id.dialog_room_title)).getText().toString();
                if (TextUtils.isEmpty(newRoomTitle)) {
                    Toast.makeText(getContext(), getResources().getString(R.string.canvas_edittitle_hint), Toast.LENGTH_SHORT).show();
                    return;
                }
                if(!isGuest)
                    mCallback.setRoomTitle(newRoomTitle);
                getDialog().dismiss();
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(prefManager.getDeviceType() == GlobalConst.DEVICE_PHONE){
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.MATCH_PARENT);
        }
    }
}
