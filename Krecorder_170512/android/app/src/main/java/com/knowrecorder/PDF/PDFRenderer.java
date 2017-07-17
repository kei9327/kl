package com.knowrecorder.PDF;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.pdf.PdfRenderer;
import android.os.ParcelFileDescriptor;
import android.widget.Toast;

import com.knowrecorder.R;
import com.knowrecorder.Toolbox.Toolbox;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Created by ssyou on 2016-02-15.
 */
public class PDFRenderer {

    static private PDFRenderer mInstance = new PDFRenderer();

    private Context context = null;
    private ParcelFileDescriptor mFileDescriptor;
    private PdfRenderer mPdfRenderer;
    private PdfRenderer.Page mCurrentPage;

    public PDFRenderer() {
    }

    static public PDFRenderer getInstance() {
        return mInstance;
    }

    public void initRenderer(Context context) {
        try {
            if (context != null) {
                openRenderer(context);
                this.context = context;
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.pdf_renderer_error + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void openRenderer(Context context) throws IOException {
        try {
            ParcelFileDescriptor parcelFileDescriptor
                    = ParcelFileDescriptor.open(new File(Toolbox.getInstance().getPdfFile()), ParcelFileDescriptor.MODE_READ_ONLY);
            mPdfRenderer = new PdfRenderer(parcelFileDescriptor);
        } catch (NullPointerException e) {
//            e.printStackTrace();
        }
    }

    public void openPdfFile(String pdfFile) throws IOException {
        try {
            ParcelFileDescriptor parcelFileDescriptor = ParcelFileDescriptor.open(new File(pdfFile), ParcelFileDescriptor.MODE_READ_ONLY);
            mPdfRenderer = new PdfRenderer(parcelFileDescriptor);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    private void closeRenderer() throws IOException {
        if (null != mCurrentPage) {
            mCurrentPage.close();
        }
        mPdfRenderer.close();
        mFileDescriptor.close();
    }

    public Bitmap getPage(int index) {
        if (mPdfRenderer.getPageCount() <= index) {
            return null;
        }

        if (null != mCurrentPage) {
            mCurrentPage.close();
        }

        mCurrentPage = mPdfRenderer.openPage(index);
        Bitmap bitmap = Bitmap.createBitmap(mCurrentPage.getWidth(), mCurrentPage.getHeight(),
                Bitmap.Config.ARGB_8888);

        mCurrentPage.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY);

        return bitmap;
    }

    public PdfRenderer getPdfRenderer() {
        return mPdfRenderer;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public static int getPdfFilePageCount(String pdfFilePath) {
        PdfRenderer pdfRenderer = null;
        ParcelFileDescriptor parcelFileDescriptor
                = null;
        try {
            parcelFileDescriptor = ParcelFileDescriptor.open(new File(pdfFilePath), ParcelFileDescriptor.MODE_READ_ONLY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        try {
            pdfRenderer = new PdfRenderer(parcelFileDescriptor);
        } catch (IOException e) {;
            e.printStackTrace();
        }

        int count = (pdfRenderer == null) ? -1 : pdfRenderer.getPageCount();
        pdfRenderer.close();

        try {
            parcelFileDescriptor.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return count;
    }
}
