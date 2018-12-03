package com.example.luna.lalaland.All.Rtc_Peer.kurento;

import android.util.Log;

import com.nhancv.webrtcpeer.rtc_comm.ws.BaseSocketCallback;
import com.nhancv.webrtcpeer.rtc_comm.ws.SocketService;
import com.nhancv.webrtcpeer.rtc_peer.RTCClient;

import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.IceCandidate;
import org.webrtc.SessionDescription;

/**
 * 방송 송출자와 시청자가 쿠렌토 미디어 서버와 통신하는 클래스
 * 방송 송출자-시청자 간 SDP Offer, SDP Answer을 주고 받을 수 있다
 */



public class KurentoPresenterRTCClient implements RTCClient {
    private static final String TAG = KurentoPresenterRTCClient.class.getSimpleName();
    int roomId; //생성된 각 방송의 아이디

    private SocketService socketService;

    public KurentoPresenterRTCClient(SocketService socketService) {
        this.socketService = socketService;
    }

    public void connectToRoom(String host, BaseSocketCallback socketCallback) {
        socketService.connect(host, socketCallback);
    }

    @Override
    public void sendOfferSdp(SessionDescription sdp) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", "presenter");
            obj.put("sdpOffer", sdp.description);
            obj.put("roomId", roomId);

            socketService.sendMessage(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    //방송 시작 시 서버로 sdp오퍼와 룸아이디를 보내기 위해 새로 정의한 메소드
    public void sendOfferSdpWithRoomId(SessionDescription sdp, int roomId) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", "presenter");
            obj.put("sdpOffer", sdp.description);
            obj.put("roomId", roomId);

            socketService.sendMessage(obj.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }//sendOfferSdpWithRoomId



    @Override
    public void sendAnswerSdp(SessionDescription sdp) {
        Log.e(TAG, "sendAnswerSdp: ");
    }

    @Override
    public void sendLocalIceCandidate(IceCandidate iceCandidate) {
        try {
            JSONObject obj = new JSONObject();
            obj.put("id", "onIceCandidate");
            JSONObject candidate = new JSONObject();
            candidate.put("candidate", iceCandidate.sdp);
            candidate.put("sdpMid", iceCandidate.sdpMid);
            candidate.put("sdpMLineIndex", iceCandidate.sdpMLineIndex);
            obj.put("candidate", candidate);

            socketService.sendMessage(obj.toString());

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendLocalIceCandidateRemovals(IceCandidate[] candidates) {
        Log.e(TAG, "sendLocalIceCandidateRemovals: ");
    }

}
