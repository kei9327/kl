package com.knowrecorder.develop.opencourse.ocexport;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.Toast;

import com.knowrecorder.Constants.ServerInfo;
import com.knowrecorder.OpenCourse.API.Models.UploadVideo;
import com.knowrecorder.OpenCourse.API.Models.VideoUploadResponse;
import com.knowrecorder.OpenCourse.ApiEndPointInterface;
import com.knowrecorder.OpenCourse.ProgressMessage;
import com.knowrecorder.R;
import com.knowrecorder.develop.file.FilePath;

//import org.apache.commons.compress.archivers.zip.ZipUtil;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OCExportTask extends AsyncTask<Void, ProgressMessage, Boolean> {
    private Context context;
    private UploadVideo uploadVideo;
    private String archiveFilePath;
    private String parentDirectory;
    private Bitmap thumbnailBitmap;
    private ProgressDialog progressDialog;

    public OCExportTask(Context context, UploadVideo uploadVideo,
                        String parentDirectory,
                        String archiveFilePath, Bitmap thumbnailBitmap,
                        ProgressDialog progressDialog) {
        this.context = context;
        this.uploadVideo = uploadVideo;
        this.parentDirectory = parentDirectory;
        this.archiveFilePath = archiveFilePath;
        this.thumbnailBitmap = thumbnailBitmap;
        this.progressDialog = progressDialog;
    }

    @Override
    protected Boolean doInBackground(Void... params) {
        publishProgress(new ProgressMessage(context.getString(R.string.opencourse_upload_step1), true));
        //todo zip Prosess
        try {
            ZipUtils zipUtils = new ZipUtils();
            zipUtils.zip(FilePath.NOTE_FOLDER, archiveFilePath, parentDirectory);
        } catch (Exception e) {
            e.printStackTrace();
        }

        publishProgress(new ProgressMessage(context.getString(R.string.opencourse_upload_step2), true));
        publishProgress(new ProgressMessage(context.getString(R.string.opencourse_upload_step3), true));

        int fileSize = (int) Math.ceil(new File(this.archiveFilePath).length() / 1024f / 1024f);
        uploadVideo.setFilesize(fileSize);
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerInfo.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiEndPointInterface api = retrofit.create(ApiEndPointInterface.class);
        boolean noProblem = true;

        try {

            Response<VideoUploadResponse> response = api.createVideo(ServerInfo.API_KEY, uploadVideo).execute();

            if (response.code() == 201) {  // 201 Created
                int videoId = response.body().getId();
                MediaType MEDIA_TYPE_PNG = MediaType.parse("image/png");
                MediaType MEDIA_TYPE_ZIP = MediaType.parse("application/zip");

                File archivedFile = new File(archiveFilePath);
                RequestBody archivedFileBody = RequestBody.create(MEDIA_TYPE_ZIP, archivedFile);

                Response<ResponseBody> archiveUploadResponse = api.uploadArchive(videoId, ServerInfo.API_KEY, archivedFileBody).execute();
                if (archiveUploadResponse.code() != 201) {
                    Toast.makeText(context, context.getString(R.string.opencourse_upload_error_zip) + archiveUploadResponse.code(), Toast.LENGTH_SHORT).show();
                    noProblem = false;
                }

                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                thumbnailBitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                RequestBody thumbnailBody = RequestBody.create(MEDIA_TYPE_PNG, stream.toByteArray());

                Response<ResponseBody> thumbnailUploadResponse = api.uploadThumbnail(videoId, ServerInfo.API_KEY, thumbnailBody).execute();
                if (thumbnailUploadResponse.code() != 201) {
                    Toast.makeText(context, context.getString(R.string.opencourse_upload_error_thumbnail) + archiveUploadResponse.code(), Toast.LENGTH_SHORT).show();
                    noProblem = false;
                }

                if (noProblem)
                    publishProgress(new ProgressMessage(context.getString(R.string.opencourse_upload_succeed), true));
                else
                    publishProgress(new ProgressMessage(context.getString(R.string.opencourse_upload_error_1), false));

            } else {
                publishProgress(new ProgressMessage(context.getString(R.string.opencourse_upload_error_2) + response.code(), false));
            }
        } catch (IOException e) {
            publishProgress(new ProgressMessage(context.getString(R.string.opencourse_upload_error_3), false));
        }

        return noProblem;
    }

    @Override
    protected void onPreExecute() {
        FilePath.makeFolder(FilePath.TEMP_ZIP_DIRECTORY);
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result)
            progressDialog.dismiss();
    }

    @Override
    protected void onProgressUpdate(ProgressMessage... values) {
        ProgressMessage message = values[0];
        if (message.increase)
            progressDialog.incrementProgressBy(1);
        progressDialog.setMessage(message.msg);
    }
}