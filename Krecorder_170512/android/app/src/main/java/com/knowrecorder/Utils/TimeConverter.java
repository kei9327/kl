package com.knowrecorder.Utils;

/**
 * Created by ssyou on 2016-03-28.
 */
public class TimeConverter {

    public static String convertMillisToStringFormat(long millis) {
        long s = (millis / 1000) % 60;
        long m = (millis / (1000 * 60)) % 60;
        long h = (millis / (1000 * 60 * 60 )) % 24;

        return h == 0 ? String.format("%02d:%02d", m, s) : String.format("%02d:%02d:%02d", h, m, s);
    }

    public static String convertSecondsToHMmSs(long millis) {
        int s = (int) (millis / 1000) % 60;
        int m = (int) (millis / (1000 * 60)) % 60;
        int h = (int) (millis / (1000 * 60 * 60)) % 24;
        return String.format("%02d:%02d:%02d", h, m, s);
    }

    public static String convertSecondsToMmSs(long millis) {
        long s = (millis / 1000) % 60;
        long m = (millis / (1000 * 60)) % 60;
        return String.format("%02d:%02d", m, s);
    }
}
