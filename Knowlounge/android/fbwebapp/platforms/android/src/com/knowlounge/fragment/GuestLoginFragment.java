package com.knowlounge.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;

/**
 * Created by we160303 on 2016-06-24.
 */
public class GuestLoginFragment extends Fragment {

    View rootView;

    Button guest_join_btn;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.guest_login_fragment, null, false);

        setFindViewById();
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        guest_join_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RoomActivity.activity.onExitRoom();
            }
        });
    }

    private void setFindViewById() {
        guest_join_btn = (Button)rootView.findViewById(R.id.guest_join_btn);
    }

    @Override
    public void onResume() {
        super.onResume();
    }
}
