//package com.knowrecorder.PaperObjects;
//
//import android.content.Context;
//import android.graphics.Canvas;
//import android.util.AttributeSet;
//import android.view.View;
//import android.widget.EditText;
//
//import com.knowrecorder.Papers.ObjectPaper;
//import com.knowrecorder.Managers.Recorder;
//
//public class CustomEditText extends EditText {
//
//    private TextLayout layout;
//    private float touchX = -1, touchY = -1;
//    private boolean alreadyDone = false;
//
//    @Override
//    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
//        super.onSizeChanged(w, h, oldw, oldh);
//
//        if (!alreadyDone && (touchX != -1 && touchY != -1)) {
//            TextLayout.saveTextPacket(ObjectPaper.textId, "Down", (int) touchX, (int) touchY, "", w, h);
//            layout.setId(ObjectPaper.textId);
//            ObjectPaper.textId++;
//            alreadyDone = true;
//        }
//        layout.setSizeOfEditText(w, h);
//    }
//
//    public CustomEditText(Context context) {
//        super(context);
//        init();
//    }
//
//    public CustomEditText(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        init();
//
//    }
//
//    private void init() {
//        this.setFocusableInTouchMode(true);
//        this.setOnFocusChangeListener(new OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                if (hasFocus == true) {
//                    if (Recorder.getInstance().isPlaying() || layout.isOpencourseObject()) {
//                        clearFocus();
//                    } else {
//                        layout.getMoveButton().setVisibility(VISIBLE);
//                    }
//                }
//            }
//        });
//    }
//
//    @Override
//    protected void onDraw(Canvas canvas) {
//        super.onDraw(canvas);
//    }
//
//    public void setLayout(TextLayout layout) {
//        this.layout = layout;
//    }
//
//    public void setTouchCoordinates(float touchX, float touchY) {
//        this.touchX = touchX;
//        this.touchY = touchY;
//    }
//}
