package com.knowrecorder.PaperObjects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.ParcelFileDescriptor;
import android.util.AttributeSet;

import com.knowrecorder.Toolbox.Toolbox;

import java.io.File;
import java.io.FileDescriptor;
import java.io.IOException;

/**
 * Created by ssyou on 2016-02-12.
 */
public class WemageView extends PaperObject {
    private String imageFile;
    private Bitmap bitmap = null;
    private float ratio = 0f;

    public WemageView(Context context) {
        super(context);
    }

    public WemageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!isDeleted) {
            canvas.save();

            if (Toolbox.getInstance().isFileLoadable() && bitmap == null) {
                try {
                    ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(new File(imageFile), ParcelFileDescriptor.MODE_READ_ONLY);
                    FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();

                    getBitmpaToFileDescriptor(fileDescriptor);
                    parcelFileDescriptor.close();
                    setRotatedBitmap(getExifOrientation(imageFile));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            if (bitmap != null) {

                if (ratio == 0f) {
                    ratio = (float) bitmap.getWidth() / bitmap.getHeight();
                }

                float width = endX - startX;
                float height = endY - startY;
                endY = endY - (height - (width / ratio));
                RectF rectF = new RectF(startX, startY, endX, endY);
                canvas.drawBitmap(bitmap, null, rectF, null);
            }
            canvas.restore();
        }
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

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
