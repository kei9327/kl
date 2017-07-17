package com.knowlounge.fragment.poll;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.common.GlobalConst;
import com.knowlounge.manager.WenotePreferenceManager;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import com.knowlounge.adapter.PollListAdapter;
import com.knowlounge.model.PollList;
import com.knowlounge.util.RestClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import cz.msebera.android.httpclient.Header;

public class pollListFragment extends Fragment implements AbsListView.OnScrollListener, PollListAdapter.PollListSelectListener
{
    private final String TAG = "pollListFragment";

    private View rootView;
    private View footer;

    private static final String BASIC_SHUTDOWN_TIME = "30";

    private WenotePreferenceManager prefManager;

    private FragmentManager fragmentManager;
    private FragmentTransaction fragmentTransaction;

    private ArrayList<PollList> pollList;
    private ArrayList<PollList> selectedList;
    private PollListAdapter adapter;
    private int pollListPage;



    private int pollListTotalpage;
    private static int type;

    private TextView pollListEditing, pollListCancel, pollListSendBtn, listTitle, notListText;
    private ListView pollListView;
    private LinearLayout type2Area, not_list_layout, pollLoadingLayout;



    private boolean mLockListView;
    public static boolean edittingMode = false;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefManager = WenotePreferenceManager.getInstance(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setInit();
        type = getArguments().getInt("type");
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreatView");

        if (rootView == null) {
            rootView = inflater.inflate(R.layout.poll_list, container, false);
        }

        fragmentManager = getFragmentManager();
        fragmentTransaction = fragmentManager.beginTransaction();

        setFindViewById();
        setting_UI();
        getPollList();

        pollListView.setOnScrollListener(pollListFragment.this);
        PollListAdapter.setPollLIstListener(this);

        return rootView;
    }

    private void setInit() {
        edittingMode = false;
        type = -1;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        pollListEditing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == GlobalConst.VIEW_POLLLIST_FRAGMENT) {
                    if (edittingMode) {
                        edittingMode = false;
                        pollListEditing.setText(getResources().getString(R.string.global_edit));
                        pollListCancel.setText(getResources().getString(R.string.global_cancel));
//                        pollListSendBtn.setVisibility(View.VISIBLE);
                        checkAbleBtn();
                        adapter.notifyDataSetChanged();
                    } else {
                        edittingMode = true;
                        pollListEditing.setText(getResources().getString(R.string.global_complete));
                        pollListCancel.setText(getResources().getString(R.string.noti_delete));
//                        pollListSendBtn.setVisibility(View.GONE);
                        checkAbleBtn();
                        adapter.notifyDataSetChanged();
                    }
                    adapter.setSelectPollInit();
                    adapter.notifyDataSetChanged();
                } else {
                    //삭제기능
                    if(adapter.getSelectedPollitemno().length() != 0)
                        deletePollRest(adapter.getSelectedPollitemno());
                    else
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_poll_delselect), Toast.LENGTH_SHORT).show();
                }
            }
        });
        pollListCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type == GlobalConst.VIEW_POLLLIST_FRAGMENT) {
                    if (edittingMode) {
                        //삭제기능
                        if(adapter.getSelectedPollitemno().length() != 0)
                            deletePollRest(adapter.getSelectedPollitemno());
                        else
                            Toast.makeText(getActivity(), getResources().getString(R.string.toast_poll_delselect) , Toast.LENGTH_SHORT).show();
                    } else {
                        //종료
                        getActivity().finish();
                    }
                } else {
                    getActivity().finish();
                }
            }
        });

        /*
        pollListSendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String pollno = adapter.getSelectedPollitemno();
                if (pollno.length() != 0) {
                    final CordovaWebView webView = RoomActivity.activity.mWebViewFragment.getCordovaWebView();
                    webView.sendJavascript("PollCtrl.Action.Master.sendPoll('" + pollno + "','" + BASIC_SHUTDOWN_TIME + "');");
                    PollTimerFragment.answerTime = Integer.parseInt(BASIC_SHUTDOWN_TIME);
                    PollTimerFragment.pollTitleName = adapter.getSelectedPollitemTitle();
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.toast_polllist_send) , Toast.LENGTH_SHORT).show();
                }
            }
        });
        */
        footer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reload_pollItem();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(TAG, "onResume");

    }


    public void onDestroy()
    {
        super.onDestroy();
        Log.i(TAG, "onDestroy");
    }

    public void onPause()
    {
        super.onPause();
        Log.i(TAG, "onPause");

    }

    @Override
    public void onScrollStateChanged(AbsListView view, int scrollState) {

    }

    @Override
    public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

        Log.d(TAG,"onScroll");
        int item_count = totalItemCount - visibleItemCount;

        Log.d("totalItemCount",Integer.toString(totalItemCount));
        Log.d("firstVisibleItem",Integer.toString(firstVisibleItem));
        Log.d("item_count", Integer.toString(item_count));
        Log.d("adapter", Boolean.toString(mLockListView));

        if(firstVisibleItem >= item_count && totalItemCount !=0
            && !mLockListView && pollListPage <= pollListTotalpage) {
            Log.d(TAG,"condition true");
            mLockListView = true;
//            reload_pollItem();
        }else{
            mLockListView = false;
        }
    }

    private void reload_pollItem(){
        pollListPage++;
        getMorePollList();
        mLockListView = false;
    }

    public void setFindViewById() {

        pollListEditing = (TextView) rootView.findViewById(R.id.poll_list_editing);
        pollListCancel = (TextView) rootView.findViewById(R.id.poll_list_cancel);
        notListText = (TextView) rootView.findViewById(R.id.not_list_text);
        listTitle = (TextView) rootView.findViewById(R.id.list_title);

//        pollListSendBtn = (TextView) rootView.findViewById(R.id.poll_List_send_btn);

        pollListView = (ListView) rootView.findViewById(R.id.poll_list_view);

//        type2Area = (LinearLayout) rootView.findViewById(R.id.type2_area);
        not_list_layout = (LinearLayout) rootView.findViewById(R.id.not_list_layout);

        pollLoadingLayout = (LinearLayout)rootView.findViewById(R.id.poll_loading_layout);

        pollLoadingLayout.setVisibility(View.VISIBLE);
        pollListView.setVisibility(View.GONE);


        pollList = new ArrayList<PollList>();
        selectedList = new ArrayList<PollList>();


        adapter = new PollListAdapter(getActivity(), fragmentManager, fragmentTransaction, pollList, type);
        pollListView.setAdapter(adapter);


        if(pollListView.getFooterViewsCount() == 0) {
            footer = getActivity().getLayoutInflater().inflate(R.layout.poll_list_footer, null, false);
            pollListView.addFooterView(footer);
        }
        pollListPage = 1;
        mLockListView = false;
    }
    public void setting_UI(){

        if (type == GlobalConst.VIEW_POLLLIST_FRAGMENT) {  // 저장된 폴 리스트
            pollListEditing.setText(getResources().getString(R.string.global_edit));
            listTitle.setText(getResources().getString(R.string.canvas_poll_list));
            pollListCancel.setText(getResources().getString(R.string.global_cancel));
            notListText.setText(getResources().getString(R.string.canvas_poll_nosaved));

        } else if(type == GlobalConst.VIEW_COMPLETE_POLLLIST_FRAGMENT) {  // 완료된 폴 리스트
            pollListEditing.setText(getResources().getString(R.string.global_delete));
            listTitle.setText(getResources().getString(R.string.canvas_poll_result));
//            type2Area.setVisibility(View.GONE);
            notListText.setText(getResources().getString(R.string.canvas_poll_noresult));
        }
    }

    public void getPollList(){
        pollList.clear();
        String pollLIstUrl = (type == GlobalConst.VIEW_POLLLIST_FRAGMENT) ? "poll/tmp/list.json" : "poll/list.json";

        RequestParams params = new RequestParams();
        params.put("pageno", "1");

        if(type == GlobalConst.VIEW_POLLLIST_FRAGMENT) {
            params.put("pollkey", RoomActivity.activity.getUserNo());
        }else{
            params.put("pollkey", RoomActivity.activity.getRoomId());
        }


        RestClient.post(pollLIstUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                Log.d(TAG, "send message success.. response : " + obj.toString());
                try {
                    if(obj.getString("result").equals("0")) {
                        final int totalnumber = obj.getInt("totalcount");

                        if (totalnumber / 10 != 0)
                            pollListTotalpage = (totalnumber / 10) + 1;
                        else
                            pollListTotalpage = totalnumber / 10;

                        JSONArray arr = obj.getJSONArray("list");

                        int len = arr.length();
                        for (int i = 0; i < len; i++) {
                            JSONObject _obj = arr.getJSONObject(i);
                            String polltempno;
                            if(type == GlobalConst.VIEW_POLLLIST_FRAGMENT)
                                polltempno = _obj.has("polltempno") ? _obj.getString("polltempno") : "0";
                            else
                                polltempno = _obj.has("pollno") ? _obj.getString("pollno") : "0";

                            String title = _obj.has("title") ? _obj.getString("title") : "0";
                            pollList.add(new PollList(totalnumber, polltempno, title));
                        }
                        if (pollListPage == pollListTotalpage || pollList.size() == 0 || pollListTotalpage == 0)
                            pollListView.removeFooterView(footer);
                        //checkAbleBtn();
                        pollLoadingLayout.setVisibility(View.GONE);

                        if (len != 0) {
                            pollListEditing.setVisibility(View.VISIBLE);
                            pollListView.setVisibility(View.VISIBLE);
                            adapter.notifyDataSetChanged();
                        } else {
                            pollListEditing.setVisibility(View.INVISIBLE);
                            pollListView.setVisibility(View.GONE);
                            not_list_layout.setVisibility(View.VISIBLE);
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "send message onFailure");
                // TODO : 예외처리
                Toast.makeText(getActivity().getBaseContext(), getResources().getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
            }
        });
    }


    public void getMorePollList(){
        String pollLIstUrl = (type == GlobalConst.VIEW_POLLLIST_FRAGMENT) ? "poll/tmp/list.json" : "poll/list.json";

        RequestParams params = new RequestParams();
        params.put("pageno", pollListPage);

        if(type == GlobalConst.VIEW_POLLLIST_FRAGMENT) {
            params.put("pollkey", RoomActivity.activity.getUserNo());
        }else{
            params.put("pollkey", RoomActivity.activity.getRoomId());
        }


        RestClient.post(pollLIstUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                Log.d(TAG, "send message success.. response : " + obj.toString());
                try {
                    if(obj.getString("result").equals("0")) {
                        final int totalnumber = obj.getInt("totalcount");

                        if (totalnumber / 10 != 0)
                            pollListTotalpage = (totalnumber / 10) + 1;
                        else
                            pollListTotalpage = totalnumber / 10;

                        JSONArray arr = obj.getJSONArray("list");

                        int len = arr.length();
                        for (int i = 0; i < len; i++) {
                            JSONObject _obj = arr.getJSONObject(i);
                            String polltempno = _obj.has("polltempno") ? _obj.getString("polltempno") : "0";
                            String title = _obj.has("title") ? _obj.getString("title") : "0";
                            pollList.add(new PollList(totalnumber, polltempno, title));
                        }
                        adapter.notifyDataSetChanged();
                        if (pollListPage == pollListTotalpage || pollList.size() == 0 || pollListTotalpage == 0)
                            pollListView.removeFooterView(footer);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "send message onFailure");
                // TODO : 예외처리
                Toast.makeText(getActivity().getBaseContext(), "데이터를 받아 올 수 없습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void deletePollRest(String list_result) {
        Log.d(TAG, "deletePollRest");

        String deleteUrl = (type == GlobalConst.VIEW_POLLLIST_FRAGMENT) ? "poll/tmp/remove.json" : "poll/remove.json";

        RequestParams params = new RequestParams();

        if (type == GlobalConst.VIEW_POLLLIST_FRAGMENT) {
            params.put("polltempno", list_result);
        } else {
            params.put("pollno", list_result);
        }
        Log.d(TAG, "API URL : " + deleteUrl + ", param : " + params.toString());
        RestClient.post(deleteUrl, params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject obj) {
                Log.d(TAG, "send message success.. response : " + obj.toString());
                try {
                    if(obj.getString("result").equals("0")){
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_poll_deleted), Toast.LENGTH_SHORT).show();
                        pollListPage = 1;
                        getPollList();

                        if(type == GlobalConst.VIEW_POLLLIST_FRAGMENT) {
                            edittingMode = false;
                            pollListEditing.setText(getResources().getString(R.string.global_edit));
                            pollListCancel.setText(getResources().getString(R.string.global_cancel));
//                            pollListSendBtn.setVisibility(View.VISIBLE);
                        }else{
                            pollListEditing.setTextColor(Color.parseColor("#969696"));
                        }

                    } else {
                        Toast.makeText(getActivity(), getResources().getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseString, Throwable throwable) {
                Log.d(TAG, "send message onFailure");
                // TODO : 예외처리
                Toast.makeText(getActivity(), getResources().getString(R.string.toast_common_error), Toast.LENGTH_SHORT).show();

            }
        });
    }


    @Override
    public void onSelect() {
        checkAbleBtn();
    }


    private void checkAbleBtn(){
        String result = adapter.getSelectedPollitemno();
        if (type == GlobalConst.VIEW_POLLLIST_FRAGMENT) {
            if (edittingMode) {
                pollListCancel.setTextColor(Color.parseColor("#969696"));
                pollListCancel.setFocusable(false);
                pollListCancel.setClickable(false);

                if (result.equals("")) {
                    pollListCancel.setTextColor(Color.parseColor("#969696"));
                    pollListCancel.setFocusable(false);
                    pollListCancel.setClickable(false);
                } else {
                    pollListCancel.setTextColor(getResources().getColor(R.color.app_base_color));
                    pollListCancel.setFocusable(true);
                    pollListCancel.setClickable(true);
                }
            } else {
                pollListCancel.setTextColor(getResources().getColor(R.color.app_base_color));
                pollListCancel.setFocusable(true);
                pollListCancel.setClickable(true);

                /*
                if (result.equals("")) {
                    pollListSendBtn.setTextColor(Color.parseColor("#969696"));
                    pollListSendBtn.setFocusable(false);
                    pollListSendBtn.setClickable(false);
                } else {
                    pollListSendBtn.setTextColor(getResources().getColor(R.color.app_base_color));
                    pollListSendBtn.setFocusable(true);
                    pollListSendBtn.setClickable(true);
                }*/
            }
        } else {
            if (result.equals("")) {
                pollListEditing.setTextColor(Color.parseColor("#969696"));
                pollListEditing.setFocusable(false);
                pollListEditing.setClickable(false);
            } else {
                pollListEditing.setTextColor(getResources().getColor(R.color.app_base_color));
                pollListEditing.setFocusable(true);
                pollListEditing.setClickable(true);
            }
        }
    }
}