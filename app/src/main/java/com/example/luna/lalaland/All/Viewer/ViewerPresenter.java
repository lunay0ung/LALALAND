package com.example.luna.lalaland.All.Viewer;

import android.app.Application;
import android.util.Log;

import com.example.luna.lalaland.All.Rtc_Peer.kurento.KurentoViewerRTCClient;
import com.example.luna.lalaland.All.Rtc_Peer.kurento.models.CandidateModel;
import com.example.luna.lalaland.All.Rtc_Peer.kurento.models.response.ServerResponse;
import com.example.luna.lalaland.All.Rtc_Peer.kurento.models.response.TypeResponse;
import com.example.luna.lalaland.All.Utils.ApiClient;
import com.example.luna.lalaland.All.Utils.ApiService;
import com.example.luna.lalaland.All.Utils.ConnectToMongoDB;
import com.example.luna.lalaland.All.Utils.ResponseModel;
import com.example.luna.lalaland.All.Utils.RxScheduler;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.hannesdorfmann.mosby.mvp.MvpBasePresenter;
import com.mongodb.client.model.UpdateOptions;
import com.mongodb.client.model.Updates;
import com.nhancv.webrtcpeer.rtc_comm.ws.BaseSocketCallback;
import com.nhancv.webrtcpeer.rtc_comm.ws.DefaultSocketService;
import com.nhancv.webrtcpeer.rtc_comm.ws.SocketService;
import com.nhancv.webrtcpeer.rtc_peer.PeerConnectionClient;
import com.nhancv.webrtcpeer.rtc_peer.PeerConnectionParameters;
import com.nhancv.webrtcpeer.rtc_peer.SignalingEvents;
import com.nhancv.webrtcpeer.rtc_peer.SignalingParameters;
import com.nhancv.webrtcpeer.rtc_peer.StreamMode;
import com.nhancv.webrtcpeer.rtc_peer.config.DefaultConfig;
import com.nhancv.webrtcpeer.rtc_plugins.RTCAudioManager;

import org.bson.Document;
import org.java_websocket.handshake.ServerHandshake;
import org.webrtc.IceCandidate;
import org.webrtc.PeerConnection;
import org.webrtc.SessionDescription;
import org.webrtc.StatsReport;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Updates.combine;
import static com.mongodb.client.model.Updates.currentDate;
import static com.mongodb.client.model.Updates.set;

/**
 * 시청자가 HomeScreenActivity에서 방송목록 중 하나를 클릭하면
 * 해당 방송의 룸아이디를 서버로 보낸다.
 * 서버에서는 그 룸아이디를 가진 방송 송출자와 연결해준다.
 */

public class ViewerPresenter extends MvpBasePresenter<ViewerView>
        implements SignalingEvents, PeerConnectionClient.PeerConnectionEvents {

    private static final String STREAM_HOST = "wss://13.124.23.131:8443/one2many"; //쿠렌토 일대다 방송 스트리밍 서버 주소

    private Application application;
    private SocketService socketService;
    private Gson gson;

    private PeerConnectionClient peerConnectionClient;
    private KurentoViewerRTCClient rtcClient;
    private PeerConnectionParameters peerConnectionParameters;
    private DefaultConfig defaultConfig;
    private RTCAudioManager audioManager;
    private SignalingParameters signalingParameters;
    private boolean iceConnected;

    private int roomId; //룸아이디 --HomeScreenActivity에서 받아옴
    int i =0;

    public ViewerPresenter(Application application) {
        this.application = application;
        this.socketService = new DefaultSocketService(application);
        this.gson = new Gson();
    }

    public void initPeerConfig() {
        rtcClient = new KurentoViewerRTCClient(socketService);
        defaultConfig = new DefaultConfig();
        peerConnectionParameters = defaultConfig.createPeerConnectionParams(StreamMode.RECV_ONLY);
        peerConnectionClient = PeerConnectionClient.getInstance();
        peerConnectionClient.createPeerConnectionFactory(
                application.getApplicationContext(), peerConnectionParameters, this);
    }

    public void disconnect() {
        if (rtcClient != null) {
            rtcClient = null;
        }
        if (peerConnectionClient != null) {
            peerConnectionClient.close();
            peerConnectionClient = null;
        }

        if (audioManager != null) {
            audioManager.stop();
            audioManager = null;
        }

        if (socketService != null) {
            socketService.close();
        }

        if (isViewAttached()) {
            getView().disconnect();
        }

        //라이브 시청자가 방을 나가면 해당 룸아이디를 가진 방송의 viewers 수에서 1을 뺀다
       checkHowPopularItIs(roomId, "minusViewers");

        //TODO 몽고디비의 conversation 컬렉션의 members에서도 해당 유저를 제외시켜야 함 --우선순위가 아니라서 일단은 보류
    }//disconnect

    public void checkHowPopularItIs(int roomId, String request) {
        //라이브 방송 시청자수(viewers)를 +1 시키거나
        //저장된 동영상의 조회수(views)를 +1 시킨다

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseModel> call = apiService.checkStatistics(roomId, request); //statistics는 서버에 보내는 신호
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                ResponseModel responseModel = response.body();
                String msgFromServer = responseModel.getMessage();
                Log.d(TAG, "라이브 방송 시청자수 -1후 서버 메시지: "+msgFromServer);

                if(responseModel.getStatus() == 1) {
                    Log.d(TAG, "라이브 방송 시청자수 -1 성공: "+roomId);
                } else {
                    Log.d(TAG, "라이브 방송 시청자수 -1 오류");
                }
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.d(TAG, "라이브 방송 시청자수 -1 설정 후 추가 서버메시지" +t.getMessage());
            }//onFailure
        });//Callback

    }//checkHowPopularItIs


    //유저가 클릭한 방송에 성공적으로 접속하면, 몽고디비에 저장된 conversation 컬렉션의 members 항목에 현재 유저를 추가한다
    Thread getInTheChatRoom = new Thread(new Runnable() {
        @Override
        public void run() {

            //몽고디비와 연결하여 conversation이라는 이름의 가진 컬렉션을 가져옴
            ConnectToMongoDB connectToMongoDB = new ConnectToMongoDB(TAG, "chatroom");

            //가져온 conversation컬렉션에서 roomId를 가진 다큐먼트의 members항목에 시청자의 username 추가
            connectToMongoDB.getCollection().updateOne(
                    eq("roomId", roomId), Updates.addToSet("members", "viewer")
            );//updateOne


        }//run
    });//Thread


    //방송에서 나가면 해당 conversation 컬렉션에서 해당 roomId 다큐먼트의 members항목에서 현재 유저 제외
    Thread getOutOfTheChatRoom = new Thread(new Runnable() {
        @Override
        public void run() {

            i++;

            //몽고디비와 연결하여 conversation이라는 이름의 가진 컬렉션을 가져옴
            ConnectToMongoDB connectToMongoDB = new ConnectToMongoDB(TAG, "conversation");

            //가져온 conversation컬렉션에서 roomId를 가진 다큐먼트의 members항목에 시청자의 username 추가
//            connectToMongoDB.getCollection().updateOne(
//                    eq("roomId", roomId), Updates.
//            );//updateOne


        }//run
    });//Thread


    public void startCall(int receivedRoomID) {
        if (rtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.");
            return;
        }

        roomId = receivedRoomID; //시청자가 클릭한 방송의 룸 아이디. HomeScreenActivity에서 받아온다
        Log.d("ViewerPresenter", "시청자가 보고싶어하는 방송의 룸아이디 검증: "+roomId);

        rtcClient.connectToRoom(STREAM_HOST, new BaseSocketCallback() {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                super.onOpen(serverHandshake);
                RxScheduler.runOnUi(o -> {
                    if (isViewAttached()) {
                        getView().logAndToast("Socket connected");
                    }
                });
                SignalingParameters parameters = new SignalingParameters(
                        new LinkedList<PeerConnection.IceServer>() {
                            {
                                //스턴서버
                                add(new PeerConnection.IceServer("stun:stun.l.google.com:19302"));
                            }
                        }, true, null, null, null, null, null);
                onSignalConnected(parameters);


                if(getInTheChatRoom.getState() == Thread.State.NEW) {

                    getInTheChatRoom.start();
                    Log.d(TAG, "시청자가 몽고디비 conversation컬렉션의 members에 추가됨");
                }

            }//open

            @Override
            public void onMessage(String serverResponse_) {
                super.onMessage(serverResponse_);
                try {
                    ServerResponse serverResponse = gson.fromJson(serverResponse_, ServerResponse.class);

                    switch (serverResponse.getIdRes()) {
                        case VIEWER_RESPONSE:
                            if (serverResponse.getTypeRes() == TypeResponse.REJECTED) {
                                RxScheduler.runOnUi(o -> {
                                    if (isViewAttached()) {
                                        getView().logAndToast(serverResponse.getMessage());
                                    }
                                });
                            } else {
                                SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER,
                                                                                serverResponse.getSdpAnswer());
                                onRemoteDescription(sdp);
                            }
                            break;
                        case ICE_CANDIDATE:
                            CandidateModel candidateModel = serverResponse.getCandidate();
                            onRemoteIceCandidate(
                                    new IceCandidate(candidateModel.getSdpMid(), candidateModel.getSdpMLineIndex(),
                                                     candidateModel.getSdp()));
                            break;
                        case STOP_COMMUNICATION:
                            RxScheduler.runOnUi(o -> {
                                if (isViewAttached()) {
                                    getView().stopCommunication();
                                }
                            });
                            break;

                    }
                } catch (JsonSyntaxException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onClose(int i, String s, boolean b) {
                super.onClose(i, s, b);
                RxScheduler.runOnUi(o -> {
                    if (isViewAttached()) {
                        getView().logAndToast("방송이 종료되었습니다(socket closed)");
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                RxScheduler.runOnUi(o -> {
                    if (isViewAttached()) {
                        getView().logAndToast(e.getMessage());
                    }
                });
            }

        });


        // Create and audio manager that will take care of audio routing,
        // audio modes, audio device enumeration etc.
        audioManager = RTCAudioManager.create(application.getApplicationContext());
        // Store existing audio settings and change audio mode to
        // MODE_IN_COMMUNICATION for best possible VoIP performance.
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start((audioDevice, availableAudioDevices) ->
                                   Log.d(TAG, "onAudioManagerDevicesChanged: " + availableAudioDevices + ", "
                                              + "selected: " + audioDevice));
    }

    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    private void callConnected() {
        if (peerConnectionClient == null) {
            Log.w(TAG, "Call is connected in closed or error state");
            return;
        }
        // Enable statistics callback.
        peerConnectionClient.enableStatsEvents(true, 1000);
    }

    @Override
    public void onSignalConnected(SignalingParameters params) {
        RxScheduler.runOnUi(o -> {
            if (isViewAttached()) {
                signalingParameters = params;
                peerConnectionClient
                        .createPeerConnection(getView().getEglBaseContext(), null,
                                              getView().getRemoteProxyRenderer(), null,
                                              signalingParameters);

                if (signalingParameters.initiator) {
                    if (isViewAttached()) getView().logAndToast("Creating OFFER...");
                    // Create offer. Offer SDP will be sent to answering client in
                    // PeerConnectionEvents.onLocalDescription event.
                    peerConnectionClient.createOffer();
                } else {
                    if (params.offerSdp != null) {
                        peerConnectionClient.setRemoteDescription(params.offerSdp);
                        if (isViewAttached()) getView().logAndToast("Creating ANSWER...");
                        // Create answer. Answer SDP will be sent to offering client in
                        // PeerConnectionEvents.onLocalDescription event.
                        peerConnectionClient.createAnswer();
                    }
                    if (params.iceCandidates != null) {
                        // Add remote ICE candidates from room.
                        for (IceCandidate iceCandidate : params.iceCandidates) {
                            peerConnectionClient.addRemoteIceCandidate(iceCandidate);
                        }
                    }
                }
            }
        });
    }

    @Override
    public void onRemoteDescription(SessionDescription sdp) {
        RxScheduler.runOnUi(o -> {
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received remote SDP for non-initilized peer connection.");
                return;
            }
            peerConnectionClient.setRemoteDescription(sdp);
            if (!signalingParameters.initiator) {
                if (isViewAttached()) getView().logAndToast("Creating ANSWER...");
                // Create answer. Answer SDP will be sent to offering client in
                // PeerConnectionEvents.onLocalDescription event.
                peerConnectionClient.createAnswer();
            }
        });
    }

    @Override
    public void onRemoteIceCandidate(IceCandidate candidate) {
        RxScheduler.runOnUi(o -> {
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received ICE candidate for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.addRemoteIceCandidate(candidate);
        });
    }

    @Override
    public void onRemoteIceCandidatesRemoved(IceCandidate[] candidates) {
        RxScheduler.runOnUi(o -> {
            if (peerConnectionClient == null) {
                Log.e(TAG, "Received ICE candidate removals for a non-initialized peer connection.");
                return;
            }
            peerConnectionClient.removeRemoteIceCandidates(candidates);
        });
    }

    @Override
    public void onChannelClose() {
        RxScheduler.runOnUi(o -> {
            if (isViewAttached()) getView().logAndToast("Remote end hung up; dropping PeerConnection");
            disconnect();
        });
    }

    @Override
    public void onChannelError(String description) {
        Log.e(TAG, "onChannelError: " + description);
    }

    @Override
    public void onLocalDescription(SessionDescription sdp) {
        RxScheduler.runOnUi(o -> {
            if (rtcClient != null) {
                if (signalingParameters.initiator) {
                    //rtcClient.sendOfferSdp(sdp); 기존에 있던 메소드 박제
                    rtcClient.sendOfferSdp(sdp, roomId); //룸아이디
                } else {
                    rtcClient.sendAnswerSdp(sdp);
                }
            }
            if (peerConnectionParameters.videoMaxBitrate > 0) {
                Log.d(TAG, "Set video maximum bitrate: " + peerConnectionParameters.videoMaxBitrate);
                peerConnectionClient.setVideoMaxBitrate(peerConnectionParameters.videoMaxBitrate);
            }
        });
    }

    @Override
    public void onIceCandidate(IceCandidate candidate) {
        RxScheduler.runOnUi(o -> {
            if (rtcClient != null) {
                rtcClient.sendLocalIceCandidate(candidate);
            }
        });
    }

    @Override
    public void onIceCandidatesRemoved(IceCandidate[] candidates) {
        RxScheduler.runOnUi(o -> {
            if (rtcClient != null) {
                rtcClient.sendLocalIceCandidateRemovals(candidates);
            }
        });
    }

    @Override
    public void onIceConnected() {
        RxScheduler.runOnUi(o -> {
            iceConnected = true;
            callConnected();
        });
    }

    @Override
    public void onIceDisconnected() {
        RxScheduler.runOnUi(o -> {
            if (isViewAttached()) getView().logAndToast("ICE disconnected");
            iceConnected = false;
            disconnect();
        });
    }

    @Override
    public void onPeerConnectionClosed() {

    }

    @Override
    public void onPeerConnectionStatsReady(StatsReport[] reports) {
        RxScheduler.runOnUi(o -> {
            if (iceConnected) {
                Log.e(TAG, "run: " + reports);
            }
        });
    }

    @Override
    public void onPeerConnectionError(String description) {
        Log.e(TAG, "onPeerConnectionError: " + description);
    }
}
