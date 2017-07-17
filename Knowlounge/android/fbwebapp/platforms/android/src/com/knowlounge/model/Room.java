package com.knowlounge.model;

/**
 * Created by Minsu on 2016-03-15.
 */
public class Room {
    public String seqNo;
    public String roomId;
    public String roomTitle;
    public String roomThumbnail;
    public int roomCount;
    public String creatorUserNm;
    public String creatorUserThumb;
    public String userLimitCnt;
    public int defaltNum;
    public boolean isMyroom;
    public int classType;
    public String more;
    public String passwdFlag;

    public Room(String seqNo, String roomId, String roomTitle, String roomThumbnail, int roomCount, String creatorUserNm, String creatorUserThumb, String userLimitCnt, boolean isMyroom, int classType, String more, String passwdFlag) {
        this.seqNo = seqNo;
        this.roomId = roomId;
        this.roomTitle = roomTitle;
        this.roomThumbnail = roomThumbnail;
        this.roomCount = roomCount;
        this.creatorUserNm = creatorUserNm;
        this.creatorUserThumb = creatorUserThumb;
        this.userLimitCnt = userLimitCnt;
        this.defaltNum = (int) (Math.random() * 5) + 1;
        this.isMyroom = isMyroom;
        this.classType = classType;
        this.more = more;
        this.passwdFlag = passwdFlag;
    }
    public Room(String seqNo, String roomId, String roomTitle, String roomThumbnail, int roomCount, String creatorUserNm, String creatorUserThumb, String userLimitCnt) {
        this.seqNo = seqNo;
        this.roomId = roomId;
        this.roomTitle = roomTitle;
        this.roomThumbnail = roomThumbnail;
        this.roomCount = roomCount;
        this.creatorUserNm = creatorUserNm;
        this.creatorUserThumb = creatorUserThumb;
        this.userLimitCnt = userLimitCnt;
        this.defaltNum = (int) (Math.random() * 5) + 1;
        this.isMyroom = false;
    }
}
