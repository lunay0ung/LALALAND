package com.example.luna.lalaland.All.Intro;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.luna.lalaland.All.AccountManagement.SharedPrefManager;
import com.example.luna.lalaland.All.AccountManagement.SignInActivity;
import com.example.luna.lalaland.All.AccountManagement.SignUpActivity;
import com.example.luna.lalaland.R;

import jnr.ffi.annotations.In;

/*
* 2018/9/19
* 앱을 실행시키면 처음으로 뜨는 화면
* => 회원가입/ 로그인 버튼을 배치하고 클릭 시 해당 과정을 진행하는 액티비티를 띄운다
* cf. UI/UX 디자인은 사운드 클라우드 참고
* */

/*
* TODO
* 로그인 여부 감지한 후 로그인 된 상태면 이 화면 finish
* */

public class MainActivity extends AppCompatActivity {

    private Button btn_signup, btn_signin;
    private static final String TAG = "메인 액티비티, 제일 처음 화면";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //로그인 정보가 저장돼 있으면 바로 홈스크린으로 이동
        if(SharedPrefManager.getmInstance(this).isLoggedIn()) {
            Log.d(TAG, "로그인 되어 있음");
            finish();
            startActivity(new Intent(this, HomeScreenActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
        }

        btn_signup = findViewById(R.id.btn_signup);
        btn_signin = findViewById(R.id.btn_signin);

        btn_signup.setOnClickListener(mClickListener);
        btn_signin.setOnClickListener(mClickListener);

    }//onCreate

    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.btn_signup:
                    startActivity(new Intent(MainActivity.this, SignUpActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    btn_signup.setVisibility(View.INVISIBLE); //회원가입, 로그인 액티비티가 반투명하므로 일단 버튼을 누르면 새로운 액티비티에서는 이 버튼이 보이지 않게 처리함
                    btn_signin.setVisibility(View.INVISIBLE);
                    break;

                case R.id.btn_signin:
                    startActivity(new Intent(MainActivity.this, SignInActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    btn_signup.setVisibility(View.INVISIBLE);
                    btn_signin.setVisibility(View.INVISIBLE);
                    break;
            }//switch
        }//onClick
    };//OnClickListener

    //다시 이 액티비티로 돌아왔을 때 이전에 invisible하게 만들었던 버튼을 보이게 해줘야 한다
    @Override
    protected void onResume() {
        super.onResume();
        btn_signup.setVisibility(View.VISIBLE);
        btn_signin.setVisibility(View.VISIBLE);
    }
}//MainActivity
