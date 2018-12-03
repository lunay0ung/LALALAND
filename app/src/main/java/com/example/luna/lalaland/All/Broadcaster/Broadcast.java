package com.example.luna.lalaland.All.Broadcaster;

import com.google.gson.annotations.SerializedName;

/**
 * Created by LUNA on 2018-09-27.
 * 홈스크린 화면의 방송목록 클래스
 * 당분간은 썸네일과 방송 제목만 저장할 예정
 *
 * TODO
 * 모바일 유튜브 앱을 참고하여 UI 구성하기--스트리머 프로필, 스트리머 닉네임, 조회수, 방송 시간 추가
 * **방송중이면 방송 중이라는 표시와 시청자 수 표기 필요
 * 혹은 너무 완벽을 기하지 말고 그냥 이런 느낌이다, 라는 정도로만 가는 것도 괜찮을 듯
 */

public class Broadcast {

    @SerializedName("thumbnail")
    private String thumbnail;
    @SerializedName("title")
    private String title;
    @SerializedName("streamer")
    private String streamer;
    @SerializedName(("roomId"))
    private int roomId;
    @SerializedName("isLive")
    private Boolean isLive;
    @SerializedName("index") //방송 리스트를 최신순으로 정렬하기 위해 데이터베이스에서 인덱스를 가져옴
    private int index;
    @SerializedName("viewers") //현재 라이브 영상을 보고 있는 사람이 몇 명인가
    private int viewers;
    @SerializedName("views") //저장된 동영상을 몇 명이나 보았는가
    private int views;
    @SerializedName("genre")
    private String genre; //장르
    @SerializedName("started_at")
    private String started_at; //방송 시작 시간

    public Broadcast() {

    }


    public Broadcast(String thumbnail, String streamer, String title, String genre, String started_at) { //썸네일, 스트리머의 닉네임, 제목으로 방송이 생성됨
        this.thumbnail = thumbnail;
        this.streamer = streamer;
        this.title = title;
        this.genre = genre;
        this.started_at = started_at;
    }

//    public Broadcast(String streamer, String title, int roomId) { //10월 10일 현재 쓰고 있지 않음. 한 달이 지난 후에도 쓰고 있지 않다면 삭제
//        this.streamer = streamer;
//        this.title = title;
//        this.roomId = roomId;
//    }


    public String getStarted_at() {
        return started_at;
    }

    public void setStarted_at(String started_at) {
        this.started_at = started_at;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public Boolean getLive() {
        return isLive;
    }

    public void setLive(Boolean live) {
        isLive = live;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        this.thumbnail = thumbnail;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getViewers() {
        return viewers;
    }

    public void setViewers(int viewers) {
        this.viewers = viewers;
    }

    public int getViews() {
        return views;
    }

    public void setViews(int views) {
        this.views = views;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }


    public String getStreamer() {
        return streamer;
    }

    public void setStreamer(String streamer) {
        this.streamer = streamer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Boolean getIsLive() {
        return isLive;
    }

    public void setIsLive(Boolean isLive) {
        this.isLive = isLive;
    }
}//BroadcastList
