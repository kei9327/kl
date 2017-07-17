package com.knowlounge.model;

import android.widget.EditText;

import com.knowlounge.R;

import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;

public class PollCreateData {
    public static final int DEFAULT_ITEM_CNT = 4;
    public static final int MAX_ITEM_CNT = 10;

    public static final int TYPE2_BASIC_QUESTION_1 = R.string.poll_ox_ab;
    public static final int TYPE2_BASIC_QUESTION_2 = R.string.poll_ox_ox;
    public static final int TYPE2_BASIC_QUESTION_3 = R.string.poll_ox_yn;
    public static final int TYPE2_BASIC_QUESTION_4 = R.string.poll_ox_direct;
    public static final int ALTER_OPT_A_B = 21;
    public static final int ALTER_OPT_O_X = 22;
    public static final int ALTER_OPT_AGREE_DISAGREE = 23;
    public static final int ALTER_OPT_CUSTOM = 24;

    public static final int SHUTDOWN_TIME_30 = 30;
    public static final int SHUTDOWN_TIME_40 = 40;
    public static final int SHUTDOWN_TIME_50 = 50;
    public static final int SHUTDOWN_TIME_60 = 60;
    public static final int SHUTDOWN_TIME_UNLIMIT = 0;

    public static final int POLL_TYPE_MULTIPLE_CHOICE = 0;
    public static final int POLL_TYPE_ALTER_CHOICE = 1;
    public static final int POLL_TYPE_SHORT_ANSWER = 2;
    public static final int POLL_TYPE_DRAWING = 3;

    public static final int FULL_SCREEN_CAPTURE = 31;
    public static final int SELECTION_CAPTURE = 32;
    public static final int DIRECT_UPLOAD = 33;

    public static final String TARGET_USER_ALL = "all";
    public static final String TARGET_USER_TEACHER = "teacher";

    private static int checkedType;
    private static int pollcnt;
    private static boolean dupcheck;
    private static boolean inputQuestionCheck;
    private static boolean isChange;
    private static int isduple;
    private static ArrayList<EditText> poll_create_type1_edit_Arr;
    private static String poll_tempno;



    private static String pollTitle;
    private static ArrayList<EditText> question_arr;
    private static int sendtype;
    private static boolean shutdown_check;
    private static int shutdown_checked;
    private static int shutdown_time;
    private static ArrayList<String> throughHandle_Question_arr;
    private static JSONArray type1_arr;
    private static int type2_checked;
    private static String type2_question1;
    private static String type2_question2;
    private static int courceType;
    private static String polltempno;

    private static int drawingMethod = -1;   // 판서폴 등록 방식
    private static String capturedImgBinary = "";

    private static String targetUser = "";



    private static String pollDataTmp = "";

    public PollCreateData() {
        sendtype = -1;
        type2_question2 = "";
        type2_question1 = "";
        question_arr = new ArrayList();
        throughHandle_Question_arr = new ArrayList();
        dupcheck = false;
        inputQuestionCheck = false;
        shutdown_check = false;
        checkedType = -1;
        poll_create_type1_edit_Arr = new ArrayList();
        pollTitle = "";
        isduple = 0;
        type2_checked = -1;
        shutdown_time = 30;
        shutdown_checked = 1;
        isChange = false;
        polltempno = "";
        type1_arr = null;
        drawingMethod = -1;
        capturedImgBinary = "";
        targetUser = "";
    }

    public void clear() {
        sendtype = -1;
        type2_question2 = "";
        type2_question1 = "";
        question_arr.clear();
        throughHandle_Question_arr.clear();
        dupcheck = false;
        inputQuestionCheck = false;
        shutdown_check = false;
        checkedType = -1;
        poll_create_type1_edit_Arr.clear();
        pollTitle = "";
        isduple = 0;
        type2_checked = -1;
        shutdown_time = 30;
        shutdown_checked = 1;
        isChange = false;
        polltempno ="";
        type1_arr = null;
        drawingMethod = -1;
        capturedImgBinary = "";
        targetUser = "";
        pollDataTmp = "";
    }

    public void clear_type1(){
        type1_arr = null;
        poll_create_type1_edit_Arr.clear();
        dupcheck = false;
        checkedType = -1;
    }

    public void clear_type2(){
        type2_question2 = "";
        type2_question1 = "";
        type2_checked = -1;
        checkedType = -1;
    }

    public void removeDrawingPollInfo() {
        drawingMethod = -1;
        capturedImgBinary = "";
    }

    public void clear_shutdown(){
        shutdown_check = false;
        shutdown_checked = -1;
        shutdown_time = 30;
    }

    public void setPolltempno(String polltempno){this.polltempno = polltempno;}
    public String getPolltempno(){return this.polltempno;}

    // poll_Create 타입
    public void setCheckedType(int paramInt)
    {
        this.checkedType = paramInt;
    }
    public int getCheckedType(){return this.checkedType;}
    public void setIsChange(boolean paramBoolean)
    {
        this.isChange = paramBoolean;
    }
    public boolean getIsChange(){ return isChange; }

    //선다형 관련
    public void setPoll_count(int count){this.pollcnt = count;}
    public int getPoll_count(){return this.pollcnt;}
    public int getPollCreateType1ArrSize(){return poll_create_type1_edit_Arr.size();}
    public EditText getPollCreateType1Arr_Index(int index){return (EditText)poll_create_type1_edit_Arr.get(index);}
    public JSONArray getType1JSONArray() { return type1_arr;  }
    public void setType1JSONArray(JSONArray paramJSONArray)
    {
        type1_arr = paramJSONArray;
    }
    public void setPollCreateType1EditArr(ArrayList<EditText> paramArrayList) {
        int i = 0;
        while (i < paramArrayList.size())
        {
            poll_create_type1_edit_Arr.add(paramArrayList.get(i));
            i += 1;
        }
        pollcnt = poll_create_type1_edit_Arr.size();
    }
    public void setPollCreateType1EditArrClear()
    {
        poll_create_type1_edit_Arr.clear();
    }
    public void setDupcheck(boolean dupcheck){this.dupcheck = dupcheck;}
    public boolean getDupchecked(){return this.dupcheck;}
    public int getDupcheck(){
        if(dupcheck)
            return getPollCreateType1ArrSize();
        else
            return 1;
    }
    public String getQuestion(){
        String result="";
        int i;
        for(i=0; i<this.getPollCreateType1ArrSize()-1;i++)
        {
            result += poll_create_type1_edit_Arr.get(i).getText().toString()+"|";
        }
        result += poll_create_type1_edit_Arr.get(i).getText().toString();
        return result;
    }
    public String getQuestion(JSONArray arr){
        try {
            String result = "";
            int i;
            for (i = 0; i < arr.length()-1; i++) {
                result += arr.getJSONObject(i).getString("itemnm")+"|";
            }
            result += arr.getJSONObject(i).getString("itemnm");
            return  result;
        }catch(JSONException j){
            return "";
        }
    }


    //양자택일 관련
    public int getType2Checked()
    {
        return type2_checked;
    }
    public int getType2Result(int paramInt)
    {
        switch (paramInt)
        {
            case ALTER_OPT_A_B:
                return TYPE2_BASIC_QUESTION_1;
            case ALTER_OPT_O_X:
                return TYPE2_BASIC_QUESTION_2;
            case ALTER_OPT_AGREE_DISAGREE:
                return TYPE2_BASIC_QUESTION_3;
            default:
                return TYPE2_BASIC_QUESTION_4;
        }

    }
    public String getType2InputQuestion()
    {
        return type2_question1 + "|" + type2_question2;
    }

    public void setType2Checked(int paramInt)
    {
        type2_checked = paramInt;
    }

    public void setType2InputQuestion(String paramString1, String paramString2)
    {
        type2_question1 = paramString1;
        type2_question2 = paramString2;
    }


    //종료시간 관련
    public int getShutdownChecked(){return shutdown_checked;}
    public int getShutdowntime(){
        switch(this.shutdown_checked)
        {
            case 1 : return SHUTDOWN_TIME_30;
            case 2 : return SHUTDOWN_TIME_40;
            case 3 : return SHUTDOWN_TIME_50;
            case 4 : return SHUTDOWN_TIME_60;
            case 0 : return SHUTDOWN_TIME_UNLIMIT;
            default: return shutdown_time;
        }
    }
    public void setShutdownChecked(int paramInt) { shutdown_checked = paramInt; }
    public void setShutdownTime(int paramInt) {shutdown_time = paramInt;}



    public int getDrawingMethod() {
        // 판서 폴의 등록 방법 정보..
        return drawingMethod;
    }

    public void setDrawingMethod(int drawingMethod) {
        // 판서 폴의 등록 방법 정보..
        this.drawingMethod = drawingMethod;
    }

    public String getCapturedImgBinary() {
        return this.capturedImgBinary;
    }

    public void setCapturedImgBinary(String binary) {
        this.capturedImgBinary = binary;
    }


    public String getTargetUser() {
        return this.targetUser;
    }

    public void setTargetUser(String target) {
        this.targetUser = target;
    }

    public static String getPollDataTmp() {
        return pollDataTmp;
    }

    public static void setPollDataTmp(String pollDataTmp) {
        PollCreateData.pollDataTmp = pollDataTmp;
    }


    public static String getPollTitle() {
        return pollTitle;
    }

    public static void setPollTitle(String pollTitle) {
        PollCreateData.pollTitle = pollTitle;
    }
}