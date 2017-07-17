//package com.knowrecorder.PaperObjects;
//
//import android.content.Context;
//import android.util.AttributeSet;
//import android.view.MotionEvent;
//import android.widget.ImageView;
//
//import com.knowrecorder.Audio.AudioRecorder;
//import com.knowrecorder.KnowRecorderApplication;
//import com.knowrecorder.Managers.NoteManager;
//import com.knowrecorder.Managers.PaperManager;
//import com.knowrecorder.Managers.Recorder;
//
//import io.realm.Realm;
//
//public class VideoMoveButton extends ImageView {
//
//    private VideoLayout layout;
//
//    public VideoMoveButton(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public VideoMoveButton(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//    public VideoMoveButton(Context context) {
//        super(context);
//    }
//
//    public boolean onTouchEvent(MotionEvent event) {
//        if (!Recorder.getInstance().isPlaying()) {
//            int touchX = (int) event.getRawX();
//            int touchY = (int) event.getRawY();
//
//            int action = event.getAction();
//
//            switch (action) {
//                case MotionEvent.ACTION_DOWN:
//                    layout.getMediaController().hide();
//                    savePacket("Move_Down");
//                    break;
//
//                case MotionEvent.ACTION_MOVE:
//                    if (Recorder.getInstance().isRecord()) {
//                        layout.adjustPosition(touchX, touchY, true);
//                    } else {
//                        layout.adjustPosition(touchX, touchY);
//                    }
//
//                    break;
//
//                case MotionEvent.ACTION_UP:
//                    layout.adjustPosition(touchX, touchY, true, false, "Move_Up");
//                    break;
//            }
//        }
//
//        return true;
//    }
//
//    public VideoLayout getLayout() {
//        return layout;
//    }
//
//    public void setLayout(VideoLayout layout) {
//        this.layout = layout;
//    }
//
//    private void savePacket(String event) {
//        VideoModel videoModel = new VideoModel();
//        videoModel.setEvent(event);
//
//        final Packet packet = new Packet();
//        packet.setCommand("Video");
//        packet.setTimeStamp(System.currentTimeMillis());
//
//        if (Recorder.getInstance().isRecord()) {
//            long relativeTimeflow = AudioRecorder.getInstance().getElapsedTime();
//            packet.setRelativeTimeflow(relativeTimeflow);
//
//            KnowRecorderApplication.setLastRecordTime(relativeTimeflow);
//            packet.setIsRecord(true);
//        } else {
//            if (KnowRecorderApplication.getLastRecordTime() > 0) {
//                packet.setRelativeTimeflow(KnowRecorderApplication.getLastRecordTime());
//            }
//        }
//
//        packet.setVideoModel(videoModel);
//        packet.setPageNumber(PaperManager.getInstance().getCurrentPage());
//        packet.setId(KnowRecorderApplication.primaryKeyValue.incrementAndGet());
//        packet.setNoteId(NoteManager.getInstance().getNoteId());
//
//        Realm realm = Realm.getDefaultInstance();
//        realm.executeTransaction(new Realm.Transaction() {
//            @Override
//            public void execute(Realm realm) {
//                realm.copyToRealm(packet);
//            }
//        });
//        realm.close();
//    }
//}
