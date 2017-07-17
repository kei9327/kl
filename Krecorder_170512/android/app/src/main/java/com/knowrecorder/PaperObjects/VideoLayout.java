//package com.knowrecorder.PaperObjects;
//
//import android.content.Context;
//import android.graphics.Bitmap;
//import android.media.MediaMetadataRetriever;
//import android.media.MediaPlayer;
//import android.util.AttributeSet;
//import android.util.DisplayMetrics;
//import android.util.Log;
//import android.view.WindowManager;
//import android.widget.FrameLayout;
//import android.widget.MediaController;
//import android.widget.Toast;
//
//import com.knowrecorder.Audio.AudioRecorder;
//import com.knowrecorder.KnowRecorderApplication;
//import com.knowrecorder.Managers.NoteManager;
//import com.knowrecorder.Managers.PaperManagerV2;
//import com.knowrecorder.R;
//import com.knowrecorder.Managers.Recorder;
//import com.knowrecorder.Utils.PixelUtil;
//
//import io.realm.Realm;
//
//public class VideoLayout extends FrameLayout {
//
//    private Context context;
//    private CustomVideoView videoView;
//    private VideoMoveButton moveButton;
//    private int id;
//    private int x, y;
//    private static final int VIDEO_WIDTH = (int) PixelUtil.getInstance().convertDpToPixel(480);
//    private int videoHeight = 0;
//    private String videoFilePath;
//    private int left, top, right, bottom;
//
//    public MediaController getMediaController() {
//        return mediaController;
//    }
//
//    private MediaController mediaController;
//
//    public int getDisplayWidth() {
//        return displayWidth;
//    }
//
//    private int displayWidth;
//
//    public int getDisplayHeight() {
//        return displayHeight;
//    }
//
//    private int displayHeight;
//
//    public VideoLayout(Context context) {
//        super(context);
//        this.context = context;
//        init();
//    }
//
//    public VideoLayout(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        this.context = context;
//        init();
// }
//
//    public VideoLayout(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        this.context = context;
//        init();
//    }
//
//    public CustomVideoView getVideoView() {
//        return videoView;
//    }
//
//    public void init() {
//        videoView = new CustomVideoView(context);
//        moveButton = new VideoMoveButton(context);
//        mediaController = new MediaController(KnowRecorderApplication.getContext());
//
//        left = top = right = bottom = 0;
//
//        LayoutParams buttonParams = new LayoutParams(
//                LayoutParams.WRAP_CONTENT,
//                LayoutParams.WRAP_CONTENT
//        );
//
//        LayoutParams videoParams = new LayoutParams(VIDEO_WIDTH, (int) PixelUtil.getInstance().convertDpToPixel(300));
//
//        buttonParams.width = (int) PixelUtil.getInstance().convertDpToPixel(25);
//        buttonParams.height = (int) PixelUtil.getInstance().convertDpToPixel(25);
//
//        buttonParams.leftMargin = (int) PixelUtil.getInstance().convertDpToPixel(15);
//        buttonParams.topMargin = (int) PixelUtil.getInstance().convertDpToPixel(50);
//
//        moveButton.setBackgroundResource(R.drawable.move);
//        moveButton.setLayout(this);
//        moveButton.setLayoutParams(buttonParams);
//
//        videoParams.width = (int) PixelUtil.getInstance().convertDpToPixel(430);
//        videoParams.height = (int) PixelUtil.getInstance().convertDpToPixel(300);
//
//        videoParams.leftMargin = (int) PixelUtil.getInstance().convertDpToPixel(50);
//        videoParams.topMargin = (int) PixelUtil.getInstance().convertDpToPixel(50);
//
//        videoView.setLayout(this);
//        videoView.setLayoutParams(videoParams);
//
//        addView(moveButton);
//        addView(videoView);
//
//        DisplayMetrics metrics = new DisplayMetrics();
//        ((WindowManager)context.getApplicationContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(metrics);
//
//        displayWidth = metrics.widthPixels;
//        displayHeight = metrics.heightPixels;
//
//        mediaController.setAnchorView(videoView);
//        videoView.setMediaController(mediaController);
//    }
//
//    public PaddingGroup initialize(int x, int y, String path) {
//        return initialize(x, y, path, 0, 0, 0, 0);
//    }
//
//    public PaddingGroup initialize(int x, int y, String path,
//                                   int left, int top, int right, int bottom) {
//
//        PaddingGroup paddingGroup = null;
//        try {
//            videoView.setVideoPath(path);
//
//            MediaMetadataRetriever retriever = new MediaMetadataRetriever();
//            retriever.setDataSource(path);
//            Bitmap bitmap = retriever.getFrameAtTime();
//            float videoWidth = (float) bitmap.getWidth();
//            float videoHeight = (float) bitmap.getHeight();
//            float ratio = videoWidth / videoHeight;
//
//            this.videoHeight = (int) videoHeight;
//
//            int paddingLeft = x + (int) PixelUtil.getInstance().convertDpToPixel(46 / 2);
//            int paddingRight = displayWidth - x - VIDEO_WIDTH + (int) PixelUtil.getInstance().convertDpToPixel(28);
//            int paddingBottom = displayHeight - (int) (VIDEO_WIDTH / ratio) - y - (int) PixelUtil.getInstance().convertDpToPixel(4);
//
//            if (left == 0 && top == 0 && right == 0 & bottom == 0) {
//                paddingGroup = new PaddingGroup(
//                        paddingLeft,
//                        0,
//                        paddingRight,
//                        paddingBottom
//                );
//            } else {
//                paddingGroup = new PaddingGroup(left, top, right, bottom);
//            }
//
//            mediaController.setPadding(
//                    paddingGroup.paddingLeft,
//                    paddingGroup.paddingTop,
//                    paddingGroup.paddingRight,
//                    paddingGroup.paddingBottom);
//
//            videoView.setPlayPauseListner(new CustomVideoView.PlayPauseListener() {
//
//                @Override
//                public boolean onPlay() {
//                    Log.d("PlayPauseListener", "onPlay");
////                    if (TimelineDrawer.isScrollChanged() && Recorder.getInstance().isPlaying()) {
////                        ((MainActivity) (MainActivity.getContext())).showResetPapersPopup();
////                        return false;
////                    } else {
//                        saveVideoPacket(id, "Start");
//                        return true;
////                    }
//                }
//
//                @Override
//                public boolean onPause() {
//                    Log.d("PlayPauseListener", "onPause");
////                    if (TimelineDrawer.isScrollChanged()) {
////                        ((MainActivity) (MainActivity.getContext())).showResetPapersPopup();
////                        return false;
////                    } else {
//                        saveVideoPausePacket(id, videoView.getCurrentPosition());
//                        return true;
////                    }
//                }
//
//                @Override
//                public boolean onSeekChanged(int msec) {
//                    Log.d("PlayPauseListener", "onSeekChanged");
////                    if (TimelineDrawer.isScrollChanged()) {
////                        ((MainActivity) (MainActivity.getContext())).showResetPapersPopup();
////                        return false;
////                    } else {
//                        saveVideoPacket(id, "Seek", msec);
//                        return true;
////                    }
//                }
//            });
//
//            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//                @Override
//                public void onCompletion(MediaPlayer mp) {
//                    saveVideoPacket(id, "Complete", mp.getDuration());
//                }
//            });
//        } catch (NullPointerException e) {
//            e.printStackTrace();
//        } catch (IllegalArgumentException e2) {
//            e2.printStackTrace();
//            Toast.makeText(this.context, R.string.video_not_found, Toast.LENGTH_SHORT).show();
//        }
//
//        setBounds(left, displayHeight - (bottom + videoHeight));
//        return paddingGroup;
//    }
//
//    public void adjustPosition(int touchX, int touchY) {
//        adjustPosition(touchX, touchY, false, false);
//    }
//
//    public void adjustPosition(int touchX, int touchY, boolean record) {
//        adjustPosition(touchX, touchY, record, false);
//    }
//
//    public void adjustPosition(int touchX, int touchY, boolean record, boolean b) {
//        adjustPosition(touchX, touchY, record, b, "Move");
//    }
//
//    public void adjustPosition(int touchX, int touchY, boolean record, boolean b, String event) {
//
//        if (touchX < displayWidth - VIDEO_WIDTH && touchY > (int) PixelUtil.getInstance().convertDpToPixel(35)
//                && touchX > 0 && touchY < displayHeight - videoView.getHeight()) {
//
//            int btnWidth;
//            int btnHeight;
//            int videoHeight;
//
//            if (b) {
//                btnWidth = (int) PixelUtil.getInstance().convertDpToPixel(25);
//                btnHeight = (int) PixelUtil.getInstance().convertDpToPixel(25);
//                videoHeight = this.videoHeight;
//            } else {
//                btnWidth = moveButton.getWidth();
//                btnHeight = moveButton.getHeight();
//                videoHeight = videoView.getHeight();
//            }
//
//            int videoTop = touchY - btnHeight - (btnHeight / 2);
//            int videoLeft = touchX + (int) PixelUtil.getInstance().convertDpToPixel(35) - (btnWidth / 2);
//
//            LayoutParams params = (LayoutParams) videoView.getLayoutParams();
//            params.topMargin = videoTop;
//            params.leftMargin = videoLeft;
//            videoView.setLayoutParams(params);
//
//            int btnTop = touchY - btnHeight - (btnHeight / 2);
//            int btnLeft = touchX - (btnWidth / 2);
//
//            params = (LayoutParams) moveButton.getLayoutParams();
//            params.topMargin = btnTop;
//            params.leftMargin = btnLeft;
//            moveButton.setLayoutParams(params);
//
//            int paddingLeft = touchX + (int) PixelUtil.getInstance().convertDpToPixel(46 / 2);
//            int paddingTop = 0;
//            int paddingRight = displayWidth - touchX - VIDEO_WIDTH + (int) PixelUtil.getInstance().convertDpToPixel(28);
//            int paddingBottom = displayHeight - videoHeight - touchY - (int) PixelUtil.getInstance().convertDpToPixel(65 / 2);
//
//            if (!b) {
//                mediaController.setPadding(
//                        paddingLeft,
//                        paddingTop,
//                        paddingRight,
//                        paddingBottom
//                );
//            }
//
//            if (record) {
//                saveVideoPacket(this.id, event, touchX, touchY, 0, btnTop, btnLeft, videoTop, videoLeft,
//                        paddingLeft, paddingTop, paddingRight, paddingBottom, null);
//            }
//        }
//    }
//
//    public void adjustMargin(int videoTop, int videoLeft, int btnTop, int btnLeft,
//                             int paddingLeft, int paddingTop, int paddingRight, int paddingBottom) {
//        LayoutParams params = (LayoutParams) videoView.getLayoutParams();
//        params.topMargin = videoTop;
//        params.leftMargin = videoLeft;
//        videoView.setLayoutParams(params);
//
//        params = (LayoutParams) moveButton.getLayoutParams();
//        params.topMargin = btnTop;
//        params.leftMargin = btnLeft;
//        moveButton.setLayoutParams(params);
//
//        mediaController.setPadding(
//                paddingLeft,
//                paddingTop,
//                paddingRight,
//                paddingBottom
//        );
//
//        setBounds(videoLeft, videoTop);
//    }
//
//    public VideoMoveButton getMoveButton() {
//        return moveButton;
//    }
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public void setCoordinates(int x, int y) {
//        this.x = x;
//        this.y = y;
//    }
//
//    public int getCoordX() {
//        return x;
//    }
//
//    public int getCoordY() {
//        return y;
//    }
//
//    public static void saveVideoPausePacket(int id, int msec) {
//        VideoModel videoModel = new VideoModel();
//        videoModel.setId(id);
//        videoModel.setMsec(msec);
//        videoModel.setEvent("Pause");
//
//        final Packet packet = new Packet();
//        packet.setCommand("Video");
//        if (Recorder.getInstance().isRecord()) {
//            long time = AudioRecorder.getInstance().getElapsedTime();
//
//            packet.setRelativeTimeflow(time);
//            KnowRecorderApplication.setLastRecordTime(time);
//            packet.setIsRecord(true);
//        } else {
//            if (KnowRecorderApplication.getLastRecordTime() > 0) {
//                packet.setRelativeTimeflow(KnowRecorderApplication.getLastRecordTime());
//            } else {
//                packet.setRelativeTimeflow(0);
//            }
//        }
//
//        packet.setVideoModel(videoModel);
//        //todo PaperManagerV2수정
//        packet.setPageNumber(PaperManagerV2.getInstance().getCurrentPage());
//        packet.setId(KnowRecorderApplication.primaryKeyValue.incrementAndGet());
//        packet.setNoteId(NoteManager.getInstance().getNoteId());
//
//        Realm realm = Realm.getDefaultInstance();
//        realm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                realm.copyToRealm(packet);
//            }
//        });
//        realm.close();
//    }
//
//    public static void saveVideoPacket(int id, String event) {
//        saveVideoPacket(id, event, 0, 0);
//    }
//
//    public static void saveVideoPacket(int id, String event, int msec) {
//        saveVideoPacket(id, event, 0, 0, msec, 0, 0, 0, 0);
//    }
//
//    public static void saveVideoPacket(int id, String event, int x, int y) {
//        saveVideoPacket(id, event, x, y, 0, 0, 0, 0, 0);
//    }
//
//    public static void saveVideoPacket(int id, String event, int x, int y,
//                                       int msec,
//                                       int btnTop, int btnLeft,
//                                       int videoTop, int videoLeft) {
//
//        saveVideoPacket(id, event, x, y, msec, btnTop, btnLeft, videoTop, videoLeft, 0, 0, 0, 0, null);
//    }
//
//    public static void saveVideoPacket(int id, String event, int x, int y,
//                                       int msec,
//                                       int btnTop, int btnLeft,
//                                       int videoTop, int videoLeft,
//                                       int paddingLeft, int paddingTop, int paddingRight, int paddingBottom,
//                                       String filePath) {
//        VideoModel videoModel = new VideoModel();
//        videoModel.setId(id);
//        videoModel.setTouchX(PixelUtil.getInstance().pixelToDp(x));
//        videoModel.setTouchY(PixelUtil.getInstance().pixelToDp(y));
//        videoModel.setMsec(msec);
//        videoModel.setEvent(event);
//
//        videoModel.setBtnLeft(PixelUtil.getInstance().pixelToDp(btnLeft));
//        videoModel.setBtnTop(PixelUtil.getInstance().pixelToDp(btnTop));
//
//        videoModel.setVideoLeft(PixelUtil.getInstance().pixelToDp(videoLeft));
//        videoModel.setVideoTop(PixelUtil.getInstance().pixelToDp(videoTop));
//
//        videoModel.setPaddingLeft(PixelUtil.getInstance().pixelToDp(paddingLeft));
//        videoModel.setPaddingTop(PixelUtil.getInstance().pixelToDp(paddingTop));
//        videoModel.setPaddingRight(PixelUtil.getInstance().pixelToDp(paddingRight));
//        videoModel.setPaddingBottom(PixelUtil.getInstance().pixelToDp(paddingBottom));
//
//        if (filePath != null) {
//            String videoFileName = filePath.substring(filePath.lastIndexOf("/") + 1);
//            videoModel.setFilePath(videoFileName);
//        }
//
//        final Packet packet = new Packet();
//        packet.setCommand("Video");
//        packet.setTimeStamp(System.currentTimeMillis());
//
//        if (Recorder.getInstance().isRecord()) {
//            long relativeTimeflow = AudioRecorder.getInstance().getElapsedTime();
//            packet.setRelativeTimeflow(relativeTimeflow);
//
//            KnowRecorderApplication.setLastRecordTime(relativeTimeflow);
//            packet.setIsRecord(true);
//        } else {
//            if (KnowRecorderApplication.getLastRecordTime() > 0) {
//                packet.setRelativeTimeflow(KnowRecorderApplication.getLastRecordTime());
//            }
//        }
//
//        packet.setVideoModel(videoModel);
//        //todo PaperManagerV2수정
//        packet.setPageNumber(PaperManagerV2.getInstance().getCurrentPage());
//        packet.setId(KnowRecorderApplication.primaryKeyValue.incrementAndGet());
//        packet.setNoteId(NoteManager.getInstance().getNoteId());
//
//        Realm realm = Realm.getDefaultInstance();
//        realm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                realm.copyToRealm(packet);
//            }
//        });
//        realm.close();
//    }
//
//    public String getVideoFilePath() {
//        return videoFilePath;
//    }
//
//    public void setVideoFilePath(String videoFilePath) {
//        this.videoFilePath = videoFilePath;
//    }
//
//    public class PaddingGroup {
//        public PaddingGroup(int left, int top, int right, int bottom) {
//            paddingLeft = left;
//            paddingTop = top;
//            paddingRight = right;
//            paddingBottom = bottom;
//        }
//
//        public int paddingLeft;
//        public int paddingTop;
//        public int paddingRight;
//        public int paddingBottom;
//    }
//
//    public boolean isInThisArea(float x, float y) {
//        return isInThisArea(x, y, false);
//    }
//
//    public boolean isInThisArea(float x, float y, boolean isAutoLoad) {
//        int left, top, right, bottom;
//
//        if (isAutoLoad) {
//            left = this.left;
//            top = this.top;
//            right = this.right;
//            bottom = this.bottom;
//        } else {
//            left = videoView.getLeft();
//            top = videoView.getTop();
//            right = videoView.getRight();
//            bottom = videoView.getBottom();
//        }
//
//        if (x <= right && x >= left
//                && y <= bottom && y >= top) {
//            return true;
//        }
//
//        return false;
//    }
//
//    private void setBounds(int left, int top) {
//        this.left = left;
//        this.top = top;
//        this.right = left + VIDEO_WIDTH;
//        this.bottom = top + videoHeight;
//    }
//}