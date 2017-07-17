package com.knowlounge.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.model.AnswerQuestionAndAnswerResultData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by Minsu on 2016-01-12.
 */
public class PollAnswerAdapter extends ArrayAdapter<AnswerQuestionAndAnswerResultData> {
    private final String TAG = "PollAnswerAdapter";

    private HashMap<Integer, Boolean> mCheckedMap = new HashMap<>();

    private LayoutInflater inflater;

    public PollAnswerAdapter(Context context, ArrayList<AnswerQuestionAndAnswerResultData> list) {
        super(context, R.layout.poll_answer_multiple_row, list);
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for (int i=0; i<list.size();i++){
            mCheckedMap.put(i, false);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view = convertView;
        if (view == null ) {
            view = inflater.inflate(R.layout.poll_answer_multiple_row, parent, false);
        }

        TextView answerName = (TextView) view.findViewById(R.id.poll_answer_type12_item);
        answerName.setText(getItem(position).getItemNm());

        ImageView checkImage = (ImageView) view.findViewById(R.id.poll_answer_type12_check);
        Boolean checked = mCheckedMap.get(position);
        if (checked != null) {
            if (checked) {
                checkImage.setImageResource(R.drawable.btn_poll_checkbox_on);
                answerName.setTextColor(Color.parseColor("#5a5a5a"));
                answerName.setTypeface(null, Typeface.BOLD);
            } else {
                checkImage.setImageResource(R.drawable.btn_poll_checkbox);
                answerName.setTextColor(Color.parseColor("#969696"));
                answerName.setTypeface(null, Typeface.NORMAL);
            }
        }
        return view;
    }

    public void toggleChecked(int position) {
        if (mCheckedMap.get(position)) {
            mCheckedMap.put(position, false);
        } else {
            mCheckedMap.put(position, true);
        }
        notifyDataSetChanged();
    }

    public boolean isSelected() {
        return mCheckedMap.isEmpty();
    }

    public boolean isChoiceAnswerMultiple() {
        boolean result = false;
        Iterator<Integer> it = mCheckedMap.keySet().iterator();
        while (it.hasNext()) {
            int index = it.next();
            if (mCheckedMap.get(index)) {
                result = true;
                break;
            }
        }
        return result;
    }

    public String getResult(){
        String result="";

        Iterator<Integer> it = mCheckedMap.keySet().iterator();

        while (it.hasNext()){
            int index = it.next();
            if(mCheckedMap.get(index))
                result += getItem(index).getPollitemno() +  "|";
        }
        result = result.substring(0, result.length() - 1);


        return result;
    }
}

