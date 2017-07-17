package com.knowlounge.util;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.knowlounge.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Minsu on 2016-05-09.
 */
public class RuntimePermissionChecker {

    public interface CheckerWrapper {
        int checkSelfPermission(@NonNull String permission);
        boolean shouldShowRequestPermissionRationale(String permission);
        void requestPermissions(@NonNull String[] permissions, int requestCode);
        Resources getResources();
    }

    private CheckerWrapper mCheckerWrapper;


    public RuntimePermissionChecker(Fragment fragment) {
        mCheckerWrapper = new FragmentChecker(fragment);
    }

    public RuntimePermissionChecker(Activity activity) {
        mCheckerWrapper = new ActivityChecker(activity);
    }


    public boolean checkSelfPermission(String[] permissions) {
        return checkSelfPermission(permissions, null);
    }

    public boolean checkSelfPermission(String[] permissions, List<String> rationales) {
        if (mCheckerWrapper == null) {
            throw new IllegalStateException("RuntimePermissionChecker needs context field(Fragment or Activity).");
        }

        boolean granted = true;

        for (String permission : permissions) {
            boolean check = mCheckerWrapper.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
            Log.d("PermissionChecker", permission + " is " + check);
            granted &= check;
            boolean res = mCheckerWrapper.shouldShowRequestPermissionRationale(permission);
            if (rationales != null && !check && !res) {
                rationales.add(permission);
            }
        }

        return granted;
    }

    public List<String> getPermissionDisplayName(List<String> permissions) {
        if (mCheckerWrapper == null) {
            throw new IllegalStateException("RuntimePermissionChecker needs context field(Fragment or Activity).");
        }

        Resources resources = mCheckerWrapper.getResources();
        List<String> result = new ArrayList<>();

        for (String permission : permissions) {
            switch (permission) {
                case Manifest.permission.CAMERA:
                    result.add(resources.getString(R.string.permission_camera));
                    break;

                case Manifest.permission.RECORD_AUDIO:
                    result.add(resources.getString(R.string.permission_mic));
                    break;

                case Manifest.permission.READ_CONTACTS:
                    result.add(resources.getString(R.string.permission_contacts));
                    break;

                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    result.add(resources.getString(R.string.permission_storage));
                    break;
            }
        }

        return result;
    }

    public void requestPermissions(@NonNull String[] permissions, int requestCode) {
        mCheckerWrapper.requestPermissions(permissions, requestCode);
    }

    public static boolean verifyPermissions(int[] grantResults) {
        // At least one result must be checked.
        if(grantResults.length < 1){
            return false;
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (int result : grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }


    public static class FragmentChecker implements CheckerWrapper {
        Fragment mContext;

        public FragmentChecker(@NonNull Fragment fragment) {
            mContext = fragment;
        }

        public int checkSelfPermission(@NonNull String permission) {
            return ContextCompat.checkSelfPermission(mContext.getContext(), permission);
        }

        public boolean shouldShowRequestPermissionRationale(String permission) {
            return mContext.shouldShowRequestPermissionRationale(permission);
        }

        public void requestPermissions(@NonNull String[] permissions, int requestCode) {
            mContext.requestPermissions(permissions, requestCode);
        }

        public Resources getResources() {
            return mContext.getResources();
        }
    }

    public static class ActivityChecker implements CheckerWrapper {
        Activity mContext;

        public ActivityChecker(@NonNull Activity activity) {
            mContext = activity;
        }

        public int checkSelfPermission(@NonNull String permission) {
            return ActivityCompat.checkSelfPermission(mContext, permission);
        }

        public boolean shouldShowRequestPermissionRationale(String permission) {
            return ActivityCompat.shouldShowRequestPermissionRationale(mContext, permission);
        }

        public void requestPermissions(@NonNull String[] permissions, int requestCode) {
            ActivityCompat.requestPermissions(mContext, permissions, requestCode);
        }

        public Resources getResources() {
            return mContext.getResources();
        }
    }
}
