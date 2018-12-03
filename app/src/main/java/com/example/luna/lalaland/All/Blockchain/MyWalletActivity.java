package com.example.luna.lalaland.All.Blockchain;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.luna.lalaland.All.AccountManagement.SharedPrefManager;
import com.example.luna.lalaland.All.AccountManagement.SignInActivity;
import com.example.luna.lalaland.All.AccountManagement.User;
import com.example.luna.lalaland.All.BuySongs_Kakaopay.BuySongsActivity;
import com.example.luna.lalaland.All.MusicVideo.WatchMVActivity;
import com.example.luna.lalaland.All.Trending.TrendingActivity;
import com.example.luna.lalaland.All.IdeaNote.NoteHomeActivity;
import com.example.luna.lalaland.All.Intro.HomeScreenActivity;
import com.example.luna.lalaland.R;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;

import org.json.JSONArray;
import org.json.JSONObject;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;

import java.io.IOException;
import java.math.BigInteger;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/*
* '루나코인' 메뉴의 홈 화면
* 현재 내가 가진 루나코인의 잔액, 이체 내역(송금/입금 현황)을 확인할 수 있다.
* 지갑 주소(QR코드 스캔 가능)를 통해 다른 유저에게 코인을 이체할 수도 있다.
*
* */
public class MyWalletActivity extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, TransactionHistoryAdapter.TxAdapterListner{

    private final static String TAG = "블록체인";
    private final static int sendToken = 1;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;
    @BindView(R.id.tb_toolBar)
    android.support.v7.widget.Toolbar tb_toolBar;
    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.tv_lut)
    TextView tv_lut;
    @BindView(R.id.tv_amount)
    TextView tv_amount; //루나토큰 잔액
    @BindView(R.id.iv_refresh)
    ImageView iv_refresh; //새로고침 버튼
    @BindView(R.id.tv_address)
    TextView tv_address; //지갑 주소
    @BindView(R.id.recyclerview_lunToken)
    RecyclerView recyclerview_lunToken;
    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    Bitmap QRCode; //QRCode를 담을 비트맵
    private Intent intent;
    private String address; //현재 유저의 지갑 주소
    private String password;
    private String addressToSend; //토큰을 송금받을 유저의 지갑 주소

    private Wallet wallet;
    private Credentials credentials;
    private Web3j web3j;

    private BigInteger balance; //토큰 잔액
    private BigInteger decimals; //토큰 단위
    private BigInteger amountToSend; //송금할 토큰의 액수
    private String infuraEndNode = "https://ropsten.infura.io/v3/9b6fb40f30bf480eb9299b103561cf53";
    private String fileName; //key 파일명

    //트랜잭션 정보 담을 변수
    private String fromAddress, toAddress, log;
    private BigInteger value;


    /*이더스캔 서버에서 트랜잭션 히스토리 받아오기 위해
    * okhttp3 이용*/
    private OkHttpClient okHttpClient;
    private String contractAddress;
    private String ApiKey = "KQEZFBDSHZXW1UD19F18BF1GEDI2M3VB7E";
    private String etherscanApiUrl;
    private String okHttpResponse, txHistory, status, time;
    private List<TransactionHistory> txList;
    private long timestamp;
    private Date date;
    JSONObject jsonObject;
    TransactionHistoryAdapter txAdapter;

    /*로그인한 유저 정보를 담고 있는 sharedPreference와 유저 정보를 담을 변수*/
    SharedPreferences pref;
    String email, username, age, gender;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_wallet);

        //버터나이프로 레이아웃 객체 초기화
        ButterKnife.bind(this);


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

        //토큰 단위 써주기
        tv_lut.setText("LUTs");

        //툴바 커스텀
        setSupportActionBar(tb_toolBar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); //이걸 추가해주지 않으면 툴바에 앱명 lalaland가 나옴
        tb_toolBar.setTitle("My Wallet");

        //내비게이션 뷰
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, tb_toolBar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View nav_header = navigationView.getHeaderView(0); //네비게이션 헤더 뷰
        TextView tv_navHeader = nav_header.findViewById(R.id.tv_navHeader);
        tv_navHeader.setText(username);


        //트랜잭션 정보를 넣어줄 리스트 초기화
        txList = new ArrayList<>();


        //리사이클러뷰
        recyclerview_lunToken.setItemAnimator(new DefaultItemAnimator());
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setReverseLayout(true);
        mLayoutManager.setStackFromEnd(true);
        recyclerview_lunToken.setLayoutManager(mLayoutManager);
        txAdapter = new TransactionHistoryAdapter(this, txList, this);
        recyclerview_lunToken.setAdapter(txAdapter);

        //지갑 객체 생성
        wallet = new Wallet();

        //인텐트로 지갑생성/로그인 액티비티에서 필요한 정보 받아오기
        Intent intent = getIntent();
        address = intent.getStringExtra("address");
        password = intent.getStringExtra("password");
        fileName = intent.getStringExtra("fileName");
        Log.d(TAG, "정보 잘 받아왔나: "+address);
        Log.d(TAG, "직접 가져온 주소: "+ wallet.getWalletAddress());
        Log.d(TAG, "파일명: "+fileName);

        //유저 지갑 주소를 받아와서 QR코드로 변환
        createQRCode(address);
        tv_address.setText(address);

        //thread policy를 strict mode로 설정
        final StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);



        try {
            /** Web3j 연결 */
            Wallet.web3Connection();
            Log.d(TAG, "노드와 연결 잘 됨");
        }catch (IOException e){
            Log.d(TAG, "커넥션 에러: "+e.getMessage());
            finish();
        }

         /*트랜잭션 내역을 가져오기 위한 okhttp3 객체*/
        okHttpClient = new OkHttpClient();
        Log.d(TAG, "트랜잭션 히스토리 async 호출");


        //토큰 잔액 가져오기
        new LongOperation().execute("getTokenBalance");
        Log.d(TAG, "잔액 가져오기 async 시작");

        //트랜잭션 정보 가져오기
        new LongOperation().execute("getTxHistory");
        Log.d(TAG, "트랜잭션 정보 가져오기 async 시작");


        //지갑 정보 새로고침
        iv_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new LongOperation().execute("getTokenBalance");
                Log.d(TAG, "잔액 정보 새로고침");
                new LongOperation().execute("getTxHistory");
                Log.d(TAG, "거래내역 새로고침");
            }//onClick
        });//setOnClickListener

    }//onCreate

    @Override
    public void onTxSelected(TransactionHistory transactionHistory) {
        //트랜잭션 내역 중 하나를 클릭했을 때
        Toast.makeText(this, "송금한 주소: "+transactionHistory.to, Toast.LENGTH_SHORT).show();
    }//onTxSelected

    private class LongOperation extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            Wallet wallet = new Wallet();
            try {
                switch (params[0]) {
                    case "getTokenBalance":
                        Log.d(TAG, "잔액 가져오기 async");
                        Log.d(TAG, "주소는 "+ address);
                        web3j = wallet.constructWeb3(infuraEndNode);
                        credentials = wallet.loadCredentials(password, fileName);
                        Log.d(TAG, "Credentials 로드됨");
                        Contract contract = new Contract(web3j, credentials);
                        Log.d(TAG, "get decimals async");
                        decimals = contract.decimals(); //decimal가져오기
                        Log.d(TAG, "decimals: " + decimals);
                        balance = contract.balanceOf(address); //잔액 가져오기

                        //decimal에 맞게 잔액 포맷팅
                        balance = contract.formatBalance(balance, decimals.intValue()).toBigInteger();

                        if(balance.intValue() <= 1) {
                            tv_lut.setText("LUT");
                        }

                        //컨트랙트 주소 받기
                        contractAddress = contract.contractAddress;
                        Log.d(TAG, "컨트랙트 주소: "+contractAddress);

                        return "getTokenBalance";

                    case "getTxHistory":


                        Request.Builder builder = new Request.Builder();
                        etherscanApiUrl = "https://api-ropsten.etherscan.io/api?" +
                                "module=account&action=tokentx&contractaddress="+contractAddress+"&address="
                                +address+"&page=1&offset=100&sort=asc&apikey="+ApiKey;

                        Log.d(TAG, "트랜잭션 내역 불러오려는 주소: "+etherscanApiUrl);
                        builder.url(etherscanApiUrl);
                        Request request = builder.build();
                        Response response = okHttpClient.newCall(request)
                                .execute();
                        okHttpResponse = response.body().string();
                        Log.d(TAG, "트랜잭션 - okhttp3 메시지: "+okHttpResponse);

                        return "getTxHistory";

                    case "sendToken":
                        Log.d(TAG, "토큰 송금시작");
                        web3j = wallet.constructWeb3(infuraEndNode);
                        credentials = wallet.loadCredentials(password, fileName);
                        Log.d(TAG, "Credentials 로드됨");
                        contract = new Contract(web3j, credentials);
                        Log.d(TAG, "get decimals async");
                        decimals = contract.decimals(); //decimal가져오기
                        Log.d(TAG, "송금 전 잔액: "+ balance);
                        Log.d(TAG, "송금할 금액: "+amountToSend);
                        amountToSend = contract.reformatBalance(amountToSend, decimals.intValue()).toBigInteger();
                        Log.d(TAG, "다시 변환한 송금 금액: "+amountToSend);
                        //contract.transferFrom(address, addressToSend, amountToSend);
                        contract.transfer(addressToSend, amountToSend);
                        Log.d(TAG, "transfer 메소드 호출");
                        return "sendToken";

                    default:
                        return "default";
                }
            } catch (Exception e) {
                e.printStackTrace();
                return e.toString();
            }
        }

        @Override
        protected void onPostExecute(String result) {

            progressBar.setVisibility(View.INVISIBLE);

            try {

                if (result == "getTokenBalance") {
                    tv_amount.setText(String.valueOf(balance));
                    Log.d(TAG, "getTokenBalance 결과: " + result);

                } else if (result == "sendToken") {
                    Toast.makeText(MyWalletActivity.this, "송금이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    //송금이 완료되면 잔액과 트랜잭션 내역을 다시 불러온다
                    new LongOperation().execute("getTokenBalance");
                    new LongOperation().execute("getTxHistory");
                    Log.d(TAG, "잔액/ 트랜잭션 내역 다시 불러오기 시작");

                } else if(result == "getTxHistory") {

                    Contract contract = new Contract(web3j, credentials);

                    jsonObject = new JSONObject(okHttpResponse); //okhttp3를 통해서 이더스캔에서 받아온 트랜잭션 내역
                    status = jsonObject.get("status").toString(); //트랜잭션 내역을 제대로 받아왔을 때 status = 1
                    Log.d(TAG, "status: "+status);

                    if(status.equals("1")){
                        Log.d(TAG, "결과값 상태가 1임");
                        JSONArray jsonArray = jsonObject.getJSONArray("result");
                        Log.d(TAG, "불러온 jsonarray"+jsonArray);

                        //from, to, value, timestamp 값을 불러오면 됨
                        //value값은 format해야됨 (case "getTokenBalance" 참고)
                        for (int i = 0; i < jsonArray.length(); i++) {
                            fromAddress = jsonArray.getJSONObject(i).getString("from");
                            toAddress = jsonArray.getJSONObject(i).getString("to");
                            timestamp = jsonArray.getJSONObject(i).getLong("timeStamp");
                            DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss", Locale.KOREA);
                            date = new Date(timestamp*1000);
                            time = dateFormat.format(date);

                            String valueStr = jsonArray.getJSONObject(i).getString("value");
                            value = new BigInteger(valueStr);
                            value = contract.formatBalance(value, decimals.intValue()).toBigInteger();

                            Log.d(TAG, i+"불러온 jsonarray에서 뽑은 from: "+fromAddress);
                            Log.d(TAG, i+"불러온 jsonarray에서 뽑은 to: "+toAddress);
                            Log.d(TAG, i+"불러온 jsonarray에서 뽑은 value: "+value);
                            Log.d(TAG, i+"불러온 jsonarray에서 뽑은 time: "+time);

                            TransactionHistory transactionHistory = new TransactionHistory(fromAddress, toAddress, value, time);
                            txList.add(transactionHistory);
                            txAdapter.notifyItemInserted(i);

                            //최신 트랜잭션 잭내역이 보이도록 자동 스크롤
                            recyclerview_lunToken.smoothScrollToPosition(txList.size() - 1);
                        }//for



                    } else {
                        Toast.makeText(MyWalletActivity.this, "트랜잭션 내역을 불러오지 못했습니다. ", Toast.LENGTH_SHORT).show();
                    }


                } else {

                    Log.d(TAG, "통신 결과: "+result);
                }


            } catch (Exception e) {
                e.printStackTrace();
            }//catch
        }//onPostExecute

        @Override
        protected void onPreExecute() {
            //서버와 통신하는 과정에서 요청한 결과가 나올 때까지 프로그레스바를 띄운다
            progressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            //서버와 통신하는 과정에서 요청한 결과가 나올 때까지 프로그레스바를 띄운다
            progressBar.setVisibility(View.VISIBLE);
        }
    }





    private Bitmap createQRCode(String address) {

           QRCode= null ;

            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            try {
            /* Encode to utf-8 */
            /*예제에 있던 코드인데 필요없어서 사용은 안 하지만 후에 참고할지도 몰라서 남려둠*/
            /*참고: https://kutar37.tistory.com/entry/Android-QR코드바코드-생성과-스캔-xzing-라이브러리 [저장소]*/
            //    Hashtable hints = new Hashtable();
            //    hints.put(EncodeHintType.CHARACTER_SET, "utf-8");

                BitMatrix bitMatrix = multiFormatWriter.encode(address, BarcodeFormat.QR_CODE,300,300);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                QRCode = barcodeEncoder.createBitmap(bitMatrix);
            } catch (WriterException e) {
                e.printStackTrace();
            }

            return QRCode;

    }//createQRCode

    //툴바에 토큰 송금 버튼과 지갑 QR code를 볼 수 있는 메뉴 삽입
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.lunacoin_menu, menu);
        return true;
    }//onCreateOptionsMenu


    //툴바에 있는 qr code 아이콘을 클릭했을 때 액션 넣기
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //나의 지갑주소를 QR코드로 보여준다
        if(id == R.id.menu_qrCode) {
            //TODO 1) 서버에서 지갑 주소를 받아온 후 2) QR 코드 제너레이터를 통해 QR코드 생성 3) 툴바의 qr코드 버튼을 누르면 투명액티비티나 다이얼로그로 qr코드 띄우기
            //참고 https://medium.com/@aanandshekharroy/generate-barcode-in-android-app-using-zxing-64c076a5d83a
            //일단은 서버에서 받아오지 않고 원래 내 지갑 주소를 하드코딩으로 넣어서 QR 코드를 생성한 상태
            intent = new Intent(MyWalletActivity.this, QRCodeActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION); //QRCodeActivity에서 QR코드를 띄워줌
            intent.putExtra("QRCode", QRCode);
            intent.putExtra("address", address);
            startActivity(intent);
            return true;
        }

        //루나코인 송금 버튼
        if(id == R.id.menu_send) {
            Log.d(TAG, "송금버튼 클릭");
            intent = new Intent(MyWalletActivity.this, SendTokenActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            startActivityForResult(intent, sendToken);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }//onCreateOptionsMenu

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {

        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case  sendToken:
                    addressToSend = data.getStringExtra("addressToSend");
                    amountToSend = BigInteger.valueOf(data.getIntExtra("amountToSend", 0));
                    Log.d(TAG, "송금 액티비티에서 받아온 주소: "+ addressToSend +", 금액: "+ amountToSend);

                    new LongOperation().execute("sendToken");
                    Log.d(TAG, "송금 프로세스 시작");
                    break;
            }//switch
        } else {
         Log.d(TAG, "송금 액티비티에서 resultCode가 not okay");
        }
    }//onActivityResult

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {

            case R.id.menu_homeScreen:
                startActivity(new Intent(MyWalletActivity.this, HomeScreenActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_memo:
                startActivity(new Intent(MyWalletActivity.this, NoteHomeActivity.class)
                        .addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_buyMusic:
                startActivity(new Intent(MyWalletActivity.this, BuySongsActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_musicVideo:
                startActivity(new Intent(MyWalletActivity.this, WatchMVActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                break;

            case R.id.menu_lunToken:
                break;

            case R.id.menu_chart:
                startActivity(new Intent(MyWalletActivity.this, TrendingActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
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

    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }
}//MyWalletActivity
