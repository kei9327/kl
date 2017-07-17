//package com.knowrecorder.PaperObjects;
//
//import android.content.Context;
//import android.util.AttributeSet;
//import android.view.MotionEvent;
//import android.widget.ImageView;
//
//import com.knowrecorder.Audio.AudioRecorder;
//import com.knowrecorder.Managers.NoteManager;
//import com.knowrecorder.Managers.PaperManager;
//import com.knowrecorder.Managers.Recorder;
//import com.knowrecorder.KnowRecorderApplication;
//
//import io.realm.Realm;
//
//public class MoveButton extends ImageView {
//
//    private TextLayout layout;
//    private float touchX, touchY;
//
//    public MoveButton(Context context, AttributeSet attrs) {
//        super(context, attrs);
//    }
//
//    public MoveButton(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//    }
//
//    public MoveButton(Context context) {
//        super(context);
//    }
//
//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//
//        layout.setSizeOfMoveButton(w, h);
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
//                    layout.adjustPosition(touchX, touchY);
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
//                    layout.adjustPosition(touchX, touchY, true);
//                    savePacket("Move_Up");
//                    break;
//            }
//        }
//
//        return true;
//    }
//
//    public TextLayout getLayout() {
//        return layout;
//    }
//
//    public void setLayout(TextLayout layout) {
//        this.layout = layout;
//    }
//
//    private void savePacket(String event) {
//        TextModel textModel = new TextModel();
//        textModel.setEvent(event);
//
//        final Packet packet = new Packet();
//        packet.setCommand("Text");
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
//        packet.setId(KnowRecorderApplication.primaryKeyValue.incrementAndGet());
//        packet.setTextModel(textModel);
//        packet.setPageNumber(PaperManager.getInstance().getCurrentPage());
//
//        packet.setNoteId(NoteManager.getInstance().getNoteId());
//
//        Realm realm = Realm.getDefaultInstance();
//        realm.executeTransactionAsync(new Realm.Transaction() {
//            @Override
//            public void execute(Realm bgRealm) {
//                bgRealm.copyToRealm(packet);
//            }
//        }, null, null);
//        realm.close();
//    }
//
//    public void setTouchCoordinates(float touchX, float touchY) {
//        this.touchX = touchX;
//        this.touchY = touchY;
//    }
//}
