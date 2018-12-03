package com.example.luna.lalaland.All.Rtc_Peer.kurento.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

/**
 * Created by nhancao on 6/19/17.
 */

public class CandidateModel implements Serializable {
    @SerializedName("sdpMid")
    private String sdpMid;
    @SerializedName("sdpMLineIndex")
    private int sdpMLineIndex;
    @SerializedName("candidate")
    private String sdp;
    private String makeRoom;

    public CandidateModel(String sdpMid, int sdpMLineIndex, String sdp) {
        this.sdpMid = sdpMid;
        this.sdpMLineIndex = sdpMLineIndex;
        this.sdp = sdp;
    }

    //10/8 추가 --새로운 라이브 방송을 시작할 때 서버에 보내는 메시지 추가
    public CandidateModel(String sdpMid, int sdpMLineIndex, String sdp, String makeRoom) {
        this.sdpMid = sdpMid;
        this.sdpMLineIndex = sdpMLineIndex;
        this.sdp = sdp;
        this.makeRoom = makeRoom;
    }


    public String getSdpMid() {
        return sdpMid;
    }

    public int getSdpMLineIndex() {
        return sdpMLineIndex;
    }

    public String getSdp() {
        return sdp;
    }

    public String getMakeRoom() { //추가 10/8
        return makeRoom;
    }

    public void setMakeRoom(String makeRoom) {  //추가 10/8
        this.makeRoom = makeRoom;
    }

    @Override
    public String toString() {
        return new Gson().toJson(this);
    }
}
