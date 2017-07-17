package com.knowrecorder.develop.fragment.Dialog;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.knowrecorder.R;
import com.knowrecorder.Utils.PixelUtil;
import com.knowrecorder.develop.controller.RealmPacketPutter;
import com.knowrecorder.develop.file.FilePath;
import com.knowrecorder.develop.manager.NoteManager;
import com.knowrecorder.develop.model.realm.Note;

import java.util.Arrays;

import io.realm.Realm;

/**
 * Created by we160303 on 2017-03-20.
 */

public class SettingDialog extends DialogFragment implements View.OnClickListener {

    private View rootView;
    private ImageView btnClose;
    private TextView facebookAccount;
    private EditText title;
    private String noteTitle;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        rootView = inflater.inflate(R.layout.rb_dialog_setting, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setBindViewId();
        setinfo();
    }

    private void setinfo() {
        Profile profile = Profile.getCurrentProfile();

        if(profile != null)
            facebookAccount.setText(profile.getName());

        Realm realm = Realm.getDefaultInstance();
        Note note = realm.where(Note.class).findFirst();
        noteTitle = note.getTitle();
        realm.close();

        title.setText(noteTitle);
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() == null)
            return;

        int dialogWidth = (int) PixelUtil.getInstance().convertDpToPixel(500);
        int dialogHeight = ViewPager.LayoutParams.WRAP_CONTENT;

        getDialog().getWindow().setLayout(dialogWidth, dialogHeight);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.btn_close :
                dismiss();
                break;
            case R.id.tv_facebook_account :
                AccessToken accessToken = AccessToken.getCurrentAccessToken();
                if(accessToken != null){
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setMessage(R.string.logout_confirm_msg);
                    builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            LoginManager.getInstance().logOut();
                            facebookAccount.setText(getString(R.string.login));
                        }
                    });
                    builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }else{
                    LoginManager.getInstance().logInWithReadPermissions(getActivity(), Arrays.asList("public_profile", "email"));
                }
                break;
        }
    }

    private void setBindViewId() {
        btnClose = (ImageView) rootView.findViewById(R.id.btn_close);
        facebookAccount = (TextView) rootView.findViewById(R.id.tv_facebook_account);
        title = (EditText) rootView.findViewById(R.id.setting_title);

        btnClose.setOnClickListener(this);
        facebookAccount.setOnClickListener(this);

    }

    public void fbSetText(String text){
        facebookAccount.setText(text);
    }


    @Override
    public void onDestroyView() {
        //생명주기상 onDestroy() 바로 이전
        super.onDestroyView();

        RealmPacketPutter.getInstance().UpdateNoteTitle(title.getText().toString());
        NoteManager.getInstance().updateTitleInRealm(FilePath.thisNoteName, title.getText().toString());

    }
}
