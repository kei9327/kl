package com.knowlounge.fragment;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.knowlounge.R;
import com.knowlounge.adapter.NotiAdapter;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.NoticeData;
import com.knowlounge.model.Room;
import com.knowlounge.util.AndroidUtils;
import com.knowlounge.util.RestClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

/**
 * Created by we160303 on 2016-05-30.
 */
public class NoticeFragment extends Fragment implements View.OnClickListener, AbsListView.OnScrollListener{

    private final String TAG = "NoticeFragment";

    private final String URL_HISTORY_LIST = "history/list.json";
    private final String URL_HISTORY_REMOVE = "history/remove.json";

    private WenotePreferenceManager prefManager;

    private View rootView;

    private View noticeFooter;

    private TextView btnNoticeEdit, btnNoticeCancel, noticeSelectedAllText, selectedRemove;
    private ImageView noticeSelectedAll;
    private ListView noticeList;
    private LinearLayout noticeEditModeLayout;
    private LinearLayout noDataScreen;
    private NotiAdapter adapter;
    private JSONObject obj = null;

    private boolean more;
    private String currentIdx;
    private boolean mLockListView = false;


    //more data
    private ArrayList<NoticeData> list;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (rootView == null) {
            rootView = inflater.inflate(R.layout.klounge_notice, container, false);
        }

        prefManager = WenotePreferenceManager.getInstance(getContext());

        //        Test json
        setFindViewById();
        getNoticeData();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        noticeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(adapter.isEdittingMode()){
                    adapter.setChecked(position);
                    adapter.notifyDataSetChanged();

                    if(adapter.getSelectedCount() == list.size()){
                        noticeSelectedAll.setImageResource(R.drawable.btn_checkbox_on);
                        noticeSelectedAllText.setTextColor(getResources().getColor(R.color.app_base_color));
                        noticeSelectedAllText.setText(getResources().getString(R.string.noti_deselect));
                        adapter.setSelectedAll(true);
                    }else{
                        noticeSelectedAll.setImageResource(R.drawable.btn_checkbox);
                        noticeSelectedAllText.setTextColor(Color.parseColor("#969696"));
                        noticeSelectedAllText.setText(getResources().getString(R.string.noti_selectall));
                        adapter.setSelectedAll(false);
                    }

                }else{
                    return;
                }
            }
        });
    }

    private void getNoticeData() {

        list.clear();
        RequestParams params = new RequestParams();
        String masterCookie = prefManager.getUserCookie();
        String checkSum = prefManager.getChecksumCookie();

        RestClient.getWithCookie(URL_HISTORY_LIST, masterCookie, checkSum, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d(TAG,"response : "+ response.toString());
                    int apiResult = response.getInt("result");

                    more = response.getString("more").equals("1") ? true : false;
                    if(more)
                        Log.d("notice_more","true");
                    else
                        Log.d("notice_more","false");
                    if(!more)
                        noticeList.removeFooterView(noticeFooter);


                    if(apiResult == 0) {
                        JSONArray objList = response.getJSONArray("list");
                        if(objList.length() != 0) {
                            for (int i = 0; i < objList.length(); i++) {
                                JSONObject obj = objList.getJSONObject(i);
                                NoticeData tempData = new NoticeData(false);
                                if (!obj.isNull("category"))
                                    tempData.setCategory(obj.getString("category"));
                                if (!obj.isNull("senderthumbnail"))
                                    tempData.setSenderThumbNail(obj.getString("senderthumbnail"));
                                if (!obj.isNull("cnt"))
                                    tempData.setCnt(Integer.parseInt(obj.getString("cnt")));
                                if (!obj.isNull("status"))
                                    tempData.setStatus(Integer.parseInt(obj.getString("status")));
                                if (!obj.isNull("usernm"))
                                    tempData.setUserName(obj.getString("usernm"));
                                if (!obj.isNull("cdatetime"))
                                    tempData.setDateTime(obj.getString("cdatetime"));
                                if (!obj.isNull("historytype"))
                                    tempData.setHistoryType(obj.getString("historytype"));
                                if (!obj.isNull("historyno"))
                                    tempData.setHistoryNo(Integer.parseInt(obj.getString("historyno")));
                                if (!obj.isNull("sendernm"))
                                    tempData.setSenderName(obj.getString("sendernm"));
                                if (!obj.isNull("title"))
                                    tempData.setNoticeTitle(obj.getString("title"));
                                if (!obj.isNull("roomseqno"))
                                    tempData.setRoomSeqNo(obj.getString("roomseqno"));
                                if (!obj.isNull("extraname"))
                                    tempData.setExtraNm(obj.getString("extraname"));
                                if (!obj.isNull("roomid"))
                                    tempData.setRoomId(obj.getString("roomid"));

                                currentIdx = obj.getString("idx");
                                list.add(tempData);
                            }
                            adapter.notifyDataSetChanged();
                            adapter.isAllSelectedMode(false);
                        }
                        existData();

                    }
                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "Get room list onFailure");
            }
        });
    }
    private void moreNoticeData() {
        mLockListView = true;

        RequestParams params = new RequestParams();
        params.put("idx",currentIdx);

        String masterCookie = prefManager.getUserCookie();
        String checkSum = prefManager.getChecksumCookie();

        RestClient.getWithCookie(URL_HISTORY_LIST, masterCookie, checkSum, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d(TAG,"response : "+ response.toString());
                    int apiResult = response.getInt("result");

                    more = response.getString("more").equals("1") ? true : false;

                    if(!more)
                        noticeList.removeFooterView(noticeFooter);

                    if(apiResult == 0) {
                        JSONArray objList = response.getJSONArray("list");
                        if(objList.length() != 0) {
                            for (int i = 0; i < objList.length(); i++) {
                                JSONObject obj = objList.getJSONObject(i);
                                NoticeData tempData = new NoticeData(false);
                                if (!obj.isNull("category"))
                                    tempData.setCategory(obj.getString("category"));
                                if (!obj.isNull("senderthumbnail"))
                                    tempData.setSenderThumbNail(obj.getString("senderthumbnail"));
                                if (!obj.isNull("cnt"))
                                    tempData.setCnt(Integer.parseInt(obj.getString("cnt")));
                                if (!obj.isNull("status"))
                                    tempData.setStatus(Integer.parseInt(obj.getString("status")));
                                if (!obj.isNull("usernm"))
                                    tempData.setUserName(obj.getString("usernm"));
                                if (!obj.isNull("cdatetime"))
                                    tempData.setDateTime(obj.getString("cdatetime"));
                                if (!obj.isNull("historytype"))
                                    tempData.setHistoryType(obj.getString("historytype"));
                                if (!obj.isNull("historyno"))
                                    tempData.setHistoryNo(Integer.parseInt(obj.getString("historyno")));
                                if (!obj.isNull("sendernm"))
                                    tempData.setSenderName(obj.getString("sendernm"));
                                if (!obj.isNull("title"))
                                    tempData.setNoticeTitle(obj.getString("title"));
                                if (!obj.isNull("roomseqno"))
                                    tempData.setRoomSeqNo(obj.getString("roomseqno"));
                                if (!obj.isNull("extraname"))
                                    tempData.setExtraNm(obj.getString("extraname"));
                                if (!obj.isNull("roomid"))
                                    tempData.setRoomId(obj.getString("roomid"));

                                currentIdx = obj.getString("idx");
                                list.add(tempData);
                            }

                            noticeSelectedAll.setImageResource(R.drawable.btn_checkbox);
                            noticeSelectedAllText.setTextColor(Color.parseColor("#969696"));
                            noticeSelectedAllText.setText(getResources().getString(R.string.noti_selectall));
                            adapter.setSelectedAll(false);

                            adapter.notifyDataSetChanged();
                            mLockListView = false;

                        }
                        existData();

                    }
                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "Get room list onFailure");
            }
        });
    }

    private void removeNotice(String selectedItem) {

        if (selectedItem.length() == 0) {
            Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.toast_noti_delselect), Toast.LENGTH_SHORT).show();
            return;
        }

        RequestParams params = new RequestParams();
        params.put("historyno",selectedItem);

        String masterCookie = prefManager.getUserCookie();
        String checkSum = prefManager.getChecksumCookie();

        RestClient.postWithCookie(URL_HISTORY_REMOVE, masterCookie, checkSum, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                try {
                    Log.d(TAG,"response : "+ response.toString());
                    int apiResult = response.getInt("result");
                    if(apiResult == 0) {
                        Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.toast_noti_deleted) , Toast.LENGTH_SHORT).show();
                        adapter.toggleEditMode();
                        setEditMode(adapter.isEdittingMode());

                        getNoticeData();
                    }
                } catch (JSONException e) {
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "Get room list onFailure");
            }
        });
    }

    private void setFindViewById() {

        noticeList = (ListView) rootView.findViewById(R.id.notice_list);
        btnNoticeEdit = (TextView) rootView.findViewById(R.id.btn_notice_edit);
        btnNoticeCancel = (TextView) rootView.findViewById(R.id.btn_notice_cancel);
        noticeEditModeLayout = (LinearLayout) rootView.findViewById(R.id.notice_edit_mode_layout);
        noticeSelectedAll = (ImageView) rootView.findViewById(R.id.notice_selected_all);
        noticeSelectedAllText = (TextView) rootView.findViewById(R.id.notice_selected_all_text);
        noDataScreen = (LinearLayout) rootView.findViewById(R.id.no_data_screen);
        selectedRemove = (TextView) rootView.findViewById(R.id.selected_remove);

        list = new ArrayList<NoticeData>();
        adapter = new NotiAdapter(getActivity(),this,list);

        noticeFooter = ((LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE)).inflate(R.layout.klounge_notice_footer, null);
        noticeList.addFooterView(noticeFooter);

        noticeList.setOnScrollListener(this);
        noticeList.setAdapter(adapter);

        btnNoticeCancel.setOnClickListener(this);
        btnNoticeEdit.setOnClickListener(this);
        noticeSelectedAll.setOnClickListener(this);
        noticeSelectedAllText.setOnClickListener(this);
        selectedRemove.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btn_notice_edit :
                adapter.toggleEditMode();
                setEditMode(adapter.isEdittingMode());
                return;
            case R.id.selected_remove :
                Log.d("remove_list",adapter.getSelectedItem());
                removeNotice(adapter.getSelectedItem());
                return;
            case R.id.btn_notice_cancel :
                getActivity().finish();
                return;

            case R.id.notice_selected_all :
            case R.id.notice_selected_all_text :
                adapter.selectedAll();
                setSelectedAll(adapter.isSelectedAll());
        }

    }


    private void setSelectedAll(boolean selectedAll) {
        if(selectedAll) {
            noticeSelectedAll.setImageResource(R.drawable.btn_checkbox_on);
            noticeSelectedAllText.setTextColor(getResources().getColor(R.color.app_base_color));
            noticeSelectedAllText.setText(getResources().getString(R.string.noti_deselect));
            adapter.isAllSelectedMode(true);
        }else {
            noticeSelectedAll.setImageResource(R.drawable.btn_checkbox);
            noticeSelectedAllText.setTextColor(Color.parseColor("#969696"));
            noticeSelectedAllText.setText(getResources().getString(R.string.noti_selectall));
            adapter.isAllSelectedMode(false);
        }
}
    private void setEditMode(boolean edittingMode) {
        if(edittingMode){
            btnNoticeEdit.setText(getResources().getString(R.string.global_complete));
            Animation ani = AnimationUtils.loadAnimation(getActivity(),R.anim.drop_down);
            adapter.isAllSelectedMode(false);
            setSelectedAll(adapter.setSelectedAll(false));
            noticeEditModeLayout.startAnimation(ani);
            noticeEditModeLayout.setVisibility(View.VISIBLE);

        }else{
            btnNoticeEdit.setText(getResources().getString(R.string.global_edit));
            Animation ani = AnimationUtils.loadAnimation(getActivity(),R.anim.riseup);
            noticeEditModeLayout.startAnimation(ani);
            noticeEditModeLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    noticeEditModeLayout.setVisibility(View.GONE);
                }
            },150);
        }
    }

    private void existData(){
        if(list.size() == 0){
            noticeList.setVisibility(View.GONE);
            noDataScreen.setVisibility(View.VISIBLE);
        }else{
            noticeList.setVisibility(View.VISIBLE);
            noDataScreen.setVisibility(View.GONE);
        }
    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
        int count = totalItemCount - visibleItemCount;

        if(firstVisibleItem >= count && totalItemCount != 0
                && !mLockListView && more)
        {
            Log.i(TAG, "Loading next items");
            moreNoticeData();
        }
    }
}


