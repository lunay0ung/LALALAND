package com.example.luna.lalaland.All.Intro;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.luna.lalaland.All.AccountManagement.SharedPrefManager;
import com.example.luna.lalaland.All.AccountManagement.SignInActivity;
import com.example.luna.lalaland.All.AccountManagement.User;
import com.example.luna.lalaland.All.Blockchain.WalletLoginActivity;
import com.example.luna.lalaland.All.Broadcaster.Broadcast;
import com.example.luna.lalaland.All.Broadcaster.BroadcastAdapter;
import com.example.luna.lalaland.All.Broadcaster.ListBroadcastModel;
import com.example.luna.lalaland.All.Broadcaster.StartBroadcastActivity;
import com.example.luna.lalaland.All.BuySongs_Kakaopay.BuySongsActivity;
import com.example.luna.lalaland.All.MusicVideo.WatchMVActivity;
import com.example.luna.lalaland.All.Trending.TrendingActivity;
import com.example.luna.lalaland.All.IdeaNote.NoteHomeActivity;
import com.example.luna.lalaland.All.Utils.ApiClient;
import com.example.luna.lalaland.All.Utils.ApiService;
import com.example.luna.lalaland.All.Utils.ResponseModel;
import com.example.luna.lalaland.All.Viewer.ViewerActivity_;
import com.example.luna.lalaland.All.Viewer.VodActivity;
import com.example.luna.lalaland.R;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
* 2018/9/27
* 로그인 후 진입하는 첫 화면:
* 현재 방송 중이거나 저장된 방송목록을 볼 수 있다
* */

public class HomeScreenActivity extends AppCompatActivity implements BroadcastAdapter.BroadcastAdapterListener, SwipeRefreshLayout.OnRefreshListener, NavigationView.OnNavigationItemSelectedListener{

    private static final String TAG = HomeScreenActivity.class.getSimpleName();
    private static final int REQUEST_BROADCAST_TITLE =1;
    private Toolbar tb_toolBar;
    FloatingActionButton btn_startStreaming;
    String title; //리스트에 넣어줄 방송 제목
    String streamer;
    String genre;
    ImageView iv_liveMark; //목록에 있는 방송이 실시간으로 스트리밍되고 있다는 것을 표시하는 라이브마크

    //임시 --채팅테스트 액티비티로 이동하는 버튼
    Button btn_chatTest;


    //당겨서 새로고침 구현 위한 레이아웃 객체
    SwipeRefreshLayout mSwipeRefreshLayout;


    //방송목록을 보여줄 리사이클러뷰
    List<Broadcast> broadcastList;
    private RecyclerView recyclerview_home; //리사이클러뷰
    BroadcastAdapter broadcastAdapter;

    //내비게이션 뷰
    DrawerLayout drawer;
    NavigationView navigationView;
    ActionBarDrawerToggle toggle;
    ImageView iv_navImg;
    TextView tv_navHeader;

    int roomId; //현재 스트리밍 중인 여러 개의 방송을 식별할 수 있는 아이디
    Boolean isLive; //방송 목록에서 유저가 클릭한 방송이 현재 라이브 스트리밍 중인지 vod방송인지 구별하기 위함
    int index; //방송을 최신순으로 정렬하는 데 기준이 되는 각 방송의 인덱스

    /*로그인한 유저 정보를 담고 있는 sharedPreference와 유저 정보를 담을 변수*/
    SharedPreferences pref;
    String email, username, age, gender;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_screen);

        //로그인 여부를 판별, 로그인 안 되어있으면 앱을 이용할 수 없다
        if(!SharedPrefManager.getmInstance(this).isLoggedIn()) {
            Log.d(TAG, "로그인 되어 있지 않음");
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(this, SignInActivity.class));
        }

        Log.d(TAG, "로그인 되어 있음");
        final User user = SharedPrefManager.getmInstance(this).getUser(); // 현재 로그인 된 유저정보

        /*유저 정보 담기*/
        username = user.getUsername(); //유저네임
        email = user.getEmail();
        gender = user.getGender();
        age = user.getAge();

        /*스트리머 변수에 현재 로그인한 유저네임 넣어주기*/
        streamer = username;


        //뷰 초기화
        tb_toolBar = findViewById(R.id.tb_toolBar);
        btn_startStreaming = findViewById(R.id.btn_startStreaming);
        recyclerview_home = findViewById(R.id.recyclerview_home);
        mSwipeRefreshLayout = findViewById(R.id.swipe_layout);


        //툴바 커스텀
        setSupportActionBar(tb_toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //이걸 추가해주지 않으면 툴바에 앱명 lalaland가 나옴
        tb_toolBar.setTitle("HOME");



        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        toggle = new ActionBarDrawerToggle(
                this, drawer, tb_toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nav_header = navigationView.getHeaderView(0); //네비게이션 헤더 뷰
        tv_navHeader = nav_header.findViewById(R.id.tv_navHeader);
        tv_navHeader.setText(username);


        //임시
        btn_chatTest = findViewById(R.id.btn_chatTest);

        //당겨서 새로고침 구현 위한 레이아웃 변수
        SwipeRefreshLayout mSwipeRefreshLayout;
        //당겨서 새로고침!
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources( //새로고침 아이콘 알록달록 꾸며주기 ㅋㅋㅋ
                R.color.myNavy
        );


        //리사이클러뷰
        broadcastList = new ArrayList<>(); //방송목록리스트 초기화
        recyclerview_home.setItemAnimator(new DefaultItemAnimator());
        recyclerview_home.setLayoutManager(new LinearLayoutManager(this));
        broadcastAdapter = new BroadcastAdapter(this, broadcastList, this); //리사이클러뷰 어댑터
        //broadcastAdapter= new BroadcastAdapter(this, R.layout.broadcast_list, broadcastList); //굳이 이렇게 안 해도 되지만 참고위해 남김
        recyclerview_home.setAdapter(broadcastAdapter); //어댑터를 리사이클러뷰에 세팅


        //클릭 리스너 세팅
        btn_startStreaming.setOnClickListener(mClickListener);
        //임시
        btn_chatTest.setOnClickListener(mClickListener);

        //방송 목록 불러오기
        loadBroadcast();


    }//onCreate

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_homeScreen:

                break;


            case R.id.menu_buyMusic:
                startActivity(new Intent(HomeScreenActivity.this, BuySongsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_musicVideo:
                startActivity(new Intent(HomeScreenActivity.this, WatchMVActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_memo:
                startActivity(new Intent(HomeScreenActivity.this, NoteHomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_lunToken:
                startActivity(new Intent(HomeScreenActivity.this, WalletLoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_chart:
                startActivity(new Intent(HomeScreenActivity.this, TrendingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
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



    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.btn_startStreaming:
                    //스트리밍 시작 버튼을 누르면 방송명을 입력하는 액티비티로 이동한다
                    Intent intent = new Intent(HomeScreenActivity.this, StartBroadcastActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivityForResult(intent, REQUEST_BROADCAST_TITLE);
                    btn_startStreaming.setVisibility(View.GONE);
                    break;

                    //임시
                case R.id.btn_chatTest:
                    Toast.makeText(HomeScreenActivity.this, "클릭", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(HomeScreenActivity.this, ChatTestActivity.class));
                    break;
            }//switch
        }//onClick
    };//mClickListener

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if(requestCode == REQUEST_BROADCAST_TITLE) {
                if(resultCode == RESULT_OK) {
                    //방금 생성된 방제 StartBroadcastActivity로부터 받아오기 --테스트용
                    title = data.getStringExtra("result");
                    //liveCode = data.getStringExtra("liveCode");
                    Log.d(TAG, "홈/제목: "+title);


                    Toast.makeText(this, ""+title, Toast.LENGTH_SHORT).show();

                }
            }//REQUEST_BROADCAST_TITLE

    }//onActivityResult



    //DB에서 현재 스트리밍 중인 방송 정보를 가져옴
    private void loadBroadcast() {

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ListBroadcastModel> call = apiService.getBroadcastInfo("temporary");
        call.enqueue(new Callback<ListBroadcastModel>() {
            @Override
            public void onResponse(Call<ListBroadcastModel> call, Response<ListBroadcastModel> response) {
                ListBroadcastModel listBroadcastModel = response.body();

                if(listBroadcastModel.getStatus() == 1 ){
                    Log.d(TAG, "라이브 방송 목록 불러오기 성공");

                    broadcastList = listBroadcastModel.getBroadcastList();
                    broadcastAdapter.setBroadcastList(getApplicationContext(), broadcastList);
                    recyclerview_home.setAdapter(broadcastAdapter);

                    //인덱스를 기준으로 최신순 정렬
                    Comparator<Broadcast> latestOnTop = new Comparator<Broadcast>() {
                        @Override
                        public int compare(Broadcast o1, Broadcast o2) {
                            return (o2.getIndex()-o1.getIndex());
                        }//compare
                    };//Comparator
                    Collections.sort(broadcastList, latestOnTop);
                } else  {
                    Log.d(TAG, "라이브 방송 목록 불러오기 절반의 성공: "+listBroadcastModel.getMessage());
                }

            }//onResponse

            @Override
            public void onFailure(Call<ListBroadcastModel> call, Throwable t) {
                Log.d(TAG, "라이브 방송 목록 불러오기 실패: "+t.getMessage());
            }//onFailure
        });//Callback

    }//loadBroadcast


    //선택된 목록 정보 가져오는 인터페이스
    @Override
    public void onBroadcastSelected(Broadcast broadcast) {
        Toast.makeText(this, broadcast.getTitle()+"(을)를 시청합니다.", Toast.LENGTH_SHORT).show();
        roomId = broadcast.getRoomId();
        isLive = broadcast.getIsLive();
        index = broadcast.getIndex();
        genre = broadcast.getGenre();
        title = broadcast.getTitle();

        Log.d(TAG, "시청 정보 보냄");
        sendStats(roomId, genre, title, username, email, gender, age);
        Log.d(TAG, "보내는 정보: "+roomId+genre+title+username+email+gender+age);


        String REQUEST;
        //해당 방송이 라이브 방송인지 이미 끝난 방송인지 확인한 후 각 상황에 맞게 처리한다다
        if(isLive) { //라이브 중일 때

            //시청하고자 하는 방송의 룸아이디를 ViewerActivity에서 받아 미디어 서버로 전송한다
            //따라서 ViewerActivity로 현재 클릭한 방송의 룸아이디를 보낸다
            Intent intent = new Intent(HomeScreenActivity.this, ViewerActivity_.class);
            intent.putExtra("roomId", roomId);
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            //라이브 방송 시청자수(viewers)를 +1 시킨다
            REQUEST = "plusViewers";

        } else {
            //이미 끝난 방송 -> 저장된 방송을 서버에서 불러온 후 플레이어로 재생시킨다
            //서버에 요청할 영상의 roomId를 보낸다
            Intent intent = new Intent(HomeScreenActivity.this, VodActivity.class);
            intent.putExtra("roomId", roomId);
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            REQUEST = "plusViews";
            //저장된 동영상의 조회수(views)를 +1 시킨다
        }

        Log.d(TAG, "지금 클릭한 방송 룸의 아이디:"+roomId+" 방송 중인가? "+isLive + "인덱스: "+index+ "서버로의 요청: "+REQUEST);
        checkHowPopularItIs(roomId, REQUEST);
    }//onBroadcastSelected

    /*
    * 추천 동영상을 보여주기 위해 유저가 시청하는 방송의 구분번호, 장르, 제목과
    * 유저 정보 -유저 네임, 이메일, 성별, 나이를 redis에 저장할 것
    * */
    private void sendStats(int roomId, String genre, String title, String username, String email, String gender, String age){
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseModel> call = apiService.sendStats(roomId, genre, title, username, email, gender, age);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                ResponseModel responseModel = response.body();
                String msgFromServer = responseModel.getMessage();
                if(msgFromServer!=null)
                Log.d(TAG, "유저/시청정보 서버 전송 후 서버로부터의 메시지: "+msgFromServer);
                if(responseModel.getStatus() == 1) {
                    Log.d(TAG, "서버로 유저정보 및 시청정보 제대로 전송");
                } else {
                    Log.d(TAG, "유저/시청정보 서버 전송 오류");
                }
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.d(TAG, "유저/시청정보 서버 전송 오류 실패" +t.getMessage());
            }//onFailure
        });
    }//sendStats

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
                Log.d(TAG, "방송 조회수/ 시청자수 설정 후 서버에서 보낸 메시지: "+msgFromServer);

                if(responseModel.getStatus() == 1) {
                    Log.d(TAG, "조회수/ 시청자수 설정한 방송의 룸아이디: "+roomId);
                } else {
                    Log.d(TAG, "방송 조회수/ 시청자수 설정 오류");
                }
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.d(TAG, "조회수/ 시청자수 설정 후 추가 서버메시지" +t.getMessage());
            }//onFailure
        });//Callback

    }//checkHowPopularItIs

    @Override //뒤로가기 버튼
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRefresh() { //유저가 리스트를 끝까지 당겼다가 놓으면 호출되는 메소드
        loadBroadcast();
        mSwipeRefreshLayout.setRefreshing(false); //새로고침 완료
    }


    //액티비티 종료 시
    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadBroadcast(); //액티비티 resume 시 방송목록 갱신
        Log.d(TAG, "제목1: "+title);
        btn_startStreaming.setVisibility(View.VISIBLE);
    }

}//HomeScreenActivity
