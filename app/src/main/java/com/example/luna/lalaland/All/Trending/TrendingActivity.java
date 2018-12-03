package com.example.luna.lalaland.All.Trending;

import android.content.Intent;
import android.support.annotation.NonNull;
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
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.anychart.charts.TagCloud;
import com.anychart.scales.OrdinalColor;
import com.example.luna.lalaland.All.AccountManagement.SharedPrefManager;
import com.example.luna.lalaland.All.AccountManagement.SignInActivity;
import com.example.luna.lalaland.All.AccountManagement.User;
import com.example.luna.lalaland.All.Blockchain.WalletLoginActivity;
import com.example.luna.lalaland.All.Broadcaster.Broadcast;
import com.example.luna.lalaland.All.Broadcaster.ListBroadcastModel;
import com.example.luna.lalaland.All.BuySongs_Kakaopay.BuySongsActivity;
import com.example.luna.lalaland.All.IdeaNote.NoteHomeActivity;
import com.example.luna.lalaland.All.Intro.HomeScreenActivity;
import com.example.luna.lalaland.All.MusicVideo.WatchMVActivity;
import com.example.luna.lalaland.All.Utils.ApiClient;
import com.example.luna.lalaland.All.Utils.ApiService;
import com.example.luna.lalaland.All.Utils.ResponseModel;
import com.example.luna.lalaland.All.Viewer.ViewerActivity_;
import com.example.luna.lalaland.All.Viewer.VodActivity;
import com.example.luna.lalaland.R;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
* 유저의 로그데이터를 수집한 후 수집된 정보를 차트로 시각화하여 보여주는 액티비티
* -비슷한 나이+같은 성별의 유저들에게 인기 있는 장르를 차트로 구현
* -가장 인기있는 장르 리스트를 리스트로 구현
*
* */
public class TrendingActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener,
        GenreAdapter.GenreAdapterListener,
        SwipeRefreshLayout.OnRefreshListener{

    private static final String TAG = "트렌딩";

    //내비게이션 뷰 + 툴바
    @BindView(R.id.nav_view)
    NavigationView navigationView;
    @BindView(R.id.tb_toolBar)
    Toolbar tb_toolBar;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;

    //기타 뷰
    @BindView(R.id.tv_intro1)
    TextView tv_intro1;
    @BindView(R.id.tv_intro2)
    TextView tv_intro2;

    //차트 뷰 (https://github.com/AnyChart/AnyChart-Android)
    @BindView(R.id.any_chart_view)
    AnyChartView anyChartView;
    @BindView(R.id.progress_bar)
    ProgressBar progressBar;
    @BindView(R.id.iv_refresh)
    ImageView refreshChart; //차트 새로고침
    Pie pie;
    List<DataEntry> data;
    TagCloud tagCloud;

    //리사이클러뷰
    @BindView(R.id.recyclerview_best)
    RecyclerView recyclerview_best;
    @BindView(R.id.swipe_layout)
    SwipeRefreshLayout swipe_layout;
    //방송목록을 보여줄 리사이클러뷰
    List<Broadcast> broadcastList;
    GenreAdapter genreAdapter;
    private StaggeredGridLayoutManager staggeredGridLayoutManager;

    SwipeRefreshLayout mSwipeRefreshLayout; //당겨서 새로고침

    //유저 정보
    String username, email, gender, age;
    List<Genre> genreList; //가능한 장르 선택지

    /*선택 가능한 장르들*/
    String Jazz = "Jazz";
    String Electronic = "Electronic";
    String Dance = "Dance";
    String HipHop = "Hip-Hop";
    String POP = "POP";
    String KPOP = "K-POP";
    String RNB = "R&B";
    String Latin = "Latin";
    String Country = "Country";
    String CCM = "CCM";
    String Classic = "Classic";

    int countJazz, countElectronic, countDance, countHipHop, countPOP, countKPOP, countRNB, countLatin, countCountry, countCCM, countClassic;
    Genre rnb, classic, dance, ccm, hiphop, kpop, jazz, latin, pop, electronic, country; //각 장르의 클래스 객체

    /*가장 인기있는 장르*/
    String topGenre;

    int ageInt;
    String ages;
    String genderInKorean;

    /*방송 시청관련 변수*/
    int roomId, index;
    Boolean isLive;
    String title;
    String genre;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trending);

        ButterKnife.bind(this);

        /*장르 리스트*/
        genreList = new ArrayList<>();

        /*
        * 유저의 로그인 여부를 확인
        * */
        if(!SharedPrefManager.getmInstance(this).isLoggedIn()) {
            Log.d(TAG, "로그인 되어 있지 않음");
            Toast.makeText(this, "로그인 후 이용해주세요.", Toast.LENGTH_SHORT).show();
            finish();
            startActivity(new Intent(this, SignInActivity.class));
        }

        Log.d(TAG, "로그인 되어 있음");
        /*현재 로그인된 유저 정보 가져오기*/
        final User user = SharedPrefManager.getmInstance(this).getUser();
        username = user.getUsername();
        Log.d(TAG, "유저네임: "+username);
        email = user.getEmail();
        age = user.getAge();
        /*string값인 나이를 int값으로 변경*/
        ageInt = Integer.valueOf(age);
        gender = user.getGender();
        Log.d(TAG, "유저 나이 /성별: "+age+gender);


        /*툴바, 내비게이션 뷰 등 기본적인 레이아웃 객체 초기화 및 커스텀*/
        defaultViews();


        /*유저 정보를 카테고리화 함*/
        if(gender.equals("F"))
            genderInKorean = "여성";
        if(gender.equals("M"))
            genderInKorean = "남성";

        if(ageInt >= 10 &&  ageInt < 20) {
            ages = "10대";
        }
        if(ageInt >= 20 &&  ageInt < 30) {
            ages = "20대";
        }
        if(ageInt >= 30 &&  ageInt < 40) {
            ages = "30대";
        }
        if(ageInt >= 40 &&  ageInt < 50) {
            ages = "40대";
        }
        if(ageInt >= 50 &&  ageInt < 60) {
            ages = "50대";
        }


        Log.d(TAG, "유저 정보를 웹서버로 보냄");
        getStats(gender, age);

        /*태그차트 제목*/
        tv_intro1.setText(ages+" "+genderInKorean+"이 즐겨 보는 장르");
        /*가장 인기있는 장르*/
        tv_intro2.setText(ages+" "+genderInKorean+"'s Pick");

        /*차트 세팅*/
        anyChartView.setProgressBar(progressBar);

        /*각 성별+나이 그룹에서 장르 인기도를 태그 차트로 한눈에 알아볼 수 있도록 구현*/
        tagCloud = AnyChart.tagCloud();
        //tagCloud.title("인기 장르 순위");

        OrdinalColor ordinalColor = OrdinalColor.instantiate();
        ordinalColor.colors(new String[] {
                "#26959f", "#f18126", "#3b8ad8", "#60727b", "#e24b26"
        });
        tagCloud.colorScale(ordinalColor);
        tagCloud.angles(new Double[] {-90d, 0d, 90d});

        tagCloud.colorRange().enabled(true);
        tagCloud.colorRange().colorLineSize(10d);
        data = new ArrayList<>();


        /*원래 태그차트 대신 파이 차트로 장르별 인기도를 시각화하려했으나
        * 태그차트가 더 디자인적으로 예쁜 것 같아서 수정. 다만 추후에 또 파이차트로 되돌아갈 수 있으므로 코드 삭제 보류*/
/*        pie = AnyChart.pie(); //파이 차트

        pie.setOnClickListener(new ListenersInterface.OnClickListener() {
            @Override
            public void onClick(Event event) {
                Toast.makeText(TrendingActivity.this, event.getData().get("x") + ":" + event.getData().get("value"), Toast.LENGTH_SHORT).show();
            }//onClick
        });//ListenersInterface*/



    }//onCreate


    /*redis서버에서 유저 정보 나이, 성별을 토대로 추천 장르를 가져온 후
    추천 장르의 컨텐츠를 조회수 순으로 5개씩 보여준다
     ==> 우선 인기 장르 5개를 추린 후
    인기 장르 3개를 꼽아 가장 인기가 많은 컨텐츠를 5개씩 보여주는 것으로 한다 */
    private void getStats(String gender, String age) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ListStatsModel> call = apiService.getStats(gender, age);
        call.enqueue(new Callback<ListStatsModel>() {
            @Override
            public void onResponse(Call<ListStatsModel> call, Response<ListStatsModel> response) {

                ListStatsModel listStatsModel = response.body();

                if(listStatsModel.getStatus() == 1) {
                    Log.d(TAG, "유저 나이+성별 그룹에서 시청된 장르 불러오기 성공");

                    topGenre = listStatsModel.getTop_genre();
                    Log.d(TAG, "가장 인기있는 장르: "+topGenre);
                    Log.d(TAG, "가장 인기있는 장르 불러오기");
                    getTopGenreList(topGenre);


                    String genreStr = listStatsModel.getGenre();
                    Log.d(TAG, "모든 장르와 카운트: "+genreStr);
                    /*cf. genreStr = {"R&B":145,"Electronic":90,"Jazz":50,"Latin":40,"Dance":110,"Hip-Hop":130,"POP":140,"K-POP":150}*/

                    JsonParser jsonParser = new JsonParser();
                    JsonObject jsonObject = (JsonObject) jsonParser.parse(genreStr);


                    /*
                    * 나이+성별 그룹이 감상한 장르를 카운트와 함께 가져온다.
                    * cf.
                    * int count는 사용자가 해당 장르에 속하는 영상을 클릭할 때마다 증가함
                    *
                    * 장르는 각각 다른 이름을 가지고 있기 때문에 일일이 꺼내줘야 한다.
                    * jsonobject에서 장르명을 기준으로 카운트를 꺼내와서 String 장르명, int 카운트를 가진
                    * 장르 객체를 생성해준다.
                    *
                    * */
                    if(jsonObject.get(Jazz)!= null) {
                        countJazz = jsonObject.get(Jazz).getAsInt();
                        jazz = new Genre(Jazz, countJazz);
                        Log.d(TAG, "재즈: "+jazz.getName()+jazz.count);
                        genreList.add(jazz);

                        /*차트 데이터리스트에 장르 클래스 객체 추가*/
                        data.add(new ValueDataEntry(jazz.name, jazz.count));
                    }

                    if(jsonObject.get(KPOP)!= null) {
                        countKPOP = jsonObject.get(KPOP).getAsInt();
                        kpop = new Genre(KPOP, countKPOP);
                        Log.d(TAG, "케이팝: "+kpop.getName()+kpop.count);
                        genreList.add(kpop);

                        data.add(new ValueDataEntry(kpop.name, kpop.count));
                    }


                    if(jsonObject.get(HipHop)!= null) {
                        countHipHop = jsonObject.get(HipHop).getAsInt();
                        hiphop = new Genre(HipHop, countHipHop);
                        genreList.add(hiphop);
                        Log.d(TAG, "힙합: "+hiphop.getName()+hiphop.count);

                        data.add(new ValueDataEntry(hiphop.name, hiphop.count));
                    }


                    if(jsonObject.get(Dance)!= null) {
                        countDance = jsonObject.get(Dance).getAsInt();
                        dance = new Genre(Dance, countDance);
                        genreList.add(dance);
                        Log.d(TAG, "댄스: "+dance.getName()+dance.count);

                        data.add(new ValueDataEntry(dance.name, dance.count));
                    }


                    if(jsonObject.get(POP)!= null) {
                        countPOP = jsonObject.get(POP).getAsInt();
                        pop = new Genre(POP, countPOP);
                        genreList.add(pop);
                        Log.d(TAG, "팝: "+pop.getName()+pop.count);

                        data.add(new ValueDataEntry(pop.name, pop.count));
                    }

                    if(jsonObject.get(Electronic)!= null) {
                        countElectronic = jsonObject.get(Electronic).getAsInt();
                        electronic = new Genre(Electronic, countElectronic);
                        genreList.add(electronic);
                        Log.d(TAG, "일렉: "+electronic.getName()+electronic.count);

                        data.add(new ValueDataEntry(electronic.name, electronic.count));
                    }


                    if(jsonObject.get(CCM) != null) {
                        countCCM = jsonObject.get(CCM).getAsInt();
                        ccm = new Genre(CCM, countCCM);
                        genreList.add(ccm);
                        Log.d(TAG, "ccm: "+ccm.getName()+ccm.count);

                        data.add(new ValueDataEntry(ccm.name, ccm.count));
                    }


                    if(jsonObject.get(Country)!= null) {
                        countCountry = jsonObject.get(Country).getAsInt();
                        country = new Genre(Country, countCountry);
                        genreList.add(country);
                        Log.d(TAG, "country: "+country.getName()+country.count);

                        data.add(new ValueDataEntry(country.name, country.count));
                    }


                    if(jsonObject.get(Latin)!= null) {
                        countLatin = jsonObject.get(Latin).getAsInt();
                        latin = new Genre(Latin, countJazz);
                        genreList.add(latin);
                        Log.d(TAG, "latin: "+latin.getName()+latin.count);

                        data.add(new ValueDataEntry(latin.name, latin.count));
                    }


                    if(jsonObject.get(RNB)!= null) {
                        countRNB = jsonObject.get(RNB).getAsInt();
                        rnb = new Genre(RNB, countRNB);
                        genreList.add(rnb);
                        Log.d(TAG, "rnb: "+rnb.getName()+rnb.count);

                        data.add(new ValueDataEntry(rnb.name, rnb.count));
                    }


                    if(jsonObject.get(Classic)!= null) {
                        countClassic = jsonObject.get(Classic).getAsInt();
                        classic = new Genre(Classic, countClassic);
                        genreList.add(classic);
                        Log.d(TAG, "classic: "+classic.getName()+classic.count);

                        data.add(new ValueDataEntry(classic.name, classic.count));
                    }

                    /*태그 차트*/
                    Log.d(TAG, "차트 로딩");
                    tagCloud.data(data);
                    anyChartView.setChart(tagCloud);

                    /*파이 차트 구현했었음. 추후 다시 파이차트로 구현할 수도 있으므로 코드는 지우지 않음*/
/*                    pie.data(data);
                    pie.title("Fruits imported in 2015 (in kg)");

                    pie.labels().position("outside");

                    pie.legend().title().enabled(true);
                    pie.legend().title()
                            .text("Retail channels")
                            .padding(0d, 0d, 10d, 0d);

                    pie.legend()
                            .position("center-bottom")
                            .itemsLayout(LegendLayout.HORIZONTAL)
                            .align(Align.CENTER);

                    Log.d(TAG, "차트");
                    anyChartView.setChart(pie);*/



                } else {
                    Log.d(TAG, "장르 불러오기 오류");
                }

            }//onResponse

            @Override
            public void onFailure(Call<ListStatsModel> call, Throwable t) {
                Log.d(TAG, "장르 불러오기 실패");
                Log.d(TAG,"실패 메시지: "+t.getMessage());
            }//onFailure
        });//Callback
    }//getStats

    /*가장 인기있는 장르의 컨텐츠를 가져온다*/
    private void getTopGenreList(String topGenre) {

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ListBroadcastModel> call = apiService.getTopGenre(topGenre);
        call.enqueue(new Callback<ListBroadcastModel>() {
            @Override
            public void onResponse(Call<ListBroadcastModel> call, Response<ListBroadcastModel> response) {
                ListBroadcastModel listBroadcastModel = response.body();

                if(listBroadcastModel.getStatus() == 1 ){
                    Log.d(TAG, "방송 목록 불러오기 성공");

                    broadcastList = listBroadcastModel.getBroadcastList();
                    genreAdapter.setBroadcastList(getApplicationContext(), broadcastList);
                    recyclerview_best.setAdapter(genreAdapter);

                    //조회수 기준 정렬
                    Comparator<Broadcast> bestOnTop = new Comparator<Broadcast>() {
                        @Override
                        public int compare(Broadcast o1, Broadcast o2) {
                            return (o2.getViews()-o1.getViews());
                        }//compare
                    };//Comparator
                    Collections.sort(broadcastList, bestOnTop);
                } else  {
                    Log.d(TAG, "방송 목록 불러오기 절반의 성공: "+listBroadcastModel.getMessage());
                }

            }//onResponse

            @Override
            public void onFailure(Call<ListBroadcastModel> call, Throwable t) {
                Log.d(TAG, "방송 목록 불러오기 실패: "+t.getMessage());
            }//onFailure
        });//Callback
    }//getTopGenreList


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
        Log.d(TAG, "보내는 정보: "+roomId+ genre +title+username+email+gender+age);


        String REQUEST;
        //해당 방송이 라이브 방송인지 이미 끝난 방송인지 확인한 후 각 상황에 맞게 처리한다다
        if(isLive) { //라이브 중일 때

            //시청하고자 하는 방송의 룸아이디를 ViewerActivity에서 받아 미디어 서버로 전송한다
            //따라서 ViewerActivity로 현재 클릭한 방송의 룸아이디를 보낸다
            Intent intent = new Intent(TrendingActivity.this, ViewerActivity_.class);
            intent.putExtra("roomId", roomId);
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            //라이브 방송 시청자수(viewers)를 +1 시킨다
            REQUEST = "plusViewers";

        } else {
            //이미 끝난 방송 -> 저장된 방송을 서버에서 불러온 후 플레이어로 재생시킨다
            //서버에 요청할 영상의 roomId를 보낸다
            Intent intent = new Intent(TrendingActivity.this, VodActivity.class);
            intent.putExtra("roomId", roomId);
            startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
            REQUEST = "plusViews";
            //저장된 동영상의 조회수(views)를 +1 시킨다
        }

        Log.d(TAG, "지금 클릭한 방송 룸의 아이디:"+roomId+" 방송 중인가? "+isLive + "인덱스: "+index+ "서버로의 요청: "+REQUEST);
        checkHowPopularItIs(roomId, REQUEST);
    }

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


    @Override
    public void onRefresh() { //유저가 리스트를 끝까지 당겼다가 놓으면 호출되는 메소드
        getTopGenreList(topGenre);
        mSwipeRefreshLayout.setRefreshing(false); //새로고침 완료
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_homeScreen:
                startActivity(new Intent(TrendingActivity.this, HomeScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;



            case R.id.menu_buyMusic:
                startActivity(new Intent(TrendingActivity.this, BuySongsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_musicVideo:
                startActivity(new Intent(TrendingActivity.this, WatchMVActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_lunToken:
                startActivity(new Intent(TrendingActivity.this, WalletLoginActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_memo:
                startActivity(new Intent(TrendingActivity.this, NoteHomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_chart:
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


    /*툴바, 내비게이션 뷰 등 기본적인 레이아웃 객체 초기화 및 커스텀*/
    private void defaultViews(){
        //툴바 커스텀
        setSupportActionBar(tb_toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //이걸 추가해주지 않으면 툴바에 앱명 lalaland가 나옴
        tb_toolBar.setTitle("Trending");

        //내비게이션 뷰
        toggle = new ActionBarDrawerToggle(
                this, drawer, tb_toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);
        View nav_header = navigationView.getHeaderView(0); //네비게이션 헤더 뷰
        TextView tv_navHeader = nav_header.findViewById(R.id.tv_navHeader);
        tv_navHeader.setText(username);

        //당겨서 새로고침 구현 위한 레이아웃 변수
        //당겨서 새로고침!
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
        mSwipeRefreshLayout.setColorSchemeResources( //새로고침 아이콘 알록달록 꾸며주기 ㅋㅋㅋ
                R.color.myNavy
        );

          /*가장 인기있는 장르의 컨텐츠를 보여줄 리사이클러뷰 세팅*/
        //리사이클러뷰
        broadcastList = new ArrayList<>(); //방송목록리스트 초기화
        recyclerview_best.setItemAnimator(new DefaultItemAnimator());
       // staggeredGridLayoutManager = new StaggeredGridLayoutManager(2, 1); //리스트가 1행 2열로 배치되게 만들어줌
       // recyclerview_best.setLayoutManager(staggeredGridLayoutManager);
        LinearLayoutManager layoutManager
                = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        recyclerview_best.setLayoutManager(layoutManager);
        genreAdapter = new GenreAdapter(this, broadcastList, this);//리사이클러뷰 어댑터
        //broadcastAdapter= new BroadcastAdapter(this, R.layout.broadcast_list, broadcastList); //굳이 이렇게 안 해도 되지만 참고위해 남김
        recyclerview_best.setAdapter(genreAdapter); //어댑터를 리사이클러뷰에 세팅
    }

    @Override //뒤로가기 버튼
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

}//TrendingActivity
