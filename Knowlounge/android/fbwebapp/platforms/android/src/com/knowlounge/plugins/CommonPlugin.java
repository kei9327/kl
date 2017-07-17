package com.knowlounge.plugins;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import static com.knowlounge.RoomSwitchActivity.TAG;

/**
 * Created by Minsu on 2016-06-22.
 */
public class CommonPlugin extends CordovaPlugin {
    private CallbackContext callbackContext = null;
    //private IndexActivity activity = null;

    private static String ACTION_TOAST = "showToast";
    private static String ACTION_CONFIRM = "showConfirm";

    private final String ACTION_OPEN_CONFIRM_DIALOG = "openConfirmDialog";

    private final String ACTION_EXECUTE_IMAGE_CROP = "executeImageCrop";

    @Override
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        //this.activity = (IndexActivity) cordova.getActivity();

    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

        if (action.equals(ACTION_TOAST)) {
            this.showToast(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_CONFIRM)) {
            this.showConfirm(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_OPEN_CONFIRM_DIALOG)) {
            this.openConfirmDialog(callbackContext, args.getJSONObject(0));
        } else if (action.equals(ACTION_EXECUTE_IMAGE_CROP)) {
            this.executeImageCrop(callbackContext, args.getJSONObject(0));
        }
        return true;
    }


    /**
     * 토스트 메세지 띄우기
     * @param callbackContext
     * @param obj
     * @throws JSONException
     */
    private void showToast(CallbackContext callbackContext, JSONObject obj) throws JSONException {

        final String msg = obj.getString("msg");
        final String toastTime = obj.has("time") ? obj.getString("time") : "";
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RoomActivity.getContext(), msg, TextUtils.equals(toastTime, "LONG") ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show();
            }
        });
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void showConfirm(final CallbackContext callbackContext, JSONObject obj) throws JSONException {
        String msg = obj.getString("body_message");
        boolean isCancelable = obj.getBoolean("cancelable");

        Context ctx = RoomActivity.activity;
        AlertDialog.Builder builder = new AlertDialog.Builder(ctx, R.style.AlertDialogCustom);
        builder.setMessage(msg).setCancelable(isCancelable)
                .setPositiveButton(ctx.getResources().getString(R.string.global_ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int id) {
                        try {
                            JSONObject resultObj = new JSONObject();
                            resultObj.put("result", true);
                            Log.d(TAG, resultObj.toString());
                            callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, resultObj));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
        if(isCancelable) {
            builder.setNegativeButton(ctx.getResources().getString(R.string.global_cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        JSONObject resultObj = new JSONObject();
                        resultObj.put("result", false);
                        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, resultObj));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        AlertDialog confirm = RoomActivity.activity.createAlertDialog(builder);
        //AlertDialog confirm = builder.create();
        confirm.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        confirm.setCanceledOnTouchOutside(isCancelable);
        confirm.setTitle(ctx.getResources().getString(R.string.global_popup_title));
        confirm.show();

    }


    /**
     * @deprecated
     * Alert Dialog 띄우기
     */
    private void openConfirmDialog(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        Log.d(this.getClass().getSimpleName(), "openConfirmDialog");
        RoomActivity.activity.openConfirmDialog(obj);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
    }


    private void executeImageCrop(CallbackContext callbackContext, JSONObject obj) throws JSONException {
        String path = obj.getString("filepath");
        Log.d(TAG, "filepath : " + path);
        //Uri mImageCaptureUri = Uri.fromFile(new File(path));

        //Uri mImageCaptureUri = Uri.fromFile(new File(path));
        RoomActivity.activity.cropCapturedImage(path);
        callbackContext.sendPluginResult(new PluginResult(PluginResult.Status.OK, new JSONObject()));
//        Intent cropIntent = new Intent("com.android.camera.action.CROP");
//        cropIntent.setDataAndType(mImageCaptureUri, "image/*");
//        cropIntent.putExtra("output", mImageCaptureUri);
    }

}
