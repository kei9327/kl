package com.knowlounge.model;

/**
 * Created by Minsu on 2016-01-21.
 */
public class AnswerQuestionAndAnswerResultData {

    private String pollitemno;
    private String itemnm;
    private int answercount;

    public AnswerQuestionAndAnswerResultData(String pollitemno, String itemnm) {
        this.pollitemno = pollitemno;
        this.itemnm = itemnm;
    }

    public AnswerQuestionAndAnswerResultData(String pollitemno, int answercount) {
        this.pollitemno = pollitemno;
        this.answercount = answercount;
    }

    public String getPollitemno() {
        return this.pollitemno;
    }

    public String getItemNm() {
        return this.itemnm;
    }

    public int getAnswercnt() {
        return this.answercount;
    }
}