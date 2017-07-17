package com.knowrecorder.develop.audio;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.os.Process;
import android.util.Log;

import com.knowrecorder.Audio.AudioFileProcessor;
import com.knowrecorder.develop.ProcessStateModel;
import com.knowrecorder.develop.file.FilePath;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by ssyou on 2016-03-21.
 */
public class AudioRecorder {
    public static final String COMPRESSED_AUDIO_FILE_MIME_TYPE = "audio/mp4a-latm";
    public static final int COMPRESSED_AUDIO_FILE_BIT_RATE = 128000;
    public static final int SAMPLING_RATE = 88200;
    public static final int BUFFER_SIZE = 44100;
    public static final int CODEC_TIMEOUT_IN_MS = 5000;
    private static final int RECORDER_BPP = 16;
    public static final String AUDIO_RECORDER_FILE_EXT_WAV = "_audio1.wav";
    public static final String AUDIO_RECORDER_TEMP_FILE = "_record_temp.raw";

    public static final String AUDIO_REMODELING_FILE_EXT_WAV = "_audio1_edit.wav";
    public static final String AUDIO_REMODELING_TEMP_FILE = "-record_temp_edit.raw";

    private static final int RECORDER_SAMPLERATE = 44100;
    private static final int RECORDER_CHANNELS = AudioFormat.CHANNEL_IN_STEREO;
    private static final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
    private static final String LOG_TAG = "AudioRecorder";
    private static AudioRecorder ourInstance = new AudioRecorder();
    private int bufferRead = 0;

    private static String mFileName;
    private AudioRecord recorder = null;
    private Thread recordingThread = null;
    private boolean isRecording = false;


    private int recorderChannels;
    private int recorderAudioEncoding;
    private int sampleRate;
    private int bufferSize;


    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }

    private long elapsedTime = 0;
    private AudioRecorder() {
        bufferSize = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, 1, RECORDER_AUDIO_ENCODING) * 2;
    }

    public static AudioRecorder getInstance() {
        return ourInstance;
    }

    public static String getFilename() {
        if(ProcessStateModel.getInstanse().isRemodeling()){
            return (FilePath.FILES_DIRECTORY + "edit_" +  mFileName + AUDIO_RECORDER_FILE_EXT_WAV);
        }else {
            return (FilePath.FILES_DIRECTORY + mFileName + AUDIO_RECORDER_FILE_EXT_WAV);
        }
    }

    public static String getTempFilename() {
        if(ProcessStateModel.getInstanse().isRemodeling()){
            return (FilePath.FILES_DIRECTORY + "edit_temp_" +  mFileName + AUDIO_RECORDER_TEMP_FILE);
        }else {
            return (FilePath.FILES_DIRECTORY + "temp_" + mFileName + AUDIO_RECORDER_TEMP_FILE);
        }
    }

    public static void setFileName(String fileName) {
        mFileName = fileName;
    }

    public static void convertWaveToMp4(String inputFilePath, String outputFilePath) {
        // 이미 변환된 파일 있으면 삭제
        File file = new File(outputFilePath);
        if (file.exists())
            file.delete();

        try {
            File inputFile = new File(inputFilePath);
            FileInputStream fis = new FileInputStream(inputFile);

            File outputFile = new File(outputFilePath);
            if (outputFile.exists()) outputFile.delete();

            MediaMuxer mux = new MediaMuxer(outputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

            MediaFormat outputFormat = MediaFormat.createAudioFormat(COMPRESSED_AUDIO_FILE_MIME_TYPE, RECORDER_SAMPLERATE, 2);
            outputFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
            outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, COMPRESSED_AUDIO_FILE_BIT_RATE);
            outputFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, BUFFER_SIZE);

            MediaCodec codec = MediaCodec.createEncoderByType(COMPRESSED_AUDIO_FILE_MIME_TYPE);
            codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            codec.start();

            ByteBuffer[] codecInputBuffers = codec.getInputBuffers(); // Note: Array of buffers
            ByteBuffer[] codecOutputBuffers = codec.getOutputBuffers();

            MediaCodec.BufferInfo outBuffInfo = new MediaCodec.BufferInfo();
            byte[] tempBuffer = new byte[BUFFER_SIZE];
            boolean hasMoreData = true;
            double presentationTimeUs = 0;
            int audioTrackIdx = 0;
            int totalBytesRead = 0;
            int percentComplete = 0;
            do {
                int inputBufIndex = 0;
                while (inputBufIndex != -1 && hasMoreData) {
                    inputBufIndex = codec.dequeueInputBuffer(CODEC_TIMEOUT_IN_MS);

                    if (inputBufIndex >= 0) {
                        ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                        dstBuf.clear();

                        int bytesRead = fis.read(tempBuffer, 0, dstBuf.limit());
                        Log.e("bytesRead", "Readed " + bytesRead);
                        if (bytesRead == -1) { // -1 implies EOS
                            hasMoreData = false;
                            codec.queueInputBuffer(inputBufIndex, 0, 0, (long) presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                        } else {
                            totalBytesRead += bytesRead;
                            dstBuf.put(tempBuffer, 0, bytesRead);
                            codec.queueInputBuffer(inputBufIndex, 0, bytesRead, (long) presentationTimeUs, 0);
                            presentationTimeUs = 1000000L * (totalBytesRead / 4) / RECORDER_SAMPLERATE;
                        }
                    }
                }
                // Drain audio
                int outputBufIndex = 0;
                while (outputBufIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
                    outputBufIndex = codec.dequeueOutputBuffer(outBuffInfo, CODEC_TIMEOUT_IN_MS);
                    if (outputBufIndex >= 0) {
                        ByteBuffer encodedData = codecOutputBuffers[outputBufIndex];
                        encodedData.position(outBuffInfo.offset);
                        encodedData.limit(outBuffInfo.offset + outBuffInfo.size);
                        if ((outBuffInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0 && outBuffInfo.size != 0) {
                            codec.releaseOutputBuffer(outputBufIndex, false);
                        } else {
                            mux.writeSampleData(audioTrackIdx, codecOutputBuffers[outputBufIndex], outBuffInfo);
                            codec.releaseOutputBuffer(outputBufIndex, false);
                        }
                    } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        outputFormat = codec.getOutputFormat();
                        Log.v(LOG_TAG, "Output format changed - " + outputFormat);
                        audioTrackIdx = mux.addTrack(outputFormat);
                        mux.start();
                    } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                        Log.e(LOG_TAG, "Output buffers changed during encode!");
                    } else if (outputBufIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                        // NO OP
                    } else {
                        Log.e(LOG_TAG, "Unknown return code from dequeueOutputBuffer - " + outputBufIndex);
                    }
                }
                percentComplete = (int) Math.round(((float) totalBytesRead / (float) inputFile.length()) * 100.0);
                Log.v(LOG_TAG, "Conversion % - " + percentComplete);
            } while (outBuffInfo.flags != MediaCodec.BUFFER_FLAG_END_OF_STREAM);
            fis.close();
            mux.stop();
            mux.release();
            Log.v(LOG_TAG, "Compression done ...");
        } catch (FileNotFoundException e) {
            Log.e(LOG_TAG, "File not found!", e);
        } catch (IOException e) {
            Log.e(LOG_TAG, "IO exception!", e);
        }
    }

    public AudioRecord getRecorder() {
        return recorder;
    }

    public void startRecording(final boolean b) {
        bufferRead = 0;
        recorder = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING, bufferSize);
        if(recorder.getState() == AudioRecord.STATE_INITIALIZED) {
//        recorder = findAudioRecord();
            recorder.startRecording();
            isRecording = true;
            recordingThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Process.setThreadPriority(Process.THREAD_PRIORITY_URGENT_AUDIO);
                    writeAudioDataToFile(b);
                }
            }, "AudioRecorder Thread");

            recordingThread.start();
        }else{
            Log.d("AudioRecorder","Audio Initialized fail");
        }
    }

    public void stopRecording() {
        if (recorder != null) {
            isRecording = false;

            recorder.stop();
            recorder.release();

            recorder = null;
            recordingThread = null;
        }

        copyWaveFile(getTempFilename(), getFilename());
    }

    public long getElapsedTime() {
        return elapsedTime + (long) (((double) bufferRead / (RECORDER_SAMPLERATE * 2 * 2)) * 1000);

        //( bufferRead / (RECORDER_SAMPLERATE * 2 * 2)) * 1000
    }

    private void writeAudioDataToFile(boolean b) {
        byte data[] = new byte[bufferSize];
        String filename = getTempFilename();
        FileOutputStream os = null;

        try {
            os = new FileOutputStream(filename, b);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        int read;

        if (os != null) {
            while (isRecording) {
                read = recorder.read(data, 0, bufferSize);
                bufferRead += read;

                if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                    try {
                        os.write(data);
//                        Log.d(LOG_TAG, "" + os.getChannel().size() / (2 * 2 * RECORDER_SAMPLERATE));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void copyWaveFile(String inFilename, String outFilename) {
        FileInputStream in;
        FileOutputStream out;
        long totalAudioLen;
        long totalDataLen;
        long longSampleRate = RECORDER_SAMPLERATE;
        int channels = 2;
        long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * channels / 8;

        byte[] data = new byte[bufferSize];

        try {
            in = new FileInputStream(inFilename);
            out = new FileOutputStream(outFilename);
            totalAudioLen = in.getChannel().size();
            totalDataLen = totalAudioLen + 44;

            AudioFileProcessor.WriteWaveFileHeader(out, totalAudioLen, totalDataLen,
                    longSampleRate, channels, byteRate);

            while (in.read(data) != -1) {
                out.write(data);
            }

            in.close();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static class ConvertTask extends AsyncTask<String, Void, Void> {

        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Void doInBackground(String... params) {
            Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
            try {
                String filePath = params[0];
                File inputFile = new File(filePath);
                FileInputStream fis = new FileInputStream(inputFile);

                filePath = params[1];
                File outputFile = new File(filePath);
                if (outputFile.exists()) outputFile.delete();

                MediaMuxer mux = new MediaMuxer(outputFile.getAbsolutePath(), MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4);

                MediaFormat outputFormat = MediaFormat.createAudioFormat(COMPRESSED_AUDIO_FILE_MIME_TYPE, RECORDER_SAMPLERATE, 2);
                outputFormat.setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC);
                outputFormat.setInteger(MediaFormat.KEY_BIT_RATE, COMPRESSED_AUDIO_FILE_BIT_RATE);
                outputFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, BUFFER_SIZE);

                MediaCodec codec = MediaCodec.createEncoderByType(COMPRESSED_AUDIO_FILE_MIME_TYPE);
                codec.configure(outputFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
                codec.start();

                ByteBuffer[] codecInputBuffers = codec.getInputBuffers(); // Note: Array of buffers
                ByteBuffer[] codecOutputBuffers = codec.getOutputBuffers();

                MediaCodec.BufferInfo outBuffInfo = new MediaCodec.BufferInfo();
                byte[] tempBuffer = new byte[BUFFER_SIZE];
                boolean hasMoreData = true;
                double presentationTimeUs = 0;
                int audioTrackIdx = 0;
                int totalBytesRead = 0;
                int percentComplete = 0;
                do {
                    int inputBufIndex = 0;
                    while (inputBufIndex != -1 && hasMoreData) {
                        inputBufIndex = codec.dequeueInputBuffer(CODEC_TIMEOUT_IN_MS);

                        if (inputBufIndex >= 0) {
                            ByteBuffer dstBuf = codecInputBuffers[inputBufIndex];
                            dstBuf.clear();

                            int bytesRead = fis.read(tempBuffer, 0, dstBuf.limit());
                            Log.e("bytesRead", "Readed " + bytesRead);
                            if (bytesRead == -1) { // -1 implies EOS
                                hasMoreData = false;
                                codec.queueInputBuffer(inputBufIndex, 0, 0, (long) presentationTimeUs, MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                            } else {
                                totalBytesRead += bytesRead;
                                dstBuf.put(tempBuffer, 0, bytesRead);
                                codec.queueInputBuffer(inputBufIndex, 0, bytesRead, (long) presentationTimeUs, 0);
                                presentationTimeUs = 1000000L * (totalBytesRead / 4) / RECORDER_SAMPLERATE;
                            }
                        }
                    }
                    // Drain audio
                    int outputBufIndex = 0;
                    while (outputBufIndex != MediaCodec.INFO_TRY_AGAIN_LATER) {
                        outputBufIndex = codec.dequeueOutputBuffer(outBuffInfo, CODEC_TIMEOUT_IN_MS);
                        if (outputBufIndex >= 0) {
                            ByteBuffer encodedData = codecOutputBuffers[outputBufIndex];
                            encodedData.position(outBuffInfo.offset);
                            encodedData.limit(outBuffInfo.offset + outBuffInfo.size);
                            if ((outBuffInfo.flags & MediaCodec.BUFFER_FLAG_CODEC_CONFIG) != 0 && outBuffInfo.size != 0) {
                                codec.releaseOutputBuffer(outputBufIndex, false);
                            } else {
                                mux.writeSampleData(audioTrackIdx, codecOutputBuffers[outputBufIndex], outBuffInfo);
                                codec.releaseOutputBuffer(outputBufIndex, false);
                            }
                        } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                            outputFormat = codec.getOutputFormat();
                            Log.v(LOG_TAG, "Output format changed - " + outputFormat);
                            audioTrackIdx = mux.addTrack(outputFormat);
                            mux.start();
                        } else if (outputBufIndex == MediaCodec.INFO_OUTPUT_BUFFERS_CHANGED) {
                            Log.e(LOG_TAG, "Output buffers changed during encode!");
                        } else if (outputBufIndex == MediaCodec.INFO_TRY_AGAIN_LATER) {
                            // NO OP
                        } else {
                            Log.e(LOG_TAG, "Unknown return code from dequeueOutputBuffer - " + outputBufIndex);
                        }
                    }
                    percentComplete = (int) Math.round(((float) totalBytesRead / (float) inputFile.length()) * 100.0);
                    Log.v(LOG_TAG, "Conversion % - " + percentComplete);
                } while (outBuffInfo.flags != MediaCodec.BUFFER_FLAG_END_OF_STREAM);
                fis.close();
                mux.stop();
                mux.release();
                Log.v(LOG_TAG, "Compression done ...");
            } catch (FileNotFoundException e) {
                Log.e(LOG_TAG, "File not found!", e);
            } catch (IOException e) {
                Log.e(LOG_TAG, "IO exception!", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

        }
    }

    public void flushBuffer() {
        this.bufferRead = 0;
    }
}
