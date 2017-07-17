package com.knowlounge.fragment.dialog;

import android.content.Context;
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

import com.google.gson.JsonObject;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.model.RoomSpec;
import com.knowlounge.network.restful.zico.command.AuthRestCommand;
import com.knowlounge.util.NetworkUtils;
import com.knowlounge.view.room.RoomActivity;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.AESUtil;
import com.knowlounge.util.RestClient;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Minsu on 2016-04-08.
 */
public class RoomPasswdDialogFragment extends DialogFragment implements StarPayNotiDialogFragment.SetOnStarPayDialogFragment {

        private static String TAG = "PasswdDialogFragment";
        private EditText passwdEditText;
        private Button btnConfirmPasswd;
        private WenotePreferenceManager prefManager;

        private String mode;
        private String roomId;
        private String roomCode;
        private String roomParam;
        private String guestNm = "";
        private String deviceId;

        private Context mContext;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        StarPayNotiDialogFragment.setStarPayNotiDialogFragment(this);
        mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_room_passwd_dialog, container);
        ButterKnife.bind(this, rootView);

        prefManager = WenotePreferenceManager.getInstance(getContext());

        deviceId = getArguments().getString("deviceid") != null ? getArguments().getString("deviceid") : "";
        final String mode = getArguments().getString("mode");
        if(mode.equals("roomcode")) {
            roomCode = getArguments().getString("roomcode");
            guestNm = getArguments().getString("guestnm") != null ? getArguments().getString("guestnm") : "";
            roomParam = roomCode;
        } else if(mode.equals("roomid")) {
            roomId = getArguments().getString("roomid");
            roomCode = getArguments().getString("roomcode");
            guestNm = getArguments().getString("guestnm") != null ? getArguments().getString("guestnm") : "";
            roomParam = roomId;
        }

        // remove dialog title
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // remove dialog background
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        passwdEditText = (EditText)rootView.findViewById(R.id.dialog_input_room_passwd);
        btnConfirmPasswd = (Button)rootView.findViewById(R.id.btn_room_passwd_confirm);

        btnConfirmPasswd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String roomPasswd = passwdEditText.getText().toString();
                if (TextUtils.isEmpty(roomPasswd)) {
                    Toast.makeText(getContext(), "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
                } else {
                    // TODO : 룸 입장전 권한 체크..
                    enterRoomWithDcode(mode, roomParam, roomPasswd);
                }
            }
        });

        return rootView;
    }


    @Override
    public void dialogDismiss() {
        if (getDialog() != null)
            getDialog().dismiss();
    }


    private void enterRoomWithDcode(String mode, final String roomParam, String roomPasswd) {
        try {
            String tokenStr = mode.equals("roomcode") ? "roomcode=" + roomParam + "&passwd=" + roomPasswd
                    : mode.equals("roomid") ? "roomid=" + roomParam + "&passwd=" + roomPasswd : "" ;

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
                            getDialog().dismiss();
                            String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;

//                            Intent mainIntent = new Intent(getContext(), RoomActivity.class);
//                            mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                            mainIntent.putExtra("roomurl", roomUrl);
//                            if(!TextUtils.isEmpty(guestNm)) {
//                                mainIntent.putExtra("guest", guestNm);
//                            }
//                            mainIntent.putExtra("deviceid", deviceId);
//
//                            getContext().startActivity(mainIntent);

                            JsonObject extraParams = new JsonObject();
                            extraParams.addProperty("type", "knowlounge");
                            extraParams.addProperty("roomurl", roomUrl);
                            extraParams.addProperty("deviceid", deviceId);
                            extraParams.addProperty("mode", GlobalConst.ENTER_ROOM_MODE);
                            if(!TextUtils.isEmpty(guestNm))
                                extraParams.addProperty("guest", guestNm);

                            navigateRoomWithGuest(roomId, deviceId, extraParams);


                        } else if (result == -201) {
                            // Invalid room
                            Toast.makeText(getContext(), "존재하지 않는 Live 입니다.", Toast.LENGTH_SHORT).show();
                        } else if (result == -102) {
                            // Incorrect password
                            Toast.makeText(getContext(), "비밀번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                        } else if (result == -207) {
                            // room count limit
                            getDialog().dismiss();
                            FragmentManager fm = getFragmentManager();

                            if (prefManager.getUserStarBalance() >= 50) {
                                String roomId = response.getJSONObject("map").getString("roomid");
                                Bundle args = new Bundle();
                                args.putString("roomid", roomId);
                                args.putString("code", roomCode);
                                args.putString("masterno", response.getJSONObject("map").getString("masterno"));

                                ExtendReqDialogFragment dialogFragment = new ExtendReqDialogFragment();
                                dialogFragment.setArguments(args);
                                dialogFragment.show(fm, "extend_user_limit");
                            } else {
                                StarPayNotiDialogFragment dialogFragment = new StarPayNotiDialogFragment();
                                dialogFragment.show(fm, "pay_star_noti");
                            }
                        } else if (result == -208) {
                            Toast.makeText(getContext(), getResources().getString(R.string.global_popup_full), Toast.LENGTH_SHORT).show();
                        } else if (result == -8001) {
                            Toast.makeText(getContext(), "수업 번호가 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
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

    private void navigateRoomWithGuest(final String roomId, final String userNo, final JsonObject extraParams) {
        // Step 1. sns oauth token 인증 작업.
        new AuthRestCommand()
                .sns(extraParams.has("guest") ? "guest" : TextUtils.equals(prefManager.getSnsType(), "0") ? "fb" : "gl")
                .token("")
                .service("knowlounge")
                .ip(NetworkUtils.getIpAddress())
                .buildApi()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<JsonObject>() {

                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted");

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError");
                        // TODO  인증 실패시 에러 처리
                    /*
                    if (e instanceof HttpException) {
                        HttpException response = (HttpException) e;
                        int code = response.code();

                        if (code >= 200 && code < 300) {
                            // success
                        } else if (code == 401) {
                            // unauthenticated
                        } else if (code >= 400 && code < 500) {
                            // client error
                        } else if (code >= 500 && code < 600) {
                            // server error
                        } else {
                            // unexpected error
                        }

                        error += ": code " + code + " response : " + response.toString();
                    } else if (e instanceof IOException) {
                        // network error
                    } else {
                        // unexpected error
                        error += ": code " + e.toString();
                    }
                    */
                    }

                    @Override
                    public void onNext(JsonObject jsonObject) {
                        Log.d(TAG, "<getAccessToken / ZICO> result : " + jsonObject.toString());
                        int resultCode = jsonObject.get("result").getAsInt();
                        if (resultCode == 0) {
                            JsonObject data = jsonObject.get("accessToken").getAsJsonObject();
                            String zicoAccessToken = data.get("token").getAsString();
                            prefManager.setZicoAccessToken(zicoAccessToken);

                            int ttl = data.get("ttl").getAsInt();

                            /**
                             * TODO 차후에 룸넘버 가져오는 루틴 정리할 것.
                             */
//                            fetchRtcServer(roomNumberEdit.getText().toString());

                            JsonObject params = new JsonObject();
                            params.addProperty("roomid", roomId);
                            params.addProperty("userno", userNo);
                            params.addProperty("usernm", extraParams.has("guest") ? extraParams.get("guest").getAsString() : prefManager.getUserNm());
                            params.addProperty("name", "");
                            params.addProperty("host", "");
                            params.addProperty("port", "");
                            params.addProperty("video", true);
                            params.addProperty("audio", true);
                            params.addProperty("volume", true);
                            params.addProperty("token", zicoAccessToken);

                            Intent intent = new Intent(getContext(), RoomActivity.class);

                            // 서브 룸의 룸아이디가 올 경우, 처리 루틴..
                            String roomIdParam = params.get("roomid").getAsString();
                            int specialCharPosition = roomIdParam.indexOf("_");
                            String roomId = specialCharPosition < 0 ? roomIdParam : roomIdParam.substring(0, specialCharPosition);

                            intent.putExtra("arguments",
                                    new RoomSpec.Builder()
                                            .host(params.get("host").getAsString())
                                            .name(params.get("name").getAsString())
                                            .port(params.get("port").getAsString())
                                            .userNo(params.get("userno").getAsString())
                                            .userNm(params.get("usernm").getAsString())
                                            .roomId(roomId)
                                            .accessToken(params.get("token").getAsString())
                                            .enableVideo(params.get("video").getAsBoolean())
                                            .enableAudio(params.get("audio").getAsBoolean())
                                            .enableVolume(params.get("volume").getAsBoolean())
                                            .build());
                            intent.putExtra("type", extraParams.get("type").getAsString());
                            intent.putExtra("roomurl", extraParams.get("roomurl").getAsString());
                            intent.putExtra("deviceid", extraParams.get("deviceid").getAsString());
                            intent.putExtra("mode", extraParams.get("mode").getAsInt());
                            intent.putExtra("guest", extraParams.get("guest").getAsString());

                            startActivity(intent);

                        } else if (resultCode == -9001) {
                            Log.d(TAG, "<ZICO> Invalid access token..");


                        }
                    }
                });
    }

}
