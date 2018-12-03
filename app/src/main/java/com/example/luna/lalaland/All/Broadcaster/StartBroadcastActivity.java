package com.example.luna.lalaland.All.Broadcaster;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.luna.lalaland.All.Utils.ApiClient;
import com.example.luna.lalaland.All.Utils.ApiService;
import com.example.luna.lalaland.All.Utils.ResponseModel;
import com.example.luna.lalaland.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
* 2018/9/26
* 스트리머가 '방송 시작하기'버튼을 누르면 시작되는 액티비티(배경화면을 투명하게 해 다이얼로그처럼 보이도록 할 것)
* 방송명을 입력하고 확인버튼을 누르면 방송이 시작되고, 해당 방송이 방송 목록에 추가된다.
* 방송명을 입력하지 않으면 확인버튼을 눌러도 방송을 시작할 수 없다.
*
* */
public class StartBroadcastActivity extends AppCompatActivity {

    private static final String TAG = "방송 스트리밍 시작 액티비티";

    private EditText et_title, et_genre; //방송제목 입력 칸
    String title; //방송제목
    private Button btn_cancel, btn_okay; //취소, 확인
    InputMethodManager manager; //키보드 제어 위한 매니저
    List<Broadcast> broadcastList; //리사이클러뷰에 넣어줄 방송목록 리스트
    String msgFromServer; //서버에서 받아올 메시지
    String streamer;
    String genre; //방송에서 보여줄 음악 장르

    int roomId; //현재 스트리밍 중인 여러 개의 방송을 식별할 수 있는 아이디 -> 새로운 방송이 시작될 때마다 생성된다


    /*로그인한 유저 정보를 담고 있는 sharedPreference와 유저 정보를 담을 변수*/
    SharedPreferences pref;
    String email, username, age, gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_broadcast);

        //로그인한 유저정보 가져오기
        pref = getSharedPreferences("userInfo", MODE_PRIVATE);
        email = pref.getString("email", "");
        username = pref.getString("username", "");
        age = pref.getString("age", "");
        gender = pref.getString("gender", "");
        Log.d(TAG, "shared에서 꺼낸 유저 정보: "+ email);
        Log.d(TAG, "shared에서 꺼낸 유저 정보: "+ username);
        Log.d(TAG, "shared에서 꺼낸 유저 정보: "+ age);
        Log.d(TAG, "shared에서 꺼낸 유저 정보: "+ gender);


        //스트리머명 변수에 유저네임을 넣는다
        streamer = username;

        //얍
        et_title = findViewById(R.id.et_title);
        et_genre = findViewById(R.id.et_genre);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_okay = findViewById(R.id.btn_okay);
        manager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);

        //방제 입력창에 포커스 주기
        et_title.requestFocus();
        //자동으로 키보드 올리기
        manager.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        //방송목록리스트 초기화
        broadcastList = new ArrayList<>();

        //클릭 리스너 달아주긔
        btn_cancel.setOnClickListener(mClicklistener);
        btn_okay.setOnClickListener(mClicklistener);

        et_genre.setOnTouchListener(mTouchListner); //장르 선택 칸에 터치리스너를 달아줌 -> et_genre를 클릭하면 장르 선택이 가능한 다이얼로그가 뜬다

    }//onCreate

    View.OnClickListener mClicklistener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.btn_cancel:
                    Log.e(TAG, "방송시작 취소");
                    manager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); //키보드 내리기
                    finish();
                    break;

                case R.id.btn_okay:
                    title = et_title.getText().toString(); //입력된 방송제목 받아오기

                    if(TextUtils.isEmpty(title) || TextUtils.isEmpty(genre)) {
                        Toast.makeText(StartBroadcastActivity.this, "방송 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
                    } else {

                        Log.e(TAG, "방송명: "+title);
                        manager.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0); //방제가 입력되면 소프트키보드를 아래로 내려줌
                        Intent intent = new Intent();
                        intent.putExtra("result", title);
                        setResult(RESULT_OK, intent); //홈스크린 액티비티로 보내줄 방제


                        //방제가 제대로 입력되면 룸아이디가 생성된다
                        Random random = new Random();
                        roomId = random.nextInt(1000000); //임의의 룸아이디 생성
                        Log.d(TAG, "방금 새로 정한 방송 룸 아이디: "+roomId);

                        //isLive = true; //라이브 중임.
                        //startLive(streamer, title, roomId, isLive); //라이브 방송 리스트 저장


                        Intent intent2 = new Intent(StartBroadcastActivity.this, BroadcasterActivity_.class);
                        intent2.putExtra("roomId", roomId); //생성된 방송정보를 BroadcasterActivity를 경유하여 서버로 보낸다
                        intent2.putExtra("streamer", streamer);
                        intent2.putExtra("title", title);
                        intent2.putExtra("genre", genre);
                        Log.d(TAG, "생성된 방송 정보를 보냄: "+streamer);
                        Log.d(TAG, "생성된 방송 정보를 보냄: "+title);
                        Log.d(TAG, "생성된 방송 정보를 보냄: "+genre);
                        startActivity(intent2.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)); //액티비티 이동 시 애니메이션 효과 제거
                        finish();
                    }
                    break;
            }//switch

        }//onClick
    };//mClicklistener

    /*
    * 방송을 시작할 때 방송에서 보여줄 음악 장르를 선택한다
    * */
    private void selectGenre() {
        final List<String> genreList = new ArrayList<>(); //가능한 장르 선택지
        genreList.add("K-POP");
        genreList.add("POP");
        genreList.add("R&B");
        genreList.add("Latin");
        genreList.add("Dance");
        genreList.add("Country");
        genreList.add("CCM");
        genreList.add("Classic");
        genreList.add("Jazz");
        genreList.add("Hip-Hop");
        genreList.add("Electronic");


        /*
        * 성별을 직접 쓰거나 비공개하는 옵션을 두는 게 좋다고 생각하지만
        * 예외처리 + 로그 분석 등을 생각했을 때 보류하는 게 좋다고 판단
        * */
        // genderList.add("직접 쓰기");
        // genderList.add("비공개");
        final CharSequence[] items =  genreList.toArray(new String[ genreList.size()]);

        final List SelectedItems  = new ArrayList();
        int defaultItem = 0;
        SelectedItems.add(defaultItem);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("성별을 선택해주세요.");
        builder.setSingleChoiceItems(items, defaultItem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SelectedItems.clear();
                        SelectedItems.add(which);
                    }
                });
        builder.setPositiveButton("확인",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        String gender="";
                        if (!SelectedItems.isEmpty()) {
                            int index = (int) SelectedItems.get(0);
                            genre = genreList.get(index);

                            et_genre.setText(genre);
                            Log.d(TAG, "selectedGender / 선택된 장르: "+genre);

                        }//if (!SelectedItems.isEmpty())
                        Toast.makeText(getApplicationContext(),
                                "Items Selected.\n"+ genre , Toast.LENGTH_LONG)
                                .show();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();

    }//selectGenre


    View.OnTouchListener mTouchListner = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {

                //터치된 상태
                case MotionEvent.ACTION_DOWN:
                    hideSoftKeyboard(et_genre);
                    selectGenre(); //장르 선택 라디오버튼이 있는 다이얼로그 띄우기
                    break;

                case MotionEvent.ACTION_CANCEL:
                    //터치 안 된 상태
                case MotionEvent.ACTION_UP:
            }//switch
            return false;
        }//onTouch
    };//OnTouchListener

    protected void hideSoftKeyboard(EditText input) { //et_gender 클릭 시 라디오버튼을 띄우는 대신 키보드를 숨기도록 함
        input.setInputType(0);
        manager.hideSoftInputFromWindow(input.getWindowToken(), 0);
    }//hideSoftKeyboard



    //액티비티 종료 시
    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }

}//StartBroadcastActivity
