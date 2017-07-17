package com.knowrecorder.develop.fragment.TimeLine;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;

import com.knowrecorder.R;
import com.knowrecorder.Utils.PixelUtil;
import com.knowrecorder.Utils.TimeConverter;
import com.knowrecorder.Views.LiveViewHolyShit;
import com.knowrecorder.develop.audio.AudioPlayer;
import com.knowrecorder.develop.event.ChangePage;
import com.knowrecorder.develop.fragment.TimeLine.Adapter.AudioArrayAdapter;
import com.knowrecorder.develop.fragment.TimeLine.Adapter.PacketArrayAdapter;
import com.knowrecorder.develop.fragment.TimeLine.Adapter.TimeArrayAdapter;
import com.knowrecorder.develop.fragment.TimeLine.Model.TImelineAudioBlock;
import com.knowrecorder.develop.fragment.TimeLine.Model.TimelinePacketBlock;
import com.knowrecorder.develop.fragment.TimeLine.Model.TimelineTimeBlock;
import com.knowrecorder.develop.manager.PageManager;
import com.knowrecorder.develop.model.realm.TimeLine;
import com.knowrecorder.develop.model.realmHoler.TimeLineHolder;
import com.knowrecorder.develop.utils.PacketUtil;
import com.knowrecorder.rxjava.RxEventFactory;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by we160303 on 2017-02-02.
 */

public class TImeLineFragment extends Fragment {

    private View rootView;
    private LiveViewHolyShit listWrapper;
    private float lastRuntime = 0;

    private HorizontalListView packetList;
    private HorizontalListView audioList;
    private HorizontalListView timeList;

    PacketArrayAdapter packetAdapter;
    TimeArrayAdapter timeAdapter;
    AudioArrayAdapter audioAdapter;

    private ArrayList<TimelinePacketBlock> packetBlocks = new ArrayList<>();
    private ArrayList<TImelineAudioBlock> audioBlocks = new ArrayList<>();
    private ArrayList<TimelineTimeBlock> timeBlocks = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.rb_timeline_fragment, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        setBindViewId();
        initPacketData();
        connectAdapter();
        setLastPacketPadding();

    }

    private void setBindViewId() {
        listWrapper = (LiveViewHolyShit) rootView.findViewById(R.id.list_wrapper);
        packetList = (HorizontalListView) rootView.findViewById(R.id.packet_list);
        audioList = (HorizontalListView) rootView.findViewById(R.id.audio_list);
        timeList = (HorizontalListView) rootView.findViewById(R.id.time_list);

        packetList.setTAG("packetList");
        audioList.setTAG("audioList");
        timeList.setTAG("timeList");

        packetList.setOnScrollStateChangedListener(new HorizontalListView.OnScrollStateChangedListener() {
            @Override
            public void onScrollStateChanged(ScrollState scrollState) {
                switch (scrollState) {
                    case SCROLL_STATE_TOUCH_SCROLL_COMPLETE:
                        float runtime = packetList.getCurrX() * 1000 / PixelUtil.getInstance().convertDpToPixel(40);
                        if(lastRuntime != runtime) {
                            Log.d("onScrollStateChanged", "Drawing SCROLL_STATE_TOUCH_SCROLL_COMPLETE    this runTIme is " + runtime);
                            RxEventFactory.get().post(new ChangePage(runtime));
                        }
                        lastRuntime = runtime;
                        callActivityCallback(runtime);
                        break;
                }
            }
        });
    }

    private void connectAdapter() {
        packetAdapter = new PacketArrayAdapter(getActivity(), packetBlocks);
        timeAdapter = new TimeArrayAdapter(getActivity(), timeBlocks);
        audioAdapter = new AudioArrayAdapter(getActivity(), audioBlocks);

        packetList.setAdapter(packetAdapter);
        audioList.setAdapter(audioAdapter);
        timeList.setAdapter(timeAdapter);
    }

    public void refreshData(){
        packetBlocks.clear();
        audioBlocks.clear();
        timeBlocks.clear();
        initPacketData();
        setLastPacketPadding();

        float runTime = PageManager.getInstance().getCurrentRunTime()/1000 * PixelUtil.getInstance().convertDpToPixel(40);
        lastRuntime = runTime;

        scrollToList((int)runTime);
    }

    public void scrollToList(int moveTo){
        Log.e("visibility=" , Integer.toString(packetList.getVisibility()) + Integer.toString(audioList.getVisibility())
                                                                             + Integer.toString(timeList.getVisibility())
                                                                             + Integer.toString(rootView.getVisibility()) ) ;
        packetList.scrollTo(moveTo);
        audioList.scrollTo(moveTo);
        timeList.scrollTo(moveTo);

    }

    private void setLastPacketPadding() {
        int totalWidthOfPackets = getTotalWidthOfListView(packetList);
        int totalWidthOfAudios = getTotalWidthOfListView(audioList);
        int totalWidthOfTimes = getTotalWidthOfListView(timeList);

        int totalWidthMax = Math.max(Math.max(totalWidthOfPackets, totalWidthOfAudios), totalWidthOfTimes);

        int packetsPaddingRight = totalWidthMax - totalWidthOfPackets;
        final int audiosPaddingRight = totalWidthMax - totalWidthOfAudios;
        int timesPaddingRight = totalWidthMax - totalWidthOfTimes;

        if(packetBlocks.size() > 0){
            packetBlocks.get(packetBlocks.size()-1).addRightGap(packetsPaddingRight);
            packetAdapter.notifyDataSetChanged();
        }
        if(audioBlocks.size() > 0){
            audioBlocks.get(0).addRightGap(audiosPaddingRight);
            audioAdapter.notifyDataSetChanged();
        }
        if(timeBlocks.size() > 0){
            timeBlocks.get(timeBlocks.size()-1).addRightGap(timesPaddingRight);
            timeAdapter.notifyDataSetChanged();
        }
        audioList.post(new Runnable() {
            @Override
            public void run() {

                int average = audioList.getmMaxX();

                if(average == Integer.MAX_VALUE)
                    average = 0;

                packetList.setAverageMaxX(average);
                audioList.setAverageMaxX(average);
                timeList.setAverageMaxX(average);

                Log.d("TImeLineFragment", "packelist_width : " + getTotalWidthOfListView(packetList) + "  maxX : " + packetList.getmMaxX());
                Log.d("TImeLineFragment", "audio_width : " + getTotalWidthOfListView(audioList) + "  maxX : " + audioList.getmMaxX());
                Log.d("TImeLineFragment", "time_width : " + getTotalWidthOfListView(timeList) + "  maxX : " + timeList.getmMaxX());

                scrollToList((int)(PageManager.getInstance().getCurrentRunTime()/1000 * PixelUtil.getInstance().convertDpToPixel(40)));
            }
        });
    }
    public int getTotalWidthOfListView(HorizontalListView listView) {
        ListAdapter adapter = listView.getAdapter();
        int totalWidth = 0;

        for (int i = 0; i < adapter.getCount(); i++) {
            View view = adapter.getView(i, null, listView);
            view.measure(
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));

            totalWidth += view.getMeasuredWidth();

            if(adapter.getItem(i) instanceof TimelinePacketBlock){
                TimelinePacketBlock block = (TimelinePacketBlock)adapter.getItem(i);
                if(block.isBasicWidth()){
                    totalWidth -= block.getWidth();
                }
            }
        }

        return totalWidth;
    }


    private void initPacketData(){

        long duration = AudioPlayer.getInstance(getContext()).prevGetDuration();
        Realm realm = Realm.getDefaultInstance();
        RealmResults<TimeLine> results = realm.where(TimeLine.class).findAllSorted("startRun", Sort.ASCENDING);
        for(int i = 0; i < results.size() ; i++){
            TimeLineHolder dataHolder = results.get(i).clone();
            int leftGap = 0;
            int rightGap = 0;
            int width;
            int type = typeConvertStringToInteger(dataHolder.getType());
            Drawable icon = getIcon(type);
            long mid = dataHolder.getMid();

            if(i == 0){
                leftGap += getCenter();
                leftGap += convertTimeToWidth(dataHolder.getStartRun());
            }else if(i == results.size() - 1){
                // 이전의 패킷과 현재 패킷의 Gap = 현재 시작 시간 - ( 이전 패킷의 startRunTime + 이전 패킷의 width)
                TimelinePacketBlock lastblock = packetBlocks.get(packetBlocks.size()-1);
                leftGap += (convertTimeToWidth(dataHolder.getStartRun()) - convertTimeToWidth(results.get(i-1).getStartRun()) - (lastblock.isBasicWidth() ? 0 : lastblock.getWidth()));
                rightGap += getCenter();
            }else{
                // 이전의 패킷과 현재 패킷의 Gap = 현재 시작 시간 - ( 이전 패킷의 startRunTime + 이전 패킷의 width)
                TimelinePacketBlock lastblock = packetBlocks.get(packetBlocks.size()-1);
                leftGap += (convertTimeToWidth(dataHolder.getStartRun()) - convertTimeToWidth(results.get(i-1).getStartRun()) - (lastblock.isBasicWidth() ? 0 : lastblock.getWidth()));
            }

            width = convertTimeToWidth(dataHolder.getEndRun()-dataHolder.getStartRun());

            packetBlocks.add(new TimelinePacketBlock(leftGap, rightGap, width, type, icon,  mid));
        }

        int second = (int)(Math.ceil((float)duration/1000));

        for(int i = 0; i <= second; i++){
            int leftGap = 0;
            int rightGap = 0;
            if(i == 0){
                leftGap += getCenter() - PixelUtil.getInstance().convertDpToPixel(20);
            }else if(i == second){
                rightGap += getCenter() - PixelUtil.getInstance().convertDpToPixel(20);
            }
            timeBlocks.add(new TimelineTimeBlock(leftGap, rightGap, TimeConverter.convertSecondsToMmSs(i*1000)));
        }

        if(duration != 0)
            audioBlocks.add(new TImelineAudioBlock(getCenter(), getCenter(), convertTimeToWidth((int)duration), (int)duration));
    }

    private int getCenter(){
        DisplayMetrics metrics = getContext().getResources().getDisplayMetrics();
        ((AppCompatActivity)getContext()).getWindowManager().getDefaultDisplay().getMetrics(metrics);

        return metrics.widthPixels/2;
    }
    private Drawable getIcon(int type){
        switch (type){
            case PacketUtil.PEN :
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_pen);
            case PacketUtil.LASER :
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_pointer);
            case PacketUtil.SHAPE :
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_shape);
            case PacketUtil.VIDEO :
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_movie);
            case PacketUtil.VIDEOSTART :
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_video_start);
            case PacketUtil.VIDEOPAUSE :
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_video_pause);
            case PacketUtil.TXTBEGIN :
            case PacketUtil.TXTEDIT :
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_text);
            case PacketUtil.UNDO :
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_undo);
            case PacketUtil.REDO :
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_redo);
            case PacketUtil.CHANGEPAGE :
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_change_page);
            case PacketUtil.IMAGE :
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_image);
            case PacketUtil.GRABOBJ :
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_hand);
            case PacketUtil.DELOBJ :
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_del);
            case PacketUtil.ALLDELETE :
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_all_delete);
            case PacketUtil.PDF:
                return ContextCompat.getDrawable( (AppCompatActivity)getContext(), R.drawable.icon_trackbar_pdf);
            default:
                return  null;
        }
    }
    private int typeConvertStringToInteger(String type){
        switch (type){
            case PacketUtil.S_PEN :
                return PacketUtil.PEN;
            case PacketUtil.S_LASER :
                return PacketUtil.LASER;
            case PacketUtil.S_SHAPE :
                return PacketUtil.SHAPE;
            case PacketUtil.S_VIDEO :
                return PacketUtil.VIDEO;
            case PacketUtil.S_VIDEOSTART :
                return PacketUtil.VIDEOSTART;
            case PacketUtil.S_VIDEOPAUSE :
                return PacketUtil.VIDEOPAUSE;
            case PacketUtil.S_TXTBEGIN :
            case PacketUtil.S_TXTEDIT :
                return PacketUtil.TXTEDIT;
            case PacketUtil.S_UNDO :
                return PacketUtil.UNDO;
            case PacketUtil.S_REDO :
                return PacketUtil.REDO;
            case PacketUtil.S_CHANGEPAGE :
                return PacketUtil.CHANGEPAGE;
            case PacketUtil.S_IMAGE :
                return PacketUtil.IMAGE;
            case PacketUtil.S_GRABOBJ :
                return PacketUtil.GRABOBJ;
            case PacketUtil.S_DELOBJ :
                return PacketUtil.DELOBJ;
            case PacketUtil.S_ALLDELETE :
                return PacketUtil.ALLDELETE;
            case PacketUtil.S_PDF :
                return PacketUtil.PDF;
            default:
                return -1;
        }
    }
    private int convertTimeToWidth(float time){
        return (int)(time/1000 * PixelUtil.getInstance().convertDpToPixel(40));
    }

    TimeLineListener activityCallback;
    public interface TimeLineListener{
        public void onTimeLineChanged(float pottition);
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try{
            activityCallback = (TimeLineListener)context;
        }catch(ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement onButtonClick");
        }
    }

    public void callActivityCallback(float runTime){
        activityCallback.onTimeLineChanged(runTime);
    }
}
