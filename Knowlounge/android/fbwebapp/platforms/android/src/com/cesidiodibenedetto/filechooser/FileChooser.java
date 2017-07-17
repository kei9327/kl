package com.cesidiodibenedetto.filechooser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import com.ipaulpro.afilechooser.utils.FileUtils;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import android.util.Base64;

/**
 * FileChooser is a PhoneGap plugin that acts as polyfill for Android KitKat and web
 * applications that need support for <input type="file">
 * 
 */
public class FileChooser extends CordovaPlugin {

    private CallbackContext callbackContext = null;
    private static final String TAG = "FileChooser";
    private static final int REQUEST_CODE = 6666; // onActivityResult request code

    private String currentMode = "";

    private void showFileChooser(String mimeType) {
        // Use the GET_CONTENT intent from the utility class
        Intent target = FileUtils.createGetContentIntent(mimeType);
        // Create the chooser Intent
        Intent intent = Intent.createChooser(
                target, this.cordova.getActivity().getString(R.string.chooser_title));
        currentMode = mimeType;
        try {
            RoomActivity.activity.mWebViewFragment.startActivityForResult((CordovaPlugin) this, intent, REQUEST_CODE);
            //this.cordova.startActivityForResult((CordovaPlugin) this, intent, REQUEST_CODE);
        } catch (ActivityNotFoundException e) {
            // The reason for the existence of aFileChooser
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult / requestCode : " + requestCode + ", resultCode : " + resultCode);
        if( requestCode == REQUEST_CODE) {
                // If the file selection was successful
                if (resultCode == Activity.RESULT_OK) {
                    if (data != null) {

                        // Get the URI of the selected file
                        final Uri uri = data.getData();

                        Log.i(TAG, "Uri = " + uri.toString());
                        JSONObject obj = new JSONObject();

                        try {
                            // Get the file path from the URI
                            final String path = FileUtils.getPath(this.cordova.getActivity(), uri, currentMode);
                            byte[] fileContent = FileUtils.readFile(this.cordova.getActivity(), uri);
                            String base64Content = Base64.encodeToString(fileContent, Base64.DEFAULT);

                            obj.put("filepath", path);
                            obj.put("content", base64Content);
                            obj.put("mimetype", currentMode);
                            obj.put("filename", FileUtils.getFile(this.cordova.getActivity(), uri).getName());

                            this.callbackContext.success(obj);
                        } catch (Exception e) {
                            Log.e("FileChooser", "File select error", e);
                            this.callbackContext.error(e.getMessage());
                        } finally {
                            currentMode = "";
                        }
                    }
                }
        }
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) {
            this.callbackContext = callbackContext;
            if (action.equals("open")) {
                try {
                    JSONObject mimeTypeObj = args.getJSONObject(0);
                    String mimeType = mimeTypeObj.getString("mime_type");
                    showFileChooser(mimeType);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                return true;
            }
            else {
                return false;
            }
    }

}
