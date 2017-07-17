package com.knowrecorder.phone.tab.Subject;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fenchtose.tooltip.Tooltip;
import com.fenchtose.tooltip.TooltipAnimation;
import com.knowrecorder.R;
import com.knowrecorder.Utils.PixelUtil;
import com.knowrecorder.phone.PhoneOpencourseActivity;
import com.knowrecorder.phone.rxevent.EventCancel;
import com.knowrecorder.rxjava.EventBus;

/**
 * Created by we160303 on 2016-12-02.
 */

public class SortSearchFragment extends Fragment implements View.OnClickListener{

    private final String TAG ="SortSearchFragment";

    private View rootView;
    private TextView sortRecently;
    private TextView sortHit;
    private TextView sortName;
    private View sortRecentlyUnder;
    private View sortHitUnder;

    private View sortNameUnder;
    private TextView menuSelect;

    private EditText search;
    private int currentMenu;

    private String currentSort;

    private Tooltip categoryMenu = null;
    private ViewGroup tootipRoot;

    private View cancel;

    public interface onSortAndSearchListner{
        void onSortAndSearch(boolean isSearch, int menu, String sort, String keyword);
    }
    public static onSortAndSearchListner callback;
    public static void setOnSortAndSearchListner(onSortAndSearchListner listner){
        callback = listner;
    }


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.p_fragment_subject_sort_search, container, false);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        sortRecently = (TextView) rootView.findViewById(R.id.p_op_sort_recently);
        sortHit = (TextView) rootView.findViewById(R.id.p_op_sort_hit);
        sortName = (TextView) rootView.findViewById(R.id.p_op_sort_name);

        sortRecentlyUnder = (View) rootView.findViewById(R.id.p_op_sort_recently_under);
        sortHitUnder = (View) rootView.findViewById(R.id.p_op_sort_hit_under);
        sortNameUnder = (View) rootView.findViewById(R.id.p_op_sort_name_under);

        menuSelect = (TextView) rootView.findViewById(R.id.p_op_menu_select);
        search = (EditText) rootView.findViewById(R.id.p_op_search);
        tootipRoot = (ViewGroup) rootView.findViewById(R.id.subject_tooltip);

        cancel = (View) rootView.findViewById(R.id.p_op_cancel);

        initialization();

        ((LinearLayout)rootView.findViewById(R.id.sort_and_search_layout)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                return;
            }
        });

        search.setImeOptions(EditorInfo.IME_ACTION_DONE);
        search.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if(actionId == EditorInfo.IME_ACTION_DONE || event.getKeyCode() == KeyEvent.KEYCODE_ENTER){
                    callback.onSortAndSearch(true, currentMenu,currentSort,search.getText().toString());
                    search.setText("");
                }
                return false;
            }
        });
        menuSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG,"menuSelect");
                if(categoryMenu != null){
                    categoryMenu = null;
                }
                categoryMenu = createTooltip(menuSelect, Tooltip.BOTTOM);
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EventBus.getInstance().post(new EventCancel());
            }
        });

        sortRecently.setOnClickListener(this);
        sortHit.setOnClickListener(this);
        sortName.setOnClickListener(this);
    }

    private Tooltip createTooltip(View anchor, @Tooltip.Position int position){

        int selectColor = Color.parseColor("#a0c81e");

        LinearLayout content = (LinearLayout) getActivity().getLayoutInflater().inflate(R.layout.tooltip_category_menu, null);
        TextView all = (TextView) content.findViewById(R.id.p_op_subject_all);
        TextView math = (TextView) content.findViewById(R.id.p_op_subject_math);
        TextView science = (TextView) content.findViewById(R.id.p_op_subject_science);
        TextView language = (TextView) content.findViewById(R.id.p_op_subject_languge);
        TextView social = (TextView) content.findViewById(R.id.p_op_subject_social);
        TextView art = (TextView) content.findViewById(R.id.p_op_subject_art);
        TextView others = (TextView) content.findViewById(R.id.p_op_subject_others);
        switch (currentMenu){
            case PhoneOpencourseActivity.ALL :
                all.setTextColor(selectColor);break;
            case PhoneOpencourseActivity.MATH :
                math.setTextColor(selectColor);break;
            case PhoneOpencourseActivity.SCIENCE :
                science.setTextColor(selectColor);break;
            case PhoneOpencourseActivity.LANGUAGE :
                language.setTextColor(selectColor);break;
            case PhoneOpencourseActivity.SOCIAL :
                social.setTextColor(selectColor);break;
            case PhoneOpencourseActivity.ART :
                art.setTextColor(selectColor);break;
            case PhoneOpencourseActivity.OTHERS :
                others.setTextColor(selectColor);break;
        }

        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchMenu(PhoneOpencourseActivity.ALL, getActivity());
            }
        });
        math.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchMenu(PhoneOpencourseActivity.MATH, getActivity());
            }
        });
        science.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchMenu(PhoneOpencourseActivity.SCIENCE, getActivity());
            }
        });
        language.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchMenu(PhoneOpencourseActivity.LANGUAGE, getActivity());
            }
        });
        social.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchMenu(PhoneOpencourseActivity.SOCIAL, getActivity());
            }
        });
        art.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchMenu(PhoneOpencourseActivity.ART, getActivity());
            }
        });
        others.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSearchMenu(PhoneOpencourseActivity.OTHERS, getActivity());
            }
        });
        Tooltip tooltip = new Tooltip.Builder(getContext())
                .anchor(anchor,position)
                .animate(new TooltipAnimation(TooltipAnimation.NONE, 400))
                .autoAdjust(true)
                .content(content)
                .cancelable(true)
                .into(tootipRoot)
                .show();
        return tooltip;
    }

    private void initialization() {
        currentMenu = PhoneOpencourseActivity.ALL;
        currentSort = "recently";
        setSort(currentSort);
        setSearchMenu(currentMenu, getActivity());
    }

    private void setSearchMenu(int menu, FragmentActivity activity) {
        if(categoryMenu != null)
            categoryMenu.dismiss();

        currentMenu = menu;
        switch (menu){
            case PhoneOpencourseActivity.ALL :
                menuSelect.setText(activity.getResources().getString(R.string.opencourse_menu_all));
                break;
            case PhoneOpencourseActivity.MATH:
                menuSelect.setText(activity.getResources().getString(R.string.opencourse_menu_math));
                break;
            case PhoneOpencourseActivity.SCIENCE:
                menuSelect.setText(activity.getResources().getString(R.string.opencourse_menu_science));
                break;
            case PhoneOpencourseActivity.LANGUAGE:
                menuSelect.setText(activity.getResources().getString(R.string.opencourse_menu_language));
                break;
            case PhoneOpencourseActivity.SOCIAL:
                menuSelect.setText(activity.getResources().getString(R.string.opencourse_menu_social));
                break;
            case PhoneOpencourseActivity.ART:
                menuSelect.setText(activity.getResources().getString(R.string.opencourse_menu_art));
                break;
            case PhoneOpencourseActivity.OTHERS:
                menuSelect.setText(activity.getResources().getString(R.string.opencourse_menu_others));
                break;
        }
    }
    private void setSort(String sort) {
        currentSort = sort;

        sortRecently.setTextColor(Color.parseColor("#646464"));
        sortRecentlyUnder.setBackgroundColor(Color.TRANSPARENT);
        sortHit.setTextColor(Color.parseColor("#646464"));
        sortHitUnder.setBackgroundColor(Color.TRANSPARENT);
        sortName.setTextColor(Color.parseColor("#646464"));
        sortNameUnder.setBackgroundColor(Color.TRANSPARENT);

        if(sort.equals("recently")){
            sortRecently.setTextColor(Color.parseColor("#96c81e"));
            sortRecentlyUnder.setBackgroundColor(Color.parseColor("#96c81e"));
        }else if(sort.equals("most_popular")){
            sortHit.setTextColor(Color.parseColor("#96c81e"));
            sortHitUnder.setBackgroundColor(Color.parseColor("#96c81e"));
        }else if(sort.equals("name")){
            sortName.setTextColor(Color.parseColor("#96c81e"));
            sortNameUnder.setBackgroundColor(Color.parseColor("#96c81e"));
        }
    }

    @Override
    public void onClick(View v) {
        String sort = "";
        switch (v.getId()){
            case R.id.p_op_sort_recently :
                sort = "recently";
                break;
            case R.id.p_op_sort_hit :
                sort = "most_popular";
                break;
            case R.id.p_op_sort_name :
                sort = "name";
                break;
        }

        if(currentSort.equals(sort))
            return;

        setSort(sort);
        callback.onSortAndSearch(false, -1,sort,"");
    }
}
