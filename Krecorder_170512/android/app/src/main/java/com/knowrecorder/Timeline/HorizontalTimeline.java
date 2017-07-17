//    package com.knowrecorder.Timeline;
//
//import android.content.Context;
//import android.content.res.Resources;
//import android.graphics.Color;
//import android.os.AsyncTask;
//import android.util.AttributeSet;
//import android.util.DisplayMetrics;
//import android.view.WindowManager;
//import android.widget.LinearLayout;
//
//import com.knowrecorder.MainActivity;
//import com.knowrecorder.Managers.NoteManager;
//import com.knowrecorder.Managers.PaperManagerV2;
//import com.knowrecorder.Utils.PixelUtil;
//
//import io.realm.Realm;
//import io.realm.RealmConfiguration;
//import io.realm.RealmResults;
//import io.realm.Sort;
//
//@Deprecated
//public class HorizontalTimeline extends LinearLayout {
//
//    private Context context;
//    private float displayWidth;
//    float centerX;
//    float DP6_TO_PIXEL;
//    float DP2_TO_PIXEL;
//    float lastPacketX = 0;
//    long downTime = 0;
//    long upTime = 0;
//    long lastPacketTime = 0;
//    long gap = 0;
//    private final int SECONDS_DEFAULT_LENGTH = 200;
//    private HorizontalTimeline horizontalTimeline = this;
//    Realm realmInMemory = null;
//    RealmConfiguration inMemoryConfig = null;
//    RealmResults<Packet> packets;
//
//    private static boolean isScrollChanged = false;
//
//    public HorizontalTimeline(Context context) {
//        super(context);
//        initView(context);
//    }
//
//    public HorizontalTimeline(Context context, AttributeSet attrs) {
//        this(context, attrs, 0);
//        initView(context);
//    }
//
//    public HorizontalTimeline(Context context, AttributeSet attrs, int defStyle) {
//        super(context, attrs, defStyle);
//        initView(context);
//    }
//
//    public static boolean isScrollChanged() {
//        return isScrollChanged;
//    }
//
//    public static void setScrollChanged(boolean isScrollChanged) {
//        HorizontalTimeline.isScrollChanged = isScrollChanged;
//    }
//
//    public void drawTimeline() {
//        new DrawingTask().execute();
//    }
//
//    private void initView(Context context) {
//        this.context = context;
//        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
//        DisplayMetrics metrics = new DisplayMetrics();
//        wm.getDefaultDisplay().getMetrics(metrics);
//        this.displayWidth = metrics.widthPixels;
//        centerX = this.displayWidth / 2;
//        DP6_TO_PIXEL = convertDpToPixel(6);
//        DP2_TO_PIXEL = convertDpToPixel(2);
//        this.setPadding((int) centerX + (int)convertDpToPixel(3), (int) convertDpToPixel(5), (int) centerX + (int)convertDpToPixel(7), 0);
//    }
//
//    @Override
//    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
//        super.onLayout(changed, left, top, right, bottom);
//    }
//
//    private float convertDpToPixel(float dp) {
//        Resources resources = context.getResources();
//        DisplayMetrics metrics = resources.getDisplayMetrics();
//        float px = dp * (metrics.densityDpi / 160f);
//        return px;
//    }
//
//
//    private class DrawingTask extends AsyncTask<Void, BlockData, Void> {
//        @Override
//        protected void onPreExecute() {
//            lastPacketX = centerX + PixelUtil.getInstance().convertDpToPixel(4.5f); // 그리기 시작점 초기화
//            downTime = 0;
//            upTime = 0;
//            gap = 0;
//            lastPacketTime = 0;
//            packets = null;
//
//            removeAllViewsInLayout();
//            MainActivity.setProgressVisibility(true);
//        }
//
//        @Override
//        protected Void doInBackground(Void... params) {
//            Realm realm = Realm.getDefaultInstance();
//            packets = realm.where(Packet.class)
//                    .equalTo("noteId", NoteManager.getInstance().getNoteId())   // 현재 노트 아이디에 해당하는 패킷만 가져옴
//                    .findAllSorted("id", Sort.ASCENDING); // id를 기준으로 오름차순 정렬해서 가져옴
//
//            for (Packet packet : packets) {
//
//                if (packet.getRelativeTimeflow() > 0) {
//
//                    float lengthOfPacket;
//                    float gapOfPacket = 0;
//                    int color;
//                    BlockData blockData;
//
//                    switch (packet.getCommand()) {
//
//                        case "Drawing":
//                            DrawingModel drawingModel = packet.getDrawingModel();
//
//                            if (drawingModel.getEvent().equals("Down")) {
//                                downTime = packet.getRelativeTimeflow();
//                                gap = downTime - lastPacketTime;
//
//                            } else if (drawingModel.getEvent().equals("Up")) {
//                                upTime = packet.getRelativeTimeflow();
//
//                                if (upTime > 0) {
//                                    if (upTime - downTime == 0) {
//                                        lengthOfPacket = PixelUtil.getInstance().convertDpToPixel(5);
//                                    } else {
//                                        lengthOfPacket = PixelUtil.getInstance().convertDpToPixel(((float) (upTime - downTime) / 25f));
//                                    }
//
//                                    if (gap > 0) {
//                                        gapOfPacket = PixelUtil.getInstance().convertDpToPixel(((float) gap / 25f));
//                                    }
//
//                                    if (packet.getPageNumber() != PaperManagerV2.getInstance().getCurrentPage()) {
//                                        color = Color.LTGRAY;
//                                    } else {
//                                        color = Color.WHITE;
//                                    }
//
//                                    blockData = new BlockData();
//                                    blockData.gap = (int) gapOfPacket;
//                                    blockData.width = (int) lengthOfPacket;
//                                    blockData.color = color;
//
//                                    if (drawingModel.getDrawingType().equals("Pen")) {
//                                        blockData.type = BlockType.PEN;
//                                    } else if (drawingModel.getDrawingType().equals("Eraser")) {
//                                        blockData.type = BlockType.ERASE;
//                                    }
//
//                                    publishProgress(blockData);
//                                }
//                            }
//                            break;
//
//                        case "Object":
//                            ObjectModel objectModel = packet.getObjectModel();
//
//                            if (objectModel.getEvent().equals("Down") || objectModel.getEvent().equals("ScaleBegin")) {
//                                downTime = packet.getRelativeTimeflow();
//                                gap = downTime - upTime;
//
//                            } else if (objectModel.getEvent().equals("Up") || objectModel.getEvent().equals("ScaleEnd")) {
//                                upTime = packet.getRelativeTimeflow();
//
//                                if (upTime > 0) {
//
//                                    lengthOfPacket = PixelUtil.getInstance().convertDpToPixel(((float) (upTime - downTime) / 25f));
//                                    if (gap > 0) {
//                                        gapOfPacket = PixelUtil.getInstance().convertDpToPixel(((float) gap / 25f));
//                                    }
//
//                                    if (packet.getPageNumber() != PaperManagerV2.getInstance().getCurrentPage()) {
//                                        color = Color.LTGRAY;
//                                    } else {
//                                        color = Color.WHITE;
//                                    }
//
//                                    blockData = new BlockData();
//                                    blockData.gap = (int) gapOfPacket;
//                                    blockData.width = (int) lengthOfPacket;
//                                    blockData.color = color;
//
//                                    if (objectModel.getObjectType().equals("Finger")) {
//                                        if (objectModel.getEvent().equals("Up")) {
//                                            blockData.type = BlockType.MOVE;
//                                        } else if (objectModel.getEvent().equals("ScaleEnd")) {
//                                            blockData.type = BlockType.SCALE;
//                                        }
//                                    } else if (objectModel.getEvent().equals("Image")) {
//                                        blockData.type = BlockType.IMAGE;
//                                    } else if (objectModel.getEvent().equals("Shape")) {
//                                        blockData.type = BlockType.SHAPE;
//                                    }
//
//                                    publishProgress(blockData);
//                                }
//                            } else if (objectModel.getEvent().equals("Delete") ||
//                                    objectModel.getObjectType().equals("PDF")) {
//                                gap = packet.getRelativeTimeflow() - lastPacketTime;
//
//                                lengthOfPacket = PixelUtil.getInstance().convertDpToPixel(5);
//                                if (gap > 0) {
//                                    gapOfPacket = PixelUtil.getInstance().convertDpToPixel(((float) gap / 25f));
//                                }
//
//                                if (packet.getPageNumber() != PaperManagerV2.getInstance().getCurrentPage()) {
//                                    color = Color.LTGRAY;
//                                } else {
//                                    color = Color.WHITE;
//                                }
//
//                                blockData = new BlockData();
//                                blockData.gap = (int) gapOfPacket;
//                                blockData.width = (int) lengthOfPacket;
//                                blockData.color = color;
//
//                                if (objectModel.getEvent().equals("Delete")) {
//                                    blockData.type = BlockType.REMOVE;
//                                } else if (objectModel.getEvent().equals("PDF")) {
//                                    blockData.type = BlockType.PDF;
//                                }
//
//                                publishProgress(blockData);
//                                break;
//                            }
//                            break;
//
//                        case "AddPage":
//                        case "PrevPage":
//                        case "NextPage":
//                            gap = packet.getRelativeTimeflow() - lastPacketTime;
//
//                            lengthOfPacket = PixelUtil.getInstance().convertDpToPixel(5);
//                            if (gap > 0) {
//                                gapOfPacket = PixelUtil.getInstance().convertDpToPixel(((float) gap / 25f));
//                            }
//
//                            if (packet.getPageNumber() != PaperManagerV2.getInstance().getCurrentPage()) {
//                                color = Color.LTGRAY;
//                            } else {
//                                color = Color.WHITE;
//                            }
//
//                            blockData = new BlockData();
//                            blockData.gap = (int) gapOfPacket;
//                            blockData.width = (int) lengthOfPacket;
//                            blockData.color = color;
//
//                            if (packet.getCommand().equals("AddPage")) {
//                                blockData.type = BlockType.NEW_PAGE;
//                            } else if (packet.getCommand().equals("PrevPage")) {
//                                blockData.type = BlockType.PREV_PAGE;
//                            } else if (packet.getCommand().equals("NextPage")) {
//                                blockData.type = BlockType.NEXT_PAGE;
//                            }
//
//                            publishProgress(blockData);
//                            break;
//
//                        case "Text":
//                        case "Video":
//
//                            String event = "";
//
//                            if (packet.getCommand().equals("Text")) {
//                                TextModel textModel = packet.getTextModel();
//                                event = textModel.getEvent();
//                            } else if (packet.getCommand().equals("Video")) {
//                                VideoModel videoModel = packet.getVideoModel();
//                                event = videoModel.getEvent();
//                            }
//
//                            if (event.equals("Move_Down")) {
//                                downTime = packet.getRelativeTimeflow();
//                                gap = downTime - lastPacketTime;
//
//                            } else if (event.equals("Move_Up")) {
//                                upTime = packet.getRelativeTimeflow();
//
//                                if (upTime > 0) {
//                                    if (upTime - downTime == 0) {
//                                        lengthOfPacket = PixelUtil.getInstance().convertDpToPixel(5);
//                                    } else {
//                                        lengthOfPacket = PixelUtil.getInstance().convertDpToPixel(((float) (upTime - downTime) / 25f));
//                                    }
//
//                                    if (gap > 0) {
//                                        gapOfPacket = PixelUtil.getInstance().convertDpToPixel(((float) gap / 25f));
//                                    }
//
//                                    if (packet.getPageNumber() != PaperManagerV2.getInstance().getCurrentPage()) {
//                                        color = Color.LTGRAY;
//                                    } else {
//                                        color = Color.WHITE;
//                                    }
//
//                                    blockData = new BlockData();
//                                    blockData.gap = (int) gapOfPacket;
//                                    blockData.width = (int) lengthOfPacket;
//                                    blockData.color = color;
//
//                                    if (packet.getCommand().equals("Text")) {
//                                        blockData.type = BlockType.TEXT;
//                                    } else if (packet.getCommand().equals("Video")) {
//                                        blockData.type = BlockType.VIDEO;
//                                    }
//
//                                    publishProgress(blockData);
//                                }
//                            } else if (event.equals("Down") || event.equals("Text")) {
//                                gap = packet.getRelativeTimeflow() - lastPacketTime;
//
//                                lengthOfPacket = PixelUtil.getInstance().convertDpToPixel(5);
//                                if (gap > 0) {
//                                    gapOfPacket = PixelUtil.getInstance().convertDpToPixel(((float) gap / 25f));
//                                }
//
//                                if (packet.getPageNumber() != PaperManagerV2.getInstance().getCurrentPage()) {
//                                    color = Color.LTGRAY;
//                                } else {
//                                    color = Color.WHITE;
//                                }
//
//                                blockData = new BlockData();
//                                blockData.gap = (int) gapOfPacket;
//                                blockData.width = (int) lengthOfPacket;
//                                blockData.color = color;
//
//                                if (packet.getCommand().equals("Text")) {
//                                    blockData.type = BlockType.TEXT;
//                                } else if (packet.getCommand().equals("Video")) {
//                                    blockData.type = BlockType.VIDEO;
//                                }
//
//                                publishProgress(blockData);
//                            }
//                            break;
//
//                        case "StartRec":
//                        case "PauseRec":
////                            gap = packet.getRelativeTimeflow() - lastPacketTime;
////
////                            lengthOfPacket = PixelUtil.getInstance().convertDpToPixel(5);
////                            if (gap > 0) {
////                                gapOfPacket = PixelUtil.getInstance().convertDpToPixel(((float) gap / 25f));
////                            }
////
////                            if (packet.getPageNumber() != OCPaperManager.getInstance().getCurrentPage()) {
////                                color = Color.LTGRAY;
////                            } else {
////                                color = Color.WHITE;
////                            }
////
////                            blockData = new BlockData();
////                            blockData.gap = (int) gapOfPacket;
////                            blockData.width = (int) lengthOfPacket;
////                            blockData.color = color;
////                            blockData.type = BlockType.START_PAUSE;
////
////                            publishProgress(blockData);
//                            break;
//                    }
//
//                    lastPacketTime = packet.getRelativeTimeflow();
//                }
//            }
//
//            realm.close();
//
//            return null;
//        }
//
//        @Override
//        protected void onProgressUpdate(BlockData... blockDatas) {
//            BlockData blockData = blockDatas[0];
//            TimelineBlock timelineBlock = new TimelineBlock(getContext());
//            timelineBlock.drawBlock(blockData.width, blockData.color, blockData.type);
//
//            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(blockData.width, (int) PixelUtil.getInstance().convertDpToPixel(30));
//            layoutParams.leftMargin = blockData.gap;
//
//            timelineBlock.setLayoutParams(layoutParams);
//
//            addView(timelineBlock);
//        }
//
//        @Override
//        protected void onPostExecute(Void aVoid) {
//            MainActivity.setProgressVisibility(false);
//        }
//    }
//
//    private class BlockData {
//        public int width;
//        public int color;
//        public int gap;
//        public int type;
//    }
//}