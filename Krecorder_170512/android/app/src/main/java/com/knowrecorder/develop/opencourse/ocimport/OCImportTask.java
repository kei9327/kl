package com.knowrecorder.develop.opencourse.ocimport;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;

import com.knowrecorder.Constants.ServerInfo;
import com.knowrecorder.Encrypt.SHA1;
import com.knowrecorder.OpenCourse.ApiEndPointInterface;
import com.knowrecorder.OpenCourse.Import.UnzipFile;
import com.knowrecorder.OpenCourse.ProgressMessage;
import com.knowrecorder.R;
import com.knowrecorder.develop.OpenCoursePlayerActivity;
import com.knowrecorder.develop.file.FilePath;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class OCImportTask extends AsyncTask<Void, ProgressMessage, PlayerMetaInfov2> {
    private ProgressDialog progressDialog;
    private Context context;
    private int videoId;
    private static final String LOG_TAG = "OpenCourseImportTask";
    private String title;

    private AsyncTask<Void, Void, Void> downloadStreaming = null;
    private boolean isRunning = false;

    public OCImportTask(Context context, ProgressDialog progressDialog, int videoId, String title) {
        this.context = context;
        this.progressDialog = progressDialog;
        this.videoId = videoId;
        this.title = title;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        isRunning = true;
    }

    @Override
    protected PlayerMetaInfov2 doInBackground(Void... params) {

        if(android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ServerInfo.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiEndPointInterface api = retrofit.create(ApiEndPointInterface.class);
        final String unzipPath;

        publishProgress(new ProgressMessage(context.getString(R.string.opencourse_download_step1), true));

        FilePath.setViewersLowFolder(Integer.toString(videoId));

        String zipPath = FilePath.VIEWER_DIRECTORY;
        File outputFolder = new File(zipPath);
        if (!outputFolder.exists())
            outputFolder.mkdirs();

        unzipPath = zipPath + videoId;
        File unzipFolder = new File(unzipPath);
        if (unzipFolder.exists()){
            if(unzipFolder.listFiles().length > 0 ) {
                //폴더의 정보가 유효하지 않은 상태로 reaml폴더가 존재한다면 그 파일은 재생할 수가 없다.
                //현재는 다운로드 도중 취소했을경우 폴더를 삭제하지만 혹시 단말 전원컷 등으로 제대로 된 정보가 없을 가능성이 있어 realm파일의 유효성을 모두 체크하여 재생 오류시 다시 받는 옵션도 제공해야 할 것으로 보임.
                File directory = unzipFolder.listFiles()[0];
                if(directory != null && directory.isDirectory()) {
                    int length = directory.toString().split("/").length;
                    FilePath.setViewersRealmFolder(directory.toString().split("/")[length - 1]);
                    return (new PlayerMetaInfov2(unzipPath, videoId, title));
                }
            }
        }else{
            unzipFolder.mkdirs();
        }

        zipPath = unzipPath + ".zip";

        Call<ResponseBody> call = api.downloadArchiveStreaming(videoId, ServerInfo.API_KEY);
        final String finalZipPath = zipPath;
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, final Response<ResponseBody> response) {
                if(response.isSuccessful()){
                    Log.d(LOG_TAG, "Got the body for the file");
                    publishProgress(new ProgressMessage(context.getString(R.string.now_in_decompress), true));
                    downloadStreaming = new AsyncTask<Void, Void, Void>(){
                        @Override
                        protected Void doInBackground(Void... params) {

                            if(saveToDisk(response.body(), finalZipPath)){
                                UnzipFile unzip = new UnzipFile(unzipPath + "/", finalZipPath);
                                try {
                                    unzip.unzip();
                                    (new File(finalZipPath)).delete();
                                    playOpenCourse( new PlayerMetaInfov2(unzipPath, videoId, title));
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            return null;
                        }
                    };
                    downloadStreaming.execute();
                }else{
                    Log.d(LOG_TAG, "Connection failed " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
                Log.e(LOG_TAG, t.getMessage());
            }
        });

        return null;
    }

    private void playOpenCourse(PlayerMetaInfov2 info) {
        if (info != null) {
            progressDialog.dismiss();

            Intent intent = new Intent(context, OpenCoursePlayerActivity.class);
            intent.putExtra("RESOURCE_PATH", info.resourcePath);
            intent.putExtra("VIDEO_ID", info.videoId);
            intent.putExtra("VIDEO_TITLE", info.title);

            context.startActivity(intent);
        }
    }

    @Override
    protected void onPostExecute(PlayerMetaInfov2 info) {
        if (info != null) {
            playOpenCourse(info);
        }
    }

    @Override
    protected void onProgressUpdate(ProgressMessage... values) {
        ProgressMessage message = values[0];
        Log.d(LOG_TAG, "onProgressUpdate Message Progress : " + message.progress);
        if (message.increase)
            progressDialog.setProgress(message.progress);
        progressDialog.setMessage(message.msg);
    }

    public boolean saveToDisk(ResponseBody body, String outputPath) {
        try {
            InputStream is = null;
            OutputStream os = null;

            try {
                Log.d(LOG_TAG, "File Size=" + body.contentLength());

                is = body.byteStream();
                os = new FileOutputStream(outputPath);

                byte data[] = new byte[4096];
                int count;
                int progress = 0;
                while ((count = is.read(data)) != -1) {
                    if (!isRunning) {
                        return false;
                    }

                    os.write(data, 0, count);
                    progress +=count;

                    publishProgress(new ProgressMessage(context.getString(R.string.now_in_decompress), true, (int)(((float)progress/body.contentLength())*100) ));
                    Log.d(LOG_TAG, "Progress: " + progress + "/" + body.contentLength() + " >>>> " + (float) progress/body.contentLength());
                }

                os.flush();

                Log.d(LOG_TAG, "File saved successfully!");

                return true;
            } catch (IOException e) {
                e.printStackTrace();
                Log.d(LOG_TAG, "Failed to save the file!");
                return false;
            } finally {
                if (is != null) is.close();
                if (os != null) os.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(LOG_TAG, "Failed to save the file!");
            return false;
        }
    }

    private boolean writeBodyToDisk(ResponseBody body, String outputPath) {
        try {
            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] reader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(new File(outputPath));

                while (true) {
                    int read = inputStream.read(reader);

                    if (read == -1) {   // EOF
                        break;
                    }

                    outputStream.write(reader, 0, read);
                    fileSizeDownloaded += read;
                    publishProgress(new ProgressMessage(context.getString(R.string.now_in_decompress), true, (int)(fileSizeDownloaded*100 / fileSize)));
                    Log.d(LOG_TAG, "file download : " + fileSizeDownloaded + " of " + fileSize);
                }

                outputStream.flush();
                return true;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } finally {
                if (inputStream != null)
                    inputStream.close();

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    private String readFileString(String path) throws IOException {
        StringBuffer buffer = new StringBuffer();
        BufferedReader reader = new BufferedReader(new FileReader(path));
        char[] buf = new char[1024];
        int read = 0;
        while ((read = reader.read(buf)) != -1) {
            String readed = String.valueOf(buf, 0, read);
            buffer.append(readed);
        }
        reader.close();
        return buffer.toString();
    }

    public void threadCancel(){
        isRunning = false;

        if(downloadStreaming != null){
            if(FilePath.VIEWERS_REALM_FOLDER != null) {
                File realmFile = new File(FilePath.VIEWERS_REALM_FOLDER);
                FilePath.deleteRecursive(realmFile);
            }
            downloadStreaming.cancel(true);
        }
        cancel(true);
    }
}

class PlayerMetaInfov2 {
    String resourcePath;
    int videoId;
    String title;

    public PlayerMetaInfov2(String resourcePath, int videoId, String title) {
        this.resourcePath = resourcePath;
        this.videoId = videoId;
        this.title = title;
    }
}