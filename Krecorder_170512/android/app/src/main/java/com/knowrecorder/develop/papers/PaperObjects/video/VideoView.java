package com.knowrecorder.develop.papers.PaperObjects.video;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Path;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.knowrecorder.R;
import com.knowrecorder.Toolbox.Toolbox;
import com.knowrecorder.Utils.TimeConverter;
import com.knowrecorder.develop.ProcessStateModel;
import com.knowrecorder.develop.file.FilePath;
import com.knowrecorder.develop.model.packetHolder.ObjectControllPacket;
import com.knowrecorder.develop.papers.DrawingPanel;
import com.knowrecorder.develop.papers.PaperObjects.ViewGroupObject;
import com.knowrecorder.develop.utils.PacketUtil;
import com.yqritc.scalablevideoview.ScalableType;
import com.yqritc.scalablevideoview.ScalableVideoView;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by we160303 on 2017-02-07.
 */

public class VideoView extends ViewGroupObject {

    private final String TAG = "VideoLayout";
    private final int MAX_VOLUME = 8;
    private final int CLOSE_WAIT_TIME = 2000;

    private Context mContext;
    private Path mPath;

    private long totalPlayTime;

    private ScalableVideoView mVideoView;
    private View controller;
    private SeekBar videoSeekBar, volumeRatingSeek;
    private ImageView playBtn, prevBtn;
    private float initProgress = 0f;

    private Handler updateHandler = new Handler();
    private boolean seekToChanged = false;

    private int[] arrVolumeId = new int[]{ R.id.volume1, R.id.volume2, R.id.volume3, R.id.volume4
            , R.id.volume5, R.id.volume6, R.id.volume7, R.id.volume8 };
    private View[] arrVolume = new View[8];

    private Timer controllerTimer;

    private boolean videoFileError = false;

    public VideoView(Context context) {
        super(context);
        this.mContext = context;
        mPath = new Path();
        isMovable = true;
        isScalable = true;
        maxScale = 1.8f;
        minScale = 0.7f;
    }

    public VideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    public void initVIew(long id, float posX, float posY, float width, float height, float initProgress) {
        mid = id;
        this.initProgress = initProgress;
        LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        mVideoView = new ScalableVideoView(mContext);
        controller = inflater.inflate(R.layout.video_layout_controller, null);
        addView(mVideoView, 0, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
        addView(controller, 1, new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, (int)convertDpToPixel(40), Gravity.BOTTOM));

        mPosX = posX;
        mPosY = posY;

        drawingRectangle(width, height);
        moveTo(mPosX, mPosY);

        playBtn = (ImageView) findViewById(R.id.btn_play);
        prevBtn = (ImageView) findViewById(R.id.btn_prev);

        videoSeekBar = (SeekBar) findViewById(R.id.seekBar);
        volumeRatingSeek = (SeekBar) findViewById(R.id.volume_rating_seek);

        controller.setVisibility(GONE);

        Uri uri = Uri.parse("");
        try {
            mVideoView.setDataSource(mContext,uri);
            mVideoView.start();
            mVideoView.stop();
        } catch (IOException e) {
            e.printStackTrace();
        }

        prevBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoView.seekTo(0);
                pauseVideo();
            }
        });
        playBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mVideoView.isPlaying()) {
                    if (ProcessStateModel.getInstanse().isRecording()){
                        makePausePacket();
                    }
                    pauseVideo();
                    controllerShow();
                }else {
                    if (ProcessStateModel.getInstanse().isRecording()){
                       makePlayPacket();
                    }
                    playVideo();
                    controllerShow();
                }
            }
        });

        mVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                if(Toolbox.getInstance().getToolType() == Toolbox.Tooltype.FINGER)
                    controllerShow();

                return false;
            }
        });

        int position = 0;
        for(int volumeId : arrVolumeId)
        {
            View v = (View)findViewById(volumeId);
            arrVolume[position++] = v;
        }


        videoSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                controllerShow();
                if(seekToChanged) {
                    if(ProcessStateModel.getInstanse().isRecording()){
                        ObjectControllPacket controllPacket = new ObjectControllPacket
                                .ObjectControllPacketBuilder()
                                .setType("videoprogressing")
                                .setTarget(mid)
                                .setAction(MotionEvent.ACTION_MOVE)
                                .setVideoProgress((float)progress / (float)totalPlayTime)
                                .build();
                        PacketUtil.makePacket(tempMid, controllPacket);
                    }

                    Log.d(TAG, "onProgressChanged");
                    mVideoView.seekTo(progress);

                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStartTrackingTouch");

                if(ProcessStateModel.getInstanse().isRecording()){
                    tempMid = DrawingPanel.mid.incrementAndGet();
                    ObjectControllPacket controllPacket = new ObjectControllPacket
                            .ObjectControllPacketBuilder()
                            .setType("videoprogressing")
                            .setTarget(mid)
                            .setAction(MotionEvent.ACTION_DOWN)
                            .setVideoProgress(seekBar.getProgress())
                            .build();
                    PacketUtil.makePacket(tempMid, controllPacket);
                }

                seekToChanged = true;
                pauseVideo();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                Log.d(TAG, "onStopTrackingTouch");
                if(ProcessStateModel.getInstanse().isRecording()){
                    ObjectControllPacket controllPacket = new ObjectControllPacket
                            .ObjectControllPacketBuilder()
                            .setType("videoprogressing")
                            .setTarget(mid)
                            .setAction(MotionEvent.ACTION_UP)
                            .setVideoProgress(seekBar.getProgress())
                            .build();
                    PacketUtil.makePacket(tempMid, controllPacket);
                }
                seekToChanged = false;
            }
        });


        volumeRatingSeek.setMax(8);
        volumeRatingSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(ProcessStateModel.getInstanse().isRecording()){
                    ObjectControllPacket controllPacket = new ObjectControllPacket
                            .ObjectControllPacketBuilder()
                            .setType("volumeprogressing")
                            .setTarget(mid)
                            .setAction(MotionEvent.ACTION_MOVE)
                            .setVolumeProgress((float)progress/8.0f)
                            .build();
                    PacketUtil.makePacket(tempMid, controllPacket);
                }
                setVolume(progress);
                controllerShow();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                if(ProcessStateModel.getInstanse().isRecording()){
                    tempMid = DrawingPanel.mid.incrementAndGet();
                    ObjectControllPacket controllPacket = new ObjectControllPacket
                            .ObjectControllPacketBuilder()
                            .setType("volumeprogressing")
                            .setTarget(mid)
                            .setAction(MotionEvent.ACTION_DOWN)
                            .setVolumeProgress(seekBar.getProgress())
                            .build();
                    PacketUtil.makePacket(tempMid, controllPacket);
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                if(ProcessStateModel.getInstanse().isRecording()){
                    ObjectControllPacket controllPacket = new ObjectControllPacket
                            .ObjectControllPacketBuilder()
                            .setType("volumeprogressing")
                            .setTarget(mid)
                            .setAction(MotionEvent.ACTION_UP)
                            .setVolumeProgress(seekBar.getProgress())
                            .build();
                    PacketUtil.makePacket(tempMid, controllPacket);
                }
            }
        });


        setBackgroundColor(Color.parseColor("#000000"));
        setVolume(4);
    }

    private void drawingRectangle(float width, float height) {
        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.lineTo(width, 0);
        mPath.lineTo(width, height);
        mPath.lineTo(0, height);
        mPath.lineTo(0, 0);
        mPath.lineTo(0, 0);

        setRegion(mPath);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawingRectangle(w, h);
    }

    @Override
    public Bitmap getDrawingCache() {
//        Bitmap bitmap = Bitmap.createBitmap(mVideoView.getWidth(), mVideoView.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//
//        mVideoView.draw(canvas);

        Matrix m = new Matrix();
        // Do matrix creation here.
        mVideoView.setTransform( m );

        // When you need to get the Bitmap
        Bitmap bitmap = mVideoView.getBitmap();
        bitmap = Bitmap.createBitmap( bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), mVideoView.getTransform( null ), true );

        return bitmap;
    }

    public void setPlayVideo(String filePath) {
        try {
            mVideoView.setDataSource(filePath);
            mVideoView.setScalableType(ScalableType.FIT_CENTER);

            mVideoView.prepare(new MediaPlayer.OnPreparedListener(){
                @Override
                public void onPrepared(MediaPlayer mp) {

                    mVideoView.start();
                    mVideoView.pause();


                    long finalTime = mVideoView.getDuration();
                    totalPlayTime = finalTime;

                    mVideoView.seekTo((int)(totalPlayTime *initProgress));

                    TextView tvTotalTime = (TextView) findViewById(R.id.tvTotalTime);
                    tvTotalTime.setText(TimeConverter.convertMillisToStringFormat(finalTime));
                    videoSeekBar.setMax((int) finalTime);
                    videoSeekBar.setProgress((int)(totalPlayTime *initProgress));
                    updateHandler.postDelayed(updateVideoTime, 100);

                }
            });
        } catch (IOException e) {
            videoFileError = true;
            e.printStackTrace();
        }
        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {

                if(ProcessStateModel.getInstanse().isRecording())
                    makePausePacket();

                pauseVideo();
            }
        });
    }

    private Runnable updateVideoTime = new Runnable(){
        public void run(){
            try {
                long currentPosition = mVideoView.getCurrentPosition();

                TextView tvRealTime = (TextView) findViewById(R.id.tvRealTime);
                tvRealTime.setText(TimeConverter.convertMillisToStringFormat(currentPosition));

                videoSeekBar.setProgress((int) currentPosition);
                updateHandler.postDelayed(this, 100);
            }catch(NullPointerException npe){
                npe.printStackTrace();
            }

        }
    };

    private boolean isVideoFileCheck(){
        if(videoFileError) {
            Toast.makeText(mContext, "비디오 파일을 열수 없습니다.", Toast.LENGTH_SHORT).show();
            return true;
        }
        return false;
    }

    public void playVideo(){

        if(isVideoFileCheck())
            return;

        playBtn.setImageResource(R.drawable.btn_video_pause_s);
        mVideoView.requestFocus();
        mVideoView.start();

    }

    public void pauseVideo()
    {
        if(isVideoFileCheck())
            return;

        playBtn.setImageResource(R.drawable.btn_video_play_s);
        mVideoView.pause();
    }

    public void seekToVideo(float seek){
        if(isVideoFileCheck())
            return;

        mVideoView.seekTo((int)(seek*totalPlayTime));
    }

    public long getTotalPlayTime(){
        return this.totalPlayTime;
    }

    public float convertDpToPixel(float dp) {
        Resources resources = mContext.getResources();
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
    }

    public void setVolume(float volume){

        if(isVideoFileCheck())
            return;

        int count = 0;
        for(View volumeView:arrVolume){
            if(count++ < volume)
                volumeView.setBackgroundColor(Color.parseColor("#96c81e"));
            else
                volumeView.setBackgroundColor(Color.parseColor("#808080"));
        }
        float currentVolume = (float) (1 - (Math.log(MAX_VOLUME-volume) / Math.log(MAX_VOLUME)));
        mVideoView.setVolume(currentVolume, currentVolume);
    }

    private void controllerShow(){
        if(FilePath.isOpencouse) {

            controller.setVisibility(GONE);
            return;
        }

        controller.setVisibility(View.VISIBLE);

        try {
            controllerTimer.cancel();
            controllerTimer.purge();
        } catch (NullPointerException ignored) {

        }
        controllerTimer = new Timer();
        controllerTimer.schedule(new TimerHidingTask(), CLOSE_WAIT_TIME);
    }


    class TimerHidingTask extends TimerTask {

        @Override
        public void run() {
            mHandler.sendEmptyMessage(0);
        }
    }

    Handler mHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == 0){
                controller.setVisibility(View.GONE);
            }
        }
    };

    public boolean isVideoPlay() { return mVideoView.isPlaying() ; }


    public void makePausePacket(){
        ObjectControllPacket controllPacket = new ObjectControllPacket
                .ObjectControllPacketBuilder()
                .setType("videopause")
                .setAction(-999)
                .setTarget(mid)
                .setEndValue((float)mVideoView.getCurrentPosition()/(float)totalPlayTime)
                .setTotalVideoTime(totalPlayTime)
                .build();
        PacketUtil.makePacket(DrawingPanel.mid.incrementAndGet(), controllPacket);
    }

    public void makePlayPacket(){
        ObjectControllPacket controllPacket = new ObjectControllPacket
                .ObjectControllPacketBuilder()
                .setType("videostart")
                .setAction(-999)
                .setTarget(mid)
                .setEndValue((float)mVideoView.getCurrentPosition()/(float)totalPlayTime)
                .setTotalVideoTime(totalPlayTime)
                .build();
        PacketUtil.makePacket(DrawingPanel.mid.incrementAndGet(), controllPacket);
    }


}
