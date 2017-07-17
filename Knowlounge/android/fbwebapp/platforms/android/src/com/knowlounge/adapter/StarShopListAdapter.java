package com.knowlounge.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.knowlounge.R;
import com.knowlounge.model.StarShopProduct;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by Minsu on 2016-05-11.
 */
public class StarShopListAdapter extends ArrayAdapter<StarShopProduct> {

    private LayoutInflater inflater;
    private Context context;
    private ArrayList<StarShopProduct> list;
    private int selectPosition = -1;

    public StarShopListAdapter(Context context, ArrayList<StarShopProduct> list) {
        super(context, 0);
        this.context = context;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(R.layout.star_shop_list_row, parent, false);
        }

        StarShopProduct product = getItem(position);

        ((TextView)convertView.findViewById(R.id.product_name)).setText(transformPrice("name", product.getProductName()));
        ((TextView)convertView.findViewById(R.id.product_price)).setText(transformPrice("price", product.getProductPrice()));

        if(position == selectPosition){
            convertView.setBackgroundColor(Color.parseColor("#FFF5F5F5"));
            convertView.findViewById(R.id.star_shop_product).setBackgroundColor(Color.parseColor("#FFF5F5F5"));
        }else{
            convertView.setBackgroundColor(Color.parseColor("#FFFFFFFF"));
            if(position == list.size()-1)
                convertView.findViewById(R.id.star_shop_product).setBackgroundColor(Color.parseColor("#FFFFFFFF"));
            else
                convertView.findViewById(R.id.star_shop_product).setBackgroundResource(R.drawable.bg_under_gray_background_white);
        }

        return convertView;
    }

    private String transformPrice(String flag, String price){

        if(flag.equals("name")){
            DecimalFormat df = new DecimalFormat("#,###");
            return df.format(Integer.parseInt(price.substring(0,price.indexOf("Star")-1))) + " Star";
        }else if(flag.equals("price")){
            return "$ "+price;
        }
        return "";
    }
    public void setSelectPosition(int position){
        this.selectPosition = position;
        notifyDataSetChanged();
    }

    @Override
    public StarShopProduct getItem(int position) {
        return list.get(position);
    }

}
