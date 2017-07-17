package com.knowrecorder.PaperObjects;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.RectF;
import android.util.AttributeSet;

/**
 * Created by ssyou on 2016-02-12.
 */
public class PDFView extends PaperObject {
    private String pdfFile;
    private int pageNumber = 0;
    private int maxPage = 1;
    public Bitmap bitmap = null;

    public PDFView(Context context) {
        super(context);
    }

    public PDFView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onDraw(Canvas canvas) {
        if (!isDeleted) {
            canvas.save();

            if (bitmap != null) {

                float ratio = (float) bitmap.getWidth() / bitmap.getHeight();
                float width = endX - startX;
                float height = endY - startY;
                endY = endY - (height - (width / ratio));
                RectF rectF = new RectF(startX, startY, endX, endY);

                canvas.drawBitmap(bitmap, null, rectF, null);
            }
            canvas.restore();
        }
    }

    public void setPdfFile(String pdfFile) {
        this.pdfFile = pdfFile;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void resetBitmap() {
        if (bitmap != null) {
            bitmap.recycle();
            bitmap = null;
        }
    }
}
