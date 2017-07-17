package com.knowlounge.network.restful.command;

import android.text.TextUtils;

import com.knowlounge.network.restful.RestApiFactory;
import com.knowlounge.network.restful.api.ApiCallFactory;
import com.knowlounge.network.restful.api.MainApiCallInterface;
import com.knowlounge.network.restful.api.ProfileApiCallInterface;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Minsu on 2016-09-09.
 */
public class MainApiCommand extends ApiCommand {

    private String mLocale = getLocale();
    private String mCredential;

    private String command;

    private final String CMD_GET_HOME_CLASS = "getHomeClassList";
    private final String CMD_GET_MY_CLASS = "getMyClassList";
    private final String CMD_GET_FRIEND_CLASS = "getFriendClassList";
    private final String CMD_GET_PUBLIC_CLASS = "getPublicClassList";
    private final String CMD_GET_SCHOOL_CLASS = "getSchoolClassList";


    private String friends;
    private String rows;


    @Override
    public Observable<?> buildApi() {
        RestApiFactory factory = RestApiFactory.getInstance();
        ApiCallFactory<MainApiCallInterface> mainApi = (ApiCallFactory<MainApiCallInterface>) factory.getApi(ApiCallFactory.API_TYPE_MAIN);
        if(command.equals(CMD_GET_HOME_CLASS)) {
            return mainApi.getApiInterface().getHomeClassList(mLocale, mCredential, friends, rows);
        } else if(command.equals(CMD_GET_MY_CLASS)) {
            return mainApi.getApiInterface().getMyClassList(mLocale, mCredential, rows);
        } else if(command.equals(CMD_GET_FRIEND_CLASS)) {
            return mainApi.getApiInterface().getFriendClassList(mLocale, mCredential, friends, rows);
        } else if(command.equals(CMD_GET_PUBLIC_CLASS)) {
            return mainApi.getApiInterface().getPublicClassList(mLocale, mCredential, rows);
        } else if(command.equals(CMD_GET_SCHOOL_CLASS)) {
            return mainApi.getApiInterface().getSchoolClassList(mLocale, mCredential, rows);
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


    public MainApiCommand command(String command) {
        if (TextUtils.isEmpty(command)) {
            throw new IllegalArgumentException("MainApiCommand command argument cannot be null or empty.");
        }
        this.command = command;
        return this;
    }


    public MainApiCommand credential(String credentialStr) {
        if (TextUtils.isEmpty(credentialStr)) {
            throw new IllegalArgumentException("MainApiCommand credential argument cannot be null or empty.");
        }

        // 암호화 대응..
//        AESUtil aesUtilObj = new AESUtil(AESUtil.KEY, AESUtil.VECTOR, AESUtil.CHARSET);
//        String encryptToken = aesUtilObj.encrypt(credentialStr);

        this.mCredential = credentialStr;
        return this;
    }


    public MainApiCommand friends(String friends) {
        this.friends = friends;
        return this;
    }

    public MainApiCommand rows(String rows) {
        this.rows = rows;
        return this;
    }
}
