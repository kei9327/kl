package com.knowrecorder.develop.papers;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;

import com.knowrecorder.PDF.PDFRendererV2;
import com.knowrecorder.Toolbox.Toolbox;
import com.knowrecorder.Utils.PixelUtil;
import com.knowrecorder.develop.ProcessStateModel;
import com.knowrecorder.develop.RecordingBoardActivity;
import com.knowrecorder.develop.controller.RealmPacketPutter;
import com.knowrecorder.develop.event.BringToFrontView;
import com.knowrecorder.develop.event.EventType;
import com.knowrecorder.develop.file.FilePath;
import com.knowrecorder.develop.manager.PageManager;
import com.knowrecorder.develop.model.body.PdfBody;
import com.knowrecorder.develop.model.body.VideoBody;
import com.knowrecorder.develop.model.packetHolder.ObjectCreatePacket;
import com.knowrecorder.develop.papers.PaperObjects.ViewGroupObject;
import com.knowrecorder.develop.papers.PaperObjects.ViewObject;
import com.knowrecorder.develop.papers.PaperObjects.image.ImageView;
import com.knowrecorder.develop.papers.PaperObjects.pdf.PDFView;
import com.knowrecorder.develop.papers.PaperObjects.shape.Circle;
import com.knowrecorder.develop.papers.PaperObjects.shape.Rectangle;
import com.knowrecorder.develop.papers.PaperObjects.shape.Triangle;
import com.knowrecorder.develop.papers.PaperObjects.text.TextLayout;
import com.knowrecorder.develop.papers.PaperObjects.video.VideoView;
import com.knowrecorder.develop.utils.PacketUtil;
import com.knowrecorder.rxjava.RxEventFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

import static java.lang.Math.abs;
import static java.lang.Math.floor;
import static java.lang.Math.floorDiv;

/**
 * Created by we160303 on 2017-02-06.
 */
public class ObjectPaperV2 extends View {

    private Canvas drawCanvas;
    private Bitmap canvasBitmap;
    private Paint drawPaint, canvasPaint;
    private Path drawPath;
    private Context mContext;
    public HashMap<Long, View> viewObjects = new HashMap<>();

    public boolean isUndoLoading = false;
    private boolean isThisPageVideoInsert = false;
    private int paintColor = 0xFF000000;
    private int mid;
    private int firstPointId;
    private float[] beginXY;
    private long videoMid = -1;
    private String pdfFile = null;

    public static long currentFocusedTextMid = -1;

    public ObjectPaperV2(Context context) {
        super(context);
        mContext = context;
        initView();
    }

    public ObjectPaperV2(Context context, AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        initView();
    }

    private void initView(){
        drawPath = new Path();
        drawPaint = new Paint();

        drawPaint.setAntiAlias(true);

        drawPaint.setColor(paintColor);

        drawPaint.setAntiAlias(true);
        drawPaint.setStrokeWidth(20);
        drawPaint.setStyle(Paint.Style.FILL);
        drawPaint.setStrokeJoin(Paint.Join.ROUND);
        drawPaint.setStrokeCap(Paint.Cap.ROUND);

        canvasPaint = new Paint(Paint.DITHER_FLAG);

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        drawCanvas = new Canvas(canvasBitmap);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if(isUndoLoading)
            return;
        try {
            canvas.drawBitmap(canvasBitmap, 0, 0, canvasPaint);
        } catch (NullPointerException e) {
            e.printStackTrace();

        }
        if(drawPath !=null && drawPaint != null) {
            canvas.drawPath(drawPath, drawPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {

        if(ProcessStateModel.getInstanse().isToolPopupShown())
            RxEventFactory.get().post(new EventType(EventType.CLOSE_POPUP));

        if(isBanType(Toolbox.getInstance().getToolType()))
            return false;

        if(!ProcessStateModel.getInstanse().isPlaying()) {

            float touchX = event.getRawX();
            float touchY = event.getRawY();
            int action = event.getAction();

            switch (action & MotionEvent.ACTION_MASK) {
                case MotionEvent.ACTION_DOWN:

                    RealmPacketPutter.getInstance().cancelTimer();

                    PageManager.getInstance().clearCurrentPageUndoList();

                    mid = DrawingPanel.mid.incrementAndGet();
                    Log.d("Touch", "Down X : " + touchX + "   Down Y : " + touchY + " mid : " + mid);
                    firstPointId = event.getPointerId(event.getActionIndex());

                    touchActionDown(touchX, touchY, action);
                    break;
                case MotionEvent.ACTION_MOVE:
                    if (event.getPointerId(event.getActionIndex()) == firstPointId) {
                        touchActionMove(touchX, touchY, action);
                        Log.d("Touch", "Move X : " + touchX + "   Move Y : " + touchY + " mid : " + mid);
                    }
                    break;
                case MotionEvent.ACTION_UP:

                    RealmPacketPutter.getInstance().startTimer();

                    if (event.getPointerId(event.getActionIndex()) == firstPointId) {
                        touchActionUp(touchX, touchY, action);
                        Log.d("Touch", "Up X : " + touchX + "   Up Y : " + touchY + " mid : " + mid);
                    }
                    break;
            }
            invalidate();
        }
        return true;
    }

    private void touchActionDown(float touchX, float touchY, int action) {
        beginXY = getPointArr(touchX, touchY);

        if(Toolbox.getInstance().getToolType() == Toolbox.Tooltype.SHAPE) {
            onShapeDown(mid);
            if(ProcessStateModel.getInstanse().isRecording()){

                ObjectCreatePacket packetHoler = new ObjectCreatePacket.ObjectCreatePacketBuilder().setType("shape")
                        .setScale(1.0f)
                        .setAction(action)
                        .setOriginX(touchX)
                        .setOriginY(touchY)
                        .setEndX(0)
                        .setEndY(0)
                        .build();

                PacketUtil.makePacket(mid, packetHoler);
            }
        }else if(Toolbox.getInstance().getToolType() == Toolbox.Tooltype.TEXT){
            onTextDown(DrawingPanel.mid.incrementAndGet(), beginXY[0], beginXY[1], true, false);
            currentFocusedTextMid = -1;
            Toolbox.getInstance().setToolType(Toolbox.Tooltype.FINGER);
        }
        PageManager.getInstance().currentPageInaddDrawingPacket(mid);
    }

    private void touchActionMove(float touchX, float touchY, int action) {
        if(Toolbox.getInstance().getToolType() == Toolbox.Tooltype.SHAPE) {
            onShapeMove(beginXY[0], beginXY[1], touchX, touchY);
            if(ProcessStateModel.getInstanse().isRecording()){
                ObjectCreatePacket packetHoler = new ObjectCreatePacket.ObjectCreatePacketBuilder().setType("pointmove")
                        .setAction(action)
                        .setOriginX(touchX)
                        .setOriginY(touchY)
                        .build();
                PacketUtil.makePacket(mid, packetHoler);
            }
        }
    }

    private void touchActionUp(float touchX, float touchY, int action) {
        if(Toolbox.getInstance().getToolType() == Toolbox.Tooltype.SHAPE) {
            onShapUp(mid, beginXY[0], beginXY[1], touchX, touchY);
            if(ProcessStateModel.getInstanse().isRecording()){

                ObjectCreatePacket packetHoler = new ObjectCreatePacket.ObjectCreatePacketBuilder().setType("pointend")
                        .setAction(action)
                        .setOriginX(touchX)
                        .setOriginY(touchY)
                        .build();

                PacketUtil.makePacket(mid, packetHoler);
            }else{

                ObjectCreatePacket packetHoler = new ObjectCreatePacket.ObjectCreatePacketBuilder().setType("shape")
                        .setScale(1.0f)
                        .setAction(action)
                        .setOriginX(beginXY[0])
                        .setOriginY(beginXY[1])
                        .setEndX(touchX)
                        .setEndY(touchY)
                        .build();

                PacketUtil.makePacket(mid, packetHoler);
            }
            Toolbox.getInstance().setToolType(Toolbox.Tooltype.FINGER);
        }
    }

    public void onShapeDownStatic(long mid, float beginX, float beginY, float endX, float endY){
        drawPath = new Path();

        Toolbox.getInstance().setToolType(Toolbox.Tooltype.SHAPE);

        paintColor = Toolbox.getInstance().currentShapeColor;
        drawPaint.setColor(paintColor);

        float tmp;
        boolean swap = false;
        if (endX < beginX) {
            tmp = endX;
            endX = beginX;
            beginX = tmp;
        }

        if (endY < beginY) {
            tmp = endY;
            endY = beginY;
            beginY = tmp;
            swap = true;
        }

        float width = abs(beginX-endX);
        float height = abs(beginY-endY);

        switch (Toolbox.getInstance().currentShape) {
            case CIRCLE:
                Circle circle = new Circle(mContext);
                circle.initView(mid, beginX, beginY, width, height);
                ((ViewGroup) getParent()).addView(circle, (int) width, (int) height);
                addObject(mid, circle);
                break;

            case TRIANGLE:
                Triangle triangle = new Triangle(mContext);
                triangle.initView(mid, beginX, beginY, width, height, swap);
                ((ViewGroup) getParent()).addView(triangle, (int) width, (int) height);
                addObject(mid, triangle);
                break;

            case RECTANGLE:
                Rectangle rectangle = new Rectangle(mContext, drawPath);
                rectangle.initView(mid, beginX, beginY, width, height);
                ((ViewGroup) getParent()).addView(rectangle, (int) width, (int) height);
                addObject(mid, rectangle);
                break;
        }
        RxEventFactory.get().post(new BringToFrontView(RecordingBoardActivity.DRAWING_PAPER));
        drawPath.reset();

    }

    public void onShapeDown(long mid) {
        drawPath = new Path();
        Toolbox.getInstance().setToolType(Toolbox.Tooltype.SHAPE);

        paintColor = Toolbox.getInstance().currentShapeColor;
        drawPaint.setColor(paintColor);

        RxEventFactory.get().post(new BringToFrontView(RecordingBoardActivity.DRAWING_OBJECT_PAPER));
    }

    public void onShapeMove(float startX, float startY, float touchX, float touchY){

        switch(Toolbox.getInstance().currentShape){
            case CIRCLE :
                RectF rectF = new RectF();
                rectF.set(startX, startY, touchX, touchY);
                drawPath.reset();
                drawPath.addOval(rectF ,Path.Direction.CW);
                break;
            case TRIANGLE :
                float[] vertexTop = new float[2];
                float[] vertexLeft = new float[2];
                float[] vertexRight = new float[2];

                vertexTop[0] = (startX + touchX) / 2;
                vertexTop[1] = startY;

                vertexLeft[0] = startX;
                vertexLeft[1] = touchY;

                vertexRight[0] = touchX;
                vertexRight[1] = touchY;

                drawPath.reset();
                drawPath.moveTo(vertexTop[0], vertexTop[1]);        // Vertex Top
                drawPath.lineTo(vertexLeft[0], vertexLeft[1]);      // Vertex Top -> Vertex Left
                drawPath.lineTo(vertexRight[0], vertexRight[1]);    // Vertex Left -> Vertext Right
                drawPath.lineTo(vertexTop[0], vertexTop[1]);        // Vertext Right -> Vertex Top
                break;
            case RECTANGLE :
                drawPath.reset();
                drawPath.moveTo(startX, startY);
                drawPath.lineTo(touchX, startY);
                drawPath.lineTo(touchX, touchY);
                drawPath.lineTo(startX, touchY);
                drawPath.lineTo(startX, startY);
                drawPath.lineTo(startX, startY);
                break;
        }
    }

    public void onShapUp(long mid, float startX, float startY, float touchX, float touchY){

        float tmp;
        boolean swap = false;
        if (touchX < startX) {
            tmp = touchX;
            touchX = startX;
            startX = tmp;
        }

        if (touchY < startY) {
            tmp = touchY;
            touchY = startY;
            startY = tmp;
            swap = true;
        }

        float width = abs(startX-touchX);
        float height = abs(startY-touchY);

        switch (Toolbox.getInstance().currentShape) {
            case CIRCLE:
                Circle circle = new Circle(mContext);
                circle.initView(mid, startX, startY, width, height);
                ((ViewGroup) getParent()).addView(circle, (int) width, (int) height);
                addObject(mid, circle);
                break;

            case TRIANGLE:
                Triangle triangle = new Triangle(mContext);
                triangle.initView(mid, startX, startY, width, height, swap);
                ((ViewGroup) getParent()).addView(triangle, (int) width, (int) height);
                addObject(mid, triangle);
                break;

            case RECTANGLE:
                Rectangle rectangle = new Rectangle(mContext, drawPath);
                rectangle.initView(mid, startX, startY, width, height);
                ((ViewGroup) getParent()).addView(rectangle, (int) width, (int) height);
                addObject(mid, rectangle);
                break;
        }
        RxEventFactory.get().post(new BringToFrontView(RecordingBoardActivity.DRAWING_PAPER));
        drawPath.reset();
    }



    public void onImageDown(long mid, float originX, float originY,  float endX, float endY,  String filePath, boolean isSave){
        ViewGroup parent  = (ViewGroup)getParent();

        int width = (int)(abs(originX - endX));
        int height = (int)(abs(originY - endY));

        ImageView imageView = new ImageView(mContext);
        imageView.setImageFile(filePath);
        imageView.initView(mid, originX, originY, width, height);
        parent.addView(imageView, width, height);
        addObject(mid, imageView);

        Toolbox.getInstance().setToolType(Toolbox.Tooltype.FINGER);

        if(!ProcessStateModel.getInstanse().isPlaying() && isSave) {
            ObjectCreatePacket packetHoler = new ObjectCreatePacket.ObjectCreatePacketBuilder().setType("image")
                    .setScale(1.0f)
                    .setOriginX(originX)
                    .setOriginY(originY)
                    .setEndX(endX)
                    .setEndY(endY)
                    .setW(abs(originX-endX))
                    .setH(abs(originY-endY))
                    .build();
            PacketUtil.makePacket(mid, packetHoler);
        }

        RxEventFactory.get().post(new BringToFrontView(RecordingBoardActivity.DRAWING_PAPER));
    }

    public void onTextDown(long mid, float startX, float startY, boolean isSave, boolean packet) {
        ViewGroup parent = (ViewGroup)getParent();

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        final TextLayout textLayout = new TextLayout(mContext);
        textLayout.initView(mid, startX, startY);
        parent.addView(textLayout, params);
        addObject(mid, textLayout);

        if(!packet){
            //직접 입력시
            textLayout.setInputMode();
            textLayout.getEditText().requestFocus();
            InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(textLayout.getEditText(), InputMethodManager.SHOW_FORCED);
        }

        if(!ProcessStateModel.getInstanse().isPlaying() && isSave){
            ObjectCreatePacket createPacket = new ObjectCreatePacket
                    .ObjectCreatePacketBuilder()
                    .setType("txtbegin")
                    .setAction(MotionEvent.ACTION_DOWN)
                    .setOriginX(startX)
                    .setOriginY(startY)
                    .setEndX(startX + textLayout.mWidth)
                    .setEndY(startY + textLayout.mHeight)
                    .setW(textLayout.mWidth)
                    .setH(textLayout.mHeight)
                    .build();
            PacketUtil.makePacket(mid,createPacket);
        }

        RxEventFactory.get().post(new BringToFrontView(RecordingBoardActivity.DRAWING_PAPER));

    }
    public void setText(long target, String content){
        Log.d("setText", "mid : " + target + "  content : " + content);
        if(viewObjects.size() != 0){
            TextLayout textLayout = (TextLayout) viewObjects.get(target);
            textLayout.setEditText(content);
        }
    }
    public void clearAllTextFocus(){
        try {
            if(currentFocusedTextMid != -1) {
                ((TextLayout) viewObjects.get(currentFocusedTextMid)).setClearFocus();
            }
        }catch(NullPointerException np) {
            np.printStackTrace();

            View focusedChild = ((ViewGroup)getParent()).getFocusedChild();
            if(focusedChild != null && focusedChild instanceof TextLayout){
                ((TextLayout)focusedChild).setClearFocus();
            }

        }
    }

    public void onVideoDown(long mid, final String filePath, boolean isSave){

        isThisPageVideoInsert = true;
        videoMid = mid;

        float videoWidth = PixelUtil.getInstance().convertDpToPixel(500) ;
        float videoHeight = PixelUtil.getInstance().convertDpToPixel(415) ;

        ViewGroup parent  = (ViewGroup)getParent();
        final VideoView videoView = new VideoView(mContext);
        videoView.initVIew(mid, 0, 0, videoWidth, videoHeight, 0);
        parent.addView(videoView, (int)videoWidth, (int)videoHeight);
        addObject(mid, videoView);

        videoView.post(new Runnable() {
            @Override
            public void run() {
                videoView.setPlayVideo(filePath);
            }
        });

        Toolbox.getInstance().setToolType(Toolbox.Tooltype.FINGER);

        if(!ProcessStateModel.getInstanse().isPlaying() && isSave) {
            ObjectCreatePacket packetHoler = new ObjectCreatePacket.ObjectCreatePacketBuilder().setType("video")
                    .setScale(1.0f)
                    .setOriginX(0)
                    .setOriginY(0)
                    .setEndX(videoWidth)
                    .setEndY(videoHeight)
                    .setW(videoWidth)
                    .setH(videoHeight)
                    .setVideoProgress(0)
                    .setVolume(4)
                    .build();
            PacketUtil.makePacket(mid, packetHoler);
        }


        RxEventFactory.get().post(new BringToFrontView(RecordingBoardActivity.DRAWING_PAPER));

    }
    // 아이오에스 때문에 생긴 메소드  static Packet을 Update하기때문에 있어야 함
    public void onVideoStaticDown(long mid, final String filePath, float w, float h, float originX, float originY, float scale, float progress){

        isThisPageVideoInsert = true;
        videoMid = mid;

        float videoWidth = w;
        float videoHeight = h;

        ViewGroup parent  = (ViewGroup)getParent();
        final VideoView videoView = new VideoView(mContext);
        videoView.initVIew(mid, originX, originY, videoWidth, videoHeight, progress);
        videoView.resizeMandoctory(scale);
        videoView.setPlayVideo(filePath);
        parent.addView(videoView, (int)videoWidth, (int)videoHeight);
        addObject(mid, videoView);

        videoView.post(new Runnable() {
            @Override
            public void run() {
                videoView.setPlayVideo(filePath);
            }
        });


        Toolbox.getInstance().setToolType(Toolbox.Tooltype.FINGER);

        RxEventFactory.get().post(new BringToFrontView(RecordingBoardActivity.DRAWING_PAPER));

    }
    public void videoStart(long target){
        if(viewObjects.size() != 0){
            VideoView video = (VideoView) viewObjects.get(target);
            video.playVideo();
        }
    }
    public void videoPause(long target){
        if(viewObjects.size() != 0){
            VideoView video = (VideoView) viewObjects.get(target);
            if(video != null) {
                video.pauseVideo();
            }
        }
    }
    public void makeVideoPausePacket(long target){
        if(viewObjects.size() != 0){
            VideoView video = (VideoView) viewObjects.get(target);
            video.makePausePacket();
        }
    }
    public void videoSeekTo(long target, final float seekTo){
        if(viewObjects.size() != 0){
            final VideoView video = (VideoView) viewObjects.get(target);
            video.post(new Runnable() {
                @Override
                public void run() {
                    video.seekToVideo(seekTo);
                }
            });
        }
    }
    public float getVideoTotalTime(){
        if(isThisPageVideoInsert){
            if(viewObjects.size() != 0){
                VideoView video = (VideoView) viewObjects.get(videoMid);
                return (float)video.getTotalPlayTime();
            }
        }

        return 0;
    }

    public void videoVolume(long target, int seekTo){
        if(viewObjects.size() != 0){
            VideoView video = (VideoView) viewObjects.get(target);
            video.setVolume(seekTo);
        }
    }

    public void spreadPdfFile(String filePath, String fileName){
        try {
            int pageCount = PDFRendererV2.getInstance(mContext).getPdfFilePageCount(filePath);
            int totalPage = PageManager.getInstance().getSumOfPage();

            for(int i = 1; i <= pageCount; i++){
                RealmPacketPutter.getInstance().InsertAddPage(totalPage+i);
                PageManager.getInstance().addSumOfPage(1);
                ObjectCreatePacket createPacket = new ObjectCreatePacket.ObjectCreatePacketBuilder()
                                                          .setType("pdf") //todo 무렁봐야함
                                                          .setAction(999)
                                                          .setOriginX(0)
                                                          .setOriginY(0)
                                                          .setEndX(canvasBitmap.getWidth())
                                                          .setEndY(canvasBitmap.getHeight())
                                                          .setPdfPageNo(i)
                                                          .setFileName(fileName)
                                                          .build();
                PacketUtil.makePacket(DrawingPanel.mid.incrementAndGet(), createPacket, DrawingPanel.pageId.get());
            }

            Toolbox.getInstance().setToolType(Toolbox.Tooltype.FINGER);
            //todo changePage
            PageManager.getInstance().changePage(totalPage+1, ProcessStateModel.getInstanse().getElapsedTime());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public void openPdfPage(final long mid, final long pageid, PdfBody pdfBody, Float resolutionRate){
        try {
            String pdfFile = FilePath.getFilesDirectory()+pdfBody.getFilename();
            PDFRendererV2.getInstance(mContext).openPdfFile(pdfFile);
            addPDFPage(mid, pageid, pdfFile, pdfBody, resolutionRate);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addPDFPage(final long mid,final long pageId, final String pdfFile, PdfBody pdfBody, Float resolutionRate){
        //ios에서 넣는 방식이랑 다름, 플레이 할때 다르게 나옴

        if(PageManager.getInstance().getCurrentPageId() != pageId || viewObjects.containsKey(mid)) {
            return;
        }

        int pdfPage = pdfBody.getPdfpageno();
        float left = pdfBody.getOriginx() * resolutionRate;
        float top = pdfBody.getOriginy() * resolutionRate;
        float right = pdfBody.getEndx() * resolutionRate;
        float bottom = pdfBody.getEndy() * resolutionRate;

        Log.d("addpdf", "mid : " + mid + "  pageid : " + pageId + "  pdfPage : " + pdfPage);

        final PDFView pdfView = new PDFView(ObjectPaperV2.this.mContext);
        final Bitmap bitmap = PDFRendererV2.getInstance(ObjectPaperV2.this.mContext).getPageBitmap(pdfPage);

        pdfView.setPdfFile(pdfFile);
        pdfView.initView(mid, left, top, right, bottom);
        pdfView.setBitmap(bitmap);

        ViewGroup parent  = (ViewGroup)getParent();
        parent.addView(pdfView, (int)(right-left) , (int)(bottom-top) );
        addObject(mid, pdfView);
    }


    public void addObject(long mid, View view){
        if(!viewObjects.containsKey(mid)){
            viewObjects.put(mid, view);
            Log.d("ObjectPaper","add View Number : " + mid + "  Size : " +viewObjects.size());
        }
    }
    public void deleteObject(long mid){
        View view = viewObjects.remove(mid);
        Log.d("ObjectPaper","delete View Number : " + mid + "  Size : " +viewObjects.size());

        if(view instanceof VideoView)
            isThisPageVideoInsert = false;

        if(view != null)
            ((ViewGroup)getParent()).removeView(view);
    }
    public void objectMove(long target, final float dx, final float dy){
        if(viewObjects.size() != 0) {
            try {
                final ViewObject view = (ViewObject)viewObjects.get(target);
                if(view != null){
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.translate(dx,dy);
                        }
                    });
                }
            }catch(ClassCastException cce){
                final ViewGroupObject view = (ViewGroupObject)viewObjects.get(target);
                if(view != null){
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.translate(dx,dy);
                        }
                    });
                }
            }
        }
    }

    public View getObject(long mid){
        if(viewObjects.size() != 0) {
            try {
                final ViewObject view = (ViewObject)viewObjects.get(mid);
                if(view != null){
                    return view;
                }
            }catch(ClassCastException cce){

            }
        }
        return null;
    }

    public void objectMoveStatic(long target, final float x, final float y){
        if(viewObjects.size() != 0) {
            try {
                final ViewObject view = (ViewObject)viewObjects.get(target);
                if(view != null){
                    view.translateMandoctory(x, y);
                }
            }catch(ClassCastException cce){
                ViewGroupObject view = (ViewGroupObject)viewObjects.get(target);
                if(view != null){
                    view.translateMandoctory(x, y);
                }
            }
        }
    }
    public void objectScaleStatic(long target, final float scale){
        if(viewObjects.size() != 0) {
            try {
                final ViewObject view = (ViewObject)viewObjects.get(target);
                if(view != null){
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.resizeMandoctory(scale);
                        }
                    });
                }
            }catch(ClassCastException cce){
                final ViewGroupObject view = (ViewGroupObject)viewObjects.get(target);
                if(view != null){
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.resizeMandoctory(scale);
                        }
                    });
                }
            }
        }
    }
    public void objectScale(long target, final float scale){
        if(viewObjects.size() != 0) {
            try {
                final ViewObject view = (ViewObject)viewObjects.get(target);
                if(view != null){
                    view.post(new Runnable() {
                        @Override
                        public void run() {
                            view.resize(0,0,scale);
                        }
                    });
                }
            }catch(ClassCastException cce){
                final ViewGroupObject view = (ViewGroupObject)viewObjects.get(target);
                view.post(new Runnable() {
                    @Override
                    public void run() {
                        view.resize(0,0,scale);
                    }
                });
            }
        }
    }

    private float[] getPointArr(float x, float y){
        float[] point = new float[2];
        point[0] = x;
        point[1] = y;
        return point;
    }

    public void clearCanvas() {
        if(drawPath == null || drawCanvas == null) {
            return;
        }

        isThisPageVideoInsert = false;
        videoMid = -1;

        drawPath.reset();

        Iterator<Long> iter = viewObjects.keySet().iterator();
        while(iter.hasNext()){
            long key = iter.next();
            View view = viewObjects.get(key);


            if(view instanceof PDFView)
                ((PDFView)view).resetBitmap();

            ((ViewGroup)getParent()).removeViewInLayout(view);
        }
        viewObjects.clear();

        clearLeftView();
        drawCanvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);

    }

    public void clearCanvasForUndo() {

        ViewGroup parent = (ViewGroup)getParent();
        viewObjects.clear();

        int children = parent.getChildCount();
        for(int i = 0; i < children; i++)
        {
            if(parent.getChildAt(i) instanceof ViewObject || parent.getChildAt(i) instanceof ViewGroupObject){
                parent.removeViewInLayout(parent.getChildAt(i));
            }
        }

    }

    private void clearLeftView() {
        ViewGroup parent = (ViewGroup)getParent();
        int children = parent.getChildCount();
        for(int i = 0; i < children; i++)
        {
            if(parent.getChildAt(i) instanceof ViewObject || parent.getChildAt(i) instanceof ViewGroupObject){
                parent.removeViewInLayout(parent.getChildAt(i));
            }
        }
    }

    private boolean isBanType(Toolbox.Tooltype tooltype){
        if(tooltype == Toolbox.Tooltype.SHAPE || tooltype == Toolbox.Tooltype.TEXT)
            return false;
        return true;
    }

    public boolean isThisPageVideoInsert(){ return isThisPageVideoInsert;}
    public long getVideoMid(){ return videoMid ; }

}
