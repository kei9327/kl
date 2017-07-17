package com.knowrecorder.develop.papers.PaperObjects.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Path;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;
import android.util.Log;

import com.knowrecorder.develop.papers.PaperObjects.ViewObject;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by Changha on 2017-02-06.
 */

public class ImageView extends ViewObject {

    private String imageFile;
    private Bitmap bitmap = null;
    Path mPath;

    public ImageView(Context context) {
        super(context);
        mPath = new Path();
        isMovable = true;
        isScalable = true;
        maxScale = 5.0f;
        minScale = 0.1f;
    }

    public ImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initView(long id, float posX, float posY, float width, float height) {

        mid = id;

        mPosX = posX;
        mPosY = posY;
        mWidth = width;
        mHeight = height;

        drawingRectangel(width, height);
        moveTo(mPosX, mPosY);
    }

    private void drawingRectangel(float w, float h){
        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.lineTo(w, 0);
        mPath.lineTo(w, h);
        mPath.lineTo(0, h);
        mPath.lineTo(0, 0);
        mPath.lineTo(0, 0);

        setRegion(mPath);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        drawingRectangel(w, h);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d(TAG+" "+mid, "ImageView onLayout");
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.d(TAG+" "+mid, "ImageView onDraw");
        canvas.save();

        if(bitmap == null){
            try {
                ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(new File(imageFile), ParcelFileDescriptor.MODE_READ_ONLY);
                FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                getBitmpaToFileDescriptor(fileDescriptor);
                parcelFileDescriptor.close();

                setRotatedBitmap(getExifOrientation(imageFile));
            }catch (Exception e ){
                // 확장자 없을경우 추가
                try {
                    ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(new File(imageFile.substring(0, imageFile.length()-4)), ParcelFileDescriptor.MODE_READ_ONLY);
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                    getBitmpaToFileDescriptor(fileDescriptor);
                    parcelFileDescriptor.close();

                    setRotatedBitmap(getExifOrientation(imageFile));
                }catch (Exception e2){
                    e.printStackTrace();
                }

            }
        }

        if (bitmap != null) {
            RectF rectF = new RectF(0, 0, getWidth(), getHeight());
            canvas.drawBitmap(bitmap, null, rectF, null);
        }
        canvas.restore();
    }
    public void setImageFile(String imageFile){ this.imageFile = imageFile ; }
    public void setBitmap(Bitmap bitmap){ this.bitmap = bitmap ; }

    private void getBitmpaToFileDescriptor(FileDescriptor fileDescriptor) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
        options.inSampleSize = calculateInSampleSize(options, 512, 384);
        options.inJustDecodeBounds = false;

        bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
    }

    private int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    private int getExifOrientation(String filepath) {
        ExifInterface exif = null;

        try {
            exif = new ExifInterface(filepath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (exif != null) {
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, -1);
            if (orientation != -1) {
                return orientation == ExifInterface.ORIENTATION_ROTATE_90 ? 90 : orientation == ExifInterface.ORIENTATION_ROTATE_180 ? 180 : orientation == ExifInterface.ORIENTATION_ROTATE_270 ? 270 : 0;
            }
        }

        return 0;
    }

    private void setRotatedBitmap(int degrees) {
        if (degrees != 0 && bitmap != null) {
            Matrix matrix = new Matrix();
            matrix.setRotate(degrees, (float) bitmap.getWidth() / 2, (float) bitmap.getHeight() / 2);
            try {
                Bitmap tempBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                if (bitmap != tempBitmap) {
                    bitmap.recycle();
                    bitmap = tempBitmap;
                }
            } catch (OutOfMemoryError ex) {
                ex.printStackTrace();
            }
        }
    }

}
