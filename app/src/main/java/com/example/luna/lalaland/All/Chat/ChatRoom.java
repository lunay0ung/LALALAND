package com.example.luna.lalaland.All.Chat;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by LUNA on 2018-10-10.
 * 라이브 스트리밍 중 이루어지는 채팅방을 관리하기 위한 클래스
 *
 * 스트리머들이 라이브 스트리밍을 시작할 때마다 임의의 숫자로 룸아이디가 생성됨.
 * 이 룸아이디를 미디어 서버로 보내서 각 라이브 스트리밍 방송이 구별될 수 있는데,
 * 이 룸아이디를 채팅서버로도 보내어 채팅방을 나눌 때도 쓴다.
 *
 * 채팅 또한 이 룸아이디를 가진 방송에서만 이루어지도록 한다.
 *
 * 단, 편리한 관리를 위해 룸아이디를 String값으로 바꾸어 사용한다.
 */

public class ChatRoom {

    private String roomId; //
    private String message;
    private boolean createRoom;


    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isCreateRoom() {
        return createRoom;
    }

    public void setCreateRoom(boolean createRoom) {
        this.createRoom = createRoom;
    }
}
