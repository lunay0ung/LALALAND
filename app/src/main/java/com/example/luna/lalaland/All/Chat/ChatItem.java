package com.example.luna.lalaland.All.Chat;

/**
 * Created by LUNA on 2018-10-03.
 * 동영상 스트리밍 시 유저들의 채팅 내용을 보여줄 때 사용하는 리사이클러뷰를 위한 클래스
 * 유저 프로필사진, 유저네임, 메시지를 보여주는 것이 목표.
 * 10/3일 현재 일단 유저네임과 메시지만 구현할 예정
 */

public class ChatItem {

    private String username;
    private String message;

    public ChatItem(String username, String message){
        this.username = username;
        this.message = message;
    }//ChatItem

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}//ChatItem
