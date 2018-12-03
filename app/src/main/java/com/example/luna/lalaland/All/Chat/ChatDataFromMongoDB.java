package com.example.luna.lalaland.All.Chat;

/**
 * Created by LUNA on 2018-10-19.
 */

/*
* 라이브 스트리밍 시 주고받은 메시지는 몽고비디의 message컬렉션에 저장된다.
* 저장된 라이브 방송을 재생할 때 VodActivity에서는 해당 방송의 roomId를 이용해
* 해당 roomId를 가진 컬렉션을 JsonObject를 가진 JsonArray형식으로 가져오게 되는데,
* 그때 Json형식으로 담긴 데이터들을 deserialize하기 위해 만든 클래스
* */

public class ChatDataFromMongoDB {

    private String sender;
    private String content;
    private int contentPosition;

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getContentPosition() {
        return contentPosition;
    }

    public void setContentPosition(int contentPosition) {
        this.contentPosition = contentPosition;
    }
}//ChatDataFromMongoDB
