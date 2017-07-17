package com.knowlounge.model;

/**
 * Created by Minsu on 2016-01-14.
 */
public class NoticeData {
    private String category;            //category : HC001(가입), HC002(프로필), HC003(초대), HC004(스타), HC005(ROOM)
    private String senderThumbNail;     //senderthumbnail : 보낸사람 썸네일
    private String senderName;          //sendername : 보내사람 이름
    private String noticeTitle;         //title : 방제목
    private String dateTime;            //cdatetime : 보낸 시간
    private String historyType;         //historytype : AC001(가입), IV001(초대), IV002(참여요청), IV003(참여자제한해제), ST001(스타구매), ST002(스타선물)
    private int historyNo;              //historyno : history 조회나 삭제에 사용
    private String roomId;
    private String roomSeqNo;          //
    private String userNm;
    private String extraname;
    private int cnt;
    private int status;
    private boolean isChecked;

    public NoticeData(boolean isChecked){
        this.isChecked = isChecked;
    }

    public void setCategory (String category) {this.category = category ;}
    public void setSenderThumbNail (String thumbNail){ this.senderThumbNail = thumbNail ;}
    public void setSenderName (String senderNm){ this.senderName = senderNm ;}
    public void setNoticeTitle (String noticeTitle) { this.noticeTitle = noticeTitle ;}
    public void setDateTime ( String dateTime) { this.dateTime = dateTime ;}
    public void setHistoryType (String historyType){ this.historyType = historyType; }
    public void setHistoryNo (int historyno) {this.historyNo = historyno;}
    public void setRoomId (String roomId) {this.roomId = roomId; }

    public void setRoomSeqNo(String roomSeqNo) { this.roomSeqNo = roomSeqNo;}
    public void setUserName(String userNm) { this.userNm = userNm;}
    public void setExtraNm(String extraNm) { this.extraname = extraNm;}
    public void setCnt(int cnt) {this.cnt = cnt;}
    public void setStatus(int status) { this.status = status;}
    public void setChecked(boolean check){ this.isChecked = check; }
    public void toggleCheck(){
        if(this.isChecked)
        {
            this.isChecked = false;
        }else{
            this.isChecked = true;
        }
    }

    public String getCategory () {return this.category;}
    public String getSenderThumbNail (){return this.senderThumbNail ;}
    public String getSenderName (){return this.senderName ;}
    public String getNoticeTitle () {return this.noticeTitle ;}
    public String getDateTime ( ) {return this.dateTime  ;}
    public String getHistoryType (){return this.historyType;}
    public int getHistoryNo ( ) {return this.historyNo;}
    public String getRoomId() { return  this.roomId; }

    public String getRoomSeqNo( ) {return this.roomSeqNo;}
    public String getUserName( ) {return this.userNm;}
    public String getExtraNm( ) {return this.extraname; }
    public int getCnt( ) {return this.cnt;}
    public int getStatus( ) {return this.status ;}
    public boolean isChecked(){return  this.isChecked ;}

}
