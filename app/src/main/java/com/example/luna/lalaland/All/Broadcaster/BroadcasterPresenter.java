package com.example.luna.lalaland.All.Broadcaster;

import android.app.Application;
import android.util.Log;

import com.example.luna.lalaland.All.Rtc_Peer.kurento.KurentoPresenterRTCClient;
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
import org.webrtc.VideoCapturer;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 라이브 방송 '송출' 시 webrtc서버와 통신한다.
 * 스트리머가 방송을 시작하면 임의의 룸아이디가 생성되어 미디어 서버로 전송된다.
 * 해당 룸아이디로 각 방송의 송출자와 시청자를 연결할 수 있다.
 */

public class BroadcasterPresenter extends MvpBasePresenter<BroadcasterView>
        implements SignalingEvents, PeerConnectionClient.PeerConnectionEvents {
    private static final String TAG = "방송 라이브 스트리밍 프레젠터";

    private static final String STREAM_HOST =  "wss://13.124.23.131:8443/one2many"; //일대다 방송을 위한 서버 주소

    private Application application;
    private SocketService socketService;
    private Gson gson;

    private PeerConnectionClient peerConnectionClient;
    private KurentoPresenterRTCClient rtcClient;
    private PeerConnectionParameters peerConnectionParameters;
    private DefaultConfig defaultConfig;
    private RTCAudioManager audioManager;
    private SignalingParameters signalingParameters;
    private boolean iceConnected;


    private int roomId; //각 방송의 아이디
    private String msgFromServer; //서버로부터 받은 메시지
    private Boolean broadcastSaved = false;

    //채팅방 데이터를 저장하기 위해 몽고디비 연동
    String MongoDB_IP = "13.124.23.131";
    int MongoDB_PORT = 27017;
    String DATABASE = "lalaland";
    String COLLECTION_NAME="chatroom";

    //메시지를 전송할 때의 시간을 구하기 위한 것
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date;
    Calendar calendar = Calendar.getInstance();
    int hour = calendar.get(calendar.HOUR_OF_DAY);
    int min = calendar.get ( calendar.MINUTE );
    int sec = calendar.get ( calendar.SECOND );


    public BroadcasterPresenter(Application application) {
        this.application = application;
        this.socketService = new DefaultSocketService(application);
        this.gson = new Gson();
    }

    public void initPeerConfig() {
        rtcClient = new KurentoPresenterRTCClient(socketService);
        defaultConfig = new DefaultConfig();
        peerConnectionParameters = defaultConfig.createPeerConnectionParams(StreamMode.SEND_ONLY);
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

        //현재 룸아이디를 데이터베이스로 보내서 이 룸아이디를 가진 방송의 상태를 false로 바꿔줌
        //단 데이터베이스에 해당 방송의 룸 아이디가 등록이 되어있을 때만 이 메소드가 호출되어야 함.
        Log.d(TAG, "방송 저장 되어있지?"+broadcastSaved);
        if(broadcastSaved){ //방송정보를 서버로 보냄
            //TODO 썸네일 주소
            String thumbnailUrl = "http://13.124.23.131/lalaland/thumbnail/"+roomId+".png";
            endLive(roomId, false, thumbnailUrl); //방송이 종료되었으니 해당 방송의 라이브 상태를 수정하고 썸네일 정보 입력
        }



    }//disconnect

    public void startCall(String streamer, String title, int roomIdReceived, String genre) { //webrtc 미디어 서버에게 접속 요청을 보낸다


        if (rtcClient == null) {
            Log.e(TAG, "AppRTC client is not allocated for a call.// 미디어 서버와 연결 오류");
            return;
        }

        roomId = roomIdReceived; //BroadcasterActivity에서 룸아이디를 받아왔음
        Log.d("BroadcasterPresenter", "서버로 보낼 룸아이디 검증: "+roomId);

        rtcClient.connectToRoom(STREAM_HOST, new BaseSocketCallback() {
            @Override
            public void onOpen(ServerHandshake serverHandshake) {
                super.onOpen(serverHandshake);
                RxScheduler.runOnUi(o -> {
                    if (isViewAttached()) {
                        Log.d(TAG, "방송 저장 직전: "+genre);
                        startLive(streamer, title, roomId, true, genre); //isLive = true;
                        getView().logAndToast("방송을 시작합니다(Socket connected).");
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


            }//onOpen

            @Override
                            public void onMessage(String serverResponse_) {
                            super.onMessage(serverResponse_);
                            try {
                                ServerResponse serverResponse = gson.fromJson(serverResponse_, ServerResponse.class);
                                System.out.println("테스트1");
                                switch (serverResponse.getIdRes()) {
                                    case PRESENTER_RESPONSE:
                                        if (serverResponse.getTypeRes() == TypeResponse.REJECTED) {
                                            RxScheduler.runOnUi(o -> {
                                                if (isViewAttached()) {
                                                    getView().logAndToast(serverResponse.getMessage());
                                                    System.out.println("테스트2"+serverResponse.getMessage());
                                                }
                                            });
                            } else {
                                SessionDescription sdp = new SessionDescription(SessionDescription.Type.ANSWER,
                                                                                serverResponse.getSdpAnswer());
                                            System.out.println("테스트3-sdp: "+sdp);
                                onRemoteDescription(sdp);

                            }


                            break;

                        case ICE_CANDIDATE:
                            CandidateModel candidateModel = serverResponse.getCandidate();
                            onRemoteIceCandidate(
                                    new IceCandidate(candidateModel.getSdpMid(), candidateModel.getSdpMLineIndex(),
                                                     candidateModel.getSdp()));

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
                        getView().logAndToast("Socket closed");
                    }
                    disconnect();
                });
            }

            @Override
            public void onError(Exception e) {
                super.onError(e);
                RxScheduler.runOnUi(o -> {
                    if (isViewAttached()) {
                        getView().logAndToast(e.getMessage());
                    }
                    disconnect();
                });
            }

        });


        //오디오 매니저 생성-오디오 매니저는 오디오 라우팅, 오디오 모드, 오디오 열거 등을 담당함
        audioManager = RTCAudioManager.create(application.getApplicationContext());
        //현재의 오디오 세팅을 저장하고 가능한한 최상의 VoIP 성능을 위해 오디오 모드를 MODE_IN_COMMUNICATION으로 변경한다
        Log.d(TAG, "Starting the audio manager...");
        audioManager.start((audioDevice, availableAudioDevices) ->
                                   Log.d(TAG, "onAudioManagerDevicesChanged: " + availableAudioDevices + ", "
                                              + "selected: " + audioDevice));
    }





    //현재 스트리밍 중인 방송목록을 데이터베이스에 저장하기 위한 메소드
    //제목, 크리에이터명, 룸아이디가 저장된다
    //TODO genre도 추가한다
    public void startLive(String username, String title, int roomId, boolean isLive, String genre) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseModel> call = apiService.startLive(username, title, roomId, isLive, genre);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                ResponseModel responseModel = response.body();
                msgFromServer = responseModel.getMessage();
                Log.d(TAG, "서버메시지: "+msgFromServer);

                if(responseModel.getStatus() == 1) { //방송 리스트 등록 성공
                    broadcastSaved = true;
                    //해당 방송 정보가 저장되었음 ->
                    // 소켓통신 실패 시 다양한 이유가 있는데,
                    // 애초에 방송을 시작조차 못한 경우 외에
                    // 스트리머가 방송을 정상적으로 시작하고 종료되어 데이터베이스에 저장된 해당 방송의 상태를 업데이트할 때 필요한 정보
                    Log.d(TAG, "서버로 전송해 DB에 저장된 방송정보: "+username+" /제목: "+title+" /룸아이디: "+roomId
                            +"/ 라이브 중인가?: "+isLive+"/장르: "+genre);
//
                    /*자꾸 네트워크 오류 발생해서 일단 주석처리 2018-11-08*/
//                    if(connectToMongo.getState() ==  Thread.State.NEW) {
//
//                        connectToMongo.start();
//                    }

                    Log.d(TAG, "몽고디비 다큐먼트 삽입: 룸아이디: "+roomId);
                } else {
                    Log.d(TAG, "서버 전송-방송 목록 저장 시 에러 발생: "+msgFromServer);
                }
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.d(TAG, "라이브 방송 등록 후 추가 서버 메시지: "+t.getMessage());
            }//onFailure
        });
    }//startLive

    Thread connectToMongo = new Thread(new Runnable() {
        @Override
        public void run() {
            //TODO 몽고디비
            //방송을 시작하면 mongodb로 해당 방송의 roomId와 스트리머의 유저네임을 보내서 conversation 컬렉션을 만든다
            //conversation : {roomId: '__', members: ['user1', 'user2' ]} 형태가 될 예정

            date = new Date();
            //몽고디비와 연결하여 COLLECTION_CONVERSATION을 가진 컬렉션을 가져옴
            ConnectToMongoDB connectToMongoDB = new ConnectToMongoDB(TAG, COLLECTION_NAME);

            //다큐먼트 생성
            Document doc = new Document()
                    .append("roomId", roomId)
                    .append("createdAt",dateFormat.format(date))
                    .append("playStartedAt", hour+":"+min+":"+sec)
                    .append("members", Arrays.asList("streamer"));
            connectToMongoDB.getCollection().insertOne(doc);

        }//run
    });//Thread


    //현재 룸아이디를 데이터베이스로 보내서 이 룸아이디를 가진 방송의 상태를 false로 바꿔줌
    public void endLive(int roomId, Boolean isLive, String thumbnail){
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseModel> call = apiService.endLive(roomId, isLive, thumbnail); //룸아이디 roodId번인 방송의 라이브 여부를 false로 수정
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {

                ResponseModel responseModel = response.body();
                String msgFromServer = responseModel.getMessage();
                Log.d(TAG, "라이브 방송 종료 설정 후 서버에서 보낸 메시지: "+msgFromServer);

                if(responseModel.getStatus() == 1) {
                    Log.d(TAG, "라이브가 끝났음이 잘 등록된 방송의 룸아이디: "+roomId);
                } else {
                    Log.d(TAG, "라이브 방송 종료설정 오류");
                }
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.d(TAG, "라이브 방송 상태 업데이트 후 추가 서버메시지" +t.getMessage());
            }//onFailure
        });//Callback
    }//endLive



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
                VideoCapturer videoCapturer = null;
                if (peerConnectionParameters.videoCallEnabled) {
                    videoCapturer = getView().createVideoCapturer();
                }
                peerConnectionClient
                        .createPeerConnection(getView().getEglBaseContext(), getView().getLocalProxyRenderer(),
                                              new ArrayList<>(), videoCapturer,
                                              signalingParameters);

                if (signalingParameters.initiator) {
                    if (isViewAttached()) getView().logAndToast("Creating OFFER...");
                    //offer를 생성. 생성된 SDP offer는 이 방송에 접속하는 클라이언트에게 보내진다
                        //클라이언트는 PeerConnectionEvents.onLocalDescription event.에 있음
                    // (Create offer. Offer SDP will be sent to answering client in
                    // PeerConnectionEvents.onLocalDescription event.)
                    peerConnectionClient.createOffer();
                } else {
                    if (params.offerSdp != null) {
                        peerConnectionClient.setRemoteDescription(params.offerSdp);
                        if (isViewAttached()) getView().logAndToast("Creating ANSWER...");
                        // answer생성. Create answer. Answer SDP will be sent to offering client in
                        // PeerConnectionEvents.onLocalDescription event.
                        peerConnectionClient.createAnswer();
                    }
                    if (params.iceCandidates != null) {
                        // 원격 ICE candidate를 추가한다. Add remote ICE candidates from room.
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
                    // rtcClient.sendOfferSdp(sdp); // 원래 있던 메소드-보관
                    //방금 시작된 방송의 룸아이디를 서버로 보낸다
                    rtcClient.sendOfferSdpWithRoomId(sdp, roomId);
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
