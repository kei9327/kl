package com.knowrecorder.Toolbox.Types;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;

import com.knowrecorder.R;

import java.util.TreeMap;

public class WLaserPointerType {

    // json형식의 패킷 데이터에서 모양 구별을 위해서 사용됨.
    public static final int CIRCLE = 0;
    public static final int ARROW = 1;
    public static final int HAND = 2;

    private Context context = null;

    // 팝업 메뉴에서 해당 버튼 리소스 아이디
    private final int[] mButtonId = new int[]{R.id.btnCircle, R.id.btnArrow, R.id.btnHand};

    // 화면에 표시될 아이콘의 리소스 아이디
    private TreeMap<Integer, Integer> circles = new TreeMap<Integer, Integer>();
    private TreeMap<Integer, Integer> arrows = new TreeMap<Integer, Integer>();
    private TreeMap<Integer, Integer> hands = new TreeMap<Integer, Integer>();

    public WLaserPointerType(Context context) {

        this.context = context;

        circles.put(getColorResource(R.color.color1), R.drawable.pointer_circle_01);
        circles.put(getColorResource(R.color.color2), R.drawable.pointer_circle_02);
        circles.put(getColorResource(R.color.color3), R.drawable.pointer_circle_03);
        circles.put(getColorResource(R.color.color4), R.drawable.pointer_circle_04);
        circles.put(getColorResource(R.color.color5), R.drawable.pointer_circle_05);
        circles.put(getColorResource(R.color.color6), R.drawable.pointer_circle_06);

        arrows.put(getColorResource(R.color.color1), R.drawable.pointer_arrow_01);
        arrows.put(getColorResource(R.color.color2), R.drawable.pointer_arrow_02);
        arrows.put(getColorResource(R.color.color3), R.drawable.pointer_arrow_03);
        arrows.put(getColorResource(R.color.color4), R.drawable.pointer_arrow_04);
        arrows.put(getColorResource(R.color.color5), R.drawable.pointer_arrow_05);
        arrows.put(getColorResource(R.color.color6), R.drawable.pointer_arrow_06);

        hands.put(getColorResource(R.color.color1), R.drawable.pointer_hand_01);
        hands.put(getColorResource(R.color.color2), R.drawable.pointer_hand_02);
        hands.put(getColorResource(R.color.color3), R.drawable.pointer_hand_03);
        hands.put(getColorResource(R.color.color4), R.drawable.pointer_hand_04);
        hands.put(getColorResource(R.color.color5), R.drawable.pointer_hand_05);
        hands.put(getColorResource(R.color.color6), R.drawable.pointer_hand_06);
    }

    public Bitmap getBitmap(int shape, int color) {
        int iconId = 0;
        if (shape == WLaserPointerType.CIRCLE) {
            iconId = circles.get(new Integer(color));
        } else if (shape == WLaserPointerType.ARROW) {
            iconId = arrows.get(new Integer(color));
        } else if (shape == WLaserPointerType.HAND) {
            iconId = hands.get(new Integer(color));
        }

        BitmapDrawable drawable = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            drawable = (BitmapDrawable) context.getDrawable(iconId);
            return drawable.getBitmap();
        }else{
            return BitmapFactory.decodeResource(context.getResources(),iconId);
        }

    }

    public int getButtonId(int shape) {
        return mButtonId[shape];
    }

    private int getColorResource(int resId) {
        if (context != null) {
            return context.getResources().getColor(resId);
        }
        return 0;
    }
}
