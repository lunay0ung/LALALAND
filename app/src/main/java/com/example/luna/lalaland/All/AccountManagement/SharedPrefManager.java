package com.example.luna.lalaland.All.AccountManagement;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.example.luna.lalaland.All.Intro.MainActivity;
import com.example.luna.lalaland.All.Utils.ApiClient;
import com.example.luna.lalaland.All.Utils.ApiService;
import com.example.luna.lalaland.All.Utils.ResponseModel;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/*
* 로그인한 유저 정보를 저장하는 sharedPreference
*
* */

public class SharedPrefManager {

    private static final String TAG= "매니저SharedPrefManager";
    //수집하는 유저정보는 이메일과 유저네임
    public static final String SHARED_PREF_NAME = "userInfo";
    public static final String KEY_EMAIL = "email";
    public static final String KEY_USERNAME = "username";
    public static final String KEY_GENDER = "gender";
    public static final String KEY_AGE = "age";

    private static SharedPrefManager mInstance;
    private static Context mCtx;

    private SharedPrefManager(Context context) {
        mCtx = context;
    }

    public static synchronized  SharedPrefManager getmInstance(Context context) {
        if (mInstance == null) {
            mInstance = new SharedPrefManager(context);
        }

        return mInstance;
    }

    //유저 로그인을 위한 함수
    //여기에 쉐어드를 통해 유저 데이터를 저장함
    public void userLogin(User user) {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        Log.d(TAG, "로그인 데이터 저장");
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(KEY_EMAIL, user.getEmail());   //유저 이메일.
        editor.putString(KEY_USERNAME, user.getUsername()); //유저 네임
        editor.putString(KEY_GENDER, user.getGender()); //성별
        editor.putString(KEY_AGE, user.getAge()); //나이
        editor.apply();
    }

    //유저가 로그인 했는지 안 했는지 체크한당
    public boolean isLoggedIn() {
        Log.d(TAG, "로그인 여부 체크");
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return sharedPreferences.getString(KEY_EMAIL, null) != null;
    }

    //로그인된 유저 반환
    public User getUser() {
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return new User(
                sharedPreferences.getString(KEY_USERNAME, null),
                sharedPreferences.getString(KEY_EMAIL, null),
                sharedPreferences.getString(KEY_GENDER, null),
                sharedPreferences.getString(KEY_AGE, null)
        );
    }


    //유저 로그아웃 시키기
    public void logout(String email) {
        Log.d(TAG, "로그아웃");
        SharedPreferences sharedPreferences = mCtx.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();
        Toast.makeText(mCtx, "로그아웃합니다.", Toast.LENGTH_SHORT).show();

        //로그아웃했음을 웹서버로 보내서 DB에 있는 계정 정보 업데이트
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        Call<ResponseModel> call = apiService.singOut(email);
        call.enqueue(new Callback<ResponseModel>() {
            @Override
            public void onResponse(Call<ResponseModel> call, Response<ResponseModel> response) {
                ResponseModel responseModel = response.body();
                String msgFromServer = responseModel.getMessage();
                Log.d(TAG, "로그아웃 후 서버에서 보낸 메시지: "+msgFromServer);

                if(responseModel.getStatus() == 1) {
                    Log.d(TAG, "로그아웃한 룸아이디: "+email);
                } else {
                    Log.d(TAG, "로그아웃 오류");
                }
            }//onResponse

            @Override
            public void onFailure(Call<ResponseModel> call, Throwable t) {
                Log.d(TAG, "로그아웃 후 추가 서버메시지" +t.getMessage());
            }//onFailure
        });


        /*로그아웃 후에는 회원가입 혹은 로그인을 선택할 수 있는 액티비티로 이동*/
        mCtx.startActivity(new Intent(mCtx, MainActivity.class));
    }


}//SharedPrefManager
