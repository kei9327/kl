package com.knowlounge.adapter;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.knowlounge.CircleTransform;
import com.knowlounge.R;
import com.knowlounge.model.FriendUser;
import com.knowlounge.common.GlobalConst;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by we160303 on 2016-04-27.
 */
public class SelectFriendRecyclerAdapter extends RecyclerView.Adapter<SelectFriendRecyclerAdapter.ViewHolder> {

    private ArrayList<FriendUser> list;
    private Context mContext;

    private static userRemove mUserRemove = null;

    public interface userRemove{
        public void onUserRemove(int position);
    }
    public static void setOnUserRemove(userRemove listener){ mUserRemove = listener; }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // ViewHolder
        public ImageView selectUserthumb;
        public TextView selectUsernm;
        public ViewHolder(View view){
            super(view);
            selectUsernm = (TextView) view.findViewById(R.id.select_usernm);
            selectUserthumb = (ImageView) view.findViewById(R.id.select_userthumb);
        }
    }

    public SelectFriendRecyclerAdapter(Context context, ArrayList<FriendUser> list) {
        if (list == null) {
            throw new IllegalArgumentException(
                    "modelData must not be null");
        }
        this.mContext = context;
        this.list = list;
    }

    @Override
    public SelectFriendRecyclerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.row_select_user_chips, viewGroup, false);
        ViewHolder vh = new ViewHolder(view);
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        String userNm = list.get(position).getUserNm();
        String userThumb = list.get(position).getUserThumbnail();

        Uri thumbnailUrl = Uri.parse(userThumb);
        Picasso.with(mContext).load(thumbnailUrl).transform(new CircleTransform(GlobalConst.TYPE_CIRCLE_THUMB)).into(holder.selectUserthumb);
        holder.selectUsernm.setText(userNm);

        holder.selectUsernm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //todo 선택 됬을경우 false로 만들고 view 제거
                mUserRemove.onUserRemove(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public String getItemUserNm(int position){return list.get(position).getUserNm();}
    public String getItemUserId(int position){return list.get(position).getId();}
    public String getSelectFriendList(){
        String result = "";
        for(int i=0; i<list.size();i++){
            result += list.get(i).getId() + ",";
        }
        return result.length() != 0 ? result.substring( 0 , result.length()-1 ) : "";
    }

}