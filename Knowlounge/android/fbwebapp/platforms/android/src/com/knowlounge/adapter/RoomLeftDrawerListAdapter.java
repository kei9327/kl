package com.knowlounge.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.view.room.RoomActivity;
import com.knowlounge.model.ExpandableMenu;
import com.knowlounge.model.ExpandableMenuItemList;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by changha on 2016-04-04.
 */
public class RoomLeftDrawerListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private ArrayList<ExpandableMenu> group_list;
    private HashMap<ExpandableMenu, ArrayList<ExpandableMenuItemList>> group_list_item;
    private ArrayList<TextView> group_list_title_arr;
    private boolean isAlreadyInserted = false;
    private static final int NUM_OF_ROWS = 4;
    private int count = 0;

    private int index = -1;


    public RoomLeftDrawerListAdapter(Context context, ArrayList<ExpandableMenu> list,
                                HashMap<ExpandableMenu, ArrayList<ExpandableMenuItemList>> group_list_item ){
        this.mContext = context;
        this.group_list = list;
        this.group_list_item = group_list_item;
        this.group_list_title_arr = new ArrayList<TextView>();
    }


    public void setIndex(int index) {
        this.index = index;
        this.notifyDataSetChanged();
    }


    @Override
    public int getGroupCount() {
        return this.group_list.size();
    }


    @Override
    public Object getGroup(int groupPosition) {
        return this.group_list.get(groupPosition);
    }


    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }


    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        String headerTitle = group_list.get(groupPosition).getGroup_title();
        int resource = group_list.get(groupPosition).getGroup_resource();
        String itemTag = group_list.get(groupPosition).getMenuTag();
        boolean hasSubList = group_list.get(groupPosition).isHasSubList();

        if(convertView == null){
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.rows_room_left_nav, null);
        }
        final LinearLayout groupItemBg = (LinearLayout) convertView.findViewById(R.id.main_group_item_layout);
        final TextView textListItem = (TextView) convertView.findViewById(R.id.main_group_text);
        final ImageView imgListItem = (ImageView) convertView.findViewById(R.id.main_group_item_img);
        final ImageView btnSubList = (ImageView) convertView.findViewById(R.id.sub_list_open_btn);

        if(hasSubList)
            btnSubList.setVisibility(View.VISIBLE);
        else
            btnSubList.setVisibility(View.GONE);

        if(index == groupPosition) {
            if(hasSubList) {
                textListItem.setTextColor(mContext.getResources().getColor(R.color.app_base_color));
                imgListItem.setImageResource(mContext.getResources().getIdentifier(itemTag + "_on", "drawable", mContext.getPackageName()));
                btnSubList.setImageResource(R.drawable.btn_leftmenu_fold_on);
                groupItemBg.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
            }
        } else {
            textListItem.setTextColor(Color.parseColor("#505050"));
            imgListItem.setImageResource(mContext.getResources().getIdentifier(itemTag, "drawable", mContext.getPackageName()));
            btnSubList.setImageResource(R.drawable.btn_leftmenu_fold);
            groupItemBg.setBackgroundResource(R.drawable.bg_under_line_group);
        }

        textListItem.setText(headerTitle);
        //mainGroupItemImg.setImageResource(resource);

        if (!isAlreadyInserted) {
            boolean isExists = false;
            for (TextView textView : group_list_title_arr) {
                if (textView.getText().equals(headerTitle)) {
                    isExists = true;
                    break;
                }
            }

            if (!isExists) {
                group_list_title_arr.add(textListItem);
            }
        }

        if (++count > NUM_OF_ROWS)
            isAlreadyInserted = true;

        if(groupPosition == group_list.size()-1){
            convertView.setBackgroundResource(R.drawable.bg_under_line_group);
        }else{
            convertView.setBackgroundColor(Color.parseColor("#ffffff"));
        }

        return convertView;
    }


    @Override
    public boolean hasStableIds() {
        return false;
    }


    @Override
    public int getChildrenCount(int groupPosition) {
        return this.group_list_item.get(this.group_list.get(groupPosition)).size();
    }


    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return this.group_list_item.get(this.group_list.get(groupPosition)).get(childPosition);
    }


    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }


    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        final String childText = group_list_item.get(group_list.get(groupPosition)).get(childPosition).getGroup_item_title();
        if (convertView == null) {
            LayoutInflater infalInflater = (LayoutInflater) this.mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = infalInflater.inflate(R.layout.room_activity_left_nav_drawer_main_list_group_item, null);
        }

        // 북마크용 스위치 UI 설정
        SwitchCompat bookmarkSwitch = (SwitchCompat) convertView.findViewById(R.id.switch_bookmark);
        if(groupPosition == 3 && childPosition == 0) {
            bookmarkSwitch.setVisibility(View.VISIBLE);
            bookmarkSwitch.setChecked(RoomActivity.activity.getIsBookmark());
            if(RoomActivity.activity.getGuestFlag())
                bookmarkSwitch.setClickable(false);
            bookmarkSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    RoomActivity.activity.onBookmark(!isChecked);
                }
            });
        } else {
            bookmarkSwitch.setVisibility(View.GONE);
        }

        TextView childItemTxt = (TextView) convertView.findViewById(R.id.main_group_item_text);
        childItemTxt.setText(childText);
        return convertView;
    }


    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }
}
