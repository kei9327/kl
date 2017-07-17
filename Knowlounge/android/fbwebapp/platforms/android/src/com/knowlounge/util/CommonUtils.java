package com.knowlounge.util;

import android.util.Patterns;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Minsu on 2015-12-28.
 */
public class CommonUtils {

    public static final float BASE_DPI = 160;

    public static float convertPixelWithDpi(float px, float density) {
        float dpi = px / (density / BASE_DPI);
        float convertPixel = dpi * (density / BASE_DPI);
        return convertPixel;
    }

    public static Map<String, Object> convertQueryStringToMap(String queryString) {
        Map<String, Object> result = new HashMap<String, Object>();
        String paramStr = queryString.split("\\?")[1];
        String[] paramArr = paramStr.split("&");
        for(String param : paramArr) {
            String[] keyValue = param.split("=");
            String key = keyValue[0];
            String value = keyValue[1];
            result.put(key, value);
        }
        return result;
    }

    /**
     * javascript의 encodeURIComponent와 동일한 방식으로 인코딩 시켜주는 메서드 (UTF-8)
     * @param originStr : 인코딩할 문자열 원문
     * @return
     */
    public static String encodeURIComponent(String originStr) {
        String encodedStr = "";
        try {
            encodedStr = URLEncoder.encode(originStr, "UTF-8")
                    .replaceAll("\\+", "%20")
                    .replaceAll("\\%21", "!")
                    .replaceAll("\\%27", "'")
                    .replaceAll("\\%28", "(")
                    .replaceAll("\\%29", ")")
                    .replaceAll("\\%7E", "~");

        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodedStr;
    }

    public static String replaceText(String txt) {
        String rtn = "";
        if (txt == null || "".equals(txt) || "null".equalsIgnoreCase(txt)) {
        } else {
            txt.replace("<", "&lt;");
            txt.replace(">", "&gt;");
            txt.replace("\"", "&#034;");
            txt.replaceAll("'", "&#039;");
            rtn = txt;
        }
        return rtn;
    }

    public static String urlEncode(String str) {
        if (str == null || str.length() == 0) return "";

        try {
            return URLEncoder.encode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return str;
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static String urlDecode(String str) {
        try {
            return URLDecoder.decode(str, "utf-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return str;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "";
    }

    public static boolean isValidEmailAddress(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

}
