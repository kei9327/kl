//package com.knowrecorder.Timeline;
//
//import android.content.Context;
//import android.content.res.Resources;
//import android.graphics.Color;
//import android.os.AsyncTask;
//import android.util.DisplayMetrics;
//import android.view.View;
//import android.view.WindowManager;
//import android.widget.ListAdapter;
//
//import com.knowrecorder.Adapters.BlockArrayAdapter;
//import com.knowrecorder.Adapters.BlockArrayaudioAdapter;
//import com.knowrecorder.Adapters.BlockTimeAdapter;
//import com.knowrecorder.MainActivity;
//import com.knowrecorder.Managers.NoteManager;
//import com.knowrecorder.Utils.PixelUtil;
//import com.knowrecorder.Views.HorizontalListView;
//
//import java.util.ArrayList;
//
//import io.realm.Realm;
//import io.realm.RealmConfiguration;
//import io.realm.RealmResults;
//import io.realm.Sort;
//
//public class TimelineDrawer {
//    private Context context;
//    private float displayWidth;
//    float centerX;
//    float DP6_TO_PIXEL;
//    float DP2_TO_PIXEL;
//    float lastPacketX = 0;
//    long lastPacketTime = 0;
//    long gap = 0;
//    int packetsWidth = 0;
//    Realm realmInMemory = null;
//    RealmConfiguration inMemoryConfig = null;
//    RealmResults<Packet> packets;
//    ArrayList<BlockData> blocks = new ArrayList<>();
//    ArrayList<BlockData> audioBlocks = new ArrayList<>();
//    ArrayList<BlockData> alignedAudioBlocks = new ArrayList<>();
//    ArrayList<TimeTextBlock> arrTimes = new ArrayList<>();
//    private HorizontalListView listView;
//    private HorizontalListView times;
//    private HorizontalListView audios;
//    private DrawingTask task;
//    private final static int SECONDS_DEFAULT_LENGTH = 200;
//    private final static String LOG_TAG = "TimelineDrawer";
//
//    private static boolean isScrollChanged = false;
//    private boolean isCancelled = false;
//    private boolean shortPacketAlreadyDrawn = false;
//
//    private static final int DIVISION_SIZE = (int) PixelUtil.getInstance().convertDpToPixel(400);
//
//    private int uid, did;
//
//    BlockArrayAdapter adapter;
//    BlockArrayaudioAdapter audioAdapter;
//    BlockTimeAdapter timeAdapter;
//
//    public TimelineDrawer(Context context, HorizontalListView listView, HorizontalListView times) {
//        initView(context, listView, times);
//    }
//
//    public static boolean isScrollChanged() {
//        return isScrollChanged;
//    }
//
//    public static void setScrollChanged(boolean isScrollChanged) {
//        TimelineDrawer.isScrollChanged = isScrollChanged;
//    }
//
//    public void drawTimeline() {
//        task = new DrawingTask();
//        task.execute();
//    }
//
//    private void initView(Context context, HorizontalListView listView, HorizontalListView times) {
//        this.context = context;
//        this.listView = listView;
//        this.times = times;
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics metrics = new DisplayMetrics();
//        wm.getDefaultDisplay().getMetrics(metrics);
//        this.displayWidth = metrics.widthPixels;
//        centerX = this.displayWidth / 2;
//        DP6_TO_PIXEL = convertDpToPixel(6);
//        DP2_TO_PIXEL = convertDpToPixel(2);
//    }
//
//    private float convertDpToPixel(float dp) {
//        Resources resources = context.getResources();
//        DisplayMetrics metrics = resources.getDisplayMetrics();
//        float px = dp * (metrics.densityDpi / 160f);
//        return px;
//    }
//
//    public void setAudios(HorizontalListView audios) {
//        this.audios = audios;
//    }
//
//    private class DrawingTask extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected void onPreExecute() {
//            lastPacketX = centerX + PixelUtil.getInstance().convertDpToPixel(4.5f); // 그리기 시작점 초기화
//            gap = 0;
//            lastPacketTime = 0;
//            packets = null;
//            blocks.clear();
//            arrTimes.clear();
//            audioBlocks.clear();
//            alignedAudioBlocks.clear();
//            isCancelled = false;
//            packetsWidth = 0;
//
//            MainActivity.setProgressVisibility(true);
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            Realm realm = Realm.getDefaultInstance();
//            packets = realm.where(Packet.class)
//                    .equalTo("noteId", NoteManager.getInstance().getNoteId())
//                    .notEqualTo("command", "AddPage")
//                    .notEqualTo("command", "NextPage")
//                    .notEqualTo("command", "PrevPage")
//                    .notEqualTo("command", "JumpPage")
//                    .findAllSorted("id", Sort.ASCENDING); // id를 기준으로 오름차순 정렬해서 가져옴
//
//            boolean fingerDownFlag = false;
//            long downTime = 0;
//            long upTime = 0;
//
//            long endTime = 0;
//
//            for (Packet packet : packets) {
//                if (isCancelled) { // Timeline 창을 닫으면 하던 작업을 멈추고 모두 종료함
//                    realm.close();
//                    return null;
//                }
//
//                if (packet.getRelativeTimeflow() > 0) { // StartRec 패킷같은 경우에는 이 값이 0 일거임
//
//                    float lengthOfPacket;           // 패킷 width
//                    float gapOfPacket = 0;          // 앞 패킷과의 거리
//
//                    switch (packet.getCommand()) {
//
//                        case "Drawing":
//                            DrawingModel drawingModel = packet.getDrawingModel();
//
//                            if (drawingModel.getEvent().equals("Down")) {   // Touch Down 이벤트의 경우
//                                downTime = packet.getRelativeTimeflow();    // Touch Down 한 시간을 가지고 있자
//                                gap = downTime - lastPacketTime;
//                                did = packet.getId();
//
//                                // lastPacketTime의 최초값은 0임
//                                // 앞 패킷과의 거리는 Touch Down 이벤트가 발생한 시간에서 제일 최근에 지나온 패킷의 time 값 (lastPacketTime)을 빼면 됨.
//
//
//                            } else if (drawingModel.getEvent().equals("Up")) {   // Touch Up 이벤트의 경우
//                                upTime = packet.getRelativeTimeflow();           // Touch Up 한 시간을 가져옴
//                                uid = packet.getId();
//
//                                if (upTime > 0) {                                // 사실 이 값은 항상 0보다는 커서 상관 없을듯
//                                    if (upTime == downTime) {                    // UpTime과 DownTime이 같으면 S Packet임
//                                        // lengthOfPacket = PixelUtil.getInstance().convertDpToPixel(5);
//                                        if (shortPacketAlreadyDrawn && lastPacketTime == packet.getRelativeTimeflow()) {
//                                            lengthOfPacket = 0;
//                                        } else {
//                                            lengthOfPacket = PixelUtil.getInstance().convertDpToPixel(5);
//                                            shortPacketAlreadyDrawn = true;
//                                        }
//                                    } else {
//                                        lengthOfPacket = PixelUtil.getInstance().convertDpToPixel((upTime - downTime) / 25f);
//                                        // D Packet의 길이를 구하려면 UpTime에서 DownTime을 뺀 값을 25로 나누면 됨
//                                        shortPacketAlreadyDrawn = false;
//                                    }
//
//                                    if (gap > 0) {  // 이 gap 값은 Touch Down Event 때 미리 앞 패킷과 거리를 이용해 구해서 저장하고 있음
//                                        gapOfPacket = PixelUtil.getInstance().convertDpToPixel(gap / 25f);
//                                    }
//
//                                    if (lengthOfPacket > 0) {
//                                        blocks.add(new BlockData((int) lengthOfPacket,
//                                                Color.WHITE,
//                                                drawingModel.getDrawingType().equals("Pen") ? BlockType.PEN : BlockType.ERASE,
//                                                (int) gapOfPacket,downTime,upTime));
//                                        packetsWidth += lengthOfPacket + gapOfPacket;
//                                    }
//                                }
//                            }
//                            break;
//
//                        case "Object":
//                            ObjectModel objectModel = packet.getObjectModel();
//
//                            if (objectModel.getEvent().equals("Down") || objectModel.getEvent().equals("ScaleBegin")) {
//
//                                if (objectModel.getObjectType().equals("Finger") && objectModel.getEvent().equals("Down")) {
//                                    fingerDownFlag = true;
//                                }
//
//                                if (!(objectModel.getEvent().equals("ScaleBegin") && fingerDownFlag)) {
//                                    downTime = packet.getRelativeTimeflow();
//                                    gap = downTime - upTime;
//                                }
//
//                            } else if (objectModel.getEvent().equals("Up") || objectModel.getEvent().equals("ScaleEnd")) {
//                                if (!(objectModel.getEvent().equals("ScaleEnd") && fingerDownFlag)) {
//
//                                    upTime = packet.getRelativeTimeflow();
//                                    uid = packet.getId();
//
//                                    if (upTime > 0) {
//                                        if (upTime == downTime) {
//                                            if (shortPacketAlreadyDrawn && lastPacketTime == packet.getRelativeTimeflow()) {
//                                                lengthOfPacket = 0;
//                                            } else {
//                                                lengthOfPacket = PixelUtil.getInstance().convertDpToPixel(5);
//                                                shortPacketAlreadyDrawn = true;
//                                            }
//                                        } else {
//                                            lengthOfPacket = PixelUtil.getInstance().convertDpToPixel((upTime - downTime) / 25f);
//                                            shortPacketAlreadyDrawn = false;
//                                        }
//                                        if (gap > 0) {
//                                            gapOfPacket = PixelUtil.getInstance().convertDpToPixel(gap / 25f);
//                                        }
//
//
//                                        int type = BlockType.MOVE;
//                                        if (objectModel.getObjectType().equals("Finger") && objectModel.getEvent().equals("Up")) {
//
//                                        } else if (objectModel.getEvent().equals("Image")) {
//                                            type = BlockType.IMAGE;
//                                        } else if (objectModel.getEvent().equals("Shape")) {
//                                            type = BlockType.SHAPE;
//                                        } else if (objectModel.getEvent().equals("ScaleEnd")) {
//                                            type = BlockType.SCALE;
//                                        }
//
//                                        if (lengthOfPacket > 0) {
//                                            blocks.add(new BlockData((int) lengthOfPacket, Color.WHITE, type, (int) gapOfPacket, downTime, upTime));
//                                            packetsWidth += lengthOfPacket + gapOfPacket;
//                                        }
//
//                                        fingerDownFlag = false;
//                                    }
//                                }
//                            }
//                            break;
//
//                        case "PauseRec":
//                            endTime = packet.getRelativeTimeflow();
//                            break;
//
//                    }
//                    if (!(packet.getCommand().equals("PauseRec") || packet.getCommand().equals("StartRec")))
//                        lastPacketTime = packet.getRelativeTimeflow();
//                }
//            }
//
//            realm.close();
//
//            for (int i = 0; i < (PixelUtil.getInstance().convertDpToPixel(endTime / 25f)
//                    / PixelUtil.getInstance().convertDpToPixel(SECONDS_DEFAULT_LENGTH)) - 1; i++) {
//                arrTimes.add(new TimeTextBlock(i * 5000));
//            }
//
//
//            int width = (int) PixelUtil.getInstance().convertDpToPixel(endTime / 25f);
//            int remainder = width % DIVISION_SIZE;
//
//            for (int i = 0; i < width / DIVISION_SIZE; i++) {
//                alignedAudioBlocks.add(new BlockData(DIVISION_SIZE, Color.WHITE, BlockType.AUDIO, 0));
//            }
//
//            if (remainder > 0) {
//                alignedAudioBlocks.add(new BlockData(remainder, Color.WHITE, BlockType.AUDIO, 0));
//            }
//
//            return null;
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            adapter = new BlockArrayAdapter(context, blocks, (int) centerX);
//            audioAdapter = new BlockArrayaudioAdapter(context, alignedAudioBlocks, (int) centerX);
//            timeAdapter = new BlockTimeAdapter(context, arrTimes, (int) centerX);
//
//            listView.setAdapter(adapter);
//            times.setAdapter(timeAdapter);
//            audios.setAdapter(audioAdapter);
//
//            int totalWidthOfPackets = getTotalWidthOfListView(listView);
//            int totalWidthOfAudios = getTotalWidthOfListView(audios);
//            int totalWidthOfTimes = getTotalWidthOfListView(times);
//
//            int totalWidthMax = Math.max(Math.max(totalWidthOfPackets, totalWidthOfAudios), totalWidthOfTimes);
//
//            int packetsPaddingRight = totalWidthMax - totalWidthOfPackets;
//            int audiosPaddingRight = totalWidthMax - totalWidthOfAudios;
//            int timesPaddingRight = totalWidthMax - totalWidthOfTimes;
//
//            if (adapter.getCount() > 0) {
//                adapter.getItem(adapter.getCount() - 1).setRightMargin(packetsPaddingRight);
//                adapter.notifyDataSetChanged();
//            }
//
//            if (audioAdapter.getCount() > 0) {
//                audioAdapter.getItem(audioAdapter.getCount() - 1).setRightMargin(audiosPaddingRight);
//                audioAdapter.notifyDataSetChanged();
//            }
//
//            if (timeAdapter.getCount() > 0) {
//                timeAdapter.getItem(timeAdapter.getCount() - 1).setRightMargin(timesPaddingRight);
//                timeAdapter.notifyDataSetChanged();
//            }
//
//            MainActivity.setProgressVisibility(false);
//        }
//    }
//
//    public void stopAllJobs() {
//        this.isCancelled = true;
//        task = null;
//    }
//
//    public int getTotalWidthOfListView(HorizontalListView listView) {
//        ListAdapter adapter = listView.getAdapter();
//        int totalWidth = 0;
//
//        for (int i = 0; i < adapter.getCount(); i++) {
//            View view = adapter.getView(i, null, listView);
//            view.measure(
//                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                    View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//            totalWidth += view.getMeasuredWidth();
//        }
//
//        return totalWidth;
//    }
//}