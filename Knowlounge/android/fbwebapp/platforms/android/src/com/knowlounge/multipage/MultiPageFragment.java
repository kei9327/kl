package com.knowlounge.multipage;

import android.content.Context;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.decoration.ItemShadowDecorator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.knowlounge.KnowloungeApplication;
import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.multipage.data.AbstractDataProvider;
import com.knowlounge.rxjava.EventBus;
import com.knowlounge.rxjava.message.MultiPageEvent;

import rx.Subscriber;

/**
 * Created by we160303 on 2016-10-17.
 */

public class MultiPageFragment extends Fragment implements View.OnClickListener{

    private final String TAG = "MultiPageFragment";

    private View rootView;
    private ImageView btnMultiPageBack;
    private TextView textMultiPage;

    private RecyclerView mRecyclerView;
    private RecyclerView.LayoutManager mLayoutManager;
    private DraggableGridItemAdapter mAdapter;
    private RecyclerView.Adapter mWrappedAdapter;
    private RecyclerViewDragDropManager mRecyclerViewDragDropManager;

    private Subscriber<? super Object> mSubscriber = new Subscriber<Object>() {
        @Override
        public void onCompleted() {

        }

        @Override
        public void onError(Throwable e) {

        }

        @Override
        public void onNext(Object o) {
            if(o instanceof MultiPageEvent){
                final MultiPageEvent data = (MultiPageEvent)o;
                Log.d(TAG, "multiPageId : "+data.getData());
                refreshAdapter(data.getTag());
                }
        }
    };

    private void refreshAdapter(final int type) {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(type ==  MultiPageEvent.CURRENT_PAGE){
                    Log.d(TAG, "reFreshAdapter ===>  currentPage Position : " + mAdapter.getCurrentPositon());
                    textMultiPage.setText((mAdapter.getCurrentPositon()+1) + "/" + (mAdapter.getItemCount()-1));
                } else if(type == MultiPageEvent.CHANGE_AUTH) {
                    checkMaster();
                } else if(type == MultiPageEvent.RELOAD_PAGE) {
                    mAdapter.notifyItemChanged(mAdapter.getCurrentPositon());
                } else {
                    checkMaster();
                    mAdapter.setDragStart(false);
                    mAdapter.notifyDataSetChanged();
                }
            }
        });
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.multi_page_fragment, container, false);

        EventBus.get().getBustObservable()
                .subscribe(mSubscriber);

        btnMultiPageBack = (ImageView) rootView.findViewById(R.id.btn_multi_page_back);
        textMultiPage = (TextView) rootView.findViewById(R.id.text_multi_page);
        btnMultiPageBack.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG,"onViewCreated");
        super.onViewCreated(view, savedInstanceState);

        Log.d(TAG,"onViewCreated - 1");
        //noinspection ConstantConditions
        mRecyclerView = (RecyclerView) getView().findViewById(R.id.recycler_view);
        mLayoutManager = new GridLayoutManager(getContext(), 1, KnowloungeApplication.isPhone ? GridLayoutManager.HORIZONTAL : GridLayoutManager.VERTICAL, false);

        Log.d(TAG,"onViewCreated - 2");
        // drag & drop manager
        mRecyclerViewDragDropManager = new RecyclerViewDragDropManager();
        Log.d(TAG,"onViewCreated - 3");

        // Start dragging after long press

        mRecyclerViewDragDropManager.setInitiateOnMove(false);
        mRecyclerViewDragDropManager.setLongPressTimeout(300);
        mRecyclerViewDragDropManager.setDragEdgeScrollSpeed(0.5f);

        Log.d(TAG,"onViewCreated - 4");
        //adapter
        mAdapter = new DraggableGridItemAdapter(getActivity(),getDataProvider());

        Log.d(TAG,"onViewCreated - 5");
        mWrappedAdapter = mRecyclerViewDragDropManager.createWrappedAdapter(mAdapter);      // wrap for dragging
        Log.d(TAG,"onViewCreated - 6");
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setAdapter(mWrappedAdapter);  // requires *wrapped* adapter
        mRecyclerView.setItemAnimator(animator);
        Log.d(TAG,"onViewCreated - 7");
        // additional decorations
        //noinspection StatementWithEmptyBody
        Log.d(TAG,"onViewCreated - 8");

        if (supportsViewElevation()) {
            // Lollipop or later has native drop shadow feature. ItemShadowDecorator is not required.
        } else {
            mRecyclerView.addItemDecoration(new ItemShadowDecorator((NinePatchDrawable) ContextCompat.getDrawable(getContext(), R.drawable.material_shadow_z1)));
        }

        mRecyclerViewDragDropManager.attachRecyclerView(mRecyclerView);
//        animator.setDebug(true);
//        animator.setMoveDuration(2000);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        mRecyclerViewDragDropManager.cancelDrag();
        super.onPause();
    }

    @Override
    public void onDestroy() {

        if(mRecyclerViewDragDropManager != null){
            mRecyclerViewDragDropManager.release();
            mRecyclerViewDragDropManager = null;
        }
        if(mRecyclerView != null){
            mRecyclerView.setItemAnimator(null);
            mRecyclerView.setAdapter(null);
            mRecyclerView = null;
        }
        if(mWrappedAdapter != null){
            WrapperAdapterUtils.releaseAll(mWrappedAdapter);
            mWrappedAdapter = null;
        }
        mAdapter = null;
        mLayoutManager = null;

        mSubscriber.unsubscribe();
        super.onDestroy();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_multi_page_back :
                RoomActivity.activity.showHideMultipage(true);
                break;
        }
    }

    private boolean supportsViewElevation() {
        return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP);
    }

    public AbstractDataProvider getDataProvider() {
        return ((RoomActivity) getActivity()).getMultiPageData();
    }

    private void checkMaster(){
        mRecyclerViewDragDropManager.setInitiateOnLongPress(RoomActivity.activity.getMasterFlag() && !RoomActivity.activity.getGuestFlag());
    }

    public void setDataProvider(AbstractDataProvider provider) {
        if(mAdapter != null) {
            mAdapter.setProvider(provider);
        }
    }
}
