package com.knowlounge.network.restful.command;

import android.text.TextUtils;

import com.knowlounge.network.restful.RestApiFactory;
import com.knowlounge.network.restful.api.ApiCallFactory;
import com.knowlounge.network.restful.api.ProfileApiCallInterface;

import okhttp3.MultipartBody;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by Minsu on 2016-08-29.
 */
public class ProfileApiCommand extends ApiCommand {

    private String mLocale = getLocale();
    private String mCredential;

    private String command;

    private final String CMD_GET_PROFILE = "getProfile";
    private final String CMD_GET_PROFILE_CODE_LIST = "getProfileCodeList";
    private final String CMD_GET_PROFILE_CODE_LIST_ALL = "getProfileCodeListAll";
    private final String CMD_SET_PROFILE = "setProfile";
    private final String CMD_UPDATE_PROFILE = "updateProfile";
    private final String CMD_UPDATE_PROFILE_IMG = "updateProfileImg";

    private String category;
    private String userType;
    private String userName;
    private String telNo;
    private String email;
    private String bio;
    private String school;
    private String grade;
    private String subject;
    private String language;

    private MultipartBody.Part uploadFile;


    @Override
    public Observable<?> buildApi() {
        RestApiFactory factory = RestApiFactory.getInstance();
        ApiCallFactory<ProfileApiCallInterface> profileApi = (ApiCallFactory<ProfileApiCallInterface>) factory.getApi(ApiCallFactory.API_TYPE_PROFILE);
        if(command.equals(CMD_GET_PROFILE)) {
            return profileApi.getApiInterface().getProfile(mLocale, mCredential);
        } else if(command.equals(CMD_GET_PROFILE_CODE_LIST)) {
            return profileApi.getApiInterface().getProfileCodeList(mLocale, mCredential, category, userType);
        } else if(command.equals(CMD_GET_PROFILE_CODE_LIST_ALL)) {
            return profileApi.getApiInterface().getProfileCodeListAll(mLocale, mCredential);
        } else if(command.equals(CMD_SET_PROFILE)) {
            return profileApi.getApiInterface().setProfile(mLocale, mCredential, userType, userName, telNo, email, bio, school, grade, subject, language);
        } else if(command.equals(CMD_UPDATE_PROFILE)) {
            return profileApi.getApiInterface().updateProfile(mLocale, mCredential, userName, telNo, email, bio, school, grade, subject, language);
        } else if(command.equals(CMD_UPDATE_PROFILE_IMG)) {
            return profileApi.getApiInterface().uploadProfileImg(mLocale, mCredential, uploadFile);
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

    public ProfileApiCommand command(String command) {
        if (TextUtils.isEmpty(command)) {
            throw new IllegalArgumentException("ProfileApiCommand command argument cannot be null or empty.");
        }
        this.command = command;
        return this;
    }

    public ProfileApiCommand credential(String credentialStr) {
        if (TextUtils.isEmpty(credentialStr)) {
            throw new IllegalArgumentException("ProfileApiCommand credential argument cannot be null or empty.");
        }

        // 암호화 대응..
//        AESUtil aesUtilObj = new AESUtil(AESUtil.KEY, AESUtil.VECTOR, AESUtil.CHARSET);
//        String encryptToken = aesUtilObj.encrypt(credentialStr);

        this.mCredential = credentialStr;
        return this;
    }

    public ProfileApiCommand category(String category) {
        this.category = category;
        return this;
    }

    public ProfileApiCommand userType(String userType) {
        this.userType = userType;
        return this;
    }

    public ProfileApiCommand userName(String userName) {
        this.userName = userName;
        return this;
    }


    public ProfileApiCommand telNo(String telNo) {
        this.telNo = telNo;
        return this;
    }

    public ProfileApiCommand email(String email) {
        this.email = email;
        return this;
    }

    public ProfileApiCommand bio(String bio) {
        this.bio = bio;
        return this;
    }

    public ProfileApiCommand school(String school) {
        this.school = school;
        return this;
    }


    public ProfileApiCommand grade(String grade) {
        this.grade = grade;
        return this;
    }


    public ProfileApiCommand subject(String subject) {
        this.subject = subject;
        return this;
    }


    public ProfileApiCommand language(String language) {
        this.language = language;
        return this;
    }



    public ProfileApiCommand upload(MultipartBody.Part uploadFile) {
        this.uploadFile = uploadFile;
        return this;
    }

}
