package com.knowrecorder.develop.papers.PaperObjects;

import android.content.Context;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.Region;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.widget.FrameLayout;

import com.knowrecorder.Toolbox.Toolbox;
import com.knowrecorder.develop.ProcessStateModel;
import com.knowrecorder.develop.RecordingBoardActivity;
import com.knowrecorder.develop.event.ObjectDeleteEvent;
import com.knowrecorder.develop.manager.PageManager;
import com.knowrecorder.develop.model.packetHolder.ObjectControllPacket;
import com.knowrecorder.develop.papers.DrawingPanel;
import com.knowrecorder.develop.utils.PacketUtil;
import com.knowrecorder.rxjava.RxEventFactory;

/**
 * Created by we160303 on 2017-02-06.
 */

public class ViewObject extends View implements ObjectProperties{
    public final String TAG = "ViewObject";
    public long mid;
    private long tempMid;
    public float minScale;
    public float maxScale;

    public boolean isMovable;
    public boolean isScalable;

    public float mWidth;
    public float mHeight;
    public float mOriginWidth;
    public float mOriginHeight;

    public float mPosX = 0;
    public float mPosY = 0;
    public float mLastTouchX = 0;
    public float mLastTouchY = 0;

    public float sumOfDx = 0;
    public float sumOfDy = 0;

    public float mScaleFactor = 1.f;
    public Region mRegion;

    private ScaleGestureDetector mScaleDetector;
    private boolean scalingFactor = false;

    public ViewObject(Context context) {
        super(context);
        mScaleDetector = new ScaleGestureDetector(context, new ScaleListener());
    }

    public ViewObject(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setRegion(Path path){// 선택 범위를 지정
        RectF rectF = new RectF();
        path.computeBounds(rectF, true);
        mRegion = new Region();
        mRegion.setPath(path, new Region((int) rectF.left, (int) rectF.top, (int) rectF.right, (int) rectF.bottom));
    }

    public boolean isContainResion(float touchX, float touchY){
        Log.d(TAG+" "+mid, "isContainResion   X : " +touchX + "    Y : "+ touchY);
        return mRegion.contains((int)touchX, (int)touchY);
    }

    @Override
    public void moveTo(float dx, float dy) {
        Log.i(TAG+" "+mid, "moveTo   X :" + dx + "   Y : " + dy);

        setX(getX() + dx);
        setY(getY() + dy);
//        printViewCoordinate();
    }

    @Override
    public boolean isMovable() {
        return this.isMovable;
    }

    @Override
    public void scale(float pivotX, float pivotY, float scaleFactor) {
        Log.i(TAG+" "+mid, "..."+scaleFactor);

        Point point = new Point(getWidth()/2, getHeight()/2);
        setScaleX(scaleFactor);
        setScaleY(scaleFactor);
        setPivotX(point.x);
        setPivotY(point.y);

        mWidth = mOriginWidth*scaleFactor;
        mHeight = mOriginHeight*scaleFactor;
    }

    public void resize(float pivotX, float pivotY, float scaleFactor){
        mScaleFactor *= scaleFactor;
        mScaleFactor = Math.max(minScale, Math.min(mScaleFactor, maxScale));
        scale(pivotX, pivotY, mScaleFactor);
    }

    public void resizeMandoctory(float scaleFactor){
        mScaleFactor = scaleFactor;
        scale(0,0,scaleFactor);
    }

    @Override
    public boolean isScalable() {
        return this.isScalable;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(TAG+" "+mid, "width : " + w + "   height : " + h);
        mWidth = w;
        mHeight = h;
        mOriginWidth = w;
        mOriginHeight = h;

    }

    private class ScaleListener extends ScaleGestureDetector.SimpleOnScaleGestureListener{
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            resize(detector.getFocusX(), detector.getFocusY(), detector.getScaleFactor());
            Log.d(TAG+" "+mid, "onScale mScaleFactor : " + mScaleFactor);
            if(ProcessStateModel.getInstanse().isRecording()){
                ObjectControllPacket packetHolder = new ObjectControllPacket
                        .ObjectControllPacketBuilder()
                        .setType("resizeobj")
                        .setTarget(mid)
                        .setScale(detector.getScaleFactor())
                        .setAction(MotionEvent.ACTION_MOVE)
                        .build();
                PacketUtil.makePacket(tempMid, packetHolder);
            }
            return false;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {
            if(ProcessStateModel.getInstanse().isRecording()){
                tempMid = DrawingPanel.mid.incrementAndGet();
                ObjectControllPacket packetHolder = new ObjectControllPacket
                        .ObjectControllPacketBuilder()
                        .setType("grabobj")
                        .setTarget(mid)
                        .setGrabbed(true)
                        .setAction(MotionEvent.ACTION_DOWN)
                        .build();
                PacketUtil.makePacket(tempMid, packetHolder);
            }
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {
            super.onScaleEnd(detector);
            if(ProcessStateModel.getInstanse().isRecording()){
                ObjectControllPacket packetHolder = new ObjectControllPacket
                        .ObjectControllPacketBuilder()
                        .setType("grabobj")
                        .setTarget(mid)
                        .setGrabbed(false)
                        .setAction(MotionEvent.ACTION_UP)
                        .build();
                PacketUtil.makePacket(tempMid, packetHolder);
            }else{
                ObjectControllPacket packetHolder = new ObjectControllPacket
                        .ObjectControllPacketBuilder()
                        .setType("resizeobj")
                        .setTarget(mid)
                        .setScale(mScaleFactor)
                        .setAction(999)
                        .build();
                PacketUtil.makePacket(DrawingPanel.mid.incrementAndGet(), packetHolder);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(ProcessStateModel.getInstanse().isPlaying())
            return true;

        if (event.getPointerCount() > 1) {
            scalingFactor = true;
            return mScaleDetector.onTouchEvent(event);
        } else {

            if(!isContainResion(event.getX(), event.getY()))
                return false;

            if (scalingFactor) {
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        scalingFactor = false;
                    }
                }, 100);
                return false;
            }

            switch (event.getAction()){
                case MotionEvent.ACTION_DOWN : {
                    if(Toolbox.getInstance().getToolType() == Toolbox.Tooltype.FINGER) {
                        final float x = event.getRawX();
                        final float y = event.getRawY();
                        mLastTouchX = x;
                        mLastTouchY = y;
                        if(ProcessStateModel.getInstanse().isRecording()){
                            tempMid = DrawingPanel.mid.incrementAndGet();
                            ObjectControllPacket packetHolder = new ObjectControllPacket
                                    .ObjectControllPacketBuilder()
                                    .setType("grabobj")
                                    .setTarget(mid)
                                    .setGrabbed(true)
                                    .setAction(MotionEvent.ACTION_DOWN)
                                    .build();
                            PacketUtil.makePacket(tempMid, packetHolder);
                        }
                        return true;
                    }else if(Toolbox.getInstance().getToolType() == Toolbox.Tooltype.REMOVE){
                        tempMid = DrawingPanel.mid.incrementAndGet();
                        ObjectControllPacket packetHolder = new ObjectControllPacket
                                .ObjectControllPacketBuilder()
                                .setType(PacketUtil.S_DELOBJ)
                                .setTarget(mid)
                                .setAction(999)
                                .build();
                        PacketUtil.makePacket(tempMid, packetHolder);
                        RxEventFactory.get().post(new ObjectDeleteEvent(mid));
                        return true;
                    }
                }

                case MotionEvent.ACTION_MOVE : {
                    if(Toolbox.getInstance().getToolType() == Toolbox.Tooltype.FINGER) {
                        final float x = event.getRawX();
                        final float y = event.getRawY();

                        //IOS에 맞춰서 mScaleFactor를 나눈 값으로 저장
                        float dX = (x - mLastTouchX) / mScaleFactor;
                        float dY = (y - mLastTouchY) / mScaleFactor;

                        mLastTouchX = x;
                        mLastTouchY = y;

                        translate(dX, dY);
                        sumOfDx += dX;
                        sumOfDy += dY;

                        if(ProcessStateModel.getInstanse().isRecording()){
                            ObjectControllPacket packetHolder = new ObjectControllPacket
                                    .ObjectControllPacketBuilder()
                                    .setType("transobj")
                                    .setTarget(mid)
                                    .setDX(dX)
                                    .setDY(dY)
                                    .setAction(MotionEvent.ACTION_MOVE)
                                    .build();
                            PacketUtil.makePacket(tempMid,packetHolder);
                        }

                        return true;
                    }
                }
                case MotionEvent.ACTION_UP : {
                    if(Toolbox.getInstance().getToolType() == Toolbox.Tooltype.FINGER) {

                        if(ProcessStateModel.getInstanse().isRecording()){
                            ObjectControllPacket packetHolder = new ObjectControllPacket
                                    .ObjectControllPacketBuilder()
                                    .setType("grabobj")
                                    .setTarget(mid)
                                    .setGrabbed(false)
                                    .setAction(MotionEvent.ACTION_UP)
                                    .build();
                            PacketUtil.makePacket(tempMid, packetHolder);
                            PageManager.getInstance().currentPageInaddDrawingPacket(tempMid);
                        }else{
                            ObjectControllPacket packetHolder = new ObjectControllPacket
                                    .ObjectControllPacketBuilder()
                                    .setType("transobj")
                                    .setTarget(mid)
                                    .setDX(sumOfDx)
                                    .setDY(sumOfDy)
                                    .setAction(999)
                                    .build();
                            PacketUtil.makePacket(DrawingPanel.mid.incrementAndGet(),packetHolder);
                        }
                        sumOfDx = 0;
                        sumOfDy = 0;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public void translate(float dx, float dy){
        moveTo(dx * mScaleFactor, dy * mScaleFactor);
    }

    public void translateMandoctory(float x, float y){
        setX(x);
        setY(y);
    }

    private Handler handler = new Handler();
}
