package com.example.luna.lalaland.All.BuySongs_Kakaopay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.luna.lalaland.All.AccountManagement.SharedPrefManager;
import com.example.luna.lalaland.All.AccountManagement.SignInActivity;
import com.example.luna.lalaland.All.AccountManagement.User;
import com.example.luna.lalaland.All.Blockchain.WalletLoginActivity;
import com.example.luna.lalaland.All.MusicVideo.WatchMVActivity;
import com.example.luna.lalaland.All.Trending.TrendingActivity;
import com.example.luna.lalaland.All.Intro.HomeScreenActivity;
import com.example.luna.lalaland.All.IdeaNote.NoteHomeActivity;
import com.example.luna.lalaland.All.Utils.ApiClient;
import com.example.luna.lalaland.All.Utils.ApiService;
import com.example.luna.lalaland.All.Utils.ResponseModel;
import com.example.luna.lalaland.R;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
* '음원구입' 메뉴에 해당하는 액티비티
* 더미데이터로 몇 가지 음원 리스트를 넣어둘 예정이며,
* 디자인은 https://www.iphonelife.com/content/tip-day-how-purchase-music-apple-music 이 페이지를 참고할 것
* 음원 리스트 중 하나를 클릭하면 카카오페이 API를 이용하여 가상으로 결제가능하도록 한다.
* */

public class BuySongsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, SongAdapter.SongAdapterListner{

    private static final String TAG = BuySongsActivity.class.getSimpleName();
    private Toolbar tb_toolBar;

    //내비게이션 뷰
    DrawerLayout drawer;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;

    //음원 목록을 보여주기 위한 리사이클러뷰 아이템
    List<SongItem> songItemList;
    private RecyclerView recyclerview_buySongs;
    SongAdapter songAdapter;

    //음원 아이템
    String title, artist;
    int orderId, price;
    int i = 0;



    //카카오페이
    //https://developers.kakao.com/docs/restapi/kakaopay-api#결제프로세스
    String next_redirect_app_url; //결제요청 성공 시 웹뷰로 띄울 결제대기 화면
    String android_app_scheme;
    String next_redirect_mobile_url;
    String tid;

    /*로그인한 유저 정보를 담고 있는 sharedPreference와 유저 정보를 담을 변수*/
    SharedPreferences pref;
    String email, username, age, gender;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_songs);

//        //로그인한 유저정보 가져오기
//        pref = getSharedPreferences("userInfo", MODE_PRIVATE);
//        email = pref.getString("email", "");
//        username = pref.getString("username", "");
//        age = pref.getString("age", "");
//        gender = pref.getString("gender", "");
//        Log.d(TAG, "shared에서 꺼낸 유저 정보: "+ email);
//        Log.d(TAG, "shared에서 꺼낸 유저 정보: "+ username);
//        Log.d(TAG, "shared에서 꺼낸 유저 정보: "+ age);
//        Log.d(TAG, "shared에서 꺼낸 유저 정보: "+ gender);

        //로그인 여부를 판별, 로그인 안 되어있으면 앱을 이용할 수 없다
        if(!SharedPrefManager.getmInstance(this).isLoggedIn()) {
            Log.d(TAG, "로그인 되어 있지 않음");
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(this, SignInActivity.class));
        }

        Log.d(TAG, "로그인 되어 있음");

        final User user = SharedPrefManager.getmInstance(this).getUser(); // 현재 로그인 된 유저정보
        username = user.getUsername(); //유저네임
        email = user.getEmail();

        initViews(); //레이아웃 객체를 초기화하는 메소드


        Random random = new Random();
        orderId = random.nextInt(10000); //임의의 주문번호 생성


        //음원 리스트 더미데이터 세팅
        songItemList.add(new SongItem(R.drawable.postmalone_album, "Better Now", "Post Malone", 11000));
        songItemList.add(new SongItem(R.drawable.shallow, "Shallow", "Lady Gaga & Bradley Cooper", 12500));
        songItemList.add(new SongItem(R.drawable.halsey, "Without Me", "Halsey", 8000));
        songItemList.add(new SongItem(R.drawable.selena, "Back To You", "Selena Gomez", 10500));
        songItemList.add(new SongItem(R.drawable.ariana, "God Is A Woman", "Ariana Grande", 15300));
        songItemList.add(new SongItem(R.drawable.taylor, "Delicate", "Taylor Swift", 14800));
        songItemList.add(new SongItem(R.drawable.bebe, "Meant To Be", "Bebe Rexha", 8200));
        songItemList.add(new SongItem(R.drawable.panic, "High Hopes", "Panic! At The Disco", 9700));
        songItemList.add(new SongItem(R.drawable.lovelytheband, "Broken", "lovelytheband", 1130));
        songItemList.add(new SongItem(R.drawable.lauv, "I Like Me Better", "lauv", 9500));

    }//onCreate


    @Override // 리스트를 클릭하면 선택된 리스트 정보를 받아옴
    public void onSongSelected(SongItem songItem) {

        i++;
        songItem.setOrderId(orderId+i); //주문 번호 증가
        orderId = songItem.getOrderId();
        title = songItem.getTitle();
        artist = songItem.getArtist();
        price = songItem.getPrice();
        Toast.makeText(this, artist+"의 "+title+"을(를) 구입하기 위해 "+price+"원을 결제합니다.", Toast.LENGTH_SHORT).show();

        System.out.println("카카오 주문번호: "+orderId);
        buySongs(title, artist, price, orderId);
    }//onSongSelected


    private void initViews() {
        //뷰 초기화
        tb_toolBar = findViewById(R.id.tb_toolBar);

        //툴바 커스텀
        setSupportActionBar(tb_toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //이걸 추가해주지 않으면 툴바에 앱명 lalaland가 나옴
        tb_toolBar.setTitle("Buy Songs");

        //리사이클러뷰
        recyclerview_buySongs = findViewById(R.id.recyclerview_buySongs);
        songItemList = new ArrayList<>();
        recyclerview_buySongs.setItemAnimator(new DefaultItemAnimator());
        recyclerview_buySongs.setLayoutManager(new LinearLayoutManager(this));
        songAdapter = new SongAdapter(this, songItemList, this);
        recyclerview_buySongs.setAdapter(songAdapter);

        //내비게이션 뷰
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, tb_toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nav_header = navigationView.getHeaderView(0); //네비게이션 헤더 뷰
        TextView tv_navHeader = nav_header.findViewById(R.id.tv_navHeader);
        tv_navHeader.setText(username);
    }//initViews

    private void buySongs(String title, String artist, int price, int orderId) {
        //서버로 구매할 음원 정보를 보냄

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseModel> call = apiService.buySongs(username, title, artist, price, orderId);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {

                ResponseModel responseModel = response.body();
                String msgFromServer = responseModel.getMessage();
                Log.d(TAG, "카카오페이 결제요청 후 서버 메시지: "+msgFromServer);

                if(responseModel.getStatus() == 1) {
                    Log.d(TAG, "카카오페이 결제요청 성공0");
                    //서버에서 보낸 json response 받아오기
                    if(msgFromServer != null){
                        JsonParser jsonParser = new JsonParser();
                        JsonObject jsonObject = (JsonObject) jsonParser.parse(msgFromServer);
                        next_redirect_mobile_url = jsonObject.get("next_redirect_mobile_url").toString();
                        next_redirect_app_url = jsonObject.get("next_redirect_app_url").toString();
                        android_app_scheme = jsonObject.get("android_app_scheme").toString();
                        tid = jsonObject.get("tid").toString();

                        //string값에서 따옴표 제거
                        next_redirect_mobile_url = next_redirect_mobile_url.replace("\"", "");
                        next_redirect_app_url = next_redirect_app_url.replace("\"", "");
                        android_app_scheme = android_app_scheme.replace("\"", "");
                        tid = tid.replace("\"", "");

                        //결제 준비된 상품정보를 데이터베이스에 저장하기
                        saveLedgerInfo(username, tid, orderId);

                        Log.d(TAG, "카카오 결제화면: "+next_redirect_app_url);

                        //서버에서 받은 결제 url을 KakaoPayActivity로 보내서
                        //웹뷰로 보여줄 것
                        Intent intent = new Intent(BuySongsActivity.this, KakaoPayActivity.class);
                        intent.putExtra("kakaoAppUrl", next_redirect_app_url);
                        intent.putExtra("kakaoScheme", android_app_scheme);
                        intent.putExtra("kakaoMobileUrl", next_redirect_mobile_url);
                        intent.putExtra("tid", tid); //카카오페이 결제 승인에 필요한 정보
                        intent.putExtra("orderId", orderId);

                        startActivity(intent);
                    }//null

                    Log.d(TAG, "카카오페이--서버에서 메시지 안 옴");

                    //finish();

                } else {
                    Log.d(TAG, "카카오페이 결제요청 성공1");

                }
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.d(TAG, "카카오페이 결제요청 오류" +t.getMessage());
            }//onFailure
        });//Callback
    }//startPayment



    private void saveLedgerInfo(String username, String tid, int orderId){

        ApiService apiService = ApiClient.getClient().create(ApiService.class);


        Call<ResponseModel> call = apiService.saveLedgerInfo(username, tid, orderId);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {

                ResponseModel responseModel = response.body();
                if(responseModel != null) {
                    String msgFromServer = responseModel.getMessage();
                    Log.d(TAG, "카카오페이 결제 준비 후: "+msgFromServer);
                }



            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.d(TAG, "카카오 결제 준비 후 문제 생김" +t.getMessage());
            }//onFailure
        });//Callback
    }//proceedPayment



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_homeScreen:
                startActivity(new Intent(BuySongsActivity.this, HomeScreenActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_memo:
                startActivity(new Intent(BuySongsActivity.this, NoteHomeActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_buyMusic:
                break;

            case R.id.menu_musicVideo:
                startActivity(new Intent(BuySongsActivity.this, WatchMVActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_lunToken:
                startActivity(new Intent(BuySongsActivity.this, WalletLoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_chart:
                startActivity(new Intent(BuySongsActivity.this, TrendingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_logout:
                /*sharedpreference에 저장된 현재 유저정보를 지움*/
                SharedPrefManager.getmInstance(getApplicationContext()).logout(email);
                finish();
                break;
        }//switch
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }//onNavigationItemSelected

    @Override //뒤로가기 버튼
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }//onBackPressed

    //액티비티 종료 시
    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }


}//BuySongsActivity
