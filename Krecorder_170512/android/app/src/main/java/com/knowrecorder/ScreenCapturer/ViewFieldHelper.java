package com.knowrecorder.ScreenCapturer;

import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hazuki21 on 2017. 1. 3..
 */

public class ViewFieldHelper {
//    private final static String FIELD_NAME_WINDOW_MANAGER = "mWindowManager";
//    private final static String FIELD_NAME_GLOBAL = "mGlobal";
//    private final static String FIELD_NAME_ROOTS = "mRoots";
//    private final static String FIELD_NAME_PARAMS = "mParams";
//    private final static String FIELD_NAME_VIEW = "mView";

    public ViewFieldHelper() {
    }

//    public static List<RootViewInfo> getRootViews(Activity activity) {
//        List<RootViewInfo> rootViews = new ArrayList<>();
//
//        Object windowManager;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            windowManager = getFieldValue(FIELD_NAME_GLOBAL, activity.getWindowManager());
//        } else {
//            windowManager = getFieldValue(FIELD_NAME_WINDOW_MANAGER, activity.getWindowManager());
//        }
//
//        Object rootObjects = getFieldValue(FIELD_NAME_ROOTS, windowManager);
//        Object paramsObject = getFieldValue(FIELD_NAME_PARAMS, windowManager);
//
//        Object[] viewRoots;
//        WindowManager.LayoutParams[] params;
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            viewRoots = ((List) rootObjects).toArray();
//            List<WindowManager.LayoutParams> paramsList = (List<WindowManager.LayoutParams>) paramsObject;
//            params = paramsList.toArray(new WindowManager.LayoutParams[paramsList.size()]);
//        } else {
//            viewRoots = (Object[]) rootObjects;
//            params = (WindowManager.LayoutParams[]) paramsObject;
//        }
//
//        for (int i = 0; i < viewRoots.length; i++) {
//            View view = (View) getFieldValue(FIELD_NAME_VIEW, viewRoots[i]);
//
//            rootViews.add(new RootViewInfo(view, params[i]));
//        }
//
//        return rootViews;
//    }

    public static List<RootViewInfo> getViewChilds(View view) {
        List<RootViewInfo> rootViews = new ArrayList<>();

        if (view instanceof ViewGroup) {

            ViewGroup v = (ViewGroup) view;
            rootViews.add(new RootViewInfo(v, v.getLayoutParams()));
            for (int i = 0; i < v.getChildCount(); i++) {
                View child = v.getChildAt(i);

                rootViews.add(new RootViewInfo(child, child.getLayoutParams()));
            }

        } else {
            rootViews.add(new RootViewInfo(view, view.getLayoutParams()));
        }

        return rootViews;
    }

//    private static Object getFieldValue(String fieldName, Object target) {
//
//        try {
//            Field field = target.getClass().getDeclaredField(fieldName);
//            field.setAccessible(true);
//            return field.get(target);
//        } catch (Exception e) {
//            throw new RuntimeException(e);
//        }
//    }
}
