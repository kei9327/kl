package com.knowlounge.util.logger;

import android.os.Binder;
import android.os.Environment;
import android.util.Log;

import com.knowlounge.KnowloungeApplication;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

/**
 * Copyright 2016 Wescan. All Rights Reserved.
 * <p/>
 * Alo
 * <p/>
 * author: Jang-Hyeok Park
 * date: 16. 6. 22..
 */
public class LogFileWrapper {
    private static final String TAG = "LogFileWrapper";
    private static final int LOG_FILE_SIZE_LIMIT = 512*1024;
    private static final int LOG_FILE_MAX_COUNT = 2;
    private static final String LOG_FILE_NAME = "FileLog%g.txt";
    private static final SimpleDateFormat formatter =
            new SimpleDateFormat("MM-dd HH:mm:ss.SSS: ", Locale.getDefault());
    private static final Date date = new Date();
    private static Logger logger;
    private static FileHandler fileHandler;

    static {
        try {
            fileHandler = new FileHandler(Environment.getExternalStorageDirectory()
                    + File.separator +
                    LOG_FILE_NAME, LOG_FILE_SIZE_LIMIT, LOG_FILE_MAX_COUNT, true);

            fileHandler.setFormatter(new Formatter() {
                @Override
                public String format(LogRecord r) {
                    date.setTime(System.currentTimeMillis());

                    StringBuilder ret = new StringBuilder(80);
                    ret.append(formatter.format(date));
                    ret.append(r.getMessage());
                    return ret.toString();
                }
            });
            logger = Logger.getLogger(Logger.class.getName());
            logger.addHandler(fileHandler);
            logger.setLevel(Level.ALL);
            logger.setUseParentHandlers(true);
            Log.d(TAG, "init success");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void d(String tag, String message) {
        if (!KnowloungeApplication.DEBUG) return;

        if (logger != null) {
            logger.log(Level.INFO, String.format("D/%s(%d): %s\n",
                    tag, Binder.getCallingPid(), message));
        }

        Log.d(tag, message);
    }

    public static void d(String tag, String message, Throwable throwable) {
        if (!KnowloungeApplication.DEBUG) return;

        if (logger != null) {
            logger.log(Level.INFO, String.format("D/%s(%d): %s\n",
                    tag, Binder.getCallingPid(), message));
        }

        Log.d(tag, message, throwable);
    }


    public static void i(String tag, String message) {
        if (!KnowloungeApplication.DEBUG) return;

        if (logger != null) {
            logger.log(Level.INFO, String.format("I/%s(%d): %s\n",
                    tag, Binder.getCallingPid(), message));
        }

        Log.i(tag, message);
    }

    public static void i(String tag, String message, Throwable throwable) {
        if (!KnowloungeApplication.DEBUG) return;

        if (logger != null) {
            logger.log(Level.INFO, String.format("I/%s(%d): %s\n",
                    tag, Binder.getCallingPid(), message));
        }

        Log.i(tag, message, throwable);
    }

    public static void w(String tag, String message) {
        if (!KnowloungeApplication.DEBUG) return;

        if (logger != null) {
            logger.log(Level.INFO, String.format("W/%s(%d): %s\n",
                    tag, Binder.getCallingPid(), message));
        }

        Log.w(tag, message);
    }

    public static void w(String tag, String message, Throwable throwable) {
        if (!KnowloungeApplication.DEBUG) return;

        if (logger != null) {
            logger.log(Level.INFO, String.format("W/%s(%d): %s\n",
                    tag, Binder.getCallingPid(), message));
        }

        Log.w(tag, message, throwable);
    }

    public static void e(String tag, String message) {
        if (!KnowloungeApplication.DEBUG) return;

        if (logger != null) {
            logger.log(Level.INFO, String.format("E/%s(%d): %s\n",
                    tag, Binder.getCallingPid(), message));
        }

        Log.e(tag, message);
    }

    public static void e(String tag, String message, Throwable throwable) {
        if (!KnowloungeApplication.DEBUG) return;

        if (logger != null) {
            logger.log(Level.INFO, String.format("E/%s(%d): %s\n",
                    tag, Binder.getCallingPid(), message));
        }

        Log.e(tag, message, throwable);
    }

    public static void v(String tag, String message) {
        if (!KnowloungeApplication.DEBUG) return;

        if (logger != null) {
            logger.log(Level.INFO, String.format("V/%s(%d): %s\n",
                    tag, Binder.getCallingPid(), message));
        }

        Log.v(tag, message);
    }

    public static void v(String tag, String message, Throwable throwable) {
        if (!KnowloungeApplication.DEBUG) return;

        if (logger != null) {
            logger.log(Level.INFO, String.format("V/%s(%d): %s\n",
                    tag, Binder.getCallingPid(), message));
        }

        Log.v(tag, message, throwable);
    }
}
