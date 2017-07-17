package com.knowlounge.model;

/**
 * Created by Minsu on 2016-01-21.
 */
public class AnswerQuestion_Data {

    private String pollitemno;
    private String itemnm;

    public AnswerQuestion_Data(String pollitemno, String itemnm) {
        this.pollitemno = pollitemno;
        this.itemnm = itemnm;
    }
    public String getPollitemno(){return this.pollitemno;}
    public String getItemNm(){return this.itemnm;}
}
