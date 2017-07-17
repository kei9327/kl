package com.knowrecorder.phone.tab.model;

/**
 * Created by we160303 on 2016-11-30.
 */

public class VideoData {
    private int id;
    private String thumbnailUrl;    // 비디오 썸네일
    private String title;           // 비디오 제목
    private String author;          // 비디오 작성자 이름
    private String hit;             // 조회수
    private String userThumbnail;   // 사용자 썸네임 이미지
    private String playtime;        // 재생 시간
    public VideoData(){

    }
    public VideoData(int id, String thumbnailUrl, String title, String author, String hit, String userThumbnail, String playtime) {
        this.id = id;
        this.thumbnailUrl = thumbnailUrl;
        this.title = title;
        this.author = author;
        this.hit = hit;
        this.userThumbnail = userThumbnail;
        this.playtime = playtime;
    }

    public void setId(int id){this.id = id ; }
    public int getId(){ return this.id ; }

    public void setThumbnailUrl(String thumbnailUrl){ this.thumbnailUrl = thumbnailUrl ; }
    public String getThumbnailUrl(){ return this.thumbnailUrl ; }

    public void setTitle(String title){ this.title = title ; }
    public String getTitle(){ return this.title ; }

    public void setAuthor(String author){ this.author = author ; }
    public String getAuthor(){ return this.author ; }

    public void setHit(String hit){ this.hit = hit ; }
    public String getHit(){ return this.hit ; }

    public void setUserThumbnail(String userThumbnail){ this.userThumbnail = userThumbnail ; }
    public String getUserThumbnail(){ return this.userThumbnail ; }

    public void setPlaytime(String playtime){ this.playtime = playtime; }
    public String getPlaytime(){ return this.playtime ; }
}
