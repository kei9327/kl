package com.knowlounge.fragment.dialog;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.JsonObject;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.MainActivity;
import com.knowlounge.R;
import com.knowlounge.login.LoginActivity;
import com.knowlounge.model.RoomSpec;
import com.knowlounge.network.restful.zico.command.AuthRestCommand;
import com.knowlounge.util.NetworkUtils;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalCode;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.fragment.ConfigFragment;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.CommonUtils;
import com.knowlounge.util.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import butterknife.BindView;
import butterknife.ButterKnife;
import cz.msebera.android.httpclient.Header;
import retrofit2.adapter.rxjava.HttpException;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Minsu on 2016-05-30.
 */
public class ExtendReqDialogFragment extends DialogFragment implements StarPayNotiDialogFragment.SetOnStarPayDialogFragment {

    private static String TAG = "ExtendReqDialogFragment";
    private WenotePreferenceManager prefManager;
    private View rootView;
    private String type;


    @BindView(R.id.btn_req_extend) Button btn_req_extend;
    @BindView(R.id.btn_extend_release) LinearLayout btn_extend_release;
    @BindView(R.id.extend_request_content) TextView extend_request_content;
    @BindView(R.id.txt_my_star) TextView txt_my_star;

    private boolean isRoom;
    private boolean cancelBtnCliecked = false;
    private boolean okBtnClicked = false;

    private final String EXTEND_STAR_PRICE = "50";

    @Override
    public void dialogDismiss() {
        getDialog().dismiss();
    }

    public interface SetRoomNotiAdapterListener {
        void updateData();
    }

    public static SetRoomNotiAdapterListener mCallback;

    public static void setRoomNotiAdapterListener(SetRoomNotiAdapterListener listener) {mCallback = listener;}

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefManager = WenotePreferenceManager.getInstance(context);
        StarPayNotiDialogFragment.setStarPayNotiDialogFragment(this);
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null)
            return;
        getDialog().getWindow().setLayout((int)(290 * prefManager.getDensity()), ViewGroup.LayoutParams.WRAP_CONTENT);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        rootView = inflater.inflate(R.layout.fragment_extend_request_dialog, container, false);
        ButterKnife.bind(this, rootView);

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);  // remove dialog title
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));  // remove dialog background

        try {
            type = getArguments().getString("type") == null ? "" : getArguments().getString("type");
        } catch (NullPointerException np) {
            type = "";
        }

        final String masterno = getArguments().getString("masterno");
        final String roomid = getArguments().getString("roomid");
        final String roomCode = getArguments().getString("code");
        final String deviceId = getArguments().containsKey("deviceid") ? getArguments().getString("deviceid") : prefManager.getDeviceId();

        if(type.indexOf("room") > -1) {
            isRoom = true;
            btn_req_extend.setVisibility(View.GONE);
            extend_request_content.setText(getResources().getString(R.string.popup_extend_message));

            /*
            // 수업 확장 다이얼로그의 안내 문구 통합되었음 - 2016.11.23
            if(type.indexOf("multipage") > -1) {
                extend_request_content.setText(getResources().getString(R.string.canvas_popup_addpage));
            } else {
                extend_request_content.setText(getResources().getString(R.string.popup_extend_message));
            }*/
        } else {
            isRoom = false;
            btn_req_extend.setVisibility(View.VISIBLE);
            extend_request_content.setText(getResources().getString(R.string.popup_apply_message));
        }

        txt_my_star.setText(EXTEND_STAR_PRICE);

        // 참여 신청하기 버튼 이벤트..
        btn_req_extend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(cancelBtnCliecked)
                    return;

                cancelBtnCliecked = true;

                if(type.equals("room") || type.equals("room_setting")) {
                    getDialog().dismiss();
                }else if(type.equals("room_multipage")) {
                    getDialog().dismiss();
                }else {
                    requestExtendRoom(roomid);
                }
            }
        });

        // 수업 확장하기 버튼 이벤트..
        btn_extend_release.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (okBtnClicked) return;

                okBtnClicked = true;
                int myStar = prefManager.getUserStarBalance();
                Log.d(TAG, "star : " + myStar);
                if(myStar < 50) {
                    Bundle argument = new Bundle();
                    argument.putString("type", type);
                    if(isRoom)
                        argument.putString("master", "true");
                    else
                        argument.putString("master", "false");
                    FragmentManager fm = getFragmentManager();
                    StarPayNotiDialogFragment dialogFragment = new StarPayNotiDialogFragment();
                    dialogFragment.setArguments(argument);
                    dialogFragment.show(fm, "notStar");
                } else {
                    extendRoomUser(roomid, roomCode, deviceId);
                }
            }
        });
        return rootView;
    }


    /**
     * 참여요청
     * @param roomId
     */
    private void requestExtendRoom(String roomId) {
        String masterCookie = prefManager.getUserCookie();
        String checksumCookie = prefManager.getChecksumCookie();
        String url = "invite/join/req.json";

        RequestParams params = new RequestParams();
        params.put("roomid", roomId);

        RestClient.postWithCookie(url, masterCookie, checksumCookie, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int apiResult = response.getInt("result");
                    if (apiResult == 0) {
                        Toast.makeText(getContext(), getResources().getString(R.string.toast_apply_send), Toast.LENGTH_SHORT).show();
                        getDialog().dismiss();
                    } else {
                        Toast.makeText(getContext(), getResources().getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                        cancelBtnCliecked = false;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                    cancelBtnCliecked = false;
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "invite join req failed.." + statusCode);
                Toast.makeText(getContext(), getResources().getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * 수업 확장하기
     * @param roomId
     */
    private void extendRoomUser(final String roomId, final String roomCode, final String deviceId) {
        String masterCookie = prefManager.getUserCookie();
        String checksumCookie = prefManager.getChecksumCookie();
        String url = "room/extendRoomUser.json";

        RequestParams params = new RequestParams();
        params.put("roomid", roomId);

        RestClient.postWithCookie(url, masterCookie, checksumCookie, params, new JsonHttpResponseHandler(){
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int apiResult = response.getInt("result");
                    Log.d(TAG, "extendRoomUser.json result : " + response.toString());
                    if(apiResult == 0) {
                        Log.d(TAG, response.toString());
                        //getStarBalance();
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                getDialog().dismiss();
                                Log.d(TAG, "type : " + (type == null ? "" : type));
                                if (type != null && type.equals("room")) {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_remove), Toast.LENGTH_SHORT).show();
                                    if(mCallback != null) {
                                        mCallback.updateData();
                                    }
                                } else if (type != null && type.equals("room_setting")) {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_remove), Toast.LENGTH_SHORT).show();
                                    mCallback.updateData();
                                    ConfigFragment.setConfigParams("userlimitcnt", "30");
                                } else if (type.equals("room_multipage")) {
                                    //Toast.makeText(getActivity(), getResources().getString(R.string.toast_remove), Toast.LENGTH_SHORT).show();
                                    RoomActivity.activity.addMultiPageNoDialog();
                                } else {
                                    Toast.makeText(getActivity(), getResources().getString(R.string.toast_remove), Toast.LENGTH_SHORT).show();
                                    String roomUrl = KnowloungeApplication.CANVAS_HTML_NAME + "?code=" + roomCode;

//                                    Intent mainIntent = new Intent(getContext(), RoomActivity.class);
//                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    mainIntent.putExtra("roomurl", roomUrl);
//                                    mainIntent.putExtra("mode", GlobalConst.ENTER_ROOM_MODE);
//                                    getActivity().startActivityForResult(mainIntent, GlobalCode.CODE_ENTER_ROOM_WITH_ROOM_CODE);


                                    JsonObject extraParams = new JsonObject();
                                    extraParams.addProperty("type", "knowlounge");
                                    extraParams.addProperty("roomurl", roomUrl);
                                    extraParams.addProperty("deviceid", deviceId);
                                    extraParams.addProperty("mode", GlobalConst.ENTER_ROOM_MODE);
                                    navigateRoom(roomId, prefManager.getUserNo(), extraParams);
                                }
                            }
                        }, 500);
                    } else if(apiResult == 1) {
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_removed), Toast.LENGTH_SHORT).show();
                        okBtnClicked = false;
                    } else if(apiResult == -3000) {  // STAR_SERVER_CONNECTION_FAIL
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                        okBtnClicked = false;
                    } else if(apiResult == -3098) {   // 스타가 부족할 때
                        Bundle argument = new Bundle();
                        argument.putString("type", type);
                        if(isRoom)
                            argument.putString("master", "true");
                        else
                            argument.putString("master", "false");
                        FragmentManager fm = getFragmentManager();
                        StarPayNotiDialogFragment dialogFragment = new StarPayNotiDialogFragment();
                        dialogFragment.setArguments(argument);
                        dialogFragment.show(fm, "notStar");
                    } else if(apiResult == -8002) {  // DB_OPERATION_FAIL
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                        okBtnClicked = false;
                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                        okBtnClicked = false;
                    }
                } catch(JSONException e) {
                    e.printStackTrace();
                    okBtnClicked = false;
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                super.onFailure(statusCode, headers, responseString, throwable);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                super.onFailure(statusCode, headers, throwable, errorResponse);
            }
        });
    }


    private void navigateRoom(final String roomId, final String userNo, final JsonObject extraParams) {
        // Step 1. sns oauth token 인증 작업.
        String accessToken = prefManager.getZicoAccessToken();
        if ("".equals(accessToken)) {
            new AuthRestCommand()
                    .sns("guest")
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
                            Log.d(TAG, "<getAccessToken / onError / ZICO>");
                            // TODO  인증 실패시 에러 처리
                            String error = "";
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

                        }

                        @Override
                        public void onNext(JsonObject jsonObject) {
                            Log.d(TAG, "<getAccessToken / ZICO> getAccessToken.json result : " + jsonObject.toString());
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
                                params.addProperty("usernm", extraParams.get("guest").getAsString());
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
        } else {
            Log.d(TAG, "<ZICO> access token already exist..");
            JsonObject params = new JsonObject();
            params.addProperty("roomid", roomId);
            params.addProperty("userno", userNo);
            params.addProperty("usernm", prefManager.getUserNm());
            params.addProperty("name", "");
            params.addProperty("host", "");
            params.addProperty("port", "");
            params.addProperty("video", true);
            params.addProperty("audio", true);
            params.addProperty("volume", true);
            params.addProperty("token", accessToken);

            Intent intent = new Intent(getContext(), RoomActivity.class);
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

            startActivity(intent);
        }
    }


    private void getStarBalance() {
        String url = "user/currency?userAccessToken=" + CommonUtils.urlEncode(prefManager.getSiAccessToken());
        RestClient.getSiPlatform(url, false, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int starCount = response.getJSONObject("balance").getJSONObject("currency").getJSONObject("knowlounge").getInt("value");
                    int savedStarCount = prefManager.getUserStarBalance();
                    if (starCount != savedStarCount) {
                        prefManager.setUserStarBalance(starCount);
                    }
                    ((TextView)rootView.findViewById(R.id.txt_my_star)).setText(prefManager.getUserStarBalance() + "");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "SI platform getBalance onFailure - " + statusCode + ", " + responseString);
            }

        });
    }
}
