package com.knowrecorder.Toolbox;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.SeekBar;

import com.knowrecorder.R;
import com.knowrecorder.Toolbox.Types.WLaserPointerType;
import com.knowrecorder.Widgets.StrokePreview;
import com.knowrecorder.develop.ProcessStateModel;
import com.knowrecorder.develop.event.EventType;
import com.knowrecorder.rxjava.RxEventFactory;

/**
 * Created by ssyou on 2016-02-01.
 */
public class Toolbox {

    public static final int STROKE_BASE = 4; // in pixel
    public static final int STROKE_MAX = 16;
    public static final int ALPHA_BASE = 127;
    public static final int ALPHA_MAX = 128;
    static private Toolbox mInstance = new Toolbox();
    public int currentStrokeWidth ;
    public int currentStrokeColor = Color.RED;
    public int currentStrokeOpacity;
    public int currentEraserWidth;
    public int currentShapeColor;
    public ShapeType currentShape;
    public RadioButton prevPenColorButton = null;
    public RadioButton prevShapeColorButton = null;
    public RadioButton prevPointerColorButton = null;
    public View.OnClickListener shapeChanged = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String value = (String) (v.getTag());
            currentShape = ShapeType.values()[Integer.parseInt(value)];
        }
    };
    private Tooltype mToolType = Tooltype.NONE;
    private View mPopup = null;
    private Context context = null;
    private StrokePreview strokePreview = null;
    public SeekBar.OnSeekBarChangeListener strokeWidthChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int value = progress + STROKE_BASE;
            currentStrokeWidth = value;
            strokePreview.setStrokeWidth(value);
            strokePreview.invalidate();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            ;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            ;
        }
    };
    public SeekBar.OnSeekBarChangeListener opacityChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            int value = progress + ALPHA_BASE;

            strokePreview.setStrokeOpacity(value);
            currentStrokeOpacity = value;

            strokePreview.invalidate();
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            ;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            ;
        }
    };
    public SeekBar.OnSeekBarChangeListener eraserWidthChanged = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            currentEraserWidth = (progress + STROKE_BASE) * 4;
            strokePreview.setStrokeWidth(currentEraserWidth/2);
            strokePreview.invalidate();

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            ;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            ;
        }
    };
    private String pdfFile = null;
    private String imageFile = null;
    private String videoUri = null;
    private boolean fileLoadable = true;
    private WLaserPointerType laserPointerType;
    private int currentPointer;
    private int currentPointerColor;
    private int currentPointerTag;
    private Bitmap currentPointerBitmap = null;
    public View.OnClickListener colorChanged = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String value = (String) (v.getTag());
            int colorValue = Color.parseColor(value);

            if (mToolType == Tooltype.PEN) {
                if (prevPenColorButton != null && currentStrokeColor != colorValue)
                    prevPenColorButton.setChecked(false);

                prevPenColorButton = (RadioButton) v;
                currentStrokeColor = colorValue;
                strokePreview.setStrokeColor(colorValue);
                strokePreview.setStrokeOpacity(currentStrokeOpacity);
                strokePreview.invalidate();

            } else if (mToolType == Tooltype.SHAPE) {
                if (prevShapeColorButton != null && currentShapeColor != colorValue)
                    prevShapeColorButton.setChecked(false);

                prevShapeColorButton = (RadioButton) v;
                currentShapeColor = colorValue;
            } else if (mToolType == Tooltype.POINTER) {
                if (prevPointerColorButton != null && currentPointerColor != colorValue)
                    prevPointerColorButton.setChecked(false);
                prevPointerColorButton = (RadioButton) v;
                switchPointer(currentPointer, colorValue);
            }
        }
    };
    public View.OnClickListener pointerTypeListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            if (v.getId() == R.id.btnCircle)
                switchPointer(WLaserPointerType.CIRCLE, currentPointerColor);
            else if (v.getId() == R.id.btnArrow)
                switchPointer(WLaserPointerType.ARROW, currentPointerColor);
            else if (v.getId() == R.id.btnHand)
                switchPointer(WLaserPointerType.HAND, currentPointerColor);
        }
    };
    private View openNotePopup = null;
    private View shareNotePopup = null;
    private RelativeLayout controlPanel = null;
    private int undoTop;
    private int objectUndoTop;

    public Toolbox() {
        initToolbox();
    }

    static public Toolbox getInstance() {
        if (mInstance == null)
            mInstance = new Toolbox();

        return mInstance;
    }

    public void initToolbox(){
        currentStrokeColor = Color.parseColor("#FA140A");
        currentStrokeWidth = 10;
        currentEraserWidth = 10;
        currentStrokeOpacity = 178;
        currentShapeColor = Color.parseColor("#000000");
        currentPointer = 0;
        currentPointerColor = Color.parseColor("#FA140A");
        currentShape = ShapeType.RECTANGLE;
        undoTop = -1;
        objectUndoTop = -1;
    }
    public void setPrevPointerColorButton(RadioButton prevPointerColorButton) {
        this.prevPointerColorButton = prevPointerColorButton;
    }

    public WLaserPointerType getLaserPointerType() {
        return laserPointerType;
    }

    public int getCurrentPointer() {
        return currentPointer;
    }
    public int getCurrentPointerTag() {
        return currentPointerTag;
    }

    public int getCurrentPointerColor() {
        return currentPointerColor;
    }

    public View getOpenNotePopup() {
        return openNotePopup;
    }

    public void setOpenNotePopup(View openNotePopup) {
        this.openNotePopup = openNotePopup;
    }

    public StrokePreview getStrokePreview() {
        return strokePreview;
    }

    public void setStrokePreview(StrokePreview strokePreview) {
        this.strokePreview = strokePreview;
    }

    public View getPopup() {
        return mPopup;
    }

    public void setPopup(View mPopup) {
        this.mPopup = mPopup;
    }

    public RelativeLayout getControlPanel() {
        return controlPanel;
    }

    public void setControlPanel(RelativeLayout controlPanel) {
        this.controlPanel = controlPanel;
    }

    public int getUndoTop() {
        return undoTop;
    }

    public void setUndoTop(int undoTop) {
        this.undoTop = undoTop;
    }

    public int getObjectUndoTop() {
        return objectUndoTop;
    }

    public void setObjectUndoTop(int objectUndoTop) {
        this.objectUndoTop = objectUndoTop;
    }

    public Tooltype getToolType() {
        return mToolType;
    }

    public void setToolType(Tooltype type) {
        mToolType = type;

        if(!ProcessStateModel.getInstanse().isPlaying())
            RxEventFactory.get().post(new EventType(EventType.CHANGE_TOOLTYPE));
    }

    public void setContext(Context context) {
        this.context = context;
        laserPointerType = new WLaserPointerType(context);
        switchPointer(currentPointer, currentPointerColor);
    }

    public void closePopup() {
        if (mPopup != null) {
            controlPanel.removeView(mPopup);
        }
    }

    public void closeOpenNotePopup() {
        if (openNotePopup != null) {
            controlPanel.removeView(openNotePopup);
        }
    }

    public String getPdfFile() {
        return pdfFile;
    }

    public void setPdfFile(String pdfFile) {
        this.pdfFile = pdfFile;
    }

    public String getImageFile() {
        return imageFile;
    }

    public void setImageFile(String imageFile) {
        this.imageFile = imageFile;
    }

    public boolean isFileLoadable() {
        return fileLoadable;
    }

    public void setFileLoadable(boolean fileLoadable) {
        this.fileLoadable = fileLoadable;
    }

    public Bitmap getCurrentPointerBitmap() {
        return currentPointerBitmap;
    }

    public void switchPointer(int newPointer, int color) {
        this.currentPointer = newPointer;
        this.currentPointerColor = color;
        this.currentPointerTag = getPointerTag(color);
        this.currentPointerBitmap = laserPointerType.getBitmap(currentPointer, color);
    }

    public String getVideoUri() {
        return videoUri;
    }

    public void setVideoUri(String videoUri) {
        this.videoUri = videoUri;
    }

    public View getShareNotePopup() {
        return shareNotePopup;
    }

    public void setShareNotePopup(View shareNotePopup) {
        this.shareNotePopup = shareNotePopup;
    }

    private int getPointerTag(int color){

        if(color == getColorResource(R.color.color1)){
            return 1;
        }
        if(color == getColorResource(R.color.color2)){
            return 2;
        }
        if(color == getColorResource(R.color.color3)){
            return 3;
        }
        if(color == getColorResource(R.color.color4)){
            return 4;
        }
        if(color == getColorResource(R.color.color5)){
            return 5;
        }
        if(color == getColorResource(R.color.color6)){
            return 6;
        }
        return 0;
    }

    private int getColorResource(int resId) {
        if (context != null) {
            return context.getResources().getColor(resId);
        }
        return 0;
    }

    public enum Tooltype {
        OBJECT,
        PALM,
        FINGER,
        POINTER,
        PEN,
        TEXT,
        ERASER,
        IMAGE,
        SHAPE,
        PDF,
        REMOVE,
        NONE,
        VIDEO
    }

    public enum ShapeType {
        CIRCLE,
        TRIANGLE,
        RECTANGLE,
        STAR,
        STRAIGHT,
        ARROW
    }
}
