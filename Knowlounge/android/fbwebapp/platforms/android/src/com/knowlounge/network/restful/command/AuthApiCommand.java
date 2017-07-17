package com.knowlounge.network.restful.command;

import android.text.TextUtils;

import com.knowlounge.network.restful.RestApiFactory;
import com.knowlounge.network.restful.api.ApiCallFactory;
import com.knowlounge.network.restful.api.AuthApiCallInterface;
import com.knowlounge.network.restful.api.MainApiCallInterface;

import okhttp3.FormBody;
import okhttp3.RequestBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Minsu on 2016-08-29.
 */
public class AuthApiCommand extends ApiCommand {

    private String mLocale = getLocale();
    private String mCredential;

    private String command;

    private String token;
    private String deviceInfo;

    private RequestBody params;

    private final String CMD_RELOAD_AUTH = "reloadAuth";
    private final String CMD_GET_TOKEN = "getToken";
    private final String CMD_FACEBOOK_SIGN_IN = "facebookSignIn";
    private final String CMD_GOOGLE_SIGN_IN = "googleSignIn";

    @Override
    public Observable<?> buildApi() {
        RestApiFactory factory = RestApiFactory.getInstance();
        ApiCallFactory<AuthApiCallInterface> authApi = (ApiCallFactory<AuthApiCallInterface>) factory.getApi(ApiCallFactory.API_TYPE_AUTH);
        if(command.equals(CMD_RELOAD_AUTH)) {
            return authApi.getApiInterface().reloadAuth(mLocale, mCredential);
        } else if(command.equals(CMD_GET_TOKEN)) {
            return authApi.getApiInterface().getToken(mLocale, mCredential);
        } else if(command.equals(CMD_FACEBOOK_SIGN_IN)) {
            return authApi.getApiInterface().facebookSignIn(mLocale, params);
        } else if(command.equals(CMD_GOOGLE_SIGN_IN)) {
            return authApi.getApiInterface().googleSignIn(mLocale, params);
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

    public AuthApiCommand command(String command) {
        if (TextUtils.isEmpty(command)) {
            throw new IllegalArgumentException("AuthApiCommand command argument cannot be null or empty.");
        }
        this.command = command;
        return this;
    }


    public AuthApiCommand credential(String credentialStr) {
        if (TextUtils.isEmpty(credentialStr)) {
            throw new IllegalArgumentException("AuthApiCommand credential argument cannot be null or empty.");
        }

        // 암호화 대응..
//        AESUtil aesUtilObj = new AESUtil(AESUtil.KEY, AESUtil.VECTOR, AESUtil.CHARSET);
//        String encryptToken = aesUtilObj.encrypt(credentialStr);

        this.mCredential = credentialStr;
        return this;
    }

    public AuthApiCommand params(String token, String deviceInfo) {
        params = new FormBody.Builder()
                .add("token", token)
                .add("deviceinfo", deviceInfo)
                .build();
        return this;
    }
}
