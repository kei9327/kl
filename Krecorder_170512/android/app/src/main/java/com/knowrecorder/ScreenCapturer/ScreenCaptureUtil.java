package com.knowrecorder.ScreenCapturer;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.os.Build;
import android.util.Log;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import com.knowrecorder.PaperObjects.CustomScalableVideoView;
import com.yqritc.scalablevideoview.ScalableType;
import com.yqritc.scalablevideoview.ScaleManager;
import com.yqritc.scalablevideoview.Size;

import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by hazuki21 on 2017. 1. 3..
 */

public class ScreenCaptureUtil {

    public static Bitmap getScreenShotBitmap(View view, View[] ignoredViews) {
        if (view == null) {
            throw new IllegalArgumentException("Parameter View cannot be null.");
        }

        final List<RootViewInfo> viewInfos = ViewFieldHelper.getViewChilds(view);

        final Bitmap bitmap;

        try {
            bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException(e);
        }

        drawRootsToBitmap(viewInfos, bitmap, ignoredViews);

        return bitmap;
    }

    private static void drawRootsToBitmap(List<RootViewInfo> viewInfos, Bitmap bitmap, View[] ignoredViews) {
        for (RootViewInfo viewData : viewInfos) {
            drawRootsToBitmap(viewData, bitmap, ignoredViews);
        }
    }

    private static void drawRootsToBitmap(RootViewInfo rootViewInfo, Bitmap bitmap, View[] ignoredViews) {
        final Canvas canvas = new Canvas(bitmap);

        canvas.translate(rootViewInfo.getLeft(), rootViewInfo.getTop());

        int[] ignoredViewsVisibility = null;
        if (ignoredViews != null) {
            ignoredViewsVisibility = new int[ignoredViews.length];

            for (int i = 0; i < ignoredViews.length; i++) {
                if (ignoredViews[i] != null) {
                    ignoredViewsVisibility[i] = ignoredViews[i].getVisibility();
                    ignoredViews[i].setVisibility(View.INVISIBLE);
                }
            }
        }

        rootViewInfo.getView().draw(canvas);
        //Draw undrawable views
        drawUnDrawableViews(rootViewInfo.getView(), canvas);

        if (ignoredViews!=null) {
            for (int i = 0; i < ignoredViews.length; i++) {
                if (ignoredViews[i] != null) {
                    ignoredViews[i].setVisibility(ignoredViewsVisibility[i]);
                }
            }
        }
    }

    private static ArrayList<View> drawUnDrawableViews(View v, Canvas canvas) {

        if (!(v instanceof ViewGroup)) {
            ArrayList<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            return viewArrayList;
        }

        ArrayList<View> result = new ArrayList<>();

        ViewGroup viewGroup = (ViewGroup) v;
        for (int i = 0; i < viewGroup.getChildCount(); i++) {

            View child = viewGroup.getChildAt(i);

            ArrayList<View> viewArrayList = new ArrayList<>();
            viewArrayList.add(v);
            viewArrayList.addAll(drawUnDrawableViews(child, canvas));

            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH
                    && child instanceof TextureView) {
                drawTextureView((TextureView) child, canvas);
            }

            if (child instanceof GLSurfaceView) {
                drawGLSurfaceView((GLSurfaceView) child, canvas);
            }

            result.addAll(viewArrayList);
        }
        return result;
    }

    private static void drawGLSurfaceView(GLSurfaceView surfaceView, Canvas canvas) {
        if (surfaceView.getWindowToken() != null) {
            int[] location = new int[2];

            surfaceView.getLocationOnScreen(location);
            final int width = surfaceView.getWidth();
            final int height = surfaceView.getHeight();

            final int x = 0;
            final int y = 0;
            int[] b = new int[width * (y + height)];

            final IntBuffer ib = IntBuffer.wrap(b);
            ib.position(0);

            //To wait for the async call to finish before going forward
            final CountDownLatch countDownLatch = new CountDownLatch(1);
            surfaceView.queueEvent(new Runnable() {
                @Override public void run() {
                    EGL10 egl = (EGL10) EGLContext.getEGL();
                    egl.eglWaitGL();
                    GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();

                    gl.glFinish();

                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    gl.glReadPixels(x, 0, width, y + height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, ib);
                    countDownLatch.countDown();
                }
            });

            try {
                countDownLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int[] bt = new int[width * height];
            int i = 0;
            for (int k = 0; i < height; k++) {
                for (int j = 0; j < width; j++) {
                    int pix = b[(i * width + j)];
                    int pb = pix >> 16 & 0xFF;
                    int pr = pix << 16 & 0xFF0000;
                    int pix1 = pix & 0xFF00FF00 | pr | pb;
                    bt[((height - k - 1) * width + j)] = pix1;
                }
                i++;
            }

            Bitmap sb = Bitmap.createBitmap(bt, width, height, Bitmap.Config.ARGB_8888);
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
            canvas.drawBitmap(sb, location[0], location[1], paint);
            sb.recycle();
        }
    }
    
    //    @RequiresApi(api = Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private static void drawTextureView(TextureView textureView, Canvas canvas) {

        int[] textureViewLocation = new int[2];
        textureView.getLocationOnScreen(textureViewLocation);
        Bitmap textureViewBitmap = textureView.getBitmap();

        Size viewSize = new Size(textureView.getWidth(), textureView.getHeight());
        Size videoSize = new Size(((CustomScalableVideoView)textureView).getVideoWidth(), ((CustomScalableVideoView)textureView).getVideoHeight());
        ScaleManager scaleManager = new ScaleManager(viewSize, videoSize);
        Matrix matrix = scaleManager.getScaleMatrix(ScalableType.FIT_CENTER);

        if (textureViewBitmap != null) {
            Paint paint = new Paint();
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_ATOP));
            canvas.drawBitmap(textureViewBitmap, matrix, paint);
            textureViewBitmap.recycle();
        }
    }
}
