package com.knowrecorder.develop.papers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;

import com.knowrecorder.Toolbox.Toolbox;
import com.knowrecorder.Utils.PixelUtil;
import com.knowrecorder.develop.ProcessStateModel;
import com.knowrecorder.develop.controller.RealmPacketPutter;
import com.knowrecorder.develop.event.EventType;
import com.knowrecorder.develop.manager.PageManager;
import com.knowrecorder.develop.model.packetHolder.DrawingPacket;
import com.knowrecorder.develop.player.PaperPlayer;
import com.knowrecorder.develop.utils.PacketUtil;
import com.knowrecorder.rxjava.RxEventFactory;

import java.util.ArrayList;

/**
 * Created by Changha on 2017-02-02.
 */

public class DrawingPaper extends View{

    private Path drawPath;
    private Paint drawPaint, canvasPaint, pointerPaint;
    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private ArrayList<float[]> points = new ArrayList<>();
    private RealmPacketPutter packetController;

    private float[] pointerPts;
    private int paintColor;
    private int pointerColor= 0x77990000;
    private float startX;
    private float startY;
    private boolean done = false;
    public boolean isUndoLoading = false;
    private int firstPointId;
    private int downCount = 0;
    private int mid;

    private final int POINTER_X = 0;
    private final int POINTER_Y = 1;

    public DrawingPaper(Context context) {
        super(context);
        initView();
    }

    public DrawingPaper(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initView() {
        drawPath = new Path();
        pointerPaint = new Paint();
        pointerPts = new float[]{-999, -999};
        paintColor = Toolbox.getInstance().currentStrokeColor;

        pointerPaint.setColor(pointerColor);

        setBasicDrawPaint();

        pointerPaint.setAntiAlias(true);
        pointerPaint.setStrokeWidth(100);
        pointerPaint.setStyle(Paint.Style.STROKE);
        pointerPaint.setStrokeJoin(Paint.Join.ROUND);
        pointerPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);
        setBackgroundColor(Color.TRANSPARENT);
        packetController = RealmPacketPutter.getInstance();

    }
    private void setBasicDrawPaint(){
        drawPaint = new Paint();
        drawPaint.setAntiAlias(true);
        drawPaint.setStyle(Paint.Style.STROKE);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);
        drawPaint.setDither(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (!done) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            drawCanvas = new Canvas(canvasBitmap);
            
            done = true;
        }
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(isUndoLoading)
            return;
        //canvas.save();

        try {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        } catch (NullPointerException e){
            e.printStackTrace();
        }
        if(drawPaint != null)
            canvas.drawPath(drawPath, drawPaint);

        /*
        if(Toolbox.getInstance().getCurrentPointerBitmap() != null){
            canvas.drawBitmap(Toolbox.getInstance().getCurrentPointerBitmap(), pointerPts[POINTER_X]- PixelUtil.getInstance().convertDpToPixel(50), pointerPts[POINTER_Y]-PixelUtil.getInstance().convertDpToPixel(50), null);
        }*/

        //canvas.restore();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(ProcessStateModel.getInstanse().isToolPopupShown())
            RxEventFactory.get().post(new EventType(EventType.CLOSE_POPUP));

        if(isBanType(Toolbox.getInstance().getToolType()))
            return false;

        if(!ProcessStateModel.getInstanse().isPlaying()){
            float touchX = event.getX();
            float touchY = event.getY();
            int action = event.getAction();

            points.add(getPointArr(touchX, touchY));
            switch (action & MotionEvent.ACTION_MASK){
                case MotionEvent.ACTION_DOWN :
                    RealmPacketPutter.getInstance().cancelTimer();
                    mid = DrawingPanel.mid.incrementAndGet();
                    firstPointId = event.getPointerId(event.getActionIndex());

                    touchActionDown(touchX, touchY, action);
                    Log.d("develop", "touch Action Down : " + mid);
                    break;

                case MotionEvent.ACTION_MOVE :
                    if(event.getPointerId(event.getActionIndex()) == firstPointId) {
                        touchActionMove(touchX, touchY, action);
                        Log.d("develop", "touch Action Move : " + mid);
                    }
                    break;


                case MotionEvent.ACTION_UP :
                    RealmPacketPutter.getInstance().startTimer();
                    if(event.getPointerId(event.getActionIndex()) == firstPointId) {
                        Log.d("develop", "touch Action Up : " + mid);
                        touchActionUp(action);
                    }
                    break;


                case MotionEvent.ACTION_POINTER_DOWN :
                    break;


                case MotionEvent.ACTION_POINTER_UP :

                    break;
            }
            invalidate();
        }
        return true;
    }

    private void touchActionDown(float touchX, float touchY, int action){
        switch(Toolbox.getInstance().getToolType()){
            case PEN : {
                if(ProcessStateModel.getInstanse().isRecording()) {
                    DrawingPacket packetHolder = new DrawingPacket
                            .DrawingPacketBuilder()
                            .setPoints(points)
                            .setType("pen")
                            .setAction(action)
                            .build();
                    PacketUtil.makePacket(mid, packetHolder);
                    points.clear();
                }
                onActionDown(touchX, touchY, false, mid);

                PageManager.getInstance().clearCurrentPageUndoList();
                addDrawingPacketInPage(mid);
                break;
            }

            case POINTER :{
                if(ProcessStateModel.getInstanse().isRecording()) {
                    DrawingPacket packetHolder = new DrawingPacket
                            .DrawingPacketBuilder()
                            .setX(points.get(0)[0])
                            .setY(points.get(0)[1])
                            .setType("laser")
                            .setAction(action)
                            .build();
                    PacketUtil.makePacket(mid, packetHolder);
                    points.clear();
                }
                onPointerDown(touchX, touchY);
                break;
            }

            case ERASER :{
                if(ProcessStateModel.getInstanse().isRecording()) {
                    DrawingPacket packetHolder = new DrawingPacket
                            .DrawingPacketBuilder()
                            .setPoints(points)
                            .setType("pen")
                            .setAction(action)
                            .build();
                    PacketUtil.makePacket(mid, packetHolder);
                    points.clear();
                }
                onActionDown(touchX, touchY, true, mid);

                PageManager.getInstance().clearCurrentPageUndoList();
                addDrawingPacketInPage(mid);
                break;
            }
        }
    }

    private void touchActionMove(float touchX, float touchY, int action){
        switch(Toolbox.getInstance().getToolType()){
            case PEN : {
                if(ProcessStateModel.getInstanse().isRecording()) {
                    DrawingPacket packetHolder = new DrawingPacket
                            .DrawingPacketBuilder()
                            .setX(points.get(0)[0])
                            .setY(points.get(0)[1])
                            .setType("pointmove")
                            .setAction(action)
                            .build();
                    PacketUtil.makePacket(mid, packetHolder);
                    points.clear();
                }
                onActionMove(touchX, touchY);
                break;
            }

            case POINTER :{
                if(ProcessStateModel.getInstanse().isRecording()) {
                    DrawingPacket packetHolder = new DrawingPacket
                            .DrawingPacketBuilder()
                            .setX(points.get(0)[0])
                            .setY(points.get(0)[1])
                            .setType("laser")
                            .setAction(action)
                            .build();
                    PacketUtil.makePacket(mid, packetHolder);
                    points.clear();
                }
                onPointerMove(touchX, touchY);
                break;
            }

            case ERASER :{
                if(ProcessStateModel.getInstanse().isRecording()) {
                    DrawingPacket packetHolder = new DrawingPacket
                            .DrawingPacketBuilder()
                            .setX(points.get(0)[0])
                            .setY(points.get(0)[1])
                            .setType("pointmove")
                            .setAction(action)
                            .build();
                    PacketUtil.makePacket(mid, packetHolder);
                    points.clear();
                }
                onActionMove(touchX, touchY);
                break;
            }
        }
    }

    private void touchActionUp(int action){
        switch(Toolbox.getInstance().getToolType()){
            case PEN : {
                if(ProcessStateModel.getInstanse().isRecording()) {
                    DrawingPacket packetHolder = new DrawingPacket
                            .DrawingPacketBuilder()
                            .setX(points.get(0)[0])
                            .setY(points.get(0)[1])
                            .setType("pointend")
                            .setAction(action)
                            .build();
                    PacketUtil.makePacket(mid, packetHolder);
                    points.clear();
                }else {
                    DrawingPacket packetHolder = new DrawingPacket
                            .DrawingPacketBuilder()
                            .setPoints(points)
                            .setType("pen")
                            .setAction(-999)
                            .build();
                    PacketUtil.makePacket(mid, packetHolder);
                    points.clear();
                }
                onActionUp(false);
                break;
            }

            case POINTER :{
                if(ProcessStateModel.getInstanse().isRecording()) {
                    DrawingPacket packetHolder = new DrawingPacket
                            .DrawingPacketBuilder()
                            .setX(points.get(0)[0])
                            .setY(points.get(0)[1])
                            .setType("laser")
                            .setAction(action)
                            .build();
                    PacketUtil.makePacket(mid, packetHolder);
                    points.clear();
                }
                onPointerUp();
                break;
            }

            case ERASER :{
                if(ProcessStateModel.getInstanse().isRecording()) {
                    DrawingPacket packetHolder = new DrawingPacket
                            .DrawingPacketBuilder()
                            .setX(points.get(0)[0])
                            .setY(points.get(0)[1])
                            .setType("pointend")
                            .setAction(action)
                            .build();
                    PacketUtil.makePacket(mid, packetHolder);
                }else {
                    DrawingPacket packetHolder = new DrawingPacket
                            .DrawingPacketBuilder()
                            .setPoints(points)
                            .setType("pen")
                            .setAction(-999)
                            .build();
                    PacketUtil.makePacket(mid, packetHolder);
                    points.clear();
                }
                onActionUp(true);
                break;
            }
        }
    }



    public void onActionDown(float touchX, float touchY, boolean isEraser, long mid) {

        ((ViewGroup) getParent()).bringChildToFront(this);

        if(downCount != 0 ){
            try {
                drawCanvas.drawPath(drawPath, drawPaint);
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            drawPath.reset();
        }
        downCount++;

        setBasicDrawPaint();
        if (Build.VERSION.SDK_INT >= 11 && isEraser) {
            Toolbox.getInstance().setToolType(Toolbox.Tooltype.ERASER);

            setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            drawPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            drawPaint.setStrokeWidth(Toolbox.getInstance().currentEraserWidth);
        } else {
            Toolbox.getInstance().setToolType(Toolbox.Tooltype.PEN);

            drawPaint.setColor(Toolbox.getInstance().currentStrokeColor);
            drawPaint.setStrokeWidth(Toolbox.getInstance().currentStrokeWidth);
            drawPaint.setAlpha(Toolbox.getInstance().currentStrokeOpacity);
        }

        drawPath.reset();

        drawPath.moveTo(touchX, touchY);
        startX = touchX;
        startY = touchY;


    }

    public void onActionMove(float touchX, float touchY) {
        drawPath.quadTo(startX, startY, (touchX + startX) / 2, (touchY + startY) / 2);
        startX = touchX;
        startY = touchY;
    }

    public void onActionUp(boolean isEraser) {
        drawPath.lineTo(startX, startY);
        try {
            drawCanvas.drawPath(drawPath, drawPaint);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        if(drawPath != null)
            drawPath.reset();
        if(drawPaint != null) {
            drawPaint.reset();
            drawPaint = null;
        }
//        if (Build.VERSION.SDK_INT >= 11 && isEraser) {
//            setLayerType(View.LAYER_TYPE_HARDWARE, null);
//            drawPaint.setXfermode(null);
//        }
        downCount--;
    }

    public void onPointerDown(float touchX, float touchY) {
        Toolbox.getInstance().setToolType(Toolbox.Tooltype.POINTER);
        pointerPts[POINTER_X] = touchX;
        pointerPts[POINTER_Y] = touchY;
        ((ViewGroup) getParent()).bringChildToFront(this);
    }

    public void onPointerMove(float touchX, float touchY) {
        pointerPts[POINTER_X] = touchX;
        pointerPts[POINTER_Y] = touchY;
    }

    public void onPointerUp() {
        pointerPts[POINTER_X] = -999;
        pointerPts[POINTER_Y] = -999;
    }

    private void addDrawingPacketInPage(long mid){
        PageManager.getInstance().currentPageInaddDrawingPacket(mid);
    }

    public void clearCanvas() {
        if(drawPath == null || drawCanvas == null) {
            return;
        }

        drawPath.reset();


        if (Build.VERSION.SDK_INT >= 11) {
            setLayerType(View.LAYER_TYPE_HARDWARE, null);
            if(drawPaint != null)
                drawPaint.setXfermode(null);
        }

        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
    }


    private boolean isBanType(Toolbox.Tooltype tooltype){
        if(tooltype == Toolbox.Tooltype.PEN || tooltype == Toolbox.Tooltype.ERASER || tooltype == Toolbox.Tooltype.POINTER)
            return false;
        return true;
    }

    private float[] getPointArr(float x, float y){
        float[] point = new float[2];
        point[0] = x;
        point[1] = y;
        return point;
    }
}
