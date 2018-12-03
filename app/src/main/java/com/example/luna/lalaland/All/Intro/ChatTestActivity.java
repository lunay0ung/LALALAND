package com.example.luna.lalaland.All.Intro;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.luna.lalaland.All.Chat.ChatAdapter;
import com.example.luna.lalaland.All.Chat.ChatItem;
import com.example.luna.lalaland.R;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/*
* 2018/10/3
* 채팅이 제대로 되는지 간단히 확인할 수 있는 임시 액티비티
*
* 2018/10/29
* 현재 쓸모없으나...프로젝트 완성 되기 전까지 삭제 보류
*
* cf. HomeScreenActivity에서 ChatTestActivity로 이동 가능한 버튼의 visibility=GONE 시킨 상태
* visibility만 변경하면 홈에서 버튼을 통해 이 액티비티로 이동할 수 있으므로 Intro 패키지에 둠
*
* */

public class ChatTestActivity extends AppCompatActivity{

    Handler handler;
    String messageReceived, messageSent;
    SocketChannel socketChannel;
    private static final String HOST = "13.124.23.131";
    private static final int PORT = 5001;
   // String msg;
   // ActivityMainBinding binding;

    Button btn_sendMsg;
    EditText et_sendMsg;
    TextView tv_msg;

    //채팅 내용을 보여줄 리사이클러뷰 관련 객체
    List<ChatItem> chatItemList;
    private RecyclerView recyclerview_chat; //액티비티에 배치한 리사이클러뷰
    ChatAdapter chatAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_test);

        btn_sendMsg = findViewById(R.id.btn_sendMsg);
        et_sendMsg = findViewById(R.id.et_sendMsg);
        tv_msg = findViewById(R.id.tv_msg);

        recyclerview_chat = findViewById(R.id.recyclerview_chat);


        //리사이클러뷰 관련
        chatItemList = new ArrayList<>();
        recyclerview_chat.setItemAnimator(new DefaultItemAnimator());
        recyclerview_chat.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(this, chatItemList);
        recyclerview_chat.setAdapter(chatAdapter);


        handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    socketChannel = SocketChannel.open();
                    socketChannel.configureBlocking(true);
                    socketChannel.connect(new InetSocketAddress(HOST, PORT));
                } catch (Exception ioe) {
                    Log.d("asd", ioe.getMessage() + "a");
                    ioe.printStackTrace();

                }
                checkUpdate.start();
            }
        }).start();


        btn_sendMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    messageSent = et_sendMsg.getText().toString();
                    if (!TextUtils.isEmpty(messageSent)) {
                        new SendmsgTask().execute(messageSent);

                        //보낸 메시지도 화면에 보여준다
                        chatItemList.add(new ChatItem("me", messageSent));
                        // 데이터 추가가 완료되었으면 notifyDataSetChanged() 메서드를 호출해 데이터 변경 체크를 실행
                        chatAdapter.notifyDataSetChanged();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }//onCreate

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
                    et_sendMsg.setText("");
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
                Charset charset = Charset.forName("UTF-8");
                messageReceived = charset.decode(byteBuffer).toString();
                Log.d("받은 메시지/receive", "msg :" + messageReceived);
                handler.post(showUpdate);
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

    private Thread checkUpdate = new Thread() {

        public void run() {
            try {
                String line;
                receive();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    };

    private Runnable showUpdate = new Runnable() {

        public void run() {
            //받은 메시지를 화면에 띄운다
            chatItemList.add(new ChatItem("other user", messageReceived));
            // 데이터 추가가 완료되었으면 notifyDataSetChanged() 메서드를 호출해 데이터 변경 체크를 실행
            chatAdapter.notifyDataSetChanged();

            //String receive =  data;
            //tv_msg.setText(messageReceived);
            //binding.receiveMsgTv.setText(receive);
        }

    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            socketChannel.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}//Main2Activity
