package com.knowlounge.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.knowlounge.MainActivity;
import com.knowlounge.R;
import com.knowlounge.adapter.StarShopListAdapter;
import com.knowlounge.manager.WenotePreferenceManager;
import com.knowlounge.model.StarShopProduct;

import java.util.ArrayList;


public class StarShopFragment extends Fragment implements View.OnClickListener, WenotePreferenceManager.OnPreferenceChangeListener {
    private final String TAG = "StarShopFragment";
    private View rootView;
    private ImageView btnMainleftStarshopBack;

    private ListView starShopListView;
    private StarShopListAdapter starShopListAdapter;
    private ArrayList<StarShopProduct> starShopList = new ArrayList<StarShopProduct>();
    private StarShopProduct selectStarProduct;


    private LinearLayout mainleft_starshop_mystar;
    private TextView btnBuyStar, ownedStarTextView;

    private WenotePreferenceManager prefManager;

    @Override
    public void onPreferenceChanged(SharedPreferences preferences, String key) {
        if (key.equals("star_balance")) {
            updateStar();
        }
    }

    public interface InAppBillingListener {
        public void onStarShopItemClicked(String productId);
    }

    private InAppBillingListener mCallback;

    @Override
    public void onAttach(Context context) {
        Log.d(TAG, "onAttach fired..");
        super.onAttach(context);
        try {
            mCallback = (InAppBillingListener) context;
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, ViewGroup container, Bundle paramBundle) {
        Log.d(TAG, "onCreateView");

        prefManager = new WenotePreferenceManager().getInstance(getActivity());

        if (rootView == null)
            rootView = inflater.inflate(R.layout.fragment_starshop, container, false);

        starShopListView = (ListView) rootView.findViewById(R.id.star_shop_list);
        starShopListAdapter = new StarShopListAdapter(getActivity(), starShopList);
        starShopListView.setAdapter(starShopListAdapter);

        // 스타샵 항목 클릭 이벤트..
        starShopListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectStarProduct = starShopList.get(position);
                starShopListAdapter.setSelectPosition(position);

                Log.d(TAG, "Select Product.. - " + selectStarProduct.getProductId());
            }
        });

        // 스타샵 상품데이터 초기화..
        String[] productIdArr = getResources().getStringArray(R.array.star_shop_product_id);
        String[] productNameArr = getResources().getStringArray(R.array.star_shop_product_name);
        String[] productPriceArr = getResources().getStringArray(R.array.star_shop_product_price);

        for (int i = 0; i < productIdArr.length; i++) {
            StarShopProduct product = new StarShopProduct(productIdArr[i], productNameArr[i], productPriceArr[i]);
            starShopList.add(product);
        }
        starShopListAdapter.addAll(starShopList);
        starShopListAdapter.notifyDataSetChanged();

        setFindViewById();

        ownedStarTextView.setText(prefManager.getUserStarBalance() + ""); // 내 보유 Star 데이터 업데이트

        return rootView;
    }


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        btnMainleftStarshopBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (getArguments() == null) {
                    getFragmentManager().popBackStack();
                } else {
                    String parentActivity = getArguments().getString("parent_activity");
                    if (TextUtils.equals(parentActivity, "MainActivity")) {
                        MainActivity._instance.closeLeftNav();
                    } else {
                        getActivity().finish();
                    }
                }
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        prefManager.registerOnPreferenceChangeListener(this);
        updateStar();
    }

    @Override
    public void onPause() {
        super.onPause();
        prefManager.unregisterOnPreferenceChangeListener(this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void setFindViewById() {
        btnMainleftStarshopBack = (ImageView) rootView.findViewById(R.id.btn_mainleft_starshop_back);
        mainleft_starshop_mystar = (LinearLayout) rootView.findViewById(R.id.mainleft_starshop_mystar);
        btnBuyStar = (TextView) rootView.findViewById(R.id.btn_buy_star);
        ownedStarTextView = (TextView) rootView.findViewById(R.id.owned_star_text);

        mainleft_starshop_mystar.setOnClickListener(this);
        btnBuyStar.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.mainleft_starshop_mystar:
                break;

//            case R.id.mainleft_starshop_buy6 :
//                break;
            case R.id.btn_buy_star:
                if (selectStarProduct != null) {
                    mCallback.onStarShopItemClicked(selectStarProduct.getProductId());
                } else {

                }
                break;
        }
    }

    private void updateStar(){
        int starCount = prefManager.getUserStarBalance();
        ownedStarTextView.setText(starCount + ""); // 내 보유 Star 데이터 업데이트
    }

}