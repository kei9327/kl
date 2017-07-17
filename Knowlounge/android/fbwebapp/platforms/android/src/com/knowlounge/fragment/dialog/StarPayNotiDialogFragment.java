package com.knowlounge.fragment.dialog;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;

import com.knowlounge.MainActivity;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.manager.WenotePreferenceManager;


/**
 * Created by Minsu on 2016-04-07.
 */
public class StarPayNotiDialogFragment extends DialogFragment {

    private String TAG = "StarPayNotiDialogFragment";

    private String type = "";
    private String master = "";
    private WenotePreferenceManager prefManager;

    public interface SetOnStarPayDialogFragment {
        public void dialogDismiss();
    }
    public static void setStarPayNotiDialogFragment(SetOnStarPayDialogFragment listener){
        mCallback = listener;
    }
    public static SetOnStarPayDialogFragment mCallback;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        prefManager = WenotePreferenceManager.getInstance(context);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_star_pay_noti, container);

        // Remove dialog title
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);

        // Remove dialog background
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        try {
            type = getArguments().getString("type") == null ? "" : getArguments().getString("type");
        } catch (NullPointerException np) {
            type = "";
        }

        try {
            master = getArguments().getString("master") == null ? "" : getArguments().getString("master");
        } catch (NullPointerException np) {
            master = "";
        }

        ((Button) rootView.findViewById(R.id.btn_pay_star)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (type != null && (type.equals("room") || TextUtils.equals(type, "room_setting") || TextUtils.equals(type, "room_multipage"))) {

//                    Intent intent = new Intent(getActivity(), RoomLeftNavActivity.class);
//                    intent.putExtra("star", "ok");
//                    intent.putExtra("roomcode", RoomActivity.activity.getRoomCode());
//                    intent.putExtra("roomtitle", RoomActivity.activity.getRoomNm());
//                    startActivity(intent);
//                    getDialog().dismiss();
//                    mCallback.dialogDismiss();

                    RoomActivity.activity.openStarShop();
                    getDialog().dismiss();
                    mCallback.dialogDismiss();
                } else {
//                    Intent leftNavIntent = new Intent(getActivity(), MainLeftNavActivity.class);
//                    leftNavIntent.putExtra("star", "ok");
//                    startActivityForResult(leftNavIntent, GlobalCode.CODE_MAIN_NAV_OPEN);
                    MainActivity._instance.openSharShop();
                    getDialog().dismiss();
                    mCallback.dialogDismiss();
                }
//                MainLeftNavActivity.mDrawerLayout.openDrawer(Gravity.LEFT);
//                StarShopFragment starShopFragment = new StarShopFragment();
//                getFragmentManager().beginTransaction().replace(R.id.mainleft_container, starShopFragment).addToBackStack(null).commit();
            }
        });
        ((Button)rootView.findViewById(R.id.btn_pay_cancel)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();
                if(master.equals("true"))
                    mCallback.dialogDismiss();

            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog() == null)
            return;
        getDialog().getWindow().setLayout((int)(290*prefManager.getDensity()), ViewGroup.LayoutParams.WRAP_CONTENT);
    }
}