package com.knowrecorder.develop.fragment.Dialog;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.widget.TextViewCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.knowrecorder.Constants.ServerInfo;
import com.knowrecorder.OpenCourse.API.Models.Category;
import com.knowrecorder.OpenCourse.ApiEndPointInterface;
import com.knowrecorder.R;
import com.knowrecorder.develop.event.EventOpenCourseExport;
import com.knowrecorder.rxjava.RxEventFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by we160303 on 2017-03-20.
 */

public class OpenCourseDialog extends DialogFragment implements View.OnClickListener {
    private final String TAG = "OpenCourseDialog";

    private View rootView;

    private ImageView btnClose;
    private EditText title;
    private Spinner category;
    private TextView btnExport, btnCancel;

    private Retrofit retrofit;
    private ApiEndPointInterface apiService;

    private ArrayAdapter<String> categoryList;
    private HashMap<String, Integer> opencourseCategoryMap = new HashMap<>();

    private int currentCategoryId;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        rootView = inflater.inflate(R.layout.rb_dialog_opencourse, container, false);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        retrofit = new Retrofit.Builder()
                .baseUrl(ServerInfo.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(ApiEndPointInterface.class);

        getCategory();
        setBindView();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    private void setBindView(){
        btnClose = (ImageView) rootView.findViewById(R.id.btn_close);
        title = (EditText) rootView.findViewById(R.id.opencourse_title);
        category = (Spinner) rootView.findViewById(R.id.categories);
        btnExport = (TextView) rootView.findViewById(R.id.btn_export);
        btnCancel = (TextView) rootView.findViewById(R.id.btn_cancel);

        btnClose.setOnClickListener(this);
        btnCancel.setOnClickListener(this);
        btnExport.setOnClickListener(this);

        categoryList= new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_dropdown_item, android.R.id.text1);
        category.setAdapter(categoryList);
        category.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int categoryId = opencourseCategoryMap.get(categoryList.getItem(position));

                Log.d(TAG, "This categoryId is "+ categoryId);
                Log.d(TAG, "This category is "+ categoryList.getItem(position));

                currentCategoryId = categoryId;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                int categoryId = opencourseCategoryMap.get(categoryList.getItem(0));

                Log.d(TAG, "This categoryId is "+ categoryId);
                Log.d(TAG, "This category is "+ categoryList.getItem(0));

                currentCategoryId = categoryId;
            }
        });
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()){
            case R.id.btn_close :
                break;

            case R.id.btn_cancel :
                break;

            case R.id.btn_export :
                String title = this.title.getText().toString();
                if(title.length() == 0)
                    title = getActivity().getResources().getString(R.string.export_default_text);

                RxEventFactory.get().post(new EventOpenCourseExport(title, currentCategoryId));
                break;
        }
        dismiss();

    }

    public void getCategory() {
        Call<List<Category>> getCategories = apiService.getCategories(ServerInfo.API_KEY); // 카테고리 목록을 가져오는  API
        getCategories.enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(Call<List<Category>> call, Response<List<Category>> response) {
                List<Category> categories = response.body();

                for (int i = 0; i < categories.size(); i++) {
                    Category category = categories.get(i);
                    opencourseCategoryMap.put(category.getCategory(), category.getId());
                    categoryList.add(category.getCategory());
                }
            }

            @Override
            public void onFailure(Call<List<Category>> call, Throwable t) {

            }
        });
    }
}
