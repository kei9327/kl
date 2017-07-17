package com.nhn.nhn_test;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;


//
//          HttpURLConnection, HttpsURLConnection 기반으로 구현한다.
//        • http, https 방식이든 모두 호출될 수 있도록 작성한다.
//        • 요청메소드타입은 GET, POST, PUT, DELETE 를 지원하도록 한다.
//        • Request Header 값을 세팅할 수 있어야 한다.
//        • Response Data 를 어떤 형식의 타입으로도 요청할 수 있어야 한다.
//        • Request Body 전달시 어떤 형식으로든 서버에 전달, 요청할 수 있어야 한다.
//        • API 접속 timeout 시간을 세팅할 수 있도록 작성한다.
//        • jar 로 패키징될 수 있도록 gradle 빌드 스크립트도 작성한다.
//        • 위의 요구사항에 대해서 테스트 케이스를 작성한다.

public class nhnConnect {

    public static class myConnect extends AsyncTask<String, Void, Object>
    {
        String m_strRequest = "GET";
        String m_strRequestHeader = "https://apis.daum.net/search/book";
        String m_strDaumApiKey = "7e4c1b1b4a853ebe56b51145ca666434";
        String m_strSearch = "위인";
        String m_strPage = "1";
        String m_strResultNum = "20";
        String m_strFullRestful = null;

        int m_nConnectTimeout = 5000;
        int m_nREadTimeout = 5000;

        @Override
        protected Object doInBackground(String... objects) {
            StringBuilder urlBuilder;// = new StringBuilder(objects[0]);
            StringBuilder ConnectString = null;

            InputStream input;
            try{
                if(m_strFullRestful != null ) {
                    urlBuilder = new StringBuilder(m_strFullRestful);
                }else{
                    urlBuilder = new StringBuilder(m_strRequestHeader);
                    urlBuilder.append("?" + "apikey=" + m_strDaumApiKey + "&" + "q=" + URLEncoder.encode(m_strSearch, "UTF8") + "&output=json" + "&pageno=" + m_strPage + "&result=" + m_strResultNum);
                }

                URL url = new URL(urlBuilder.toString());
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setConnectTimeout(m_nConnectTimeout);
                conn.setReadTimeout(m_nREadTimeout);
                conn.setRequestMethod(m_strRequest);
                int retCode = conn.getResponseCode();
                BufferedReader rd;
                if (retCode >= 200 && retCode <= 300) {
                    rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    rd = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = rd.readLine()) != null) {
                    sb.append(line);
                    if (isCancelled()) break;
                }
                ConnectString = sb;
                rd.close();
                conn.disconnect();



                m_strFullRestful = null;

            }catch (Exception e)
            {
                e.printStackTrace();
            }


            return ConnectString;
        }


        public boolean setRequestMethod(String strRequest)
        {
            if( strRequest.equals("GET") || strRequest.equals("POST") || strRequest.equals("DELETE") || strRequest.equals("PUT") ) {
                m_strRequest = strRequest;
                return true;
            }else
            {
                Log.w("TAG", "invalid setRequestMethod: ");
                return false;
            }
        }

        public boolean setRequestHeader(String strRequestHeader)
        {
            if( strRequestHeader.length() != 0 ) {
                m_strRequestHeader = strRequestHeader;
                return true;
            }else
            {
                Log.w("TAG", "invalid setRequestHeader:");
                return false;
            }
        }

        public boolean setRequestPage(String strRequestPage)
        {
            if( strRequestPage.length() != 0 ) {
                m_strPage = strRequestPage;
                return true;
            }else
            {
                Log.w("TAG", "invalid setRequestPage:");
                return false;
            }
        }

        public boolean setResultNum(String strResultNum)
        {
            if( strResultNum.length() != 0 ) {
                m_strResultNum = strResultNum;
                return true;
            }else
            {
                Log.w("TAG", "invalid setRequestNum:");
                return false;
            }
        }

        public boolean setFullRestful(String strFullRestful)
        {
            if( strFullRestful.length() != 0 ) {
                m_strFullRestful = strFullRestful;
                return true;
            }else
            {
                Log.w("TAG", "invalid setFullRestful:");
                return false;
            }
        }
    }
}


