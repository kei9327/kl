package com.knowrecorder.develop.fragment.LeftMenu;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.knowrecorder.R;
import com.knowrecorder.Utils.E;
import com.knowrecorder.ViewerListActivity;
import com.knowrecorder.develop.event.EventType;
import com.knowrecorder.rxjava.RxEventFactory;

import java.util.ArrayList;

/**
 * Created by we160303 on 2017-02-02.
 */

public class LeftMenuFragment extends Fragment implements View.OnClickListener{

    private View rootView;

    private TextView btnNewNote;
    private TextView btnOpenNote;
    private TextView btnShareNote;
    private TextView btnOpenCourse;

    private ImageView btnGuide;
    private ImageView btnInfo;
    private ImageView btnHelp;

    private RecyclerView openListView;
    private TextView shareEmptyView;

    private leftMenuController controller;

    private ArrayList<NoteInformation> openList = null;
    private NoteAdapter noteAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.rb_leftmenu_fragment,container,false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        setBindViewId();
        controller = new leftMenuController();
    }

    private void setBindViewId() {
        btnNewNote = (TextView) rootView.findViewById(R.id.left_new);
        btnOpenNote = (TextView) rootView.findViewById(R.id.left_open);
        btnShareNote = (TextView) rootView.findViewById(R.id.left_share);
        btnOpenCourse = (TextView) rootView.findViewById(R.id.left_open_course);
        openListView = (RecyclerView) rootView.findViewById(R.id.open_list);
        shareEmptyView = (TextView) rootView.findViewById(R.id.share_empty);

        btnGuide = (ImageView) rootView.findViewById(R.id.btn_guide);
        btnInfo = (ImageView) rootView.findViewById(R.id.btn_info);
        btnHelp = (ImageView) rootView.findViewById(R.id.btn_help);

        btnNewNote.setOnClickListener(this);
        btnOpenNote.setOnClickListener(this);
        btnShareNote.setOnClickListener(this);
        btnShareNote.setVisibility(View.GONE);
        btnOpenCourse.setOnClickListener(this);

        openList = new ArrayList<>();

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false);
        openListView.setLayoutManager(linearLayoutManager);

        noteAdapter = new NoteAdapter(getContext(), openList);
        openListView.setAdapter(noteAdapter);

        btnGuide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxEventFactory.get().post(new EventType(EventType.OPEN_SERVICE_GUIDE));
            }
        });
        btnInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RxEventFactory.get().post(new EventType(EventType.OPEN_INFOMATION));
            }
        });
        btnHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RxEventFactory.get().post(new EventType(EventType.OPEN_HELP));
            }
        });
    }


    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.left_new :
                showNewNotePopup();
                break;
            case R.id.left_open :
                if(!openListView.isShown())
                    openList(R.id.left_open);
                else
                    closeList(R.id.left_open);
                break;
            case R.id.left_share :
//                openListView(R.id.left_share);
                if(!shareEmptyView.isShown())
                    openList(R.id.left_share);
                else
                    closeList(R.id.left_share);

                break;
            case R.id.left_open_course :
                startActivity(new Intent(getActivity(), ViewerListActivity.class));
                break;
        }

    }

    private void showNewNotePopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(R.string.new_note_confirm);
        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                RxEventFactory.get().post(new EventType(EventType.NEW_NOTE));
            }
        });
        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openList(int id){

        Animation ani = AnimationUtils.loadAnimation(getContext(), R.anim.ani_rb_leftmenu_show);
        if(id == R.id.left_open){
            //todo setOpenList setAdapter
            shareEmptyView.setVisibility(View.GONE);
            openListView.startAnimation(ani);
            openListView.setVisibility(View.VISIBLE);
        }else{
            //todo setSharList setAdapter
            openListView.setVisibility(View.GONE);
            shareEmptyView.startAnimation(ani);
            shareEmptyView.setVisibility(View.VISIBLE);
        }
    }

    public void closeList(final int id) {
        Animation ani = AnimationUtils.loadAnimation(getContext(), R.anim.ani_main_menu_hide);
        ani.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                if(id == R.id.left_open)
                    openListView.setVisibility(View.GONE);
                else
                    shareEmptyView.setVisibility(View.GONE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        if(id == R.id.left_open)
            openListView.startAnimation(ani);
        else
            shareEmptyView.startAnimation(ani);
    }

    public void closeList() {
        openListView.setVisibility(View.GONE);
        shareEmptyView.setVisibility(View.GONE);
    }

    public void getNoteList(){
        controller.getNoteList(openList);
        noteAdapter.notifyDataSetChanged();
    }
}



