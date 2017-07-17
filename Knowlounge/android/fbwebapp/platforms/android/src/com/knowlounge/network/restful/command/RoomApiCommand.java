package com.knowlounge.network.restful.command;

import android.text.TextUtils;

import com.knowlounge.network.restful.RestApiFactory;
import com.knowlounge.network.restful.api.ApiCallFactory;
import com.knowlounge.network.restful.api.MainApiCallInterface;
import com.knowlounge.network.restful.api.RoomApiCallInterface;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Minsu on 2016-09-09.
 */
public class RoomApiCommand extends ApiCommand {

    private String mLocale = getLocale();
    private String mCredential;

    private String command;

    private final String CMD_GET_MY_CLASS = "getMyClassList";
    private final String CMD_GET_RECENT_CLASS = "getRecentClassList";
    private final String CMD_GET_BOOKMARK_CLASS = "getBookmarkClassList";
    private final String CMD_GET_FRIEND_CLASS = "getFriendClassList";
    private final String CMD_GET_SCHOOL_CLASS = "getSchoolClassList";
    private final String CMD_GET_PUBLIC_CLASS = "getPublicClassList";
    private final String CMD_REMOVE_ROOM = "removeRoom";

    private String idx;
    private String rows;
    private String flag;
    private String friends;
    private String roomId;
    private RequestBody parmas;

    @Override
    public Observable buildApi() {
        RestApiFactory factory = RestApiFactory.getInstance();
        ApiCallFactory<RoomApiCallInterface> roomApi = (ApiCallFactory<RoomApiCallInterface>) factory.getApi(ApiCallFactory.API_TYPE_ROOM);
        if(command.equals(CMD_GET_MY_CLASS)) {
            return roomApi.getApiInterface().getMyClassList(mLocale, mCredential, idx, rows, flag);
        } else if(command.equals(CMD_GET_RECENT_CLASS)) {
            return roomApi.getApiInterface().getRecentClassList(mLocale, mCredential, idx, rows, flag);
        } else if(command.equals(CMD_GET_BOOKMARK_CLASS)) {
            return roomApi.getApiInterface().getBookmarkClassList(mLocale, mCredential, idx, rows, flag);
        } else if(command.equals(CMD_GET_FRIEND_CLASS)) {
            return roomApi.getApiInterface().getFriendClassList(mLocale, mCredential, friends, idx, rows, flag);
        } else if(command.equals(CMD_GET_SCHOOL_CLASS)) {
            return roomApi.getApiInterface().getSchoolClassList(mLocale, mCredential, idx, rows, flag);
        } else if(command.equals(CMD_GET_PUBLIC_CLASS)) {
            return roomApi.getApiInterface().getPublicClassList(mLocale, mCredential, idx, rows, flag);
        } else if(command.equals(CMD_REMOVE_ROOM)) {
            return roomApi.getApiInterface().removeRoom(mLocale, mCredential, parmas);
        } else {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public void execute() {
        Observable<?> call = buildApi();

        if(mApiCallEvent != null) {
            mApiCallEvent.onApiCall(this, call);
        } else {
            call.subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe();
        }
    }


    public RoomApiCommand command(String command) {
        if (TextUtils.isEmpty(command)) {
            throw new IllegalArgumentException("RoomApiCommand command argument cannot be null or empty.");
        }
        this.command = command;
        return this;
    }


    public RoomApiCommand credential(String credentialStr) {
        if (TextUtils.isEmpty(credentialStr)) {
            throw new IllegalArgumentException("RoomApiCommand credential argument cannot be null or empty.");
        }

        // 암호화 대응..
//        AESUtil aesUtilObj = new AESUtil(AESUtil.KEY, AESUtil.VECTOR, AESUtil.CHARSET);
//        String encryptToken = aesUtilObj.encrypt(credentialStr);

        this.mCredential = credentialStr;
        return this;
    }


    public RoomApiCommand friends(String friends) {
        this.friends = friends;
        return this;
    }


    public RoomApiCommand idx(String idx) {
        this.idx = idx;
        return this;
    }


    public RoomApiCommand rows(String rows) {
        this.rows = rows;
        return this;
    }


    public RoomApiCommand flag(String flag) {
        this.flag = flag;
        return this;
    }

    public RoomApiCommand roomId(String roomId) {
        RequestBody params = new FormBody.Builder().add("roomid", roomId).build();
        this.parmas = params;
        return this;
    }
}
