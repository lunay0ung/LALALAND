package com.example.luna.lalaland.All.Broadcaster;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import android.widget.Button;

import com.example.luna.lalaland.All.Chat.ChatAdapter;
import com.example.luna.lalaland.All.Chat.ChatItem;
import com.example.luna.lalaland.All.Chat.ChatRoom;
import com.example.luna.lalaland.All.Utils.ApiClient;
import com.example.luna.lalaland.All.Utils.ApiService;
import com.example.luna.lalaland.All.Utils.ConnectToMongoDB;
import com.example.luna.lalaland.R;
import com.google.gson.Gson;
import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.nhancv.npermission.NPermission;
import com.nhancv.webrtcpeer.rtc_plugins.ProxyRenderer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.bson.Document;
import org.webrtc.Camera1Enumerator;
import org.webrtc.Camera2Enumerator;
import org.webrtc.CameraEnumerator;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoRenderer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * 라이브 방송 스트리머(송출자)가 보는 화면
 * -방송 중 시청자와 채팅할 수 있다.
 *  ㄴ이 액티비티에 있는 리사이클러뷰는 채팅메시지 발신, 수신을 위한 것
 * -우측 상단의 OFF 버튼을 눌러서 방송을 종료할 수 있다.
 *
 */
@EActivity(R.layout.activity_broadcaster)
public class BroadcasterActivity extends MvpActivity<BroadcasterView, BroadcasterPresenter>
        implements BroadcasterView, NPermission.OnPermissionResult {
    private static final String TAG = BroadcasterActivity.class.getSimpleName();

    @ViewById(R.id.vGLSurfaceViewCall) //카메라 화면
    protected SurfaceViewRenderer vGLSurfaceViewCall;
//    @ViewById(R.id.btn_flipCamera) //카메라 방향전환 버튼
//    protected Button btn_flipCamera;
    protected ImageView iv_live;
    @ViewById(R.id.iv_viewerIcon)
    protected ImageView iv_viewerIcon;
    @ViewById(R.id.tv_viewers)
    protected TextView tv_viewers; //핸들러를 이용하여 5초마다 시청자수를 갱신하여 보여줌

    //runOnUiThread 작업을 위한 타이머
    Timer timer;
    TimerTask timerTask;

    //현재 방송 정보를 담을 방송목록 리스트
    List<Broadcast> broadcastList;
    int currentViewers;

    @ViewById(R.id.btn_stopBroadcasting) //스트리밍 중지 버튼
    protected Button btn_stopBroadcasting;
    private NPermission nPermission; //유저에게 카메라, 오디오 사용 시 권한 허용을 요청
    private EglBase rootEglBase;
    private ProxyRenderer localProxyRenderer;
    private Toast logToast;
    private boolean isGranted;

    @ViewById(R.id.et_sendMessage) //메시지를 쓸 수 있는 edittext
    protected EditText et_sendMessage;
    @ViewById(R.id.btn_sendMessage) //메시지 보내기 버튼
    protected Button btn_sendMessage;
    @ViewById(R.id.recyclerview_chat) //채팅 메시지를 보여줄 리사이클러뷰
    protected RecyclerView recyclerview_chat;

    //채팅데이터를 주고 받기 위한 서버-클라이언 트 간 통신시 필요한 것들
    Handler handler;
    String messageReceived, messageSent; //받은 메시지, 보낸 메시지
    SocketChannel socketChannel;
    private static final String HOST = "13.124.23.131"; //아마존 ec2 인스턴스 주소
    private static final int PORT = 5001; //채팅 시 사용할 포트


    //채팅 내용을 보여줄 리사이클러뷰 관련 객체
    List<ChatItem> chatItemList;
    ChatAdapter chatAdapter;

    ChatRoom chatRoomId; //채팅방 관리를 위한 클래스
    Gson gson; //서버에 보낼 값을 json형식으로 바꿈

    String streamer, title, genre; //스트리머(방송 송출자) 이름과 방송 제목, 방송 장르
    Boolean isLive; //라이브 방송 중인지 방송이 종료되었는지 여부 구별
    int roomId; //현재 방송의 룸아이디 -미디어 서버로 보냄
    String roomIdJson; //룸아이디를 json형식으로 바꾸어 채팅서버로 보냄
    String messageJson; //채팅서버로 보낼 메시지를 json형식으로 바꾼 것

    //메시지를 전송할 때의 시간을 구하기 위한 것
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date;
    Calendar calendar = Calendar.getInstance();
    int hour = calendar.get(calendar.HOUR_OF_DAY); //쓰려다가 안 쓰는 변수들..일단 삭제 보류(10/18)
    int min = calendar.get ( calendar.MINUTE );
    int sec = calendar.get ( calendar.SECOND );


    //방송이 시작한 시간으로부터 얼마나 시간이 흘렀는지 측정하기 위한 것
    int contentPosition = 0;


    @AfterViews
    protected void init() {

        //StartBroadcastActivity에서 생성된 방송정보를 받아온다
        Intent intent = getIntent();
        roomId = intent.getIntExtra("roomId", 0);
        streamer = intent.getStringExtra("streamer");
        title = intent.getStringExtra("title");
        genre = intent.getStringExtra("genre");
        Log.d("TAG", "방송액티비티에서 서버로 보낼 룸아이디: "+roomId);
        Log.d("TAG", "방송액티비티에서 서버로 보낼 장르: "+genre);

        nPermission = new NPermission(true);

        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);



        //config peer
        localProxyRenderer = new ProxyRenderer();
        rootEglBase = EglBase.create();

        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        vGLSurfaceViewCall.setEnableHardwareScaler(true);
        vGLSurfaceViewCall.setMirror(true);
        localProxyRenderer.setTarget(vGLSurfaceViewCall);

        presenter.initPeerConfig();

        //방송이 시작한 시간으로부터 얼마나 시간이 흘렀는지 측정하기 위한 쓰레드
        GetContentPosition getContentPosition = new GetContentPosition();
        Thread thread = new Thread(getContentPosition);
        thread.setDaemon(true);


        //채팅메시지를 보여줄 리사이클러뷰 관련
        chatItemList = new ArrayList<>();
        recyclerview_chat.setItemAnimator(new DefaultItemAnimator());
        recyclerview_chat.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(this, chatItemList);
        recyclerview_chat.setAdapter(chatAdapter);


        //채팅서버로 룸아이디를 전송하기 전 관련 클래스 생성
        chatRoomId = new ChatRoom();
        chatRoomId.setRoomId(roomId+"");
        chatRoomId.setCreateRoom(true); //새로운 채팅방 생성
        gson = new Gson();
        roomIdJson = gson.toJson(chatRoomId);  //Json으로 변경한 룸아이디

        //채팅 서버와 통신
        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open(); //소켓채널을 열어줌
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT)); //13.124.23.131의 5001 포트로 연결

                    thread.start(); //채팅서버와 연결된 후 몇 초가 지났는지 측정


                    new SendmsgTask().execute(roomIdJson); //채팅 서버와 연결되면 해당 방송의 룸아이디를 보냄
                    Log.d(TAG, "서버 연결 직후: 송출자-채팅서버로 보낼 값"+ roomIdJson);

                } catch (Exception ioe) {
                    Log.d("스트리밍 중- 채팅 시 소켓 통신 에러", ioe.getMessage() + "");
                    ioe.printStackTrace();

                }//catch
                checkUpdate.start(); //채팅내용 업데이트
            }//run
        }).start();


        //'보내기'버튼을 누르면 채팅 메시지를 보낸다
        btn_sendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    messageSent = et_sendMessage.getText().toString();
                    if (!TextUtils.isEmpty(messageSent)) {


                        ChatRoom chatRoomMsg = new ChatRoom(); //채팅방 관리 클래스
                        chatRoomMsg.setMessage(messageSent); //채팅방에 메시지를 넣어줌
                        chatRoomMsg.setRoomId(roomId+"");
                        messageJson = gson.toJson(chatRoomMsg); //메시지를 json형식으로 변환
                        new SendmsgTask().execute(messageJson);
                        Log.d(TAG, "방송 송출자가 서버로 보내는 채팅메시지: "+messageJson);
                        // new SendmsgTask().execute(messageSent);
                    //    Log.d(TAG, "방송 송출자가 서버로 보내는 채팅메시지: "+messageSent);
                        //보낸 메시지도 화면에 보여준다
                        chatItemList.add(new ChatItem("나", messageSent));
                        // 데이터 추가가 완료되었으면 notifyDataSetChanged() 메서드를 호출해 데이터 변경 체크를 실행
                        chatAdapter.notifyDataSetChanged();

                        //최신 메시지가 보이도록 리사이클러뷰 자동 스크롤
                        recyclerview_chat.smoothScrollToPosition(chatItemList.size() -1);


                        Thread saveMessages = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                date = new Date();

                                Log.d(TAG, roomId+"번 방에서 스트리머가 채팅메시지를 보내는 시간/ 몽고: "+dateFormat.format(date));
                                //몽고디비와 연결하여 COLLECTION_CONVERSATION을 가진 컬렉션을 가져옴
                                ConnectToMongoDB connectToMongoDB = new ConnectToMongoDB(TAG, "message");


                                //다큐먼트 생성
                                Document doc = new Document()
                                        .append("roomId", roomId)
                                        .append("sender", "streamer")
                                        .append("content", messageSent)
                                        .append("contentPosition", contentPosition)
                                        .append("sentAt", dateFormat.format(date));
                                        //.append("sentAt", hour+":"+min+":"+sec);
                                connectToMongoDB.getCollection().insertOne(doc);


                            }//run
                        });

                        saveMessages.start();
                        Log.d(TAG, "몽고디비로 메시지 보냄");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }//catch
            }//onClick
        });//setOnClickListener


        //레이아웃 우측 상단의 off버튼을 누르면 스트리밍 방송을 종료한다
        //종료 전 한번 더 묻는다
        btn_stopBroadcasting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                askBeforeEndLive(); //종료 전 재확인 다이얼로그
            }//onClick
        });//setOnClickListener

        timer = new Timer();
        timerTask = new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() { //현재 해당 방송을 시청 중인 시청자수를 가져온다
                    @Override
                    public void run() {

                        getViewers(roomId);
                        tv_viewers.setText(currentViewers+"");

                    }//run
                });//Runnable
            }//run
        };//TimerTask
        timer.schedule(timerTask, 0, 5000); //0초 이후 시작하여 5초마다 한번씩 run

    }//init()


    //방송이 시작한 시간으로부터 얼마나 시간이 흘렀는지 측정하기 위한 쓰레드
    class GetContentPosition implements Runnable {
        @Override
        public void run() {
            while (true) {
                contentPosition++;
                Log.d(TAG, "방송 시작 한 후로 흐른 시간은 "+ contentPosition +"초");
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }//run
    }//GetContentPosition

    private void getViewers(int roomId) {
        //현재 시청자수를 가져옴
        String request = "getViewers";
        Log.d(TAG, "브로드캐스터액티비티-방송 룸아이디: "+roomId);
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ListBroadcastModel> call = apiService.getViewers(roomId);
        call.enqueue(new Callback<ListBroadcastModel>() {
            @Override
            public void onResponse(Call<ListBroadcastModel> call, Response<ListBroadcastModel> response) {
                ListBroadcastModel listBroadcastModel = response.body();
                if(listBroadcastModel.getStatus() == 1 ){
                    Log.d(TAG, "현재 방송 시청자수 가져오기 성공");
                    currentViewers = listBroadcastModel.getViewers();
                } else  {
                    Log.d(TAG, "현재 방송 시청자수 가져오기 절반의 성공: "+listBroadcastModel.getMessage());
                }

            }//onResponse

            @Override
            public void onFailure(Call<ListBroadcastModel> call, Throwable t) {
                Log.d(TAG, "현재 방송 시청자수 가져오기 실패: "+t.getMessage());
            }//onFailure
        });//Callback
    }//getViewers




    void askBeforeEndLive()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //builder.setTitle("AlertDialog Title");
        builder.setMessage("방송을 종료하시겠습니까?");
        builder.setPositiveButton("예",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(BroadcasterActivity.this, "방송을 종료합니다.", Toast.LENGTH_SHORT).show();
                        presenter.disconnect(); //방송 종료
                    }
                });
        builder.setNegativeButton("아니오",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();
    }

    private class SendmsgTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            try {
                socketChannel
                        .socket()
                        .getOutputStream()
                        .write(strings[0].getBytes("UTF-8")); // 서버로
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    et_sendMessage.setText(""); //메시지 입력창 비우기
                    //binding.sendMsgEditText.setText("");
                }
            });
        }
    }

    void receive() {
        while (true) {
            try {
                ByteBuffer byteBuffer = ByteBuffer.allocate(256);
                //서버가 비정상적으로 종료했을 경우 IOException 발생
                int readByteCount = socketChannel.read(byteBuffer); //데이터받기
                Log.d("readByteCount", readByteCount + "");
                //서버가 정상적으로 Socket의 close()를 호출했을 경우
                if (readByteCount == -1) {
                    throw new IOException();
                }

                byteBuffer.flip(); // 문자열로 변환
                Charset charset = Charset.forName("UTF-8"); //한글 인코딩
                messageReceived = charset.decode(byteBuffer).toString(); //받은 메시지 String에 담기
                Log.d("송출자가 서버로부터 받은 메시지", "msg :" + messageReceived);
                handler.post(showUpdate); //받은 메시지 화면에 띄우기


                Thread saveMessages = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        date = new Date();

                        Log.d(TAG, roomId+"번 방에서 스트리머가 받은 메시지를 저장하는 시간/ 몽고: "+dateFormat.format(date));
                        //몽고디비와 연결하여 COLLECTION_CONVERSATION을 가진 컬렉션을 가져옴
                        ConnectToMongoDB connectToMongoDB = new ConnectToMongoDB("몽고디비", "message");

                        //다큐먼트 생성
                        Document doc = new Document()
                                .append("roomId", roomId)
                                .append("sender", "viewer") //나중에 수정 필요
                                .append("content", messageReceived)
                                .append("contentPosition", contentPosition)
                                .append("sentAt", dateFormat.format(date));
                        //.append("sentAt", hour+":"+min+":"+sec);
                        connectToMongoDB.getCollection().insertOne(doc);


                    }//run
                });

                saveMessages.start();

            } catch (IOException e) {
                Log.d("getMsg", e.getMessage() + "");
                try {
                    socketChannel.close();
                    break;
                } catch (IOException ee) {
                    ee.printStackTrace();
                }
            }
        }
    }

    //다른 유저에게서 메시지를 받았는지 확인
    private Thread checkUpdate = new Thread() {

        public void run() {
            try {
                String line;
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }//catch
        }//run
    };//Thread


    private Runnable showUpdate = new Runnable() {
        public void run() {
            //받은 메시지를 화면에 띄운다
            chatItemList.add(new ChatItem("다른 유저", messageReceived));
            // 데이터 추가가 완료되었으면 notifyDataSetChanged() 메서드를 호출해 데이터 변경 체크를 실행
            chatAdapter.notifyDataSetChanged();

            //최신 메시지가 보이도록 리사이클러뷰 자동 스크롤
            recyclerview_chat.smoothScrollToPosition(chatItemList.size() -1);

        }//run
    };//Runnable


    @Override //webrtc
    protected void onDestroy() {
        super.onDestroy();
        try {
            socketChannel.close(); //소켓통신 종료
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @Override //webrtc
    public void disconnect() {
        localProxyRenderer.setTarget(null);
        if (vGLSurfaceViewCall != null) {
            vGLSurfaceViewCall.release();
            vGLSurfaceViewCall = null;
        }

        finish();
    }//disconnect



    @Override //webrtc
    public void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < 23 || isGranted) {

            //BroadcasterPresenter로 방금 스트리밍이 요청된 방송의 정보를 보낸다
            //미디어 서버와의 통신이 성공하면 해당 내용이 저장되고, 성공하지 못하면 저장되지 않는다
            presenter.startCall(streamer, title, roomId, genre);
            Log.d(TAG, "broadcasterPresenter로 보낼 내용2: "+streamer+title+roomId+genre);


        } else {
            Log.e(TAG, "0");
            nPermission.requestPermission(BroadcasterActivity.this, Manifest.permission.CAMERA);
            Log.e(TAG, "1");
        }

    }

    @Override //webrtc
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        nPermission.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override //webrtc
    public void onPermissionResult(String permission, boolean isGranted) {
        switch (permission) {
            case Manifest.permission.CAMERA:
                this.isGranted = isGranted;
                if (!isGranted) {
                    nPermission.requestPermission(this, Manifest.permission.CAMERA);
                } else {
                    //nPermission.requestPermission(this, Manifest.permission.RECORD_AUDIO);

                    //BroadcasterPresenter로 방금 스트리밍이 요청된 방송의 정보를 보낸다
                    //미디어 서버와의 통신이 성공하면 해당 내용이 저장되고, 성공하지 못하면 저장되지 않는다
                    presenter.startCall(streamer, title, roomId, genre);
                    Log.d(TAG, "broadcasterPresenter로 보낼 내용1: "+streamer+title+roomId+genre);

                }
                break;
            default:
                break;
        }
    }

    @NonNull
    @Override //webrtc
    public BroadcasterPresenter createPresenter() {
        return new BroadcasterPresenter(getApplication());
    }


    @Override //webrtc
    public void onBackPressed() { //뒤로 가기 버튼을 눌러도 방송을 종료할 수 있다
        askBeforeEndLive();
        //super.onBackPressed();
    }

    @Override //webrtc
    public void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    @Override //webrtc
    public VideoCapturer createVideoCapturer() {
        VideoCapturer videoCapturer;
        if (useCamera2()) {
            if (!captureToTexture()) {
                return null;
            }
            videoCapturer = createCameraCapturer(new Camera2Enumerator(this));
        } else {
            videoCapturer = createCameraCapturer(new Camera1Enumerator(captureToTexture()));
        }
        if (videoCapturer == null) {
            return null;
        }
        return videoCapturer;
    }

    @Override //webrtc
    public EglBase.Context getEglBaseContext() {
        return rootEglBase.getEglBaseContext();
    }

    @Override //webrtc
    public VideoRenderer.Callbacks getLocalProxyRenderer() {
        return localProxyRenderer;
    }

    private boolean useCamera2() {
        return Camera2Enumerator.isSupported(this) && presenter.getDefaultConfig().isUseCamera2();
    }

    //webrtc
    private boolean captureToTexture() {
        return presenter.getDefaultConfig().isCaptureToTexture();
    }

    //webrtc
    private VideoCapturer createCameraCapturer(CameraEnumerator enumerator) {
        final String[] deviceNames = enumerator.getDeviceNames();
        // First, try to find front facing camera
        for (String deviceName : deviceNames) {
            if (enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        // 전방 카메라가 발견되지 않을 때 Front facing camera not found, try something else
        for (String deviceName : deviceNames) {
            if (!enumerator.isFrontFacing(deviceName)) {
                VideoCapturer videoCapturer = enumerator.createCapturer(deviceName, null);

                if (videoCapturer != null) {
                    return videoCapturer;
                }
            }
        }

        return null;
    }

    //액티비티 종료 시
    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }

}//BroadcasterActivity
