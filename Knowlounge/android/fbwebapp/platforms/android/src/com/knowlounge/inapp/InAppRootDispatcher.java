package com.knowlounge.inapp;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.widget.Toast;

import com.knowlounge.R;
import com.knowlounge.common.GlobalCode;
import com.knowlounge.billingutil.IabHelper;
import com.knowlounge.billingutil.IabResult;
import com.knowlounge.billingutil.Inventory;
import com.knowlounge.billingutil.Purchase;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.util.CommonUtils;
import com.knowlounge.util.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cz.msebera.android.httpclient.Header;

/**
 * Copyright 2016 Wescan. All Rights Reserved.
 * <p>
 * Alo 인앱 관련 처리를 위임받아 처리하는 Delegater
 * <p>
 *     처리 로직
 * 1. 아이템 구매 버튼 클릭
 * 2. DevelopPayload 요청(RestAPI)
 * 3. DevelopPayload Callback
 *      - Call launchPurchaseFlow() : 실제 구매 프로세스 구동 (IABHelper에서 이루어짐)
 * 4. onActivityResult()을 통해 launchPurchaseFlow()의 결과를 리턴 받음
 *      - 성공시 OnIabPurchaseFinishedListener를 통해 콜백을 받고, requestVerifyDevelopPayload 요청한 후, consumeAsync()처리하여 실제 InBox에서 삭제시켜주면 완료
 *      - 실패시 단순히 launchPurchaseFlow() 종료되는걸로 완료됨
 *      - 성공하였으나 네트워크 문제로 서버에 전달이 되지 않았을 때, Inbox(consume)가 남아 있으면, 이는 QueryInventoryFinishedListener에서 알려줌
 *
 * author: Jang-Hyeok Park
 * date: 16. 6. 10..
 */
public class InAppRootDispatcher {
    private final static String TAG = "InAppRootDispatcher";

    private FragmentActivity mActivity;
    private IabHelper mHelper;
    private InAppListener mListener;
    private WenotePreferenceManager prefManager;

    public interface InAppListener {
        void onFinished();
    }

    public InAppRootDispatcher(FragmentActivity activity) {
        this.mActivity = activity;
    }

    public void onCreate() {
        Log.d(TAG, "Starting billing.");
        mHelper = new IabHelper(mActivity, "");
        prefManager = new WenotePreferenceManager().getInstance(mActivity);
        boolean isDebug = false;
        // enable debug logging (for a production application, you should set this to false).
        //CacheManager.ISDEBUG
        if (isDebug) {
            mHelper.enableDebugLogging(true, "billing");
        } else {
            mHelper.enableDebugLogging(false);
        }

        // Start setup. This is asynchronous and the specified listener
        // will be called once setup completes.
        Log.d(TAG, "Starting billing startSetup.");
        // IabHelper 초기화..
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                Log.d(TAG, "In-App Billing startSetup finished..");
                try {
                    if (!result.isSuccess()) {
                        // Oh noes, there was a problem.
                        // complain("Problem setting up in-app billing: " + result);
                        Log.d(TAG, "Problem setting up in-app billing: " + result);

                        return;
                    }

                    // Have we been disposed of in the meantime? If so, quit.
                    if (mHelper == null) return;
                    // IAB is fully set up. Now, let's get an inventory of stuff we own.
                    Log.d(TAG, "billing startSetup successful. Querying inventory.");
                    mHelper.queryInventoryAsync(false, mGotInventoryListener);

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void onDestroy() {
        if(mHelper != null)
            mHelper.dispose();
        mHelper = null;
    }

    public void setListener(InAppListener listener) {
        mListener = listener;
    }

    public void start(String productId) {

        Log.d(TAG,"InApp Start");

        String asyncOperation = mHelper.getAsyncOperation();

        if(mHelper.getSetupDone()) {
            if(!mHelper.getAsyncInProgress()) {
//                pDialog = ProgressDialog.show(this, "", "로딩 중입니다. 잠시 기다려주세요.");
            } else {
//                pDialog.dismiss();
            }
            try {
                Log.d(TAG,"InApp Running...");
                mHelper.launchPurchaseFlow(mActivity, productId, GlobalCode.CODE_ACTION_BILLING, mPurchaseFinishedListener);
            } catch (NullPointerException e) {
                e.printStackTrace();
            } catch (IllegalStateException e) {
                e.printStackTrace();
                mHelper.handleActivityResult(GlobalCode.CODE_ACTION_BILLING, Activity.RESULT_OK, null);
            }
        } else {
            Toast.makeText(mActivity, mActivity.getString(R.string.error_pay_prpr_ing), Toast.LENGTH_SHORT).show();
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case GlobalCode.CODE_ACTION_BILLING :
                if (mHelper == null) return;
                // 구글 체크아웃을 통해 결제가 성공하면 바로 onActivityResult로 진입
                // 결과를 mHelper를 통해 처리합니다.
                if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
                    // 구매 요청시에 파라미터로 사용했었던 구매 완료 콜백이 호출
                    // 처리할 결과물이 아닐경우 이곳으로 빠져 기본처리를 하도록 합니다.
//                    super.onActivityResult(requestCode, resultCode, data);

                } else {
                    // exception
                }

                break;
        }
    }

    /*
     * Listeners for Billing
     *
     * - IabHelper.QueryInventoryFinishedListener
     *          // Listener that's called when we finish querying the items and subscriptions we own
     *
     * - IabHelper.OnIabPurchaseFinishedListener
     *          // Callback for when a purchase is finished
     *
     * - IabHelper.OnConsumeFinishedListener
     *         // Called when consumption is complete
     *
     * - IabHelper.OnConsumeMultiFinishedListener
     *         // Called when consumption is complete
     */
    // billing
    // Listener that's called when we finish querying the items and subscriptions we own
    IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            Log.d(TAG, "Query inventory finished.");
            // Have we been disposed of in the meantime? If so, quit.
            if (mHelper == null) return;
            // Is it a failure?
            if (result.isFailure()) {
                return;
            }

            ArrayList<Purchase> purchaseList = (ArrayList<Purchase>) inventory.getAllPurchases();
            if (purchaseList != null && purchaseList.size() > 0) {
                for (final Purchase purchase : purchaseList) {
                    if (purchase.getPurchaseState() == 0) {  // 0 : 구매 , 1 : 취소 , 2 : 환불
                        playStorePurchaseData(purchase);
                    }
                }
            } else {
                Log.d(TAG, "Query inventory not exist");
            }

//                mHelper.consumeAsync(purchaseList, mConsumeMultiFinishedListener);
//            } else if(purchaseList.size() == 1) {
//                mHelper.consumeAsync(purchaseList.get(0), mConsumeFinishedListener);
//            } else {
//                Log.d(TAG, "Query inventory not exist");
//            }
            Log.d(TAG, "Query inventory was successful ");

        }
    };

    // 구매가 완료되었을 때 호출되는 콜백
    public IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, final Purchase purchase) {
            Log.d(TAG, "Purchase finished: " + result + ", purchase: " + purchase);
            if (mHelper == null) return;
            if (result.isFailure()) {
                return;
            }
            if (result.getResponse() == IabHelper.BILLING_RESPONSE_RESULT_OK) {
                playStorePurchaseData(purchase);
            } else if (result.getResponse() == IabHelper.IABHELPER_USER_CANCELLED) {
            } else if (result.getResponse() == IabHelper.IABHELPER_UNKNOWN_PURCHASE_RESPONSE) {
            } else if (result.getResponse() == IabHelper.IABHELPER_UNKNOWN_ERROR) {
            } else {
            }
            Log.d(TAG, "Purchase successful.");
        }
    };

    // Consume이 끝나면 호출되는 콜백
    public IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            Log.d(TAG, "Consumption finished. Purchase: " + purchase + ", result: " + result);
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;
            if (result.isSuccess()) {
                Log.d(TAG, "Consumption successful. Provisioning.");
            } else {
            }
            Log.d(TAG, "End consumption flow.");
        }
    };
    public IabHelper.OnConsumeMultiFinishedListener mConsumeMultiFinishedListener = new IabHelper.OnConsumeMultiFinishedListener() {
        @Override
        public void onConsumeMultiFinished(List<Purchase> purchases, List<IabResult> results) {
            // if we were disposed of in the meantime, quit.
            if (mHelper == null) return;
        }
    };

    private void playStorePurchaseData(final Purchase purchase) {
        String purchaseData = purchase.getOriginalJson();
        String dataSignature = purchase.getSignature();
        final String url = "user/currency/playStorePurchaseData?" +
                "purchaseData=" + CommonUtils.urlEncode(purchaseData) + "&" +
                "dataSignature=" + CommonUtils.urlEncode(dataSignature) + "&" +
                "userAccessToken=" + prefManager.getSiAccessToken();

        RestClient.postSiPlatform(url, false, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d(TAG, "playStorePurchaseData response : " + response.toString());
                    int starBalanceCnt = response.getJSONObject("balance").getJSONObject("currency").getJSONObject("knowlounge").getInt("value");
                    prefManager.setUserStarBalance(starBalanceCnt);
                    mHelper.consumeAsync(purchase, mConsumeFinishedListener);

                    mListener.onFinished();

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "playStorePurchaseData failed.. : " + statusCode + ", " + responseString);
            }

        });
    }
}
