package com.knowlounge.fragment.dialog;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.knowlounge.KnowloungeApplication;
import com.knowlounge.MainActivity;
import com.knowlounge.R;
import com.knowlounge.common.GlobalCode;
import com.knowlounge.view.room.RoomActivity;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.AESUtil;
import com.knowlounge.util.RestClient;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * Created by Minsu on 2016-04-07.
 */
public class DirectEnterDialogFragment extends DialogFragment {

    private static String TAG = "DirectEnterDialog";
    private EditText dCodeEditText;
    private Button btnDirectEnter;
    private WenotePreferenceManager prefManager;
    private Activity activity;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_enter_room_direct, container);

        prefManager = WenotePreferenceManager.getInstance(getContext());

        // remove dialog title
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // remove dialog background
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        dCodeEditText = (EditText)rootView.findViewById(R.id.dialog_input_direct_code);
        btnDirectEnter = (Button)rootView.findViewById(R.id.btn_direct_enter_confirm);

        rootView.findViewById(R.id.btn_direct_enter_confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomCode = dCodeEditText.getText().toString();
                if (TextUtils.isEmpty(roomCode)) {
                    Toast.makeText(getContext(), getString(R.string.login_join_empty), Toast.LENGTH_SHORT).show();
                } else {

                    // TODO : 룸 입장전 권한 체크..
//                    getDialog().dismiss();
//                    String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;
//                    Intent mainIntent = new Intent(getContext(), RoomActivity.class);
//                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                    mainIntent.putExtra("roomurl", roomUrl);
//                    getContext().startActivity(mainIntent);

                    ((MainActivity) getActivity()).enterRoom(roomCode);
                    getDialog().dismiss();
                    //enterRoomWithDcode(roomCode);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() == null)
            return;
        getDialog().getWindow().setLayout((int)(290*prefManager.getDensity()), ViewGroup.LayoutParams.WRAP_CONTENT);
    }

    private void enterRoomWithDcode(final String roomCode) {
        try {
            String passwd = "";
            String tokenStr = "roomcode=" + roomCode + "&passwd=" + passwd;

            AESUtil aesUtilObj = new AESUtil(prefManager.getKEY(), prefManager.getVECTOR(), prefManager.getCHARSET());
            String encryptToken = aesUtilObj.encrypt(tokenStr);

            final RequestParams params = new RequestParams();
            params.put("token", encryptToken);

            String masterCookie = prefManager.getUserCookie();
            String checksumCookie = prefManager.getChecksumCookie();
            RestClient.postWithCookie("room/check.json", masterCookie, checksumCookie, params, new JsonHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                    try {
                        Log.d(TAG, response.toString());
                        int result = response.getInt("result");
                        if (result == 0) {
                            String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;
                            Intent mainIntent = new Intent(activity, RoomActivity.class);
                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            mainIntent.putExtra("roomurl", roomUrl);
                            activity.startActivityForResult(mainIntent, GlobalCode.CODE_ENTER_ROOM_WITH_ROOM_CODE);
                            getDialog().dismiss();
                        } else if (result == -201 | result == -8001) {
                            // Invalid room
                            Toast.makeText(getContext(), getResources().getString(R.string.login_join_invalidcode), Toast.LENGTH_SHORT).show();
                        } else if (result == -102) {
                            // Incorrect password
                            FragmentManager fm = getFragmentManager();
                            RoomPasswdDialogFragment dialogFragment = new RoomPasswdDialogFragment();
                            Bundle args = new Bundle();
                            args.putString("mode", "roomcode");
                            args.putString("roomcode", roomCode);
                            dialogFragment.setArguments(args);
                            dialogFragment.show(fm, "room_passwd");
                            getDialog().dismiss();
                        } else if (result == -207) {  // room count limit
                            String roomId = response.getJSONObject("map").getString("roomid");
                            Bundle args = new Bundle();
                            args.putString("roomid", roomId);
                            args.putString("code", roomCode);
                            args.putString("masterno", response.getJSONObject("map").getString("masterno"));

                            ExtendReqDialogFragment dialogFragment = new ExtendReqDialogFragment();
                            dialogFragment.setArguments(args);
                            dialogFragment.show(getFragmentManager(), "extend_user_limit");

//                            StarPayNotiDialogFragment dialogFragment = new StarPayNotiDialogFragment();
//                            dialogFragment.show(fm, "pay_star_noti");
                            getDialog().dismiss();

                        } else if (result == -208) {
                            Toast.makeText(getContext(), getResources().getString(R.string.global_popup_full), Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                    Log.d(TAG, "Create room onFailure " + statusCode);
                }
            });
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

}
