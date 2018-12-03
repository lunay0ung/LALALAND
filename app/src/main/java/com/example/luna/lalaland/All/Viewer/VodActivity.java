package com.example.luna.lalaland.All.Viewer;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.luna.lalaland.All.Broadcaster.BroadcasterActivity;
import com.example.luna.lalaland.All.Chat.ChatAdapter;
import com.example.luna.lalaland.All.Chat.ChatDataFromMongoDB;
import com.example.luna.lalaland.All.Chat.ChatItem;
import com.example.luna.lalaland.All.Utils.ConnectToMongoDB;
import com.example.luna.lalaland.R;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.ui.DebugTextViewHelper;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.model.Updates;
import com.mongodb.util.JSON;

import org.bson.BSON;
import org.bson.Document;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.mongodb.client.model.Filters.and;
import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Projections.exclude;
import static com.mongodb.client.model.Projections.excludeId;
import static com.mongodb.client.model.Projections.fields;
import static com.mongodb.client.model.Projections.include;

/*
*  종료된 방송을 다시 볼 수 있다.
*  -HomeScreenActivity에 있는 방송 목록 중 'live' 마크가 없는 것을 클릭하면 이 화면으로 이동
* cf. ViewerActivity에서는 WebRTC로 전송되는 라이브 스트리밍 방송을 볼 수 있음
*
* TODO 채팅 싱크 맞추는 중
*
* */

public class VodActivity extends AppCompatActivity {

    final private String TAG = VodActivity.class.getSimpleName();

    ProgressDialog mDialog; //비디오를 로딩할 때 유저에게 보여줄 다이얼로그
    VideoView videoView; //비디오를 보여줄 비디오뷰
    ImageButton btn_playAndPause; //



    int roomId; //유저가 다시 보고자 하는 방송의 룸아이디
    String baseVideoUrl = "http://13.124.23.131/lalaland/hls_video/"; //hls 영상을 감상할 기본 url
    String finalVideoUrl;  //유저가 홈화면에서 클릭한 영상을 볼 수 있는 최종 url
    String m3u8 = ".m3u8"; // hls--ts파일 정보를 담고 있는 m3u8 파일 확장자

    //뷰 관련
    View rootView;         //비디오 화면의 루트뷰
    PlayerView playerView; //비디오 재생 화면
    ProgressBar progressBar;

    //exoplayer 관련
    private DataSource.Factory dataSourceFactory;
    private SimpleExoPlayer player;
    private FrameworkMediaDrm mediaDrm;
    private MediaSource mediaSource;
    private DefaultTrackSelector trackSelector;

    private int startWindow;
    private long startPosition;
    private long contentPosition; //현재 플레이되고 있는 위치

    //현재 영상재생위치를 파악하기 위한 쓰레드에 쓰임
    Timer timer;
    TimerTask timerTask;

    //채팅메시지를 뿌려주기 위한 리사이클러뷰
    RecyclerView recyclerview_chat;

    //채팅 내용을 보여줄 리사이클러뷰 관련 객체
    List<ChatItem> chatItemList;
    ChatAdapter chatAdapter;
    String message;

    JsonParser jsonParser;
    JsonObject jsonObject;
    Gson gson;
    JSONObject test;

    public static final int SEND_CHATMESSAGE =1;
    int currentPosition;
    int getCurrentPosition;

    private final MyHandler myHandler = new MyHandler(this);

    String chatData, sender, content;
    JSONArray jsonArray;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vod);

        //HomeScreenActivity로부터 데이터 받기
        Intent intent = getIntent();
        roomId = intent.getIntExtra("roomId", 0); //HomeScreenActivity로부터 받아온 룸아이디
        Log.d(TAG, "다시 보려는 방송의 룸아이디: "+roomId);

        //최종적으로 플레이어에 띄울 url을 완성
        finalVideoUrl = baseVideoUrl + roomId + m3u8;
        Log.d(TAG, "다시 보려는 방송의 최종 url: "+ finalVideoUrl);


        //initViews(); //뷰 초기화
        rootView = findViewById(R.id.root);
        playerView = findViewById(R.id.playerView);
        progressBar = findViewById(R.id.progressBar);
        recyclerview_chat = findViewById(R.id.recyclerview_chat);

        //채팅메시지를 보여줄 리사이클러뷰 관련
        chatItemList = new ArrayList<>();
        recyclerview_chat.setItemAnimator(new DefaultItemAnimator());
        recyclerview_chat.setLayoutManager(new LinearLayoutManager(this));
        chatAdapter = new ChatAdapter(this, chatItemList);
        recyclerview_chat.setAdapter(chatAdapter);

        //몽고디비 message컬렉션에서 가져온 채팅 정보를 파싱하기 위해
        jsonParser = new JsonParser();

//        GetMessageContent getMessageContent = new GetMessageContent();
//        if(getMessageContent.getState() == Thread.State.NEW){
//            getMessageContent.start();
//        }

    }//onCreate




    private void initializePlayer() { //플레이어에 비디오 재생 준비
        playerView.requestFocus();
        TrackSelection.Factory videoTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory();
        trackSelector = new DefaultTrackSelector(videoTrackSelectionFactory);
        player = ExoPlayerFactory.newSimpleInstance(this, trackSelector);

        initDataSource();
        initHLSPlayer();
        player.setPlayWhenReady(true); //영상 준비되면 자동 재생
        playerView.setPlayer(player);

        if(player.getPlayWhenReady()){ //플레이어가 준비되면
//            GetContentPosition getContentPosition = new GetContentPosition();
//            if(getContentPosition.getState()== Thread.State.NEW) {
//                getContentPosition.setDaemon(true);
//                getContentPosition.start();
//            }//State

        }//getPlayWhenReady


        //플레이어가 준비되었는지 여부에 따라 프로그레스바를 띄운다
        player.addListener(new Player.EventListener() {
            @Override //참고 https://developer.android.com/reference/android/media/session/PlaybackState
            //https://google.github.io/ExoPlayer/doc/reference/com/google/android/exoplayer2/Player.EventListener.html
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
             if(playbackState == PlaybackStateCompat.STATE_CONNECTING || playbackState == PlaybackStateCompat.STATE_BUFFERING) {

                progressBar.setVisibility(View.VISIBLE);
             } else if(playWhenReady) {
                 progressBar.setVisibility(View.GONE);

             } else if(playbackState == PlaybackStateCompat.STATE_ERROR) {
                 progressBar.setVisibility(View.VISIBLE);
                 Toast.makeText(VodActivity.this, "state error", Toast.LENGTH_SHORT).show();
             }

            }//onPlayerStateChanged

            @Override
            public void onPlayerError(ExoPlaybackException error) {
                Log.d(TAG, "영상 재생 중 플레이어 에러 발생: "+error);
                //Toast.makeText(VodActivity.this, "동영상 로딩 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                //finish();
            }
        });//EventListener

    }//initializePlayer

    private class GetMessageContent extends Thread {
        @Override
        public void run() {
            super.run();
            //몽고디비에 접속해서 채팅 메시지 가져오기
            ConnectToMongoDB connectToMongoDB = new ConnectToMongoDB(TAG, "message");

            FindIterable<Document> docs = connectToMongoDB.getCollection().find(
                    eq("roomId", roomId)).projection(fields(include("sender", "content", "contentPosition"), excludeId()));

            BasicDBList documentList = new BasicDBList();
            for(Document doc: docs){
                documentList.add(doc);
            }//
            System.out.println("실험 메시지: "+JSON.serialize(documentList));

            chatData = String.valueOf(JSON.serialize(documentList));
            System.out.println("스레드 메시지 테스트: "+chatData);
            try {
                jsonArray = new JSONArray(chatData);
                System.out.println("어레이 테스트"+jsonArray);
            } catch (JSONException e) {
                e.printStackTrace();
            }//try

        }//run
    }//class

    private class GetContentPosition extends Thread {
        @Override
        public void run() {
           while(true){

               if(player != null) {
                   contentPosition =  player.getContentPosition(); //현재 몇 초째 재생하고 있는가
                   contentPosition = contentPosition/1000;
                   Log.d(TAG,"현재 재생 중인 영상 위치: "+contentPosition);

                   Message message = myHandler.obtainMessage(); //메시지 객체
                  // message.what = SEND_CONTENTPOSITION; //메시지 아이디
                   message.arg1 = (int) contentPosition; //메시지 내용
                   myHandler.sendMessage(message);  //메시지 보내기
               }//if

               try {
                   Thread.sleep(1000);
               } catch (InterruptedException e) {
                   e.printStackTrace();
               }//try

           }//while
        }//run
    }//getContentPosition



    private final class MyHandler extends Handler{

        private final WeakReference<VodActivity> mActivity;

        public MyHandler(VodActivity vodActivity) {
            mActivity = new WeakReference<VodActivity>(vodActivity);
        }//MyHandler


        // 스레드로부터 메시지 받아오기
        // Handler 객체를 생성한 스레드 만이 다른 스레드가 전송하는 Message나 Runnable 객체를
        // 수신 가능
        @Override
        public void handleMessage(Message msg) {
            VodActivity activity = mActivity.get();
            if(activity != null){ //액티비티가 살아있을 때

                currentPosition = msg.arg1;
                System.out.println("핸들러가 받은 현재 위치 테스트"+currentPosition);


                if(jsonArray != null) {
                    try {

                        JSONObject eachObject;
                        for (int i = 0; i < jsonArray.length(); i ++){
                            eachObject = jsonArray.getJSONObject(i);
                            System.out.println("테스트2"+eachObject);

                            System.out.println("핸들러 현재 위치 테스트2: "+currentPosition);

                            //동영상이 현재 플레이되고 있는 시간에 맞춰서 채팅 메시지를 리사이클러뷰에 뿌려준다
                            if(eachObject.getInt("contentPosition") == currentPosition){
                                content  = eachObject.getString("content");
                                sender = eachObject.getString("sender");
                                System.out.println("테스트3: "+content+"&"+sender);

                            }//if

                        }//for

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                }// if(jsonArray != null)


            }//if(activity != null)
        }//handleMessage
    }//class MyHandler



//    private class getMessageFromMongo extends AsyncTask<Void, Void, Void> {
//        @Override
//        protected void onPreExecute() {
//            super.onPreExecute();
//        }
//
//        @Override
//        protected void onProgressUpdate(Void... values) {
//            super.onProgressUpdate(values);
//        }
//
//        @Override
//        protected Void doInBackground(Void... voids) {
//
//            //몽고디비에 접속해서 현재 영상 위치에 맞는 메시지를 가져온 후
//            //플레이어 화면에 뿌려준다
//            //몽고디비 message 컬렉션에 접속
//            ConnectToMongoDB connectToMongoDB = new ConnectToMongoDB(TAG, "message");
//
//            //roomId, contentPosition이 일치하는 다큐먼트의 message를 가져오면 됨
////            FindIterable<Document> docs = connectToMongoDB.getCollection().find(
////                    and(eq("roomId", roomId),eq("contentPosition", contentPosition))
////            );
//
//
//            FindIterable<Document> docs = connectToMongoDB.getCollection().find(
//                    eq("roomId", roomId)).projection(fields(include("sender", "content", "contentPosition"), excludeId()));
//
//            for (Document doc : docs) {
//
//                String jsonMessage = doc.toJson();
//                Log.d(TAG, "메시지 + 전체: "+jsonMessage);
//
//                 jsonParser = new JsonParser();
//                 jsonObject = (JsonObject) jsonParser.parse(jsonMessage);
//
//                Log.d(TAG, "메시지만: "+jsonObject.get("content"));
//
////
////                message = doc.getString("content");  //라이브 스트리밍 시 contentPosition초에 주고받았던 채팅메시지
////                Log.d(TAG,"영상 위치/ 메시지-----"+contentPosition+"초: "+message);
//            }//for
//            return null;
//        }//doInBackground

//        @Override
//        protected void onPostExecute(Void aVoid) {
//            super.onPostExecute(aVoid);
//            runOnUiThread(new Runnable() {
//                @Override
//                public void run() {
//
//                    if(message != null){
//                        Log.d(TAG, "영상에 메시지 뿌려주기 전 검증: "+message);
//                        chatItemList.add(new ChatItem("user", message));
//                        // 데이터 추가가 완료되었으면 notifyDataSetChanged() 메서드를 호출해 데이터 변경 체크를 실행
//                        chatAdapter.notifyDataSetChanged();
//                        //최신 메시지가 보이도록 리사이클러뷰 자동 스크롤
//                        recyclerview_chat.smoothScrollToPosition(chatItemList.size() -1);
//                    }
//                }
//            });//runOnUiThread
//        }//onPostExecute
//    }//getMessageFromMongo



    private void releasePlayer() { //플레이어 해제

        if (player!=null) {
            player.release();
            player = null;
        }

    }//releasePlayer

    private void initDataSource() {
        dataSourceFactory =
                new DefaultDataSourceFactory(this,
                        Util.getUserAgent(this, "LALALAND"),
                        new DefaultBandwidthMeter());
    }


    private void initHLSPlayer() {//hls 미디어 소스
        mediaSource = new HlsMediaSource.Factory(dataSourceFactory)
                .createMediaSource(Uri.parse(finalVideoUrl));
        player.prepare(mediaSource);
    }


    @Override
    protected void onStart() {
        super.onStart();
        initializePlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initializePlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releasePlayer();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer();
    }

    //액티비티 종료 시
    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }

}//VodActivity
