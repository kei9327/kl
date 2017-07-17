package com.knowlounge.util.logger;

/**
 * Copyright 2016 Wescan. All Rights Reserved.
 *
 *  Alo
 *
 * author: Jun-hyoung Lee
 * date: 2016-01-22.
 */
public final class AppLog {

    public static final String TAG = "[Knowlounge]";
    public static final String AUTH_TAG = "[AUTH]";
    public static final String BLUR_TAG = "[BLUR]";
    public static final String GCM_TAG = "[GCM]";
    public static final String PARAMS_TAG = "[PARAMS]";
    public static final String INAPP_TAG = "[INAPP]";
    public static final String CLASSLIST_TAG = "[CLASSLIST]";

    public static boolean isValid(String tag) {
        return tag.equals(TAG) ||
                tag.equals(AUTH_TAG) ||
                tag.equals(BLUR_TAG) ||
                tag.equals(PARAMS_TAG) ||
                tag.equals(INAPP_TAG) ||
                tag.equals(CLASSLIST_TAG) ||
                tag.equals(GCM_TAG)
                ;
    }

    public static void i(String tag, String message) {
        if (isValid(tag)) {
            LogFileWrapper.i(tag, message);
        }
    }

    public static void i(String tag, String message, Throwable throwable) {
        if (isValid(tag)) {
            LogFileWrapper.i(tag, message, throwable);
        }
    }

    public static void w(String tag, String message) {
        if (isValid(tag)) {
            LogFileWrapper.w(tag, message);
        }
    }

    public static void w(String tag, String message, Throwable throwable) {
        if (isValid(tag)) {
            LogFileWrapper.w(tag, message, throwable);
        }
    }

    public static void d(String tag, String message) {
        if (isValid(tag)) {
            LogFileWrapper.d(tag, message);
        }
    }

    public static void d(String tag, String message, Throwable throwable) {
        if (isValid(tag)) {
            LogFileWrapper.d(tag, message, throwable);
        }
    }

    public static void e(String tag, String message) {
        if (isValid(tag)) {
            LogFileWrapper.e(tag, message);
        }
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (isValid(tag)) {
            LogFileWrapper.e(tag, message, throwable);
        }
    }
}
