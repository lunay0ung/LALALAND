package com.example.luna.lalaland.All.Utils;


import com.example.luna.lalaland.All.AccountManagement.ListUserInfoModel;
import com.example.luna.lalaland.All.Broadcaster.ListBroadcastModel;
import com.example.luna.lalaland.All.Trending.ListStatsModel;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;

/**
 * Created by LUNA on 2018-09-27.
 */

/*
* 서버와 통신할 때 필요한 인터페이스
* */

public interface ApiService {


    //방송 목록 관련
    @FormUrlEncoded
    @POST("broadcast/saveBroadcastList.php") //방금 시작한 라이브 방송 정보를 서버로 보냄 + 끝난 라이브 방송 정보의 라이브 여부 isLive = false로 수정
    Call<ResponseModel> startLive (
        @Field("streamer") String streamer, //스트리머 이름
        @Field("title") String title,      //방제
        @Field("roomId") int roomId, //룸아이디
        @Field("isLive") Boolean isLive,
        @Field("genre") String genre //장르
    );

    @FormUrlEncoded
    @POST("broadcast/saveBroadcastList.php") //끝난 라이브 방송 정보의 라이브 여부 isLive = false로 수정
    Call<ResponseModel> endLive (
            @Field("roomId") int roomId, //룸아이디
            @Field("isLive") Boolean isLive,
            @Field("thumbnail") String thumbnail //썸네일 주소
    );

    @FormUrlEncoded
    @POST("broadcast/getBroadcastInfo.php") //데이터베이스에서 현재 스트리밍 중인 방송제목 가져오기
    Call<ListBroadcastModel> getBroadcastInfo(
            @Field("temporary") String temporary //임시로 넣어둠
    );

    @FormUrlEncoded
    @POST("broadcast/setBroadcastStatistics.php")
    Call<ResponseModel> checkStatistics ( //현재 라이브 방송을 시청 중인 시청자수가 몇 명인지 / 녹화영상의 조회수가 몇인지 저장
         @Field("roomId") int roomId, //룸아이디
         @Field("request") String request //서버에 보낼 요청
    );

    @FormUrlEncoded
    @POST("broadcast/getViewers.php") //데이터베이스에서 현재 스트리밍 중인 방송의 시청자수 가져오기
    Call<ListBroadcastModel> getViewers(
            @Field("roomId") int roomId
    );

    //음원 결제 관련
    @FormUrlEncoded
    @POST("kakaopay/buySongs.php") //해당 음원 정보 보내기
    Call<ResponseModel> buySongs(
            @Field("username") String username,
            @Field("title") String title,
            @Field("artist") String artist,
            @Field("price") int price,
            @Field("orderId") int orderId
    );

    @FormUrlEncoded
    @POST("kakaopay/saveLedgerInfo.php") //음원 결제 정보  보내기
    Call<ResponseModel> saveLedgerInfo(
            @Field("username") String username,
            @Field("tid") String tid,
            @Field("orderId") int orderId
    );

    @FormUrlEncoded
    @POST("kakaopay/paymentSuccess.php") //서버에서 결제승인 처리상황이 어떻게 되어가고 있는지 알아보자
    Call<ResponseModel> approvePayment(
            @Field("signal") String signal
    );


    //추천영상 메뉴 관련
    @FormUrlEncoded
    @POST("trending/sendStats.php")  //유저의 시청 목록을 보냄
    Call<ResponseModel> sendStats(
            @Field("roomId") int roomId,
            @Field("genre") String genre,
            @Field("title") String title,
            @Field("username") String username,
            @Field("email") String email,
            @Field("gender") String gender,
            @Field("age") String age
    );

    //추천영상 메뉴 관련
    @FormUrlEncoded
    @POST("trending/getStats.php")  //유저 나이대+성별에서 가장 인기있는 장르 5개를 가져옴
    Call<ListStatsModel> getStats(
            @Field("gender") String gender,
            @Field("age") String age
    );


    @FormUrlEncoded
    @POST("trending/getTopGenre.php") //유저 나이대+성별에서 가장 인기있는 장르의 전체 리스트를 가져옴
    Call<ListBroadcastModel> getTopGenre(
            @Field("genre") String genre
    );


    //회원정보 관리
    @FormUrlEncoded
    @POST("account_management/signup.php")  //회원가입
    Call<ResponseModel> signUp (            //유저네임, 이메일, 비밀번호를 서버로 보냄 
      @Field("username") String username,
      @Field("email") String email,
      @Field("password") String password,
      @Field("age") String age,
      @Field("gender") String gender
    );

    @FormUrlEncoded
    @POST("account_management/signin.php")  //로그인
    Call<ResponseModel> signIn (            //이메일, 비밀번호를 서버로 보냄
        @Field("email") String email,
        @Field("password") String password
    );

    @FormUrlEncoded
    @POST("account_management/signin.php")  //로그인 //이메일, 비밀번호를 서버로 보냄
        Call<ListUserInfoModel> signInAndGetUserInfo (
        @Field("email") String email,
        @Field("password") String password
    );

    @FormUrlEncoded
    @POST("account_management/signout.php")  //로그아웃 -이메일 정보만 보냄
    Call<ResponseModel> singOut (
            @Field("email") String email
    );



    @FormUrlEncoded
    @POST("account_management/setProfilePhoto.php") //회원정보 수정 시 유저가 등록한 프로필 이미지를 서버로 보낸다
    Call<ResponseModel> setProfilePhoto(
            @Field("username") String username, //문의작성자 정보를 보냄
            @Field("email") String email,
            @Field("photo") String photo,
            @Field("order") String order
    );

    @FormUrlEncoded
    @POST("account_management/setProfilePhoto.php") //유저가 등록한 프로필 이미지를 가져온다
    Call<ResponseModel> getProfilePhoto(
            @Field("username") String username, //문의작성자 정보를 보냄
            @Field("email") String email,
            @Field("photo") String photo,
            @Field("order") String get
    );


    //회원정보 수정 시 서버로 보낼 유저정보
    @FormUrlEncoded
    @POST("account_management/updateInfo.php")
    Call<ResponseModel> updateInfo(
            @Field("newUsername") String newUsername,
            @Field("email") String email
    );

}//ApiService
