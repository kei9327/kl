package com.knowlounge.fragment;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.knowlounge.CircleTransformTemp;
import com.knowlounge.MainActivity;
import com.knowlounge.ProfileInitActivity;
import com.knowlounge.R;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.premium.PremiumWebViewActivity;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.CommonUtils;
import com.knowlounge.util.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONException;
import org.json.JSONObject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import cz.msebera.android.httpclient.Header;

public class MainLeftNavFragment extends Fragment implements WenotePreferenceManager.OnPreferenceChangeListener{
    private final String TAG = "MainLeftNavFragment";

    private final int HOME = 0;
    private final int MY_CLASS = 1;
    private final int FRIEND_CLASS = 2;
    private final int PUBLICK_CLASS = 3;
    private final int SCHOOL_CLASS = 4;

    private View rootView;

    private WenotePreferenceManager prefManager;

    @BindView(R.id.btn_mainleft_home) LinearLayout btnMainleftHome;
    @BindView(R.id.btn_mainleft_myclass) LinearLayout btnMainleftMyClass;
    @BindView(R.id.btn_mainleft_friendclass) LinearLayout btnMainleftFriendClass;
    @BindView(R.id.btn_mainleft_schoolclass) LinearLayout btnMainleftSchoolClass;
    @BindView(R.id.btn_mainleft_publicclass) LinearLayout btnMainleftPublicClass;
    @BindView(R.id.btn_mainleft_setting) LinearLayout btnMainleftSetting;
    @BindView(R.id.btn_mainleft_help) LinearLayout btnMainleftHelp;
    @BindView(R.id.btn_mainleft_starshop) LinearLayout btnMainleftStarshop;
    @BindView(R.id.btn_mainleft_myinfo) LinearLayout btnMainleftMyinfo;
    @BindView(R.id.btn_mainleft_logout) LinearLayout btnMainleftLogout;

    @BindView(R.id.btn_mainleft_premium) LinearLayout btnMainleftPremium;

    @BindView(R.id.btn_mainleft_setup) ImageView btnMainleftSetup;
    @BindView(R.id.mainleft_img) ImageView mainleftImg;
    @BindView(R.id.mainleft_login_type) ImageView mainleftLoginType;
    @BindView(R.id.mainleft_my_star_ico) ImageView mainleftMyStarIco;
    @BindView(R.id.btn_leftmenu_back) ImageView btnLeftmenuBack;

    @BindView(R.id.mainleft_my_name) TextView mainleftMyName;
    @BindView(R.id.mainleft_my_star) TextView mainleftMyStar;
    @BindView(R.id.mainleft_my_email) TextView mainleftMyEmail;
    @BindView(R.id.mainleft_my_star_charge) TextView mainleftMyStarCharge;

    LinearLayout mLastSelector;
    TextView mLastSelectorText;

    private boolean isProfileCliked = false;

    @Override
    public void onPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals("star_balance")) {
            updateStar();
        }
    }

    public interface OnMainLeftNavEvent {
        void onCurrentPage(int page);
        void onLogOut();

        void openAppConfig();
        void openHelpDesk();
    }
    private OnMainLeftNavEvent mCallback;
    private Unbinder unbinder;

    public static MainLeftNavFragment _instance;


    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach");
        super.onAttach(context);
        mCallback = (OnMainLeftNavEvent) context;
        prefManager = WenotePreferenceManager.getInstance(context);
        _instance = this;
    }


    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");

        rootView = inflater.inflate(R.layout.fragment_main_left_nav, container, false);
        ButterKnife.bind(this, rootView);

//        if (rootView == null) {
//            rootView = inflater.inflate(R.layout.fragment_main_left_nav, container, false);
//        }
//
//        if(unbinder == null) {
//            unbinder = ButterKnife.bind(this, rootView);
//        }


        setFindViewById();

        getStarBalance();

        return rootView;
    }


    @Override
    public void onResume() {
        Log.d(TAG, "onCreate");
        super.onResume();
        prefManager.registerOnPreferenceChangeListener(this);

        initSelector();
        //TODO 썸네일, 스타, 유저이름;
        mainleftMyName.setText(prefManager.getUserNm());
        mainleftMyEmail.setText(prefManager.getEmail());

        if (prefManager.getSnsType().equals("0"))
            mainleftLoginType.setImageResource(R.drawable.ico_mainleft_profilefacebook);
        else
            mainleftLoginType.setImageResource(R.drawable.ico_mainleft_profile_google);

        Glide.with(getActivity())
                .load(AndroidUtils.changeSizeThumbnail(prefManager.getUserThumbnail(), 200))
                .error(getResources().getDrawable(R.drawable.img_userlist_default01))
                .transform(new CircleTransformTemp(getActivity())).into(mainleftImg);
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView");
        prefManager.unregisterOnPreferenceChangeListener(this);
        super.onDestroyView();
//        if(unbinder != null)
//            unbinder.unbind();
    }

    public void updateUI() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mainleftMyName.setText(prefManager.getUserNm());
                mainleftMyEmail.setText(prefManager.getEmail());
            }
        });
    }

    private void setFindViewById() {
        //menu 관련

        btnLeftmenuBack.setOnClickListener(clickListener);
        btnMainleftHome.setOnClickListener(clickListener);
        btnMainleftMyClass.setOnClickListener(clickListener);
        btnMainleftFriendClass.setOnClickListener(clickListener);
        btnMainleftSchoolClass.setOnClickListener(clickListener);
        btnMainleftPublicClass.setOnClickListener(clickListener);
        btnMainleftSetting.setOnClickListener(clickListener);
        btnMainleftHelp.setOnClickListener(clickListener);
        btnMainleftStarshop.setOnClickListener(clickListener);
        btnMainleftMyinfo.setOnClickListener(clickListener);
        btnMainleftLogout.setOnClickListener(clickListener);

        mainleftMyStarIco.setOnClickListener(clickListener);
        mainleftMyStar.setOnClickListener(clickListener);

        mainleftImg.setOnClickListener(clickListener);
        mainleftMyName.setOnClickListener(clickListener);
        mainleftMyEmail.setOnClickListener(clickListener);

        mainleftMyStarCharge.setOnClickListener(clickListener);

        btnMainleftSetup.setOnClickListener(clickListener);

        btnMainleftPremium.setOnClickListener(clickListener);

        isProfileCliked = true;
    }



    View.OnClickListener clickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //if (!isProfileCliked) return;

            initSelector();

            switch (v.getId()) {
                case R.id.btn_leftmenu_back :
                    //mainleftMenuFinish();
                    MainActivity._instance.closeLeftNav();
                    break;

                case R.id.btn_mainleft_home :
                    mCallback.onCurrentPage(HOME);
                    setMenuSelected(v, ((TextView)v.findViewById(R.id.btn_mainleft_home_text)));
                    MainActivity._instance.closeLeftNav();
                    break;

                case R.id.btn_mainleft_myclass :
                    mCallback.onCurrentPage(MY_CLASS);
                    setMenuSelected(v, ((TextView)v.findViewById(R.id.btn_mainleft_myclass_text)));
                    MainActivity._instance.closeLeftNav();
                    break;

                case R.id.btn_mainleft_friendclass :
                    mCallback.onCurrentPage(FRIEND_CLASS);
                    setMenuSelected(v, ((TextView)v.findViewById(R.id.btn_mainleft_friendclass_text)));
                    MainActivity._instance.closeLeftNav();
                    break;

                case R.id.btn_mainleft_schoolclass :
                    mCallback.onCurrentPage(SCHOOL_CLASS);
                    setMenuSelected(v, ((TextView)v.findViewById(R.id.btn_mainleft_schoolclass_text)));
                    MainActivity._instance.closeLeftNav();
                    break;

                case R.id.btn_mainleft_publicclass :
                    mCallback.onCurrentPage(PUBLICK_CLASS);
                    setMenuSelected(v, ((TextView)v.findViewById(R.id.btn_mainleft_publicclass_text)));
                    MainActivity._instance.closeLeftNav();
                    break;

                case R.id.btn_mainleft_setting :
                    setMenuSelected(v, ((TextView)v.findViewById(R.id.btn_mainleft_setting_text)));
                case R.id.btn_mainleft_setup :
                    mCallback.openAppConfig();
                    break;

                case R.id.btn_mainleft_help :
                    setMenuSelected(v, ((TextView)v.findViewById(R.id.btn_mainleft_help_text)));
                    mCallback.openHelpDesk();
                    break;

                case R.id.btn_mainleft_starshop :
                    setMenuSelected(v, ((TextView)v.findViewById(R.id.btn_mainleft_starshop_text)));
                case R.id.mainleft_my_star :
                case R.id.mainleft_my_star_ico :
                case R.id.mainleft_my_star_charge :
                    MainActivity._instance.openSharShop();
                    break;

                case R.id.btn_mainleft_myinfo :
                    setMenuSelected(v, ((TextView)v.findViewById(R.id.btn_mainleft_myinfo_text)));
                case R.id.mainleft_img :
                case R.id.mainleft_my_name :
                    getProfileRestClient();
                    break;

                case R.id.btn_mainleft_logout :
                    alertDialogSignOut();
                    break;

                case R.id.btn_mainleft_premium :   // 프리미엄 테스트용.. - 2016.12.26 작성
//                    Bundle argument = new Bundle();
//                    argument.putString("url", "https://www.knowlounges.com");   // 초기 진입 URL 설정은 이곳에서..
                    Intent premiumIntent = new Intent(getContext(), PremiumWebViewActivity.class);
//                    premiumIntent.putExtras(argument);
                    startActivity(premiumIntent);
                    break;
            }
            isProfileCliked = false;
        }
    };


    private void alertDialogSignOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.AlertDialogCustom);
        builder.setMessage(getResources().getString(R.string.confirm_signout)).setCancelable(true)
                .setPositiveButton(getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isProfileCliked = true;
                        mCallback.onLogOut();
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        isProfileCliked = true;
                        initSelector();
                    }
                });
        AlertDialog confirm = builder.create();
        confirm.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        confirm.setCanceledOnTouchOutside(true);
        confirm.setTitle(getResources().getString(R.string.global_popup_title));

        confirm.show();
    }


    private void getProfileRestClient() {
        String masterCookie = prefManager.getUserCookie();
        String checksumCookie = prefManager.getChecksumCookie();

        RestClient.getWithCookie("/profile/getProfile.json", masterCookie, checksumCookie, new RequestParams(), new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                Log.d(TAG, "getProfile.json success / response : " + obj.toString());
                try {
                    obj = obj.getJSONObject("map");
                    String usertype = obj.getString("usertype");
                    if (usertype.equals("0")) {
                        Context ctx = getActivity();
                        Intent intent = new Intent(ctx, ProfileInitActivity.class);
                        startActivity(intent);
                    } else {
                        MainActivity._instance.openUserProfile();
                    }
                } catch (JSONException j) {
                    j.printStackTrace();
                }

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "send message onFailure");
                // TODO : 예외처리
            }
        });
    }

    private void updateStar(){
        int starCount = prefManager.getUserStarBalance();
        mainleftMyStar.setText(starCount + ""); // 내 보유 Star 데이터 업데이트
    }


    private void initSelector() {
        if (mLastSelector != null)
            mLastSelector.setBackgroundColor(Color.parseColor("#ffffff"));
        if(mLastSelectorText != null)
            mLastSelectorText.setBackgroundResource(R.drawable.bg_under_mainleft_item);
    }


    private void setMenuSelected(View v, View t) {
        v.setBackgroundResource(R.drawable.bg_main_left_nav_selector);
        t.setBackgroundColor(Color.parseColor("#00000000"));
        mLastSelector = (LinearLayout)v;
        mLastSelectorText = (TextView)t;
    }


    private void getStarBalance() {
        String url = "user/currency?userAccessToken=" + CommonUtils.urlEncode(prefManager.getSiAccessToken());
        RestClient.getSiPlatform(url, false, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    int starCount = response.getJSONObject("balance").getJSONObject("currency").getJSONObject("knowlounge").getInt("value");
                    int savedStarCount = prefManager.getUserStarBalance();
                    Log.d(TAG, "starCount : " + starCount);
                    if(starCount != savedStarCount) {
                        prefManager.setUserStarBalance(starCount);
                    }
                    mainleftMyStar.setText(prefManager.getUserStarBalance() + "");
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