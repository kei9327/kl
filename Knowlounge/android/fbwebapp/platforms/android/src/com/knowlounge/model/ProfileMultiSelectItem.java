package com.knowlounge.model;

/**
 * Created by we160303 on 2016-05-31.
 */
public class ProfileMultiSelectItem {
    private String itemNm;
    private String itemCode;
    private boolean ischecked;

    public ProfileMultiSelectItem(String itemNm, String itemCode, boolean ischecked){
        this.itemNm = itemNm;
        this.itemCode = itemCode;
        this.ischecked = ischecked;
    }

    public void toggleCheck(){
        if(this.ischecked)
            this.ischecked = false;
        else
            this.ischecked = true;
    }
    public void initCheck(){this.ischecked = false ; }
    public String getItemNm(){ return this.itemNm ; }
    public String getItemCode(){ return this.itemCode; }
    public boolean isChecked(){ return this.ischecked; }

}
