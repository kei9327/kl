//package com.knowrecorder.PaperObjects;
//
//import android.content.Context;
//import android.graphics.Color;
//import android.graphics.Rect;
//import android.util.AttributeSet;
//import android.view.MotionEvent;
//import android.view.inputmethod.InputMethodManager;
//import android.widget.FrameLayout;
//
//import com.knowrecorder.Audio.AudioRecorder;
//import com.knowrecorder.KnowRecorderApplication;
//import com.knowrecorder.Managers.NoteManager;
//import com.knowrecorder.Managers.PaperManagerV2;
//import com.knowrecorder.R;
//import com.knowrecorder.Managers.Recorder;
//import com.knowrecorder.Toolbox.Toolbox;
//import com.knowrecorder.Utils.PixelUtil;
//
//import io.realm.Realm;
//
//public class TextLayout extends FrameLayout {
//
//    private Context context;
//
//    public CustomEditText getEditText() {
//        return editText;
//    }
//
//    private CustomEditText editText;
//    private MoveButton moveButton;
//    private int id;
//    private boolean isAlreadyDown = false;
//    private boolean isOpencourseObject = false;
//
//    private int leftMarginOfText, topMarginOfText;
//
//    private int x, y;
//    private int width = 0, height = 0;
//    private int widthOfEditText = 0, heightOfEditText = 0;
//    private int widthOfMoveButton = 0, heightOfMoveButton = 0;
//
//    public TextLayout(Context context) {
//        super(context);
//        this.context = context;
//        init();
//    }
//
//    public TextLayout(Context context, AttributeSet attrs) {
//        super(context, attrs);
//        this.context = context;
//        init();
//    }
//
//    public TextLayout(Context context, AttributeSet attrs, int defStyleAttr) {
//        super(context, attrs, defStyleAttr);
//        this.context = context;
//        init();
//    }
//
//    public void init() {
//        editText = new CustomEditText(context);
//        moveButton = new MoveButton(context);
//
//        editText.setLayout(this);
//
//        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//                FrameLayout.LayoutParams.WRAP_CONTENT,
//                FrameLayout.LayoutParams.WRAP_CONTENT
//        );
//
//        editText.setTextColor(Color.BLACK);
//        editText.setText(R.string.enter_text_here);
//        editText.setLayoutParams(params);
//
//        params = new FrameLayout.LayoutParams(
//                (int) PixelUtil.getInstance().convertDpToPixel(25),
//                (int) PixelUtil.getInstance().convertDpToPixel(25)
//        );
//
//        moveButton.setLayoutParams(params);
//        moveButton.setBackgroundResource(R.drawable.move);
//        moveButton.setLayout(this);
//
//        this.setFocusable(true);
//        this.setFocusableInTouchMode(true);
//
//        addView(moveButton);
//        addView(editText);
//    }
//
//    public void adjustPosition(int touchX, int touchY) {
//        adjustPosition(touchX, touchY, false);
//    }
//
//    public void adjustPosition(int touchX, int touchY, boolean record) {
//        int textTop =  touchY - moveButton.getHeight() - (moveButton.getHeight() / 2);
//        int textLeft = touchX + (int) PixelUtil.getInstance().convertDpToPixel(35) - (moveButton.getWidth() / 2);
//
//        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) editText.getLayoutParams();
//        params.topMargin = textTop;
//        params.leftMargin = textLeft;
//        editText.setLayoutParams(params);
//
//        int btnTop = touchY - moveButton.getHeight() - (moveButton.getHeight() / 2);
//        int btnLeft = touchX - (moveButton.getWidth() / 2);
//
//        params = (FrameLayout.LayoutParams) moveButton.getLayoutParams();
//        params.topMargin = btnTop;
//        params.leftMargin = btnLeft;
//        moveButton.setLayoutParams(params);
//
//        if (record) {
//            saveTextPacket(this.id, "Move", touchX, touchY, "", textTop, btnTop, textLeft, btnLeft, editText.getWidth(), editText.getHeight());
//        }
//
//        topMarginOfText = textTop;
//        leftMarginOfText = textLeft;
//    }
//
//    public void adjustPositionWithDimension(int touchX, int touchY) {
//        heightOfMoveButton = (int) PixelUtil.getInstance().convertDpToPixel(25);
//        widthOfMoveButton = (int) PixelUtil.getInstance().convertDpToPixel(25);
//
//        int textTop =  touchY - heightOfMoveButton - (heightOfMoveButton / 2);
//        int textLeft = touchX + (int) PixelUtil.getInstance().convertDpToPixel(35) - (widthOfMoveButton / 2);
//
//        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) editText.getLayoutParams();
//        params.topMargin = textTop;
//        params.leftMargin = textLeft;
//        editText.setLayoutParams(params);
//
//        int btnTop = touchY - heightOfMoveButton - (heightOfMoveButton / 2);
//        int btnLeft = touchX - (widthOfMoveButton / 2);
//
//        params = (FrameLayout.LayoutParams) moveButton.getLayoutParams();
//        params.topMargin = btnTop;
//        params.leftMargin = btnLeft;
//        moveButton.setLayoutParams(params);
//
//        topMarginOfText = textTop;
//        leftMarginOfText = textLeft;
//    }
//
//    public void adjustMargin(int textTop, int textLeft, int btnTop, int btnLeft) {
//        FrameLayout.LayoutParams textParams = (FrameLayout.LayoutParams) editText.getLayoutParams();
//        textParams.topMargin = textTop;
//        textParams.leftMargin = textLeft;
//        editText.setLayoutParams(textParams);
//
//        FrameLayout.LayoutParams btnParams = (FrameLayout.LayoutParams) moveButton.getLayoutParams();
//        btnParams.topMargin = btnTop;
//        btnParams.leftMargin = btnLeft;
//        moveButton.setLayoutParams(btnParams);
//
//        topMarginOfText = textTop;
//        leftMarginOfText = textLeft;
//    }
//
//    public boolean dispatchTouchEvent(MotionEvent event) {
//        if (event.getAction() == MotionEvent.ACTION_DOWN) {
//            if (Toolbox.getInstance().getToolType() == Toolbox.Tooltype.REMOVE) {
//
//                if (!isAlreadyDown) {
//                    PaperManagerV2.getInstance().getObjectPaper()
//                            .deleteShape(event.getRawX(), event.getRawY(), false);
//                    PaperManagerV2.getInstance().getObjectPaper()
//                            .saveObjectPacket("Shape", "Delete", 0, 0, event.getRawX(), event.getRawY(), 0);
//                    isAlreadyDown = true;
//                }
//                return true;
//            }
//
//            Rect outRect = new Rect();
//            Rect outRect2 = new Rect();
//            editText.getGlobalVisibleRect(outRect);
//            moveButton.getGlobalVisibleRect(outRect2);
//
//            if (editText.isFocused()) {
//                if (!outRect.contains((int)event.getRawX(), (int)event.getRawY())
//                        && !outRect2.contains((int)event.getRawX(), (int)event.getRawY())) {
//
//                    // 텍스트박스 밖을 클릭했을 때
//
//                    moveButton.setVisibility(GONE);
//                    editText.clearFocus();
//                    this.requestFocus();
//                    InputMethodManager imm = (InputMethodManager) this.getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
//                    imm.hideSoftInputFromWindow(this.getWindowToken(), 0);
//
//                    if (editText.getText().toString().equals("")) {
//                        editText.setText(context.getString(R.string.enter_text_here));
//                    }
//
//                    saveTextPacket(this.id, "Text", 0, 0, editText.getText().toString(), editText.getWidth(), editText.getHeight());
//                }
//            } else if (outRect.contains((int)event.getRawX(), (int)event.getRawY())) {
//                if (editText.getText().toString().equals(context.getString(R.string.enter_text_here))) {
//                    editText.setText("");
//                    this.clearFocus();
//                    editText.requestFocus();
//                }
//            }
//        } else if (event.getAction() == MotionEvent.ACTION_UP) {
//            isAlreadyDown = false;
//        }
//
//        return super.dispatchTouchEvent(event);
//    }
//
//    public void changeText(String text) {
//        editText.setText(text);
//    }
//
//    public MoveButton getMoveButton() {
//        return moveButton;
//    }
//
//    public static void saveTextPacket(int id, String event, int x, int y, String text) {
//        saveTextPacket(id, event, x, y, text, 0, 0, 0, 0);
//    }
//
//    public static void  saveTextPacket(int id, String event, int x, int y, String text, int width, int height) {
//        saveTextPacket(id, event, x, y, text, 0, 0, 0, 0, width, height);
//    }
//
//    public static void saveTextPacket(int id, String event, int x, int y, String text,
//                                      int textTop, int btnTop, int textLeft, int btnLeft) {
//        saveTextPacket(id, event, x, y, text, textTop, btnTop, textLeft, btnLeft, 0, 0);
//    }
//
//    public static void saveTextPacket(int id, String event, int x, int y, String text,
//                                      int textTop, int btnTop, int textLeft, int btnLeft,
//                                      int width, int height) {
//        TextModel textModel = new TextModel();
//        textModel.setId(id);
//        textModel.setTouchX(PixelUtil.getInstance().pixelToDp(x));
//        textModel.setTouchY(PixelUtil.getInstance().pixelToDp(y));
//        textModel.setText(text);
//        textModel.setEvent(event);
//
//        textModel.setTextLeft(PixelUtil.getInstance().pixelToDp(textLeft));
//        textModel.setBtnLeft(PixelUtil.getInstance().pixelToDp(btnLeft));
//        textModel.setTextTop(PixelUtil.getInstance().pixelToDp(textTop));
//        textModel.setBtnTop(PixelUtil .getInstance().pixelToDp(btnTop));
//
//        textModel.setWidth(PixelUtil.getInstance().pixelToDp(width));
//        textModel.setHeight(PixelUtil.getInstance().pixelToDp(height));
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
//        packet.setPageNumber(PaperManagerV2.getInstance().getCurrentPage());
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
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    public void setCoordinates(int x, int y) {
//        this.x = x;
//        this.y = y;
//    }
//
//    public int getCoordX() {
//        return x;
//    }
//
//    public int getCoordY() {
//        return y;
//    }
//
//    public boolean isInThisArea(float x, float y, boolean isAutoload) {
//        boolean ret = false;
//
//        int left, top, right, bottom;
//
//        if (isAutoload) {
//            left = leftMarginOfText;
//            top = topMarginOfText;
//            right = leftMarginOfText + width;
//            bottom = topMarginOfText + height;
//        } else {
//            left = leftMarginOfText;
//            top = topMarginOfText;
//            right = leftMarginOfText + editText.getWidth();
//            bottom = topMarginOfText + editText.getHeight();
//        }
//
//        if (x <= right && x >= left
//                && y <= bottom && y >= top) {
//            ret = true;
//        }
//
//        return ret;
//    }
//
//    public void setDimension(int width, int height) {
//        this.width = width;
//        this.height = height;
//    }
//
//    public void setSizeOfEditText(int width, int height) {
//        this.widthOfEditText = width;
//        this.heightOfEditText = height;
//    }
//
//    public void setSizeOfMoveButton(int width, int height) {
//        this.widthOfMoveButton = width;
//        this.heightOfMoveButton = height;
//    }
//
//    public int getWidthOfEditText() {
//        return widthOfEditText;
//    }
//
//    public int getHeightOfEditText() {
//        return heightOfEditText;
//    }
//
//    public int getWidthOfMoveButton() {
//        return widthOfMoveButton;
//    }
//
//    public int getHeightOfMoveButton() {
//        return heightOfMoveButton;
//    }
//
//    public void setOpencourseObject(boolean opencourseObject) {
//        isOpencourseObject = opencourseObject;
//        this.moveButton.setEnabled(false);
//        this.editText.setEnabled(false);
//    }
//
//    public boolean isOpencourseObject() {
//        return isOpencourseObject;
//    }
//}