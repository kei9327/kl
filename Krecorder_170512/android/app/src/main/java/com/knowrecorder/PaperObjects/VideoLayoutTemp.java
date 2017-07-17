//package com.knowrecorder.PaperObjects;
//
//import android.content.Context;
//import android.content.res.Resources;
//import android.graphics.Color;
//import android.media.MediaPlayer;
//import android.net.Uri;
//import android.os.Handler;
//import android.os.Message;
//import android.util.AttributeSet;
//import android.util.Log;
//import android.util.TypedValue;
//import android.view.Gravity;
//import android.view.LayoutInflater;
//import android.view.MotionEvent;
//import android.view.View;
//import android.widget.FrameLayout;
//import android.widget.ImageView;
//import android.widget.SeekBar;
//import android.widget.TextView;
//
//import com.knowrecorder.Audio.AudioRecorder;
//import com.knowrecorder.KnowRecorderApplication;
//import com.knowrecorder.Managers.NoteManager;
//import com.knowrecorder.Managers.PaperManagerV2;
//import com.knowrecorder.Managers.Recorder;
//import com.knowrecorder.R;
//import com.knowrecorder.Toolbox.Toolbox;
//import com.knowrecorder.Utils.PixelUtil;
//import com.knowrecorder.Utils.TimeConverter;
//import com.yqritc.scalablevideoview.ScalableType;
//
//import java.io.IOException;
//import java.util.Timer;
//import java.util.TimerTask;
//
//import io.realm.Realm;
//
///**
// * Created by we160303 on 2016-12-28.
// */
//
//public class VideoLayoutTemp extends FrameLayout {
//    private final String TAG = "VideoLayout";
//    private final int MAX_VOLUME = 8;
//    private final int CLOSE_WAIT_TIME = 3000;
//    private Context mContext;
//
//    private CustomScalableVideoView mVideoView;
//    private View controller;
//    private SeekBar videoSeekBar, volumeRatingSeek;
//    private ImageView playBtn, prevBtn;
//
//    private Handler updateHandler = new Handler();
//    private boolean seekToChanged = false;
//
//    private int[] arrVolumeId = new int[]{ R.id.volume1, R.id.volume2, R.id.volume3, R.id.volume4
//            , R.id.volume5, R.id.volume6, R.id.volume7, R.id.volume8 };
//    private View[] arrVolume = new View[8];
//
//    private Timer controllerTimer;
//
//    private int id;
//
//    private float mScaleFactor = 1.f;
//
//    protected float startX;
//    protected float startY;
//    protected float endX;
//    protected float endY;
//
//    protected float originStartX;
//    protected float originStartY;
//    protected float originEndX;
//    protected float originEndY;
//
//    private float mOriginWidth;
//    private float mOriginHeight;
//    private float mWidth;
//    private float mHeight;
//
//    private VideoEventListener mListener;
//
//    private boolean isPlayer;
//    private boolean isVideoPlaying;
//
//    private float mPosX = 0;
//    private float mPosY = 0;
//    private float mLastTouchX;
//    private float mLastTouchY;
//
//    public VideoLayoutTemp(Context context, boolean isPlayer) {
//        super(context);
//        this.mContext = context;
//        this.isPlayer = isPlayer;
//        initVIew();
//    }
//
//    public VideoLayoutTemp(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        this.mContext = context;
//
//        initVIew();
//    }
//
//    public void initVIew(){
//        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//
//
//        mVideoView = new CustomScalableVideoView(mContext);
//
//        controller = inflater.inflate(R.layout.video_layout_controller, null);
//
//        addView(mVideoView, 0, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
//        addView(controller, 1, new FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, (int)convertDpToPixel(40), Gravity.BOTTOM));
//
//        mVideoView.setDrawingCacheEnabled(true);
//        controller.setDrawingCacheEnabled(true);
//
//        mVideoView.setCallback(new CustomScalableVideoView.VideoStateCallback() {
//            @Override
//            public void onPlayed(int msec) {
//                if (mListener != null)
//                    mListener.onPlaying(msec);
//            }
//        });
//
//        playBtn = (ImageView) findViewById(R.id.btn_play);
//        prevBtn = (ImageView) findViewById(R.id.btn_prev);
//
//        videoSeekBar = (SeekBar) findViewById(R.id.seekBar);
//        volumeRatingSeek = (SeekBar) findViewById(R.id.volume_rating_seek);
//
//        Uri uri = Uri.parse("");
//        try {
//            mVideoView.setDataSource(mContext,uri);
//            mVideoView.start();
//            mVideoView.stop();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//
//        prevBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                mVideoView.seekTo(0);
//                pauseVideo();
//            }
//        });
//        playBtn.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if(mVideoView.isPlaying()) {
//
//                    pauseVideo();
//                }else {
//
//                    playVideo();
//                }
//            }
//        });
//
//        int position = 0;
//        for(int id:arrVolumeId)
//        {
//            View v = (View)findViewById(id);
//            arrVolume[position++] = v;
//        }
//
//
//        videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                controllerShow();
//                if(seekToChanged) {
//                    Log.d(TAG, "onProgressChanged : " + progress);
//
////                    if(Recorder.getInstance().isRecord())
////                        saveVideoPacket(id, "Seek", progress);
//
//                    mVideoView.seekTo(progress);
//                }
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//                Log.d(TAG, "onStartTrackingTouch");
//                seekToChanged = true;
//                isVideoPlaying = mVideoView.isPlaying();
//                pauseVideo();
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//                Log.d(TAG, "onStopTrackingTouch");
//                seekToChanged = false;
//                if(isVideoPlaying)
//                    playVideo();
//            }
//        });
//
//
//        volumeRatingSeek.setMax(8);
//        volumeRatingSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
//            @Override
//            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//                setVolume(progress);
//            }
//
//            @Override
//            public void onStartTrackingTouch(SeekBar seekBar) {
//
//            }
//
//            @Override
//            public void onStopTrackingTouch(SeekBar seekBar) {
//
//            }
//        });
//
//        setBackgroundColor(Color.parseColor("#000000"));
//        setVolume(4);
//    }
//
//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//        Log.d("TestLog", "width : " + w + "   height : " + h);
//        mWidth = w;
//        mHeight = h;
//        mOriginWidth = w;
//        mOriginHeight = h;
//        updateCoordinate();
//        mListener.onVideoDone();
//    }
//
//    @Override
//    public boolean onTouchEvent(MotionEvent event) {
//        if(Toolbox.getInstance().getToolType() == Toolbox.Tooltype.FINGER)
//            controllerShow();
//        printCoodinates();
//
//        return false;
//    }
//
//    public void setVideoEvent(VideoEventListener listener) {
//        this.mListener = listener;
//    }
//
//    public void setPlayVideo(String filePath) {
//        try {
////            playVideo();
////            pauseVideo();
//            mVideoView.setDataSource(filePath);
//            mVideoView.setScalableType(ScalableType.FIT_CENTER);
//
//            mVideoView.prepare(new MediaPlayer.OnPreparedListener(){
//                @Override
//                public void onPrepared(MediaPlayer mp) {
//
//                    mVideoView.start();
//                    mVideoView.pause();
//
//                    long finalTime = mp.getDuration();
//                    TextView tvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
//                    tvTotalTime.setText(TimeConverter.convertMillisToStringFormat(finalTime));
//                    videoSeekBar.setMax((int) finalTime);
//                    videoSeekBar.setProgress(0);
//                    updateHandler.postDelayed(updateVideoTime, 100);
//                    controllerShow();
//                }
//            });
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
//            @Override
//            public void onCompletion(MediaPlayer mp) {
//                pauseVideo();
//            }
//        });
//    }
//
//    private Runnable updateVideoTime = new Runnable(){
//        public void run(){
//            try {
//                long currentPosition = mVideoView.getCurrentPosition();
//
//                TextView tvRealTime = (TextView) findViewById(R.id.tvRealTime);
//                tvRealTime.setText(TimeConverter.convertMillisToStringFormat(currentPosition));
//
//                videoSeekBar.setProgress((int) currentPosition);
//                updateHandler.postDelayed(this, 100);
//            }catch(NullPointerException npe){
//                npe.printStackTrace();
//            }
//
//        }
//    };
//
//    public int[] getVideoViewPosition() {
//        int[] position = new int[2];
//
//        mVideoView.getLocationOnScreen(position);
//
//        return position;
//    }
//
//    public void updateCoordinate() {
//        setCoordinates(this.startX, this.startY, this.startX+mWidth, this.startY+mHeight);
//    }
//
//    public void playVideo(){
//        if(Recorder.getInstance().isRecord())
//            saveVideoPacket(id, "Start");
//        controllerShow();
//        playBtn.setImageResource(R.drawable.btn_video_pause_s);
//        mVideoView.requestFocus();
//        mVideoView.start();
//
//        mListener.onStart();
//
//    }
//
//    public void pauseVideo()
//    {
//        if(Recorder.getInstance().isRecord())
//            saveVideoPausePacket(id, mVideoView.getCurrentPosition());
//
//        controllerShow();
//        playBtn.setImageResource(R.drawable.btn_video_play_s);
//        mVideoView.pause();
//        mListener.onPause();
//    }
//
//    public void seekTo(int seek){
//        mVideoView.seekTo(seek);
//    }
//
//    public float convertDpToPixel(float dp) {
//        Resources resources = mContext.getResources();
//        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
//    }
//
//    public void setVolume(float volume){
//        int count = 0;
//        controllerShow();
//        for(View volumeView:arrVolume){
//            if(count++ < volume)
//                volumeView.setBackgroundColor(Color.parseColor("#96c81e"));
//            else
//                volumeView.setBackgroundColor(Color.parseColor("#808080"));
//        }
//        float currentVolume = (float) (1 - (Math.log(MAX_VOLUME-volume) / Math.log(MAX_VOLUME)));
//        mVideoView.setVolume(currentVolume, currentVolume);
//    }
//
//    private void controllerShow(){
//
//        if(isPlayer || Recorder.getInstance().isPlaying()) {
//            Log.d("VideoLayout","hide");
//            controller.setVisibility(View.GONE);
//            return;
//        }
//
//        controller.setVisibility(View.VISIBLE);
//        try {
//            controllerTimer.cancel();
//            controllerTimer.purge();
//        } catch (NullPointerException ignored) {
//
//        }
//        controllerTimer = new Timer();
//        controllerTimer.schedule(new TimerHidingTask(), CLOSE_WAIT_TIME);
//    }
//
//
//    class TimerHidingTask extends TimerTask {
//
//        @Override
//        public void run() {
//            mHandler.sendEmptyMessage(0);
//        }
//    }
//
//    Handler mHandler = new Handler(){
//        @Override
//        public void handleMessage(Message msg) {
//            if(msg.what == 0){
//                controller.setVisibility(View.GONE);
//            }
//        }
//    };
//
//    public boolean isVideoPlay() { return mVideoView.isPlaying() ; }
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//
//    public void setCoordinates(float startX, float startY, float endX, float endY) {
//        this.startX = startX;
//        this.startY = startY;
//        this.endX = endX;
//        this.endY = endY;
//        printCoodinates();
//    }
//
//    public void setOriginCoordinates(float startX, float startY, float endX, float endY) {
//        this.originStartX = startX;
//        this.originStartY = startY;
//        this.originEndX = endX;
//        this.originEndY = endY;
//    }
//
//    public float[] getCoordinates() {
//        float[] pts = new float[]{startX, startY, endX, endY};
//        return pts;
//    }
//
//    public float[] getOriginCoordinates() {
//        float[] pts = new float[]{originStartX, originStartY, originEndX, originEndY
//        };
//        return pts;
//    }
//
//    public boolean isInThisArea(float pointX, float pointY, boolean isAutoLoad) {
//
//        printCoodinates();
//        Log.d("videos좌표", "position X: " + pointX + "posotion Y : " + pointY);
//
//        if (startX <= pointX && startY <= pointY
//                && endX >= pointX && endY >= pointY) {
//            return true;
//        } else {
//            return false;
//        }
//    }
//
//    public void setVideoXY(float x, float y){
//        this.startX += x;
//        this.startY += y;
//
//        setX((int)startX);
//        setY((int)startY);
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
//        packet.setId(KnowRecorderApplication.primaryKeyValue.incrementAndGet());
//        packet.setVideoModel(videoModel);
//        packet.setPageNumber(PaperManagerV2.getInstance().getCurrentPage());
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
//            if (KnowRecorderApplication.getCurrentTimeStame() > 0) {
//                packet.setRelativeTimeflow(KnowRecorderApplication.getCurrentTimeStame());
//            }
//        }
//
//        packet.setId(KnowRecorderApplication.primaryKeyValue.incrementAndGet());
//        packet.setVideoModel(videoModel);
//        packet.setPageNumber(PaperManagerV2.getInstance().getCurrentPage());
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
//    public void setLastTouch(float touchX, float touchY) {
//        this.mLastTouchX = touchX;
//        this.mLastTouchY = touchY;
//    }
//    public void setScaleFactor(float scaleFactor){
//        mScaleFactor = scaleFactor;
//    }
//    public interface VideoEventListener {
//        void onStart();
//        void onPause();
//        void onPlaying(int msec);
//        void onVideoDone();
//    }
//
//    public float getOriginWidth(){ return mOriginWidth ; }
//    public float getOriginHeight(){ return mOriginHeight ; }
//    public float getMwidth(){ return mWidth ; }
//    public float getMheight(){ return mHeight; }
//
//    public void setMwidth(float w){
//        this.mWidth = w ;
//    }
//    public void setmHeight(float h){ this.mHeight = h ; }
//
//    public void printCoodinates(){
//        Log.d("videos좌표 ", "startX : "+ this.startX +
//                           "  startY : "+ this.startY +
//                           "  endX : "+ this.endX +
//                           "  endY : "+ this.endY);
//    }
//
//}
