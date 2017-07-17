package com.knowlounge.sqllite;

/**
 * Created by we160303 on 2016-10-11.
 */

public class SearchKeyword {
    private String userId;
    private String keyword;

    public SearchKeyword(String userId, String keyword){
        this.userId = userId;
        this.keyword = keyword;
    }

    public String getUserId(){ return this.userId ;}
    public String getKeyword(){ return this.keyword ;}
}
