package com.example.luna.lalaland.All.Viewer;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.luna.lalaland.All.Broadcaster.Broadcast;
import com.example.luna.lalaland.All.Broadcaster.ListBroadcastModel;
import com.example.luna.lalaland.All.Chat.ChatAdapter;
import com.example.luna.lalaland.All.Chat.ChatItem;
import com.example.luna.lalaland.All.Chat.ChatRoom;
import com.example.luna.lalaland.All.Utils.ApiClient;
import com.example.luna.lalaland.All.Utils.ApiService;
import com.example.luna.lalaland.All.Utils.ConnectToMongoDB;
import com.example.luna.lalaland.All.Utils.ResponseModel;
import com.example.luna.lalaland.R;
import com.google.gson.Gson;
import com.hannesdorfmann.mosby.mvp.MvpActivity;
import com.mongodb.client.model.Updates;
import com.nhancv.webrtcpeer.rtc_plugins.ProxyRenderer;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.bson.Document;
import org.w3c.dom.Text;
import org.webrtc.EglBase;
import org.webrtc.RendererCommon;
import org.webrtc.SurfaceViewRenderer;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.ContentValues.TAG;
import static com.mongodb.client.model.Filters.eq;


/*
* WebRTC로 전송되는 라이브 스트리밍 방송을 볼 수 있다.
* -HomeScreenActivity에 있는 방송 목록 중 'live'라고 표시되어있는 것을 클릭하면 이 화면으로 이동
* -방송 중 스트리머와 채팅할 수 있음 --> 이 액티비티에 있는 리사이클러뷰는 채팅메시지 발신, 수신을 위한 것
* cf. VodActivity에서는 종료된 방송을 Vod로 다시 볼 수 있다.
* */

@EActivity(R.layout.activity_viewer)
public class ViewerActivity extends MvpActivity<ViewerView, ViewerPresenter> implements ViewerView {
    private static final String TAG = ViewerActivity.class.getSimpleName();

    @ViewById(R.id.vGLSurfaceViewCall)
    protected SurfaceViewRenderer vGLSurfaceViewCall;

    @ViewById(R.id.et_sendMessage) //메시지를 쓸 수 있는 edittext
    protected EditText et_sendMessage;
    @ViewById(R.id.btn_sendMessage) //메시지 보내기 버튼
    protected Button btn_sendMessage;
    @ViewById(R.id.recyclerview_chat) //채팅 메시지를 보여줄 리사이클러뷰
    protected RecyclerView recyclerview_chat;
    @ViewById(R.id.iv_live)
    protected ImageView iv_live; //라이브 중임을 알리는 마크
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

    //채팅데이터를 주고 받기 위한 서버-클라이언 트 간 통신시 필요한 것들
    Handler handler;
    String messageReceived, messageSent; //받은 메시지, 보낸 메시지
    SocketChannel socketChannel;
    private static final String HOST = "13.124.23.131"; //아마존 ec2 인스턴스 주소
    private static final int PORT = 5001; //채팅 시 사용할 포트

    //메시지를 보여줄 리사이클러뷰
    //채팅 내용을 보여줄 리사이클러뷰 관련 객체
    List<ChatItem> chatItemList;
    ChatAdapter chatAdapter;


    private EglBase rootEglBase;
    private ProxyRenderer remoteProxyRenderer;
    private Toast logToast;


    ChatRoom chatRoomId;//채팅방 관리를 위한 클래스
    Gson gson;//서버에 보낼 값을 json형식으로 바꿈

    int roomId; //시청자가 보고자 하는 방송의 룸아이디 - 방송마다 고유한 룸아이디를 가짐 -미디어 서버로 보냄
    String roomIdJson; //룸아이디를 json형식으로 바꾸어 채팅서버로 보냄
    String messageJson; //채팅서버로 보낼 메시지를 json형식으로 바꾼 것


    //메시지를 전송할 때의 시간을 구하기 위함
    //10/18 현재 안 쓰기로 결정했지만 삭제 보류
    DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    Date date;
    Calendar calendar = Calendar.getInstance();

    @AfterViews
    protected void init() {

        Intent intent = getIntent(); //HomeScreenActivity에서 보낸 룸아이디를 받는 인텐트
        roomId = intent.getIntExtra("roomId", 0);
        Log.d(TAG, "시청자가 클릭한 방송의 룸아이디@뷰어액티비티: "+roomId);


        //채팅서버로 룸아이디를 전송하기 전 관련 클래스 생성
        chatRoomId = new ChatRoom();
        chatRoomId.setRoomId(roomId+"");
        chatRoomId.setCreateRoom(false); //기존 채팅방 합류 (채팅방 생성 false)
        gson = new Gson();
        roomIdJson = gson.toJson(chatRoomId);  //Json으로 변경한 룸아이디


        //config peer
        remoteProxyRenderer = new ProxyRenderer();
        rootEglBase = EglBase.create();

        vGLSurfaceViewCall.init(rootEglBase.getEglBaseContext(), null);
        vGLSurfaceViewCall.setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        vGLSurfaceViewCall.setEnableHardwareScaler(true);
        vGLSurfaceViewCall.setMirror(true);
        remoteProxyRenderer.setTarget(vGLSurfaceViewCall);

        presenter.initPeerConfig();
        presenter.startCall(roomId); //룸아이디를 함께 넣어 ViewerPresenter로 보낸다

        //리사이클러뷰 관련
        chatItemList = new ArrayList<>();
        recyclerview_chat.setItemAnimator(new DefaultItemAnimator());
        recyclerview_chat.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(this, chatItemList);
        recyclerview_chat.setAdapter(chatAdapter);

        //채팅 서버와 통신
        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));
                    new SendmsgTask().execute(roomIdJson); //채팅 서버와 연결되면 해당 방송의 룸아이디를 보냄
                    Log.d(TAG, "서버 연결 직후: 시청자-채팅서버로 보낼 값"+ roomIdJson);


                } catch (Exception ioe) {
                    Log.d("방송 송출화면/채팅 시 소켓 통신 에러", ioe.getMessage() + "");
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
                        chatRoomMsg.setRoomId(roomId+"");
                        chatRoomMsg.setMessage(messageSent); //채팅방에 메시지를 넣어줌
                        messageJson = gson.toJson(chatRoomMsg); //메시지를 json형식으로 변환

                        new SendmsgTask().execute(messageJson);
                        Log.d(TAG, "방송 시청자가 서버로 보내는 채팅메시지: "+messageJson);


                     //   new SendmsgTask().execute(messageSent);
                     //   Log.d(TAG, "방송 시청자가 서버로 보내는 채팅메시지: "+messageSent);

                        //보낸 메시지도 화면에 보여준다
                        chatItemList.add(new ChatItem("나", messageSent));
                        // 데이터 추가가 완료되었으면 notifyDataSetChanged() 메서드를 호출해 데이터 변경 체크를 실행
                        chatAdapter.notifyDataSetChanged();


                        //안 쓰기로 하고 삭제하려다 보류하는 코드 10/18- 한달 후에도 안 쓰면 삭제
//                        Thread saveMessages = new Thread(new Runnable() {
//                            @Override
//                            public void run() {
//                                date = new Date();
//
//                                Log.d(TAG, roomId+"번 방에서 시청자가 채팅메시지를 보내는 시간/ 몽고: "+dateFormat.format(date));
//                                //몽고디비와 연결하여 COLLECTION_CONVERSATION을 가진 컬렉션을 가져옴
//                                ConnectToMongoDB connectToMongoDB = new ConnectToMongoDB("몽고디비", "message");
//
//                                //다큐먼트 생성
//                                Document doc = new Document()
//                                        .append("roomId", roomId)
//                                        .append("sender", "viewer")
//                                        .append("content", messageSent)
//                                        .append("sentAt", dateFormat.format(date));
//                                        //.append("sentAt", hour+":"+min+":"+sec);
//                                connectToMongoDB.getCollection().insertOne(doc);
//
//                            }//run
//                        });
//
//                        saveMessages.start();
//                        Log.d(TAG, "몽고디비로 메시지 보냄");

                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }//catch
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



    private void getViewers(int roomId) {
        //현재 시청자수를 가져옴
        String request = "getViewers";
        Log.d(TAG, "뷰어액티비티-방송 룸아이디: "+roomId);
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
                    Log.d("시청자가 서버로부터 받은 메시지", "msg :" + messageReceived);
                    handler.post(showUpdate); //받은 메시지 화면에 띄우기
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

        }//run
    };//Runnable


    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socketChannel.close(); //소켓통신 종료
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    @Override
    public void disconnect() {
        remoteProxyRenderer.setTarget(null);
        if (vGLSurfaceViewCall != null) {
            vGLSurfaceViewCall.release();
            vGLSurfaceViewCall = null;
        }

        finish();
    }

    @NonNull
    @Override
    public ViewerPresenter createPresenter() {
        return new ViewerPresenter(getApplication());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        presenter.disconnect();
    }

    @Override
    public void stopCommunication() {
        onBackPressed();
    }

    @Override
    public void logAndToast(String msg) {
        Log.d(TAG, msg);
        if (logToast != null) {
            logToast.cancel();
        }
        logToast = Toast.makeText(this, msg, Toast.LENGTH_SHORT);
        logToast.show();
    }

    @Override
    public EglBase.Context getEglBaseContext() {
        return rootEglBase.getEglBaseContext();
    }

    @Override
    public VideoRenderer.Callbacks getRemoteProxyRenderer() {
        return remoteProxyRenderer;
    }

}
