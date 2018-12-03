package com.example.luna.lalaland.All.Utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by LUNA on 2018-09-27.
 * 이 프로젝트에서 쓰이는 URL과 초기화된 레트로핏 저장
 */


public class ApiClient {


    //lalaland 서버 기본 url
    public static final String BASE_URL = "http://13.124.23.131/lalaland/";

    public static Retrofit RETROFIT = null;

    public static Retrofit getClient() {

        if(RETROFIT == null) {

            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new LoggingInterceptor())
                    .build();
//
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            RETROFIT = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .client(client)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

        }//if

        return RETROFIT;
    }//getClient


}//ApiClient
