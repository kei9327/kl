package com.knowrecorder.PDF;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

import com.github.barteksc.pdfviewer.source.DocumentSource;
import com.github.barteksc.pdfviewer.source.FileSource;
import com.github.barteksc.pdfviewer.source.UriSource;
import com.knowrecorder.Managers.PdfInfoManager;
import com.knowrecorder.Utils.CommonUtils;
import com.shockwave.pdfium.PdfDocument;
import com.shockwave.pdfium.PdfiumCore;

import java.io.File;
import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by we160303 on 2017-01-11.
 */

public class PDFRendererV2 {

    static private PDFRendererV2 mInstance;
    private Context context = null;
    private PdfiumCore pdfiumCore = null;
    private PdfDocument pdfDocument;
    private int totalPage;
    private float width, height;
    private boolean loadComplet = false;
    private String filePath = "";

    public interface OnLoadFinishListener {
            void onFinished();
    }

    private OnLoadFinishListener mListener = null;

    public static PDFRendererV2 getInstance(Context context) {
        if (mInstance == null)
            mInstance = new PDFRendererV2(context);

        return mInstance;
    }

    private PDFRendererV2(Context context) {
        this.context = context;
        pdfiumCore = new PdfiumCore(context);
    }

    public void openPdfFile(String pdfFile) throws IOException {
        if(TextUtils.equals(filePath, pdfFile)) {
            return;
        }

        filePath = pdfFile;
        loadDocument(context, new FileSource(new File(pdfFile)), "", pdfiumCore);
    }
    public void openPdfFile(String pdfFile, OnLoadFinishListener listener) throws IOException {

        if(TextUtils.equals(filePath, pdfFile)) {
            listener.onFinished();
            return;
        }

        filePath = pdfFile;
        loadDocument(context, new FileSource(new File(pdfFile)), "", pdfiumCore);
        this.mListener = listener;
    }

    private void loadDocument(final Context context, final DocumentSource docSource, final String password, final PdfiumCore pdfiumCor) {

        PdfDocument pdfDoc = null;
        try {
            pdfDoc = docSource.createDocument(context, pdfiumCor, password);
            loadComplete(pdfDoc);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    public void loadComplete(PdfDocument pdfDocument){

        loadComplet = true;
        int firstPageIndex = 0;
        totalPage = pdfiumCore.getPageCount(pdfDocument);
        this.pdfDocument = pdfDocument;
        pdfiumCore.openPage(pdfDocument, firstPageIndex, totalPage-1);
        this.width = pdfiumCore.getPageWidth(pdfDocument, firstPageIndex);
        this.height= pdfiumCore.getPageHeight(pdfDocument, firstPageIndex);

        float ratio = CommonUtils.getDisplayRatio(context, this.width, this.height);
        PdfInfoManager.getInstance().appendInfo("pdfWidth", (width*ratio) + "");
        PdfInfoManager.getInstance().appendInfo("pdfHeight", (height*ratio) + "");
        Log.d("PDFRendererV2", "TotalPage : " + totalPage);
        Log.d("PDFRendererV2", "width : " + width);
        Log.d("PDFRendererV2", "height: " + height);
    }

    public Bitmap getPageBitmap(int index){
        float ratio = CommonUtils.getDisplayRatio(context, this.width, this.height);
        float width, height;

        if(ratio > 1 || ratio == 0)
            ratio = 1.f;

        if(this.width == 0){
            width = CommonUtils.getDisplaySize(context,0);
            height = CommonUtils.getDisplaySize(context, 1);
        }else{
            width = this.width;
            height = this.height;
        }

        Bitmap bitmap = Bitmap.createBitmap((int)(width*ratio), (int)(height*ratio),
                Bitmap.Config.ARGB_8888);
        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, index-1 , 0,0,(int)(width*ratio), (int)(height*ratio));

        return bitmap;
    }
    public Bitmap getPageBitmap(int index, float pdfWidth, float pdfHeight){

        Bitmap bitmap = Bitmap.createBitmap((int)pdfWidth, (int)pdfHeight, Bitmap.Config.ARGB_8888);
        pdfiumCore.renderPageBitmap(pdfDocument, bitmap, index-1, 0,0,(int)pdfWidth, (int)pdfHeight);

        return bitmap;
    }


    public int getTotalPage(){return  this.totalPage ; }
    public  boolean isLoadComplet(){return  this.loadComplet ; }


    public int getPdfFilePageCount(String pdfFilePath) throws IOException {
        if(filePath.equals(pdfFilePath))
            return getTotalPage();
        else{
            DocumentSource docSource = new FileSource(new File(pdfFilePath));
            PdfDocument pdfDoc = docSource.createDocument(context, pdfiumCore, "");
            return pdfiumCore.getPageCount(pdfDoc);
        }
    }


}

