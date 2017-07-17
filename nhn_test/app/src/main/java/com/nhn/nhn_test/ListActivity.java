package com.nhn.nhn_test;

import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Handler;
import android.os.Message;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ListActivity extends AppCompatActivity {

    private Button testButton;
    private static nhnConnect.myConnect m_myConnect = new nhnConnect.myConnect();
    private SwipeRefreshLayout m_refreshLayout;
    private String[] srtTst = null;
    private ListView m_listView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Button testButton = (Button)this.findViewById(R.id.test_btn);
        Button Btn_delete = (Button)this.findViewById(R.id.search_delete_btn);
        testButton.setOnClickListener(testListener);
        Btn_delete.setOnClickListener(deleteButtonListener);

        m_refreshLayout = (SwipeRefreshLayout)findViewById(R.id.swipeLayout);
        m_refreshLayout.setOnRefreshListener(m_refreshLayoutListener);

        m_listView = (ListView)findViewById(R.id.listView);

        SQLiteDatabase sampleDB = null;


        try{
            sampleDB = this.openOrCreateDatabase("nhnDB", MODE_PRIVATE, null);
            sampleDB.execSQL("CREATE TABLE " + "nhnTable"  + " (name VARCHAR(20));");
            sampleDB.execSQL("DELETE FROM" + "nhnTable");
            sampleDB.close();

        }catch(SQLiteException se)
        {
            Toast.makeText(getApplicationContext(), se.getMessage(), Toast.LENGTH_LONG).show();
            Log.e("", se.getMessage());
        }

    }


    Button.OnClickListener testListener = new Button.OnClickListener()
    {
        @Override
        public void onClick(View v) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        m_myConnect = new nhnConnect.myConnect();
                        m_myConnect.setRequestMethod("GET");
                        m_myConnect.execute("test");
                        JSONObject jsonOb = new JSONObject(m_myConnect.get().toString());
                        JSONArray infoArray = jsonOb.getJSONObject("channel").getJSONArray("item");
                        int nJsonLength = infoArray.length();
                        srtTst = new String[nJsonLength];



                        List<HashMap<String,String>> countries = new ArrayList<HashMap<String, String>>();


                        for(int i = 0 ; i < nJsonLength ; i++)
                        {
                            srtTst[i] = infoArray.getJSONObject(i).getString("title");
                            HashMap<String, String> strList = new HashMap<String, String>();
                            strList.put("title", srtTst[i]);
                            countries.add(strList);
                        }

                        SimpleAdapter adapter = new SimpleAdapter(getBaseContext(), countries, R.layout.lv_layout, new String[]{"title"}, new int[]{R.id.tv_country});
                        m_listView.setAdapter(adapter);


                    }catch (Exception e)
                    {

                    }
                }
            }, 1);
        }
    };

    SwipeRefreshLayout.OnRefreshListener m_refreshLayoutListener = new SwipeRefreshLayout.OnRefreshListener()
    {
        @Override
        public void onRefresh() {
            m_refreshLayout.setRefreshing(false);
        }
    };

    Button.OnClickListener deleteButtonListener = new Button.OnClickListener()
    {
        @Override
        public void onClick(View v) {

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        m_myConnect.cancel(true);
                        nhnConnect.myConnect myConnect = new nhnConnect.myConnect();
                        myConnect.setRequestMethod("POST");
                        myConnect.setFullRestful("https://s3-ap-northeast-1.amazonaws.com/zigbang/recruit/mobile.json");
                        myConnect.execute("test");
                    }catch (Exception e)
                    {

                    }
                }
            }, 1);
        }
    };

    /*
    public static Handler mHandler = new Handler(){
        public void handleMessage(Message msg)
        {
            switch (msg.what) {
                case 1:{
                    String response = msg.obj.toString();
                    try {

                        String test = m_myConnect.get();
                        int a = 0;

                    }catch (Exception e)
                    {

                    }
                }
            }
        }
    };*/




}
