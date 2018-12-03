package com.example.luna.lalaland.All.AccountManagement;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.luna.lalaland.All.Intro.HomeScreenActivity;
import com.example.luna.lalaland.All.Utils.ApiClient;
import com.example.luna.lalaland.All.Utils.ApiService;
import com.example.luna.lalaland.All.Utils.ResponseModel;
import com.example.luna.lalaland.R;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/*
* 2018-09-20
* 일단 로그인 화면만 구현해둠 -> 아직 회원가입도 구현이 안 되어있으므로 로그인 버튼을 누르면 바로 메뉴화면으로 이동
*
* 2018-11-05
* 로그인 구현
* TODO 로그인 후 sharedpreference에 유저 정보 담기
*
* */

public class SignInActivity extends AppCompatActivity {

    private static final String TAG = "로그인 액티비티";
    Button btn_signinWithGoogleAccount, btn_cancel, btn_okay, btn_forgotPassword;
    EditText et_email, et_password;
    String username, emailToSend, password, gender, age, recievedEmail;


    SharedPreferences sharedPreferences;

    List<User> userInfoList;
    Boolean canSignIn = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        userInfoList = new ArrayList<>(); //유저 정보를 담을 리스트

        //뷰 초기화
        btn_signinWithGoogleAccount = findViewById(R.id.btn_signinWithGoogleAccount);
        btn_cancel = findViewById(R.id.btn_cancel);
        btn_okay = findViewById(R.id.btn_okay);
        btn_forgotPassword = findViewById(R.id.btn_forgotPassword);

        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);

        /*
        * 로그인 하는 경우의 수 2가지
        * 1) 회원가입 후 바로 로그인 --> 유저 정보 sharedpreferences에 담아서 자동 로그인 시키기
        * 2) 일반 로그인  --> 로그인 성공 시 유저 정보 받아와서 sharedpreferences에 담은 후 자동 로그인 시키기
        * */

        /*
        * 1) 회원 가입 후 바로 로그인 하는 경우
        *   회원가입 시 입력한 이메일 주소 받아오기
        * */
        Intent intent = getIntent();

        emailToSend = intent.getStringExtra("emailToSend");
        gender = intent.getStringExtra("gender");
        age = intent.getStringExtra("age");
        Log.d(TAG, "받아온 이메일: "+ emailToSend);
        Log.d(TAG, "받아온 성별: "+gender);
        Log.d(TAG, "받아온 나이: "+age);

        if(emailToSend != null) //받아온 정보가 있다면 여기에 띄우기
            et_email.setText(emailToSend);


        /*
        * 2) 일반 로그인 하는 경우
        * */

        //'비밀번호가 기억나지 않아요' 버튼에 있는 텍스트에 밑줄효과 주기
        btn_forgotPassword.setPaintFlags(btn_forgotPassword.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


        //버튼에 리스너 달아주기
        btn_signinWithGoogleAccount.setOnClickListener(mClickListener);
        btn_cancel.setOnClickListener(mClickListener);
        btn_okay.setOnClickListener(mClickListener);
        btn_forgotPassword.setOnClickListener(mClickListener);


    }//onCreate



    //일단 로그인 버튼 누르면 바로 메뉴를 볼 수 있는 액티비티로 이동하도록 처리(2018/9/20)
    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.btn_cancel:
                    Log.d(TAG, "로그인 취소");
                    finish();
                    break;

                case R.id.btn_okay:
                    emailToSend = et_email.getText().toString(); //이메일 받아오기
                    password = et_password.getText().toString(); //비밀번호 받아오기
                    Log.d(TAG, "로그인 버튼 누름");
                    Log.d(TAG, "로그인 처리할 서버에 보낼 이메일과 비번: "+ emailToSend + password);
                    checkEditText();
                    if(canSignIn)
                    signInAndGetUserInfo(emailToSend, password);
                    break;

            }//switch
        }//onClick
    };//OnClickListener

    /*
    * 로그인 입력 edit text 체크
    * */

    private void checkEditText() {
        if(TextUtils.isEmpty(emailToSend)) {
            Toast.makeText(this, "로그인 정보를 확인해주세요.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "이메일 입력 안 함");
        } else if(TextUtils.isEmpty(password)) {
            Toast.makeText(this, "로그인 정보를 확인해주세요.", Toast.LENGTH_SHORT).show();
            Log.d(TAG, "비번 입력 안 함");
        } else {
            canSignIn = true;
            Log.d(TAG, "로그인 가능");
        }

    }//checkEditText

    /*
    * 유저 이메일과 비밀번호를 서버로 보내서 로그인 처리를 한 후
    * 로그인 한 유저 정보를 받아온다
    * */
    private void signInAndGetUserInfo(String email, String password){

        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ListUserInfoModel> call = apiService.signInAndGetUserInfo(email, password);
        call.enqueue(new Callback<ListUserInfoModel>() {
            @Override
            public void onResponse(Call<ListUserInfoModel> call, Response<ListUserInfoModel> response) {
                ListUserInfoModel listUserInfoModel = response.body();

                if(listUserInfoModel.getStatus() == 1) {
                    Log.d(TAG, "로그인 후 유저 정보 불러오기 성공");

                    recievedEmail = listUserInfoModel.getEmail();
                    gender = listUserInfoModel.getGender();
                    username = listUserInfoModel.getUsername();
                    age = listUserInfoModel.getAge();

                    User user= new User(username, recievedEmail, gender, age);
                    Log.d(TAG, "로그인 테스트: "+user.gender);

                    //savePreferences(username, email, gender, age);
                    //유저 정보를 shared preferences에 저장
                    SharedPrefManager.getmInstance(getApplicationContext()).userLogin(user);
                    Log.d(TAG, "shared에 저장할 유저네임: "+user.username);
                    Log.d(TAG, "shared에 저장할 이메일: "+user.email);
                    Log.d(TAG, "shared에 저장할 성별: "+user.gender);
                    Log.d(TAG, "shared에 저장할 나이: "+user.age);

                    startActivity(new Intent(SignInActivity.this, HomeScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    finish();

                } else {
                    Log.d(TAG, "로그인 오류");
                }
            }//onResponse

            @Override
            public void onFailure(Call<ListUserInfoModel> call, Throwable t) {
                Log.d(TAG, "로그인 실패");
            }//onFailure
        });//Callback
    }//signInAndGetUserInfo


    // SharedPreferences에 유저 정보 저장하기
    private void savePreferences(String username, String email, String gender, String age){
        SharedPreferences pref = getSharedPreferences("userInfo", MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString("username", username);
        editor.putString("emailToSend", email);
        editor.putString("gender", gender);
        editor.putString("age", age);
        Log.d(TAG, "유저 정보 저장함");
        editor.commit();
    }

    //액티비티 종료 시
    @Override
    public void finish() {
        super.finish();
        //애니메이션 없앰
        overridePendingTransition(0, 0);
    }


}//SignInActivity
