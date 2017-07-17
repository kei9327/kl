package com.knowlounge.customview;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnMultiChoiceClickListener;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.knowlounge.R;
import com.knowlounge.common.GlobalConst;

import java.util.List;

/**
 * Created by we160303 on 2016-05-10.
 */
public class MultiSpinner extends Spinner implements
        OnMultiChoiceClickListener, DialogInterface.OnCancelListener{

    private List<String> items;
    private boolean[] selected;
    private String defaultText;
    private int startType;
    private String type;
    private MultiSpinnerListener listener;

    public MultiSpinner(Context context) {
        super(context);
    }

    public MultiSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MultiSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onClick(DialogInterface dialog, int which, boolean isChecked) {
        if(isChecked)
            selected[which] = true;
        else
            selected[which] = false;

    }

    @Override
    public void onCancel(DialogInterface dialog) {
    }
    public void onSubmit(){
        StringBuffer spinnerBuffer = new StringBuffer();
        boolean someUnselected = false;
        for (int i = 0; i < items.size(); i++) {
            if (selected[i] == true) {
                spinnerBuffer.append(items.get(i));
                spinnerBuffer.append(",");
                someUnselected = true;
            }
        }
        String spinnerText;
        if (someUnselected) {
            spinnerText = spinnerBuffer.toString();
            if (spinnerText.length() > 1)
                spinnerText = spinnerText.substring(0, spinnerText.length() - 1);
        } else {
            spinnerText = defaultText;
        }
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item,
                new String[] { spinnerText });
        setAdapter(adapter);
        listener.onItemsSelected(selected, type);
    }
    @Override
    public boolean performClick() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.AlertDialogCustom);
        builder.setMultiChoiceItems(
                items.toArray(new CharSequence[items.size()]), selected, this);
        builder.setPositiveButton(android.R.string.ok,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onSubmit();
                    }
                });
        builder.setOnCancelListener(this);
        if(startType == GlobalConst.TYPE_STUDENT_START){
            if(type.equals(GlobalConst.CATEGORY_SUBJECT)){
                builder.setTitle(getResources().getString(R.string.profile_subject_studentguide));
            }else if(type.equals(GlobalConst.CATEGORY_LANGUAGE)){
                builder.setTitle(getResources().getString(R.string.profile_language_guide));
            }
        }else{
            if(type.equals(GlobalConst.CATEGORY_SUBJECT)){
                builder.setTitle(getResources().getString(R.string.profile_subject_teacherguide));
            }else if(type.equals(GlobalConst.CATEGORY_LANGUAGE)){
                builder.setTitle(getResources().getString(R.string.profile_language_guide));
            }
        }

        builder.show();
        return true;
    }


    public void setItems(List<String> items, String allText,
                         MultiSpinnerListener listener, String type, int startType) {
        this.items = items;
        this.defaultText = allText;
        this.listener = listener;
        this.type = type;
        this.startType = startType;

        // all selected by default
        selected = new boolean[items.size()];
        for (int i = 0; i < selected.length; i++)
            selected[i] = false;

        // all text on the spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                android.R.layout.simple_spinner_item, new String[] { allText });
        setAdapter(adapter);
    }
    public boolean[] getSelected(){
        return this.selected;
    }

    public interface MultiSpinnerListener {
        public void onItemsSelected(boolean[] selected, String type);
    }

}
