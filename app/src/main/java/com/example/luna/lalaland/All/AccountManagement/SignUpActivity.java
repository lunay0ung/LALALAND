package com.example.luna.lalaland.All.AccountManagement;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.luna.lalaland.R;

/*
* 2018-09-19
* 메인화면의 회원가입 버튼을 누르면 이 액티비티를 띄운다.
* 구글 계정으로 로그인하거나 일반 회원가입이 가능하도록 만들 예정인데
* 일단은 버튼만 생성해둠
*
* 2018-09-30
* '구글 계정으로 로그인'과 '다른 이메일 계정으로 가입' 두 가지 버튼이 있음.
* 각 버튼명에 맞게 처리할 것
* */
public class SignUpActivity extends AppCompatActivity {

    private Button btn_createAnAccount, btn_signinWithGoogleAccount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        initViews(); //객체 초기화

        //버튼에 클릭 리스너 달아주기
        btn_createAnAccount.setOnClickListener(mClickListener);
    }//onCreate


    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_signinWithGoogleAccount: //구글 로그인

                    break;

                case R.id.btn_createAnAccount: //다른 계정으로 로그인
                    startActivity(new Intent(SignUpActivity.this, CreateAnAccountActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION));
                    btn_signinWithGoogleAccount.setVisibility(View.GONE);
                    btn_createAnAccount.setVisibility(View.GONE);
                break;
            }
        }//onClick
    };//OnClickListener

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
        //회원가입 정보 기입창으로 이동할 때 Gone시켰던 버튼들 다시 보이게 하기
        btn_createAnAccount.setVisibility(View.VISIBLE);
        btn_signinWithGoogleAccount.setVisibility(View.VISIBLE);
    }//

    //객체 초기화
    private void initViews(){
        btn_createAnAccount = findViewById(R.id.btn_createAnAccount);
        btn_signinWithGoogleAccount = findViewById(R.id.btn_signinWithGoogleAccount);
    }//initViews

}//SignUpActivity

