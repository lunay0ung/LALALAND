package com.example.luna.lalaland.All.BuySongs_Kakaopay;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.example.luna.lalaland.All.Utils.ApiClient;
import com.example.luna.lalaland.All.Utils.ApiService;
import com.example.luna.lalaland.All.Utils.ResponseModel;
import com.example.luna.lalaland.R;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
*
* BuySongsActivity에서 유저가 음원 결제를 요청한 후 결제준비까지 완료되면
* 이 액티비티에서 웹뷰-서버와의 통신을 통해 결제 확인, 결제 승인까지 전 과정을 처리한다.
*
* 카카오페이 API 공식홈페이지:
* https://developers.kakao.com/docs/restapi/kakaopay-api#단건결제-결제승인
*
* */
public class KakaoPayActivity extends AppCompatActivity {

    private WebView webView;
    private String kakaoAppUrl, kakaoScheme, kakaoMobileUrl, tid;
    int orderId;
    private WebSettings webSettings;
    private final static String TAG = KakaoPayActivity.class.getSimpleName();

    String username;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kakao_pay);

        //buysongsactivity에서 카카오 결제화면 url 받아오기
        Intent intent = getIntent();
        kakaoAppUrl = intent.getStringExtra("kakaoAppUrl");
        kakaoScheme = intent.getStringExtra("kakaoScheme"); //카카오 앱을 호출하기 위한 스킴
        kakaoMobileUrl = intent.getStringExtra("kakaoMobileUrl");
        tid = intent.getStringExtra("tid");
        orderId = intent.getIntExtra("orderId", 0);
        username = "username";



        Log.d(TAG, "카카오 app url 확인: "+kakaoAppUrl);
        Log.d(TAG, "카카오 mobile url 확인: "+kakaoMobileUrl);
        Log.d(TAG, "카카오 스킴 확인: "+kakaoScheme);
        Log.d(TAG, "카카오 tid 확인: "+tid);
        Log.d(TAG, "카카오 orderId 확인: "+orderId);

        //결제승인 페이지에 username, orderId와 tid값 보내기
        //proceedPayment(username, tid, orderId);

        //웹뷰 세팅
        webView = (WebView) findViewById(R.id.webView);
        webSettings = webView.getSettings();
        webView.setWebViewClient(new MyWebViewClient());
        webSettings.setJavaScriptEnabled(true);
        webView.loadUrl(kakaoAppUrl);


    }//onCreate


    //10/24 지금 당장은 필요없지만 나중을 대비하여 삭제 보류
    //서버의처리
    private void approvePayment(String signal) {

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseModel> call = apiService.approvePayment(signal);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {

                ResponseModel responseModel = response.body();
                if(responseModel != null) {
                    String msgFromServer = responseModel.getMessage();
                    Log.d(TAG, "카카오페이 승인 요청 후: "+msgFromServer);

                    if(responseModel.getStatus() == 1){
                        Log.d(TAG, "카카오 페이 결제 승인");
                        finish();

                    }//처리 성공

                }//if null
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.d(TAG, "카카오페이 결제 승인 요청 오류" +t.getMessage());

            }//onFailure
        });//Callback

    }//approvePayment

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        Log.d(TAG, "카카오3.5");
    }

    private class MyWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
         if(url.startsWith("intent:")) {
             Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(kakaoScheme));
             startActivity(intent);
             Log.d(TAG, "카카오1");
         } else {
             url = url;
             view.loadUrl(url);
             //approvePayment(tid, orderId);
             Log.d(TAG, "카카오2");
         }
            Log.d(TAG, "카카오3");
         return true;
        }//shouldOverrideUrlLoading
    }//WebViewClient

    // 4 5 1 3  2 3 4 5

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "카카오4");

    }//onResume


//    @Override //웹뷰 뒤로가기 버튼 활성화하지 않음 (재결제 등 불필요한 오류 발생 방지)
//    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if ((keyCode == KeyEvent.KEYCODE_BACK) && webView.canGoBack()) {
//            webView.goBack();
//            return true;
//        }
//        return super.onKeyDown(keyCode, event);
//    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}//KakaoPayActivity
