package com.knowlounge.youtube.model;

/**
 * Created by we160303 on 2016-09-28.
 */
public class VideoModel {
    private String seqNo;
    private String title;
    private String userNo;
    private String typeFlag;
    private String mediaKey;
    private String thumbNail;
    private String cDateTime;
    private String updateTime;

    public void setSeqNo(String seqNo){ this.seqNo = seqNo ; }
    public void setUserNo(String userNo){ this.userNo= userNo ; }
    public void setTypeFlag(String typeFlag){ this.typeFlag = typeFlag ; }
    public void setMediaKey(String mediaKey){ this.mediaKey = mediaKey ; }
    public void setThumbNail(String thumbNail){ this.thumbNail = thumbNail ; }
    public void setcDateTime(String cDateTime){ this.cDateTime = cDateTime ; }
    public void setUpdateTime(String updateTime){ this.updateTime = updateTime ; }
    public void setTitle(String title){ this.title = title ; }

    public String getSeqNo(){ return this.seqNo ; }
    public String getUserNo(){ return this.userNo ; }
    public String getTypeFlag(){ return this.typeFlag ; }
    public String getMediaKey(){ return this.mediaKey ; }
    public String getThumbNail(){ return this.thumbNail ; }
    public String getcDateTime(){ return this.cDateTime ; }
    public String getUpdateTime(){ return this.updateTime ; }
    public String getTitle(){ return this.title;}
}
