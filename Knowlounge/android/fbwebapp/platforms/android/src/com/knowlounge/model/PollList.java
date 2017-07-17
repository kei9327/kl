package com.knowlounge.model;

/**
 * Created by Minsu on 2016-01-14.
 */
public class PollList {


    private String polltempno;  //폴 넘버
    private String title;  //폴 타이틀
    private static int totalnumber;
    private boolean checked;


    public PollList(int totalnumber, String polltempno, String title) {
        this.totalnumber = totalnumber;
        this.polltempno = polltempno;
        this.title = title;
        this.checked = false;
    }



    public void setPolltempno(String polltempno) { this.polltempno = polltempno;  }
    public void setTitle(String title) { this.title = title; }
    public void setTotalnumber(int totalnumber){ this.totalnumber = totalnumber; }
    public String getPolltempno() { return polltempno; }
    public String getTitle() {
        return title;
    }
    public int getTotalcount(){return this.totalnumber;}

    public void setChecked(boolean check){this.checked = check;}
    public boolean getChecked(){return checked;}

    public void toggleChecked(){
        if(checked)
            checked = false;
        else
            checked = true;
    }
}
