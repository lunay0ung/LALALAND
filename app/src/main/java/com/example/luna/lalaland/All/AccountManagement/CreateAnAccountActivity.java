package com.example.luna.lalaland.All.AccountManagement;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telecom.Call;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.luna.lalaland.All.Intro.HomeScreenActivity;
import com.example.luna.lalaland.All.Utils.ApiClient;
import com.example.luna.lalaland.All.Utils.ApiService;
import com.example.luna.lalaland.All.Utils.ResponseModel;
import com.example.luna.lalaland.R;

import java.util.ArrayList;
import java.util.List;

import jnr.ffi.annotations.In;
import retrofit2.Callback;
import retrofit2.Response;

public class CreateAnAccountActivity extends AppCompatActivity {

    private static final String TAG = "회원가입 액티비티";
    EditText et_username, et_email, et_password, et_passwordCheck, et_age, et_gender, et_genderCustom; //회원가입 시 필요한 이메일, 비밀번호, 나이, 성별 정보입력칸
    Button btn_cancel, btn_okay; //회원가입 취소, 확정 버튼
    String username, email, password, passwordCheck, gender, age, genderCustomed;
    InputMethodManager manager; //키보드 제어 위한 키보드매니저
    int passwordLength; //et_password에 입력된 글자수를 받아오기 위함
    Boolean finishProcess = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_an_account);

        initViews();//객체 초기화

        //버튼에 클릭 리스너 달아주기
        btn_cancel.setOnClickListener(mClickListener);
        btn_okay.setOnClickListener(mClickListener);
        et_gender.setOnTouchListener(mTouchListner);



    }//onCreate


    View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.btn_cancel: //회원가입 취소
                        finish();
                    break;

                case R.id.btn_okay: //회원가입 확정
                    //이메일, 비밀번호, 나이, 성별 모든 칸이 채워져야 함. 하나라도 비어있으면 회원가입이 진행되지 않도록 한다.
                    //String gender는 selectGender()에서 처리
                    username = et_username.getText().toString();
                    email = et_email.getText().toString();
                    password = et_password.getText().toString();
                    passwordLength = et_password.getText().length();
                    passwordCheck = et_passwordCheck.getText().toString();
                    age = et_age.getText().toString();
                    gender = et_gender.getText().toString();
                    genderCustomed= et_genderCustom.getText().toString(); //성별을 직접 쓸 경우
                    Log.d(TAG, "확인버튼 눌렀을 때 성별: "+gender);
                    checkEditText(); //모든 정보가 기입돼있는지 체크
                    if(finishProcess) {

                        if(gender.equals("남자"))
                        gender = "M";
                        if(gender.equals("여자"))
                        gender = "F";
                        Log.d(TAG, "회원가입 서버로 보낼 정보 이메일: "+email);
                        Log.d(TAG, "회원가입 서버로 보낼 정보 성별: "+gender);
                        Log.d(TAG, "회원가입 서버로 보낼 정보 나이: "+age);
                        startSigningUp(); //기입된 정보를 서버로 보내서 회원가입 처리 시작
                    }
                    break;
            }//switch
        }//onClick
    };//OnClickListener


    private void startSigningUp(){ //모든 정보가 기입됐을 때 회원가입을 시작하는 메소드
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        retrofit2.Call<ResponseModel> call = apiService.signUp(username, email, password, age, gender);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(retrofit2.Call<ResponseModel> call, Response<ResponseModel> response) {
                ResponseModel responseModel = response.body();
                String msgFromServer = responseModel.getMessage();
                Log.d(TAG, "회원가입 후 서버에서 보낸 메시지: "+msgFromServer);

                if(responseModel.getStatus() == 1) {
                    Log.d(TAG, "회원가입 성공");
                    Toast.makeText(CreateAnAccountActivity.this, "회원가입이 완료되었습니다.", Toast.LENGTH_SHORT).show();
                    /*여기서 입력한 이메일 가져가서 로그인 액티비티 '이메일' 칸에 자동 입력해주기*/

                    Intent intent = new Intent(CreateAnAccountActivity.this, SignInActivity.class).addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    intent.putExtra("emailToSend", email);
                    intent.putExtra("gender", gender);
                    intent.putExtra("age", age);
                    Log.d(TAG, "로그인 화면에 보내는 유저 정보: "+email + age + gender);
                    startActivity(intent);
                    finish();
                } else {
                    Log.d(TAG, "회원가입 오류");
                }
            }//onResponse

            @Override
            public void onFailure(retrofit2.Call<ResponseModel> call, Throwable t) {
                Log.d(TAG, "회원가입 실패" +t.getMessage());
            }//onFailure
        });//signUp

    }//startSigningUp


    View.OnTouchListener mTouchListner = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {

            switch (event.getAction()) {

                //터치된 상태
                case MotionEvent.ACTION_DOWN:
                    hideSoftKeyboard(et_gender);
                    selectGender(); //성별 선택 라디오버튼이 있는 다이얼로그 띄우기
                    break;

                case MotionEvent.ACTION_CANCEL:
                //터치 안 된 상태
                case MotionEvent.ACTION_UP:
            }//switch
            return false;
        }//onTouch
    };//OnTouchListener

    private void selectGender() {
        final List<String> genderList = new ArrayList<>(); //가능한 성별 선택지
        genderList.add("여자");
        genderList.add("남자");
        /*
        * 성별을 직접 쓰거나 비공개하는 옵션을 두는 게 좋다고 생각하지만
        * 예외처리 + 로그 분석 등을 생각했을 때 보류하는 게 좋다고 판단
        * */
       // genderList.add("직접 쓰기");
       // genderList.add("비공개");
        final CharSequence[] items =  genderList.toArray(new String[ genderList.size()]);

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
                            gender = genderList.get(index);

                            if(gender.equals("남자")) {
                                et_gender.setText("남자");
                                //gender = "M";
                                Log.d(TAG, "남자 선택");
                            }
                            if(gender.equals("여자")) {
                                et_gender.setText("여자");
                                //gender = "F";
                                Log.d(TAG, "여자 선택");
                            }
                            Log.d(TAG, "selectedGender / 선택된 성별: "+gender);

                                /*
                                * 성별을 직접 쓰거나 비공개하는 옵션을 두는 게 좋다고 생각하지만
                                * 예외처리 + 로그 분석 등을 생각했을 때 보류하는 게 좋다고 판단
                                * */
//                            if(gender.equals("직접 쓰기")) { //직접 쓰기를 선택했을 때
//                                et_gender.getText().clear();//기존에 입력된 것이 있다면 지워줌
//                                et_genderCustom.getText().clear();//기존에 입력된 것이 있다면 지워줌
//                                gender = "";
//                                et_gender.setVisibility(View.INVISIBLE);
//                                et_genderCustom.setVisibility(View.VISIBLE);
//                                gender = et_genderCustom.getText().toString(); //직접쓰기 칸에 입력된 정보값 gender에 담기
//                                Log.d(TAG, "직접 쓴 성별: "+gender);
//                            } else {
//                                et_gender.setText(gender); //"직접 쓰기"외에 선택된 성별정보 et_gender에 입력
//                                gender = et_gender.getText().toString();
//                                Log.d(TAG, "선택된 성별: "+gender);
//                            }//else
                        }//if (!SelectedItems.isEmpty())
                        Toast.makeText(getApplicationContext(),
                                "Items Selected.\n"+ gender , Toast.LENGTH_LONG)
                                .show();
                    }
                });
        builder.setNegativeButton("취소",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
        builder.show();

    }//selectGender

    /*예외처리 메소드*/
    //-이메일, 비밀번호, 나이, 성별 칸이 모두 채워져있는지 체크
    //-비밀번호 일치 여부 확인
    //-나이 항목의 경우 10000이나 1390 같은 값을 예외처리해야하지만 일정상 생략하고 빈칸 체크만 하고 넘어간다
    private void checkEditText(){


        Log.d(TAG, "유저네임: "+username);
        Log.d(TAG, "선택한 성별: "+gender);
        Log.d(TAG, "이메일: "+email);
        Log.d(TAG, "비밀번호: "+password);
        Log.d(TAG, "비밀번호 확인: "+passwordCheck);
        Log.d(TAG, "나이: "+age);


        if(TextUtils.isEmpty(username)) {
            Toast.makeText(this, "사용할 닉네임을 입력해주세요.", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(email)) {
            Toast.makeText(this, "이메일을 입력해주세요.", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(password)){
            Toast.makeText(this, "비밀번호를 입력해주세요.", Toast.LENGTH_SHORT).show();
        } else if(!TextUtils.isEmpty(password) && passwordLength < 6) {
            Toast.makeText(this, "비밀번호는 6자 이상 입력해주세요.", Toast.LENGTH_SHORT).show();
            //et_password.setText("");
        }

        if(!password.equals(passwordCheck)) {
            Toast.makeText(this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
        }


        if(TextUtils.isEmpty(age)) {
            Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
        }

        if(TextUtils.isEmpty(gender)){
            Toast.makeText(this, "모든 정보를 입력해주세요.", Toast.LENGTH_SHORT).show();
        }


        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password) && !TextUtils.isEmpty(passwordCheck) &&  !TextUtils.isEmpty(age) && !TextUtils.isEmpty(gender)) {
                Toast.makeText(this, "회원가입을 진행합니다.", Toast.LENGTH_SHORT).show();
                finishProcess = true;

        }

        Log.d(TAG, "가입진행여부: "+finishProcess);
    }//checkEmptyEditText

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
    }//finish

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG,"다시 진입");
        et_gender.setText("");
        et_genderCustom.setText("");
        gender ="";
        Log.d(TAG, "액티비티 재진입 시 성별정보: "+gender);
    }//

    private void initViews() { //객체초기화

        et_username = findViewById(R.id.et_username);
        et_email = findViewById(R.id.et_email);
        et_password = findViewById(R.id.et_password);
        et_passwordCheck = findViewById(R.id.et_passwordCheck);
        et_age = findViewById(R.id.et_age);
        et_gender = findViewById(R.id.et_gender);
        et_genderCustom = findViewById(R.id.et_genderCustom);

        btn_cancel = findViewById(R.id.btn_cancel);
        btn_okay = findViewById(R.id.btn_okay);

        manager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE); //키보드 매니저
    }//initViews

}//CreateAnAccountActivity
