package com.knowrecorder.develop.papers.PaperObjects.text;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Path;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import com.knowrecorder.R;
import com.knowrecorder.develop.ProcessStateModel;
import com.knowrecorder.develop.file.FilePath;
import com.knowrecorder.develop.manager.PageManager;
import com.knowrecorder.develop.model.packetHolder.ObjectControllPacket;
import com.knowrecorder.develop.papers.DrawingPanel;
import com.knowrecorder.develop.papers.ObjectPaperV2;
import com.knowrecorder.develop.papers.PaperObjects.ViewGroupObject;
import com.knowrecorder.develop.utils.PacketUtil;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by we160303 on 2017-02-07.
 */

public class TextLayout extends ViewGroupObject {

    private EditText editText;
    private View view;
    private Context mContext;
    private Path mPath;

    private Timer inputTimer;
    private boolean hasFocused = false;
    private ViewVisibleHandler handler = new ViewVisibleHandler();
    private boolean textChanged = false;

    public TextLayout(Context context) {
        super(context);
        this.mContext = context;
        this.mPath = new Path();
        isMovable = true;
        isScalable = false;
    }

    public TextLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void initView(long id, float posX, float posY){

        mid = id;

        editText = new EditText(mContext);

        FrameLayout.LayoutParams editParams = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        );

        editText.setTextColor(Color.BLACK);

        if(!ProcessStateModel.getInstanse().isPlaying())
            editText.setHint(R.string.enter_text_here);

        editText.setLayoutParams(editParams);
        editText.setBackgroundColor(Color.TRANSPARENT);

        editText.setFocusable(false);
        editText.setClickable(false);

        view = new View(mContext);
        editText.measure(MeasureSpec.UNSPECIFIED, MeasureSpec.UNSPECIFIED);
        mWidth = editText.getMeasuredWidth();
        mHeight = editText.getMeasuredHeight();
        FrameLayout.LayoutParams viewParams = new FrameLayout.LayoutParams(
                editText.getMeasuredWidth(),
                editText.getMeasuredHeight()
        );

//        view.setBackgroundColor(Color.RED);
//        view.setAlpha(0.3f);
        view.setLayoutParams(viewParams);

        addView(editText);
        addView(view);

        editText.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                hasFocused = hasFocus;
                if(hasFocus){
                    ObjectPaperV2.currentFocusedTextMid = mid;
                    tempMid = DrawingPanel.mid.incrementAndGet();
                    textChanged = false;
                    try {
                        view.setVisibility(GONE);
                        editText.setBackgroundResource(R.drawable.img_canvas_textbox);
                        inputTimer.cancel();
                        inputTimer.purge();
                    } catch (NullPointerException ignored) {

                    }
                }else{
                    if(textChanged){
                        //todo 저장
                        ObjectControllPacket controllPacket = new ObjectControllPacket
                                .ObjectControllPacketBuilder()
                                .setType("txtend")
                                .setAction(999)
                                .setTarget(mid)
                                .setContent(editText.getText().toString())
                                .build();
                        PacketUtil.makePacket(tempMid, controllPacket);

                        PageManager.getInstance().currentPageInaddDrawingPacket(tempMid);

                    }else{
                        DrawingPanel.mid.getAndDecrement();
                    }

                    ObjectPaperV2.currentFocusedTextMid = -1;

                    view.setVisibility(VISIBLE);
                    editText.setBackgroundColor(Color.TRANSPARENT);
                    editText.setFocusable(false);
                    editText.setClickable(false);

                    if (editText != null) {
                        InputMethodManager imm = (InputMethodManager)mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                        imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                    }

                }
            }
        });

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(ProcessStateModel.getInstanse().isRecording()){
                    ObjectControllPacket controllPacket = new ObjectControllPacket
                            .ObjectControllPacketBuilder()
                            .setType("txtedit")
                            .setAction(999)
                            .setTarget(mid)
                            .setContent(s.toString())
                            .build();
                    PacketUtil.makePacket(tempMid, controllPacket);
                }
                textChanged = true;
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                TextLayout.super.onTouchEvent(event);
                return false;
            }
        });

        view.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                setInputMode();
            }
        });
        mPosX = posX;
        mPosY = posY;

        moveTo(mPosX, mPosY);
    }

    public void setInputMode()
    {
        view.setVisibility(GONE);
        editText.setFocusable(true);
        editText.setClickable(true);
        editText.setFocusableInTouchMode(true);
        try {
            inputTimer.cancel();
            inputTimer.purge();
        } catch (NullPointerException ignored) {

        }
        inputTimer = new Timer();
        inputTimer.schedule(new ViewVisibleTask(), 100);
    }

    public float getLayoutWidth(){ return this.mWidth ; }
    public float getLayoutHeight(){ return this.mHeight; }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        mPath.reset();
        mPath.moveTo(0, 0);
        mPath.lineTo(w, 0);
        mPath.lineTo(w, h);
        mPath.lineTo(0, h);
        mPath.lineTo(0, 0);
        mPath.lineTo(0, 0);

        setRegion(mPath);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(w,h);
        view.setLayoutParams(params);
    }

    public class ViewVisibleHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            view.setVisibility(View.VISIBLE);

        }
    }

    class ViewVisibleTask extends TimerTask {

        @Override
        public void run() {
            handler.sendEmptyMessage(0);
        }
    }

    public boolean isFocused(){ return this.hasFocused;}
    public void setClearFocus(){ editText.clearFocus();}

    public void setEditText(String s){ editText.setText(s);}

    public EditText getEditText(){
        return editText;
    }

}
